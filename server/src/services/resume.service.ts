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

export interface JobDescriptionAnalysisResult {
  jobRoleDetected: string;
  experienceRequirement: string;
  technicalSkillsRequired: string[];
  softSkillsRequired: string[];
  matchedSkills: Array<{ skill: string; evidenceInResume: string }>;
  partialSkills: Array<{ skill: string; relatedSkillFound: string; reason: string }>;
  missingSkills: Array<{ skill: string; priority: 'high' | 'medium' | 'low'; recommendation: string }>;
  overallMatchPercent: number;
  modelUsed: string;
  summary: string;
}

const COMMON_SOFT_SKILLS = [
  'Communication',
  'Cross-functional Collaboration',
  'Mentorship & Leadership',
  'Problem Solving',
  'Agile / Scrum',
  'Ownership & Initiative',
  'Stakeholder Management',
  'Critical Thinking',
  'Team Leadership',
  'Code Review Best Practices'
];

const SKILL_EQUIVALENCE_MAP: Record<string, string[]> = {
  'TypeScript': ['JavaScript'],
  'Next.js': ['React', 'JavaScript', 'TypeScript'],
  'Kubernetes': ['Docker & Containerization', 'Docker'],
  'PostgreSQL': ['SQL', 'Databases'],
  'Redis Caching & Pub/Sub': ['PostgreSQL', 'MongoDB', 'Databases'],
  'Microservices & Distributed Systems': ['Node.js', 'RESTful API Architecture', 'Go'],
  'RAG (Retrieval Augmented Generation)': ['Python', 'Vector Databases (Pinecone/pgvector)', 'LLM Prompting & Function Calling'],
  'AWS Cloud Architecture': ['Docker & Containerization', 'CI/CD Pipelines (GitHub Actions)'],
  'CI/CD Pipelines (GitHub Actions)': ['Docker & Containerization'],
  'Terraform (IaC)': ['AWS Cloud Architecture', 'Kubernetes']
};

export function analyzeJobDescription(
  jobDescriptionText: string,
  resumeText: string = '',
  userSkills: string[] = []
): JobDescriptionAnalysisResult {
  const db = getDatabase();
  const allSkills = db.prepare('SELECT name FROM skills').all() as { name: string }[];
  const lowerJd = jobDescriptionText.toLowerCase();
  const lowerResume = resumeText.toLowerCase();

  // 1. Extract Role Title Heuristic
  const roleRegex = /(?:seeking|looking for|hire|role|position|title):\s*([a-zA-Z\s/&-]+(?:Engineer|Developer|Architect|Lead|Manager|Specialist|Scientist))/i;
  const roleMatch = jobDescriptionText.match(roleRegex);
  let jobRoleDetected = roleMatch ? roleMatch[1].trim() : 'Software Engineering Role';
  if (jobRoleDetected.length > 50) {
    jobRoleDetected = 'Software Engineering Professional';
  }

  // 2. Extract Experience Requirements
  const expRegex = /(\d+\+?\s*(?:to|-)?\s*\d*\s*years?(?:\s+of)?(?:\s+(?:professional|relevant|hands-on|industry))?\s+experience|\bminimum\s+\d+\s+years?\b)/i;
  const expMatch = jobDescriptionText.match(expRegex);
  const experienceRequirement = expMatch ? expMatch[0].trim() : 'Experience specified in JD description';

  // 3. Extract Technical Skills from JD
  const technicalSkillsRequired: string[] = [];
  for (const skill of allSkills) {
    const exactPattern = new RegExp(`\\b${skill.name.toLowerCase().replace(/[-/\\^$*+?.()|[\]{}]/g, '\\$&')}\\b`, 'i');
    if (exactPattern.test(lowerJd)) {
      technicalSkillsRequired.push(skill.name);
      continue;
    }
    const parts = skill.name.split(/[\s/&,()]+/).filter(p => p.length >= 3);
    for (const part of parts) {
      if (['and', 'with', 'the', 'for', 'modern'].includes(part.toLowerCase())) continue;
      const partPattern = new RegExp(`\\b${part.toLowerCase().replace(/[-/\\^$*+?.()|[\]{}]/g, '\\$&')}\\b`, 'i');
      if (partPattern.test(lowerJd)) {
        technicalSkillsRequired.push(skill.name);
        break;
      }
    }
  }

  const uniqueTechSkills = Array.from(new Set(technicalSkillsRequired));

  // 4. Extract Soft Skills from JD
  const softSkillsRequired: string[] = [];
  for (const soft of COMMON_SOFT_SKILLS) {
    if (lowerJd.includes(soft.toLowerCase())) {
      softSkillsRequired.push(soft);
    }
  }

  // 5. Compare with Resume & Acquired Skills
  const candidateSkills = new Set([
    ...userSkills.map(s => s.toLowerCase()),
    ...parseResumeContent(resumeText).extractedSkills.map(s => s.toLowerCase())
  ]);

  const matchedSkills: Array<{ skill: string; evidenceInResume: string }> = [];
  const partialSkills: Array<{ skill: string; relatedSkillFound: string; reason: string }> = [];
  const missingSkills: Array<{ skill: string; priority: 'high' | 'medium' | 'low'; recommendation: string }> = [];

  for (const required of uniqueTechSkills) {
    const reqLower = required.toLowerCase();
    
    // Check direct match
    let isDirectMatch = candidateSkills.has(reqLower);
    if (!isDirectMatch) {
      for (const cs of candidateSkills) {
        if (cs.includes(reqLower) || reqLower.includes(cs)) {
          isDirectMatch = true;
          break;
        }
      }
    }

    if (isDirectMatch) {
      // Find real sentence evidence from resume
      let evidence = `Demonstrated competency in ${required} detected in profile and resume analysis.`;
      if (resumeText) {
        const sentences = resumeText.split(/[.\n\r]+/);
        const matchSentence = sentences.find(s => s.toLowerCase().includes(reqLower));
        if (matchSentence && matchSentence.trim().length > 10) {
          evidence = matchSentence.trim().slice(0, 140) + '...';
        }
      }
      matchedSkills.push({ skill: required, evidenceInResume: evidence });
      continue;
    }

    // Check partial / adjacent skills
    const related = SKILL_EQUIVALENCE_MAP[required] || [];
    const foundRelated = related.find(r => candidateSkills.has(r.toLowerCase()));

    if (foundRelated) {
      partialSkills.push({
        skill: required,
        relatedSkillFound: foundRelated,
        reason: `Candidate possesses foundational experience in ${foundRelated}, facilitating rapid transition to ${required}.`
      });
      continue;
    }

    // Otherwise missing
    missingSkills.push({
      skill: required,
      priority: uniqueTechSkills.indexOf(required) < 3 ? 'high' : 'medium',
      recommendation: `Add verifiable project implementations leveraging ${required} before applying to this posting.`
    });
  }

  // Calculate genuine match percentage based on real metrics
  const totalRequired = uniqueTechSkills.length;
  let overallMatchPercent = 0;
  if (totalRequired > 0) {
    const rawScore = (matchedSkills.length * 1.0) + (partialSkills.length * 0.5);
    overallMatchPercent = Math.min(100, Math.max(10, Math.round((rawScore / totalRequired) * 100)));
  } else {
    overallMatchPercent = 65; // Baseline when JD has general descriptions
  }

  const summary = `Extracted ${uniqueTechSkills.length} required technical skills and ${softSkillsRequired.length} soft skills. Found ${matchedSkills.length} verified matches, ${partialSkills.length} transferable competencies, and ${missingSkills.length} skill gaps.`;

  return {
    jobRoleDetected,
    experienceRequirement,
    technicalSkillsRequired: uniqueTechSkills,
    softSkillsRequired,
    matchedSkills,
    partialSkills,
    missingSkills,
    overallMatchPercent,
    modelUsed: 'CareerHub-Edge-JD-Matcher-v1',
    summary
  };
}

