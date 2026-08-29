package com.example.careerpilot.data.repository

data class BenchmarkRequirement(
    val skill: String,
    val category: String,
    val requiredLevel: Int,
    val weight: Float
)

data class InterviewQuestionTemplate(
    val questionText: String,
    val category: String,
    val difficulty: String,
    val rubric: String,
    val keywords: List<String>
)

object BenchmarkCatalog {
    val ROLE_BENCHMARKS = mapOf(
        "Full Stack Engineer" to listOf(
            BenchmarkRequirement("TypeScript", "Programming Languages", 4, 1.5f),
            BenchmarkRequirement("React", "Frontend", 4, 1.5f),
            BenchmarkRequirement("Node.js / Express", "Backend", 4, 1.5f),
            BenchmarkRequirement("PostgreSQL", "Databases", 3, 1.3f),
            BenchmarkRequirement("REST & GraphQL APIs", "Backend", 4, 1.2f),
            BenchmarkRequirement("Docker & Containers", "DevOps & Cloud", 3, 1.1f),
            BenchmarkRequirement("System Design & Architecture", "Architecture", 3, 1.4f),
            BenchmarkRequirement("CI/CD Pipelines", "DevOps & Cloud", 3, 1.0f)
        ),
        "Frontend Engineer" to listOf(
            BenchmarkRequirement("TypeScript", "Programming Languages", 5, 1.6f),
            BenchmarkRequirement("React / Jetpack Compose", "Frontend", 5, 1.8f),
            BenchmarkRequirement("Next.js & SSR", "Frontend", 4, 1.4f),
            BenchmarkRequirement("Tailwind & Modern CSS", "Frontend", 4, 1.3f),
            BenchmarkRequirement("Web & App Performance", "Frontend", 4, 1.5f),
            BenchmarkRequirement("Automated UI Testing", "Testing", 3, 1.2f),
            BenchmarkRequirement("State Management (Redux/Flow)", "Frontend", 4, 1.4f)
        ),
        "Backend Engineer" to listOf(
            BenchmarkRequirement("Kotlin / Java / Go", "Programming Languages", 4, 1.5f),
            BenchmarkRequirement("Distributed Systems & Microservices", "Backend", 4, 1.6f),
            BenchmarkRequirement("PostgreSQL & Index Tuning", "Databases", 5, 1.7f),
            BenchmarkRequirement("Redis Caching & PubSub", "Databases", 4, 1.4f),
            BenchmarkRequirement("High-Throughput Concurrency", "Backend", 4, 1.6f),
            BenchmarkRequirement("Kubernetes & Docker", "DevOps & Cloud", 4, 1.3f),
            BenchmarkRequirement("System Design & Sharding", "Architecture", 4, 1.7f)
        ),
        "AI / Machine Learning Engineer" to listOf(
            BenchmarkRequirement("Python & PyTorch", "Programming Languages", 5, 1.8f),
            BenchmarkRequirement("LLM Prompting & Function Calling", "AI & ML", 4, 1.7f),
            BenchmarkRequirement("RAG & Vector Embeddings", "AI & ML", 4, 1.6f),
            BenchmarkRequirement("Model Fine-Tuning & Evaluation", "AI & ML", 3, 1.5f),
            BenchmarkRequirement("FastAPI & Model Serving", "Backend", 4, 1.3f),
            BenchmarkRequirement("Data Pipelines & Feature Stores", "Data", 3, 1.2f)
        ),
        "Mobile Engineer (Android / Multiplatform)" to listOf(
            BenchmarkRequirement("Kotlin & Coroutines/Flow", "Mobile", 5, 1.8f),
            BenchmarkRequirement("Jetpack Compose & M3", "Mobile", 5, 1.7f),
            BenchmarkRequirement("Room & Local SQLite Persistence", "Mobile", 4, 1.4f),
            BenchmarkRequirement("Android Architecture (MVVM/MVI)", "Mobile", 5, 1.6f),
            BenchmarkRequirement("Performance Profiling & Memory Leak Audit", "Mobile", 4, 1.5f),
            BenchmarkRequirement("Gradle Build Automation & KSP", "DevOps & Cloud", 3, 1.2f)
        ),
        "DevOps / Cloud Architect" to listOf(
            BenchmarkRequirement("Terraform / IaC", "DevOps & Cloud", 5, 1.8f),
            BenchmarkRequirement("Kubernetes & Container Orchestration", "DevOps & Cloud", 5, 1.8f),
            BenchmarkRequirement("AWS / GCP Cloud Architecture", "DevOps & Cloud", 5, 1.7f),
            BenchmarkRequirement("Observability (Prometheus/Grafana)", "DevOps & Cloud", 4, 1.4f),
            BenchmarkRequirement("Network Security & Zero Trust", "Security", 4, 1.5f)
        )
    )

    val INTERVIEW_QUESTIONS = listOf(
        InterviewQuestionTemplate(
            questionText = "How do you optimize a database query that is causing high latency in a production microservice under high concurrent load?",
            category = "Database Performance & Optimization",
            difficulty = "Senior",
            rubric = "Candidate must mention EXPLAIN/ANALYZE query plans, indexing strategy (composite/covering indexes), connection pool sizing, caching layers (Redis/Memcached), and read-replica offloading.",
            keywords = listOf("explain", "index", "cache", "redis", "query plan", "replica", "connection pool", "latency", "n+1", "sharding")
        ),
        InterviewQuestionTemplate(
            questionText = "Explain how you would design an idempotent payment processing endpoint to guarantee that network timeouts do not trigger duplicate charges.",
            category = "Distributed Systems & System Design",
            difficulty = "Senior",
            rubric = "Candidate should explain unique idempotency keys stored in an atomic cache or transactional table, distributed locks, database transactions, retry handling with exponential backoff, and webhook reconciliation.",
            keywords = listOf("idempotency key", "unique key", "atomic", "transaction", "distributed lock", "retry", "webhook", "exponential backoff", "duplicate")
        ),
        InterviewQuestionTemplate(
            questionText = "What architectural patterns do you employ in modern UI applications (like Jetpack Compose or React) to separate business logic from rendering and avoid state drift?",
            category = "Frontend & UI Architecture",
            difficulty = "Intermediate",
            rubric = "Candidate should discuss unidirectional data flow (UDF), ViewModel/StateFlow encapsulation, pure composables/components, declarative state binding, and immutability.",
            keywords = listOf("unidirectional", "udf", "viewmodel", "stateflow", "immutable", "recomposition", "side effect", "separation of concerns", "clean architecture")
        ),
        InterviewQuestionTemplate(
            questionText = "Describe your approach to implementing a robust Retrieval-Augmented Generation (RAG) pipeline with semantic vector search and low latency.",
            category = "AI Engineering & LLMs",
            difficulty = "Senior",
            rubric = "Candidate must mention document chunking strategies, embedding generation, vector database indexing (HNSW/IVF), hybrid search with reranking, context window management, and hallucination guardrails.",
            keywords = listOf("chunking", "embedding", "vector db", "similarity", "cosine", "hnsw", "rerank", "context window", "hallucination", "guardrails")
        )
    )

    val INITIAL_LEARNING_RESOURCES = listOf(
        Pair("Mastering Distributed Systems & Consistency Patterns", "Martin Kleppmann / DDIA"),
        Pair("High-Performance Jetpack Compose & State Hoisting", "Android Developer Guides"),
        Pair("Database Indexing & Query Plan Deep-Dive", "Use The Index, Luke"),
        Pair("Production RAG Pipelines: Chunking, Vectors & Reranking", "DeepLearning.AI"),
        Pair("System Design for Microservices & Event-Driven Architecture", "System Design Primer"),
        Pair("Docker & Kubernetes Production Cluster Security", "Cloud Native Computing Foundation")
    )

    val INITIAL_JOB_APPLICATIONS = listOf(
        com.example.careerpilot.data.model.JobApplication(
            id = "app_1",
            company = "Stripe",
            roleTitle = "Senior Infrastructure & Backend Engineer",
            stage = "TECHNICAL",
            location = "San Francisco, CA (Hybrid)",
            salaryOffered = "$175,000 - $210,000 + Equity",
            notes = "Completed recruiter phone screen. Live architecture round scheduled.",
            interviewDate = "Tuesday, 2:00 PM PST",
            matchScore = 92
        ),
        com.example.careerpilot.data.model.JobApplication(
            id = "app_2",
            company = "Airbnb",
            roleTitle = "Senior Android / Mobile Architect",
            stage = "SCREENING",
            location = "Remote (US)",
            salaryOffered = "$165,000 - $195,000 + Equity",
            notes = "Recruiter screening on Jetpack Compose and offline sync.",
            interviewDate = "Thursday, 10:30 AM PST",
            matchScore = 88
        ),
        com.example.careerpilot.data.model.JobApplication(
            id = "app_3",
            company = "Anthropic",
            roleTitle = "Applied AI & Agent Systems Engineer",
            stage = "WISHLIST",
            location = "San Francisco, CA",
            salaryOffered = "$180,000 - $230,000 + Equity",
            notes = "Resume tailored with X-Y-Z formula. Ready to submit with employee referral.",
            interviewDate = "Not Scheduled",
            matchScore = 85
        )
    )

    val INITIAL_CODING_CHALLENGES = listOf(
        com.example.careerpilot.data.model.CodingChallenge(
            id = "code_1",
            title = "Distributed In-Memory LRU Cache with TTL",
            category = "Concurrency",
            difficulty = "Medium",
            problemStatement = "Implement a thread-safe LRU (Least Recently Used) cache with key expiration (TTL) in Kotlin. Ensure O(1) get() and put() time complexity using a HashMap and doubly linked list with Mutex synchronization.",
            starterCode = """class LRUCache<K, V>(private val capacity: Int) {
    private val map = mutableMapOf<K, Node<K, V>>()
    // TODO: Implement doubly linked list and thread-safe lock
    
    suspend fun get(key: K): V? {
        return map[key]?.value
    }
    
    suspend fun put(key: K, value: V, ttlMs: Long = 60000L) {
        // TODO: Evict oldest if capacity exceeded
    }
}""",
            solutionReference = "Use java.util.concurrent.ConcurrentHashMap combined with custom DoublyLinkedList and Kotlin Mutex locks.",
            timeComplexityTarget = "O(1) Get / Put",
            spaceComplexityTarget = "O(Capacity)",
            isCompleted = false
        ),
        com.example.careerpilot.data.model.CodingChallenge(
            id = "code_2",
            title = "Rate Limiter (Token Bucket Algorithm)",
            category = "System Design",
            difficulty = "Medium",
            problemStatement = "Design an API Rate Limiter that allows a client up to N requests per window using the Token Bucket algorithm with millisecond refill resolution.",
            starterCode = """class TokenBucketRateLimiter(
    private val maxTokens: Long,
    private val refillRatePerSecond: Double
) {
    private var availableTokens = maxTokens.toDouble()
    private var lastRefillTimestamp = System.currentTimeMillis()

    @Synchronized
    fun allowRequest(tokens: Long = 1): Boolean {
        // TODO: Refill based on elapsed time and decrement
        return true
    }
}""",
            solutionReference = "Calculate elapsed time since last request: tokensToAdd = elapsed * rate. Refill min(maxTokens, current + tokensToAdd).",
            timeComplexityTarget = "O(1)",
            spaceComplexityTarget = "O(1)",
            isCompleted = true
        ),
        com.example.careerpilot.data.model.CodingChallenge(
            id = "code_3",
            title = "CRDT Conflict-Free Replicated State Engine",
            category = "Architecture",
            difficulty = "Hard",
            problemStatement = "Implement a state-based Observed-Remove Set (OR-Set) or Last-Write-Wins Register (LWW-Register) for collaborative real-time sync without central coordinator locks.",
            starterCode = """data class LWWRegister<T>(
    val value: T,
    val timestamp: Long,
    val peerId: String
) {
    fun merge(incoming: LWWRegister<T>): LWWRegister<T> {
        // TODO: Deterministic merge based on timestamp and peer tie-breaking
        return if (incoming.timestamp > this.timestamp) incoming else this
    }
}""",
            solutionReference = "Enforce commutative and associative merge operators with Lamport clocks or monotonically increasing timestamps.",
            timeComplexityTarget = "O(1) Merge",
            spaceComplexityTarget = "O(N) State Size",
            isCompleted = false
        )
    )

    val INITIAL_PEER_MATCHES = listOf(
        com.example.careerpilot.data.model.PeerMatch(
            id = "peer_1",
            peerName = "Sarah Lin",
            peerHeadline = "Staff Engineer @ Snowflake",
            targetRole = "Principal Distributed Systems Architect",
            companyTarget = "Google / Snowflake",
            timezone = "PST (UTC-8)",
            experienceLevel = "7+ Years",
            rating = 4.96f,
            sessionsCompleted = 34,
            skillsSpecialty = listOf("System Design", "Distributed Systems", "Database Internals"),
            availabilityStatus = "Available Today at 4 PM"
        ),
        com.example.careerpilot.data.model.PeerMatch(
            id = "peer_2",
            peerName = "David Kim",
            peerHeadline = "Senior Mobile Engineer @ Square",
            targetRole = "Lead Mobile Architect",
            companyTarget = "Stripe / Block / Uber",
            timezone = "EST (UTC-5)",
            experienceLevel = "5 Years",
            rating = 4.92f,
            sessionsCompleted = 21,
            skillsSpecialty = listOf("Jetpack Compose", "Android Concurrency", "Offline-First Sync"),
            availabilityStatus = "Available Tomorrow"
        ),
        com.example.careerpilot.data.model.PeerMatch(
            id = "peer_3",
            peerName = "Marcus Vance",
            peerHeadline = "AI Infrastructure Specialist",
            targetRole = "Staff AI Systems Engineer",
            companyTarget = "Anthropic / OpenAI / Meta",
            timezone = "PST (UTC-8)",
            experienceLevel = "6 Years",
            rating = 4.98f,
            sessionsCompleted = 48,
            skillsSpecialty = listOf("LLM Infrastructure", "RAG Optimization", "High-Throughput Serving"),
            availabilityStatus = "Available Today at 6 PM"
        )
    )

    val INITIAL_SKILL_SPRINTS = listOf(
        com.example.careerpilot.data.model.SkillSprint(
            id = "sprint_1",
            sprintTitle = "Distributed Systems & In-Memory Sharding Sprint",
            targetSkill = "System Design & Concurrency",
            description = "Build a multi-node distributed key-value store with consistent hashing, heartbeat health checks, and replicate data across partitions.",
            durationDays = 7,
            currentDay = 4,
            milestoneTasks = listOf(
                "Implement Murmur3 Consistent Hash Ring with virtual nodes (Completed)",
                "Build gRPC inter-node sync service with proto definitions (Completed)",
                "Add Raft consensus leader election simulation (In Progress)",
                "Publish verified GitHub repo proof and load test benchmark"
            ),
            completedMilestones = 2,
            badgeName = "🏆 Distributed Systems Architect Badge",
            rewardXp = 500,
            isClaimed = false
        ),
        com.example.careerpilot.data.model.SkillSprint(
            id = "sprint_2",
            sprintTitle = "7-Day High-Performance Jetpack Compose Sprint",
            targetSkill = "Android & Compose Canvas",
            description = "Master custom layout modifiers, subcomposition, 120 FPS canvas charts, and zero-recomposition state hoisting.",
            durationDays = 7,
            currentDay = 7,
            milestoneTasks = listOf(
                "Create smooth bezier cubic curve animated sparkline charts (Completed)",
                "Audit app layout passes with Android Studio Layout Inspector (Completed)",
                "Implement custom drag-to-dismiss bottom sheet with spring physics (Completed)",
                "Push complete open-source Compose component library to GitHub (Completed)"
            ),
            completedMilestones = 4,
            badgeName = "⚡ Jetpack Compose UI Master",
            rewardXp = 450,
            isClaimed = true
        )
    )
}

