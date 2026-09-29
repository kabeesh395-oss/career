package com.example.careerpilot.data.local

import androidx.room.*
import com.example.careerpilot.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CareerDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = :userId LIMIT 1")
    fun getUserProfileFlow(userId: String): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = :userId LIMIT 1")
    suspend fun getUserProfile(userId: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    // User Skills
    @Query("SELECT * FROM user_skills WHERE userId = :userId ORDER BY category ASC, skillName ASC")
    fun getUserSkillsFlow(userId: String): Flow<List<UserSkill>>

    @Query("SELECT * FROM user_skills WHERE userId = :userId")
    suspend fun getUserSkills(userId: String): List<UserSkill>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSkill(skill: UserSkill)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSkills(skills: List<UserSkill>)

    @Delete
    suspend fun deleteUserSkill(skill: UserSkill)

    @Query("DELETE FROM user_skills WHERE userId = :userId")
    suspend fun clearUserSkills(userId: String)

    // Skill Gaps
    @Query("SELECT * FROM skill_gaps WHERE userId = :userId ORDER BY gapScore DESC, priority DESC")
    fun getSkillGapsFlow(userId: String): Flow<List<SkillGap>>

    @Query("SELECT * FROM skill_gaps WHERE userId = :userId ORDER BY gapScore DESC")
    suspend fun getSkillGaps(userId: String): List<SkillGap>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkillGaps(gaps: List<SkillGap>)

    @Query("DELETE FROM skill_gaps WHERE userId = :userId")
    suspend fun clearSkillGaps(userId: String)

    // Roadmaps
    @Query("SELECT * FROM roadmaps WHERE userId = :userId ORDER BY id DESC LIMIT 1")
    fun getActiveRoadmapFlow(userId: String): Flow<Roadmap?>

    @Query("SELECT * FROM roadmaps WHERE userId = :userId ORDER BY id DESC LIMIT 1")
    suspend fun getActiveRoadmap(userId: String): Roadmap?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRoadmap(roadmap: Roadmap)

    @Query("DELETE FROM roadmaps WHERE userId = :userId")
    suspend fun clearRoadmaps(userId: String)

    // Roadmap Items
    @Query("SELECT * FROM roadmap_items WHERE userId = :userId ORDER BY phaseNumber ASC, orderIndex ASC")
    fun getRoadmapItemsFlow(userId: String): Flow<List<RoadmapItem>>

    @Query("SELECT * FROM roadmap_items WHERE userId = :userId ORDER BY phaseNumber ASC, orderIndex ASC")
    suspend fun getRoadmapItems(userId: String): List<RoadmapItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadmapItems(items: List<RoadmapItem>)

    @Update
    suspend fun updateRoadmapItem(item: RoadmapItem)

    @Query("DELETE FROM roadmap_items WHERE userId = :userId")
    suspend fun clearRoadmapItems(userId: String)

    // Portfolio Projects
    @Query("SELECT * FROM portfolio_projects WHERE userId = :userId ORDER BY id DESC")
    fun getProjectsFlow(userId: String): Flow<List<PortfolioProject>>

    @Query("SELECT * FROM portfolio_projects WHERE userId = :userId ORDER BY id DESC")
    suspend fun getProjects(userId: String): List<PortfolioProject>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: PortfolioProject)

    @Update
    suspend fun updateProject(project: PortfolioProject)

    @Delete
    suspend fun deleteProject(project: PortfolioProject)

    @Query("DELETE FROM portfolio_projects WHERE userId = :userId")
    suspend fun clearProjects(userId: String)

    // Resume Audits
    @Query("SELECT * FROM resume_audits WHERE userId = :userId ORDER BY createdAt DESC")
    fun getResumeAuditsFlow(userId: String): Flow<List<ResumeAudit>>

    @Query("SELECT * FROM resume_audits WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getResumeAudits(userId: String): List<ResumeAudit>

    @Query("SELECT * FROM resume_audits WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    fun getLatestResumeAuditFlow(userId: String): Flow<ResumeAudit?>

    @Query("SELECT * FROM resume_audits WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestResumeAudit(userId: String): ResumeAudit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResumeAudit(audit: ResumeAudit)

    @Query("DELETE FROM resume_audits WHERE userId = :userId")
    suspend fun clearResumeAudits(userId: String)

    // Interview Sessions & Answers
    @Query("SELECT * FROM interview_sessions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getInterviewsFlow(userId: String): Flow<List<InterviewSession>>

    @Query("SELECT * FROM interview_sessions WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getInterviews(userId: String): List<InterviewSession>

    @Query("SELECT * FROM interview_sessions WHERE id = :sessionId AND userId = :userId LIMIT 1")
    suspend fun getInterviewSession(sessionId: String, userId: String): InterviewSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateInterview(session: InterviewSession)

    @Query("SELECT * FROM interview_answers WHERE interviewId = :sessionId AND userId = :userId ORDER BY questionNumber ASC")
    fun getInterviewAnswersFlow(sessionId: String, userId: String): Flow<List<InterviewAnswer>>

    @Query("SELECT * FROM interview_answers WHERE interviewId = :sessionId AND userId = :userId ORDER BY questionNumber ASC")
    suspend fun getInterviewAnswers(sessionId: String, userId: String): List<InterviewAnswer>

    @Query("SELECT * FROM interview_answers WHERE userId = :userId ORDER BY submittedAt DESC")
    suspend fun getAllInterviewAnswers(userId: String): List<InterviewAnswer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterviewAnswer(answer: InterviewAnswer)

    // Learning Resources
    @Query("SELECT * FROM learning_resources ORDER BY category ASC, id ASC")
    fun getLearningResourcesFlow(): Flow<List<LearningResource>>

    @Query("SELECT * FROM learning_resources ORDER BY category ASC, id ASC")
    suspend fun getLearningResources(): List<LearningResource>

    @Query("SELECT * FROM learning_resources WHERE id = :id LIMIT 1")
    suspend fun getLearningResource(id: Long): LearningResource?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningResources(resources: List<LearningResource>)

    @Update
    suspend fun updateLearningResource(resource: LearningResource)

    @Query("DELETE FROM learning_resources")
    suspend fun clearLearningResources()

    // Integrations
    @Query("SELECT * FROM integrations WHERE userId = :userId")
    fun getIntegrationsFlow(userId: String): Flow<List<IntegrationAccount>>

    @Query("SELECT * FROM integrations WHERE userId = :userId")
    suspend fun getIntegrations(userId: String): List<IntegrationAccount>

    @Query("SELECT * FROM integrations WHERE provider = :provider AND userId = :userId LIMIT 1")
    suspend fun getIntegration(provider: String, userId: String): IntegrationAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateIntegration(account: IntegrationAccount)

    @Query("DELETE FROM integrations WHERE userId = :userId")
    suspend fun clearIntegrations(userId: String)

    // Audit Issues & Red Flags
    @Query("SELECT * FROM audit_issues WHERE userId = :userId ORDER BY CASE severity WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 WHEN 'LOW' THEN 4 ELSE 5 END, createdAt DESC")
    fun getAuditIssuesFlow(userId: String): Flow<List<AuditIssue>>

    @Query("SELECT * FROM audit_issues WHERE userId = :userId ORDER BY CASE severity WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 WHEN 'LOW' THEN 4 ELSE 5 END, createdAt DESC")
    suspend fun getAuditIssues(userId: String): List<AuditIssue>

    @Query("SELECT * FROM audit_issues WHERE id = :issueId AND userId = :userId LIMIT 1")
    suspend fun getAuditIssue(issueId: String, userId: String): AuditIssue?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditIssue(issue: AuditIssue)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditIssues(issues: List<AuditIssue>)

    @Update
    suspend fun updateAuditIssue(issue: AuditIssue)

    @Delete
    suspend fun deleteAuditIssue(issue: AuditIssue)

    @Query("DELETE FROM audit_issues WHERE userId = :userId")
    suspend fun clearAuditIssues(userId: String)

    // Analytics Events
    @Query("SELECT * FROM analytics_events WHERE userId = :userId ORDER BY timestamp DESC LIMIT 20")
    fun getRecentAnalyticsFlow(userId: String): Flow<List<AnalyticsEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyticsEvent(event: AnalyticsEvent)

    @Query("DELETE FROM analytics_events WHERE userId = :userId")
    suspend fun clearAnalyticsEvents(userId: String)

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

    @Query("SELECT * FROM job_matches WHERE userId = :userId ORDER BY calculatedAt DESC")
    fun getJobMatchesFlow(userId: String): Flow<List<JobMatchResult>>

    @Query("SELECT * FROM job_matches WHERE jobPostingId = :jobPostingId AND userId = :userId ORDER BY calculatedAt DESC LIMIT 1")
    fun getJobMatchForPostingFlow(jobPostingId: String, userId: String): Flow<JobMatchResult?>

    @Query("SELECT * FROM job_matches WHERE jobPostingId = :jobPostingId AND userId = :userId ORDER BY calculatedAt DESC LIMIT 1")
    suspend fun getJobMatchForPosting(jobPostingId: String, userId: String): JobMatchResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobMatchResult(result: JobMatchResult)

    @Query("DELETE FROM job_matches WHERE jobPostingId = :jobPostingId AND userId = :userId")
    suspend fun deleteJobMatchForPosting(jobPostingId: String, userId: String)

    @Query("DELETE FROM job_matches WHERE userId = :userId")
    suspend fun clearJobMatches(userId: String)

    // Job Applications Pipeline CRM
    @Query("SELECT * FROM job_applications WHERE userId = :userId ORDER BY appliedDate DESC")
    fun getJobApplicationsFlow(userId: String): Flow<List<JobApplication>>

    @Query("SELECT * FROM job_applications WHERE userId = :userId ORDER BY appliedDate DESC")
    suspend fun getJobApplications(userId: String): List<JobApplication>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobApplication(app: JobApplication)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobApplications(apps: List<JobApplication>)

    @Update
    suspend fun updateJobApplication(app: JobApplication)

    @Delete
    suspend fun deleteJobApplication(app: JobApplication)

    @Query("DELETE FROM job_applications WHERE userId = :userId")
    suspend fun clearJobApplications(userId: String)

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

    @Query("DELETE FROM peer_matches WHERE id LIKE 'peer_%'")
    suspend fun clearDummyPeerMatches()

    // Skill Sprints
    @Query("SELECT * FROM skill_sprints ORDER BY isClaimed ASC, currentDay DESC")
    fun getSkillSprintsFlow(): Flow<List<SkillSprint>>

    @Query("SELECT * FROM skill_sprints")
    suspend fun getSkillSprints(): List<SkillSprint>

    @Query("SELECT * FROM skill_sprints WHERE id = :id LIMIT 1")
    suspend fun getSkillSprint(id: String): SkillSprint?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkillSprint(sprint: SkillSprint)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkillSprints(sprints: List<SkillSprint>)

    @Update
    suspend fun updateSkillSprint(sprint: SkillSprint)

    // Career Opportunities (Certifications, Hackathons, Fellowships, Open Source, Hiring)
    @Query("SELECT * FROM career_opportunities ORDER BY isFeatured DESC, matchScore DESC, title ASC")
    fun getOpportunitiesFlow(): Flow<List<CareerOpportunity>>

    @Query("SELECT * FROM career_opportunities WHERE category = :category ORDER BY isFeatured DESC, matchScore DESC")
    fun getOpportunitiesByCategoryFlow(category: String): Flow<List<CareerOpportunity>>

    @Query("SELECT * FROM career_opportunities WHERE id = :id LIMIT 1")
    suspend fun getOpportunityById(id: String): CareerOpportunity?

    @Query("SELECT * FROM career_opportunities")
    suspend fun getAllOpportunities(): List<CareerOpportunity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpportunities(opportunities: List<CareerOpportunity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpportunity(opportunity: CareerOpportunity)

    @Update
    suspend fun updateOpportunity(opportunity: CareerOpportunity)

    @Query("UPDATE career_opportunities SET status = :status WHERE id = :id")
    suspend fun updateOpportunityStatus(id: String, status: String)

    @Query("UPDATE career_opportunities SET userNotes = :notes WHERE id = :id")
    suspend fun updateOpportunityNotes(id: String, notes: String)

    @Query("UPDATE career_opportunities SET reminderSet = :reminderSet WHERE id = :id")
    suspend fun updateOpportunityReminder(id: String, reminderSet: Boolean)
}
