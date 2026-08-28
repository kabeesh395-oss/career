package com.example.careerpilot.data.repository

import com.example.careerpilot.data.model.*
import java.util.UUID

object JobMatcherEngine {

    val PRESET_JOB_POSTINGS = listOf(
        TargetJobPosting(
            id = "preset_google_l4",
            company = "Google",
            title = "Software Engineer III (L4) - Cloud Platforms",
            level = "Mid-Senior (L4)",
            location = "Mountain View, CA / Remote",
            minYearsExperience = 3.0f,
            requiredKeywords = listOf(
                "Kotlin", "Java", "Go", "Distributed Systems", "gRPC", "Docker", "Kubernetes", "Microservices", "CI/CD", "High Throughput"
            ),
            preferredKeywords = listOf(
                "GCP", "Spanner", "Bigtable", "Terraform", "System Architecture", "Prometheus", "SLO/SLA"
            ),
            fullJobDescription = """
                Google Cloud is hiring a Software Engineer III (L4) to build scalable, fault-tolerant infrastructure and high-throughput APIs.
                
                Minimum Qualifications:
                - BS/MS degree in Computer Science or equivalent practical experience.
                - 3+ years of experience building distributed backend services in Kotlin, Java, or Go.
                - Experience with containerization (Docker, Kubernetes), gRPC, and microservice architectures.
                - Demonstrated expertise in writing unit/integration test suites and CI/CD automation.
                
                Preferred Qualifications:
                - Experience with Google Cloud Platform (GCP), Spanner, Bigtable, or Terraform.
                - Proven ability to optimize p99 latency, design resilient caching layers, and maintain 99.99% SLA.
                - Strong background in system design and data modeling under high concurrency.
            """.trimIndent(),
            isPreset = true
        ),
        TargetJobPosting(
            id = "preset_stripe_backend",
            company = "Stripe",
            title = "Backend Engineer - Payments Infrastructure",
            level = "Senior",
            location = "San Francisco, CA / Remote",
            minYearsExperience = 4.0f,
            requiredKeywords = listOf(
                "Kotlin", "Java", "Ruby", "PostgreSQL", "Idempotency", "Distributed Transactions", "Kafka", "Docker", "API Design", "Unit Testing"
            ),
            preferredKeywords = listOf(
                "Financial Ledger", "Redis", "Zero-Downtime Migration", "AWS", "Rate Limiting", "Circuit Breakers"
            ),
            fullJobDescription = """
                Stripe builds economic infrastructure for the internet. We are looking for Backend Engineers to build mission-critical payment rails that process billions in volume.
                
                Key Responsibilities:
                - Design and implement idempotent APIs with strict zero-loss consistency guarantees.
                - Scale distributed message queues (Kafka) and PostgreSQL databases.
                - Build resilient transaction management with two-phase commits and saga patterns.
                - Drive high test coverage with automated mock simulations and chaos engineering.
            """.trimIndent(),
            isPreset = true
        ),
        TargetJobPosting(
            id = "preset_meta_fullstack",
            company = "Meta",
            title = "Full Stack Engineer - Reality Labs & Web",
            level = "Mid-Level (E4)",
            location = "Menlo Park, CA / Remote",
            minYearsExperience = 2.5f,
            requiredKeywords = listOf(
                "TypeScript", "React", "GraphQL", "Kotlin", "REST APIs", "State Management", "Jest", "Responsive UI", "Web Performance"
            ),
            preferredKeywords = listOf(
                "Relay", "Next.js", "WebSockets", "Modern CSS", "Accessibility (a11y)", "BFF Architecture"
            ),
            fullJobDescription = """
                Meta is seeking a Full Stack Engineer to create responsive, high-performance web and backend applications.
                
                Responsibilities:
                - Build responsive UI with TypeScript, React, and modern state management.
                - Design GraphQL and REST APIs connecting client applications with scalable backend services.
                - Optimize client render cycles, Core Web Vitals, and network bundle sizes.
                - Write end-to-end and component tests with Jest and Cypress.
            """.trimIndent(),
            isPreset = true
        ),
        TargetJobPosting(
            id = "preset_netflix_distributed",
            company = "Netflix",
            title = "Senior Platform Engineer - Edge Routing & Resiliency",
            level = "Senior",
            location = "Los Gatos, CA / Remote",
            minYearsExperience = 5.0f,
            requiredKeywords = listOf(
                "Distributed Systems", "Kotlin", "Java", "Spring Boot", "Kafka", "Redis", "Observability", "Circuit Breakers", "Chaos Engineering", "Docker"
            ),
            preferredKeywords = listOf(
                "Zuul / Envoy", "Prometheus", "Grafana", "Kubernetes", "Multi-region Replication", "High Availability"
            ),
            fullJobDescription = """
                Netflix delivers entertainment to over 260 million members globally. We are looking for a Senior Platform Engineer to build hyper-resilient edge infrastructure.
                
                What You Will Do:
                - Architect edge gateway services handling millions of concurrent video streams.
                - Implement adaptive rate limiting, circuit breaking (Resilience4j/Envoy), and dynamic failover.
                - Integrate observability stacks (Prometheus, OpenTelemetry, Grafana) to monitor p99 latency spikes.
            """.trimIndent(),
            isPreset = true
        )
    )

    fun evaluateJobMatch(
        jobPosting: TargetJobPosting,
        userProfile: UserProfile?,
        skills: List<UserSkill>,
        projects: List<PortfolioProject>,
        latestResume: ResumeAudit?
    ): JobMatchResult {
        // Collect candidate signal corpus
        val candidateCorpus = buildString {
            append(" ")
            append(userProfile?.fullName ?: "")
            append(" ")
            append(userProfile?.targetRole ?: "")
            append(" ")
            skills.forEach { s ->
                append("${s.skillName} (${s.category}, level ${s.proficiencyLevel}, verified: ${s.verified}) ")
            }
            projects.forEach { p ->
                append("${p.title} ${p.description} ${p.technologies} ${p.skillsTargeted} ")
            }
            if (latestResume != null) {
                append("${latestResume.rawText} ${latestResume.strengths} ${latestResume.weaknesses}")
            }
        }.lowercase()

        // 1. Check Required Keywords
        val matchedRequired = mutableListOf<String>()
        val missingRequired = mutableListOf<String>()
        for (kw in jobPosting.requiredKeywords) {
            val kwLower = kw.lowercase()
            if (candidateCorpus.contains(kwLower) || isFuzzySkillMatch(kwLower, candidateCorpus)) {
                matchedRequired.add(kw)
            } else {
                missingRequired.add(kw)
            }
        }

        // 2. Check Preferred Keywords
        val matchedPreferred = mutableListOf<String>()
        val missingPreferred = mutableListOf<String>()
        for (kw in jobPosting.preferredKeywords) {
            val kwLower = kw.lowercase()
            if (candidateCorpus.contains(kwLower) || isFuzzySkillMatch(kwLower, candidateCorpus)) {
                matchedPreferred.add(kw)
            } else {
                missingPreferred.add(kw)
            }
        }

        // 3. Compute Keyword Match Weight
        val totalReq = jobPosting.requiredKeywords.size.coerceAtLeast(1)
        val totalPref = jobPosting.preferredKeywords.size.coerceAtLeast(1)
        val reqRatio = matchedRequired.size.toFloat() / totalReq
        val prefRatio = matchedPreferred.size.toFloat() / totalPref

        // 4. Experience delta
        val userExp = userProfile?.experienceYears ?: 1.0f
        val expRatio = (userExp / jobPosting.minYearsExperience.coerceAtLeast(1.0f)).coerceAtMost(1.2f)

        // 5. Verified skills boost
        val verifiedCount = skills.count { it.verified }
        val verifiedBonus = (verifiedCount * 2).coerceAtMost(10)

        // 6. Resume presence boost
        val resumeScore = latestResume?.overallScore ?: 60

        // Composite match score: 45% Required Keywords, 20% Preferred, 15% Experience, 10% Verified Bonus, 10% ATS Resume Quality
        val rawScore = (reqRatio * 45f) + (prefRatio * 20f) + (expRatio * 15f) + verifiedBonus + ((resumeScore / 100f) * 10f)
        val finalScore = rawScore.toInt().coerceIn(15, 98)

        // Key Gaps
        val keyGaps = mutableListOf<String>()
        if (missingRequired.isNotEmpty()) {
            keyGaps.add("Missing ${missingRequired.size} core required keywords: ${missingRequired.take(3).joinToString(", ")}")
        }
        if (userExp < jobPosting.minYearsExperience) {
            keyGaps.add("Experience gap: You have ${userExp} yrs vs ${jobPosting.minYearsExperience} yrs required")
        }
        if (projects.none { it.liveUrl.isNotBlank() }) {
            keyGaps.add("No live production URL detected in your portfolio for verifiable deployment proof")
        }
        if (skills.none { it.verified }) {
            keyGaps.add("Zero verified skills on record. Link GitHub repositories to prove hard skill proficiency")
        }

        // ATS Recommendations
        val atsRecommendations = mutableListOf<String>()
        if (missingRequired.isNotEmpty()) {
            atsRecommendations.add("Inject explicit bullet points incorporating '${missingRequired.take(2).joinToString("', '")}' into your experience section")
        }
        atsRecommendations.add("Highlight measurable scale metrics (e.g. latency reduction %, QPS handled, uptime SLA)")
        if (missingPreferred.isNotEmpty()) {
            atsRecommendations.add("Add a 'Technical Skills' category covering preferred technologies like ${missingPreferred.take(2).joinToString(", ")}")
        }
        atsRecommendations.add("Ensure bullet points follow the Google X-Y-Z formula: 'Accomplished [X] as measured by [Y] by doing [Z]'")

        val fitSummary = when {
            finalScore >= 85 -> "Outstanding Match! Your technical stack and project history strongly align with ${jobPosting.company}'s requirements."
            finalScore >= 70 -> "Strong Contender. You satisfy core requirements, but addressing ${missingRequired.size} missing keywords will significantly boost ATS ranking."
            finalScore >= 50 -> "Moderate Fit. Focus on incorporating required keywords and demonstrating live deployment proof to pass initial screening."
            else -> "High Keyword Gap. Tailor your resume experience and portfolio projects specifically to ${jobPosting.title} requirements before applying."
        }

        return JobMatchResult(
            id = "match_${jobPosting.id}_${System.currentTimeMillis()}",
            jobPostingId = jobPosting.id,
            company = jobPosting.company,
            jobTitle = jobPosting.title,
            matchScore = finalScore,
            matchedKeywords = matchedRequired + matchedPreferred,
            missingRequiredKeywords = missingRequired,
            missingPreferredKeywords = missingPreferred,
            keyGaps = keyGaps,
            atsRecommendations = atsRecommendations,
            fitSummary = fitSummary,
            calculatedAt = System.currentTimeMillis()
        )
    }

    private fun isFuzzySkillMatch(kw: String, corpus: String): Boolean {
        return when (kw) {
            "gcp", "google cloud" -> corpus.contains("gcp") || corpus.contains("google cloud") || corpus.contains("cloud")
            "aws" -> corpus.contains("aws") || corpus.contains("amazon web services")
            "ci/cd" -> corpus.contains("ci/cd") || corpus.contains("pipeline") || corpus.contains("github actions")
            "microservices" -> corpus.contains("microservices") || corpus.contains("distributed") || corpus.contains("api gateway")
            "grpc" -> corpus.contains("grpc") || corpus.contains("protobuf")
            "kafka" -> corpus.contains("kafka") || corpus.contains("message queue") || corpus.contains("event-driven")
            "redis" -> corpus.contains("redis") || corpus.contains("cache") || corpus.contains("caching")
            "typescript" -> corpus.contains("typescript") || corpus.contains("ts") || corpus.contains("javascript")
            "docker" -> corpus.contains("docker") || corpus.contains("container")
            "kubernetes" -> corpus.contains("kubernetes") || corpus.contains("k8s")
            else -> false
        }
    }
}
