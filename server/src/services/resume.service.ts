import fs from 'fs';
import path from 'path';
import pdfParse from 'pdf-parse';
import { getDatabase } from '../db/database.js';

export interface ExtractedResumeData {
  rawText: string;
  extractedSkills: string[];
  wordCount: number;
  hasMetrics: boolean;
  hasEducation: boolean;
  hasExperience: boolean;
  detectedRole?: string;
}

export async function extractTextFromResume(filePath: string, mimeType: string): Promise<string> {
  if (!fs.existsSync(filePath)) {
    throw new Error('Resume file not found on server.');
  }

  const ext = path.extname(filePath).toLowerCase();

  if (mimeType === 'application/pdf' || ext === '.pdf') {
    const dataBuffer = fs.readFileSync(filePath);
    const pdfData = await (pdfParse as any)(dataBuffer);
    return pdfData.text || '';
  } else if (mimeType === 'text/plain' || ext === '.txt') {
    return fs.readFileSync(filePath, 'utf8');
  } else {
    // For DOC/DOCX or binary text fallback, extract visible ASCII strings
    const buffer = fs.readFileSync(filePath);
    const raw = buffer.toString('utf8');
    const cleaned = raw.replace(/[^\x20-\x7E\n\r\t]/g, ' ');
    return cleaned.trim();
  }
}

export function parseResumeContent(text: string): ExtractedResumeData {
  const db = getDatabase();
  const allSkills = db.prepare('SELECT name FROM skills').all() as { name: string }[];

  const lowerText = text.toLowerCase();
  const extractedSkills: string[] = [];

  for (const skill of allSkills) {
    // Check exact name match
    const exactPattern = new RegExp(`\\b${skill.name.toLowerCase().replace(/[-/\\^$*+?.()|[\]{}]/g, '\\$&')}\\b`, 'i');
    if (exactPattern.test(lowerText)) {
      extractedSkills.push(skill.name);
      continue;
    }

    // Check main sub-parts (e.g., "Docker" from "Docker & Containerization", "AWS" from "AWS Cloud Architecture")
    const parts = skill.name.split(/[\s/&,()]+/).filter(p => p.length >= 3);
    for (const part of parts) {
      if (['and', 'with', 'the', 'for', 'modern'].includes(part.toLowerCase())) continue;
      const partPattern = new RegExp(`\\b${part.toLowerCase().replace(/[-/\\^$*+?.()|[\]{}]/g, '\\$&')}\\b`, 'i');
      if (partPattern.test(lowerText)) {
        extractedSkills.push(skill.name);
        break;
      }
    }
  }

  // Detect metrics/quantifiable impact (e.g. 50%, $100k, 10x, 20+ engineers, 45%)
  const metricRegex = /(\d+\s*%|\$\d+[\d,]*|\b\d+x\b|\b\d+\+\b|\bincreased\b|\breduced\b|\breducing\b|\bscaled\b|\bgpa:\s*\d)/i;
  const hasMetrics = metricRegex.test(text);

  // Detect education section
  const educationRegex = /\b(bachelor|master|b\.s\.|m\.s\.|b\.tech|m\.tech|phd|degree|university|college|gpa)\b/i;
  const hasEducation = educationRegex.test(lowerText);

  // Detect experience section
  const experienceRegex = /\b(experience|work history|employment|senior|software engineer|developer|intern|lead|architect|manager)\b/i;
  const hasExperience = experienceRegex.test(lowerText);

  const wordCount = text.trim().split(/\s+/).filter(Boolean).length;

  return {
    rawText: text,
    extractedSkills: Array.from(new Set(extractedSkills)),
    wordCount,
    hasMetrics,
    hasEducation,
    hasExperience
  };
}
