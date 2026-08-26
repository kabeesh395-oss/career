import { Router, Response } from 'express';
import fs from 'fs';
import crypto from 'crypto';
import { getDatabase } from '../db/database.js';
import { authenticate, AuthenticatedRequest } from '../middleware/auth.js';
import { uploadResume } from '../middleware/upload.js';
import { extractTextFromResume, parseResumeContent } from '../services/resume.service.js';
import { AIService } from '../services/ai.service.js';
import { AnalyticsService } from '../services/analytics.service.js';

const router = Router();
router.use(authenticate);

// GET /api/resume (List resumes & latest analysis)
router.get('/', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const db = getDatabase();

    const resumes = db.prepare('SELECT * FROM resumes WHERE user_id = ? ORDER BY created_at DESC').all(userId);
    const analyses = db.prepare('SELECT * FROM resume_analysis WHERE user_id = ? ORDER BY created_at DESC').all(userId);

    const formattedAnalyses = analyses.map((a: any) => ({
      ...a,
      skills_detected: JSON.parse(a.skills_detected_json || '[]'),
      strengths: JSON.parse(a.strengths_json || '[]'),
      weaknesses: JSON.parse(a.weaknesses_json || '[]'),
      recommendations: JSON.parse(a.recommendations_json || '[]')
    }));

    return res.json({
      resumes,
      latestAnalysis: formattedAnalyses[0] || null,
      analyses: formattedAnalyses
    });
  } catch (err) {
    next(err);
  }
});

// POST /api/resume/upload
router.post('/upload', uploadResume.single('resume'), async (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const file = req.file;

    if (!file) {
      return res.status(400).json({
        error: { code: 'FILE_MISSING', message: 'No resume file was uploaded. Please attach a valid PDF, TXT, or DOCX document.' }
      });
    }

    const db = getDatabase();
    const profile = db.prepare('SELECT * FROM profiles WHERE user_id = ?').get(userId) as any;
    const targetRole = req.body.targetRole || profile?.target_role || 'Full Stack Engineer';

    // 1. Text extraction
    let extractedText = '';
    try {
      extractedText = await extractTextFromResume(file.path, file.mimetype);
    } catch (parseErr: any) {
      return res.status(422).json({
        error: { code: 'EXTRACTION_FAILED', message: `Could not parse text from uploaded file: ${parseErr.message}` }
      });
    }

    if (!extractedText.trim()) {
      return res.status(422).json({
        error: { code: 'EMPTY_DOCUMENT', message: 'The uploaded resume contains no readable text.' }
      });
    }

    // 2. Skill parsing & heuristic metrics
    const parsedData = parseResumeContent(extractedText);

    // 3. AI / ATS Evaluation against target role
    const analysisResult = await AIService.analyzeResumeAgainstRole(
      extractedText,
      parsedData.extractedSkills,
      targetRole,
      parsedData.wordCount,
      parsedData.hasMetrics,
      parsedData.hasEducation,
      parsedData.hasExperience
    );

    const resumeId = `res_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;
    const analysisId = `ran_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;

    // 4. Atomic database persistence
    const saveResumeTx = db.transaction(() => {
      db.prepare(`
        INSERT INTO resumes (id, user_id, original_filename, stored_filename, file_size, mime_type, file_path, extracted_text, status, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'analyzed', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
      `).run(
        resumeId,
        userId,
        file.originalname,
        file.filename,
        file.size,
        file.mimetype,
        file.path,
        extractedText
      );

      db.prepare(`
        INSERT INTO resume_analysis (
          id, resume_id, user_id, target_role, overall_score, impact_score, brevity_score, style_score,
          skills_detected_json, strengths_json, weaknesses_json, recommendations_json, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
      `).run(
        analysisId,
        resumeId,
        userId,
        targetRole,
        analysisResult.overallScore,
        analysisResult.impactScore,
        analysisResult.brevityScore,
        analysisResult.styleScore,
        JSON.stringify(analysisResult.skillsDetected),
        JSON.stringify(analysisResult.strengths),
        JSON.stringify(analysisResult.weaknesses),
        JSON.stringify(analysisResult.recommendations)
      );
    });

    saveResumeTx();

    AnalyticsService.trackEvent(userId, 'resume_uploaded_and_analyzed', {
      resumeId,
      filename: file.originalname,
      overallScore: analysisResult.overallScore,
      skillsCount: analysisResult.skillsDetected.length,
      modelUsed: analysisResult.modelUsed
    });

    const resumeRecord = db.prepare('SELECT * FROM resumes WHERE id = ?').get(resumeId);

    return res.status(201).json({
      resume: resumeRecord,
      analysis: {
        id: analysisId,
        resume_id: resumeId,
        target_role: targetRole,
        overall_score: analysisResult.overallScore,
        impact_score: analysisResult.impactScore,
        brevity_score: analysisResult.brevityScore,
        style_score: analysisResult.styleScore,
        skills_detected: analysisResult.skillsDetected,
        strengths: analysisResult.strengths,
        weaknesses: analysisResult.weaknesses,
        recommendations: analysisResult.recommendations,
        model_used: analysisResult.modelUsed
      }
    });
  } catch (err) {
    next(err);
  }
});

// DELETE /api/resume/:resumeId
router.delete('/:resumeId', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const { resumeId } = req.params;
    const db = getDatabase();

    const resume = db.prepare('SELECT * FROM resumes WHERE id = ? AND user_id = ?').get(resumeId, userId) as any;
    if (!resume) {
      return res.status(404).json({
        error: { code: 'RESUME_NOT_FOUND', message: 'Resume not found or you do not have permission to delete it.' }
      });
    }

    // Delete file from disk if present
    if (resume.file_path && fs.existsSync(resume.file_path)) {
      try {
        fs.unlinkSync(resume.file_path);
      } catch (e) {
        console.warn('Could not remove file on disk:', e);
      }
    }

    db.prepare('DELETE FROM resumes WHERE id = ? AND user_id = ?').run(resumeId, userId);

    return res.json({ success: true, message: 'Resume and associated analysis successfully deleted.' });
  } catch (err) {
    next(err);
  }
});

export default router;
