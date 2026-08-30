package com.example.careerpilot.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = "default_user",
    val fullName: String = "",
    val email: String = "",
    val headline: String = "",
    val bio: String = "",
    val location: String = "",
    val education: String = "",
    val experienceYears: Float = 0.0f,
    val targetRole: String = "Full Stack Engineer",
    val targetIndustry: String = "",
    val targetSalary: String = "",
    val targetCompanyTier: String = "Top Tech",
    val readinessScore: Int? = null,
    val onboardingCompleted: Boolean = false
)

@Entity(tableName = "user_skills")
data class UserSkill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val skillName: String,
    val category: String,
    val proficiencyLevel: Int, // 1 to 5
    val verified: Boolean = false,
    val source: String = "self_reported"
)

@Entity(tableName = "skill_gaps")
data class SkillGap(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetRole: String,
    val skillName: String,
    val category: String,
    val requiredLevel: Int,
    val currentLevel: Int,
    val gapScore: Int,
    val priority: String, // "high", "medium", "low"
    val recommendation: String
)

@Entity(tableName = "roadmaps")
data class Roadmap(
    @PrimaryKey val id: String = "active_roadmap",
    val title: String,
    val targetRole: String,
    val summary: String,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val progressPercent: Float = 0f,
    val status: String = "in_progress"
)

@Entity(tableName = "roadmap_items")
data class RoadmapItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roadmapId: String = "active_roadmap",
    val phaseNumber: Int,
    val phaseTitle: String,
    val title: String,
    val description: String,
    val category: String,
    val estimatedHours: Float,
    val orderIndex: Int,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)

@Entity(tableName = "portfolio_projects")
data class PortfolioProject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val repositoryUrl: String,
    val liveUrl: String,
    val status: String, // "planning", "in_progress", "completed"
    val technologies: String,
    val skillsTargeted: String
)

@Entity(tableName = "resume_audits")
data class ResumeAudit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filename: String,
    val targetRole: String,
    val overallScore: Int,
    val impactScore: Int,
    val brevityScore: Int,
    val styleScore: Int,
    val skillsDetected: String, // Comma separated
    val strengths: String, // Newline separated
    val weaknesses: String, // Newline separated
    val recommendations: String, // Newline separated
    val rawText: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "interview_sessions")
data class InterviewSession(
    @PrimaryKey val id: String,
    val roleTarget: String,
    val difficulty: String,
    val status: String = "in_progress", // "in_progress", "completed"
    val totalQuestions: Int = 4,
    val completedQuestions: Int = 0,
    val overallScore: Int = 0,
    val feedbackSummary: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "interview_answers")
data class InterviewAnswer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val interviewId: String,
    val questionNumber: Int,
    val questionText: String,
    val category: String,
    val difficulty: String,
    val rubric: String,
    val answerText: String,
    val score: Int,
    val clarityScore: Int,
    val technicalScore: Int,
    val feedback: String,
    val suggestedImprovement: String,
    val submittedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "learning_resources")
data class LearningResource(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val provider: String,
    val url: String,
    val category: String,
    val skillTags: String = "",
    val estimatedMinutes: Int,
    val difficulty: String,
    val resourceType: String = "Article", // "Article", "Video", "Course", "Documentation"
    val status: String = "NOT_STARTED", // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
    val progressPercent: Int = 0,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val lastStudiedAt: Long? = null,
    val studyMinutesSpent: Int = 0,
    val notes: String = "",
    val contentSummary: String = "",
    val quizQuestion: String = "",
    val quizOptions: String = "",
    val quizCorrectIndex: Int = 0,
    val isCompleted: Boolean = false
)

@Entity(tableName = "integrations")
data class IntegrationAccount(
    @PrimaryKey val provider: String, // "github", "linkedin"
    val username: String = "",
    val connectionStatus: String = "NOT_CONNECTED", // "NOT_CONNECTED", "CHECKING", "CONNECTED", "INVALID", "NOT_FOUND", "RATE_LIMITED", "ERROR"
    val isConnected: Boolean = false,
    val lastSyncedAt: Long = 0L,
    val avatarUrl: String = "",
    val displayName: String = "",
    val publicReposCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val publicGistsCount: Int = 0,
    val bio: String = "",
    val company: String = "",
    val location: String = "",
    val topRepositoriesJson: String = "",
    val details: String = "",
    val errorMessage: String = ""
)

@Entity(tableName = "analytics_events")
data class AnalyticsEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventName: String,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class NextBestAction(
    val actionId: String,
    val title: String,
    val category: String,
    val whyItMatters: String,
    val evidence: String,
    val estimatedMinutes: Int,
    val priority: String, // "urgent", "high", "medium"
    val targetRoute: String,
    val ctaText: String
)

@Entity(tableName = "audit_issues")
data class AuditIssue(
    @PrimaryKey val id: String,
    val ruleId: String,
    val category: String, // "Resume Quality", "Skill Depth", "Production Evidence", "GitHub Evidence", "Mock Interview", "System Design", "ATS Compatibility"
    val title: String,
    val severity: String, // "CRITICAL", "HIGH", "MEDIUM", "LOW", "RESOLVED"
    val scoreImpact: Int, // Negative integer like -5, -4, -3, -2 or 0 if resolved
    val evidence: String,
    val explanation: String,
    val recommendedFix: String,
    val estimatedEffort: String,
    val verificationRequirement: String,
    val status: String = "OPEN", // "OPEN", "IN_PROGRESS", "VERIFICATION", "RESOLVED"
    val evidenceStatus: String = "UNVERIFIED", // "VERIFIED", "PARTIALLY_VERIFIED", "UNVERIFIED", "EVIDENCE_UNAVAILABLE"
    val confidence: String = "HIGH", // "HIGH", "MEDIUM", "LOW"
    val targetRoute: String = "projects",
    val ctaText: String = "Start Fix",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class AuditScoreSummary(
    val readinessScore: Int? = null,
    val netAuditScore: Int? = null,
    val totalDemerits: Int = 0,
    val criticalCount: Int = 0,
    val highCount: Int = 0,
    val mediumCount: Int = 0,
    val lowCount: Int = 0,
    val resolvedCount: Int = 0,
    val totalIssuesCount: Int = 0,
    val evidenceCoveragePercent: Int = 0,
    val profileConfidence: String = "NOT_EVALUATED",
    val isOfflineEvaluated: Boolean = true,
    val hasEvaluatedData: Boolean = false,
    val lastEvaluatedAt: Long = System.currentTimeMillis()
)

data class AuditPenaltyConfig(
    val missingResumeMetrics: Int = -3,
    val missingGithubEvidence: Int = -4,
    val missingDeploymentEvidence: Int = -5,
    val missingCicdTesting: Int = -4,
    val unverifiedAdvancedSkill: Int = -2,
    val weakSystemDesignDepth: Int = -5,
    val weakInterviewTradeoffs: Int = -3,
    val atsStructureDeficit: Int = -2
)

// === FEATURE 1: TARGET JOB DESCRIPTION MATCHER ===
@Entity(tableName = "job_postings")
data class TargetJobPosting(
    @PrimaryKey val id: String,
    val company: String,
    val title: String,
    val level: String, // "Mid-Level", "Senior", "Staff"
    val location: String, // "Remote", "San Francisco, CA", etc.
    val minYearsExperience: Float,
    val requiredKeywords: List<String>,
    val preferredKeywords: List<String>,
    val fullJobDescription: String,
    val isPreset: Boolean = true
)

@Entity(tableName = "job_matches")
data class JobMatchResult(
    @PrimaryKey val id: String,
    val jobPostingId: String,
    val company: String,
    val jobTitle: String,
    val matchScore: Int, // 0 - 100
    val matchedKeywords: List<String>,
    val missingRequiredKeywords: List<String>,
    val missingPreferredKeywords: List<String>,
    val keyGaps: List<String>,
    val atsRecommendations: List<String>,
    val fitSummary: String,
    val calculatedAt: Long = System.currentTimeMillis()
)

// === FEATURE 2: AI RESUME BULLET REWRITER (GOOGLE X-Y-Z FORMULA) ===
data class BulletRewriteOption(
    val id: String,
    val style: String, // "METRIC_MAX", "ARCHITECTURE_FOCUSED", "LEADERSHIP_SCALE"
    val formulaType: String = "X-Y-Z",
    val rewrittenText: String,
    val accomplishedX: String,
    val measuredByY: String,
    val actionZ: String,
    val powerVerb: String,
    val impactScore: Int // 85 - 99
)

data class BulletAnalysis(
    val originalBullet: String,
    val weaknessFlags: List<String>,
    val missingMetrics: Boolean,
    val passiveVoiceDetected: Boolean,
    val options: List<BulletRewriteOption>
)

// === FEATURE 3: CONVERSATIONAL MOCK AI PROBING ===
data class ProbingChallenge(
    val id: String,
    val category: String, // "Concurrency", "Cache Consistency", "Fault Tolerance", "Security"
    val triggerPhrase: String,
    val probeQuestion: String,
    val evaluationCriteria: String,
    val sampleIdealAnswer: String
)

data class ConversationMessage(
    val id: String,
    val sender: String, // "AI" or "USER"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isProbingQuestion: Boolean = false,
    val feedbackSnippet: String? = null
)

// === FEATURE 4: JOB APPLICATION PIPELINE CRM & SALARY CALCULATOR ===
@Entity(tableName = "job_applications")
data class JobApplication(
    @PrimaryKey val id: String,
    val company: String,
    val roleTitle: String,
    val stage: String, // "WISHLIST", "APPLIED", "SCREENING", "TECHNICAL", "OFFER", "REJECTED"
    val location: String,
    val salaryOffered: String,
    val notes: String,
    val interviewDate: String, // e.g. "Tomorrow at 2:00 PM"
    val matchScore: Int = 85,
    val appliedDate: Long = System.currentTimeMillis()
)

// === FEATURE 5: LIVE CODING & SYSTEM DESIGN SANDBOX ===
@Entity(tableName = "coding_challenges")
data class CodingChallenge(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // "Algorithms", "System Design", "Concurrency", "Architecture"
    val difficulty: String, // "Easy", "Medium", "Hard"
    val problemStatement: String,
    val starterCode: String,
    val solutionReference: String,
    val timeComplexityTarget: String,
    val spaceComplexityTarget: String,
    val isCompleted: Boolean = false
)

// === FEATURE 6: PEER MOCK INTERVIEWS & MENTOR MATCHMAKING ===
@Entity(tableName = "peer_matches")
data class PeerMatch(
    @PrimaryKey val id: String,
    val peerName: String,
    val peerHeadline: String,
    val targetRole: String,
    val companyTarget: String,
    val timezone: String,
    val experienceLevel: String,
    val rating: Float, // 4.8 to 5.0
    val sessionsCompleted: Int,
    val skillsSpecialty: List<String>,
    val availabilityStatus: String = "Available Today"
)

// === FEATURE 7: WEEKLY SKILL SPRINTS & GITHUB PROOF BADGES ===
@Entity(tableName = "skill_sprints")
data class SkillSprint(
    @PrimaryKey val id: String,
    val sprintTitle: String,
    val targetSkill: String,
    val description: String,
    val durationDays: Int = 7,
    val currentDay: Int = 1,
    val milestoneTasks: List<String>,
    val completedMilestones: Int = 0,
    val badgeName: String,
    val rewardXp: Int = 350,
    val isClaimed: Boolean = false
)


