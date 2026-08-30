import { AIService, CareerReadinessResult, GeneratedRoadmap, ResumeAnalysisOutput, AnswerEvaluationOutput } from './ai.service.js';

export interface IAiProvider {
  name: string;
  isAvailable(): boolean;
  evaluateReadiness(skills: Array<{ name: string; proficiency_level: number }>, targetRole: string, expYears: number): Promise<CareerReadinessResult>;
  generateRoadmap(targetRole: string, gaps: any[]): Promise<GeneratedRoadmap>;
  analyzeResume(text: string, skills: string[], role: string, wordCount: number, hasMetrics: boolean): Promise<ResumeAnalysisOutput>;
  evaluateInterview(qText: string, aText: string, category: string, difficulty: string): Promise<AnswerEvaluationOutput>;
}

export class GeminiProvider implements IAiProvider {
  name = 'Gemini-1.5-Flash';

  isAvailable(): boolean {
    return !!process.env.GEMINI_API_KEY;
  }

  async evaluateReadiness(skills: Array<{ name: string; proficiency_level: number }>, targetRole: string, expYears: number): Promise<CareerReadinessResult> {
    return AIService.analyzeCareerReadinessAndGaps(skills, targetRole, expYears);
  }

  async generateRoadmap(targetRole: string, gaps: any[]): Promise<GeneratedRoadmap> {
    return AIService.generatePersonalizedRoadmap(targetRole, gaps);
  }

  async analyzeResume(text: string, skills: string[], role: string, wordCount: number, hasMetrics: boolean): Promise<ResumeAnalysisOutput> {
    return AIService.analyzeResumeAgainstRole(text, skills, role, wordCount, hasMetrics, true, true);
  }

  async evaluateInterview(qText: string, aText: string, category: string, difficulty: string): Promise<AnswerEvaluationOutput> {
    return AIService.evaluateInterviewAnswer(qText, aText, category, difficulty);
  }
}

export class FallbackDeterministicProvider implements IAiProvider {
  name = 'CareerHub-Deterministic-NLP-v1';

  isAvailable(): boolean {
    return true; // Always available offline / fallback
  }

  async evaluateReadiness(skills: Array<{ name: string; proficiency_level: number }>, targetRole: string, expYears: number): Promise<CareerReadinessResult> {
    return AIService.analyzeCareerReadinessAndGaps(skills, targetRole, expYears);
  }

  async generateRoadmap(targetRole: string, gaps: any[]): Promise<GeneratedRoadmap> {
    return AIService.generatePersonalizedRoadmap(targetRole, gaps);
  }

  async analyzeResume(text: string, skills: string[], role: string, wordCount: number, hasMetrics: boolean): Promise<ResumeAnalysisOutput> {
    return AIService.analyzeResumeAgainstRole(text, skills, role, wordCount, hasMetrics, true, true);
  }

  async evaluateInterview(qText: string, aText: string, category: string, difficulty: string): Promise<AnswerEvaluationOutput> {
    return AIService.evaluateInterviewAnswer(qText, aText, category, difficulty);
  }
}

export class AiProviderManager {
  private primary: IAiProvider = new GeminiProvider();
  private fallback: IAiProvider = new FallbackDeterministicProvider();

  public getProvider(): IAiProvider {
    if (this.primary.isAvailable()) {
      return this.primary;
    }
    console.warn('[AI Provider] Primary AI provider unavailable/unconfigured. Falling back to deterministic NLP engine.');
    return this.fallback;
  }
}

export const aiProviderManager = new AiProviderManager();
