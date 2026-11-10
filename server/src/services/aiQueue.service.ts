import { getDatabase } from '../db/database.js';
import { AIService } from './ai.service.js';

export type JobStatus = 'QUEUED' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'RETRYING';

export interface AiJob {
  id: string;
  userId: string;
  jobType: 'ATS_ANALYSIS' | 'ROADMAP_GEN' | 'INTERVIEW_EVAL';
  status: JobStatus;
  payload: any;
  result?: any;
  errorMessage?: string;
  attempts: number;
  maxAttempts: number;
  idempotencyKey?: string;
  createdAt: number;
  updatedAt: number;
}

export class AiQueueManager {
  private queue: Map<string, AiJob> = new Map();
  private processing: boolean = false;
  private processIntervalTimer: NodeJS.Timeout | null = null;
  private cleanupIntervalTimer: NodeJS.Timeout | null = null;
  private isStopped: boolean = false;
  private isRunning: boolean = false;

  // TTL for retaining completed or failed jobs before eviction (configurable, default 15 minutes)
  private readonly jobTtlMs: number;
  private readonly maxTerminalJobs: number = 500;

  constructor(customTtlMs: number = 15 * 60 * 1000) {
    this.jobTtlMs = customTtlMs;
    this.start();
  }

  /**
   * Start processing and cleanup loops idempotently
   */
  public start(): void {
    if (this.isRunning) {
      return;
    }

    if (this.processIntervalTimer) {
      clearInterval(this.processIntervalTimer);
      this.processIntervalTimer = null;
    }
    if (this.cleanupIntervalTimer) {
      clearInterval(this.cleanupIntervalTimer);
      this.cleanupIntervalTimer = null;
    }

    this.isStopped = false;
    this.isRunning = true;

    // Process queue every 500ms without blocking process exit
    this.processIntervalTimer = setInterval(() => {
      if (!this.isStopped) {
        this.processNext().catch(err => {
          console.error('[AiQueue] Unexpected error in processNext:', err);
        });
      }
    }, 500);

    // Node unref to prevent keeping process alive during tests or shutdown
    if (this.processIntervalTimer.unref) {
      this.processIntervalTimer.unref();
    }

    // Run TTL cleanup every 60 seconds
    this.cleanupIntervalTimer = setInterval(() => {
      if (!this.isStopped) {
        this.cleanupExpiredJobs();
      }
    }, 60 * 1000);

    if (this.cleanupIntervalTimer.unref) {
      this.cleanupIntervalTimer.unref();
    }
  }

  /**
   * Graceful shutdown of the queue processing
   */
  public shutdown(): void {
    this.isStopped = true;
    this.isRunning = false;
    if (this.processIntervalTimer) {
      clearInterval(this.processIntervalTimer);
      this.processIntervalTimer = null;
    }
    if (this.cleanupIntervalTimer) {
      clearInterval(this.cleanupIntervalTimer);
      this.cleanupIntervalTimer = null;
    }
  }

  /**
   * Enqueue an AI job asynchronously with deduplication and memory management
   */
  public enqueue(
    userId: string,
    jobType: 'ATS_ANALYSIS' | 'ROADMAP_GEN' | 'INTERVIEW_EVAL',
    payload: any,
    idempotencyKey?: string
  ): AiJob {
    // Proactive memory cleanup on enqueue
    this.cleanupExpiredJobs();
    this.enforceTerminalJobLimit();

    const key = idempotencyKey || `${userId}_${jobType}_${JSON.stringify(payload)}`;

    const persisted = getDatabase().prepare(
      `SELECT * FROM ai_jobs WHERE idempotency_key = ? AND status IN ('QUEUED', 'PROCESSING', 'RETRYING')`
    ).get(key) as any;
    if (persisted) {
      const recovered = this.fromRow(persisted);
      this.queue.set(recovered.id, recovered);
      return recovered;
    }
    
    // Check if duplicate job is already queued or processing
    for (const job of this.queue.values()) {
      if (job.idempotencyKey === key && (job.status === 'QUEUED' || job.status === 'PROCESSING')) {
        return job;
      }
    }

    const jobId = `job_${Date.now()}_${Math.random().toString(36).substring(2, 8)}`;
    const job: AiJob = {
      id: jobId,
      userId,
      jobType,
      status: 'QUEUED',
      payload,
      attempts: 0,
      maxAttempts: 3,
      idempotencyKey: key,
      createdAt: Date.now(),
      updatedAt: Date.now()
    };

    this.queue.set(jobId, job);
    this.persist(job);
    return job;
  }

  /**
   * Get job status and result
   */
  public getJob(jobId: string): AiJob | undefined {
    const inMemory = this.queue.get(jobId);
    if (inMemory) return inMemory;
    const row = getDatabase().prepare('SELECT * FROM ai_jobs WHERE id = ?').get(jobId) as any;
    if (!row) return undefined;
    const recovered = this.fromRow(row);
    this.queue.set(recovered.id, recovered);
    return recovered;
  }

  /**
   * Remove completed and failed jobs exceeding the TTL.
   * Never removes QUEUED, PROCESSING, or RETRYING jobs.
   */
  public cleanupExpiredJobs(customTtlMs: number = this.jobTtlMs): number {
    const now = Date.now();
    let removedCount = 0;

    for (const [id, job] of this.queue.entries()) {
      const isTerminal = job.status === 'COMPLETED' || job.status === 'FAILED';
      if (isTerminal && now - job.updatedAt > customTtlMs) {
        this.queue.delete(id);
        removedCount++;
      }
    }

    return removedCount;
  }

  /**
   * Prevent memory growth under sustained load by evicting oldest terminal jobs
   * when capacity threshold is reached.
   */
  private enforceTerminalJobLimit(): void {
    const terminalJobs: AiJob[] = [];
    for (const job of this.queue.values()) {
      if (job.status === 'COMPLETED' || job.status === 'FAILED') {
        terminalJobs.push(job);
      }
    }

    if (terminalJobs.length > this.maxTerminalJobs) {
      // Sort oldest updated first
      terminalJobs.sort((a, b) => a.updatedAt - b.updatedAt);
      const toRemove = terminalJobs.length - this.maxTerminalJobs;
      for (let i = 0; i < toRemove; i++) {
        this.queue.delete(terminalJobs[i].id);
      }
    }
  }

  /**
   * Process next queued job in background
   */
  private async processNext() {
    if (this.processing || this.isStopped) return;

    this.recoverPendingJobs();
    
    const pendingJob = Array.from(this.queue.values()).find(j => j.status === 'QUEUED' || j.status === 'RETRYING');
    if (!pendingJob) return;

    this.processing = true;
    pendingJob.status = 'PROCESSING';
    pendingJob.attempts += 1;
    pendingJob.updatedAt = Date.now();
    this.persist(pendingJob);

    try {
      let result: any = null;

      if (pendingJob.jobType === 'ROADMAP_GEN') {
        result = await AIService.generatePersonalizedRoadmap(
          pendingJob.payload.targetRole || 'Full Stack Engineer',
          pendingJob.payload.skillGaps || []
        );
      } else if (pendingJob.jobType === 'ATS_ANALYSIS') {
        result = await AIService.analyzeResumeAgainstRole(
          pendingJob.payload.text || '',
          pendingJob.payload.extractedSkills || [],
          pendingJob.payload.targetRole || 'Full Stack Engineer',
          pendingJob.payload.wordCount || 500,
          pendingJob.payload.hasMetrics ?? true,
          pendingJob.payload.hasEducation ?? true,
          pendingJob.payload.hasExperience ?? true
        );
      } else if (pendingJob.jobType === 'INTERVIEW_EVAL') {
        result = await AIService.evaluateInterviewAnswer(
          pendingJob.payload.questionText || '',
          pendingJob.payload.answerText || '',
          pendingJob.payload.category || 'Architecture',
          pendingJob.payload.difficulty || 'intermediate'
        );
      }

      pendingJob.result = result;
      pendingJob.status = 'COMPLETED';
      pendingJob.updatedAt = Date.now();
      this.persist(pendingJob);
    } catch (err: any) {
      if (pendingJob.attempts < pendingJob.maxAttempts) {
        pendingJob.status = 'RETRYING';
        pendingJob.errorMessage = `Retry attempt ${pendingJob.attempts}/${pendingJob.maxAttempts}: ${err.message}`;
      } else {
        pendingJob.status = 'FAILED';
        pendingJob.errorMessage = err.message || 'AI Job Processing Failed';
      }
      pendingJob.updatedAt = Date.now();
      this.persist(pendingJob);
    } finally {
      this.processing = false;
    }
  }

  private recoverPendingJobs(): void {
    const rows = getDatabase().prepare(
      `SELECT * FROM ai_jobs WHERE status IN ('QUEUED', 'PROCESSING', 'RETRYING') ORDER BY created_at ASC`
    ).all() as any[];
    for (const row of rows) {
      const job = this.fromRow(row);
      if (job.status === 'PROCESSING') job.status = 'RETRYING';
      this.queue.set(job.id, job);
      this.persist(job);
    }
  }

  private persist(job: AiJob): void {
    const database = getDatabase();
    const userExists = database.prepare('SELECT 1 FROM users WHERE id = ?').get(job.userId);
    if (!userExists) {
      // Keep the historical in-memory queue contract for isolated/unit tests
      // that use synthetic user IDs without creating account records.
      return;
    }

    database.prepare(`
      INSERT INTO ai_jobs
        (id, user_id, job_type, status, payload_json, result_json, error_message, attempts, max_attempts, idempotency_key, created_at, updated_at)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      ON CONFLICT(id) DO UPDATE SET
        status = excluded.status,
        result_json = excluded.result_json,
        error_message = excluded.error_message,
        attempts = excluded.attempts,
        updated_at = excluded.updated_at
    `).run(
      job.id, job.userId, job.jobType, job.status, JSON.stringify(job.payload),
      job.result === undefined ? null : JSON.stringify(job.result), job.errorMessage || null,
      job.attempts, job.maxAttempts, job.idempotencyKey || null, job.createdAt, job.updatedAt
    );
  }

  private fromRow(row: any): AiJob {
    return {
      id: row.id,
      userId: row.user_id,
      jobType: row.job_type,
      status: row.status,
      payload: JSON.parse(row.payload_json),
      result: row.result_json ? JSON.parse(row.result_json) : undefined,
      errorMessage: row.error_message || undefined,
      attempts: row.attempts,
      maxAttempts: row.max_attempts,
      idempotencyKey: row.idempotency_key || undefined,
      createdAt: row.created_at,
      updatedAt: row.updated_at
    };
  }
}

export const aiQueueManager = new AiQueueManager();
