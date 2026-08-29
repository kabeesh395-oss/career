package com.example.careerpilot

import com.example.careerpilot.data.model.*
import com.example.careerpilot.data.repository.BenchmarkCatalog
import org.junit.Assert.*
import org.junit.Test

class CareerLogicTest {

    @Test
    fun testRoleBenchmarksDefined() {
        val roles = BenchmarkCatalog.ROLE_BENCHMARKS
        assertTrue("Role benchmarks should not be empty", roles.isNotEmpty())
        assertTrue("Full Stack Engineer benchmark exists", roles.containsKey("Full Stack Engineer"))
        assertTrue("DevOps / Cloud Architect benchmark exists", roles.containsKey("DevOps / Cloud Architect"))

        val fullStackSkills = roles["Full Stack Engineer"]
        assertNotNull(fullStackSkills)
        assertTrue(fullStackSkills!!.any { it.skill == "TypeScript" })
    }

    @Test
    fun testInterviewQuestionsAvailability() {
        val questions = BenchmarkCatalog.INTERVIEW_QUESTIONS
        assertEquals("Should have 4 core technical interview questions", 4, questions.size)
        assertTrue(questions.any { it.category == "Distributed Systems & System Design" })
        assertTrue(questions.any { it.category == "Database Performance & Optimization" })
    }

    @Test
    fun testInitialLearningResources() {
        val resources = BenchmarkCatalog.INITIAL_LEARNING_RESOURCES
        assertTrue("Curated learning resources should exist", resources.isNotEmpty())
        assertTrue(resources.any { it.second.contains("Martin Kleppmann") })
    }

    @Test
    fun testAuditEngineEvaluation() {
        val profile = UserProfile(
            fullName = "Alex Rivera",
            targetRole = "Full Stack Engineer",
            experienceYears = 3.5f,
            readinessScore = 76
        )

        val skills = listOf(
            UserSkill(
                id = 1L,
                skillName = "Kotlin / TypeScript",
                category = "Languages",
                proficiencyLevel = 4,
                verified = true,
                source = "github_verified"
            ),
            UserSkill(
                id = 2L,
                skillName = "Distributed Systems",
                category = "Backend",
                proficiencyLevel = 4,
                verified = false,
                source = "self_reported"
            )
        )

        val projects = listOf(
            PortfolioProject(
                id = 1L,
                title = "Cloud API Gateway",
                description = "High throughput reverse proxy with Docker containers",
                repositoryUrl = "https://github.com/alex/gateway",
                liveUrl = "https://gateway.example.com",
                status = "completed",
                technologies = "Kotlin, Ktor, Docker",
                skillsTargeted = "Backend, Distributed Systems"
            )
        )

        val (issues, summary) = com.example.careerpilot.data.repository.AuditEngine.evaluateCandidate(
            profile = profile,
            skills = skills,
            projects = projects,
            latestResume = null,
            interviewAnswers = emptyList(),
            integrations = listOf(
                IntegrationAccount(
                    provider = "github",
                    username = "alex_dev",
                    isConnected = true,
                    connectionStatus = "CONNECTED",
                    lastSyncedAt = System.currentTimeMillis(),
                    details = "Verified"
                )
            )
        )

        assertNotNull(issues)
        assertTrue("Should detect missing ATS resume", issues.any { it.ruleId == "RULE_RES_AUDIT_MISSING" })
        assertTrue("Should detect missing interview telemetry", issues.any { it.ruleId == "RULE_INTERVIEW_TELEMETRY_DEFICIT" })
        assertTrue("Net audit score should be calculated", summary.netAuditScore <= summary.readinessScore)
        assertTrue("Evidence coverage percent should be > 0", summary.evidenceCoveragePercent > 0)
    }

    @Test
    fun testLearningResourceWorkflowInitialState() {
        val resource = LearningResource(
            id = 1L,
            title = "Designing Data-Intensive Applications",
            url = "https://dataintensive.net",
            provider = "O'Reilly",
            category = "Architecture",
            resourceType = "Course",
            difficulty = "Advanced",
            estimatedMinutes = 180,
            status = "NOT_STARTED",
            progressPercent = 0,
            quizQuestion = "Which consensus algorithm uses leader election?",
            quizOptions = "Raft / Paxos|Two-Phase Locking|Consistent Hashing",
            quizCorrectIndex = 0
        )

        assertEquals("NOT_STARTED", resource.status)
        assertEquals(0, resource.progressPercent)
        assertFalse(resource.isCompleted)
        assertEquals(0, resource.quizCorrectIndex)
    }
}
