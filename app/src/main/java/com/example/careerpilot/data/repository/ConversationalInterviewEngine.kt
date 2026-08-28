package com.example.careerpilot.data.repository

import com.example.careerpilot.data.model.ConversationMessage
import com.example.careerpilot.data.model.ProbingChallenge
import java.util.UUID

object ConversationalInterviewEngine {

    val PROBING_CATALOG = listOf(
        ProbingChallenge(
            id = "probe_cache_stampede",
            category = "Cache Consistency & Concurrency",
            triggerPhrase = "cache",
            probeQuestion = "You mentioned introducing a caching layer. What happens when a popular cache key expires during a surge of 50,000 requests/sec (Cache Stampede), and how would your design prevent database saturation?",
            evaluationCriteria = "Candidate should mention Mutex/Distributed locks, probabilistic early expiration (XFetch algorithm), or background asynchronous refresh.",
            sampleIdealAnswer = "To prevent cache stampedes, I implement distributed locking (e.g. Redlock) so only one worker thread regenerates the cache key while others wait or serve slightly stale data (stale-while-revalidate)."
        ),
        ProbingChallenge(
            id = "probe_idempotency_failures",
            category = "Distributed Transactions & Resilience",
            triggerPhrase = "payment",
            probeQuestion = "In your payment and order processing flow, what happens if the network drops right after the payment gateway charges the customer but before your database commits? How do you guarantee exact-once settlement idempotency?",
            evaluationCriteria = "Candidate should address Idempotency keys, transactional outbox pattern, and two-phase reconciliation / webhook callbacks.",
            sampleIdealAnswer = "Every client request includes a unique UUID Idempotency-Key. We insert a pending payment record atomically. If a network failure occurs, the client retries with the same key, and we query the gateway state rather than re-charging."
        ),
        ProbingChallenge(
            id = "probe_db_partition_sharding",
            category = "Database Performance & Scale",
            triggerPhrase = "database",
            probeQuestion = "As your user table grows past 100 million records, single-node query indexing degrades. How would you choose a partition/sharding key, and how will your system handle cross-shard queries and joins?",
            evaluationCriteria = "Candidate should discuss hash vs range sharding, choosing tenant/user_id as shard key, and eliminating cross-shard joins via read replicas or denormalization.",
            sampleIdealAnswer = "I shard by consistent hash of `user_id` or `tenant_id` to ensure related entities reside on the same shard. Cross-shard joins are avoided by denormalizing read views into search clusters like Elasticsearch."
        ),
        ProbingChallenge(
            id = "probe_eventual_consistency",
            category = "Event-Driven Architecture",
            triggerPhrase = "kafka",
            probeQuestion = "When utilizing Kafka message streams to update downstream analytics and inventory services asynchronously, how do you handle out-of-order event arrivals and consumer lag during worker crashes?",
            evaluationCriteria = "Candidate should mention partition keys for order preservation, event versioning / vector clocks, and dead-letter queues with offset replay.",
            sampleIdealAnswer = "We use the business entity ID as the Kafka partition key to ensure per-entity FIFO ordering, pair events with monotonically increasing sequence numbers, and route unparseable messages to a Dead Letter Queue (DLQ)."
        ),
        ProbingChallenge(
            id = "probe_rate_limiting_spikes",
            category = "API Gateway & Security",
            triggerPhrase = "api",
            probeQuestion = "If malicious actors launch a distributed credential-stuffing attack on your login endpoint, what rate-limiting algorithm would you implement at the API Gateway, and how is state shared across multi-region gateway instances?",
            evaluationCriteria = "Candidate should explain Token Bucket or Leaky Bucket algorithms, Redis Sliding Window log, and geographic rate limits.",
            sampleIdealAnswer = "I implement a Sliding Window Counter algorithm backed by a Redis cluster with local memory caching for sub-millisecond lookups, applying adaptive IP + IP subnet + account fingerprint rate limiting."
        )
    )

    fun detectFollowUpProbe(userAnswer: String): ProbingChallenge {
        val lower = userAnswer.lowercase()
        return PROBING_CATALOG.firstOrNull { probe ->
            lower.contains(probe.triggerPhrase) || lower.contains(probe.category.lowercase().take(5))
        } ?: PROBING_CATALOG.random()
    }

    fun evaluateAnswerAndGenerateResponse(
        currentQuestion: String,
        userAnswer: String,
        isFollowUp: Boolean = false
    ): Pair<ConversationMessage, Int> {
        val lower = userAnswer.lowercase()
        var score = 65

        // Scoring heuristics
        val technicalTerms = listOf(
            "latency", "throughput", "concurrency", "lock", "redis", "kafka", "postgres",
            "index", "p99", "sla", "slo", "idempotent", "distributed", "partition", "sharding",
            "cache", "asynchronous", "circuit breaker", "retry", "backoff", "dead letter"
        )
        val matchedTerms = technicalTerms.count { lower.contains(it) }
        score += (matchedTerms * 4).coerceAtMost(25)

        val wordCount = userAnswer.split(Regex("""\s+""")).filter { it.isNotBlank() }.size
        if (wordCount > 60) score += 10
        else if (wordCount < 20) score -= 15

        val finalScore = score.coerceIn(35, 98)

        val feedbackSnippet = when {
            finalScore >= 85 -> "Excellent technical depth. You articulated architectural trade-offs, scale thresholds, and mitigation mechanisms clearly."
            finalScore >= 70 -> "Solid answer. Consider quantifying real-world performance impacts (e.g. latency in ms, QPS, failure recovery time)."
            else -> "Surface-level response. Focus on concrete engineering mechanics (e.g., locking strategies, consensus protocols, and specific failure modes)."
        }

        val aiContent = if (!isFollowUp) {
            val probe = detectFollowUpProbe(userAnswer)
            """
                Good initial foundation! Let's probe deeper on your architecture:
                
                👉 **Follow-Up Challenge**: ${probe.probeQuestion}
                
                *(Evaluation focus: ${probe.category})*
            """.trimIndent()
        } else {
            """
                Thanks for addressing the probe. Here is your round evaluation:
                
                • **Score**: $finalScore / 100
                • **Feedback**: $feedbackSnippet
                
                You demonstrated clear grasp of architectural resilience. Ready for the next core technical question!
            """.trimIndent()
        }

        val message = ConversationMessage(
            id = "msg_${UUID.randomUUID().toString().take(6)}",
            sender = "AI",
            content = aiContent,
            timestamp = System.currentTimeMillis(),
            isProbingQuestion = !isFollowUp,
            feedbackSnippet = feedbackSnippet
        )

        return Pair(message, finalScore)
    }
}
