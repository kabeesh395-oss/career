# Career Backend Security & Performance Audit

**Repository:** `kabeesh395-oss/career`  
**Scope:** `server/src`, backend dependency tree, SQLite schema/query plans, tracked backend artifacts, and existing backend test suites  
**Audit date:** 2026-11-01  
**Method:** static review, dependency audit, query-plan inspection, artifact inspection, TypeScript build, and existing security/QA/load tests. No source code was changed.

## Executive summary

The backend has good foundations: parameterized SQL, JWT authentication, bcrypt password hashing, Helmet, foreign keys, WAL mode, Zod on several routes, and passing repository test suites. However, it is **not ready for internet-facing production** without addressing the critical/high findings below.

The most urgent issues are:

1. **Sensitive resume documents are tracked in the public repository** (`server/uploads/resumes/*`). Treat these as exposed and rotate/remove them from Git history.
2. **Resume deletion/account deletion removes database records but not files on disk**, leaving personal documents behind.
3. **Upload validation trusts client-controlled MIME type or extension**, allowing file-type spoofing and increasing parser/zip-bomb risk.
4. **The dependency tree contains 1 critical and 6 high advisories**, including a direct vulnerable `sqlite3` path and direct vulnerable `zod` version.
5. **Request payloads and analytics fields are insufficiently bounded**, enabling memory/CPU/database-growth denial of service.
6. **JWTs are valid for seven days without revocation or per-request user lookup**, so deleted users and changed roles are not immediately invalidated.

## Findings by severity

### Critical — tracked resume files in a public repository

**Evidence:** `git ls-files server` shows ten tracked files under `server/uploads/resumes/`, including `.txt` and `.docx` resume documents.

**Impact:** These may contain names, contact information, employment history, education, and other personal data. Because the repository is public, the files should be treated as publicly exposed even if the database is not. Removing them from the latest commit is insufficient because Git history and forks/caches may retain them.

**Required action:**

- Immediately determine whether these are real user documents or fixtures.
- If real or potentially real, remove them from the full Git history using a history-rewrite tool, force-push the cleaned repository, and notify affected users if applicable.
- Rotate any credentials or tokens that may have been present in uploaded files.
- Add an explicit repository-level rule preventing `server/uploads/` and database files from being committed.
- Keep test fixtures synthetic and clearly labeled.

### High — uploaded files are retained after deletion or processing failure

**Evidence:** `server/src/routes/resume.routes.ts:57-65` extracts the uploaded file but does not delete it when parsing fails; `server/src/routes/resume.routes.ts:156-158` forwards unexpected failures without cleanup. `server/src/routes/user.routes.ts:17-35` deletes database rows but never removes the corresponding files from `resume.file_path`.

**Impact:** Personal resumes remain on disk after user deletion and after failed/aborted processing. This is a privacy/compliance issue and creates unbounded storage growth.

**Required action:**

- Centralize cleanup in a `finally` block for every upload request.
- During account deletion, enumerate the user's resume paths and securely delete them before/within the deletion workflow.
- Store uploads outside the repository and preferably in private object storage with lifecycle expiration.
- Add a scheduled orphan-file sweeper that deletes files not referenced by a live resume record.
- Do not return absolute `file_path` values in API responses.

### High — dependency vulnerabilities

`npm audit` reported **15 vulnerabilities: 1 critical, 6 high, 6 moderate, and 2 low** across 302 installed dependencies.

Important paths:

- Direct `sqlite3@5.1.7` pulls vulnerable `node-gyp`, `make-fetch-happen`, `cacache`, and `tar`.
- `tar@6.2.1` is reported with critical/high path traversal, symlink, archive parsing, and denial-of-service advisories.
- Direct `zod@3.25.76` is affected by an oversized constrained-collection memory exhaustion advisory; the audit recommends the major upgrade to `zod@4.6.7`.
- `express@4.22.3` resolves `body-parser@1.20.8`, which is reported with a high-severity compressed-request denial-of-service advisory.

**Required action:**

- Upgrade `sqlite3` to the fixed major version if compatibility allows, or replace it with the already-used `better-sqlite3` path and remove the unused vulnerable dependency.
- Upgrade Zod with an explicit compatibility test plan; enforce maximum string/array/object sizes regardless of library version.
- Upgrade or replace Multer 1.x and review `pdf-parse`/DOCX parsing dependencies.
- Run `npm audit --omit=dev` in CI and fail on critical/high production vulnerabilities after an agreed allowlist review.
- Regenerate and commit the lockfile after each dependency change.

### High — upload type validation is spoofable and parser exposure is broad

**Evidence:** `server/src/middleware/upload.ts:27-43` accepts a file if **either** the client MIME type or extension is allowed. `server/src/middleware/upload.ts:45-50` limits the compressed/uploaded file to 10 MB but does not inspect file signatures or decompressed size. `server/src/services/resume.service.ts:25-46` sends PDFs and DOCX files directly to parsers.

**Impact:** An attacker can rename or relabel arbitrary content as `.pdf`, `.docx`, or `.txt`. Malformed PDFs and ZIP-based DOCX files can cause high CPU/memory use, parser crashes, or decompression-bomb behavior. Parser error messages are also returned to clients from `resume.routes.ts:61-64`.

**Required action:**

- Validate magic bytes using a file-signature library, not just extension/MIME.
- Reject mismatched extension/signature combinations.
- Apply separate low limits to extracted text length, page count, DOCX uncompressed size, and processing time.
- Parse in an isolated worker/process with a CPU/memory timeout for untrusted documents.
- Delete rejected files immediately.
- Return a generic extraction error externally and log the detailed parser error internally.

### High — authentication has no revocation and trusts stale token claims

**Evidence:** `server/src/middleware/auth.ts:44-49` issues seven-day JWTs. `server/src/middleware/auth.ts:66-81` verifies the token but does not load the user from the database or check status, token version, or current role.

**Impact:** A token remains valid after account deletion, role changes, or credential compromise until expiry. The `role` in the request is whatever was signed at issuance, not necessarily the current database role.

**Required action:**

- Add a short-lived access token plus refresh-token rotation, or a server-side token-version/revocation check.
- Check that the user still exists and is active for sensitive routes.
- Do not use token role claims for authorization without reconciling them with current server-side role state.
- Add issuer, audience, and explicit algorithm configuration to JWT verification.
- Consider a password-change/logout-all token-version increment.

### High — authentication endpoints lack dedicated brute-force protection

**Evidence:** Only the global limiter is installed in `server/src/server.ts:73-75`; signup/login are not covered by a stricter account/IP limiter. `server/src/routes/auth.routes.ts:13` accepts passwords of six characters.

**Impact:** Attackers can perform credential stuffing and password spraying. Six-character passwords are weak for a production service.

**Required action:**

- Add dedicated login/signup limits by IP and normalized account identifier.
- Add progressive delays or temporary lockout without creating account-existence leaks.
- Require a stronger password policy or use breached-password screening.
- Add audit events for repeated failed login attempts without logging passwords or tokens.

### High — permissive CORS policy with credentials enabled

**Evidence:** `server/src/server.ts:54-70` allows `CORS_ORIGIN=*` and also sets `credentials: true`; outside production, any origin is accepted by the callback.

**Impact:** A misconfigured deployment can allow arbitrary websites to make credentialed browser requests. Wildcard/credential combinations are also easy to misconfigure and can behave differently across browsers and proxies.

**Required action:**

- Require an explicit allowlist in all non-test environments.
- Reject `*` when credentials are enabled.
- Fail startup in production if `CORS_ORIGIN` is missing or wildcard.
- Add integration tests for allowed and rejected origins.

### Medium — unbounded request fields and analytics storage

**Evidence:** `express.json` and `urlencoded` allow 10 MB globally (`server/src/server.ts:95-96`). Analytics accepts arbitrary `eventName`, arbitrary `eventData` (`server/src/routes/analytics.routes.ts:9-13`), and no size bounds. Profile strings have no maximum lengths (`server/src/routes/profile.routes.ts:11-20`). Job descriptions and roadmap target roles are also not bounded.

**Impact:** Authenticated users can consume memory, CPU, and database space with oversized strings, deeply nested JSON, or high-cardinality analytics events. Large resume/JD text is repeatedly copied into memory and prompts.

**Required action:**

- Set small route-specific body limits; do not use a 10 MB global default for ordinary JSON APIs.
- Add maximum lengths to every user-controlled string and array.
- Cap analytics event names, keys, nesting depth, and serialized event size.
- Cap resume/JD extracted text before regex analysis or AI submission.
- Add per-user quotas for expensive operations and storage.

### Medium — internal error details are exposed

**Evidence:** `server/src/middleware/error.ts:15-33` returns `err.message` and `err.details`; resume, GitHub integration, readiness, and account deletion routes also return raw error messages.

**Impact:** Clients may receive local paths, parser/library details, provider response details, or database errors. This helps attackers fingerprint the service and may expose personal data.

**Required action:**

- Return stable generic messages and correlation IDs to clients.
- Keep detailed stack/parser/provider errors in structured server logs with redaction.
- Never return `file_path`, provider payloads, SQL errors, or raw exception text.

### Medium — GitHub integration has no outbound timeout or strict request quota

**Evidence:** `server/src/services/github.service.ts:28-52` performs a fetch without an `AbortSignal` timeout. The connect route has only the broad API limiter, and its username schema has no maximum length.

**Impact:** Slow upstream requests can occupy server resources; repeated authenticated requests can consume the configured GitHub token's quota. The fixed GitHub URL avoids a direct SSRF primitive, but the endpoint still needs abuse controls.

**Required action:**

- Add a short timeout and bounded response/body handling.
- Add a dedicated per-user/IP GitHub-sync limiter and cache with a freshness window.
- Validate usernames against GitHub's allowed character/length rules.
- Return normalized provider errors.

## Performance findings

### Blocking work runs on the Node.js event loop

`better-sqlite3` calls are synchronous throughout the routes/services. Resume extraction uses synchronous `fs.readFileSync` and CPU-heavy PDF/DOCX parsing in the request handler (`resume.service.ts:25-41`). The AI service then waits up to 12 seconds for Gemini (`ai.service.ts:138-174`).

This is acceptable for a low-volume prototype but becomes a latency and throughput bottleneck under concurrent uploads, large documents, or simultaneous AI requests.

**Recommendation:** Move document extraction and AI evaluation to a durable worker queue. Keep the API request short: validate, persist a job, return `202`, and provide authenticated job status. If SQLite remains, use a single database writer boundary and measure busy/lock time.

### AI queue is currently dead code and not production durable

`server/src/services/aiQueue.service.ts` contains an in-memory `Map`, a single-worker `processing` flag, no admission limit for queued jobs, and no persistence. `grep` found only import/shutdown usage in `server/src/server.ts`; no route currently enqueues jobs.

If exposed later without changes, it will lose jobs on restart, process only one expensive job at a time, and allow memory growth from queued payloads. The idempotency key is derived from `JSON.stringify(payload)`, which is CPU/memory-expensive for large payloads.

**Recommendation:** Either remove the unused queue or finish it with durable storage, bounded queue depth, per-user quotas, payload-size caps, worker concurrency, cancellation, retry backoff, and authenticated ownership checks.

### Missing composite indexes cause avoidable sorts

SQLite query plans show a temporary B-tree for ordering in the following common patterns:

- `resumes WHERE user_id = ? ORDER BY created_at DESC`
- `resume_analysis WHERE user_id = ? ORDER BY created_at DESC`
- `roadmaps WHERE user_id = ? ORDER BY created_at DESC`
- `analytics_events WHERE user_id = ? ORDER BY timestamp DESC`

The schema has single-column user indexes but not composite `(user_id, created_at DESC)` / `(user_id, timestamp DESC)` indexes. This is not a current incident at the observed database size, but it will produce extra work as each user's history grows.

**Recommendation:** Add composite indexes matching the access paths and verify with `EXPLAIN QUERY PLAN` after migration. Add pagination instead of returning every resume/analysis/history row.

### Several endpoints return unbounded collections and wide rows

Examples include the resume list (`SELECT *` for all resumes and all analyses), interview history, project/profile collections, and analytics data. Resume list responses include stored extracted text and internal file metadata. This increases response size and exposes more sensitive data than needed.

**Recommendation:** Select only required columns, exclude `extracted_text` and absolute paths from list responses, paginate by cursor, and fetch detailed content only on an authenticated detail endpoint.

### Account deletion is a large synchronous transaction

The deletion route performs many synchronous deletes in one request (`user.routes.ts:17-35`) and returns raw errors. It also does not delete files. At scale, this can create lock contention and long request latency.

**Recommendation:** Mark the account for deletion, run a bounded background deletion workflow, make it idempotent, and report completion status. If immediate deletion is required, delete files and rows in measured batches.

## Positive controls observed

- Parameterized SQLite queries are used consistently; no obvious SQL string interpolation was found in the reviewed routes.
- Foreign keys are enabled and most user-owned tables have `ON DELETE CASCADE`.
- Passwords are hashed with bcrypt rather than stored in plaintext.
- Helmet is enabled.
- JWT verification rejects malformed/expired tokens.
- Many routes use Zod validation.
- Database uses WAL mode and foreign-key enforcement.
- Existing backend build and suites passed: security/E2E, QA, phase 3, and load/chaos tests.

## Recommended remediation order

### Immediate (before public production use)

1. Remove and purge tracked resume artifacts; confirm whether any personal data was exposed.
2. Fix upload cleanup on all failure/deletion paths.
3. Add file-signature validation, parser limits, extracted-text limits, and isolated parsing.
4. Address `sqlite3`/`tar`/`zod`/`body-parser` vulnerabilities and regenerate the lockfile.
5. Enforce explicit production CORS allowlisting.
6. Add auth-specific brute-force limits and stronger password requirements.
7. Stop returning raw internal errors and absolute file paths.

### Next hardening cycle

1. Implement token revocation/current-user checks and explicit JWT issuer/audience/algorithm.
2. Add route-specific schemas with maximum lengths and per-user quotas.
3. Add GitHub upstream timeouts, caching, and dedicated rate limits.
4. Add composite indexes and cursor pagination.
5. Move resume parsing and AI work to a durable worker system.
6. Add tests for IDOR, deleted-user tokens, file cleanup, oversized inputs, malformed documents, CORS, and brute-force limits.

## Verification performed

- `npm run build`: passed.
- `npm run test:security`: passed.
- `npm run test:qa`: passed.
- `npm run test:phase3`: passed.
- `npm run test:load`: passed.
- `npm audit`: 15 vulnerabilities reported (1 critical, 6 high, 6 moderate, 2 low).
- SQLite `EXPLAIN QUERY PLAN`: user indexes are used, but common ordered queries require temporary B-trees.
- Tracked artifact inspection: resume files are present under `server/uploads/resumes/`.
- No code or repository files were changed by this audit.
