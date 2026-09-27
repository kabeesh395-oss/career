# CareerHub Demo Guide & Evaluation Script

## 1. Quick Start & Setup

### 1.1 Running the Unified Application
```bash
# Install dependencies
npm run install:all

# Start backend server (Port 5000)
npm run server

# Start client development server (Port 5173)
npm run client
```

## 2. Competition Walkthrough Script (5-Minute CUJ)

### Step 1: Authentication & Onboarding
- Navigate to the login screen.
- Log in with existing credentials or create a new user profile.
- Notice the instantaneous JWT session hydration and dark M3 design language.

### Step 2: Edge AI Architecture Inspection (Competition Mode)
- In the sidebar, select **Edge AI (Snapdragon)** under the Platform section.
- **Inspect**:
  - The transparent notice: **"Snapdragon validation pending"** (confirming zero fabricated metrics).
  - The 4 supported edge workloads (Private Resume Tokenization, Skill Gap Matrix, Job Description Matcher, Interview Evaluation).
  - Click **"Run Real Measurement"** to test live client-side tokenizer latency and backend roundtrip time.

### Step 3: Real Resume Upload & ATS Parsing
- In the sidebar, navigate to **Resume**.
- Upload a real resume file (PDF or TXT).
- **Observe the multi-state visual transition**:
  - *Uploading* → *Parsing text & extracting skills* → *Evaluating ATS scoring* → *Final ATS Results*.
- Review detected technical skills, quantifiable impact scores, brevity density, strengths, and recommendations.

### Step 4: Job Description Analyzer (Phase 6)
- Inside the Resume workspace, switch to the **Job Description Matcher** tab.
- Click **"Paste Sample JD"** (or paste any real job posting).
- Click **"Extract Requirements & Compare"**.
- Review the real-time breakdown:
  - **Matched Skills**: Direct matches with real sentence excerpts quoted from the uploaded resume.
  - **Partial / Transferable Skills**: Foundational competencies detected (e.g. React → Next.js).
  - **Missing Skills**: Priority gaps with actionable project recommendations.
  - **Overall Match Percentage**: Calculated mathematically from actual counts.

### Step 5: Personalized Roadmap Generation
- In the sidebar, click **Roadmap**.
- Click **"Generate Roadmap"**.
- Expand any milestone card to see:
  - **Skill**
  - **Why It Matters** (architectural justification)
  - **Learning Objective**
  - **Practice Project**
  - **Interview Relevance**
- Toggle milestones to observe genuine progress percentage calculation.

### Step 6: Verified Curated Learning
- In the sidebar, click **Learning**.
- Filter by All / In Progress / Completed.
- Click any verified resource link (e.g. System Design Primer, TypeScript Deep Dive, React Docs) to verify real, non-fabricated URLs.

### Step 7: Activity Telemetry & Privacy Erasure
- In the sidebar, click **Analytics**.
- Review empirical metrics calculated strictly from your completed actions.
- In the sidebar, click **Profile**.
- Scroll to the **Danger Zone** and verify the GDPR atomic account deletion workflow.
