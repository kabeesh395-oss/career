package com.example.careerpilot.data.repository

import com.example.careerpilot.data.model.JobApplication
import com.example.careerpilot.data.model.UserProfile

data class GeneratedOutreachLetter(
    val company: String,
    val roleTitle: String,
    val subjectLine: String,
    val linkedInInMail: String,
    val tailoredCoverLetter: String,
    val keySkillsHighlighted: List<String>
)

object AiCareerAssistant {

    fun generateOutreachAndCoverLetter(
        app: JobApplication,
        profile: UserProfile,
        skills: List<String>
    ): GeneratedOutreachLetter {
        val topSkills = skills.take(4).ifEmpty { listOf("Kotlin", "System Design", "Distributed Systems", "Cloud Infrastructure") }
        val skillsJoined = topSkills.joinToString(", ")

        val subject = "Senior Engineer Application — ${profile.fullName} | ${app.roleTitle}"

        val inMail = """Hi ${app.company} Recruiting Team,

I noticed the open ${app.roleTitle} role at ${app.company} and wanted to reach out directly. 

With over ${profile.experienceYears} years engineering scalable, high-throughput systems specializing in $skillsJoined, I recently optimized production services handling high traffic with 99.9% uptime and reduced query latency by 45%.

Given ${app.company}'s engineering focus on ${app.roleTitle}, I would love 10 minutes to discuss how my background in distributed systems and architecture aligns with your team's upcoming roadmap.

Best regards,
${profile.fullName}
${profile.email} | github.com/alexchen"""

        val coverLetter = """Dear Hiring Team at ${app.company},

I am writing to express my strong enthusiasm for the ${app.roleTitle} role. Having followed ${app.company}'s technical innovations, I am eager to bring my expertise in $skillsJoined to your engineering organization.

In my recent engineering experience, I have focused on building robust, scalable architectures:
• Architected high-availability services and data pipelines handling millions of daily events.
• Restructured database indexing and multi-tier Redis caching, cutting p99 latency by over 40%.
• Championed engineering excellence with automated CI/CD pipelines, containerization, and rigorous automated testing.

I am particularly excited about ${app.company}'s mission and would welcome the opportunity to discuss how my technical skills and ownership mindset can drive measurable outcomes for your team.

Thank you for your time and consideration.

Sincerely,
${profile.fullName}
${profile.email}"""

        return GeneratedOutreachLetter(
            company = app.company,
            roleTitle = app.roleTitle,
            subjectLine = subject,
            linkedInInMail = inMail,
            tailoredCoverLetter = coverLetter,
            keySkillsHighlighted = topSkills
        )
    }

    fun evaluateInterviewAnswerWithAi(
        question: String,
        userAnswer: String,
        category: String
    ): Triple<Int, String, String> {
        val lower = userAnswer.lowercase()
        var score = 75
        val feedbackBuilder = StringBuilder()
        var followUpProbe = ""

        if (lower.contains("tradeoff") || lower.contains("trade-off") || lower.contains("latency") || lower.contains("throughput")) {
            score += 10
            feedbackBuilder.append("✓ Strong articulation of architectural trade-offs. ")
        } else {
            score -= 5
            feedbackBuilder.append("Notice: You could strengthen this by explicitly contrasting latency vs. consistency trade-offs. ")
        }

        if (lower.contains("redis") || lower.contains("kafka") || lower.contains("lock") || lower.contains("mutex") || lower.contains("cache") || lower.contains("sharding")) {
            score += 10
            feedbackBuilder.append("✓ Concrete technical mechanisms identified. ")
        }

        if (lower.contains("metrics") || lower.contains("sla") || lower.contains("slo") || lower.contains("%") || lower.contains("monitoring")) {
            score += 5
            feedbackBuilder.append("✓ Included telemetry & operational observability. ")
        }

        val clampedScore = score.coerceIn(60, 98)

        followUpProbe = when {
            category.contains("Concurrency", ignoreCase = true) || lower.contains("lock") ->
                "Follow-up: How do you prevent split-brain execution if the node holding the distributed lock encounters an unexpected 10-second JVM Stop-the-World GC pause before releasing it?"
            category.contains("Cache", ignoreCase = true) || lower.contains("cache") ->
                "Follow-up: Suppose a celebrity user account causes an unexpected 100x hotkey spike. How does your caching topology prevent downstream database collapse during cache invalidation?"
            category.contains("Database", ignoreCase = true) || lower.contains("shard") ->
                "Follow-up: When rebalancing your hash ring with virtual nodes, how do you handle in-flight concurrent writes during the data migration window without data corruption?"
            else ->
                "Follow-up: If your primary cloud region suffers a complete network partition, what is your fallback mechanism for maintaining read availability while preventing dirty writes?"
        }

        return Triple(clampedScore, feedbackBuilder.toString().trim(), followUpProbe)
    }
}
