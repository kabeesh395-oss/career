# 📕 CareerHub Production Runbooks & SRE Disaster Recovery Guide

## 1. System Targets & Objectives
- **Availability SLO:** 99.9% uptime per calendar month.
- **Recovery Point Objective (RPO):** < 15 minutes (SQLite WAL replication / snapshot intervals).
- **Recovery Time Objective (RTO):** < 10 minutes for full instance recovery.
- **API Latency SLA:** p50 < 45ms, p95 < 120ms, p99 < 350ms (excluding AI background queue jobs).

---

## 2. Incident Runbooks

### 🚨 Runbook A: AI Provider Outage / Gemini Rate Limiting (429 / 503)
- **Symptoms:** `AiQueueService` reports retries or job state transition to `RETRYING`/`FAILED`.
- **Immediate Diagnostic Check:**
  ```bash
  curl -s http://localhost:5000/api/v1/ready
  ```
- **Mitigation Procedure:**
  1. `AiProviderManager` automatically routes new requests to `FallbackDeterministicProvider`.
  2. Non-AI functionality (roadmaps, portfolio tracking, local ATS parsing) continues uninterrupted.
  3. Pending jobs in `AiQueueService` automatically retry using exponential backoff (attempts 1..3).
- **Verification:** Monitor `/api/v1/health` status returns `status: "healthy"`.

---

### 🚨 Runbook B: Database Corruption or Disk Storage Exhaustion
- **Symptoms:** Server logcat outputs `better-sqlite3: disk I/O error` or `/ready` endpoint returns HTTP 503.
- **Recovery Procedure:**
  1. Stop backend service process: `systemctl stop careerhub-backend`.
  2. Verify disk space: `df -h`. Clean stale log files if space < 10%.
  3. Execute automated SQLite WAL checkpoint & integrity check:
     ```bash
     sqlite3 careerpilot.db "PRAGMA integrity_check;"
     ```
  4. If corruption detected, restore latest verified SQLite backup snapshot:
     ```bash
     cp ./backups/careerpilot_latest.db ./careerpilot.db
     ```
  5. Restart backend server: `systemctl start careerhub-backend`.
- **Verification:** Execute `npm run test:security` to confirm multi-tenant data integrity.

---

### 🚨 Runbook C: High API Rate-Limit Spikes or Unauthorized Scraping
- **Symptoms:** Alert fires for HTTP 429 response rate > 5% of total traffic.
- **Mitigation Procedure:**
  1. Check rate limit logs for offender IP/User ID in `[HTTP]` access logs.
  2. Confirm `apiLimiter` (100 req/min) and `expensiveAiLimiter` (10 req/min) are active.
  3. If required, update `RATE_LIMIT_MAX` environment variable in `.env` and reload configuration.

---

## 3. Disaster Recovery & Backup Verification
- **Automated Backup Strategy:** SQLite DB WAL snapshot copied every 6 hours to isolated encrypted object storage.
- **Restore Testing Protocol:** Run `npm run test:phase3` on restored database file to verify tables, foreign key integrity, and canonical skill catalog state.
