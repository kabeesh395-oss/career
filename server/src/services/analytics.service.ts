import crypto from 'crypto';
import { getDatabase } from '../db/database.js';

export interface TelemetryEvent {
  eventName: string;
  eventData?: Record<string, any>;
  idempotencyKey?: string;
}

export class AnalyticsService {
  public static trackEvent(
    userId: string,
    eventName: string,
    eventData: Record<string, any> = {},
    idempotencyKey?: string
  ): void {
    const db = getDatabase();
    const eventId = `evt_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;
    const key = idempotencyKey || `${userId}_${eventName}_${Date.now()}`;

    try {
      const stmt = db.prepare(`
        INSERT OR IGNORE INTO analytics_events (id, user_id, event_name, event_data_json, timestamp, idempotency_key)
        VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)
      `);
      stmt.run(eventId, userId, eventName, JSON.stringify(eventData), key);
    } catch (err) {
      console.error('[Analytics] Failed to record event:', err);
    }
  }

  public static getUserDashboardAnalytics(userId: string) {
    const db = getDatabase();

    const profile = db.prepare('SELECT * FROM profiles WHERE user_id = ?').get(userId) as any;
    
    // Tasks Stats
    const tasksStat = db.prepare(`
      SELECT 
        COUNT(*) as total,
        SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed
      FROM roadmap_items 
      WHERE user_id = ?
    `).get(userId) as { total: number; completed: number };

    // Interview Stats
    const interviewStat = db.prepare(`
      SELECT 
        COUNT(*) as completed_count,
        AVG(overall_score) as avg_score
      FROM interviews 
      WHERE user_id = ? AND status = 'completed'
    `).get(userId) as { completed_count: number; avg_score: number | null };

    // Skills & Gaps
    const skillsStat = db.prepare(`
      SELECT COUNT(*) as count FROM user_skills WHERE user_id = ?
    `).get(userId) as { count: number };

    const gapsStat = db.prepare(`
      SELECT COUNT(*) as count FROM skill_gaps WHERE user_id = ?
    `).get(userId) as { count: number };

    // Projects
    const projectsStat = db.prepare(`
      SELECT 
        COUNT(*) as total,
        SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed
      FROM projects 
      WHERE user_id = ?
    `).get(userId) as { total: number; completed: number };

    // Resume score & total analyses
    const resumeCountStat = db.prepare(`
      SELECT COUNT(*) as count FROM resumes WHERE user_id = ?
    `).get(userId) as { count: number };

    const latestResume = db.prepare(`
      SELECT overall_score, impact_score, brevity_score, style_score, created_at 
      FROM resume_analysis 
      WHERE user_id = ? 
      ORDER BY created_at DESC LIMIT 1
    `).get(userId) as any;

    // Learning Activity Stats
    const learningStat = db.prepare(`
      SELECT 
        COUNT(*) as total_engaged,
        SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed_modules,
        SUM(CASE WHEN status = 'started' THEN 1 ELSE 0 END) as in_progress_modules
      FROM learning_progress 
      WHERE user_id = ?
    `).get(userId) as { total_engaged: number; completed_modules: number; in_progress_modules: number };

    // Recent activity events
    const recentEvents = db.prepare(`
      SELECT event_name, event_data_json, timestamp 
      FROM analytics_events 
      WHERE user_id = ? 
      ORDER BY timestamp DESC LIMIT 8
    `).all(userId) as Array<{ event_name: string; event_data_json: string; timestamp: string }>;

    // Check if user has sufficient real data
    const hasSufficientData = (
      (tasksStat.total > 0) ||
      (interviewStat.completed_count > 0) ||
      (skillsStat.count > 0) ||
      (resumeCountStat.count > 0) ||
      (learningStat.total_engaged > 0)
    );

    // Calculate genuine readiness score from real data
    let computedReadiness = 0;
    if (hasSufficientData && profile?.target_role) {
      const baseScore = profile.current_readiness_score || 25;
      const taskContribution = tasksStat.total > 0 ? (tasksStat.completed / tasksStat.total) * 25 : 0;
      const interviewContribution = interviewStat.completed_count > 0 ? ((interviewStat.avg_score || 0) / 100) * 20 : 0;
      const resumeContribution = latestResume ? (latestResume.overall_score / 100) * 20 : 0;
      const projectContribution = Math.min(10, (projectsStat.completed || 0) * 5);
      const learningContribution = Math.min(10, (learningStat.completed_modules || 0) * 3);

      computedReadiness = Math.min(100, Math.round(baseScore * 0.2 + taskContribution + interviewContribution + resumeContribution + projectContribution + learningContribution));
    }

    return {
      userId,
      hasSufficientData,
      readinessScore: computedReadiness,
      targetRole: profile?.target_role || null,
      onboardingCompleted: Boolean(profile?.onboarding_completed),
      tasks: {
        total: tasksStat.total || 0,
        completed: tasksStat.completed || 0,
        percent: tasksStat.total > 0 ? Math.round((tasksStat.completed / tasksStat.total) * 100) : 0,
        hasData: tasksStat.total > 0
      },
      interviews: {
        completed: interviewStat.completed_count || 0,
        averageScore: interviewStat.avg_score ? Math.round(interviewStat.avg_score) : 0,
        hasData: interviewStat.completed_count > 0
      },
      skills: {
        acquired: skillsStat.count || 0,
        gapsIdentified: gapsStat.count || 0,
        hasData: skillsStat.count > 0 || gapsStat.count > 0
      },
      learning: {
        engagedCount: learningStat.total_engaged || 0,
        completedCount: learningStat.completed_modules || 0,
        inProgressCount: learningStat.in_progress_modules || 0,
        hasData: (learningStat.total_engaged || 0) > 0
      },
      projects: {
        total: projectsStat.total || 0,
        completed: projectsStat.completed || 0,
        hasData: projectsStat.total > 0
      },
      resume: latestResume ? {
        overallScore: latestResume.overall_score,
        impactScore: latestResume.impact_score,
        brevityScore: latestResume.brevity_score,
        styleScore: latestResume.style_score,
        totalUploaded: resumeCountStat.count || 1,
        lastAnalyzedAt: latestResume.created_at,
        hasData: true
      } : {
        totalUploaded: resumeCountStat.count || 0,
        hasData: false
      },
      recentActivity: recentEvents.map(e => ({
        eventName: e.event_name,
        data: e.event_data_json ? JSON.parse(e.event_data_json) : {},
        timestamp: e.timestamp
      }))
    };
  }
}
