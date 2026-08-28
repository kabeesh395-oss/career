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
}
