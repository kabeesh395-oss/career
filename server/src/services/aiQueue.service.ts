import { db } from '../db/database.js';
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

class AiQueueManager {
  private queue: Map<string, AiJob> = new Map();
  private processing: boolean = false;

  constructor() {
    // Process queue every 500ms
    setInterval(() => this.processNext(), 500);
  }

  /**
   * Enqueue an AI job asynchronously with deduplication
   */
  public enqueue(
    userId: string,
    jobType: 'ATS_ANALYSIS' | 'ROADMAP_GEN' | 'INTERVIEW_EVAL',
    payload: any,
    idempotencyKey?: string
  ): AiJob {
    const key = idempotencyKey || `${userId}_${jobType}_${JSON.stringify(payload)}`;
    
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
    return job;
  }

  /**
   * Get job status and result
   */
  public getJob(jobId: string): AiJob | undefined {
    return this.queue.get(jobId);
  }

  /**
   * Process next queued job in background
   */
  private async processNext() {
    if (this.processing) return;
    
    const pendingJob = Array.from(this.queue.values()).find(j => j.status === 'QUEUED' || j.status === 'RETRYING');
    if (!pendingJob) return;

    this.processing = true;
    pendingJob.status = 'PROCESSING';
    pendingJob.attempts += 1;
    pendingJob.updatedAt = Date.now();

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
    } catch (err: any) {
      if (pendingJob.attempts < pendingJob.maxAttempts) {
        pendingJob.status = 'RETRYING';
        pendingJob.errorMessage = `Retry attempt ${pendingJob.attempts}/${pendingJob.maxAttempts}: ${err.message}`;
      } else {
        pendingJob.status = 'FAILED';
        pendingJob.errorMessage = err.message || 'AI Job Processing Failed';
      }
      pendingJob.updatedAt = Date.now();
    } finally {
      this.processing = false;
    }
  }
}

export const aiQueueManager = new AiQueueManager();
