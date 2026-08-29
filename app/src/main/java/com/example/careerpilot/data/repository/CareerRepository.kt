package com.example.careerpilot.data.repository

import com.example.careerpilot.data.local.CareerDao
import com.example.careerpilot.data.model.*
import com.example.careerpilot.data.remote.github.GitHubApiClient
import com.example.careerpilot.data.remote.github.GitHubRepoItem
import com.example.careerpilot.data.remote.github.GitHubValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class CareerRepository(private val dao: CareerDao) {

    val userProfileFlow: Flow<UserProfile?> = dao.getUserProfileFlow()
    val userSkillsFlow: Flow<List<UserSkill>> = dao.getUserSkillsFlow()
    val skillGapsFlow: Flow<List<SkillGap>> = dao.getSkillGapsFlow()
    val activeRoadmapFlow: Flow<Roadmap?> = dao.getActiveRoadmapFlow()
    val roadmapItemsFlow: Flow<List<RoadmapItem>> = dao.getRoadmapItemsFlow()
    val projectsFlow: Flow<List<PortfolioProject>> = dao.getProjectsFlow()
    val latestResumeAuditFlow: Flow<ResumeAudit?> = dao.getLatestResumeAuditFlow()
    val resumeAuditsFlow: Flow<List<ResumeAudit>> = dao.getResumeAuditsFlow()
    val interviewsFlow: Flow<List<InterviewSession>> = dao.getInterviewsFlow()
    val learningResourcesFlow: Flow<List<LearningResource>> = dao.getLearningResourcesFlow()
    val integrationsFlow: Flow<List<IntegrationAccount>> = dao.getIntegrationsFlow()
    val recentAnalyticsFlow: Flow<List<AnalyticsEvent>> = dao.getRecentAnalyticsFlow()
    val auditIssuesFlow: Flow<List<AuditIssue>> = dao.getAuditIssuesFlow()
    val jobPostingsFlow: Flow<List<TargetJobPosting>> = dao.getJobPostingsFlow()
    val jobMatchesFlow: Flow<List<JobMatchResult>> = dao.getJobMatchesFlow()
    val jobApplicationsFlow: Flow<List<JobApplication>> = dao.getJobApplicationsFlow()
    val codingChallengesFlow: Flow<List<CodingChallenge>> = dao.getCodingChallengesFlow()
    val peerMatchesFlow: Flow<List<PeerMatch>> = dao.getPeerMatchesFlow()
    val skillSprintsFlow: Flow<List<SkillSprint>> = dao.getSkillSprintsFlow()

    suspend fun initializeDefaultDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingProfile = dao.getUserProfile()
        if (existingProfile == null) {
            val initialProfile = UserProfile()
            dao.insertOrUpdateProfile(initialProfile)

            // Initial Skills
            val defaultSkills = listOf(
                UserSkill(skillName = "TypeScript", category = "Programming Languages", proficiencyLevel = 4, verified = false),
                UserSkill(skillName = "React", category = "Frontend", proficiencyLevel = 4, verified = false),
                UserSkill(skillName = "Kotlin & Coroutines", category = "Mobile", proficiencyLevel = 4, verified = false),
                UserSkill(skillName = "Jetpack Compose", category = "Mobile", proficiencyLevel = 4, verified = false),
                UserSkill(skillName = "Node.js / Express", category = "Backend", proficiencyLevel = 3, verified = false),
                UserSkill(skillName = "PostgreSQL", category = "Databases", proficiencyLevel = 3, verified = false),
                UserSkill(skillName = "Docker & Containers", category = "DevOps & Cloud", proficiencyLevel = 2, verified = false),
                UserSkill(skillName = "System Design & Architecture", category = "Architecture", proficiencyLevel = 2, verified = false)
            )
            dao.insertUserSkills(defaultSkills)

            // Initial Projects
            dao.insertProject(
                PortfolioProject(
                    title = "Distributed Task Queue & Telemetry Hub",
                    description = "High-throughput asynchronous task orchestrator with Redis backed retry queues and live metrics dashboard.",
                    repositoryUrl = "https://github.com/alexchen/distributed-queue",
                    liveUrl = "https://queue-demo.dev.io",
                    status = "in_progress",
                    technologies = "Kotlin, Coroutines, Redis, Docker, Prometheus",
                    skillsTargeted = "Backend, Distributed Systems, Concurrency"
                )
            )
            dao.insertProject(
                PortfolioProject(
                    title = "Real-time Collaborative Canvas Engine",
                    description = "Low-latency whiteboard application leveraging WebSockets, CRDT conflict resolution, and Jetpack Compose canvas rendering.",
                    repositoryUrl = "https://github.com/alexchen/crdt-canvas",
                    liveUrl = "https://canvas.dev.io",
                    status = "in_progress",
                    technologies = "Jetpack Compose, WebSockets, TypeScript, Node.js",
                    skillsTargeted = "Frontend, Real-time Systems, UI Performance"
                )
            )

            // Initial Learning Resources (Proper functional lifecycle: Not Started -> In Progress -> Verified Completed)
            val learningList = listOf(
                LearningResource(
                    title = "Mastering Distributed Systems & Consistency Patterns",
                    provider = "Designing Data-Intensive Applications",
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
                    provider = "Android Developer Guides",
                    url = "https://developer.android.com/topic/architecture",
                    category = "Mobile",
                    skillTags = "Jetpack Compose, StateFlow, Coroutines, UDF",
                    estimatedMinutes = 90,
                    difficulty = "Intermediate",
                    resourceType = "Documentation",
                    status = "NOT_STARTED",
                    progressPercent = 0,
                    contentSummary = "Explores Unidirectional Data Flow (UDF), managing state hoisting in Jetpack Compose, handling configuration changes with ViewModel, and safely collecting StateFlows with repeatOnLifecycle.",
                    quizQuestion = "Why is collectAsStateWithLifecycle preferred over collectAsState when consuming Flow in Android Jetpack Compose?",
                    quizOptions = "It automatically halts Flow collection when the Composable lifecycle is below STARTED, saving battery and CPU|It converts synchronous network calls into coroutines automatically|It renders the UI directly to OpenGL without recomposition|It bypasses ViewModel state saving during screen rotations",
                    quizCorrectIndex = 0,
                    isCompleted = false
                ),
                LearningResource(
                    title = "Database Indexing & Query Plan Optimization",
                    provider = "Use The Index, Luke",
                    url = "https://use-the-index-luke.com",
                    category = "Databases",
                    skillTags = "PostgreSQL, B-Tree Indexing, EXPLAIN ANALYZE",
                    estimatedMinutes = 60,
                    difficulty = "Intermediate",
                    resourceType = "Article",
                    status = "NOT_STARTED",
                    progressPercent = 0,
                    contentSummary = "Comprehensive study on how B-Tree indexes function internally, how multi-column index order impacts query selectivity, avoiding sequential scans, and reading EXPLAIN ANALYZE execution plans.",
                    quizQuestion = "In a compound (multi-column) index on columns (A, B), which query CANNOT efficiently use the index leading edge?",
                    quizOptions = "SELECT * FROM tbl WHERE B = 10 (omitting column A)|SELECT * FROM tbl WHERE A = 5 AND B = 10|SELECT * FROM tbl WHERE A = 5|SELECT * FROM tbl WHERE A = 5 ORDER BY B",
                    quizCorrectIndex = 0,
                    isCompleted = false
                ),
                LearningResource(
                    title = "Production RAG & Vector Embeddings with LLMs",
                    provider = "DeepLearning.AI",
                    url = "https://deeplearning.ai",
                    category = "AI & ML",
                    skillTags = "Vector Embeddings, Cosine Similarity, Chunking, RAG",
                    estimatedMinutes = 100,
                    difficulty = "Advanced",
                    resourceType = "Course",
                    status = "NOT_STARTED",
                    progressPercent = 0,
                    contentSummary = "Practical guide to building production Retrieval-Augmented Generation pipelines: optimal text chunking strategies, semantic similarity search with HNSW indexes, hybrid search with BM25, and prompt grounding.",
                    quizQuestion = "What is the primary benefit of adding a Re-ranking step (e.g. Cross-Encoder) after preliminary vector similarity search in RAG?",
                    quizOptions = "It precisely scores semantic relevance between the query and top candidates, eliminating irrelevant context hallucinations|It speeds up vector indexing by 100x on cold start|It generates synthetic training tokens for downstream model fine-tuning|It eliminates the need for vector databases altogether",
                    quizCorrectIndex = 0,
                    isCompleted = false
                ),
                LearningResource(
                    title = "High-Performance Concurrency with Kotlin Coroutines",
                    provider = "Kotlin Official Documentation",
                    url = "https://kotlinlang.org/docs/coroutines-overview.html",
                    category = "Mobile",
                    skillTags = "Kotlin, Coroutines, Dispatchers, Mutex",
                    estimatedMinutes = 75,
                    difficulty = "Intermediate",
                    resourceType = "Article",
                    status = "NOT_STARTED",
                    progressPercent = 0,
                    contentSummary = "Structured concurrency principles, CoroutineScope hierarchy, exception propagation with SupervisorJob, non-blocking suspension vs blocking threads, and shared state synchronization with Mutex.",
                    quizQuestion = "What happens to sibling coroutines in a standard CoroutineScope (without SupervisorJob) when one child coroutine fails with an uncaught exception?",
                    quizOptions = "The exception cancels the parent scope, which immediately cancels all other sibling coroutines|Sibling coroutines continue running silently indefinitely|The failing coroutine is automatically restarted 3 times|The entire Android OS kills the application process immediately",
                    quizCorrectIndex = 0,
                    isCompleted = false
                )
            )
            dao.insertLearningResources(learningList)

            // Initial Integrations (Clean real state: Not Connected until verified)
            dao.insertOrUpdateIntegration(
                IntegrationAccount(
                    provider = "github",
                    username = "",
                    connectionStatus = "NOT_CONNECTED",
                    isConnected = false,
                    lastSyncedAt = 0L,
                    details = "Connect your GitHub profile to sync public repositories and commit telemetry."
                )
            )
            dao.insertOrUpdateIntegration(
                IntegrationAccount(
                    provider = "linkedin",
                    username = "",
                    connectionStatus = "NOT_CONNECTED",
                    isConnected = false,
                    lastSyncedAt = 0L,
                    details = "Connect your LinkedIn profile for keyword visibility."
                )
            )

            // Seed preset target job postings
            dao.insertJobPostings(JobMatcherEngine.PRESET_JOB_POSTINGS)

            // Seed initial Job Applications, Coding Sandbox, Peers, and Sprints
            dao.insertJobApplications(BenchmarkCatalog.INITIAL_JOB_APPLICATIONS)
            dao.insertCodingChallenges(BenchmarkCatalog.INITIAL_CODING_CHALLENGES)
            dao.insertPeerMatches(BenchmarkCatalog.INITIAL_PEER_MATCHES)
            dao.insertSkillSprints(BenchmarkCatalog.INITIAL_SKILL_SPRINTS)

            // Run initial skill gap calibration and roadmap generation
            recalibrateSkillGaps(initialProfile.targetRole)
            generateRoadmapForRole(initialProfile.targetRole)

            // Run initial Audit Evaluation
            recalibrateAudit()

            // Calculate initial matches for preset jobs
            JobMatcherEngine.PRESET_JOB_POSTINGS.forEach { job ->
                val match = JobMatcherEngine.evaluateJobMatch(
                    jobPosting = job,
                    userProfile = initialProfile,
                    skills = defaultSkills,
                    projects = dao.getProjects(),
                    latestResume = null
                )
                dao.insertJobMatchResult(match)
            }

            dao.insertAnalyticsEvent(
                AnalyticsEvent(
                    eventName = "App Initialized",
                    detail = "Initialized default profile, benchmarks, audit engine, and job matcher."
                )
            )
        } else {
            // Ensure presets exist if added later
            val existingPostings = dao.getJobPostings()
            if (existingPostings.isEmpty()) {
                dao.insertJobPostings(JobMatcherEngine.PRESET_JOB_POSTINGS)
            }
            val existingApps = dao.getJobApplications()
            if (existingApps.isEmpty()) {
                dao.insertJobApplications(BenchmarkCatalog.INITIAL_JOB_APPLICATIONS)
                dao.insertCodingChallenges(BenchmarkCatalog.INITIAL_CODING_CHALLENGES)
                dao.insertPeerMatches(BenchmarkCatalog.INITIAL_PEER_MATCHES)
                dao.insertSkillSprints(BenchmarkCatalog.INITIAL_SKILL_SPRINTS)
            }
            // Recalibrate audit on startup
            recalibrateAudit()
        }
    }

    suspend fun recalibrateAudit(): AuditScoreSummary = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfile()
        val skills = dao.getUserSkills()
        val projects = dao.getProjects()
        val latestResume = dao.getLatestResumeAudit()
        val interviewAnswers = dao.getAllInterviewAnswers()
        val integrations = dao.getIntegrations()
        val existingIssues = dao.getAuditIssues()

        val (newIssues, summary) = AuditEngine.evaluateCandidate(
            profile = profile,
            skills = skills,
            projects = projects,
            latestResume = latestResume,
            interviewAnswers = interviewAnswers,
            integrations = integrations,
            existingIssues = existingIssues
        )

        dao.clearAuditIssues()
        dao.insertAuditIssues(newIssues)

        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                eventName = "Audit Recalibrated",
                detail = "Net Readiness: ${summary.netAuditScore}% (${summary.totalDemerits} demerits, ${summary.criticalCount} critical, ${summary.highCount} high)"
            )
        )

        summary
    }

    suspend fun updateAuditIssueStatus(issueId: String, newStatus: String) = withContext(Dispatchers.IO) {
        val issue = dao.getAuditIssue(issueId)
        if (issue != null) {
            val updated = issue.copy(
                status = newStatus,
                scoreImpact = if (newStatus == "RESOLVED") 0 else issue.scoreImpact,
                updatedAt = System.currentTimeMillis()
            )
            dao.updateAuditIssue(updated)
            recalibrateAudit()

            dao.insertAnalyticsEvent(
                AnalyticsEvent(
                    eventName = "Audit Issue Status Updated",
                    detail = "${issue.title} -> $newStatus"
                )
            )
        }
    }

    suspend fun updateProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateProfile(profile)
        recalibrateSkillGaps(profile.targetRole)
        recalibrateAudit()
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                eventName = "Profile Updated",
                detail = "Target role set to ${profile.targetRole}"
            )
        )
    }

    suspend fun recalibrateSkillGaps(targetRole: String) = withContext(Dispatchers.IO) {
        val benchmarks = BenchmarkCatalog.ROLE_BENCHMARKS[targetRole]
            ?: BenchmarkCatalog.ROLE_BENCHMARKS["Full Stack Engineer"]!!
        val userSkills = dao.getUserSkills().associateBy { it.skillName.lowercase() }

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

        dao.clearSkillGaps()
        dao.insertSkillGaps(newGaps)

        val score = if (totalWeight > 0) ((earnedWeight / totalWeight) * 100f).roundToInt() else 65
        val currentProfile = dao.getUserProfile() ?: UserProfile()
        dao.insertOrUpdateProfile(currentProfile.copy(readinessScore = score, targetRole = targetRole))

        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                eventName = "Skill Calibration Completed",
                detail = "Calibrated readiness score: $score% for $targetRole"
            )
        )
    }

    suspend fun addOrUpdateUserSkill(skill: UserSkill) = withContext(Dispatchers.IO) {
        dao.insertUserSkill(skill)
        val profile = dao.getUserProfile()
        if (profile != null) {
            recalibrateSkillGaps(profile.targetRole)
        }
        recalibrateAudit()
    }

    suspend fun deleteUserSkill(skill: UserSkill) = withContext(Dispatchers.IO) {
        dao.deleteUserSkill(skill)
        val profile = dao.getUserProfile()
        if (profile != null) {
            recalibrateSkillGaps(profile.targetRole)
        }
        recalibrateAudit()
    }

    suspend fun generateRoadmapForRole(targetRole: String) = withContext(Dispatchers.IO) {
        val gaps = dao.getSkillGaps()
        val highPriority = gaps.filter { it.priority == "high" }
        val mediumPriority = gaps.filter { it.priority == "medium" }

        val items = mutableListOf<RoadmapItem>()
        var order = 0

        // Phase 1: Core Deficiencies & Foundations
        items.add(
            RoadmapItem(
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

        // Phase 3: Portfolio, Capstones & Interview Readiness
        items.add(
            RoadmapItem(
                phaseNumber = 3,
                phaseTitle = "Portfolio Capstones & Staff Interview Readiness",
                title = "Publish Production Portfolio Project with Documentation",
                description = "Deploy live demonstrator, write comprehensive architectural README with system diagrams, and record walkthrough.",
                category = "Portfolio Deliverable",
                estimatedHours = 8.0f,
                orderIndex = ++order,
                isCompleted = false
            )
        )
        items.add(
            RoadmapItem(
                phaseNumber = 3,
                phaseTitle = "Portfolio Capstones & Staff Interview Readiness",
                title = "Complete 3 AI System Design & Algorithmic Mock Interviews",
                description = "Simulate technical phone screens, practice structured verbal responses, and calibrate rubric scores to >85%.",
                category = "Interview Prep",
                estimatedHours = 4.0f,
                orderIndex = ++order,
                isCompleted = false
            )
        )

        dao.clearRoadmapItems()
        dao.insertRoadmapItems(items)

        val total = items.size
        val completed = items.count { it.isCompleted }
        val percent = if (total > 0) (completed.toFloat() / total.toFloat()) * 100f else 0f

        val roadmap = Roadmap(
            id = "active_roadmap",
            title = "3-Phase Trajectory for $targetRole",
            targetRole = targetRole,
            summary = "Structured progression addressing ${highPriority.size} high-priority gap areas and building production portfolio proof.",
            totalTasks = total,
            completedTasks = completed,
            progressPercent = percent,
            status = "in_progress"
        )
        dao.insertOrUpdateRoadmap(roadmap)

        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                eventName = "Roadmap Generated",
                detail = "Created 3-phase progression with $total milestones."
            )
        )
    }

    suspend fun toggleRoadmapItem(itemId: Long) = withContext(Dispatchers.IO) {
        val items = dao.getRoadmapItems()
        val target = items.find { it.id == itemId } ?: return@withContext
        val updated = target.copy(
            isCompleted = !target.isCompleted,
            completedAt = if (!target.isCompleted) System.currentTimeMillis() else null
        )
        dao.updateRoadmapItem(updated)

        val updatedItems = dao.getRoadmapItems()
        val total = updatedItems.size
        val completed = updatedItems.count { it.isCompleted }
        val percent = if (total > 0) (completed.toFloat() / total.toFloat()) * 100f else 0f

        val roadmap = dao.getActiveRoadmap()
        if (roadmap != null) {
            dao.insertOrUpdateRoadmap(
                roadmap.copy(
                    totalTasks = total,
                    completedTasks = completed,
                    progressPercent = percent
                )
            )
        }

        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                eventName = if (updated.isCompleted) "Roadmap Task Completed" else "Roadmap Task Reopened",
                detail = updated.title
            )
        )
    }

    suspend fun analyzeResumeText(rawText: String, filename: String = "Resume.pdf"): ResumeAudit = withContext(Dispatchers.IO) {
        val lower = rawText.lowercase()
        val profile = dao.getUserProfile()
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
        recalibrateAudit()
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                eventName = "Resume Analyzed",
                detail = "ATS Score calculated: $overallScore/100"
            )
        )
        audit
    }

    suspend fun startInterviewSession(roleTarget: String, difficulty: String): InterviewSession = withContext(Dispatchers.IO) {
        val sessionId = UUID.randomUUID().toString()
        val session = InterviewSession(
            id = sessionId,
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

        val improvement = "💡 Pro Tip: Frame your answers using the STAR method (Situation, Task, Action, Result) and explicitly mention performance trade-offs."

        val answer = InterviewAnswer(
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

        val answers = dao.getInterviewAnswers(sessionId)
        val avgScore = if (answers.isNotEmpty()) answers.map { it.score }.average().roundToInt() else score
        val session = dao.getInterviewSession(sessionId)
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

        recalibrateAudit()

        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                eventName = "Interview Answer Submitted",
                detail = "Question ${questionIndex + 1} scored $score/100"
            )
        )

        answer
    }

    suspend fun getNextBestAction(): NextBestAction = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfile()
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
        val auditIssues = dao.getAuditIssues().filter { it.status != "RESOLVED" }
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

        val gaps = dao.getSkillGaps()
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

        val roadmap = dao.getActiveRoadmap()
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

        val items = dao.getRoadmapItems()
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
        dao.insertProject(project)
        recalibrateAudit()
    }

    suspend fun updateProject(project: PortfolioProject) = withContext(Dispatchers.IO) {
        dao.updateProject(project)
        recalibrateAudit()
    }

    suspend fun deleteProject(project: PortfolioProject) = withContext(Dispatchers.IO) {
        dao.deleteProject(project)
        recalibrateAudit()
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
                    eventName = "Learning Verified & Completed",
                    detail = "Mastered '${resource.title}' (+${resource.estimatedMinutes}m competency credit)"
                )
            )
            return@withContext true
        } else {
            dao.insertAnalyticsEvent(
                AnalyticsEvent(
                    eventName = "Comprehension Check Attempted",
                    detail = "Quiz attempt incorrect for '${resource.title}' — review required."
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
        val trimmed = username.trim()
        if (trimmed.isBlank()) {
            val errResult = GitHubValidationResult.Error(400, "Please enter a valid GitHub username.")
            return@withContext errResult
        }

        // Set state to CHECKING
        val existing = dao.getIntegration("github") ?: IntegrationAccount(provider = "github")
        dao.insertOrUpdateIntegration(
            existing.copy(
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
                recalibrateAudit()

                dao.insertAnalyticsEvent(
                    AnalyticsEvent(
                        eventName = "GitHub Connected",
                        detail = "Verified GitHub account '${profile.username}' (${profile.publicRepos} repos, ${profile.followers} followers)"
                    )
                )
            }
            is GitHubValidationResult.UserNotFound -> {
                val notFoundAccount = IntegrationAccount(
                    provider = "github",
                    username = trimmed,
                    connectionStatus = "NOT_FOUND",
                    isConnected = false,
                    errorMessage = apiResult.message,
                    details = "User '$trimmed' not found on GitHub."
                )
                dao.insertOrUpdateIntegration(notFoundAccount)
                recalibrateAudit()

                dao.insertAnalyticsEvent(
                    AnalyticsEvent(
                        eventName = "GitHub Verification Failed",
                        detail = "User '$trimmed' does not exist on GitHub (HTTP 404)"
                    )
                )
            }
            is GitHubValidationResult.RateLimited -> {
                val rateLimitedAccount = IntegrationAccount(
                    provider = "github",
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
        val disconnected = IntegrationAccount(
            provider = "github",
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
        recalibrateAudit()

        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                eventName = "GitHub Disconnected",
                detail = "Disconnected GitHub integration and cleared telemetry."
            )
        )
    }

    suspend fun importGitHubRepoToPortfolio(repo: GitHubRepoItem) = withContext(Dispatchers.IO) {
        val newProject = PortfolioProject(
            title = repo.name,
            description = repo.description.ifBlank { "Production repository synced from GitHub (${repo.language})." },
            repositoryUrl = repo.url,
            liveUrl = "",
            status = "in_progress",
            technologies = repo.language.ifBlank { "Kotlin, Software Engineering" },
            skillsTargeted = repo.language
        )
        dao.insertProject(newProject)
        recalibrateAudit()

        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                eventName = "GitHub Repo Imported",
                detail = "Imported '${repo.name}' (${repo.language}, ★${repo.stars}) into portfolio projects."
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
        if (provider == "github") {
            val current = dao.getIntegration("github")
            if (current?.isConnected == true) {
                disconnectGitHub()
            } else {
                validateAndConnectGitHub(username)
            }
        } else {
            val existing = dao.getIntegration(provider)
            val isNowConnected = !(existing?.isConnected ?: false)
            val updated = IntegrationAccount(
                provider = provider,
                username = username.ifBlank { "linkedin-user" },
                connectionStatus = if (isNowConnected) "CONNECTED" else "NOT_CONNECTED",
                isConnected = isNowConnected,
                lastSyncedAt = if (isNowConnected) System.currentTimeMillis() else 0L,
                details = if (isNowConnected) "Profile linked • Keyword visibility synced" else "Disconnected"
            )
            dao.insertOrUpdateIntegration(updated)
            recalibrateAudit()
        }
    }

    // === FEATURE 1: JOB DESCRIPTION MATCHER ===
    suspend fun recalculateJobMatch(jobPostingId: String): JobMatchResult = withContext(Dispatchers.IO) {
        val job = dao.getJobPosting(jobPostingId) ?: JobMatcherEngine.PRESET_JOB_POSTINGS.first()
        val profile = dao.getUserProfile()
        val skills = dao.getUserSkills()
        val projects = dao.getProjects()
        val latestResume = dao.getLatestResumeAudit()

        val match = JobMatcherEngine.evaluateJobMatch(
            jobPosting = job,
            userProfile = profile,
            skills = skills,
            projects = projects,
            latestResume = latestResume
        )
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

        val profile = dao.getUserProfile()
        val skills = dao.getUserSkills()
        val projects = dao.getProjects()
        val latestResume = dao.getLatestResumeAudit()

        val match = JobMatcherEngine.evaluateJobMatch(
            jobPosting = customPosting,
            userProfile = profile,
            skills = skills,
            projects = projects,
            latestResume = latestResume
        )
        dao.insertJobMatchResult(match)
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
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

    suspend fun applyBulletReplacement(originalBullet: String, newBulletText: String) = withContext(Dispatchers.IO) {
        val latest = dao.getLatestResumeAudit()
        if (latest != null) {
            val updatedResumeText = if (latest.rawText.contains(originalBullet)) {
                latest.rawText.replace(originalBullet, newBulletText)
            } else {
                "${latest.rawText}\n• $newBulletText"
            }
            val reAudited = analyzeResumeText(updatedResumeText, latest.filename)
            dao.insertResumeAudit(reAudited)
            recalibrateAudit()
        }
    }

    // === FEATURE 3: CONVERSATIONAL MOCK AI PROBING ===
    suspend fun processInterviewProbingTurn(
        sessionId: String,
        question: String,
        userAnswer: String,
        isFollowUp: Boolean
    ): Pair<ConversationMessage, Int> = withContext(Dispatchers.IO) {
        val (aiMessage, score) = ConversationalInterviewEngine.evaluateAnswerAndGenerateResponse(
            currentQuestion = question,
            userAnswer = userAnswer,
            isFollowUp = isFollowUp
        )

        // Save answer entry
        val existingAnswers = dao.getInterviewAnswers(sessionId)
        val newAnswer = InterviewAnswer(
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
        val allAnswers = dao.getInterviewAnswers(sessionId)
        val avgScore = allAnswers.map { it.score }.average().toInt()
        val session = dao.getInterviewSession(sessionId)
        if (session != null) {
            dao.insertOrUpdateInterview(session.copy(overallScore = avgScore, status = if (isFollowUp) "completed" else "in_progress"))
        }

        recalibrateAudit()
        return@withContext Pair(aiMessage, score)
    }

    // === FEATURE 4: 1-CLICK CAREER STARTER PRESETS (COLD-START RESOLUTION) ===
    suspend fun applyCareerStarterTemplate(roleName: String) = withContext(Dispatchers.IO) {
        val currentProfile = dao.getUserProfile() ?: UserProfile()
        
        val (headline, industry, salary, skills, projects) = when (roleName) {
            "Android Mobile Engineer" -> {
                Quint(
                    "Senior Android Architect & Kotlin Specialist",
                    "Consumer Mobile & FinTech",
                    "$145,000 - $185,000",
                    listOf(
                        UserSkill(skillName = "Kotlin & Coroutines", category = "Mobile", proficiencyLevel = 5, verified = true),
                        UserSkill(skillName = "Jetpack Compose", category = "Mobile", proficiencyLevel = 5, verified = true),
                        UserSkill(skillName = "Room & SQLite Persistence", category = "Mobile", proficiencyLevel = 4, verified = true),
                        UserSkill(skillName = "Android Architecture (MVVM/MVI)", category = "Mobile", proficiencyLevel = 4, verified = true),
                        UserSkill(skillName = "Performance Profiling & Memory Leaks", category = "Mobile", proficiencyLevel = 3, verified = false),
                        UserSkill(skillName = "Gradle & CI/CD Automation", category = "DevOps & Cloud", proficiencyLevel = 3, verified = false)
                    ),
                    listOf(
                        PortfolioProject(
                            title = "High-Performance Mobile Finance & Trading App",
                            description = "Real-time crypto & stock portfolio tracker with Jetpack Compose Canvas charts, offline Room caching, and biometrics.",
                            repositoryUrl = "https://github.com/alexchen/compose-fintech",
                            liveUrl = "https://play.google.com/store/apps/details?id=com.fintech.app",
                            status = "completed",
                            technologies = "Kotlin, Jetpack Compose, Room, Coroutines, Flow, Retrofit",
                            skillsTargeted = "Mobile, Jetpack Compose, State Management"
                        ),
                        PortfolioProject(
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
                        UserSkill(skillName = "Python & PyTorch", category = "Programming Languages", proficiencyLevel = 5, verified = true),
                        UserSkill(skillName = "LLM Prompting & Function Calling", category = "AI & ML", proficiencyLevel = 4, verified = true),
                        UserSkill(skillName = "RAG & Vector Embeddings", category = "AI & ML", proficiencyLevel = 4, verified = true),
                        UserSkill(skillName = "Vector Databases (pgvector/Pinecone)", category = "AI & ML", proficiencyLevel = 4, verified = true),
                        UserSkill(skillName = "FastAPI & Model Serving", category = "Backend", proficiencyLevel = 3, verified = false),
                        UserSkill(skillName = "Data Pipelines & Feature Stores", category = "Data", proficiencyLevel = 3, verified = false)
                    ),
                    listOf(
                        PortfolioProject(
                            title = "Enterprise Autonomous RAG Knowledge Base",
                            description = "High-accuracy semantic document intelligence engine with hybrid lexical-vector retrieval and self-corrective query reranking.",
                            repositoryUrl = "https://github.com/alexchen/enterprise-rag",
                            liveUrl = "https://rag-demo.ai.platform",
                            status = "completed",
                            technologies = "Python, PyTorch, LangChain, Pinecone, FastAPI, Docker",
                            skillsTargeted = "AI & ML, Vector Search, Scalable Serving"
                        ),
                        PortfolioProject(
                            title = "Real-time Agentic Code Review Assistant",
                            description = "Multi-agent LLM orchestrator that pulls GitHub PRs, runs AST static analysis, and proposes verified diffs.",
                            repositoryUrl = "https://github.com/alexchen/agentic-pr-reviewer",
                            liveUrl = "https://ai-reviewer.cloud",
                            status = "in_progress",
                            technologies = "Python, Gemini API, Redis, Celery, Docker",
                            skillsTargeted = "AI Agents, LLM Evaluation, Automation"
                        )
                    )
                )
            }
            "DevOps / Cloud Architect" -> {
                Quint(
                    "Cloud Infrastructure & Reliability Architect",
                    "Cloud Infrastructure & FinTech Scaleups",
                    "$150,000 - $195,000",
                    listOf(
                        UserSkill(skillName = "Terraform / IaC", category = "DevOps & Cloud", proficiencyLevel = 5, verified = true),
                        UserSkill(skillName = "Kubernetes & Container Orchestration", category = "DevOps & Cloud", proficiencyLevel = 4, verified = true),
                        UserSkill(skillName = "AWS / GCP Cloud Architecture", category = "DevOps & Cloud", proficiencyLevel = 4, verified = true),
                        UserSkill(skillName = "Observability (Prometheus/Grafana)", category = "DevOps & Cloud", proficiencyLevel = 4, verified = true),
                        UserSkill(skillName = "CI/CD Pipelines (GitHub Actions)", category = "DevOps & Cloud", proficiencyLevel = 4, verified = true),
                        UserSkill(skillName = "Network Security & Zero Trust", category = "Security", proficiencyLevel = 3, verified = false)
                    ),
                    listOf(
                        PortfolioProject(
                            title = "Multi-Region Kubernetes Disaster Recovery Mesh",
                            description = "Automated Terraform IaC provisioning with Istio service mesh, Prometheus observability, and zero-downtime failover.",
                            repositoryUrl = "https://github.com/alexchen/k8s-mesh-infra",
                            liveUrl = "https://mesh-status.infra.io",
                            status = "completed",
                            technologies = "Terraform, Kubernetes, Helm, Istio, AWS EKS, Prometheus",
                            skillsTargeted = "DevOps & Cloud, Zero Trust, High Availability"
                        ),
                        PortfolioProject(
                            title = "GitOps Enterprise Continuous Delivery Engine",
                            description = "ArgoCD and GitHub Actions pipeline with automated canary deployments, security vulnerability scanning, and Slack alerting.",
                            repositoryUrl = "https://github.com/alexchen/gitops-cd-engine",
                            liveUrl = "https://cd.internal.cloud",
                            status = "in_progress",
                            technologies = "GitHub Actions, ArgoCD, Docker, Trivy, Vault",
                            skillsTargeted = "CI/CD, Security, Infrastructure"
                        )
                    )
                )
            }
            else -> { // Default: Full Stack Engineer
                Quint(
                    "Senior Full Stack & Systems Engineer",
                    "Fintech & Scalable Cloud Platforms",
                    "$140,000 - $180,000",
                    listOf(
                        UserSkill(skillName = "TypeScript", category = "Programming Languages", proficiencyLevel = 4, verified = true),
                        UserSkill(skillName = "React", category = "Frontend", proficiencyLevel = 4, verified = true),
                        UserSkill(skillName = "Kotlin & Coroutines", category = "Mobile", proficiencyLevel = 4, verified = true),
                        UserSkill(skillName = "Node.js / Express", category = "Backend", proficiencyLevel = 4, verified = true),
                        UserSkill(skillName = "PostgreSQL & Index Tuning", category = "Databases", proficiencyLevel = 4, verified = true),
                        UserSkill(skillName = "Docker & Containers", category = "DevOps & Cloud", proficiencyLevel = 3, verified = false),
                        UserSkill(skillName = "System Design & Scalability", category = "Architecture", proficiencyLevel = 3, verified = false)
                    ),
                    listOf(
                        PortfolioProject(
                            title = "Distributed High-Throughput Task Queue",
                            description = "Asynchronous task orchestrator with Redis backed retry queues, worker concurrency pools, and telemetry.",
                            repositoryUrl = "https://github.com/alexchen/distributed-queue",
                            liveUrl = "https://queue-demo.dev.io",
                            status = "completed",
                            technologies = "Kotlin, Coroutines, Redis, Docker, Prometheus",
                            skillsTargeted = "Backend, Distributed Systems, Concurrency"
                        ),
                        PortfolioProject(
                            title = "Real-time Collaborative Whiteboard Engine",
                            description = "Low-latency whiteboard application leveraging WebSockets, CRDT conflict resolution, and Compose canvas rendering.",
                            repositoryUrl = "https://github.com/alexchen/crdt-canvas",
                            liveUrl = "https://canvas.dev.io",
                            status = "in_progress",
                            technologies = "Jetpack Compose, WebSockets, TypeScript, Node.js",
                            skillsTargeted = "Frontend, Real-time Systems, UI Performance"
                        )
                    )
                )
            }
        }

        // 1. Update Profile
        dao.insertOrUpdateProfile(
            currentProfile.copy(
                targetRole = roleName,
                headline = headline,
                targetIndustry = industry,
                targetSalary = salary,
                readinessScore = 82
            )
        )

        // 2. Refresh Skills & Projects
        dao.clearUserSkills()
        dao.insertUserSkills(skills)

        dao.clearProjects()
        projects.forEach { dao.insertProject(it) }

        // 3. Recalibrate Skill Gaps and Roadmap
        recalibrateSkillGaps(roleName)
        generateRoadmapForRole(roleName)
        recalibrateAudit()

        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                eventName = "Career Preset Applied",
                detail = "Applied full 1-click starter configuration for $roleName."
            )
        )
    }

    // === JOB APPLICATION CRM ACTIONS ===
    suspend fun addJobApplication(app: JobApplication) = withContext(Dispatchers.IO) {
        dao.insertJobApplication(app)
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
                eventName = "Job Application Added",
                detail = "Added ${app.company} (${app.roleTitle}) to pipeline [${app.stage}]"
            )
        )
    }

    suspend fun updateJobApplicationStage(app: JobApplication, newStage: String) = withContext(Dispatchers.IO) {
        dao.updateJobApplication(app.copy(stage = newStage))
        dao.insertAnalyticsEvent(
            AnalyticsEvent(
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
                    eventName = "Sprint Badge Awarded",
                    detail = "Claimed ${sprint.badgeName} (+${sprint.rewardXp} XP)"
                )
            )
        }
    }
}


private data class Quint<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

