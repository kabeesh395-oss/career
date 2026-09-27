# AI Model Selection & Quantization Strategy

## 1. Target Model Specifications

For on-device edge execution within CareerHub, the selected model architectures are specifically tailored for compact parameter size, low memory footprint, and high instruction-following quality:

| Model Identity | Parameters | Precision / Format | Primary Use Case | Target Engine |
| :--- | :--- | :--- | :--- | :--- |
| **Llama-3.2-3B-Instruct** | 3.2 Billion | INT8 (W8A8) / QNN Context | Skill gap analysis, ATS recommendations, interview rubric scoring | Qualcomm Hexagon NPU |
| **Llama-3.2-1B-Instruct** | 1.2 Billion | INT4 (W4A16) / ONNX | Mobile low-power quick text parsing and summary generation | Qualcomm Hexagon NPU |
| **Whisper-Base** | 74 Million | INT8 Quantized | Audio speech-to-text during mock interview simulation | Qualcomm Hexagon NPU |
| **Gemini-1.5-Flash** | Scaled Cloud | FP16 / Cloud API | Cloud fallback for high-concurrency remote web access | Google Cloud TPU |

## 2. Quantization & Qualcomm AI Hub Compilation

### 2.1 Quantization Strategy
- **Weight Precision**: 8-bit Integer (INT8) weights and activations to match the vector registers of the Qualcomm Hexagon Tensor Processor (HTP).
- **Per-Channel Calibration**: Applied during model compilation via Qualcomm AI Hub CLI to prevent perplexity degradation in technical terminology.

### 2.2 Compilation Command Flow (Qualcomm AI Hub Target)
```bash
# 1. Compile model via Qualcomm AI Hub for Snapdragon X Elite
qai-hub compile \
  --model "meta-llama/Llama-3.2-3B-Instruct" \
  --device "Snapdragon X Elite CRD" \
  --options "--target_backend qnn_htp --precision int8"

# 2. Package runtime binary
qai-hub package --compile-job <JOB_ID> --output-dir ./models/snapdragon_qnn
```

## 3. Fallback Deterministic Model Architecture
When running in zero-network environments without mounted weights, CareerHub utilizes `FallbackDeterministicProvider`:
- **Algorithm**: Regex-based token boundary analysis matched against normalized canonical skill schemas.
- **Scoring**: Weighted linear algebra matrix calculation (`earnedScoreWeight / totalScoreWeight`).
- **Reproducibility**: 100% deterministic output with 0ms cold-start latency.
