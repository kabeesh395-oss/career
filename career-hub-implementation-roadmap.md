# Career Hub AI — Prioritized Implementation Roadmap

**Repository:** `kabeesh395-oss/career`  
**Prepared:** 2026-11-01  
**Product goal:** Turn Career Hub from a collection of AI-assisted career tools into a measurable, personalized career-readiness system centered on the student's next best action.

## 1. Executive direction

Career Hub should not launch all nine pillars as independent features. The highest-value product loop is:

> **Assess → explain gaps → recommend one next action → help the student complete it → measure evidence → update readiness.**

The roadmap therefore prioritizes the shared readiness data model, evidence collection, and Next Best Action engine before adding broad feature surface area such as jobs and LinkedIn.

## 2. Current capability baseline

The repository already contains meaningful backend foundations:

| Capability | Current state | Roadmap implication |
|---|---|---|
| Authentication and user profiles | Implemented | Harden, add consent and lifecycle controls |
| Skills and skill-gap analysis | Implemented | Normalize evidence and connect to readiness |
| Career analysis | Implemented | Make scoring explainable and persistent |
| Personalized roadmap | Implemented | Add milestone completion and evidence links |
| Resume upload and ATS analysis | Implemented | Hardened upload path; add versioning and comparisons |
| Job-description matching | Implemented | Reuse as a job-readiness signal |
| Mock interviews | Implemented | Add communication and longitudinal scoring |
| Learning resources/progress | Implemented | Connect completion to verified evidence |
| Projects | Implemented | Add project evidence and GitHub links |
| GitHub profile integration | Partially implemented | Expand repository evidence and sync controls |
| Analytics | Implemented | Convert into product outcome metrics |
| AI provider/fallback layer | Implemented | Add structured output validation and budget controls |
| AI career coach/chat | Partial or not exposed as a complete product flow | Build a bounded, context-aware assistant |
| Aptitude assessment | Missing | Priority P1 |
| Communication practice | Missing/partial | Priority P1 |
| Jobs and internships | Missing | Priority P2 |
| LinkedIn analysis | Missing | Priority P2 |
| Nine-pillar readiness dashboard | Partial | Priority P0/P1 |
| Durable background processing | Missing | Required before scale |

## 3. Priority model

- **P0 — Foundation and trust:** Required before declaring the product production-ready.
- **P1 — Core differentiation:** Directly strengthens the assess → action → evidence loop.
- **P2 — Growth and ecosystem:** Expands acquisition, retention, and career utility.
- **P3 — Scale and monetization:** Improves reliability, institutional use, and paid offerings.

## 4. P0 — Foundation and trust

### P0.1 Establish the canonical readiness model

**Why first:** The nine pillars currently exist mostly as a product concept. Without a common scoring contract, each feature will produce disconnected metrics.

Define a `readiness_snapshots` model containing:

- `user_id`
- `overall_score`
- `score_version`
- `pillar_scores_json`
- `confidence_json`
- `evidence_coverage_json`
- `calculated_at`
- `source_event_id`

Define the nine pillars explicitly:

1. Technical skills
2. Resume and ATS readiness
3. Projects and GitHub evidence
4. Aptitude
5. Communication
6. Interview performance
7. LinkedIn/profile quality
8. Job alignment
9. Learning consistency

Every pillar must return both a score and an explanation:

```json
{
  "score": 72,
  "confidence": 0.68,
  "evidence": ["resume_analysis:abc", "github_sync:def"],
  "gaps": ["No production deployment evidence"],
  "nextActions": ["Publish one deployed project with README and tests"]
}
```

**Acceptance criteria:**

- Same input produces a deterministic score for the same score version.
- Every displayed score has evidence, timestamp, and confidence.
- Missing data reduces confidence rather than silently generating a fabricated score.
- Score changes are auditable through an event history.

### P0.2 Make Next Best Action the product orchestrator

Replace the current isolated recommendation behavior with a single action contract:

- `id`
- `pillar`
- `title`
- `reason`
- `estimated_minutes`
- `impact_score`
- `difficulty`
- `prerequisites`
- `deep_link`
- `evidence_required`
- `status`
- `completed_at`

Initial action ranking should combine:

```text
priority = gap severity × confidence × expected score impact × user intent × feasibility
```

**Initial action types:**

- Complete profile
- Upload or improve resume
- Add verified skill evidence
- Finish a roadmap milestone
- Complete an aptitude quiz
- Practice an interview answer
- Improve a GitHub repository
- Analyze a target job

**Acceptance criteria:**

- Dashboard displays exactly one primary next action and up to three secondary actions.
- Completing an action creates evidence and triggers a readiness recalculation.
- The same action is not repeatedly recommended after completion.

### P0.3 Finish security, privacy, and operational foundations

Before adding more personal data integrations:

- Purge historical resume artifacts from Git history after explicit approval.
- Complete token revocation/current-user validation.
- Add strict per-route input limits and authentication-specific rate limits.
- Add consent records for resume, GitHub, LinkedIn, and job data.
- Add data export and complete deletion workflows, including object-storage files.
- Move uploaded documents and AI work out of synchronous request paths.
- Add production error redaction, audit logging, and request correlation.
- Define retention periods for resumes, interview answers, analytics, and integrations.

**Acceptance criteria:**

- Security tests cover IDOR, deleted-user tokens, oversized inputs, malformed files, and CORS.
- A user can see connected data sources and revoke each one.
- Deletion removes database rows, files, cached AI outputs, and integration data.

## 5. P1 — Core Career Hub differentiation

### P1.1 Nine-pillar readiness dashboard

Build the dashboard around the canonical readiness model instead of isolated feature cards.

**UI sections:**

- Overall readiness score with confidence indicator
- Nine-pillar radial or grid view
- Score movement over time
- Evidence coverage indicator
- Strengths and gaps
- Primary Next Best Action
- Recent completed activities
- Target-role comparison

**Backend work:**

- `GET /api/readiness/current`
- `GET /api/readiness/history`
- `POST /api/readiness/recalculate`
- `GET /api/readiness/evidence`

**Acceptance criteria:**

- A new user sees an honest empty state, not a fabricated score.
- Each pillar identifies what is known, unknown, and recommended next.
- Readiness history is immutable and versioned.

### P1.2 Aptitude and reasoning assessment

Add quantitative, logical, verbal, and data-interpretation assessments.

**MVP scope:**

- Question bank with difficulty and topic metadata
- Timed quiz sessions
- Randomized question selection
- Attempt and answer persistence
- Scoring by topic and difficulty
- Explanations after submission
- Retake cooldown and improvement tracking

**Suggested APIs:**

- `GET /api/aptitude/assessments`
- `POST /api/aptitude/sessions`
- `POST /api/aptitude/sessions/:id/answers`
- `POST /api/aptitude/sessions/:id/complete`
- `GET /api/aptitude/history`

**Data model:**

- `aptitude_question_banks`
- `aptitude_questions`
- `aptitude_sessions`
- `aptitude_answers`
- `aptitude_scores`

**Acceptance criteria:**

- Questions are server-selected and answers are not exposed before submission.
- A session can resume safely after network interruption.
- Score includes topic-level gaps and feeds the readiness engine.

### P1.3 Communication practice

Build a low-cost text-first communication coach before adding live voice.

**MVP exercises:**

- Explain a project in 60 seconds
- STAR-format behavioral response
- Technical concept explanation
- Written professional response
- Email and workplace communication scenarios

**Outputs:**

- Clarity
- Structure
- Relevance
- Conciseness
- Professional tone
- Suggested improvement
- Before/after comparison

**Later:** speech-to-text, filler-word analysis, pacing, pronunciation, and confidence signals.

**Acceptance criteria:**

- User content is not stored indefinitely without consent.
- AI feedback is rubric-based and reproducible.
- Communication score contributes to the pillar score only after a minimum number of attempts.

### P1.4 Evidence-backed GitHub portfolio analysis

The repository already has basic GitHub profile analysis. Expand it into evidence rather than vanity metrics.

Analyze:

- Repository activity consistency
- README quality
- Test and CI presence
- License and documentation
- Deployment/demo links
- Commit quality and recency
- Language and architecture breadth
- Original work versus forks
- Project completeness

**Suggested APIs:**

- `POST /api/integrations/github/sync`
- `GET /api/integrations/github/evidence`
- `POST /api/projects/:id/link-github`

**Acceptance criteria:**

- GitHub token scope is minimal and read-only.
- Sync is cached, rate-limited, cancellable, and manually revocable.
- Recommendations cite the exact repository evidence that produced them.

### P1.5 Resume and job-readiness iteration loop

Extend the existing resume and JD analysis into a measurable improvement workflow:

1. Upload resume version.
2. Analyze against a target role or job description.
3. Show highest-impact gaps.
4. Let the user record an improvement.
5. Re-analyze a new version.
6. Compare score, evidence, and remaining gaps.

**Add:**

- Resume version history
- Resume-to-job match history
- Diff between analyses
- Exportable improvement checklist
- User confirmation before AI-generated resume text is applied

**Acceptance criteria:**

- Every analysis references a resume version and target role.
- Users can compare two versions without losing earlier results.
- AI suggestions never invent skills, employers, certifications, or metrics.

## 6. P2 — Growth and ecosystem features

### P2.1 Jobs and internship discovery

Do not begin with a broad scraping system. Start with one compliant provider or user-supplied job descriptions.

**MVP:**

- Search by role, location, experience, work mode, and skills
- Save jobs
- Match a job against the user's profile/resume
- Application status tracking
- Deadline reminders
- Missing-skill explanation

**Data model:**

- `job_sources`
- `jobs`
- `saved_jobs`
- `job_matches`
- `applications`

**Important constraints:**

- Respect provider terms and rate limits.
- Cache normalized job records.
- Deduplicate by canonical URL and source ID.
- Do not promise application submission in the first release.

### P2.2 LinkedIn/profile review

Start with a user-provided profile export or manually entered profile fields rather than unauthorized scraping.

Review:

- Headline clarity
- About section
- Skills alignment
- Evidence and quantified outcomes
- Project links
- Target-role keywords
- Consistency with resume and GitHub

**Acceptance criteria:**

- Explicit user consent and data deletion.
- No scraping that violates provider terms.
- Contradictions between resume, profile, and GitHub are surfaced as questions—not silently corrected.

### P2.3 AI Career Coach

Build a bounded assistant around the canonical user context, not a generic chatbot.

**Context layers:**

- Current profile
- Target role
- Readiness snapshot
- Resume analysis
- Roadmap state
- Recent actions
- Connected evidence sources

**Safety rules:**

- Show which data sources informed an answer.
- Do not fabricate market facts, job availability, or qualifications.
- Use retrieval from approved learning resources.
- Keep prompts and outputs within route-specific size limits.
- Apply per-user AI budgets and usage telemetry.

**Acceptance criteria:**

- Responses can recommend existing platform actions with deep links.
- Every recommendation is traceable to a gap, goal, or evidence item.
- User can delete conversation history.

### P2.4 Learning marketplace and resource quality

Improve the existing learning resources feature by adding:

- Resource freshness checks
- Provider and difficulty metadata
- Skill outcomes
- Completion evidence
- User ratings
- Duplicate detection
- Broken-link monitoring

Connect resource completion to roadmap milestones and readiness evidence only when completion is verifiable.

## 7. P3 — Scale, institutions, and monetization

### P3.1 Durable background architecture

Move long-running tasks to a durable worker system:

- Resume parsing
- AI analysis
- GitHub sync
- Readiness recalculation
- Job matching
- Notifications

Required properties:

- Durable job state
- Retry with exponential backoff
- Dead-letter handling
- Per-user quotas
- Idempotency
- Cancellation
- Progress status
- Worker metrics

### P3.2 Notifications and habit loop

Add opt-in notifications for:

- Next action reminders
- Learning targets
- Interview practice
- Job deadlines
- Score changes
- Stale integrations

Avoid spam by using frequency caps, quiet hours, and user-configurable preferences.

### P3.3 Institution and placement dashboard

After the individual product is stable, add institution-level capabilities:

- Cohort readiness trends
- Skill-gap heatmaps
- Placement preparation progress
- Aggregated, privacy-preserving analytics
- Mentor assignments
- Assignment templates
- Institution content libraries

Never expose individual resume, interview, or profile data to an institution without explicit authorization and role-based access.

### P3.4 Monetization candidates

Potential paid capabilities:

- Advanced resume versioning and tailored applications
- Premium mock interview packs
- Human mentor review
- Institution analytics
- Job-match automation
- Advanced communication/voice coaching

Do not monetize the basic readiness baseline or hide essential safety/privacy controls behind a paywall.

## 8. Suggested release sequence

### Release 0 — Trust and instrumentation

- Finish critical security remediation.
- Add readiness schema and score versioning.
- Add evidence and consent models.
- Add product analytics for activation, action completion, and retention.
- Add empty-state and confidence handling.

**Exit criteria:** No fabricated scores; all data sources are consented and deletable.

### Release 1 — Career readiness MVP

- Readiness dashboard
- Next Best Action orchestrator
- Improved resume/JD iteration
- Roadmap evidence links
- GitHub evidence analysis
- Basic communication text exercises

**Exit criteria:** A new student can complete onboarding, receive one evidence-backed action, complete it, and observe a measurable score/evidence update.

### Release 2 — Assessment and practice

- Aptitude assessments
- Communication rubric and history
- Interview longitudinal reporting
- Readiness history and comparisons
- Notifications and habit loop

**Exit criteria:** At least three pillars can update from real user activity with confidence scores.

### Release 3 — Career marketplace

- Jobs/internships search
- Job matching
- Saved jobs and application tracking
- LinkedIn/profile review using compliant input methods
- AI Career Coach with grounded context

**Exit criteria:** A user can move from target role to matched opportunity to personalized preparation plan.

### Release 4 — Scale and institutions

- Durable workers
- Provider integrations and caching
- Institution dashboard
- Mentor workflows
- Paid plans

## 9. 12-week execution plan

| Weeks | Focus | Deliverables |
|---|---|---|
| 1–2 | Trust foundation | Security hardening, consent/retention, score contract, evidence model |
| 3–4 | Readiness core | Snapshot calculation, pillar APIs, dashboard, confidence states |
| 5–6 | Next action loop | Action ranking, deep links, completion events, roadmap/resume integration |
| 7–8 | Evidence | GitHub evidence, resume version comparison, project evidence |
| 9–10 | Assessment | Aptitude MVP, communication text exercises, interview trend reporting |
| 11 | Retention | Notifications, score history, weekly goals, product analytics |
| 12 | Release hardening | Load/security tests, privacy review, mobile integration, beta release |

Jobs, LinkedIn, and the full AI Career Coach should follow the first stable readiness MVP rather than compete with it.

## 10. Definition of done for every feature

Every feature should ship with:

- Authenticated, user-scoped API routes
- Zod schema with maximum lengths and array limits
- Unit tests for scoring/business rules
- Authorization and IDOR tests
- Empty, loading, retry, and error states
- Audit events without sensitive payload leakage
- Data retention and deletion behavior
- Rate limits and per-user quotas for expensive work
- Structured output validation for AI responses
- Performance budget and query-plan review
- Product analytics for activation and completion
- Documentation and a short demo flow

## 11. North-star metrics

Track product outcomes rather than only usage:

- Percentage of new users completing a target-role profile
- Time to first evidence-backed Next Best Action
- Weekly action completion rate
- Readiness score confidence and evidence coverage
- Resume improvement from first to latest version
- Aptitude/communication improvement over repeated attempts
- Target-job match-to-application conversion
- Retention after the first completed action
- Percentage of AI recommendations accepted and completed
- Percentage of users with at least three evidence-backed pillars

## Final prioritization

The recommended order is:

1. **Readiness data model and evidence/consent foundation**
2. **Next Best Action orchestration**
3. **Nine-pillar dashboard with confidence and history**
4. **Resume/JD improvement loop**
5. **GitHub evidence analysis**
6. **Aptitude assessment**
7. **Communication practice**
8. **Interview longitudinal scoring**
9. **Jobs and internship matching**
10. **Compliant LinkedIn/profile review**
11. **Grounded AI Career Coach**
12. **Durable workers, institution features, and monetization**

This sequence creates a coherent career-readiness product before expanding into higher-risk external integrations and marketplace features.
