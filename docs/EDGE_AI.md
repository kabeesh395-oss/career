# Edge AI & Snapdragon® Integration Strategy

## 1. Executive Summary

CareerHub reimagines talent intelligence by moving data-intensive and privacy-sensitive AI workloads directly to edge silicon. Traditional career platforms transmit sensitive personal resumes, salary expectations, employment history, and recorded voice responses to remote third-party cloud LLM endpoints. 

By targeting **Qualcomm® Snapdragon® platforms** (specifically the **Snapdragon® X Elite** for Copilot+ PCs and the **Snapdragon® 8 Gen 3** for mobile devices), CareerHub enables secure, low-latency, offline career guidance.

## 2. Snapdragon® Hardware & Software Stack

```
+--------------------------------------------------------------------+
|                      CareerHub Application Tier                    |
+--------------------------------------------------------------------+
                                 |
+--------------------------------v-----------------------------------+
|               LocalInferenceProvider (AIProvider Layer)             |
+--------------------------------------------------------------------+
                                 |
+--------------------------------v-----------------------------------+
|               Qualcomm AI Hub Model Repository (INT8)              |
|        - Llama-3.2-3B-Instruct (Quantized for Hexagon NPU)          |
|        - Whisper-Base (Speech-to-Text for Mock Interviews)         |
+--------------------------------------------------------------------+
                                 |
+--------------------------------v-----------------------------------+
|              Qualcomm AI Engine Direct (QNN SDK Runtime)            |
|       - Graph Execution, Quantization Mapping, Context Cache       |
+--------------------------------------------------------------------+
                                 |
+--------------------------------v-----------------------------------+
|                   Qualcomm® Hexagon™ NPU Hardware                  |
|                 45 TOPS (Snapdragon X Elite Target)                |
+--------------------------------------------------------------------+
```

## 3. Supported Edge AI Workloads

### 3.1 On-Device Resume PII Stripping & Parsing
- **Problem**: Transmitting raw resumes exposes candidate names, addresses, phone numbers, and past employers.
- **Edge Approach**: Resume parsing and tokenization run entirely inside local client memory. The document never leaves the device untrusted.

### 3.2 Skill Gap Delta Inference
- **Problem**: Comparing 50+ candidate skills against 100+ industry requirements creates high cloud inference costs and network dependencies.
- **Edge Approach**: Local normalized matrices and INT8 embeddings run directly on the Hexagon NPU, calculating exact skill gap deltas in milliseconds.

### 3.3 Target Job Description Matching
- **Problem**: Candidates evaluating confidential corporate job descriptions risk leaking non-public job requisitions to cloud providers.
- **Edge Approach**: The candidate pastes the Job Description into the local matcher. The comparison against their active resume occurs in isolated runtime memory.

## 4. Hardware Validation Status

> **CRITICAL TRANSPARENCY NOTICE**:
> **Status: Snapdragon validation pending.**
>
> In strict accordance with the Qualcomm competition guidelines, this repository does NOT fabricate synthetic NPU utilization gauges, fake token-per-second numbers, or artificial memory bandwidth metrics. Physical device testing is scheduled on Snapdragon reference developer hardware.

## 5. Deployment & Execution Instructions
1. **Model Retrieval**: Pull the target model from Qualcomm AI Hub (`qai-hub download llama-3.2-3b-instruct-quantized`).
2. **Runtime Binding**: Mount the QNN shared libraries (`libQnnCpu.so`, `libQnnHtp.so` for Hexagon Tensor Processor).
3. **Provider Initialization**: Instantiate `LocalInferenceProvider({ modelPath: '/path/to/quantized_model.onnx' })`.
