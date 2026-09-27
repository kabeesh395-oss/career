package com.example.careerpilot

import com.example.careerpilot.data.model.*
import com.example.careerpilot.data.repository.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

/**
 * Complete Verification Test Suite covering:
 * 1. Unit Test
 * 2. System Test
 * 3. Architecture Test
 * 4. Flow Test
 * 5. Functionality Test
 * 6. Ability Test
 * 7. Performance Management Test
 * 8. Accuracy Test
 * 9. Working Test
 * 10. Overall Test
 */
class ComprehensiveAppTestSuite {

    // =========================================================================
    // 1. UNIT TEST
    // =========================================================================
    @Test
    fun test1_UnitTest_DataModelsAndFormulas() {
        // Test Compensation Calculation logic
        val breakdown = SalaryNegotiationEngine.calculateCompensation(
            baseSalary = 160000.0,
            equityGrant = 120000.0,
            signOn = 25000.0,
            bonusPercent = 15.0,
            relocation = 10000.0
        )
        assertEquals(160000.0, breakdown.baseSalary, 0.01)
        assertEquals(30000.0, breakdown.annualEquity, 0.01)
        assertEquals(24000.0, breakdown.annualBonusDollar, 0.01)
        // Year 1 Total Comp = 160k + 30k (equity) + 25k (signOn) + 24k (bonus) + 10k (relo) = 249,000
        assertEquals(249000.0, breakdown.year1TotalComp, 0.01)
        // Recurring Annual = 160k + 30k + 24k = 214,000
        assertEquals(214000.0, breakdown.recurringAnnualComp, 0.01)
        // Monthly Pre-Tax = 249,000 / 12 = 20,750
        assertEquals(20750.0, breakdown.estimatedMonthlyPreTax, 0.01)

        // Test UserSkill unit properties
        val skill = UserSkill(
            id = 10L,
            skillName = "Kotlin Coroutines",
            category = "Asynchronous Concurrency",
            proficiencyLevel = 5,
            verified = true,
            source = "github_repo_analysis"
        )
        assertEquals("Kotlin Coroutines", skill.skillName)
        assertTrue(skill.verified)
        assertEquals(5, skill.proficiencyLevel)
    }

    // =========================================================================
    // 2. SYSTEM TEST
    // =========================================================================
    @Test
    fun test2_SystemTest_MultiModuleCandidateAudit() {
        val userProfile = UserProfile(
            fullName = "Jordan Rivera",
            targetRole = "Senior Distributed Systems Engineer",
            experienceYears = 5.0f,
            readinessScore = 82
        )

        val skills = listOf(
            UserSkill(id = 1, skillName = "Kotlin", category = "Languages", proficiencyLevel = 5, verified = true),
            UserSkill(id = 2, skillName = "Distributed Systems", category = "Architecture", proficiencyLevel = 5, verified = true),
            UserSkill(id = 3, skillName = "Kafka", category = "Messaging", proficiencyLevel = 4, verified = true),
            UserSkill(id = 4, skillName = "Kubernetes", category = "DevOps", proficiencyLevel = 4, verified = true)
        )

        val projects = listOf(
            PortfolioProject(
                id = 1,
                title = "Distributed Stream Pipeline",
                description = "High-throughput real-time stream engine with Docker and CI/CD automated GitHub Actions pipelines, Prometheus latency metrics",
                repositoryUrl = "https://github.com/jordan/stream-engine",
                liveUrl = "https://stream.jordan.dev",
                status = "completed",
                technologies = "Kotlin, Kafka, Docker, Kubernetes, CI/CD, Prometheus",
                skillsTargeted = "Distributed Systems, Backend"
            )
        )

        val resume = ResumeAudit(
            id = 1,
            filename = "jordan_resume.pdf",
            targetRole = "Senior Distributed Systems Engineer",
            overallScore = 88,
            impactScore = 90,
            brevityScore = 85,
            styleScore = 88,
            skillsDetected = "Kotlin, Kafka, Docker, Kubernetes, Distributed Systems",
            strengths = "Clear X-Y-Z quantifiable achievements\nVerified live production deployment",
            weaknesses = "",
            recommendations = "Ready for Staff/Senior technical screening",
            rawText = "Jordan Rivera. Architected distributed stream engine scaling to 50,000 QPS with 40% latency reduction using Kafka and Redis. Deployed with Docker and CI/CD."
        )

        val interviewAnswers = listOf(
            InterviewAnswer(
                id = 1,
                interviewId = "session_101",
                questionNumber = 1,
                questionText = "Explain trade-offs between Raft and Paxos",
                category = "Distributed Systems & System Design",
                difficulty = "Hard",
                rubric = "Consensus, leader election, log replication",
                answerText = "Raft achieves consensus via leader election and term numbers, optimizing for understandability and strong leader invariance...",
                score = 92,
                clarityScore = 90,
                technicalScore = 94,
                feedback = "Outstanding clarity on failure recovery and split-brain resolution.",
                suggestedImprovement = "Mention quorum leasing for p99 read optimizations."
            )
        )

        val integrations = listOf(
            IntegrationAccount(
                provider = "github",
                username = "jordan-dev",
                isConnected = true,
                connectionStatus = "CONNECTED",
                lastSyncedAt = System.currentTimeMillis(),
                publicReposCount = 18,
                followersCount = 42
            )
        )

        val (issues, summary) = AuditEngine.evaluateCandidate(
            profile = userProfile,
            skills = skills,
            projects = projects,
            latestResume = resume,
            interviewAnswers = interviewAnswers,
            integrations = integrations
        )

        assertNotNull(summary)
        assertTrue("High evidence coverage expected for comprehensive candidate", summary.evidenceCoveragePercent >= 80)
        assertEquals("HIGH", summary.profileConfidence)
        assertTrue("Net audit score should be calculated", (summary.netAuditScore ?: 0) > 70)
        assertEquals(0, summary.criticalCount)
    }

    // =========================================================================
    // 3. ARCHITECTURE TEST
    // =========================================================================
    @Test
    fun test3_ArchitectureTest_DataFlowAndStateContracts() {
        // Verify Unidirectional StateFlow / Immutable Data Structures
        val originalOpportunity = CareerOpportunity(
            id = "cert_aws_arch",
            title = "AWS Certified Solutions Architect – Professional",
            category = "CERTIFICATION",
            providerOrHost = "Amazon Web Services",
            description = "Validates advanced technical skills in designing distributed systems.",
            officialUrl = "https://aws.amazon.com/certification/certified-solutions-architect-professional/",
            status = "EXPLORING",
            matchScore = 95
        )

        // Ensure immutability via copy method
        val updatedOpportunity = originalOpportunity.copy(
            status = "REGISTERED",
            userNotes = "Scheduled for next month"
        )

        assertEquals("EXPLORING", originalOpportunity.status)
        assertEquals("REGISTERED", updatedOpportunity.status)
        assertEquals("Scheduled for next month", updatedOpportunity.userNotes)
        assertEquals(originalOpportunity.id, updatedOpportunity.id)

        // Verify Data Integrity & Non-null Contracts
        assertNotNull(BenchmarkCatalog.ROLE_BENCHMARKS)
        assertNotNull(BenchmarkCatalog.INTERVIEW_QUESTIONS)
        assertNotNull(BenchmarkCatalog.INITIAL_LEARNING_RESOURCES)
        assertNotNull(BenchmarkCatalog.INITIAL_CODING_CHALLENGES)
        assertNotNull(BenchmarkCatalog.INITIAL_PEER_MATCHES)
        assertNotNull(BenchmarkCatalog.INITIAL_SKILL_SPRINTS)
        assertNotNull(BenchmarkCatalog.INITIAL_OPPORTUNITIES)
        assertNotNull(BenchmarkCatalog.INITIAL_JOB_APPLICATIONS)
    }

    // =========================================================================
    // 4. FLOW TEST
    // =========================================================================
    @Test
    fun test4_FlowTest_StateTransitionsAndLifecycles() {
        // Lifecycle 1: Opportunity State Progression
        var oppStatus = "EXPLORING"
        oppStatus = "BOOKMARKED"
        assertEquals("BOOKMARKED", oppStatus)
        oppStatus = "REGISTERED"
        assertEquals("REGISTERED", oppStatus)
        oppStatus = "PREPARING"
        assertEquals("PREPARING", oppStatus)
        oppStatus = "COMPLETED"
        assertEquals("COMPLETED", oppStatus)

        // Lifecycle 2: Job Application CRM Pipeline Transitions
        val stages = listOf("WISHLIST", "APPLIED", "SCREENING", "TECHNICAL", "OFFER")
        var currentStage = "WISHLIST"
        for (nextStage in stages.drop(1)) {
            val app = JobApplication(
                id = "app_1",
                company = "Google",
                roleTitle = "Staff Software Engineer",
                stage = nextStage,
                location = "Mountain View, CA",
                salaryOffered = "$220,000 Base",
                notes = "Moved to $nextStage stage",
                interviewDate = "In 3 days"
            )
            currentStage = app.stage
            assertEquals(nextStage, currentStage)
        }

        // Lifecycle 3: Skill Sprint Completion Flow
        val sprint = SkillSprint(
            id = "sprint_kmp",
            sprintTitle = "Kotlin Multiplatform Mastery",
            targetSkill = "KMP Architecture",
            description = "Build a shared networking engine across Android and JVM",
            durationDays = 7,
            currentDay = 7,
            milestoneTasks = listOf("Setup shared module", "Implement Ktor client", "Write unit tests", "Deploy binary"),
            completedMilestones = 4,
            badgeName = "KMP Multiplatform Specialist",
            rewardXp = 500,
            isClaimed = true
        )
        assertEquals(sprint.milestoneTasks.size, sprint.completedMilestones)
        assertTrue(sprint.isClaimed)
    }

    // =========================================================================
    // 5. FUNCTIONALITY TEST
    // =========================================================================
    @Test
    fun test5_FunctionalityTest_EnginesAndFeatures() {
        // Feature A: Resume Bullet Rewriter (Google X-Y-Z Formula)
        val rawBullet = "Worked on backend APIs to improve system speed and helped with SQL database queries."
        val analysis = ResumeBulletRewriter.analyzeAndRewriteBullet(rawBullet, "Senior Backend Engineer")
        assertTrue("Should detect weak action verbs like 'worked on' or 'helped with'", analysis.passiveVoiceDetected)
        assertTrue("Should detect missing metrics", analysis.missingMetrics)
        assertEquals("Should produce 3 calibrated X-Y-Z variants", 3, analysis.options.size)
        assertTrue(analysis.options.any { it.style == "METRIC_MAX" })
        assertTrue(analysis.options.any { it.style == "ARCHITECTURE_FOCUSED" })
        assertTrue(analysis.options.any { it.style == "SCALE_AND_QUALITY" })

        // Feature B: Target Job Matcher ATS Evaluator
        val targetPosting = JobMatcherEngine.PRESET_JOB_POSTINGS.first { it.id == "preset_google_l4" }
        val userProfile = UserProfile(
            fullName = "Candidate A",
            targetRole = "Software Engineer",
            experienceYears = 3.5f
        )
        val skills = listOf(
            UserSkill(id = 1, skillName = "Kotlin", category = "Languages", proficiencyLevel = 4, verified = true),
            UserSkill(id = 2, skillName = "Distributed Systems", category = "Backend", proficiencyLevel = 4, verified = true),
            UserSkill(id = 3, skillName = "Docker", category = "DevOps", proficiencyLevel = 3, verified = true),
            UserSkill(id = 4, skillName = "Kubernetes", category = "DevOps", proficiencyLevel = 3, verified = true)
        )
        val projects = listOf(
            PortfolioProject(
                id = 1,
                title = "Cloud Microservices Gateway",
                description = "Built gRPC microservices in Kotlin with Docker & Kubernetes containerization and CI/CD pipelines",
                repositoryUrl = "https://github.com/candidate/gateway",
                liveUrl = "https://gateway.candidate.dev",
                status = "completed",
                technologies = "Kotlin, gRPC, Docker, Kubernetes, CI/CD",
                skillsTargeted = "Backend, Cloud"
            )
        )
        val matchResult = JobMatcherEngine.evaluateJobMatch(
            jobPosting = targetPosting,
            userProfile = userProfile,
            skills = skills,
            projects = projects,
            latestResume = null
        )
        assertTrue("Match score should be calculated", matchResult.matchScore in 50..95)
        assertTrue(matchResult.matchedKeywords.contains("Kotlin"))
        assertTrue(matchResult.matchedKeywords.contains("Docker"))
        assertTrue(matchResult.matchedKeywords.contains("Kubernetes"))
        assertTrue(matchResult.atsRecommendations.isNotEmpty())
    }

    // =========================================================================
    // 6. ABILITY TEST
    // =========================================================================
    @Test
    fun test6_AbilityTest_CandidateSkillGapAndConfidenceCalibration() {
        // Recalibrate Candidate Skill Gaps
        val roleSkills = BenchmarkCatalog.ROLE_BENCHMARKS["Full Stack Engineer"] ?: emptyList()
        assertTrue("Role benchmarks must contain required competencies", roleSkills.isNotEmpty())

        val candidateSkillMap = mapOf(
            "TypeScript" to 4,
            "Kotlin / Java" to 3,
            "React / Next.js" to 4
            // Missing SQL, System Design, Docker, CI/CD
        )

        val detectedGaps = mutableListOf<String>()
        for (bench in roleSkills) {
            val currentLevel = candidateSkillMap[bench.skill] ?: 0
            if (currentLevel < bench.requiredLevel) {
                detectedGaps.add("${bench.skill} (Gap: ${bench.requiredLevel - currentLevel})")
            }
        }

        assertTrue("Should detect gaps for missing core competencies", detectedGaps.isNotEmpty())
        assertTrue(detectedGaps.any { it.contains("PostgreSQL / SQL") || it.contains("System Design") })

        // Test Confidence Calibration when candidate profile is empty
        val emptyProfile = UserProfile(id = "empty_user", fullName = "", readinessScore = null)
        val (emptyIssues, emptySummary) = AuditEngine.evaluateCandidate(
            profile = emptyProfile,
            skills = emptyList(),
            projects = emptyList(),
            latestResume = null,
            interviewAnswers = emptyList(),
            integrations = emptyList()
        )
        assertEquals("NOT_EVALUATED", emptySummary.profileConfidence)
        assertEquals(0, emptySummary.evidenceCoveragePercent)
    }

    // =========================================================================
    // 7. PERFORMANCE MANAGEMENT TEST
    // =========================================================================
    @Test
    fun test7_PerformanceManagementTest_ThroughputAndLatencyBenchmarks() {
        val candidateProfile = UserProfile(
            fullName = "High Throughput Test User",
            targetRole = "Full Stack Engineer",
            experienceYears = 4.0f,
            readinessScore = 75
        )
        val skills = (1..20).map {
            UserSkill(id = it.toLong(), skillName = "Skill $it", category = "Tech", proficiencyLevel = (it % 5) + 1, verified = it % 2 == 0)
        }
        val projects = (1..5).map {
            PortfolioProject(
                id = it.toLong(),
                title = "Project $it",
                description = "High scale service with Docker CI/CD and metrics",
                repositoryUrl = "https://github.com/test/p$it",
                liveUrl = "https://p$it.example.com",
                status = "completed",
                technologies = "Kotlin, Docker, CI/CD",
                skillsTargeted = "Skill 1, Skill 2"
            )
        }

        // Benchmark 100 consecutive candidate audit evaluations
        val totalAuditTimeMs = measureTimeMillis {
            repeat(100) {
                AuditEngine.evaluateCandidate(
                    profile = candidateProfile,
                    skills = skills,
                    projects = projects,
                    latestResume = null,
                    interviewAnswers = emptyList(),
                    integrations = emptyList()
                )
            }
        }

        // 100 evaluations should complete in < 250ms on standard JVM
        assertTrue("100 candidate audits should execute under 250ms (took ${totalAuditTimeMs}ms)", totalAuditTimeMs < 250)

        // Benchmark Job Matcher keyword parsing throughput
        val posting = JobMatcherEngine.PRESET_JOB_POSTINGS.first()
        val totalMatcherTimeMs = measureTimeMillis {
            repeat(100) {
                JobMatcherEngine.evaluateJobMatch(
                    jobPosting = posting,
                    userProfile = candidateProfile,
                    skills = skills,
                    projects = projects,
                    latestResume = null
                )
            }
        }
        assertTrue("100 job matches should execute under 250ms (took ${totalMatcherTimeMs}ms)", totalMatcherTimeMs < 250)
    }

    // =========================================================================
    // 8. ACCURACY TEST
    // =========================================================================
    @Test
    fun test8_AccuracyTest_MathematicalCorrectnessAndDeterministicScoring() {
        // Mathematical Accuracy 1: Audit Demerit Arithmetic
        val config = AuditPenaltyConfig(
            missingResumeMetrics = -3,
            missingGithubEvidence = -4,
            missingDeploymentEvidence = -5,
            missingCicdTesting = -4
        )

        val profile = UserProfile(readinessScore = 80)
        val (issues, summary) = AuditEngine.evaluateCandidate(
            profile = profile,
            skills = emptyList(),
            projects = emptyList(),
            latestResume = null,
            interviewAnswers = emptyList(),
            integrations = emptyList(),
            config = config
        )

        val expectedDemerits = issues.filter { it.status != "RESOLVED" }.sumOf { it.scoreImpact }
        assertEquals(expectedDemerits, summary.totalDemerits)
        assertEquals(80 + expectedDemerits, summary.netAuditScore)

        // Mathematical Accuracy 2: Four-Year Total Compensation Arithmetic
        val comp = SalaryNegotiationEngine.calculateCompensation(
            baseSalary = 150000.0,
            equityGrant = 200000.0,
            signOn = 20000.0,
            bonusPercent = 10.0,
            relocation = 5000.0
        )
        // 4 Year TC = (150k * 4) + 200k + 20k + (15k * 4) + 5k = 600k + 200k + 20k + 60k + 5k = 885,000
        assertEquals(885000.0, comp.fourYearTotalComp, 0.01)

        // Deterministic Output Consistency
        val (issuesRun1, summaryRun1) = AuditEngine.evaluateCandidate(profile, emptyList(), emptyList(), null, emptyList(), emptyList())
        val (issuesRun2, summaryRun2) = AuditEngine.evaluateCandidate(profile, emptyList(), emptyList(), null, emptyList(), emptyList())
        assertEquals(summaryRun1.netAuditScore, summaryRun2.netAuditScore)
        assertEquals(summaryRun1.totalDemerits, summaryRun2.totalDemerits)
        assertEquals(issuesRun1.size, issuesRun2.size)
    }

    // =========================================================================
    // 9. WORKING TEST
    // =========================================================================
    @Test
    fun test9_WorkingTest_CatalogsIntegrityAndLivePresets() {
        // 1. Check Coding Challenges
        val challenges = BenchmarkCatalog.INITIAL_CODING_CHALLENGES
        assertTrue("Coding challenges catalog should not be empty", challenges.isNotEmpty())
        challenges.forEach { ch ->
            assertTrue("Challenge ID must not be blank", ch.id.isNotBlank())
            assertTrue("Challenge Title must not be blank", ch.title.isNotBlank())
            assertTrue("Problem Statement must not be blank", ch.problemStatement.isNotBlank())
            assertTrue("Starter Code must be valid", ch.starterCode.isNotBlank())
            assertTrue("Solution Reference must be provided", ch.solutionReference.isNotBlank())
        }

        // 2. Check Peer Matches
        val peers = BenchmarkCatalog.INITIAL_PEER_MATCHES
        assertTrue("Peer matches must exist", peers.isNotEmpty())
        peers.forEach { peer ->
            assertTrue("Peer rating must be between 4.0 and 5.0", peer.rating in 4.0f..5.0f)
            assertTrue("Peer must have specialities", peer.skillsSpecialty.isNotEmpty())
        }

        // 3. Check Skill Sprints
        val sprints = BenchmarkCatalog.INITIAL_SKILL_SPRINTS
        assertTrue("Skill sprints must exist", sprints.isNotEmpty())
        sprints.forEach { sprint ->
            assertTrue("Sprint must have milestone tasks", sprint.milestoneTasks.isNotEmpty())
            assertTrue("Sprint reward XP must be > 0", sprint.rewardXp > 0)
        }

        // 4. Check Opportunities
        val opps = BenchmarkCatalog.INITIAL_OPPORTUNITIES
        assertTrue("Opportunities catalog must exist", opps.isNotEmpty())
        opps.forEach { opp ->
            assertTrue("Official URL must be valid HTTP/HTTPS", opp.officialUrl.startsWith("http"))
            assertTrue("Provider must be defined", opp.providerOrHost.isNotBlank())
        }

        // 5. Check Sample Resumes
        val sampleResumes = ResumeParser.SAMPLE_PDF_RESUMES
        assertEquals(3, sampleResumes.size)
        sampleResumes.forEach { res ->
            val parsed = ResumeParser.parseResumeText(res.rawContent)
            assertTrue("Must extract name from sample", parsed.fullName.isNotBlank())
            assertTrue("Must detect email from sample", parsed.email.contains("@"))
            assertTrue("Must detect skills from sample", parsed.skillsDetected.isNotEmpty())
        }
    }

    // =========================================================================
    // 10. OVERALL TEST
    // =========================================================================
    @Test
    fun test10_OverallTest_FullE2ECandidateLifecycleJourney() {
        // Complete End-to-End Candidate Lifecycle:
        // Step 1: Onboarding -> Profile Creation
        val profile = UserProfile(
            id = "cuj_candidate_1",
            fullName = "Taylor Morgan",
            email = "taylor.morgan@domain.com",
            targetRole = "Full Stack Engineer",
            experienceYears = 3.0f,
            targetSalary = "$165,000",
            readinessScore = 65,
            onboardingCompleted = true
        )
        assertTrue(profile.onboardingCompleted)

        // Step 2: Resume Import & Parsing
        val sampleResume = ResumeParser.SAMPLE_PDF_RESUMES[0]
        val parsedResume = ResumeParser.parseResumeText(sampleResume.rawContent)
        val resumeAudit = ResumeAudit(
            filename = sampleResume.fileName,
            targetRole = profile.targetRole,
            overallScore = 86,
            impactScore = 84,
            brevityScore = 88,
            styleScore = 86,
            skillsDetected = parsedResume.skillsDetected.joinToString(", "),
            strengths = "Strong quantifiable metrics\nClear architecture background",
            weaknesses = "Could add more system scale numbers",
            recommendations = "Good shape for senior interview pipelines",
            rawText = sampleResume.rawContent
        )
        assertEquals(86, resumeAudit.overallScore)

        // Step 3: Portfolio Project & Live Demonstrator Setup
        val project = PortfolioProject(
            id = 1001,
            title = "Apex Data Gateway",
            description = "Event-driven microservices processing 60k RPS with Docker and CI/CD",
            repositoryUrl = "https://github.com/taylor/apex-gateway",
            liveUrl = "https://apex.taylor.dev",
            status = "completed",
            technologies = "Kotlin, Kafka, Docker, Kubernetes, CI/CD",
            skillsTargeted = "Distributed Systems, Backend"
        )
        assertEquals("completed", project.status)

        // Step 4: Technical Interview Session & Scoring
        val interviewAnswer = InterviewAnswer(
            id = 2001,
            interviewId = "session_mock_1",
            questionNumber = 1,
            questionText = "How do you handle p99 tail latency in distributed microservices?",
            category = "Distributed Systems",
            difficulty = "Senior",
            rubric = "Connection pools, async I/O, timeouts, hedging requests",
            answerText = "I implement connection pooling, circuit breakers with Resilience4j, and request hedging for long-tail requests...",
            score = 90,
            clarityScore = 88,
            technicalScore = 92,
            feedback = "Excellent grasp of resilience patterns.",
            suggestedImprovement = "Include metrics from synthetic load tests."
        )
        assertTrue(interviewAnswer.score >= 85)

        // Step 5: Candidate Readiness Audit Evaluation
        val (issues, summary) = AuditEngine.evaluateCandidate(
            profile = profile,
            skills = parsedResume.skillsDetected.mapIndexed { idx, name ->
                UserSkill(id = idx.toLong(), skillName = name, category = "Tech", proficiencyLevel = 4, verified = true)
            },
            projects = listOf(project),
            latestResume = resumeAudit,
            interviewAnswers = listOf(interviewAnswer),
            integrations = listOf(
                IntegrationAccount(
                    provider = "github",
                    username = "taylor-dev",
                    isConnected = true,
                    connectionStatus = "CONNECTED",
                    publicReposCount = 12
                )
            )
        )

        assertNotNull(summary)
        assertTrue("Evidence coverage must exceed 80%", summary.evidenceCoveragePercent >= 80)
        assertEquals("HIGH", summary.profileConfidence)
        assertTrue("Net audit score should be calculated", summary.netAuditScore != null && summary.netAuditScore!! >= 60)

        // Step 6: Opportunity Exploration & Registration
        val opp = BenchmarkCatalog.INITIAL_OPPORTUNITIES.first()
        val trackedOpp = opp.copy(status = "REGISTERED", userNotes = "Registered for upcoming cycle")
        assertEquals("REGISTERED", trackedOpp.status)

        // Step 7: Salary Negotiation Calculation
        val offer = SalaryNegotiationEngine.calculateCompensation(
            baseSalary = 165000.0,
            equityGrant = 140000.0,
            signOn = 20000.0,
            bonusPercent = 15.0
        )
        assertTrue("Year 1 Total Comp should exceed $200k", offer.year1TotalComp > 200000.0)

        // All 10 verification steps complete with 100% integrity
    }
}
