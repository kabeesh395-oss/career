import crypto from 'crypto';
import { getDatabase } from '../db/database.js';
import { NextBestAction, NextBestActionService } from './nextBestAction.service.js';

export const READINESS_SCORE_VERSION = 'v1.0.0';

export type ReadinessPillar =
  | 'technical_skills'
  | 'resume_ats'
  | 'projects_github'
  | 'aptitude'
  | 'communication'
  | 'interview'
  | 'profile'
  | 'job_alignment'
  | 'learning_consistency';

export interface PillarResult {
  score: number;
  confidence: number;
  evidence: string[];
  gaps: string[];
  nextActions: string[];
}

export interface ReadinessSnapshot {
  id: string;
  userId: string;
  overallScore: number;
  overallConfidence: number;
  scoreVersion: string;
  pillars: Record<ReadinessPillar, PillarResult>;
  evidenceCoverage: Record<ReadinessPillar, { hasEvidence: boolean; count: number }>;
  sourceEventId: string | null;
  calculatedAt: string;
}

export interface ReadinessAction {
  id: string;
  pillar: ReadinessPillar;
  title: string;
  reason: string;
  estimatedMinutes: number;
  impactScore: number;
  difficulty: 'easy' | 'medium' | 'hard';
  prerequisites: string[];
  deepLink: string;
  evidenceRequired: string;
  status: 'recommended';
  completedAt: null;
  category: NextBestAction['category'];
  ctaText: string;
}

const PILLAR_WEIGHTS: Record<ReadinessPillar, number> = {
  technical_skills: 20,
  resume_ats: 15,
  projects_github: 15,
  aptitude: 10,
  communication: 10,
  interview: 10,
  profile: 5,
  job_alignment: 5,
  learning_consistency: 10
};

const emptyPillar = (nextAction: string): PillarResult => ({
  score: 0,
  confidence: 0,
  evidence: [],
  gaps: ['No evidence has been recorded yet.'],
  nextActions: [nextAction]
});

function clamp(value: number, min = 0, max = 100): number {
  return Math.max(min, Math.min(max, Math.round(value)));
}

function asNumber(value: unknown, fallback = 0): number {
  const result = Number(value);
  return Number.isFinite(result) ? result : fallback;
}

function toAction(action: NextBestAction): ReadinessAction {
  const pillarByCategory: Record<NextBestAction['category'], ReadinessPillar> = {
    onboarding: 'profile',
    skill_gap: 'technical_skills',
    roadmap_task: 'learning_consistency',
    resume: 'resume_ats',
    interview: 'interview',
    portfolio: 'projects_github',
    career_advance: 'job_alignment'
  };
  const impactByPriority = { urgent: 95, high: 75, medium: 55 } as const;
  const difficulty = action.estimatedMinutes <= 5 ? 'easy' : action.estimatedMinutes <= 20 ? 'medium' : 'hard';
  return {
    id: action.actionId,
    pillar: pillarByCategory[action.category],
    title: action.title,
    reason: action.whyItMatters,
    estimatedMinutes: action.estimatedMinutes,
    impactScore: impactByPriority[action.priority],
    difficulty,
    prerequisites: [],
    deepLink: action.targetRoute,
    evidenceRequired: action.evidence,
    status: 'recommended',
    completedAt: null,
    category: action.category,
    ctaText: action.ctaText
  };
}

function parseSnapshot(row: any): ReadinessSnapshot {
  return {
    id: row.id,
    userId: row.user_id,
    overallScore: row.overall_score,
    overallConfidence: row.overall_confidence,
    scoreVersion: row.score_version,
    pillars: JSON.parse(row.pillar_scores_json),
    evidenceCoverage: JSON.parse(row.evidence_coverage_json),
    sourceEventId: row.source_event_id || null,
    calculatedAt: row.created_at
  };
}

export class ReadinessService {
  public static calculateAndPersist(userId: string, sourceEventId?: string): ReadinessSnapshot {
    const db = getDatabase();
    const profile = db.prepare('SELECT * FROM profiles WHERE user_id = ?').get(userId) as any;
    const targetRole = profile?.target_role || null;

    const skills = db.prepare('SELECT id FROM user_skills WHERE user_id = ?').all(userId) as Array<{ id: string }>;
    const gaps = targetRole
      ? db.prepare('SELECT id, skill_name, gap_score FROM skill_gaps WHERE user_id = ? AND target_role = ? ORDER BY gap_score DESC').all(userId, targetRole) as Array<{ id: string; skill_name: string; gap_score: number }>
      : [];
    const latestResume = db.prepare('SELECT id, overall_score FROM resume_analysis WHERE user_id = ? ORDER BY created_at DESC LIMIT 1').get(userId) as any;
    const projects = db.prepare('SELECT id, status FROM projects WHERE user_id = ?').all(userId) as Array<{ id: string; status: string }>;
    const interviews = db.prepare("SELECT id, overall_score FROM interviews WHERE user_id = ? AND status = 'completed'").all(userId) as Array<{ id: string; overall_score: number }>;
    const learning = db.prepare('SELECT id, status FROM learning_progress WHERE user_id = ?').all(userId) as Array<{ id: string; status: string }>;

    const pillars: Record<ReadinessPillar, PillarResult> = {
      technical_skills: gaps.length > 0
        ? {
            score: clamp(100 - gaps.reduce((sum, gap) => sum + asNumber(gap.gap_score), 0) / gaps.length),
            confidence: Math.min(1, gaps.length / 5),
            evidence: gaps.map(gap => `skill_gap:${gap.id}`),
            gaps: gaps.slice(0, 3).map(gap => gap.skill_name),
            nextActions: gaps.slice(0, 2).map(gap => `Improve ${gap.skill_name}`)
          }
        : skills.length > 0
          ? { score: 70, confidence: 0.6, evidence: skills.map(skill => `user_skill:${skill.id}`), gaps: ['Run a target-role skill-gap calibration.'], nextActions: ['Run career analysis against the target role.'] }
          : emptyPillar('Add skills and run a target-role skill-gap calibration.'),
      resume_ats: latestResume
        ? { score: clamp(latestResume.overall_score), confidence: 0.85, evidence: [`resume_analysis:${latestResume.id}`], gaps: latestResume.overall_score < 80 ? ['Resume ATS score is below the 80-point target.'] : [], nextActions: latestResume.overall_score < 80 ? ['Upload an improved resume version.'] : ['Match the resume against a target job.'] }
        : emptyPillar('Upload a resume for ATS analysis.'),
      projects_github: projects.length > 0
        ? { score: clamp((projects.filter(project => project.status === 'completed').length / projects.length) * 100), confidence: Math.min(1, projects.length / 3), evidence: projects.map(project => `project:${project.id}`), gaps: projects.some(project => project.status === 'completed') ? [] : ['No completed project evidence yet.'], nextActions: ['Complete and document one portfolio project.'] }
        : emptyPillar('Add a project with repository or deployment evidence.'),
      aptitude: emptyPillar('Complete a quantitative and logical aptitude assessment.'),
      communication: emptyPillar('Complete a structured communication practice exercise.'),
      interview: interviews.length > 0
        ? { score: clamp(interviews.reduce((sum, interview) => sum + asNumber(interview.overall_score), 0) / interviews.length), confidence: Math.min(1, interviews.length / 3), evidence: interviews.map(interview => `interview:${interview.id}`), gaps: [], nextActions: ['Complete another mock interview and improve the lowest rubric score.'] }
        : emptyPillar('Complete a mock interview.'),
      profile: profile
        ? { score: clamp((profile.target_role ? 40 : 0) + (profile.onboarding_completed ? 30 : 0) + (profile.education ? 10 : 0) + (profile.experience_years ? 10 : 0) + (profile.headline ? 10 : 0)), confidence: 0.8, evidence: [`profile:${profile.id}`], gaps: profile.onboarding_completed ? [] : ['Onboarding profile is incomplete.'], nextActions: profile.onboarding_completed ? ['Keep your target role and experience current.'] : ['Complete the career profile.'] }
        : emptyPillar('Complete your career profile.'),
      job_alignment: emptyPillar('Analyze a target job description.'),
      learning_consistency: learning.length > 0
        ? { score: clamp((learning.filter(item => item.status === 'completed').length / learning.length) * 100), confidence: Math.min(1, learning.length / 5), evidence: learning.map(item => `learning:${item.id}`), gaps: [], nextActions: ['Complete the next learning roadmap milestone.'] }
        : emptyPillar('Start a learning roadmap milestone.')
    };

    const weightedObserved = Object.entries(PILLAR_WEIGHTS).reduce((sum, [pillar, weight]) => {
      const result = pillars[pillar as ReadinessPillar];
      return sum + (result.confidence > 0 ? weight : 0);
    }, 0);
    const weightedScore = Object.entries(PILLAR_WEIGHTS).reduce((sum, [pillar, weight]) => {
      const result = pillars[pillar as ReadinessPillar];
      return sum + (result.confidence > 0 ? result.score * weight : 0);
    }, 0);
    const weightedConfidence = Object.entries(PILLAR_WEIGHTS).reduce((sum, [pillar, weight]) => {
      return sum + pillars[pillar as ReadinessPillar].confidence * weight;
    }, 0);

    const snapshotId = `rds_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;
    db.prepare(`
      INSERT INTO readiness_snapshots (
        id, user_id, overall_score, overall_confidence, score_version,
        pillar_scores_json, confidence_json, evidence_coverage_json, source_event_id
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    `).run(
      snapshotId,
      userId,
      weightedObserved > 0 ? clamp(weightedScore / weightedObserved) : 0,
      Math.round((weightedConfidence / 100) * 100) / 100,
      READINESS_SCORE_VERSION,
      JSON.stringify(pillars),
      JSON.stringify(Object.fromEntries(Object.entries(pillars).map(([key, value]) => [key, value.confidence]))),
      JSON.stringify(Object.fromEntries(Object.entries(pillars).map(([key, value]) => [key, { hasEvidence: value.evidence.length > 0, count: value.evidence.length }]))),
      sourceEventId || null
    );

    return parseSnapshot(db.prepare('SELECT * FROM readiness_snapshots WHERE id = ?').get(snapshotId));
  }

  public static getCurrent(userId: string, forceRecalculate = false): ReadinessSnapshot {
    const db = getDatabase();
    const latest = db.prepare('SELECT * FROM readiness_snapshots WHERE user_id = ? ORDER BY created_at DESC LIMIT 1').get(userId) as any;
    if (!forceRecalculate && latest) {
      const createdAt = Date.parse(String(latest.created_at).replace(' ', 'T') + 'Z');
      if (Number.isFinite(createdAt) && Date.now() - createdAt < 30_000) return parseSnapshot(latest);
    }
    return this.calculateAndPersist(userId, `readiness_recalculated_${Date.now()}`);
  }

  public static getHistory(userId: string, limit = 20): ReadinessSnapshot[] {
    const db = getDatabase();
    return (db.prepare('SELECT * FROM readiness_snapshots WHERE user_id = ? ORDER BY created_at DESC LIMIT ?').all(userId, Math.min(Math.max(limit, 1), 100)) as any[]).map(parseSnapshot);
  }

  public static getEvidence(userId: string): Record<ReadinessPillar, { hasEvidence: boolean; count: number }> {
    return this.getCurrent(userId).evidenceCoverage;
  }

  public static getNextAction(userId: string): ReadinessAction {
    return toAction(NextBestActionService.calculateNextBestAction(userId));
  }
}
