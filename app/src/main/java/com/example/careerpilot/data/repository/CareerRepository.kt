package com.example.careerpilot.data.repository

import com.example.careerpilot.data.local.CareerDao
import com.example.careerpilot.data.model.*
import com.example.careerpilot.data.remote.github.GitHubApiClient
import com.example.careerpilot.data.remote.github.GitHubRepoItem
import com.example.careerpilot.data.remote.github.GitHubValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class CareerRepository(
    private val dao: CareerDao,
    private val userIdFlow: Flow<String?> = flowOf(null),
    private val userIdProvider: () -> String? = { null }
) {

    fun currentUid(): String = userIdProvider() ?: "legacy_user"

    @OptIn(ExperimentalCoroutinesApi::class)
    val userProfileFlow: Flow<UserProfile?> = userIdFlow.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(null) else dao.getUserProfileFlow(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userSkillsFlow: Flow<List<UserSkill>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else dao.getUserSkillsFlow(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val skillGapsFlow: Flow<List<SkillGap>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else dao.getSkillGapsFlow(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeRoadmapFlow: Flow<Roadmap?> = userIdFlow.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(null) else dao.getActiveRoadmapFlow(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val roadmapItemsFlow: Flow<List<RoadmapItem>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else dao.getRoadmapItemsFlow(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val projectsFlow: Flow<List<PortfolioProject>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else dao.getProjectsFlow(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val latestResumeAuditFlow: Flow<ResumeAudit?> = userIdFlow.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(null) else dao.getLatestResumeAuditFlow(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val resumeAuditsFlow: Flow<List<ResumeAudit>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else dao.getResumeAuditsFlow(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val interviewsFlow: Flow<List<InterviewSession>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else dao.getInterviewsFlow(uid)
    }

    val learningResourcesFlow: Flow<List<LearningResource>> = dao.getLearningResourcesFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val integrationsFlow: Flow<List<IntegrationAccount>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else dao.getIntegrationsFlow(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentAnalyticsFlow: Flow<List<AnalyticsEvent>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else dao.getRecentAnalyticsFlow(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val auditIssuesFlow: Flow<List<AuditIssue>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else dao.getAuditIssuesFlow(uid)
    }

    val jobPostingsFlow: Flow<List<TargetJobPosting>> = dao.getJobPostingsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val jobMatchesFlow: Flow<List<JobMatchResult>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else dao.getJobMatchesFlow(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val jobApplicationsFlow: Flow<List<JobApplication>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else dao.getJobApplicationsFlow(uid)
    }

    val codingChallengesFlow: Flow<List<CodingChallenge>> = dao.getCodingChallengesFlow()
    val peerMatchesFlow: Flow<List<PeerMatch>> = dao.getPeerMatchesFlow()
    val skillSprintsFlow: Flow<List<SkillSprint>> = dao.getSkillSprintsFlow()
    val opportunitiesFlow: Flow<List<CareerOpportunity>> = dao.getOpportunitiesFlow()

    suspend fun initializeDefaultDataIfEmpty(userId: String? = null) = withContext(Dispatchers.IO) {
        val uid = userId ?: currentUid()
        val existingProfile = dao.getUserProfile(uid)
        if (existingProfile == null && uid.isNotBlank() && uid != "legacy_user") {
            val initialProfile = UserProfile(id = uid, readinessScore = null)
            dao.insertOrUpdateProfile(initialProfile)

            // Calculate initial matches for preset jobs
            JobMatcherEngine.PRESET_JOB_POSTINGS.forEach { job ->
                val match = JobMatcherEngine.evaluateJobMatch(
                    jobPosting = job,
                    userProfile = initialProfile,
                    skills = dao.getUserSkills(uid),
                    projects = dao.getProjects(uid),
                    latestResume = null
                ).copy(userId = uid)
                dao.insertJobMatchResult(match)
            }

            dao.insertAnalyticsEvent(
                AnalyticsEvent(
                    userId = uid,
                    eventName = "User Initialized",
                    detail = "Initialized clean user profile and job benchmarks for account $uid."
                )
            )
        }

        // Global Presets Initialization (catalog items shared across users)
        val existingResources = dao.getLearningResources()
        if (existingResources.isEmpty()) {
            val learningList = listOf(
                LearningResource(
                    title = "Mastering Distributed Systems & Consistency Patterns",
                    provider = "O'Reilly & Martin Kleppmann",
                    url = "https://dataintensive.net",
                    category = "Architecture",
                    skillTags = "Distributed Systems, Raft, Consensus, Partition Tolerance",
                    estimatedMinutes = 120,
                    difficulty = "Advanced",
                    resourceType = "Article",
                    status = "NOT_STARTED",
                    progressPercent = 0,
                    contentSummary = "Deep dive into CAP theorem trade-offs, Paxos vs Raft consensus algorithms, linearizability vs serializability, and distributed transaction isolation levels like 2PC and Saga orchestrators.",
                    quizQuestion = "Under the CAP theorem, when a network partition occurs in a distributed database, what fundamental trade-off must the system make?",
                    quizOptions = "Must choose between Consistency (returning errors/waiting) or Availability (returning stale data)|Sacrifice Partition Tolerance to gain infinite throughput|Automatically elect a single master node across the globe without delay|Disable write replication until all servers reboot",
                    quizCorrectIndex = 0,
                    isCompleted = false
                ),
                LearningResource(
                    title = "Modern Android Architecture & Reactive State Management",
                    provider = "Google & Android Developers",
                    url = "https://developer.android.com/topic/architecture",
                    category = "Mobile",
                    skillTags = "Jetpack Compose, StateFlow, Coroutines, UDF",
                    estimatedMinutes = 90,
                    difficulty = "Intermediate",
                    resourceType = "Documentation",
                    status = "NOT_STARTED",
                    progressPercent = 0,
                    contentSummary = "Architecting robust Android applications using Unidirectional Data Flow (UDF), ViewModel lifecycle coroutines, Room persistence, and declarative UI composition patterns.",
                    quizQuestion = "Why is StateFlow preferred over LiveData in modern Kotlin-first Android Jetpack Compose architectures?",
                    quizOptions = "StateFlow provides native coroutine Flow operators, strict initial state, and seamless Compose state collection without Android framework lifecycle coupling|LiveData consumes 10x more battery power during background execution|StateFlow bypasses the Android Main Thread and renders directly to GPU hardware|StateFlow is only supported in legacy XML layouts",
                    quizCorrectIndex = 0,
                    isCompleted = false
                ),
                LearningResource(
                    title = "PostgreSQL Indexing Internals: B-Tree, GIN & GiST Under Load",
                    provider = "PostgreSQL Global Development Group",
                    url = "https://www.postgresql.org/docs/current/indexes.html",
                    category = "Databases",
                    skillTags = "PostgreSQL, Indexing, Query Optimization, EXPLAIN ANALYZE",
                    estimatedMinutes = 60,
                    difficulty = "Advanced",
                    resourceType = "Deep Dive",
                    status = "NOT_STARTED",
                    progressPercent = 0,
                    contentSummary = "Comprehensive exploration of PostgreSQL internal index structures. How query planner leverages bitmap index scans, composite indexes, and index-only scans to eliminate sequential heap scans.",
                    quizQuestion = "What index type in PostgreSQL is specifically optimized for composite JSONB attributes and full-text search token arrays?",
                    quizOptions = "GIN (Generalized Inverted Index)|Standard B-Tree Index|Hash Index|BRIN Index",
                    quizCorrectIndex = 0,
                    isCompleted = false
                ),
                LearningResource(
                    title = "Docker & Multi-Stage Builds for Minimal Attack Surface",
                    provider = "Docker Documentation",
                    url = "https://docs.docker.com/build/building/multi-stage/",
                    category = "DevOps",
                    skillTags = "Docker, Container Security, Alpine, CI/CD",
                    estimatedMinutes = 45,
                    difficulty = "Beginner",
                    resourceType = "Hands-on Guide",
                    status = "NOT_STARTED",
                    progressPercent = 0,
                    contentSummary = "Practical guide to reducing container image footprint by separating build-time dependencies from production runtimes using scratch/distroless base images.",
                    quizQuestion = "What is the primary operational benefit of using multi-stage Docker builds?",
                    quizOptions = "Eliminates build tooling, compiler SDKs, and intermediate artifacts from the final production container image|Forces containers to run with elevated root privileges automatically|Doubles the build speed by skipping layer checksum verifications|Compresses container memory usage dynamically at runtime",
                    quizCorrectIndex = 0,
                    isCompleted = false
                ),
                LearningResource(
                    title = "OAuth 2.1 & PKCE Authentication Flows in Mobile Applications",
                    provider = "IETF & Auth0 Security",
                    url = "https://oauth.net/2/pkce/",
                    category = "Security",
                    skillTags = "OAuth2, PKCE, Cryptography, Mobile Security",
                    estimatedMinutes = 75,
                    difficulty = "Intermediate",
                    resourceType = "Specification",
                    status = "NOT_STARTED",
                    progressPercent = 0,
                    contentSummary = "Security best practices for mobile client authentication. Why client secrets cannot be securely stored on mobile devices and how Proof Key for Code Exchange (PKCE) prevents authorization code interception.",
                    quizQuestion = "Why does OAuth 2.1 mandate the PKCE (Proof Key for Code Exchange) flow for public mobile and single-page clients?",
                    quizOptions = "Public clients cannot securely maintain a client secret, and PKCE dynamically binds authorization codes to the requesting client using a cryptographic code verifier|PKCE removes the need for HTTPS encryption across network sockets|PKCE allows authentication without any user interaction or password entry|PKCE automatically logs the user into all third-party services permanently",
                    quizCorrectIndex = 0,
                    isCompleted = false
                )
            )
            dao.insertLearningResources(learningList)
        }

        val existingPostings = dao.getJobPostings()
        if (existingPostings.isEmpty()) {
            dao.insertJobPostings(JobMatcherEngine.PRESET_JOB_POSTINGS)
        }

        val existingChallenges = dao.getCodingChallenge("challenge_1")
        if (existingChallenges == null) {
            dao.insertCodingChallenges(BenchmarkCatalog.INITIAL_CODING_CHALLENGES)
            dao.insertPeerMatches(BenchmarkCatalog.INITIAL_PEER_MATCHES)
            dao.insertSkillSprints(BenchmarkCatalog.INITIAL_SKILL_SPRINTS)
        }

        val existingOpps = dao.getAllOpportunities()
        if (existingOpps.isEmpty()) {
            dao.insertOpportunities(BenchmarkCatalog.INITIAL_OPPORTUNITIES)
        }

        dao.clearDummyPeerMatches()
    }

    suspend fun recalibrateAudit(userId: String? = null): AuditScoreSummary = withContext(Dispatchers.IO) {
        val uid = userId ?: currentUid()
        val profile = dao.getUserProfile(uid)
        val skills = dao.getUserSkills(uid)
        val projects = dao.getProjects(uid)
        val latestResume = dao.getLatestResumeAudit(uid)
        val interviewAnswers = dao.getAllInterviewAnswers(uid)
        val integrations = dao.getIntegrations(uid)
        val existingIssues = dao.getAuditIssues(uid)

        val (newIssues, summary) = AuditEngine.evaluateCandidate(
            profile = profile,
            skills = skills,
            projects = projects,
            latestResume = latestResume,
            interviewAnswers = interviewAnswers,
            integrations = integrations,
            existingIssues = existingIssues
        )

        dao.clearAuditIssues(uid)
        val userIssues = newIssues.map { it.copy(userId = uid) }
        dao.insertAuditIssues(userIssues)

        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = uid,
                eventName = "Audit Recalibrated",
                detail = "Net Readiness: ${summary.netAuditScore}% (${summary.totalDemerits} demerits, ${summary.criticalCount} critical, ${summary.highCount} high)"
            )
        )

        summary
    }

    suspend fun updateAuditIssueStatus(issueId: String, newStatus: String) = withContext(Dispatchers.IO) {
        val uid = currentUid()
        val issue = dao.getAuditIssue(issueId, uid)
        if (issue != null) {
            val updated = issue.copy(
                status = newStatus,
                scoreImpact = if (newStatus == "RESOLVED") 0 else issue.scoreImpact,
                updatedAt = System.currentTimeMillis()
            )
            dao.updateAuditIssue(updated)
            recalibrateAudit(uid)

            dao.insertAnalyticsEvent(
                AnalyticsEvent(
                    userId = uid,
                    eventName = "Audit Issue Status Updated",
                    detail = "${issue.title} -> $newStatus"
                )
            )
        }
    }

    suspend fun updateProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        val uid = currentUid()
        val userProfile = profile.copy(id = uid)
        dao.insertOrUpdateProfile(userProfile)
        recalibrateSkillGaps(userProfile.targetRole, uid)
        recalibrateAudit(uid)
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = uid,
                eventName = "Profile Updated",
                detail = "Target role set to ${userProfile.targetRole}"
            )
        )
    }

    suspend fun recalibrateSkillGaps(targetRole: String, userId: String? = null) = withContext(Dispatchers.IO) {
        val uid = userId ?: currentUid()
        if (targetRole.isBlank()) {
            dao.clearSkillGaps(uid)
            return@withContext
        }
        val benchmarks = BenchmarkCatalog.ROLE_BENCHMARKS[targetRole] ?: return@withContext
        val userSkills = dao.getUserSkills(uid).associateBy { it.skillName.lowercase() }

        var totalWeight = 0f
        var earnedWeight = 0f
        val newGaps = mutableListOf<SkillGap>()

        for (bench in benchmarks) {
            totalWeight += bench.weight
            val userSkill = userSkills[bench.skill.lowercase()]
            val currentLevel = userSkill?.proficiencyLevel ?: 0
            val gap = max(0, bench.requiredLevel - currentLevel)

            val factor = min(1f, currentLevel.toFloat() / bench.requiredLevel.toFloat())
            earnedWeight += factor * bench.weight

            val priority = when {
                gap >= 3 -> "high"
                gap >= 2 -> "high"
                gap == 1 -> "medium"
                else -> "low"
            }

            val recommendation = when {
                gap >= 2 -> "Critical prerequisite for $targetRole. Complete focused system design projects and code labs."
                gap == 1 -> "Refine practical knowledge, performance profiling, and write end-to-end integration tests."
                else -> "Meets benchmark. Continue maintaining mastery through active code reviews."
            }

            newGaps.add(
                SkillGap(
                    userId = uid,
                    targetRole = targetRole,
                    skillName = bench.skill,
                    category = bench.category,
                    requiredLevel = bench.requiredLevel,
                    currentLevel = currentLevel,
                    gapScore = gap * 20,
                    priority = priority,
                    recommendation = recommendation
                )
            )
        }

        dao.clearSkillGaps(uid)
        dao.insertSkillGaps(newGaps)

        val score = if (totalWeight > 0) ((earnedWeight / totalWeight) * 100f).roundToInt() else 65
        val currentProfile = dao.getUserProfile(uid) ?: UserProfile(id = uid)
        dao.insertOrUpdateProfile(currentProfile.copy(readinessScore = score, targetRole = targetRole))

        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = uid,
                eventName = "Skill Calibration Completed",
                detail = "Calibrated readiness score: $score% for $targetRole"
            )
        )
    }

    suspend fun addOrUpdateUserSkill(skill: UserSkill) = withContext(Dispatchers.IO) {
        val uid = currentUid()
        val userSkill = skill.copy(userId = uid)
        dao.insertUserSkill(userSkill)
        val profile = dao.getUserProfile(uid)
        if (profile != null) {
            recalibrateSkillGaps(profile.targetRole, uid)
        }
        recalibrateAudit(uid)
    }

    suspend fun deleteUserSkill(skill: UserSkill) = withContext(Dispatchers.IO) {
        val uid = currentUid()
        dao.deleteUserSkill(skill)
        val profile = dao.getUserProfile(uid)
        if (profile != null) {
            recalibrateSkillGaps(profile.targetRole, uid)
        }
        recalibrateAudit(uid)
    }

    suspend fun generateRoadmapForRole(targetRole: String, userId: String? = null) = withContext(Dispatchers.IO) {
        val uid = userId ?: currentUid()
        val gaps = dao.getSkillGaps(uid)
        val highPriority = gaps.filter { it.priority == "high" }
        val mediumPriority = gaps.filter { it.priority == "medium" }

        val items = mutableListOf<RoadmapItem>()
        var order = 0

        // Phase 1: Core Deficiencies & Foundations
        items.add(
            RoadmapItem(
                userId = uid,
                phaseNumber = 1,
                phaseTitle = "Foundations & High-Priority Skill Elevation",
                title = "Deep-dive Core Architecture & Hands-on Lab",
                description = "Master fundamental paradigms and close key proficiency gaps for ${highPriority.firstOrNull()?.skillName ?: "Core Languages"}.",
                category = "Skill Mastery",
                estimatedHours = 6.0f,
                orderIndex = ++order,
                isCompleted = false
            )
        )
        items.add(
            RoadmapItem(
                userId = uid,
                phaseNumber = 1,
                phaseTitle = "Foundations & High-Priority Skill Elevation",
                title = "Database Indexing & Schema Tuning Exercise",
                description = "Design optimized relational models, measure query execution plans, and implement connection pooling.",
                category = "Databases",
                estimatedHours = 4.5f,
                orderIndex = ++order,
                isCompleted = false
            )
        )

        // Phase 2: Production Systems & Scalability
        items.add(
            RoadmapItem(
                userId = uid,
                phaseNumber = 2,
                phaseTitle = "Production Systems, Scalability & Architecture",
                title = "Build Distributed Microservice with Caching & PubSub",
                description = "Implement an event-driven service utilizing Redis caching, idempotent endpoints, and asynchronous queue workers.",
                category = "System Architecture",
                estimatedHours = 10.0f,
                orderIndex = ++order,
                isCompleted = false
            )
        )
        items.add(
            RoadmapItem(
                userId = uid,
                phaseNumber = 2,
                phaseTitle = "Production Systems, Scalability & Architecture",
                title = "Containerization & Automated CI/CD Pipeline",
                description = "Configure multi-stage Docker builds, GitHub Actions CI workflow, and automated test coverage thresholds.",
                category = "DevOps & Cloud",
                estimatedHours = 5.0f,
                orderIndex = ++order,
                isCompleted = false
            )
        )

        // Phase 3: Capstone Proof & Interview Calibration
        items.add(
            RoadmapItem(
                userId = uid,
                phaseNumber = 3,
                phaseTitle = "Production Capstone & Interview Calibration",
                title = "Full-Scale Capstone Project with Real Telemetry",
                description = "Deploy a production-grade application featuring real database persistence, observability metrics, and comprehensive unit tests.",
                category = "Portfolio Proof",
                estimatedHours = 12.0f,
                orderIndex = ++order,
                isCompleted = false
            )
        )
        items.add(
            RoadmapItem(
                userId = uid,
                phaseNumber = 3,
                phaseTitle = "Production Capstone & Interview Calibration",
                title = "System Design & Technical Articulation Mock Interview",
                description = "Simulate technical phone screens, practice structured verbal responses, and calibrate rubric scores to >85%.",
                category = "Interview Prep",
                estimatedHours = 4.0f,
                orderIndex = ++order,
                isCompleted = false
            )
        )

        val roadmapId = UUID.randomUUID().toString()
        val total = items.size
        val completed = items.count { it.isCompleted }
        val percent = if (total > 0) (completed.toFloat() / total.toFloat()) * 100f else 0f

        val roadmap = Roadmap(
            id = roadmapId,
            userId = uid,
            title = "3-Phase Trajectory for $targetRole",
            targetRole = targetRole,
            summary = "Structured progression addressing ${highPriority.size} high-priority gap areas and building production portfolio proof.",
            totalTasks = total,
            completedTasks = completed,
            progressPercent = percent,
            status = "in_progress"
        )
        dao.clearRoadmaps(uid)
        dao.insertOrUpdateRoadmap(roadmap)

        val userItems = items.map { it.copy(userId = uid, roadmapId = roadmapId) }
        dao.clearRoadmapItems(uid)
        dao.insertRoadmapItems(userItems)

        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = uid,
                eventName = "Roadmap Generated",
                detail = "Created 3-phase progression with $total milestones."
            )
        )
    }

    suspend fun toggleRoadmapItem(itemId: Long) = withContext(Dispatchers.IO) {
        val uid = currentUid()
        val items = dao.getRoadmapItems(uid)
        val target = items.find { it.id == itemId } ?: return@withContext
        val updated = target.copy(
            isCompleted = !target.isCompleted,
            completedAt = if (!target.isCompleted) System.currentTimeMillis() else null
        )
        dao.updateRoadmapItem(updated)

        val updatedItems = dao.getRoadmapItems(uid)
        val total = updatedItems.size
        val completed = updatedItems.count { it.isCompleted }
        val percent = if (total > 0) (completed.toFloat() / total.toFloat()) * 100f else 0f

        val roadmap = dao.getActiveRoadmap(uid)
        if (roadmap != null) {
            dao.insertOrUpdateRoadmap(
                roadmap.copy(
                    completedTasks = completed,
                    progressPercent = percent,
                    status = if (completed == total && total > 0) "completed" else "in_progress"
                )
            )
        }

        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = uid,
                eventName = "Roadmap Task Toggled",
                detail = "${target.title} -> ${if (updated.isCompleted) "Completed" else "Incomplete"}"
            )
        )
    }

    suspend fun analyzeResumeText(rawText: String, filename: String): ResumeAudit = withContext(Dispatchers.IO) {
        val uid = currentUid()
        val lower = rawText.lowercase()
        val profile = dao.getUserProfile(uid)
        val targetRole = profile?.targetRole ?: "Full Stack Engineer"

        val detectedSkills = mutableListOf<String>()
        val allPossibleSkills = listOf(
            "Kotlin", "Java", "Python", "TypeScript", "JavaScript", "React", "Jetpack Compose",
            "Node.js", "Express", "PostgreSQL", "MySQL", "MongoDB", "Redis", "Docker", "Kubernetes",
            "AWS", "GCP", "CI/CD", "Git", "System Design", "Microservices", "REST APIs", "GraphQL",
            "Room", "Coroutines", "Flow", "Retrofit", "Unit Testing", "TDD", "Agile"
        )

        for (skill in allPossibleSkills) {
            if (lower.contains(skill.lowercase())) {
                detectedSkills.add(skill)
            }
        }

        val hasMetrics = lower.contains("%") || lower.contains("$") || lower.contains("increased") ||
                lower.contains("reduced") || lower.contains("scaled") || lower.contains("optimized") ||
                lower.contains("users") || lower.contains("latency")

        val hasActionVerbs = lower.contains("architected") || lower.contains("developed") ||
                lower.contains("implemented") || lower.contains("led") || lower.contains("orchestrated") ||
                lower.contains("engineered")

        val wordCount = rawText.split(Regex("\\s+")).count { it.isNotBlank() }

        // Scores calculation
        val impactScore = if (hasMetrics && hasActionVerbs) 88 else if (hasMetrics || hasActionVerbs) 74 else 58
        val brevityScore = when {
            wordCount in 250..650 -> 92
            wordCount in 150..900 -> 78
            else -> 62
        }
        val styleScore = if (rawText.length > 200) 85 else 60
        val keywordMatchScore = min(95, max(50, detectedSkills.size * 9))
        val overallScore = ((impactScore * 0.35f) + (brevityScore * 0.25f) + (styleScore * 0.15f) + (keywordMatchScore * 0.25f)).roundToInt()

        val strengths = buildList {
            if (hasMetrics) add("Strong quantification of outcomes (metrics, percentages, and scaling figures)")
            if (hasActionVerbs) add("Action-oriented verbs at the beginning of experience bullet points")
            if (detectedSkills.size >= 5) add("High technical keyword density matching $targetRole profiles")
            if (wordCount in 250..700) add("Concise page length and digestible narrative structure")
        }.joinToString("\n")

        val weaknesses = buildList {
            if (!hasMetrics) add("Lacks measurable business or performance metrics (e.g. latency reduced by X%)")
            if (detectedSkills.size < 5) add("Missing key modern industry buzzwords for $targetRole")
            if (wordCount < 200) add("Resume text appears overly brief or sparse in architectural detail")
            if (!lower.contains("system design") && !lower.contains("architecture")) add("Needs explicit callouts to system design, scalability, or code ownership")
        }.joinToString("\n")

        val recommendations = buildList {
            add("Adopt the Google XYZ formula: 'Accomplished [X] as measured by [Y], by doing [Z]'")
            add("Incorporate top keywords from target job descriptions ($targetRole)")
            add("Feature your production portfolio project GitHub links prominently in the header")
            add("Highlight unit & integration testing methodology (e.g. TDD, 85%+ coverage)")
        }.joinToString("\n")

        val audit = ResumeAudit(
            userId = uid,
            filename = filename,
            targetRole = targetRole,
            overallScore = overallScore,
            impactScore = impactScore,
            brevityScore = brevityScore,
            styleScore = styleScore,
            skillsDetected = detectedSkills.joinToString(", "),
            strengths = strengths,
            weaknesses = weaknesses,
            recommendations = recommendations,
            rawText = rawText
        )

        dao.insertResumeAudit(audit)
        recalibrateAudit(uid)
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = uid,
                eventName = "Resume Analyzed",
                detail = "ATS Score calculated: $overallScore/100"
            )
        )
        audit
    }

    suspend fun startInterviewSession(roleTarget: String, difficulty: String): InterviewSession = withContext(Dispatchers.IO) {
        val uid = currentUid()
        val sessionId = UUID.randomUUID().toString()
        val session = InterviewSession(
            id = sessionId,
            userId = uid,
            roleTarget = roleTarget,
            difficulty = difficulty,
            status = "in_progress",
            totalQuestions = BenchmarkCatalog.INTERVIEW_QUESTIONS.size,
            completedQuestions = 0,
            overallScore = 0,
            feedbackSummary = "Interview session in progress."
        )
        dao.insertOrUpdateInterview(session)
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = uid,
                eventName = "Mock Interview Started",
                detail = "Started $difficulty interview for $roleTarget"
            )
        )
        session
    }

    suspend fun submitInterviewAnswer(
        sessionId: String,
        questionIndex: Int,
        answerText: String
    ): InterviewAnswer = withContext(Dispatchers.IO) {
        val uid = currentUid()
        val question = BenchmarkCatalog.INTERVIEW_QUESTIONS[questionIndex]
        val lowerAnswer = answerText.lowercase()

        val keywordMatches = question.keywords.count { lowerAnswer.contains(it.lowercase()) }
        val keywordRatio = keywordMatches.toFloat() / max(1, question.keywords.size).toFloat()

        val wordCount = answerText.split(Regex("\\s+")).count { it.isNotBlank() }

        val clarityScore = when {
            wordCount in 35..180 -> 90
            wordCount in 20..300 -> 78
            else -> 60
        }

        val technicalScore = min(98, (keywordRatio * 75f + min(25f, (wordCount / 5f))).roundToInt())
        val score = ((clarityScore * 0.4f) + (technicalScore * 0.6f)).roundToInt()

        val feedback = when {
            score >= 85 -> "Outstanding answer! Articulated the core architectural trade-offs, addressed latency bottlenecks, and demonstrated staff-level clarity."
            score >= 70 -> "Solid technical foundation. Touched upon the essential components, but could be elevated by providing specific production metrics and edge case mitigations."
            else -> "Good preliminary attempt. Expand your response with concrete system components like distributed caches, query plans, or idempotency keys."
        }

        val improvement = "Recommendation: Frame your answers using the STAR method (Situation, Task, Action, Result) and explicitly mention performance trade-offs."

        val answer = InterviewAnswer(
            userId = uid,
            interviewId = sessionId,
            questionNumber = questionIndex + 1,
            questionText = question.questionText,
            category = question.category,
            difficulty = question.difficulty,
            rubric = question.rubric,
            answerText = answerText,
            score = score,
            clarityScore = clarityScore,
            technicalScore = technicalScore,
            feedback = feedback,
            suggestedImprovement = improvement
        )

        dao.insertInterviewAnswer(answer)

        val answers = dao.getInterviewAnswers(sessionId, uid)
        val avgScore = if (answers.isNotEmpty()) answers.map { it.score }.average().roundToInt() else score
        val session = dao.getInterviewSession(sessionId, uid)
        if (session != null) {
            val isCompleted = answers.size >= session.totalQuestions
            dao.insertOrUpdateInterview(
                session.copy(
                    completedQuestions = answers.size,
                    overallScore = avgScore,
                    status = if (isCompleted) "completed" else "in_progress",
                    feedbackSummary = if (isCompleted) "Interview completed with overall score $avgScore%." else session.feedbackSummary
                )
            )
        }

        recalibrateAudit(uid)

        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = uid,
                eventName = "Interview Answer Submitted",
                detail = "Question ${questionIndex + 1} scored $score/100"
            )
        )

        answer
    }

    suspend fun getNextBestAction(): NextBestAction = withContext(Dispatchers.IO) {
        val uid = currentUid()
        val profile = dao.getUserProfile(uid)
        if (profile == null || profile.targetRole.isBlank()) {
            return@withContext NextBestAction(
                actionId = "nba_onboarding",
                title = "Define Target Role & Objectives",
                category = "Onboarding",
                whyItMatters = "Career Hub requires your target role to calibrate readiness scores and personalized roadmaps.",
                evidence = "Target role is currently unset.",
                estimatedMinutes = 2,
                priority = "urgent",
                targetRoute = "profile",
                ctaText = "Set Role"
            )
        }

        // Check for highest-impact unresolved red flag / demerit in Audit
        val auditIssues = dao.getAuditIssues(uid).filter { it.status != "RESOLVED" }
        val topRedFlag = auditIssues.firstOrNull { it.severity == "CRITICAL" }
            ?: auditIssues.firstOrNull { it.severity == "HIGH" }

        if (topRedFlag != null) {
            val penaltyAbs = kotlin.math.abs(topRedFlag.scoreImpact)
            return@withContext NextBestAction(
                actionId = "nba_audit_${topRedFlag.id}",
                title = "Resolve Red Flag: ${topRedFlag.title}",
                category = topRedFlag.category,
                whyItMatters = "Eliminates a $penaltyAbs-point demerit deduction and validates evidence for ${profile.targetRole}.",
                evidence = topRedFlag.evidence,
                estimatedMinutes = when (topRedFlag.estimatedEffort) {
                    "15 minutes" -> 15
                    "30 minutes" -> 30
                    "45 minutes" -> 45
                    else -> 60
                },
                priority = if (topRedFlag.severity == "CRITICAL") "urgent" else "high",
                targetRoute = topRedFlag.targetRoute,
                ctaText = topRedFlag.ctaText
            )
        }

        val gaps = dao.getSkillGaps(uid)
        if (gaps.isEmpty()) {
            return@withContext NextBestAction(
                actionId = "nba_skill_gap",
                title = "Calibrate Skill Readiness for ${profile.targetRole}",
                category = "Skill Calibration",
                whyItMatters = "Determines the technical competencies required by top employers and pinpoints elevation areas.",
                evidence = "No skill gap assessment recorded for current target role.",
                estimatedMinutes = 2,
                priority = "urgent",
                targetRoute = "career",
                ctaText = "Run Analysis"
            )
        }

        val roadmap = dao.getActiveRoadmap(uid)
        if (roadmap == null) {
            return@withContext NextBestAction(
                actionId = "nba_roadmap",
                title = "Generate 3-Phase Career Roadmap",
                category = "Roadmap",
                whyItMatters = "Translates identified skill gaps into step-by-step actionable milestone deliverables.",
                evidence = "Skill gaps identified and ready for roadmap synthesis.",
                estimatedMinutes = 2,
                priority = "high",
                targetRoute = "roadmap",
                ctaText = "Generate Roadmap"
            )
        }

        val items = dao.getRoadmapItems(uid)
        val nextTask = items.find { !it.isCompleted }
        if (nextTask != null) {
            return@withContext NextBestAction(
                actionId = "nba_task_${nextTask.id}",
                title = nextTask.title,
                category = "Roadmap Milestone",
                whyItMatters = "Fulfills Phase ${nextTask.phaseNumber} requirements: ${nextTask.phaseTitle}.",
                evidence = "Estimated duration: ${nextTask.estimatedHours}h in ${nextTask.category}.",
                estimatedMinutes = (nextTask.estimatedHours * 60).roundToInt(),
                priority = "high",
                targetRoute = "roadmap",
                ctaText = "View Task"
            )
        }

        return@withContext NextBestAction(
            actionId = "nba_interview",
            title = "Practice AI Mock Interview",
            category = "Interview Readiness",
            whyItMatters = "Sharpen real-time technical articulation and system design responses under simulated pressure.",
            evidence = "Ready for senior technical assessment calibration.",
            estimatedMinutes = 15,
            priority = "high",
            targetRoute = "interview",
            ctaText = "Start Interview"
        )
    }

    // Projects CRUD
    suspend fun addProject(project: PortfolioProject) = withContext(Dispatchers.IO) {
        val uid = currentUid()
        dao.insertProject(project.copy(userId = uid))
        recalibrateAudit(uid)
    }

    suspend fun updateProject(project: PortfolioProject) = withContext(Dispatchers.IO) {
        val uid = currentUid()
        dao.updateProject(project.copy(userId = uid))
        recalibrateAudit(uid)
    }

    suspend fun deleteProject(project: PortfolioProject) = withContext(Dispatchers.IO) {
        val uid = currentUid()
        dao.deleteProject(project)
        recalibrateAudit(uid)
    }

    // ==========================================
    // REAL LEARNING RESOURCE WORKFLOW
    // ==========================================
    suspend fun startLearningResource(resourceId: Long) = withContext(Dispatchers.IO) {
        val resource = dao.getLearningResource(resourceId) ?: return@withContext
        val now = System.currentTimeMillis()
        val updated = resource.copy(
            status = "IN_PROGRESS",
            startedAt = resource.startedAt ?: now,
            lastStudiedAt = now,
            progressPercent = max(15, resource.progressPercent),
            isCompleted = false
        )
        dao.updateLearningResource(updated)
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = currentUid(),
                eventName = "Learning Started",
                detail = "Started '${resource.title}' (${resource.provider})"
            )
        )
    }

    suspend fun updateLearningProgress(
        resourceId: Long,
        additionalMinutes: Int,
        newProgressPercent: Int,
        userNotes: String
    ) = withContext(Dispatchers.IO) {
        val resource = dao.getLearningResource(resourceId) ?: return@withContext
        val now = System.currentTimeMillis()
        val totalMinutes = resource.studyMinutesSpent + max(0, additionalMinutes)
        val cappedPercent = if (resource.status == "COMPLETED") 100 else newProgressPercent.coerceIn(0, 95)
        val updated = resource.copy(
            status = if (resource.status == "NOT_STARTED") "IN_PROGRESS" else resource.status,
            startedAt = resource.startedAt ?: now,
            lastStudiedAt = now,
            studyMinutesSpent = totalMinutes,
            progressPercent = cappedPercent,
            notes = userNotes.ifBlank { resource.notes }
        )
        dao.updateLearningResource(updated)
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = currentUid(),
                eventName = "Learning Progress Logged",
                detail = "${resource.title}: $cappedPercent% (${totalMinutes}m logged)"
            )
        )
    }

    suspend fun verifyAndCompleteLearning(
        resourceId: Long,
        selectedQuizIndex: Int,
        userNotes: String
    ): Boolean = withContext(Dispatchers.IO) {
        val resource = dao.getLearningResource(resourceId) ?: return@withContext false
        val isQuizCorrect = selectedQuizIndex == resource.quizCorrectIndex

        if (isQuizCorrect) {
            val now = System.currentTimeMillis()
            val updated = resource.copy(
                status = "COMPLETED",
                isCompleted = true,
                progressPercent = 100,
                completedAt = now,
                lastStudiedAt = now,
                studyMinutesSpent = max(resource.studyMinutesSpent, resource.estimatedMinutes),
                notes = userNotes.ifBlank { resource.notes }
            )
            dao.updateLearningResource(updated)
            recalibrateAudit()

            dao.insertAnalyticsEvent(
                AnalyticsEvent(
                    userId = currentUid(),
                    eventName = "Learning Verified & Completed",
                    detail = "Mastered '${resource.title}' (+${resource.estimatedMinutes}m competency credit)"
                )
            )
            return@withContext true
        } else {
            dao.insertAnalyticsEvent(
                AnalyticsEvent(
                    userId = currentUid(),
                    eventName = "Comprehension Check Attempted",
                    detail = "Quiz attempt incorrect for '${resource.title}'. Review required."
                )
            )
            return@withContext false
        }
    }

    suspend fun resetLearningResource(resourceId: Long) = withContext(Dispatchers.IO) {
        val resource = dao.getLearningResource(resourceId) ?: return@withContext
        val updated = resource.copy(
            status = "NOT_STARTED",
            progressPercent = 0,
            startedAt = null,
            completedAt = null,
            lastStudiedAt = null,
            studyMinutesSpent = 0,
            isCompleted = false
        )
        dao.updateLearningResource(updated)
        recalibrateAudit()
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = currentUid(),
                eventName = "Learning Module Reset",
                detail = "Reset progress for '${resource.title}'"
            )
        )
    }

    suspend fun toggleLearningCompleted(resource: LearningResource) = withContext(Dispatchers.IO) {
        if (resource.isCompleted || resource.status == "COMPLETED") {
            resetLearningResource(resource.id)
        } else {
            startLearningResource(resource.id)
        }
    }

    // ==========================================
    // REAL GITHUB INTEGRATION & API TELEMETRY
    // ==========================================
    suspend fun validateAndConnectGitHub(username: String): GitHubValidationResult = withContext(Dispatchers.IO) {
        val uid = currentUid()
        val trimmed = username.trim()
        if (trimmed.isBlank()) {
            val errResult = GitHubValidationResult.Error(400, "Please enter a valid GitHub username.")
            return@withContext errResult
        }

        // Set state to CHECKING
        val existing = dao.getIntegration("github", uid) ?: IntegrationAccount(provider = "github", userId = uid)
        dao.insertOrUpdateIntegration(
            existing.copy(
                userId = uid,
                username = trimmed,
                connectionStatus = "CHECKING",
                errorMessage = ""
            )
        )

        val apiResult = GitHubApiClient.fetchGitHubProfileAndRepos(trimmed)

        when (apiResult) {
            is GitHubValidationResult.Success -> {
                val profile = apiResult.profile
                val topReposJson = buildTopReposJson(profile.topRepos)
                val detailsString = "${profile.publicRepos} public repos • ${profile.followers} followers • ${profile.topRepos.size} recent active repos"

                val connectedAccount = IntegrationAccount(
                    provider = "github",
                    userId = uid,
                    username = profile.username,
                    connectionStatus = "CONNECTED",
                    isConnected = true,
                    lastSyncedAt = System.currentTimeMillis(),
                    avatarUrl = profile.avatarUrl,
                    displayName = profile.displayName,
                    publicReposCount = profile.publicRepos,
                    followersCount = profile.followers,
                    followingCount = profile.following,
                    publicGistsCount = profile.publicGists,
                    bio = profile.bio,
                    company = profile.company,
                    location = profile.location,
                    topRepositoriesJson = topReposJson,
                    details = detailsString,
                    errorMessage = ""
                )
                dao.insertOrUpdateIntegration(connectedAccount)
                recalibrateAudit(uid)

                dao.insertAnalyticsEvent(
                    AnalyticsEvent(
                        userId = uid,
                        eventName = "GitHub Connected",
                        detail = "Verified GitHub account '${profile.username}' (${profile.publicRepos} repos, ${profile.followers} followers)"
                    )
                )
            }
            is GitHubValidationResult.UserNotFound -> {
                val notFoundAccount = IntegrationAccount(
                    provider = "github",
                    userId = uid,
                    username = trimmed,
                    connectionStatus = "NOT_FOUND",
                    isConnected = false,
                    errorMessage = apiResult.message,
                    details = "User '$trimmed' not found on GitHub."
                )
                dao.insertOrUpdateIntegration(notFoundAccount)
                recalibrateAudit(uid)

                dao.insertAnalyticsEvent(
                    AnalyticsEvent(
                        userId = uid,
                        eventName = "GitHub Verification Failed",
                        detail = "User '$trimmed' does not exist on GitHub (HTTP 404)"
                    )
                )
            }
            is GitHubValidationResult.RateLimited -> {
                val rateLimitedAccount = IntegrationAccount(
                    provider = "github",
                    userId = uid,
                    username = trimmed,
                    connectionStatus = "RATE_LIMITED",
                    isConnected = false,
                    errorMessage = apiResult.message,
                    details = "GitHub API rate limit exceeded."
                )
                dao.insertOrUpdateIntegration(rateLimitedAccount)
            }
            is GitHubValidationResult.Error -> {
                val errorAccount = IntegrationAccount(
                    provider = "github",
                    userId = uid,
                    username = trimmed,
                    connectionStatus = "ERROR",
                    isConnected = false,
                    errorMessage = apiResult.message,
                    details = "Error verifying account: ${apiResult.message}"
                )
                dao.insertOrUpdateIntegration(errorAccount)
            }
        }

        return@withContext apiResult
    }

    suspend fun disconnectGitHub() = withContext(Dispatchers.IO) {
        val uid = currentUid()
        val disconnected = IntegrationAccount(
            provider = "github",
            userId = uid,
            username = "",
            connectionStatus = "NOT_CONNECTED",
            isConnected = false,
            lastSyncedAt = 0L,
            avatarUrl = "",
            displayName = "",
            publicReposCount = 0,
            followersCount = 0,
            followingCount = 0,
            publicGistsCount = 0,
            bio = "",
            company = "",
            location = "",
            topRepositoriesJson = "",
            details = "Disconnected. Connect your GitHub profile to sync public repositories.",
            errorMessage = ""
        )
        dao.insertOrUpdateIntegration(disconnected)
        recalibrateAudit(uid)

        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = uid,
                eventName = "GitHub Disconnected",
                detail = "Disconnected GitHub integration and cleared telemetry."
            )
        )
    }

    suspend fun importGitHubRepoToPortfolio(repo: GitHubRepoItem) = withContext(Dispatchers.IO) {
        val uid = currentUid()
        val newProject = PortfolioProject(
            userId = uid,
            title = repo.name,
            description = repo.description.ifBlank { "Production repository synced from GitHub (${repo.language})." },
            repositoryUrl = repo.url,
            liveUrl = "",
            status = "in_progress",
            technologies = repo.language.ifBlank { "Kotlin, Software Engineering" },
            skillsTargeted = repo.language
        )
        dao.insertProject(newProject)
        recalibrateAudit(uid)

        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = uid,
                eventName = "GitHub Repo Imported",
                detail = "Imported '${repo.name}' (${repo.language}, ${repo.stars} stars) into portfolio projects."
            )
        )
    }

    private fun buildTopReposJson(repos: List<GitHubRepoItem>): String {
        val jsonArray = org.json.JSONArray()
        repos.forEach { repo ->
            val obj = org.json.JSONObject()
            obj.put("name", repo.name)
            obj.put("description", repo.description)
            obj.put("url", repo.url)
            obj.put("stars", repo.stars)
            obj.put("forks", repo.forks)
            obj.put("language", repo.language)
            obj.put("updatedAt", repo.updatedAt)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    // Integration Sync
    suspend fun toggleIntegration(provider: String, username: String) = withContext(Dispatchers.IO) {
        val uid = currentUid()
        if (provider == "github") {
            val current = dao.getIntegration("github", uid)
            if (current?.isConnected == true) {
                disconnectGitHub()
            } else {
                validateAndConnectGitHub(username)
            }
        } else {
            val existing = dao.getIntegration(provider, uid)
            val isNowConnected = !(existing?.isConnected ?: false)
            val updated = IntegrationAccount(
                provider = provider,
                userId = uid,
                username = username.ifBlank { "linkedin-user" },
                connectionStatus = if (isNowConnected) "CONNECTED" else "NOT_CONNECTED",
                isConnected = isNowConnected,
                lastSyncedAt = if (isNowConnected) System.currentTimeMillis() else 0L,
                details = if (isNowConnected) "Profile linked • Keyword visibility synced" else "Disconnected"
            )
            dao.insertOrUpdateIntegration(updated)
            recalibrateAudit(uid)
        }
    }

    // === FEATURE 1: JOB DESCRIPTION MATCHER ===
    suspend fun recalculateJobMatch(jobPostingId: String): JobMatchResult = withContext(Dispatchers.IO) {
        val uid = currentUid()
        val job = dao.getJobPosting(jobPostingId) ?: JobMatcherEngine.PRESET_JOB_POSTINGS.first()
        val profile = dao.getUserProfile(uid)
        val skills = dao.getUserSkills(uid)
        val projects = dao.getProjects(uid)
        val latestResume = dao.getLatestResumeAudit(uid)

        val match = JobMatcherEngine.evaluateJobMatch(
            jobPosting = job,
            userProfile = profile,
            skills = skills,
            projects = projects,
            latestResume = latestResume
        ).copy(userId = uid)
        dao.insertJobMatchResult(match)
        return@withContext match
    }

    suspend fun analyzeCustomJobDescription(
        company: String,
        title: String,
        level: String,
        minExp: Float,
        jdText: String
    ): JobMatchResult = withContext(Dispatchers.IO) {
        val uid = currentUid()
        val extractedKeywords = mutableListOf<String>()
        val candidates = listOf(
            "Kotlin", "Java", "TypeScript", "React", "Node.js", "Python", "Go", "Distributed Systems",
            "Docker", "Kubernetes", "gRPC", "GraphQL", "PostgreSQL", "Redis", "Kafka", "CI/CD",
            "Microservices", "AWS", "GCP", "System Architecture", "High Availability", "Testing"
        )
        candidates.forEach { kw ->
            if (jdText.contains(kw, ignoreCase = true)) {
                extractedKeywords.add(kw)
            }
        }
        if (extractedKeywords.isEmpty()) {
            extractedKeywords.addAll(listOf("TypeScript", "React", "REST APIs", "Unit Testing", "Git"))
        }

        val customPosting = TargetJobPosting(
            id = "custom_${UUID.randomUUID().toString().take(6)}",
            company = company.ifBlank { "Custom Target Company" },
            title = title.ifBlank { "Software Engineer" },
            level = level.ifBlank { "Mid-Senior" },
            location = "Custom / Remote",
            minYearsExperience = minExp,
            requiredKeywords = extractedKeywords.take(6),
            preferredKeywords = extractedKeywords.drop(6),
            fullJobDescription = jdText,
            isPreset = false
        )
        dao.insertJobPosting(customPosting)

        val profile = dao.getUserProfile(uid)
        val skills = dao.getUserSkills(uid)
        val projects = dao.getProjects(uid)
        val latestResume = dao.getLatestResumeAudit(uid)

        val match = JobMatcherEngine.evaluateJobMatch(
            jobPosting = customPosting,
            userProfile = profile,
            skills = skills,
            projects = projects,
            latestResume = latestResume
        ).copy(userId = uid)
        dao.insertJobMatchResult(match)
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = uid,
                eventName = "Custom JD Matched",
                detail = "Matched against ${customPosting.company} (${customPosting.title}): Score ${match.matchScore}%"
            )
        )
        return@withContext match
    }

    // === FEATURE 2: AI RESUME BULLET REWRITER ===
    fun analyzeBullet(bulletText: String, targetRole: String = "Full Stack Engineer"): BulletAnalysis {
        return ResumeBulletRewriter.analyzeAndRewriteBullet(bulletText, targetRole)
    }

    suspend fun analyzeBulletWithAi(bulletText: String, targetRole: String = "Full Stack Engineer"): BulletAnalysis {
        return ResumeBulletRewriter.analyzeAndRewriteWithAi(bulletText, targetRole)
    }

    suspend fun applyBulletReplacement(originalBullet: String, newBulletText: String) = withContext(Dispatchers.IO) {
        val uid = currentUid()
        val latest = dao.getLatestResumeAudit(uid)
        if (latest != null) {
            val updatedResumeText = if (latest.rawText.contains(originalBullet)) {
                latest.rawText.replace(originalBullet, newBulletText)
            } else {
                "${latest.rawText}\n• $newBulletText"
            }
            val reAudited = analyzeResumeText(updatedResumeText, latest.filename).copy(userId = uid)
            dao.insertResumeAudit(reAudited)
            recalibrateAudit(uid)
        }
    }

    // === FEATURE 3: CONVERSATIONAL MOCK AI PROBING ===
    suspend fun processInterviewProbingTurn(
        sessionId: String,
        question: String,
        userAnswer: String,
        isFollowUp: Boolean
    ): Pair<ConversationMessage, Int> = withContext(Dispatchers.IO) {
        val uid = currentUid()
        val (aiMessage, score) = ConversationalInterviewEngine.evaluateAnswerAndGenerateResponseWithAi(
            currentQuestion = question,
            userAnswer = userAnswer,
            isFollowUp = isFollowUp
        )

        // Save answer entry
        val existingAnswers = dao.getInterviewAnswers(sessionId, uid)
        val newAnswer = InterviewAnswer(
            userId = uid,
            interviewId = sessionId,
            questionNumber = existingAnswers.size + 1,
            questionText = question,
            category = "Conversational Probing",
            difficulty = "Senior",
            rubric = "Production trade-offs & edge cases",
            answerText = userAnswer,
            score = score,
            clarityScore = (score * 0.95f).toInt().coerceIn(40, 98),
            technicalScore = score,
            feedback = aiMessage.feedbackSnippet ?: "Completed round.",
            suggestedImprovement = if (score >= 80) "Maintain deep quantitative metric focus." else "Quantify latencies and error boundaries under concurrency."
        )
        dao.insertInterviewAnswer(newAnswer)

        // Update overall session score
        val allAnswers = dao.getInterviewAnswers(sessionId, uid)
        val avgScore = allAnswers.map { it.score }.average().toInt()
        val session = dao.getInterviewSession(sessionId, uid)
        if (session != null) {
            dao.insertOrUpdateInterview(session.copy(userId = uid, overallScore = avgScore, status = if (isFollowUp) "completed" else "in_progress"))
        }

        recalibrateAudit(uid)
        return@withContext Pair(aiMessage, score)
    }

    // === FEATURE 4: 1-CLICK CAREER STARTER PRESETS (COLD-START RESOLUTION) ===
    suspend fun applyCareerStarterTemplate(roleName: String) = withContext(Dispatchers.IO) {
        val uid = currentUid()
        val currentProfile = dao.getUserProfile(uid) ?: UserProfile(id = uid)
        
        val (headline, industry, salary, skills, projects) = when (roleName) {
            "Android Mobile Engineer" -> {
                Quint(
                    "Senior Android Architect & Kotlin Specialist",
                    "Consumer Mobile & FinTech",
                    "$145,000 - $185,000",
                    listOf(
                        UserSkill(userId = uid, skillName = "Kotlin & Coroutines", category = "Mobile", proficiencyLevel = 5, verified = true),
                        UserSkill(userId = uid, skillName = "Jetpack Compose", category = "Mobile", proficiencyLevel = 5, verified = true),
                        UserSkill(userId = uid, skillName = "Room & SQLite Persistence", category = "Mobile", proficiencyLevel = 4, verified = true),
                        UserSkill(userId = uid, skillName = "Android Architecture (MVVM/MVI)", category = "Mobile", proficiencyLevel = 4, verified = true),
                        UserSkill(userId = uid, skillName = "Performance Profiling & Memory Leaks", category = "Mobile", proficiencyLevel = 3, verified = false),
                        UserSkill(userId = uid, skillName = "Gradle & CI/CD Automation", category = "DevOps & Cloud", proficiencyLevel = 3, verified = false)
                    ),
                    listOf(
                        PortfolioProject(
                            userId = uid,
                            title = "High-Performance Mobile Finance & Trading App",
                            description = "Real-time crypto & stock portfolio tracker with Jetpack Compose Canvas charts, offline Room caching, and biometrics.",
                            repositoryUrl = "https://github.com/alexchen/compose-fintech",
                            liveUrl = "https://play.google.com/store/apps/details?id=com.fintech.app",
                            status = "completed",
                            technologies = "Kotlin, Jetpack Compose, Room, Coroutines, Flow, Retrofit",
                            skillsTargeted = "Mobile, Jetpack Compose, State Management"
                        ),
                        PortfolioProject(
                            userId = uid,
                            title = "Offline-First Voice AI Audio Journal",
                            description = "Low-latency audio transcription and AI summarizer using on-device ML Kit and background Coroutine workers.",
                            repositoryUrl = "https://github.com/alexchen/voice-ai-journal",
                            liveUrl = "https://play.google.com/store/apps/details?id=com.voiceai.journal",
                            status = "in_progress",
                            technologies = "Kotlin, Room, WorkManager, CameraX, Compose M3",
                            skillsTargeted = "Mobile, Background Processing, On-Device AI"
                        )
                    )
                )
            }
            "AI / Machine Learning Engineer" -> {
                Quint(
                    "Applied AI Engineer & LLM Systems Specialist",
                    "Enterprise AI & Autonomous Agents",
                    "$160,000 - $210,000",
                    listOf(
                        UserSkill(userId = uid, skillName = "Python & PyTorch", category = "Programming Languages", proficiencyLevel = 5, verified = true),
                        UserSkill(userId = uid, skillName = "LLM Prompting & Function Calling", category = "AI & ML", proficiencyLevel = 4, verified = true),
                        UserSkill(userId = uid, skillName = "RAG & Vector Embeddings", category = "AI & ML", proficiencyLevel = 4, verified = true),
                        UserSkill(userId = uid, skillName = "Vector Databases (pgvector/Pinecone)", category = "AI & ML", proficiencyLevel = 4, verified = true),
                        UserSkill(userId = uid, skillName = "FastAPI & Model Serving", category = "Backend", proficiencyLevel = 3, verified = false),
                        UserSkill(userId = uid, skillName = "Data Pipelines & Feature Stores", category = "Data", proficiencyLevel = 3, verified = false)
                    ),
                    listOf(
                        PortfolioProject(
                            userId = uid,
                            title = "Enterprise Autonomous RAG Knowledge Base",
                            description = "High-accuracy semantic document intelligence engine with hybrid lexical-vector retrieval and self-corrective query reranking.",
                            repositoryUrl = "https://github.com/alexchen/enterprise-rag",
                            liveUrl = "https://rag-demo.careerhub.ai",
                            status = "completed",
                            technologies = "Python, FastAPI, pgvector, LangChain, OpenAI/Gemini",
                            skillsTargeted = "AI, Vector Search, Information Retrieval"
                        ),
                        PortfolioProject(
                            userId = uid,
                            title = "Agentic Code Review & Quality Assurance Bot",
                            description = "Multi-agent LLM workflow analyzing PR diffs against security vulnerabilities, test coverage gaps, and architectural anti-patterns.",
                            repositoryUrl = "https://github.com/alexchen/agentic-pr-review",
                            liveUrl = "https://pr-agent.careerhub.ai",
                            status = "in_progress",
                            technologies = "Python, PyTorch, Transformers, GitHub Webhooks",
                            skillsTargeted = "AI Agents, Code Analysis, Developer Tooling"
                        )
                    )
                )
            }
            "DevOps / Cloud Platform Engineer" -> {
                Quint(
                    "Cloud Infrastructure & Platform Architect",
                    "Cloud Computing & SaaS Infrastructure",
                    "$150,000 - $195,000",
                    listOf(
                        UserSkill(userId = uid, skillName = "Kubernetes & Container Orchestration", category = "DevOps & Cloud", proficiencyLevel = 5, verified = true),
                        UserSkill(userId = uid, skillName = "AWS Cloud Architecture", category = "DevOps & Cloud", proficiencyLevel = 4, verified = true),
                        UserSkill(userId = uid, skillName = "Terraform & Infrastructure-as-Code", category = "DevOps & Cloud", proficiencyLevel = 4, verified = true),
                        UserSkill(userId = uid, skillName = "CI/CD Automation (GitHub Actions)", category = "DevOps & Cloud", proficiencyLevel = 4, verified = true),
                        UserSkill(userId = uid, skillName = "Observability (Prometheus/Grafana)", category = "DevOps & Cloud", proficiencyLevel = 3, verified = false),
                        UserSkill(userId = uid, skillName = "Linux Internals & Bash Scripting", category = "Programming Languages", proficiencyLevel = 4, verified = true)
                    ),
                    listOf(
                        PortfolioProject(
                            userId = uid,
                            title = "Multi-Region Kubernetes Platform with GitOps",
                            description = "Automated zero-downtime cluster provisioning with Terraform, ArgoCD GitOps pipelines, and Istio service mesh mTLS.",
                            repositoryUrl = "https://github.com/alexchen/gitops-k8s-platform",
                            liveUrl = "https://grafana.cloud-infra.dev",
                            status = "completed",
                            technologies = "Kubernetes, Terraform, ArgoCD, Helm, AWS EKS, Prometheus",
                            skillsTargeted = "DevOps, Kubernetes, Infrastructure-as-Code"
                        ),
                        PortfolioProject(
                            userId = uid,
                            title = "Serverless Cloud Security Auditor & Auto-Remediator",
                            description = "Event-driven security scanner detecting S3 public buckets, unrotated IAM keys, and deploying automated remediation lambdas.",
                            repositoryUrl = "https://github.com/alexchen/cloud-security-auditor",
                            liveUrl = "https://security.cloud-infra.dev",
                            status = "in_progress",
                            technologies = "AWS Lambda, Python, EventBridge, CloudWatch",
                            skillsTargeted = "Cloud Security, Serverless, Compliance Automation"
                        )
                    )
                )
            }
            else -> {
                // Full Stack Engineer
                Quint(
                    "Senior Full Stack Software Architect",
                    "Enterprise B2B Software & FinTech",
                    "$140,000 - $180,000",
                    listOf(
                        UserSkill(userId = uid, skillName = "TypeScript & React", category = "Frontend", proficiencyLevel = 5, verified = true),
                        UserSkill(userId = uid, skillName = "Node.js & Express / NestJS", category = "Backend", proficiencyLevel = 5, verified = true),
                        UserSkill(userId = uid, skillName = "PostgreSQL & Database Design", category = "Databases", proficiencyLevel = 4, verified = true),
                        UserSkill(userId = uid, skillName = "Redis Caching & PubSub", category = "Databases", proficiencyLevel = 3, verified = false),
                        UserSkill(userId = uid, skillName = "Docker & Containerization", category = "DevOps & Cloud", proficiencyLevel = 4, verified = true),
                        UserSkill(userId = uid, skillName = "RESTful & GraphQL API Design", category = "Backend", proficiencyLevel = 4, verified = true)
                    ),
                    listOf(
                        PortfolioProject(
                            userId = uid,
                            title = "Distributed Real-Time Collaboration Canvas",
                            description = "Multiplayer interactive whiteboard powered by WebSockets, CRDT synchronization algorithms, and Redis pub/sub backplane.",
                            repositoryUrl = "https://github.com/alexchen/realtime-canvas",
                            liveUrl = "https://canvas.dev-preview.app",
                            status = "completed",
                            technologies = "TypeScript, React, Node.js, WebSockets, Redis, PostgreSQL",
                            skillsTargeted = "Full Stack, Concurrency, Real-Time Architecture"
                        ),
                        PortfolioProject(
                            userId = uid,
                            title = "Idempotent Payment Gateway & Ledger Engine",
                            description = "High-throughput financial ledger handling double-entry accounting, distributed locks, and automated reconciliation.",
                            repositoryUrl = "https://github.com/alexchen/payment-ledger",
                            liveUrl = "https://ledger.dev-preview.app",
                            status = "in_progress",
                            technologies = "Node.js, TypeScript, PostgreSQL, Docker, Jest",
                            skillsTargeted = "Backend, Distributed Transactions, Financial Systems"
                        )
                    )
                )
            }
        }

        // 1. Update Profile
        dao.insertOrUpdateProfile(
            currentProfile.copy(
                id = uid,
                targetRole = roleName,
                headline = headline,
                targetIndustry = industry,
                targetSalary = salary,
                readinessScore = 82
            )
        )

        // 2. Refresh Skills & Projects
        dao.clearUserSkills(uid)
        dao.insertUserSkills(skills)

        dao.clearProjects(uid)
        projects.forEach { dao.insertProject(it) }

        // 3. Recalibrate Skill Gaps and Roadmap
        recalibrateSkillGaps(roleName, uid)
        generateRoadmapForRole(roleName, uid)
        recalibrateAudit(uid)

        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = uid,
                eventName = "Career Preset Applied",
                detail = "Applied full 1-click starter configuration for $roleName."
            )
        )
    }

    // === JOB APPLICATION CRM ACTIONS ===
    suspend fun addJobApplication(app: JobApplication) = withContext(Dispatchers.IO) {
        val uid = currentUid()
        dao.insertJobApplication(app.copy(userId = uid))
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = uid,
                eventName = "Job Application Added",
                detail = "Added ${app.company} (${app.roleTitle}) to pipeline [${app.stage}]"
            )
        )
    }

    suspend fun updateJobApplicationStage(app: JobApplication, newStage: String) = withContext(Dispatchers.IO) {
        val uid = currentUid()
        dao.updateJobApplication(app.copy(userId = uid, stage = newStage))
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = uid,
                eventName = "Application Stage Advanced",
                detail = "${app.company} -> $newStage"
            )
        )
    }

    suspend fun deleteJobApplication(app: JobApplication) = withContext(Dispatchers.IO) {
        dao.deleteJobApplication(app)
    }

    // === CODING SANDBOX ACTIONS ===
    suspend fun toggleCodingChallengeCompletion(challengeId: String) = withContext(Dispatchers.IO) {
        val challenge = dao.getCodingChallenge(challengeId)
        if (challenge != null) {
            val updated = challenge.copy(isCompleted = !challenge.isCompleted)
            dao.updateCodingChallenge(updated)
            dao.insertAnalyticsEvent(
                AnalyticsEvent(
                    userId = currentUid(),
                    eventName = if (updated.isCompleted) "Challenge Completed" else "Challenge Reopened",
                    detail = "Coding sandbox: ${challenge.title}"
                )
            )
        }
    }

    // === SKILL SPRINTS ACTIONS ===
    suspend fun toggleSprintMilestone(sprintId: String, milestoneIndex: Int) = withContext(Dispatchers.IO) {
        val sprint = dao.getSkillSprint(sprintId)
        if (sprint != null) {
            val total = sprint.milestoneTasks.size
            val newCompleted = if (milestoneIndex < sprint.completedMilestones) {
                max(0, milestoneIndex)
            } else {
                min(total, milestoneIndex + 1)
            }
            val updated = sprint.copy(
                completedMilestones = newCompleted,
                currentDay = min(sprint.durationDays, max(1, newCompleted * 2))
            )
            dao.updateSkillSprint(updated)
            dao.insertAnalyticsEvent(
                AnalyticsEvent(
                    userId = currentUid(),
                    eventName = "Sprint Milestone Updated",
                    detail = "${sprint.sprintTitle}: $newCompleted/$total completed"
                )
            )
        }
    }

    suspend fun claimSprintReward(sprintId: String) = withContext(Dispatchers.IO) {
        val sprint = dao.getSkillSprint(sprintId)
        if (sprint != null) {
            val updated = sprint.copy(isClaimed = true, completedMilestones = sprint.milestoneTasks.size)
            dao.updateSkillSprint(updated)
            dao.insertAnalyticsEvent(
                AnalyticsEvent(
                    userId = currentUid(),
                    eventName = "Sprint Badge Awarded",
                    detail = "Claimed ${sprint.badgeName} (+${sprint.rewardXp} XP)"
                )
            )
        }
    }

    suspend fun updateOpportunityStatus(opportunityId: String, newStatus: String) = withContext(Dispatchers.IO) {
        dao.updateOpportunityStatus(opportunityId, newStatus)
        val opp = dao.getOpportunityById(opportunityId)
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = currentUid(),
                eventName = "Opportunity Status Changed",
                detail = "${opp?.title ?: opportunityId} -> $newStatus"
            )
        )
    }

    suspend fun updateOpportunityNotes(opportunityId: String, notes: String) = withContext(Dispatchers.IO) {
        dao.updateOpportunityNotes(opportunityId, notes)
    }

    suspend fun toggleOpportunityReminder(opportunityId: String, reminderSet: Boolean) = withContext(Dispatchers.IO) {
        dao.updateOpportunityReminder(opportunityId, reminderSet)
        val opp = dao.getOpportunityById(opportunityId)
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = currentUid(),
                eventName = if (reminderSet) "Opportunity Reminder Set" else "Opportunity Reminder Removed",
                detail = opp?.title ?: opportunityId
            )
        )
    }

    suspend fun addCustomOpportunity(opportunity: CareerOpportunity) = withContext(Dispatchers.IO) {
        dao.insertOpportunity(opportunity)
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                userId = currentUid(),
                eventName = "Custom Opportunity Added",
                detail = opportunity.title
            )
        )
    }
}

private data class Quint<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
