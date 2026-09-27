# CareerHub Architecture Specification

## 1. System Overview

CareerHub is an Edge-First, Multi-Tenant Career Operating System engineered to provide autonomous career readiness evaluation, document intelligence, personalized learning trajectories, and job market matching. 

Designed for the **Snapdragon® AI Lab Build & Present Challenge**, CareerHub decouples heavy cloud reliance by routing sensitive workloads—including resume document parsing, PII sanitization, and candidate skill-gap inference—to local on-device execution targets powered by the Qualcomm® Hexagon™ NPU.

```
+-------------------------------------------------------------------------+
|                              Client Tier                                |
|  - React 18 + Vite (Tailwind / Pure M3 Dark Theme)                      |
|  - Capacitor Android Container (Edge Mobile Execution)                 |
|  - Client-side tokenization, real-time rubric evaluation                |
+------------------------------------+------------------------------------+
                                     |
                         HTTPS / REST / WebSocket
                                     |
+------------------------------------+------------------------------------+
|                              Server Tier                                |
|  - Node.js / Express Architecture with TypeScript                       |
|  - Modular Domain Routers: Auth, Resume, Roadmap, Edge AI, Analytics   |
|  - Rate Limiting (express-rate-limit), Security Headers (Helmet)        |
+------------------------------------+------------------------------------+
                                     |
           +-------------------------+-------------------------+
           |                                                   |
           v                                                   v
+-------------------------------+             +---------------------------------+
|      AI Provider Layer        |             |        Data Tier (SQLite)       |
|  - AIProvider Abstraction     |             |  - SQLite (WAL Mode, FKs ON)    |
|  - GeminiCloudProvider        |             |  - Zero-Cloud DB Persistence    |
|  - LocalInferenceProvider     |             |  - GDPR Strict Data Isolation   |
|    (Qualcomm AI Hub Target)   |             |  - Atomic Transactions          |
|  - FallbackDeterministicNLP   |             +---------------------------------+
+-------------------------------+
```

## 2. Core Architectural Pillars

### 2.1 Multi-Layered AI Provider Architecture
The system encapsulates AI inference via the `AIProvider` contract:
- **`GeminiCloudProvider`**: Provides cloud LLM inference via Google Gemini 1.5 Flash when external internet and API credentials are provided.
- **`LocalInferenceProvider`**: Serves as the primary Qualcomm AI Hub integration point. Targets quantized models (Llama 3.2 3B Instruct) compiled for the Qualcomm® Hexagon™ NPU via Qualcomm AI Engine Direct (QNN SDK). Hardware validation status is tracked transparently (`Snapdragon validation pending`).
- **`FallbackDeterministicProvider`**: A zero-network, rule-based NLP and ATS engine capable of 100% offline document parsing and matrix scoring without network access.

### 2.2 Relational Data Isolation & Persistence
- **Storage Engine**: SQLite with Write-Ahead Logging (`PRAGMA journal_mode = WAL`) and full foreign-key cascading enforcement (`PRAGMA foreign_keys = ON`).
- **Zero-Leakage Multi-Tenancy**: Every database table partitions records by `user_id`, enforcing strict tenant boundaries.
- **Permanent Data Erasure**: Full GDPR compliance via atomic cascading delete transaction (`DELETE /api/user/delete`).

## 3. Workload Distribution

| Workload | Primary Target | Fallback Target | Latency Profile |
| :--- | :--- | :--- | :--- |
| **Resume Text & Skill Extraction** | Local Regex / C++ Tokenizer | Server Heuristic Parser | < 15ms (Local) |
| **Skill Gap & Readiness Analysis** | LocalInferenceProvider (QNN) | GeminiCloudProvider / Deterministic | Sub-second |
| **Job Description Matcher** | Local Semantic Evaluator | GeminiCloudProvider | < 50ms |
| **Roadmap Generation** | AIProvider Pipeline | Normalized Milestone Engine | Instant |
| **STAR Rubric Interview Feedback** | Local Model / QNN Whisper | Server Heuristic Evaluator | Sub-second |

## 4. Security & Compliance
1. **Zero Secret Exposure**: No API keys or credentials exist in frontend client bundles.
2. **Document Lifecycle**: Resumes are parsed in temporary memory and can be permanently deleted with zero residual disk storage.
3. **Auditability**: All state mutations generate telemetry events stored in local relational storage without sending identifiers to external analytical networks.
