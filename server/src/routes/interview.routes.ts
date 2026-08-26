import { Router, Response } from 'express';
import { z } from 'zod';
import crypto from 'crypto';
import { getDatabase } from '../db/database.js';
import { authenticate, AuthenticatedRequest } from '../middleware/auth.js';
import { AIService } from '../services/ai.service.js';
import { AnalyticsService } from '../services/analytics.service.js';

const router = Router();
router.use(authenticate);

const StartInterviewSchema = z.object({
  targetRole: z.string().optional(),
  difficulty: z.enum(['junior', 'intermediate', 'advanced', 'expert']).default('intermediate')
});

const SubmitAnswerSchema = z.object({
  questionId: z.string().min(1, 'Question ID is required'),
  answerText: z.string().min(5, 'Answer text must contain substantive explanation')
});

// GET /api/interview/history
router.get('/history', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const db = getDatabase();

    const interviews = db.prepare('SELECT * FROM interviews WHERE user_id = ? ORDER BY created_at DESC').all(userId);
    return res.json({ interviews });
  } catch (err) {
    next(err);
  }
});

// POST /api/interview/start
router.post('/start', async (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const { targetRole, difficulty } = StartInterviewSchema.parse(req.body);
    const db = getDatabase();

    const profile = db.prepare('SELECT * FROM profiles WHERE user_id = ?').get(userId) as any;
    const role = targetRole || profile?.target_role || 'Full Stack Engineer';

    const generatedQuestions = await AIService.generateInterviewQuestions(role, difficulty);
    const interviewId = `itv_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;

    const createInterviewTx = db.transaction(() => {
      db.prepare(`
        INSERT INTO interviews (id, user_id, role_target, difficulty, status, total_questions, completed_questions, overall_score, created_at, updated_at)
        VALUES (?, ?, ?, ?, 'in_progress', ?, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
      `).run(interviewId, userId, role, difficulty, generatedQuestions.length);

      const insertQuestionStmt = db.prepare(`
        INSERT INTO interview_questions (id, interview_id, question_number, question_text, category, difficulty, ideal_rubric)
        VALUES (?, ?, ?, ?, ?, ?, ?)
      `);

      for (const q of generatedQuestions) {
        const qId = `iqs_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;
        insertQuestionStmt.run(qId, interviewId, q.questionNumber, q.questionText, q.category, q.difficulty, q.idealRubric);
      }
    });

    createInterviewTx();

    AnalyticsService.trackEvent(userId, 'interview_started', { interviewId, role, difficulty });

    const interview = db.prepare('SELECT * FROM interviews WHERE id = ?').get(interviewId);
    const questions = db.prepare('SELECT * FROM interview_questions WHERE interview_id = ? ORDER BY question_number ASC').all(interviewId);

    return res.status(201).json({
      interview,
      questions
    });
  } catch (err: any) {
    if (err.name === 'ZodError') {
      return res.status(422).json({
        error: { code: 'VALIDATION_ERROR', message: err.errors.map((e: any) => e.message).join(', ') }
      });
    }
    next(err);
  }
});

// GET /api/interview/:interviewId
router.get('/:interviewId', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const { interviewId } = req.params;
    const db = getDatabase();

    const interview = db.prepare('SELECT * FROM interviews WHERE id = ? AND user_id = ?').get(interviewId, userId);
    if (!interview) {
      return res.status(404).json({
        error: { code: 'INTERVIEW_NOT_FOUND', message: 'Interview session not found or unauthorized.' }
      });
    }

    const questions = db.prepare('SELECT * FROM interview_questions WHERE interview_id = ? ORDER BY question_number ASC').all(interviewId);
    const answers = db.prepare('SELECT * FROM interview_answers WHERE interview_id = ? AND user_id = ?').all(interviewId, userId);

    return res.json({
      interview,
      questions,
      answers
    });
  } catch (err) {
    next(err);
  }
});

// POST /api/interview/:interviewId/answer (Submit & score answer)
router.post('/:interviewId/answer', async (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const { interviewId } = req.params;
    const { questionId, answerText } = SubmitAnswerSchema.parse(req.body);
    const db = getDatabase();

    // Verify interview session
    const interview = db.prepare('SELECT * FROM interviews WHERE id = ? AND user_id = ?').get(interviewId, userId) as any;
    if (!interview) {
      return res.status(404).json({
        error: { code: 'INTERVIEW_NOT_FOUND', message: 'Interview session not found or unauthorized.' }
      });
    }

    // Verify question belongs to interview
    const question = db.prepare('SELECT * FROM interview_questions WHERE id = ? AND interview_id = ?').get(questionId, interviewId) as any;
    if (!question) {
      return res.status(404).json({
        error: { code: 'QUESTION_NOT_FOUND', message: 'Question does not belong to this interview session.' }
      });
    }

    // Check if already answered (deduplication)
    const existingAnswer = db.prepare('SELECT id FROM interview_answers WHERE question_id = ? AND user_id = ?').get(questionId, userId);
    if (existingAnswer) {
      return res.status(409).json({
        error: { code: 'DUPLICATE_SUBMISSION', message: 'You have already submitted an answer for this question.' }
      });
    }

    // AI / Heuristic evaluation of answer
    const evalResult = await AIService.evaluateInterviewAnswer(
      question.question_text,
      answerText,
      question.category,
      question.difficulty
    );

    const answerId = `ans_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;

    // Transaction for saving answer and updating interview overall score
    const saveAnswerTx = db.transaction(() => {
      db.prepare(`
        INSERT INTO interview_answers (id, question_id, interview_id, user_id, answer_text, score, clarity_score, technical_score, feedback, suggested_improvement, submitted_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
      `).run(
        answerId,
        questionId,
        interviewId,
        userId,
        answerText,
        evalResult.score,
        evalResult.clarityScore,
        evalResult.technicalScore,
        evalResult.feedback,
        evalResult.suggestedImprovement
      );

      // Compute new overall score
      const stats = db.prepare(`
        SELECT 
          COUNT(*) as answered_count,
          AVG(score) as avg_score
        FROM interview_answers 
        WHERE interview_id = ? AND user_id = ?
      `).get(interviewId, userId) as { answered_count: number; avg_score: number };

      const isCompleted = stats.answered_count >= interview.total_questions;
      const status = isCompleted ? 'completed' : 'in_progress';
      const overallScore = Math.round(stats.avg_score);

      db.prepare(`
        UPDATE interviews 
        SET completed_questions = ?, overall_score = ?, status = ?, updated_at = CURRENT_TIMESTAMP
        WHERE id = ? AND user_id = ?
      `).run(stats.answered_count, overallScore, status, interviewId, userId);
    });

    saveAnswerTx();

    AnalyticsService.trackEvent(userId, 'interview_question_answered', {
      interviewId,
      questionId,
      score: evalResult.score,
      modelUsed: evalResult.modelUsed
    });

    const savedAnswer = db.prepare('SELECT * FROM interview_answers WHERE id = ?').get(answerId);
    const updatedInterview = db.prepare('SELECT * FROM interviews WHERE id = ?').get(interviewId);

    return res.status(201).json({
      answer: savedAnswer,
      interview: updatedInterview,
      evaluation: evalResult
    });
  } catch (err: any) {
    if (err.name === 'ZodError') {
      return res.status(422).json({
        error: { code: 'VALIDATION_ERROR', message: err.errors.map((e: any) => e.message).join(', ') }
      });
    }
    next(err);
  }
});

export default router;
