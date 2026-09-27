import { Router, Request, Response } from 'express';
import { aiProviderManager } from '../services/aiProvider.service.js';

const router = Router();

// GET /api/edge-ai/status
router.get('/status', (req: Request, res: Response) => {
  const activeStatus = aiProviderManager.getActiveProviderStatus();
  const allProviders = aiProviderManager.getAllProvidersStatus();

  return res.json({
    activeProvider: activeStatus,
    providers: allProviders,
    competitionMetadata: {
      challenge: 'Snapdragon® AI Lab Build & Present Challenge',
      edgeAiConcept: 'Zero-cloud document privacy and low-latency career intelligence via on-device Qualcomm Hexagon NPU execution.',
      hardwareTarget: 'Qualcomm® Snapdragon® X Elite / Snapdragon® 8 Gen 3 Mobile Platform',
      snapdragonValidation: 'Snapdragon validation pending',
      validationPolicy: 'Real hardware benchmarks require physical dev kit testing. Simulated metrics, fake NPU utilization, and fabricated latencies are strictly prohibited.',
      supportedWorkloads: [
        {
          id: 'workload_doc_parse',
          name: 'Private Resume Tokenization & PII Stripping',
          targetNpu: 'Qualcomm Hexagon NPU',
          format: 'Local C++ / Regex + Tokenizer Engine',
          status: 'Active (Local)',
          privacyGuarantee: 'Resume text is sanitized locally; personal PII never crosses external network.'
        },
        {
          id: 'workload_skill_gap',
          name: 'Skill Gap Matrix & Role Requirements Inference',
          targetNpu: 'Qualcomm Hexagon NPU (QNN Quantized)',
          format: 'ONNX INT8 / Qualcomm AI Hub Model',
          status: 'Snapdragon validation pending',
          privacyGuarantee: 'Computes exact delta against industry standard benchmarks on-device.'
        },
        {
          id: 'workload_jd_matcher',
          name: 'Job Description Requirement Extraction & Skill Matching',
          targetNpu: 'Qualcomm Hexagon NPU',
          format: 'Local Deterministic Semantic Matcher + LLM',
          status: 'Active (Deterministic Engine) / QNN Pending',
          privacyGuarantee: 'Proprietary JD criteria compared against candidate profile in isolated memory.'
        },
        {
          id: 'workload_interview_eval',
          name: 'Mock Interview Audio & Answer Evaluation',
          targetNpu: 'Qualcomm Hexagon NPU / Whisper-Base Quantized',
          format: 'Qualcomm AI Hub Whisper + Llama-3.2-1B',
          status: 'Snapdragon validation pending',
          privacyGuarantee: 'Candidate voice audio and speech-to-text remain entirely on-device.'
        }
      ]
    },
    privacy: {
      zeroDataLeakage: true,
      onDeviceStorage: 'SQLite WAL mode with client isolation',
      gdprCompliant: true,
      accountDataPermanentErasureSupported: true
    },
    timestamp: new Date().toISOString()
  });
});

export default router;
