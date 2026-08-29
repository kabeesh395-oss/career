package com.example.careerpilot.data.local

import androidx.room.*
import com.example.careerpilot.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CareerDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 'default_user' LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 'default_user' LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    // User Skills
    @Query("SELECT * FROM user_skills ORDER BY category ASC, skillName ASC")
    fun getUserSkillsFlow(): Flow<List<UserSkill>>

    @Query("SELECT * FROM user_skills")
    suspend fun getUserSkills(): List<UserSkill>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSkill(skill: UserSkill)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSkills(skills: List<UserSkill>)

    @Delete
    suspend fun deleteUserSkill(skill: UserSkill)

    @Query("DELETE FROM user_skills")
    suspend fun clearUserSkills()

    // Skill Gaps
    @Query("SELECT * FROM skill_gaps ORDER BY gapScore DESC, priority DESC")
    fun getSkillGapsFlow(): Flow<List<SkillGap>>

    @Query("SELECT * FROM skill_gaps ORDER BY gapScore DESC")
    suspend fun getSkillGaps(): List<SkillGap>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkillGaps(gaps: List<SkillGap>)

    @Query("DELETE FROM skill_gaps")
    suspend fun clearSkillGaps()

    // Roadmaps
    @Query("SELECT * FROM roadmaps WHERE id = 'active_roadmap' LIMIT 1")
    fun getActiveRoadmapFlow(): Flow<Roadmap?>

    @Query("SELECT * FROM roadmaps WHERE id = 'active_roadmap' LIMIT 1")
    suspend fun getActiveRoadmap(): Roadmap?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRoadmap(roadmap: Roadmap)

    // Roadmap Items
    @Query("SELECT * FROM roadmap_items WHERE roadmapId = 'active_roadmap' ORDER BY phaseNumber ASC, orderIndex ASC")
    fun getRoadmapItemsFlow(): Flow<List<RoadmapItem>>

    @Query("SELECT * FROM roadmap_items WHERE roadmapId = 'active_roadmap' ORDER BY phaseNumber ASC, orderIndex ASC")
    suspend fun getRoadmapItems(): List<RoadmapItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadmapItems(items: List<RoadmapItem>)

    @Update
    suspend fun updateRoadmapItem(item: RoadmapItem)

    @Query("DELETE FROM roadmap_items WHERE roadmapId = 'active_roadmap'")
    suspend fun clearRoadmapItems()

    // Portfolio Projects
    @Query("SELECT * FROM portfolio_projects ORDER BY id DESC")
    fun getProjectsFlow(): Flow<List<PortfolioProject>>

    @Query("SELECT * FROM portfolio_projects ORDER BY id DESC")
    suspend fun getProjects(): List<PortfolioProject>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: PortfolioProject)

    @Update
    suspend fun updateProject(project: PortfolioProject)

    @Delete
    suspend fun deleteProject(project: PortfolioProject)

    @Query("DELETE FROM portfolio_projects")
    suspend fun clearProjects()

    // Resume Audits
    @Query("SELECT * FROM resume_audits ORDER BY createdAt DESC")
    fun getResumeAuditsFlow(): Flow<List<ResumeAudit>>

    @Query("SELECT * FROM resume_audits ORDER BY createdAt DESC")
    suspend fun getResumeAudits(): List<ResumeAudit>

    @Query("SELECT * FROM resume_audits ORDER BY createdAt DESC LIMIT 1")
    fun getLatestResumeAuditFlow(): Flow<ResumeAudit?>

    @Query("SELECT * FROM resume_audits ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestResumeAudit(): ResumeAudit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResumeAudit(audit: ResumeAudit)

    // Interview Sessions & Answers
    @Query("SELECT * FROM interview_sessions ORDER BY createdAt DESC")
    fun getInterviewsFlow(): Flow<List<InterviewSession>>

    @Query("SELECT * FROM interview_sessions ORDER BY createdAt DESC")
    suspend fun getInterviews(): List<InterviewSession>

    @Query("SELECT * FROM interview_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getInterviewSession(sessionId: String): InterviewSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateInterview(session: InterviewSession)

    @Query("SELECT * FROM interview_answers WHERE interviewId = :sessionId ORDER BY questionNumber ASC")
    fun getInterviewAnswersFlow(sessionId: String): Flow<List<InterviewAnswer>>

    @Query("SELECT * FROM interview_answers WHERE interviewId = :sessionId ORDER BY questionNumber ASC")
    suspend fun getInterviewAnswers(sessionId: String): List<InterviewAnswer>

    @Query("SELECT * FROM interview_answers ORDER BY submittedAt DESC")
    suspend fun getAllInterviewAnswers(): List<InterviewAnswer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterviewAnswer(answer: InterviewAnswer)

    // Learning Resources
    @Query("SELECT * FROM learning_resources ORDER BY category ASC, id ASC")
    fun getLearningResourcesFlow(): Flow<List<LearningResource>>

    @Query("SELECT * FROM learning_resources ORDER BY category ASC, id ASC")
    suspend fun getLearningResources(): List<LearningResource>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningResources(resources: List<LearningResource>)

    @Update
    suspend fun updateLearningResource(resource: LearningResource)

    // Integrations
    @Query("SELECT * FROM integrations")
    fun getIntegrationsFlow(): Flow<List<IntegrationAccount>>

    @Query("SELECT * FROM integrations")
    suspend fun getIntegrations(): List<IntegrationAccount>

    @Query("SELECT * FROM integrations WHERE provider = :provider LIMIT 1")
    suspend fun getIntegration(provider: String): IntegrationAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateIntegration(account: IntegrationAccount)

    // Audit Issues & Red Flags
    @Query("SELECT * FROM audit_issues ORDER BY CASE severity WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 WHEN 'LOW' THEN 4 ELSE 5 END, createdAt DESC")
    fun getAuditIssuesFlow(): Flow<List<AuditIssue>>

    @Query("SELECT * FROM audit_issues ORDER BY CASE severity WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 WHEN 'LOW' THEN 4 ELSE 5 END, createdAt DESC")
    suspend fun getAuditIssues(): List<AuditIssue>

    @Query("SELECT * FROM audit_issues WHERE id = :issueId LIMIT 1")
    suspend fun getAuditIssue(issueId: String): AuditIssue?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditIssue(issue: AuditIssue)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditIssues(issues: List<AuditIssue>)

    @Update
    suspend fun updateAuditIssue(issue: AuditIssue)

    @Delete
    suspend fun deleteAuditIssue(issue: AuditIssue)

    @Query("DELETE FROM audit_issues")
    suspend fun clearAuditIssues()

    // Analytics Events
    @Query("SELECT * FROM analytics_events ORDER BY timestamp DESC LIMIT 20")
    fun getRecentAnalyticsFlow(): Flow<List<AnalyticsEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyticsEvent(event: AnalyticsEvent)

    // Target Job Postings & Match Results
    @Query("SELECT * FROM job_postings ORDER BY isPreset DESC, company ASC")
    fun getJobPostingsFlow(): Flow<List<TargetJobPosting>>

    @Query("SELECT * FROM job_postings ORDER BY isPreset DESC, company ASC")
    suspend fun getJobPostings(): List<TargetJobPosting>

    @Query("SELECT * FROM job_postings WHERE id = :id LIMIT 1")
    suspend fun getJobPosting(id: String): TargetJobPosting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobPosting(posting: TargetJobPosting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobPostings(postings: List<TargetJobPosting>)

    @Query("SELECT * FROM job_matches ORDER BY calculatedAt DESC")
    fun getJobMatchesFlow(): Flow<List<JobMatchResult>>

    @Query("SELECT * FROM job_matches WHERE jobPostingId = :jobPostingId ORDER BY calculatedAt DESC LIMIT 1")
    fun getJobMatchForPostingFlow(jobPostingId: String): Flow<JobMatchResult?>

    @Query("SELECT * FROM job_matches WHERE jobPostingId = :jobPostingId ORDER BY calculatedAt DESC LIMIT 1")
    suspend fun getJobMatchForPosting(jobPostingId: String): JobMatchResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobMatchResult(result: JobMatchResult)

    @Query("DELETE FROM job_matches WHERE jobPostingId = :jobPostingId")
    suspend fun deleteJobMatchForPosting(jobPostingId: String)

    // Job Applications Pipeline CRM
    @Query("SELECT * FROM job_applications ORDER BY appliedDate DESC")
    fun getJobApplicationsFlow(): Flow<List<JobApplication>>

    @Query("SELECT * FROM job_applications ORDER BY appliedDate DESC")
    suspend fun getJobApplications(): List<JobApplication>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobApplication(app: JobApplication)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobApplications(apps: List<JobApplication>)

    @Update
    suspend fun updateJobApplication(app: JobApplication)

    @Delete
    suspend fun deleteJobApplication(app: JobApplication)

    // Coding Sandbox Challenges
    @Query("SELECT * FROM coding_challenges ORDER BY isCompleted ASC, difficulty ASC")
    fun getCodingChallengesFlow(): Flow<List<CodingChallenge>>

    @Query("SELECT * FROM coding_challenges WHERE id = :id LIMIT 1")
    suspend fun getCodingChallenge(id: String): CodingChallenge?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCodingChallenge(challenge: CodingChallenge)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCodingChallenges(challenges: List<CodingChallenge>)

    @Update
    suspend fun updateCodingChallenge(challenge: CodingChallenge)

    // Peer Matches
    @Query("SELECT * FROM peer_matches ORDER BY rating DESC")
    fun getPeerMatchesFlow(): Flow<List<PeerMatch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeerMatches(peers: List<PeerMatch>)

    // Skill Sprints
    @Query("SELECT * FROM skill_sprints ORDER BY isClaimed ASC, currentDay DESC")
    fun getSkillSprintsFlow(): Flow<List<SkillSprint>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkillSprint(sprint: SkillSprint)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkillSprints(sprints: List<SkillSprint>)

    @Update
    suspend fun updateSkillSprint(sprint: SkillSprint)
}
