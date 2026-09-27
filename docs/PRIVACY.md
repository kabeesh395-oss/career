# Privacy Architecture & GDPR Compliance

## 1. Zero-Cloud Privacy Model

Candidate career documents (resumes, cover letters, self-evaluations, interview transcripts) represent sensitive personal data. Standard cloud-based career platforms send raw PDF text to remote cloud APIs, exposing:
- Full candidate names, addresses, phone numbers, email addresses
- Complete employment histories and corporate names
- Academic records and grades

CareerHub resolves this fundamental privacy violation through an **Edge-First Architecture**:

```
[Candidate PDF Document]
          |
          v
[On-Device Tokenizer & PII Sanitizer]
          |
          +---> [Local Storage (SQLite WAL Mode)] -> Data stays on device
          |
          +---> [Qualcomm Hexagon NPU Inference]  -> On-device INT8 Model
          |
   (No Remote Transmission of Personal Data)
```

## 2. Key Privacy Guarantees

1. **Client Isolation**: Every table in CareerHub enforces strict `user_id` separation. No user can view or query another user's documents or evaluations.
2. **Zero Client Secret Exposure**: No API keys or credentials are baked into client distribution builds.
3. **Atomic Account & Data Deletion (GDPR Right to Erasure)**:
   - When a user requests account deletion via the Profile page or `DELETE /api/user/delete`, the system executes an atomic transaction.
   - All associated database records (`resumes`, `resume_analysis`, `roadmaps`, `roadmap_items`, `projects`, `interviews`, `interview_answers`, `user_skills`, `analytics_events`, and `users`) are permanently erased with cascading foreign key guarantees.
   - Temporary document files on disk are unlinked immediately.
4. **Offline Resilience**: The system operates with full deterministic capability even when network connectivity is severed.
