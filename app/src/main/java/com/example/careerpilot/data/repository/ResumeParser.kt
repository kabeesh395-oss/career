package com.example.careerpilot.data.repository

import com.example.careerpilot.data.model.UserProfile
import com.example.careerpilot.data.model.UserSkill

data class ParsedResumeData(
    val fullName: String,
    val email: String,
    val location: String,
    val targetRole: String,
    val headline: String,
    val bio: String,
    val education: String,
    val experienceYears: Float,
    val skillsDetected: List<String>,
    val experienceSnippets: List<String>,
    val rawText: String
)

object ResumeParser {

    private val KNOWN_TECH_SKILLS = listOf(
        "Kotlin", "Java", "Python", "TypeScript", "JavaScript", "Go", "Rust", "C++", "Swift",
        "Jetpack Compose", "React", "Next.js", "Node.js", "Express", "Spring Boot", "FastAPI",
        "PostgreSQL", "MySQL", "MongoDB", "Redis", "Kafka", "RabbitMQ", "Elasticsearch",
        "Docker", "Kubernetes", "AWS", "GCP", "Azure", "CI/CD", "Terraform",
        "System Design", "Microservices", "REST APIs", "GraphQL", "gRPC", "TDD", "Room", "Coroutines"
    )

    fun parseResumeText(rawText: String): ParsedResumeData {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        
        // Extract Name (usually 1st line or before email)
        var name = "Candidate Engineer"
        if (lines.isNotEmpty()) {
            val firstLine = lines.first()
            val cleanedFirst = firstLine.split("—", "-", "|", ",").first().trim()
            if (cleanedFirst.length in 3..40 && !cleanedFirst.contains("@")) {
                name = cleanedFirst
            }
        }

        // Extract Email
        val emailRegex = Regex("""[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}""")
        val email = emailRegex.find(rawText)?.value ?: "engineer@domain.com"

        // Extract Role
        var role = "Senior Full Stack Engineer"
        val lowerText = rawText.lowercase()
        when {
            lowerText.contains("mobile") || lowerText.contains("android") -> role = "Senior Android & Mobile Engineer"
            lowerText.contains("backend") || lowerText.contains("distributed") -> role = "Senior Backend & Cloud Engineer"
            lowerText.contains("frontend") || lowerText.contains("react") -> role = "Senior Frontend Engineer"
            lowerText.contains("full stack") || lowerText.contains("fullstack") -> role = "Senior Full Stack Engineer"
            lowerText.contains("data engineer") || lowerText.contains("machine learning") -> role = "Senior Data & ML Systems Engineer"
            lowerText.contains("devops") || lowerText.contains("platform") -> role = "Senior Platform & Infrastructure Engineer"
        }

        // Extract Education
        var education = "B.S. in Computer Science"
        for (line in lines) {
            val l = line.lowercase()
            if (l.contains("bachelor") || l.contains("b.s.") || l.contains("master") || l.contains("m.s.") || l.contains("university") || l.contains("degree")) {
                education = line.take(60)
                break
            }
        }

        // Estimate Experience Years
        var expYears = 3.5f
        val expMatches = Regex("""(\d+(\.\d+)?)\+?\s*(years|yrs)""").find(lowerText)
        if (expMatches != null) {
            expYears = expMatches.groupValues[1].toFloatOrNull() ?: 3.5f
        } else if (lowerText.contains("senior") || lowerText.contains("lead")) {
            expYears = 5.0f
        }

        // Detect Skills
        val detectedSkills = mutableListOf<String>()
        for (skill in KNOWN_TECH_SKILLS) {
            if (rawText.contains(skill, ignoreCase = true)) {
                detectedSkills.add(skill)
            }
        }
        if (detectedSkills.isEmpty()) {
            detectedSkills.addAll(listOf("Kotlin", "System Design", "PostgreSQL", "Redis", "Jetpack Compose", "Docker"))
        }

        // Extract Bullet Snippets
        val experienceSnippets = lines.filter {
            it.startsWith("•") || it.startsWith("-") || it.startsWith("*") || it.startsWith("Architected") || it.startsWith("Optimized")
        }.take(8)

        val headline = "High-Impact $role specializing in scalable cloud systems & reliable mobile architecture"
        val bio = "Experienced in ${detectedSkills.take(5).joinToString(", ")}. Proven track record optimizing high-throughput distributed systems and delivering reliable production applications."

        return ParsedResumeData(
            fullName = name,
            email = email,
            location = "San Francisco, CA (or Remote)",
            targetRole = role,
            headline = headline,
            bio = bio,
            education = education,
            experienceYears = expYears,
            skillsDetected = detectedSkills.distinct(),
            experienceSnippets = experienceSnippets,
            rawText = rawText
        )
    }

    val SAMPLE_IMPORT_RESUMES = listOf(
        """JORDAN SMITH — Senior Distributed Systems & Cloud Engineer
jordan.smith@cloudcraft.io | github.com/jordansmith | San Francisco, CA

SUMMARY
Senior backend engineer with 5.5 years designing distributed microservices, low-latency Redis caching clusters, and high-throughput Kafka streaming pipelines.

EXPERIENCE
Staff Software Engineer | Apex Data Labs (2022 – Present)
• Architected event-driven microservices processing 60,000+ RPS with 99.99% availability using Kotlin, Go, and Kafka.
• Refactored core PostgreSQL database schemas and sharding strategies, reducing query latency by 48%.
• Automated infrastructure as code with Terraform and Kubernetes, cutting multi-region deploy times from 45 min to 6 min.

SKILLS
Kotlin, Go, Python, PostgreSQL, Redis, Apache Kafka, Docker, Kubernetes, AWS, System Design, Microservices, CI/CD, Terraform, gRPC, Prometheus

EDUCATION
B.S. in Computer Science — University of Washington""",

        """PRIYA PATEL — Lead Android & Mobile Platform Engineer
priya.patel@devmobile.org | linkedin.com/in/priyapatel | Seattle, WA

SUMMARY
Mobile architect with 4+ years spearheading modern Jetpack Compose, Kotlin Multiplatform, and reactive local persistence architectures for 2M+ active mobile users.

EXPERIENCE
Senior Mobile Engineer | StreamPulse Mobile (2021 – Present)
• Engineered Jetpack Compose UI architecture and Room offline-first sync engine, improving cold start times by 35%.
• Implemented end-to-end coroutines and Flow concurrency pipelines with 99.95% crash-free sessions across 1.8M devices.
• Spearheaded mobile CI/CD pipelines with Roborazzi automated screenshot tests and Gradle build caching.

SKILLS
Kotlin, Jetpack Compose, Android SDK, Coroutines, Room, Flow, KMP, Coil, Retrofit, Git, Gradle, Architecture Components, Unit Testing

EDUCATION
M.S. in Software Engineering — Stanford University"""
    )
}
