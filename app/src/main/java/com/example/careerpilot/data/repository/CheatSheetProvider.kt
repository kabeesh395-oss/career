package com.example.careerpilot.data.repository

data class SystemDesignCheatSheet(
    val id: String,
    val title: String,
    val category: String,
    val keyConcept: String,
    val architecturePattern: String,
    val keyTradeoffs: List<String>,
    val interviewTalkingPoints: List<String>,
    val codeSnippetOrFormula: String
)

object CheatSheetProvider {

    val CHEAT_SHEETS = listOf(
        SystemDesignCheatSheet(
            id = "caching_strategies",
            title = "Distributed Caching & Eviction Policies",
            category = "Distributed Systems",
            keyConcept = "Cache-Aside vs. Write-Through vs. Write-Behind. Managing Cache Stampede, Hotkey Thundering Herds, and Eviction Policies (LRU, LFU, ARC).",
            architecturePattern = "Multi-tier Redis cluster with Redis Sentinel / Cluster sharding. Local in-memory L1 cache (Caffeine) backed by L2 distributed Redis cluster with Mutex/SingleFlight lock on cache miss.",
            keyTradeoffs = listOf(
                "Cache-Aside: Simple, resilience to cache failure, but initial cache miss latency and potential stale reads.",
                "Write-Through: Strong consistency, higher write latency on every mutation.",
                "Write-Behind (Write-Back): High write throughput, risk of data loss if cache crashes before async DB flush."
            ),
            interviewTalkingPoints = listOf(
                "Always mention Cache Stampede prevention using Probabilistic Early Eviction (XFetch algorithm) or distributed mutexes.",
                "Discuss Redis cluster memory limits (maxmemory-policy: allkeys-lru) and persistence choices (RDB snapshots vs. AOF append-only).",
                "Explain how to handle hotkeys via local JVM caching + key salting with random replicas."
            ),
            codeSnippetOrFormula = """// Probabilistic Early Refresh (XFetch)
val shouldRefresh = (delta * beta * -ln(random())) >= (expiry - now)"""
        ),

        SystemDesignCheatSheet(
            id = "database_sharding",
            title = "Database Sharding & Consistent Hashing",
            category = "Database Architecture",
            keyConcept = "Horizontal partitioning across nodes using Consistent Hashing with virtual vnodes to minimize key migration during cluster scale-out.",
            architecturePattern = "Virtual Node Hash Ring (e.g., 256 vnodes per physical node using MurmurHash3). Shard router directs read/write queries by tenant ID or user UUID.",
            keyTradeoffs = listOf(
                "Consistent Hashing: Reallocates only K/N keys when adding a new node, but requires complex distributed routing.",
                "Cross-Shard Joins: Extremely expensive; avoid by denormalizing data or co-locating related entity partitions.",
                "Rebalancing: Requires background data migration tasks and dual-writing during shard re-allocation."
            ),
            interviewTalkingPoints = listOf(
                "Emphasize selecting high-cardinality partition keys (e.g. OrgId/UserId) to avoid uneven hotspot nodes.",
                "Address distributed transactions across shards using 2-Phase Commit (2PC) or Saga pattern with compensating events.",
                "Highlight Read Replicas with CDC (Change Data Capture) pipelines for analytics offloading."
            ),
            codeSnippetOrFormula = """// Consistent Hashing VNode mapping
val vnodeHash = MurmurHash3.hash64("${'$'}nodeIp#${'$'}vnodeIndex")
val targetShard = ring.ceilingEntry(keyHash)?.value ?: ring.firstEntry().value"""
        ),

        SystemDesignCheatSheet(
            id = "concurrency_locking",
            title = "Distributed Locking & Idempotency",
            category = "Concurrency & Fault Tolerance",
            keyConcept = "Safe mutual exclusion across distributed microservices using Redis Redlock, ZooKeeper ephemeral nodes, and Idempotency Keys.",
            architecturePattern = "Client sends UUID idempotency key in HTTP header. Service stores request state in Redis with TTL. Redis distributed lock with auto-extending lease thread prevents split-brain execution.",
            keyTradeoffs = listOf(
                "Optimistic Locking (Version Column): Zero lock overhead, high throughput, but fails on high contention.",
                "Pessimistic Locking (SELECT FOR UPDATE): Guaranteed consistency, but holds database connections and causes lock contention.",
                "Distributed Lock (Redlock): Safe across independent nodes, but sensitive to clock drift and GC pauses."
            ),
            interviewTalkingPoints = listOf(
                "Always pair distributed locks with a fencing token (monotonically increasing integer) to reject stale out-of-order writes.",
                "Explain the exactly-once delivery myth: distributed systems provide at-least-once + idempotent deduplication.",
                "Mention exponential backoff with full jitter to avoid synchronous retry storms."
            ),
            codeSnippetOrFormula = """// Redis Atomic Lock Acquire with TTL
SET lock_key unique_token NX PX 5000"""
        ),

        SystemDesignCheatSheet(
            id = "microservice_resiliency",
            title = "Resiliency: Circuit Breakers & Rate Limiters",
            category = "Resilience & Security",
            keyConcept = "Preventing cascading failures in microservices via Circuit Breakers (CLOSED -> OPEN -> HALF-OPEN), Bulkheads, and Token Bucket Rate Limiters.",
            architecturePattern = "Resilience4j / Envoy proxy sidecar implementing Token Bucket algorithm with sliding-window error rate metrics. Fallback to cached default responses on degraded upstream.",
            keyTradeoffs = listOf(
                "Token Bucket: Allows short traffic bursts up to bucket capacity while enforcing steady rate limit.",
                "Leaky Bucket: Smooths output rate to constant throughput, but drops requests if queue fills.",
                "Circuit Breaker: Protects downstream dependencies, but requires carefully tuned failure percentage thresholds."
            ),
            interviewTalkingPoints = listOf(
                "Describe circuit breaker state machine transitions based on error rate over a sliding time window (e.g. 50% errors in 100 calls).",
                "Explain bulkheads to isolate thread pools so failure in non-critical service doesn't starve core APIs.",
                "Discuss HTTP 429 Too Many Requests response headers: Retry-After, X-RateLimit-Remaining."
            ),
            codeSnippetOrFormula = """// Token Bucket refilling
val newTokens = min(capacity, currentTokens + (elapsedSeconds * refillRate))"""
        )
    )
}
