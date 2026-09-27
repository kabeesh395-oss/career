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

    val INITIAL_JOB_APPLICATIONS = emptyList<com.example.careerpilot.data.model.JobApplication>()

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
            isCompleted = false
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
            peerHeadline = "Staff Engineer @ Distributed Systems",
            targetRole = "Principal Distributed Systems Architect",
            companyTarget = "Cloud Scale Systems",
            timezone = "PST (UTC-8)",
            experienceLevel = "7+ Years",
            rating = 4.96f,
            sessionsCompleted = 34,
            skillsSpecialty = listOf("System Design", "Distributed Systems", "Database Internals"),
            availabilityStatus = "Available for booking"
        ),
        com.example.careerpilot.data.model.PeerMatch(
            id = "peer_2",
            peerName = "David Kim",
            peerHeadline = "Senior Mobile Engineer @ Architecture",
            targetRole = "Lead Mobile Architect",
            companyTarget = "Mobile Platforms",
            timezone = "EST (UTC-5)",
            experienceLevel = "5 Years",
            rating = 4.92f,
            sessionsCompleted = 21,
            skillsSpecialty = listOf("Jetpack Compose", "Android Concurrency", "Offline-First Sync"),
            availabilityStatus = "Available for booking"
        ),
        com.example.careerpilot.data.model.PeerMatch(
            id = "peer_3",
            peerName = "Marcus Vance",
            peerHeadline = "AI Infrastructure Specialist",
            targetRole = "Staff AI Systems Engineer",
            companyTarget = "AI Research & Platform",
            timezone = "PST (UTC-8)",
            experienceLevel = "6 Years",
            rating = 4.98f,
            sessionsCompleted = 48,
            skillsSpecialty = listOf("LLM Infrastructure", "RAG Optimization", "High-Throughput Serving"),
            availabilityStatus = "Available for booking"
        )
    )

    val INITIAL_SKILL_SPRINTS = listOf(
        com.example.careerpilot.data.model.SkillSprint(
            id = "sprint_1",
            sprintTitle = "Distributed Systems & In-Memory Sharding Sprint",
            targetSkill = "System Design & Concurrency",
            description = "Build a multi-node distributed key-value store with consistent hashing, heartbeat health checks, and replicate data across partitions.",
            durationDays = 7,
            currentDay = 1,
            milestoneTasks = listOf(
                "Implement Murmur3 Consistent Hash Ring with virtual nodes",
                "Build gRPC inter-node sync service with proto definitions",
                "Add Raft consensus leader election simulation",
                "Publish verified GitHub repo proof and load test benchmark"
            ),
            completedMilestones = 0,
            badgeName = "Distributed Systems Architect Certification",
            rewardXp = 500,
            isClaimed = false
        ),
        com.example.careerpilot.data.model.SkillSprint(
            id = "sprint_2",
            sprintTitle = "7-Day High-Performance Jetpack Compose Sprint",
            targetSkill = "Android & Compose Canvas",
            description = "Master custom layout modifiers, subcomposition, 120 FPS canvas charts, and zero-recomposition state hoisting.",
            durationDays = 7,
            currentDay = 1,
            milestoneTasks = listOf(
                "Create smooth bezier cubic curve animated sparkline charts",
                "Audit app layout passes with Android Studio Layout Inspector",
                "Implement custom drag-to-dismiss bottom sheet with spring physics",
                "Push complete open-source Compose component library to GitHub"
            ),
            completedMilestones = 0,
            badgeName = "Jetpack Compose Architecture Specialization",
            rewardXp = 450,
            isClaimed = false
        )
    )

    val INITIAL_OPPORTUNITIES = listOf(
        // === CERTIFICATIONS ===
        com.example.careerpilot.data.model.CareerOpportunity(
            id = "cert_aws_saa",
            title = "AWS Certified Solutions Architect – Associate (SAA-C03)",
            category = "CERTIFICATION",
            providerOrHost = "Amazon Web Services (AWS)",
            description = "The industry-standard benchmark for cloud architects. Validates expertise in designing highly available, cost-efficient, fault-tolerant, and scalable distributed systems on AWS.",
            officialUrl = "https://aws.amazon.com/certification/certified-solutions-architect-associate/",
            registrationUrl = "https://www.aws.training/certification",
            syllabusOrDocsUrl = "https://d1.awsstatic.com/training-and-certification/docs-sa-assoc/AWS-Certified-Solutions-Architect-Associate_Exam-Guide.pdf",
            difficulty = "Intermediate",
            mode = "Online Proctored / Testing Center",
            deadlineOrSchedule = "On-Demand (Schedule anytime)",
            costOrPrize = "$150 USD (50% voucher available on AWS Cloud Quest)",
            skillsTargeted = listOf("AWS", "Cloud Architecture", "S3", "EC2", "VPC", "IAM", "CloudFormation", "High Availability"),
            targetRoles = listOf("Full Stack Engineer", "Backend Engineer", "DevOps / Cloud Architect"),
            prerequisites = "1 year hands-on experience designing available, cost-effective, fault-tolerant distributed systems on AWS.",
            careerRoiSummary = "+18% average salary boost ($135k-$165k avg); mandatory requirement in ~35% of senior cloud architecture postings.",
            keyDomains = listOf("Design Resilient Architectures (26%)", "Design High-Performing Architectures (24%)", "Design Secure Applications (30%)", "Design Cost-Optimized Architectures (20%)"),
            prepTimeWeeks = 6,
            isFeatured = true,
            matchScore = 95
        ),
        com.example.careerpilot.data.model.CareerOpportunity(
            id = "cert_gcp_ace",
            title = "Google Cloud Associate Cloud Engineer (ACE)",
            category = "CERTIFICATION",
            providerOrHost = "Google Cloud",
            description = "Validates the ability to deploy applications, monitor operations, and manage enterprise enterprise infrastructure on Google Cloud Platform using Google Cloud Console and CLI.",
            officialUrl = "https://cloud.google.com/learn/certification/cloud-engineer",
            registrationUrl = "https://www.webassessor.com/googlecloud",
            syllabusOrDocsUrl = "https://cloud.google.com/learn/certification/guides/cloud-engineer",
            difficulty = "Intermediate",
            mode = "Online Proctored / Testing Center",
            deadlineOrSchedule = "On-Demand (Year-Round)",
            costOrPrize = "$125 USD (Includes Google Cloud Skills Boost credits)",
            skillsTargeted = listOf("Google Cloud Platform", "GKE", "Compute Engine", "Cloud Storage", "BigQuery", "IAM", "Cloud Monitoring"),
            targetRoles = listOf("DevOps / Cloud Architect", "Backend Engineer", "Full Stack Engineer"),
            prerequisites = "6+ months hands-on experience with Google Cloud console and gcloud CLI commands.",
            careerRoiSummary = "Top-ranked cloud certification for engineering credibility; recognized across Fortune 500 tech teams.",
            keyDomains = listOf("Setting up a cloud solution environment (17.5%)", "Planning and configuring a cloud solution (17.5%)", "Deploying and implementing a cloud solution (25%)", "Ensuring successful operation of a cloud solution (20%)", "Configuring access and security (20%)"),
            prepTimeWeeks = 5,
            isFeatured = false,
            matchScore = 91
        ),
        com.example.careerpilot.data.model.CareerOpportunity(
            id = "cert_cncf_ckad",
            title = "Certified Kubernetes Application Developer (CKAD)",
            category = "CERTIFICATION",
            providerOrHost = "CNCF & Linux Foundation",
            description = "100% hands-on performance-based exam where candidates solve real-world container orchestration challenges in a live Linux terminal environment.",
            officialUrl = "https://www.cncf.io/training/certification/ckad/",
            registrationUrl = "https://training.linuxfoundation.org/certification/certified-kubernetes-application-developer-ckad/",
            syllabusOrDocsUrl = "https://github.com/cncf/curriculum/blob/master/CKAD_Curriculum.pdf",
            difficulty = "Advanced",
            mode = "Live Performance Terminal Lab",
            deadlineOrSchedule = "On-Demand (Valid for 12 months)",
            costOrPrize = "$395 USD (Includes 2 exam attempts & killer.sh simulator)",
            skillsTargeted = listOf("Kubernetes", "Docker", "Pod Design", "Deployments", "ConfigMaps & Secrets", "Services & Ingress", "Observability"),
            targetRoles = listOf("Backend Engineer", "DevOps / Cloud Architect", "Full Stack Engineer"),
            prerequisites = "Solid understanding of container runtimes, Linux bash commands, and building microservices.",
            careerRoiSummary = "The highest-credibility practical performance exam for DevOps and cloud backend roles worldwide.",
            keyDomains = listOf("Application Design and Build (20%)", "Application Deployment (20%)", "Application Observability and Maintenance (15%)", "Application Environment, Configuration and Security (25%)", "Services and Networking (20%)"),
            prepTimeWeeks = 8,
            isFeatured = true,
            matchScore = 94
        ),
        com.example.careerpilot.data.model.CareerOpportunity(
            id = "cert_meta_frontend",
            title = "Meta Front-End Developer Professional Certificate",
            category = "CERTIFICATION",
            providerOrHost = "Meta & Coursera",
            description = "Comprehensive credential developed directly by Meta engineers. Covers React architecture, modern JavaScript, UI/UX implementation, automated testing with Jest, and end-to-end portfolio projects.",
            officialUrl = "https://www.coursera.org/professional-certificates/meta-front-end-developer",
            registrationUrl = "https://www.coursera.org/professional-certificates/meta-front-end-developer",
            syllabusOrDocsUrl = "https://www.coursera.org/professional-certificates/meta-front-end-developer#courses",
            difficulty = "Beginner / Intermediate",
            mode = "100% Online Self-Paced",
            deadlineOrSchedule = "Enrollment Open Anytime",
            costOrPrize = "Financial Aid Available (or $49/mo Coursera Plus)",
            skillsTargeted = listOf("React", "JavaScript (ES6+)", "HTML5 & Modern CSS", "UI/UX Principles", "Jest Testing", "Git"),
            targetRoles = listOf("Frontend Engineer", "Full Stack Engineer"),
            prerequisites = "No prior experience required; foundational logical reasoning.",
            careerRoiSummary = "Grants direct exclusive access to the Meta Career Programs Job Board with 200+ hiring employer partners.",
            keyDomains = listOf("Introduction to Front-End Development", "Programming with JavaScript", "Version Control with Git", "HTML and CSS in depth", "React Basics & Advanced React", "Front-End Developer Capstone Project"),
            prepTimeWeeks = 6,
            isFeatured = false,
            matchScore = 89
        ),
        com.example.careerpilot.data.model.CareerOpportunity(
            id = "cert_terraform_associate",
            title = "HashiCorp Certified: Terraform Associate (003)",
            category = "CERTIFICATION",
            providerOrHost = "HashiCorp",
            description = "Validates foundational Infrastructure-as-Code (IaC) principles, writing declarative HCL templates, managing remote state backends, and orchestrating multi-cloud provisioning.",
            officialUrl = "https://www.hashicorp.com/certification/terraform-associate",
            registrationUrl = "https://www.hashicorp.com/certification/terraform-associate",
            syllabusOrDocsUrl = "https://developer.hashicorp.com/terraform/tutorials/certification-003/associate-review-003",
            difficulty = "Intermediate",
            mode = "Online Proctored",
            deadlineOrSchedule = "On-Demand",
            costOrPrize = "$70.50 USD",
            skillsTargeted = listOf("Terraform", "Infrastructure as Code", "HCL", "Terraform Cloud", "State Management", "Modules"),
            targetRoles = listOf("DevOps / Cloud Architect", "Backend Engineer"),
            prerequisites = "Basic terminal knowledge and cloud concepts (AWS/Azure/GCP).",
            careerRoiSummary = "The fastest growing Infrastructure-as-Code credential in the industry; proven resume differentiator.",
            keyDomains = listOf("Understand Infrastructure as Code concepts", "Understand Terraform's purpose", "Understand Terraform basics & CLI", "Navigate Terraform workflow", "Implement and maintain state", "Read, generate, and modify configuration"),
            prepTimeWeeks = 3,
            isFeatured = false,
            matchScore = 88
        ),

        // === HACKATHONS ===
        com.example.careerpilot.data.model.CareerOpportunity(
            id = "hack_gemini_challenge",
            title = "Google Gemini API Global Developer Challenge",
            category = "HACKATHON",
            providerOrHost = "Google & Devpost",
            description = "Build groundbreaking AI applications utilizing the latest Gemini 1.5 Pro / Flash multimodal capabilities, function calling, structured JSON output, and context caching.",
            officialUrl = "https://ai.google.dev/competition",
            registrationUrl = "https://ai.google.dev/competition",
            syllabusOrDocsUrl = "https://ai.google.dev/gemini-api/docs",
            difficulty = "All Levels",
            mode = "Global / Virtual",
            deadlineOrSchedule = "Upcoming Fall Cohort (Submissions Close Nov 30)",
            costOrPrize = "$1,000,000+ in Cash Prizes & Custom DeLorean",
            skillsTargeted = listOf("Gemini API", "Multimodal AI", "Android / Web", "Function Calling", "Prompt Engineering", "Python / Kotlin"),
            targetRoles = listOf("AI / Machine Learning Engineer", "Full Stack Engineer", "Mobile Engineer (Android / Multiplatform)"),
            prerequisites = "Free Gemini API key from Google AI Studio.",
            careerRoiSummary = "Direct exposure to Google DeepMind leadership, global press spotlight, and seed funding opportunities.",
            keyDomains = listOf("Multimodal AI Capabilities", "Useful Real-World Impact", "Technical Execution & Polish", "Creative Integration of Audio/Video/Vision"),
            prepTimeWeeks = 3,
            isFeatured = true,
            matchScore = 98
        ),
        com.example.careerpilot.data.model.CareerOpportunity(
            id = "hack_ethglobal_2026",
            title = "ETHGlobal Online Global Hackathon",
            category = "HACKATHON",
            providerOrHost = "ETHGlobal",
            description = "Premier virtual builder sprint connecting top engineers with leading web3 protocols. Build decentralized applications, zero-knowledge tooling, or consumer onboarding systems.",
            officialUrl = "https://ethglobal.com",
            registrationUrl = "https://ethglobal.com/events",
            syllabusOrDocsUrl = "https://ethglobal.com/guides",
            difficulty = "Intermediate",
            mode = "Virtual (3-day sprint)",
            deadlineOrSchedule = "Monthly Cycles",
            costOrPrize = "$300,000+ Pool in Sponsor Bounties & Grants",
            skillsTargeted = listOf("Solidity", "Smart Contracts", "Web3", "Next.js", "Cryptography", "EVM"),
            targetRoles = listOf("Full Stack Engineer", "Backend Engineer"),
            prerequisites = "Basic web development or smart contract knowledge.",
            careerRoiSummary = "Direct venture capital scouts, top Web3 startup hiring, and instant ecosystem grants.",
            keyDomains = listOf("DeFi Infrastructure", "Consumer Crypto UX", "Zero-Knowledge Proofs", "Developer Tooling"),
            prepTimeWeeks = 2,
            isFeatured = false,
            matchScore = 85
        ),
        com.example.careerpilot.data.model.CareerOpportunity(
            id = "hack_mlh_global",
            title = "MLH Global Hack Week & Seasonal League",
            category = "HACKATHON",
            providerOrHost = "Major League Hacking (MLH)",
            description = "Week-long celebration of learning, hacking, and building alongside 10,000+ passionate developers worldwide. Daily mini-challenges, live workshops, and mentor support.",
            officialUrl = "https://globalhackweek.mlh.io",
            registrationUrl = "https://globalhackweek.mlh.io/register",
            syllabusOrDocsUrl = "https://mlh.io/hackathons",
            difficulty = "Beginner / Intermediate",
            mode = "Hybrid & Virtual Discord Hubs",
            deadlineOrSchedule = "Weekly & Monthly Seasons",
            costOrPrize = "100% Free • Swag Bags, Hardware Kits & Sponsor Interviews",
            skillsTargeted = listOf("Full Stack", "Mobile Apps", "APIs", "Cloud Deployment", "Git", "Teamwork"),
            targetRoles = listOf("Full Stack Engineer", "Frontend Engineer", "Mobile Engineer (Android / Multiplatform)"),
            prerequisites = "Open to students, career changers, and professional builders.",
            careerRoiSummary = "Build 3-5 verified portfolio projects in a week; high-conversion pipeline to tech company sponsors.",
            keyDomains = listOf("Beginner Track", "AI & Machine Learning Track", "Cloud & Web Track", "Game & Mobile Track"),
            prepTimeWeeks = 1,
            isFeatured = false,
            matchScore = 90
        ),
        com.example.careerpilot.data.model.CareerOpportunity(
            id = "hack_kaggle_grand",
            title = "Kaggle Community & Industry AI Grand Competitions",
            category = "HACKATHON",
            providerOrHost = "Kaggle / Google",
            description = "Solve competitive machine learning problems benchmarked against ground truth test sets. Feature engineering, gradient boosted trees, PyTorch deep neural networks, and LLM fine-tuning.",
            officialUrl = "https://www.kaggle.com/competitions",
            registrationUrl = "https://www.kaggle.com/competitions",
            syllabusOrDocsUrl = "https://www.kaggle.com/docs/competitions",
            difficulty = "Advanced",
            mode = "Online Asynchronous",
            deadlineOrSchedule = "Ongoing (2-3 months submission window)",
            costOrPrize = "$50,000 - $150,000 Cash Prizes per contest",
            skillsTargeted = listOf("Python", "PyTorch", "Data Science", "Feature Engineering", "Ensemble Models", "EDA"),
            targetRoles = listOf("AI / Machine Learning Engineer"),
            prerequisites = "Python programming and foundational machine learning algorithms.",
            careerRoiSummary = "Kaggle Grandmaster/Master badges are recognized as premier hiring signals by Google, Meta, and OpenAI.",
            keyDomains = listOf("Model Architecture", "Cross-Validation Strategy", "Feature Pipeline Optimization", "Inference Speed Benchmarking"),
            prepTimeWeeks = 4,
            isFeatured = false,
            matchScore = 93
        ),

        // === FELLOWSHIPS & OPEN SOURCE ===
        com.example.careerpilot.data.model.CareerOpportunity(
            id = "opp_gsoc_2026",
            title = "Google Summer of Code (GSoC)",
            category = "FELLOWSHIP",
            providerOrHost = "Google Open Source",
            description = "A global, online program focused on bringing new contributors into open source software development. Work with an open source organization on a 12+ week programming project under experienced mentorship.",
            officialUrl = "https://summerofcode.withgoogle.com",
            registrationUrl = "https://summerofcode.withgoogle.com/get-started",
            syllabusOrDocsUrl = "https://google.github.io/gsocguides/student/",
            difficulty = "Intermediate / Advanced",
            mode = "Remote (12 to 22 Weeks)",
            deadlineOrSchedule = "Annual Applications Open Feb/March",
            costOrPrize = "$1,500 - $6,000 USD Stipend (Location-based adjusted)",
            skillsTargeted = listOf("Open Source", "Git & GitHub", "C++", "Python", "Rust", "Kotlin", "Go", "Architecture"),
            targetRoles = listOf("Full Stack Engineer", "Backend Engineer", "Mobile Engineer (Android / Multiplatform)", "AI / Machine Learning Engineer"),
            prerequisites = "18+ years old, ability to write structured technical proposal for participating open source organization.",
            careerRoiSummary = "The gold standard open-source credential; alumni frequently receive direct referral fast-tracks to Google and top tech.",
            keyDomains = listOf("Proposal Writing & Scoping", "Code Review & PR Standards", "Unit & Integration Testing", "Documentation & Community Engagement"),
            prepTimeWeeks = 8,
            isFeatured = true,
            matchScore = 97
        ),
        com.example.careerpilot.data.model.CareerOpportunity(
            id = "opp_mlh_fellowship",
            title = "MLH Fellowship (Software Engineering & Open Source)",
            category = "FELLOWSHIP",
            providerOrHost = "Major League Hacking, Meta, GitHub & AWS",
            description = "A 12-week remote internship alternative where fellows collaborate on production open-source software used by millions worldwide. Paired with professional mentors and a supportive peer pod.",
            officialUrl = "https://fellowship.mlh.io",
            registrationUrl = "https://fellowship.mlh.io/programs/software-engineering",
            syllabusOrDocsUrl = "https://fellowship.mlh.io/faq",
            difficulty = "Intermediate",
            mode = "Remote (12-Week Intensive)",
            deadlineOrSchedule = "Spring, Summer, and Fall Cohorts (Rolling)",
            costOrPrize = "$1,000 - $5,000 USD Need-Based Educational Grant",
            skillsTargeted = listOf("Production CI/CD", "Code Quality", "Full Stack", "Open Source", "Agile Sprints", "System Architecture"),
            targetRoles = listOf("Full Stack Engineer", "Frontend Engineer", "Backend Engineer"),
            prerequisites = "Proficiency in at least one programming language and git workflow.",
            careerRoiSummary = "Work directly on production repositories like React, Jest, Babel, and PyTorch under senior mentor guidance.",
            keyDomains = listOf("Software Engineering Track", "Open Source Track", "Site Reliability Engineering Track", "Web3 Track"),
            prepTimeWeeks = 4,
            isFeatured = true,
            matchScore = 96
        ),
        com.example.careerpilot.data.model.CareerOpportunity(
            id = "opp_lfx_mentorship",
            title = "Linux Foundation (LFX) Mentorship Program",
            category = "OPEN_SOURCE",
            providerOrHost = "The Linux Foundation & CNCF",
            description = "Paid mentorship program that provides opportunities to contribute to open source projects hosted by The Linux Foundation (such as Kubernetes, Hyperledger, Envoy, Prometheus).",
            officialUrl = "https://lfx.linuxfoundation.org/tools/mentorship/",
            registrationUrl = "https://mentorship.lfx.linuxfoundation.org/",
            syllabusOrDocsUrl = "https://docs.linuxfoundation.org/lfx/mentorship",
            difficulty = "Advanced",
            mode = "Remote (3-Month Full-Time / Part-Time Terms)",
            deadlineOrSchedule = "Term 1 (Mar-May), Term 2 (Jun-Aug), Term 3 (Sep-Nov)",
            costOrPrize = "$3,000 - $6,600 USD Term Stipend",
            skillsTargeted = listOf("Kubernetes", "Linux Kernel", "Hyperledger", "Prometheus", "Envoy", "Go", "Rust", "C"),
            targetRoles = listOf("Backend Engineer", "DevOps / Cloud Architect"),
            prerequisites = "Demonstrated commits/PRs in open source projects and strong systems programming fundamentals.",
            careerRoiSummary = "Direct pipeline to becoming an official CNCF maintainer or landing cloud infrastructure roles.",
            keyDomains = listOf("Cloud Native Architecture", "Linux Kernel Optimization", "Security & Supply Chain Security", "Distributed Storage & Mesh"),
            prepTimeWeeks = 6,
            isFeatured = false,
            matchScore = 92
        ),
        com.example.careerpilot.data.model.CareerOpportunity(
            id = "opp_outreachy",
            title = "Outreachy Open Source Internship",
            category = "OPEN_SOURCE",
            providerOrHost = "Software Freedom Conservancy",
            description = "Provides three-month remote internships for people subject to systemic bias and impacted by underrepresentation in the technical industry. Work on real open source tools with dedicated 1-on-1 mentors.",
            officialUrl = "https://www.outreachy.org",
            registrationUrl = "https://www.outreachy.org/apply/",
            syllabusOrDocsUrl = "https://www.outreachy.org/docs/applicant/",
            difficulty = "Intermediate",
            mode = "Remote (3 Months)",
            deadlineOrSchedule = "May & December Cohorts",
            costOrPrize = "$7,000 USD Stipend + $500 Travel Grant",
            skillsTargeted = listOf("Open Source", "Full Stack", "Documentation", "Git", "Community Collaboration"),
            targetRoles = listOf("Full Stack Engineer", "Frontend Engineer", "Backend Engineer"),
            prerequisites = "Eligible underrepresented tech groups; completion of initial contribution period.",
            careerRoiSummary = "Exceptional career launchpad with mentorship from Mozilla, Wikimedia, Fedora, GNOME, and Tor.",
            keyDomains = listOf("Initial Contribution Period", "Project Scoping", "Async Collaboration", "Technical Writing"),
            prepTimeWeeks = 4,
            isFeatured = false,
            matchScore = 89
        ),
        com.example.careerpilot.data.model.CareerOpportunity(
            id = "opp_github_octernships",
            title = "GitHub Octernships & Campus Expert Program",
            category = "FELLOWSHIP",
            providerOrHost = "GitHub Education & Partner Startups",
            description = "Connects verified students and early-career developers with paid remote micro-internships at tech startups across the globe, solving real GitHub issues and pull requests.",
            officialUrl = "https://education.github.com/students/octernships",
            registrationUrl = "https://education.github.com/students/octernships",
            syllabusOrDocsUrl = "https://education.github.com/experts",
            difficulty = "Intermediate",
            mode = "Remote (1 to 6 Months)",
            deadlineOrSchedule = "Rolling Applications on GitHub Education Portal",
            costOrPrize = "$500 - $2,000 USD/mo Paid Remote Internship",
            skillsTargeted = listOf("GitHub Actions", "Full Stack", "DevOps", "Modern Web", "API Integration"),
            targetRoles = listOf("Full Stack Engineer", "Frontend Engineer", "DevOps / Cloud Architect"),
            prerequisites = "GitHub Student Developer Pack verified account.",
            careerRoiSummary = "Gain verifiable paid work experience at international tech companies while studying or building.",
            keyDomains = listOf("Application Assessment Task", "Code Review & PR Rigor", "Product Delivery", "Technical Leadership"),
            prepTimeWeeks = 3,
            isFeatured = false,
            matchScore = 91
        ),

        // === HIRING CHALLENGES ===
        com.example.careerpilot.data.model.CareerOpportunity(
            id = "opp_leetcode_biweekly",
            title = "LeetCode Global Weekly & Biweekly Contests",
            category = "HIRING_CHALLENGE",
            providerOrHost = "LeetCode",
            description = "Timed 90-minute algorithmic challenges to test data structures, time complexity optimization, and problem solving speed against 25,000+ global software engineers.",
            officialUrl = "https://leetcode.com/contest/",
            registrationUrl = "https://leetcode.com/contest/",
            syllabusOrDocsUrl = "https://leetcode.com/discuss/general-discussion/",
            difficulty = "All Levels",
            mode = "Online Live (90 Minutes every weekend)",
            deadlineOrSchedule = "Every Saturday 8:00 PM EST & Sunday 10:30 AM EST",
            costOrPrize = "100% Free • LeetCode Coins, Badges, Recruiter Inquiries",
            skillsTargeted = listOf("Algorithms", "Data Structures", "Dynamic Programming", "Graph Theory", "Binary Search", "Trees"),
            targetRoles = listOf("Full Stack Engineer", "Backend Engineer", "AI / Machine Learning Engineer"),
            prerequisites = "Free LeetCode account.",
            careerRoiSummary = "Top contest rankings (Knight/Guardian badge) trigger direct recruiter outbound messages from Google, Citadel, Meta.",
            keyDomains = listOf("Easy Warm-up (10 min)", "Medium Implementation (20 min)", "Medium Logic/Graph (30 min)", "Hard Dynamic Programming (30 min)"),
            prepTimeWeeks = 4,
            isFeatured = false,
            matchScore = 87
        ),
        com.example.careerpilot.data.model.CareerOpportunity(
            id = "opp_yc_startup_school",
            title = "Y Combinator Startup School & Co-Founder Matching",
            category = "HIRING_CHALLENGE",
            providerOrHost = "Y Combinator",
            description = "The premier co-founder matchmaking platform and startup incubator curriculum created by Y Combinator. Pair with vetted technical and product co-founders to build and launch.",
            officialUrl = "https://www.startupschool.org/cofounder-matching",
            registrationUrl = "https://www.startupschool.org/cofounder-matching",
            syllabusOrDocsUrl = "https://www.ycombinator.com/library",
            difficulty = "All Levels",
            mode = "Online Platform",
            deadlineOrSchedule = "Continuous Matching / Bi-annual YC Batches",
            costOrPrize = "Free • Fast track to $500k YC Investment & $100k+ Cloud Deals",
            skillsTargeted = listOf("Product Engineering", "MVP Development", "System Architecture", "0-to-1 Prototyping", "Pitching"),
            targetRoles = listOf("Full Stack Engineer", "Backend Engineer", "Frontend Engineer", "Mobile Engineer (Android / Multiplatform)"),
            prerequisites = "Passion to build innovative tech products and find technical or business co-founders.",
            careerRoiSummary = "Over 100,000 founders matched, resulting in 1,000+ funded tech startups.",
            keyDomains = listOf("Co-founder Profile Creation", "Mutual Candidate Matching", "Trial Working Project", "YC Core Application"),
            prepTimeWeeks = 2,
            isFeatured = false,
            matchScore = 88
        )
    )
}

