package com.example.careerpilot.data.repository

import com.example.careerpilot.data.model.BulletAnalysis
import com.example.careerpilot.data.model.BulletRewriteOption
import java.util.UUID

object ResumeBulletRewriter {

    private val WEAK_VERB_PATTERNS = listOf(
        "worked on", "helped with", "responsible for", "assisted", "participated in",
        "handled", "managed", "used", "made", "did", "created a"
    )

    private val POWER_VERBS = listOf(
        "Architected", "Engineered", "Optimized", "Spearheaded", "Refactored",
        "Automated", "Pioneered", "Accelerated", "Orchestrated", "Scaled"
    )

    fun analyzeAndRewriteBullet(bulletText: String, targetRole: String = "Full Stack Engineer"): BulletAnalysis {
        val trimmed = bulletText.trim().removePrefix("-").removePrefix("•").trim()
        val lower = trimmed.lowercase()

        val weaknessFlags = mutableListOf<String>()
        var missingMetrics = false
        var passiveVoiceDetected = false

        // Check weak verbs
        if (WEAK_VERB_PATTERNS.any { lower.startsWith(it) || lower.contains(" $it ") }) {
            weaknessFlags.add("Weak/passive action verb detected (e.g. 'worked on', 'helped', 'responsible for')")
            passiveVoiceDetected = true
        }

        // Check for quantitative metrics
        val containsMetric = lower.contains("%") || lower.contains("ms") || lower.contains("qps") ||
                lower.contains("k") || lower.contains("m") || lower.contains("x") ||
                Regex("""\d+""").containsMatchIn(lower)

        if (!containsMetric) {
            weaknessFlags.add("Zero quantified metrics (e.g., latency %, throughput, cost savings, test coverage)")
            missingMetrics = true
        }

        // Check for missing business impact / outcome
        if (!lower.contains("resulting in") && !lower.contains("improving") && !lower.contains("reducing") && !lower.contains("enabling") && !lower.contains("achieving")) {
            weaknessFlags.add("Lacks clear outcome mechanism explaining how technical work moved business or system metrics")
        }

        // Generate 3 calibrated Google X-Y-Z formula variants
        val options = generateXYZVariants(trimmed, targetRole)

        return BulletAnalysis(
            originalBullet = trimmed,
            weaknessFlags = weaknessFlags,
            missingMetrics = missingMetrics,
            passiveVoiceDetected = passiveVoiceDetected,
            options = options
        )
    }

    private fun generateXYZVariants(original: String, targetRole: String): List<BulletRewriteOption> {
        val clean = original.replace(Regex("""^(worked on|helped with|responsible for|built|developed)\s+""", RegexOption.IGNORE_CASE), "").trim()

        // Extract key technical nouns if present
        val techNoun = when {
            clean.contains("api", ignoreCase = true) -> "REST & gRPC microservices"
            clean.contains("database", ignoreCase = true) || clean.contains("sql", ignoreCase = true) -> "PostgreSQL database queries and indexing"
            clean.contains("frontend", ignoreCase = true) || clean.contains("ui", ignoreCase = true) -> "responsive UI rendering pipeline"
            clean.contains("pipeline", ignoreCase = true) || clean.contains("ci/cd", ignoreCase = true) -> "automated CI/CD deployment pipelines"
            clean.contains("cache", ignoreCase = true) || clean.contains("redis", ignoreCase = true) -> "multi-tier Redis caching layer"
            else -> clean.ifBlank { "core application services and data pipelines" }
        }

        val option1 = BulletRewriteOption(
            id = "opt_metric_${UUID.randomUUID().toString().take(6)}",
            style = "METRIC_MAX",
            rewrittenText = "Optimized $techNoun, reducing p99 response latency by 42% and increasing throughput to 1,500+ QPS by implementing connection pooling and asynchronous non-blocking I/O.",
            accomplishedX = "Reduced p99 response latency by 42% and supported 1,500+ QPS",
            measuredByY = "Measured via Datadog APM & Prometheus telemetry",
            actionZ = "Implemented connection pooling and asynchronous non-blocking I/O in $techNoun",
            powerVerb = "Optimized",
            impactScore = 96
        )

        val option2 = BulletRewriteOption(
            id = "opt_arch_${UUID.randomUUID().toString().take(6)}",
            style = "ARCHITECTURE_FOCUSED",
            rewrittenText = "Architected highly available $techNoun, achieving 99.98% service uptime and zero data-loss failover by decoupling synchronous workflows with Kafka event queues.",
            accomplishedX = "Achieved 99.98% uptime and zero data-loss failover across high-traffic loads",
            measuredByY = "Verified against production SLO dashboards and synthetic chaos load tests",
            actionZ = "Decoupled synchronous workflows with partitioned Kafka message streams",
            powerVerb = "Architected",
            impactScore = 94
        )

        val option3 = BulletRewriteOption(
            id = "opt_leadership_${UUID.randomUUID().toString().take(6)}",
            style = "SCALE_AND_QUALITY",
            rewrittenText = "Spearheaded refactoring of $techNoun, accelerating deployment velocity by 3.5x and cutting production defect rates by 60% through automated integration test suites and containerized staging environments.",
            accomplishedX = "Accelerated release cycle 3.5x while slashing production bugs by 60%",
            measuredByY = "Tracked via Jira velocity charts and GitHub Actions CI pass rates",
            actionZ = "Instituted comprehensive integration test suites and Docker staging automation",
            powerVerb = "Spearheaded",
            impactScore = 92
        )

        return listOf(option1, option2, option3)
    }

    val SAMPLE_WEAK_BULLETS = listOf(
        "Worked on backend APIs using Kotlin and Spring Boot for user login.",
        "Helped with database optimization and fixed slow queries.",
        "Responsible for building frontend web components in React and TypeScript.",
        "Assisted team with Docker setup and CI/CD pipelines on GitHub.",
        "Managed Redis caching layer to make the application faster."
    )
}
