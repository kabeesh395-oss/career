import { getDatabase } from '../db/database.js';

export interface NextBestAction {
  actionId: string;
  title: string;
  category: 'onboarding' | 'skill_gap' | 'roadmap_task' | 'resume' | 'interview' | 'portfolio' | 'career_advance';
  whyItMatters: string;
  skillAddressed?: string;
  evidence: string;
  estimatedMinutes: number;
  priority: 'urgent' | 'high' | 'medium';
  actionType: 'NAVIGATE' | 'TRIGGER_MODAL';
  targetRoute: string;
  ctaText: string;
}

export class NextBestActionService {
  public static calculateNextBestAction(userId: string): NextBestAction {
    const db = getDatabase();

    // 1. Check user profile & onboarding
    const profile = db.prepare('SELECT * FROM profiles WHERE user_id = ?').get(userId) as any;
    if (!profile || !profile.target_role) {
      return {
        actionId: 'act_complete_onboarding',
        title: 'Define Target Role & Career Objectives',
        category: 'onboarding',
        whyItMatters: 'CareerPilot AI requires your target role and current experience to calibrate readiness scores and personalized learning roadmaps.',
        evidence: 'Target role and experience profile are currently unset.',
        estimatedMinutes: 3,
        priority: 'urgent',
        actionType: 'NAVIGATE',
        targetRoute: '/profile',
        ctaText: 'Complete Profile'
      };
    }

    // 2. Check skill gaps
    const skillGapsCount = db.prepare('SELECT COUNT(*) as count FROM skill_gaps WHERE user_id = ?').get(userId) as { count: number };
    if (skillGapsCount.count === 0) {
      return {
        actionId: 'act_run_skill_gap',
        title: `Calibrate Skill Readiness for ${profile.target_role}`,
        category: 'skill_gap',
        whyItMatters: 'Determines the exact technical competencies required by top employers and pinpoints your specific elevation priorities.',
        evidence: 'No skill gap assessment is recorded for your current target role.',
        estimatedMinutes: 2,
        priority: 'urgent',
        actionType: 'NAVIGATE',
        targetRoute: '/career',
        ctaText: 'Run Career Analysis'
      };
    }

    // 3. Check roadmap
    const activeRoadmap = db.prepare('SELECT * FROM roadmaps WHERE user_id = ? ORDER BY created_at DESC LIMIT 1').get(userId) as any;
    if (!activeRoadmap) {
      return {
        actionId: 'act_generate_roadmap',
        title: 'Generate Personalized 3-Phase Roadmap',
        category: 'roadmap_task',
        whyItMatters: 'Translates identified skill gaps into step-by-step actionable milestone tasks and system design deliverables.',
        evidence: 'Identified skill gaps are ready to be structured into an execution roadmap.',
        estimatedMinutes: 2,
        priority: 'high',
        actionType: 'NAVIGATE',
        targetRoute: '/roadmap',
        ctaText: 'Generate Roadmap'
      };
    }

    // 4. Check next pending roadmap task
    const nextTask = db.prepare(`
      SELECT * FROM roadmap_items 
      WHERE roadmap_id = ? AND user_id = ? AND status != 'completed' 
      ORDER BY phase_number ASC, order_index ASC LIMIT 1
    `).get(activeRoadmap.id, userId) as any;

    if (nextTask) {
      return {
        actionId: `act_task_${nextTask.id}`,
        title: nextTask.title,
        category: 'roadmap_task',
        whyItMatters: `This deliverable directly fulfills Phase ${nextTask.phase_number} requirements: ${nextTask.phase_title}.`,
        skillAddressed: nextTask.category,
        evidence: `Task is currently marked as ${nextTask.status} with estimated duration of ${nextTask.estimated_hours}h.`,
        estimatedMinutes: Math.round(nextTask.estimated_hours * 60),
        priority: 'high',
        actionType: 'NAVIGATE',
        targetRoute: '/roadmap',
        ctaText: 'Work on Task'
      };
    }

    // 5. Check resume
    const resumeAnalysis = db.prepare('SELECT * FROM resume_analysis WHERE user_id = ? ORDER BY created_at DESC LIMIT 1').get(userId) as any;
    if (!resumeAnalysis) {
      return {
        actionId: 'act_upload_resume',
        title: 'Upload Resume for ATS Score & Keyword Audit',
        category: 'resume',
        whyItMatters: 'Validate that your resume accurately reflects high-impact metrics and the keywords target hiring managers scan for.',
        evidence: 'No resume document has been parsed and scored against your target role.',
        estimatedMinutes: 5,
        priority: 'high',
        actionType: 'NAVIGATE',
        targetRoute: '/resume',
        ctaText: 'Audit Resume'
      };
    }

    // 6. Check mock interview
    const interview = db.prepare('SELECT * FROM interviews WHERE user_id = ? ORDER BY created_at DESC LIMIT 1').get(userId) as any;
    if (!interview || interview.status !== 'completed') {
      return {
        actionId: 'act_mock_interview',
        title: `Simulate Technical Interview for ${profile.target_role}`,
        category: 'interview',
        whyItMatters: 'Practice real technical and architecture questions under timed conditions with instant rubric scoring and feedback.',
        evidence: 'Completed mock interviews provide key signals for readiness verification.',
        estimatedMinutes: 15,
        priority: 'high',
        actionType: 'NAVIGATE',
        targetRoute: '/interview',
        ctaText: 'Start Mock Interview'
      };
    }

    // 7. All primary milestones completed
    return {
      actionId: 'act_portfolio_capstone',
      title: 'Review Capstone Deliverables & Prepare Applications',
      category: 'portfolio',
      whyItMatters: 'You have systematically completed your roadmap tasks, resume audit, and interview simulation.',
      evidence: `Active readiness score is ${profile.current_readiness_score || 85}%.`,
      estimatedMinutes: 30,
      priority: 'medium',
      actionType: 'NAVIGATE',
      targetRoute: '/analytics',
      ctaText: 'View Career Intelligence Summary'
    };
  }
}
