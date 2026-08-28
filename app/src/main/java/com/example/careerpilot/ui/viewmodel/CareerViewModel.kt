package com.example.careerpilot.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.careerpilot.data.local.AppDatabase
import com.example.careerpilot.data.model.*
import com.example.careerpilot.data.repository.BenchmarkCatalog
import com.example.careerpilot.data.repository.CareerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CareerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CareerRepository

    val userProfile: StateFlow<UserProfile?>
    val userSkills: StateFlow<List<UserSkill>>
    val skillGaps: StateFlow<List<SkillGap>>
    val activeRoadmap: StateFlow<Roadmap?>
    val roadmapItems: StateFlow<List<RoadmapItem>>
    val projects: StateFlow<List<PortfolioProject>>
    val latestResumeAudit: StateFlow<ResumeAudit?>
    val resumeAudits: StateFlow<List<ResumeAudit>>
    val interviews: StateFlow<List<InterviewSession>>
    val learningResources: StateFlow<List<LearningResource>>
    val integrations: StateFlow<List<IntegrationAccount>>
    val recentAnalytics: StateFlow<List<AnalyticsEvent>>
    val auditIssues: StateFlow<List<AuditIssue>>
    val jobPostings: StateFlow<List<TargetJobPosting>>
    val jobMatches: StateFlow<List<JobMatchResult>>

    private val _selectedJobPosting = MutableStateFlow<TargetJobPosting?>(null)
    val selectedJobPosting: StateFlow<TargetJobPosting?> = _selectedJobPosting.asStateFlow()

    private val _activeJobMatch = MutableStateFlow<JobMatchResult?>(null)
    val activeJobMatch: StateFlow<JobMatchResult?> = _activeJobMatch.asStateFlow()

    private val _bulletAnalysis = MutableStateFlow<BulletAnalysis?>(null)
    val bulletAnalysis: StateFlow<BulletAnalysis?> = _bulletAnalysis.asStateFlow()

    private val _conversationalMessages = MutableStateFlow<List<ConversationMessage>>(emptyList())
    val conversationalMessages: StateFlow<List<ConversationMessage>> = _conversationalMessages.asStateFlow()

    private val _isConversationalMode = MutableStateFlow(true)
    val isConversationalMode: StateFlow<Boolean> = _isConversationalMode.asStateFlow()

    private val _currentProbeChallenge = MutableStateFlow<ProbingChallenge?>(null)
    val currentProbeChallenge: StateFlow<ProbingChallenge?> = _currentProbeChallenge.asStateFlow()

    private val _auditSummary = MutableStateFlow(AuditScoreSummary())
    val auditSummary: StateFlow<AuditScoreSummary> = _auditSummary.asStateFlow()

    private val _nextBestAction = MutableStateFlow<NextBestAction?>(null)
    val nextBestAction: StateFlow<NextBestAction?> = _nextBestAction.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _activeInterviewSession = MutableStateFlow<InterviewSession?>(null)
    val activeInterviewSession: StateFlow<InterviewSession?> = _activeInterviewSession.asStateFlow()

    private val _currentInterviewQuestionIndex = MutableStateFlow(0)
    val currentInterviewQuestionIndex: StateFlow<Int> = _currentInterviewQuestionIndex.asStateFlow()

    private val _interviewAnswers = MutableStateFlow<List<InterviewAnswer>>(emptyList())
    val interviewAnswers: StateFlow<List<InterviewAnswer>> = _interviewAnswers.asStateFlow()

    private val _lastEvaluation = MutableStateFlow<InterviewAnswer?>(null)
    val lastEvaluation: StateFlow<InterviewAnswer?> = _lastEvaluation.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CareerRepository(db.careerDao())

        userProfile = repository.userProfileFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), null
        )
        userSkills = repository.userSkillsFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        skillGaps = repository.skillGapsFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        activeRoadmap = repository.activeRoadmapFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), null
        )
        roadmapItems = repository.roadmapItemsFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        projects = repository.projectsFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        latestResumeAudit = repository.latestResumeAuditFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), null
        )
        resumeAudits = repository.resumeAuditsFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        interviews = repository.interviewsFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        learningResources = repository.learningResourcesFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        integrations = repository.integrationsFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        recentAnalytics = repository.recentAnalyticsFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        auditIssues = repository.auditIssuesFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        jobPostings = repository.jobPostingsFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        jobMatches = repository.jobMatchesFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
            val summary = repository.recalibrateAudit()
            _auditSummary.value = summary
            
            // Set initial selected job posting
            val postings = repository.jobPostingsFlow.first()
            if (postings.isNotEmpty()) {
                _selectedJobPosting.value = postings.first()
                val match = repository.recalculateJobMatch(postings.first().id)
                _activeJobMatch.value = match
            }

            refreshNextBestAction()
        }

        viewModelScope.launch {
            auditIssues.collect { issues ->
                val profile = userProfile.value
                val baseReadiness = profile?.readinessScore ?: 76
                val openIssues = issues.filter { it.status != "RESOLVED" }
                val totalDemerits = openIssues.sumOf { it.scoreImpact }
                val netScore = kotlin.math.max(0, kotlin.math.min(100, baseReadiness + totalDemerits))
                val critical = openIssues.count { it.severity == "CRITICAL" }
                val high = openIssues.count { it.severity == "HIGH" }
                val medium = openIssues.count { it.severity == "MEDIUM" }
                val low = openIssues.count { it.severity == "LOW" }
                val resolved = issues.count { it.status == "RESOLVED" }

                val current = _auditSummary.value
                _auditSummary.value = current.copy(
                    readinessScore = baseReadiness,
                    netAuditScore = netScore,
                    totalDemerits = totalDemerits,
                    criticalCount = critical,
                    highCount = high,
                    mediumCount = medium,
                    lowCount = low,
                    resolvedCount = resolved,
                    totalIssuesCount = issues.size,
                    lastEvaluatedAt = System.currentTimeMillis()
                )
            }
        }
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    fun recalibrateAudit() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val summary = repository.recalibrateAudit()
                _auditSummary.value = summary
                refreshNextBestAction()
                _userMessage.value = "Red flag audit refreshed. Net readiness: ${summary.netAuditScore}%."
            } catch (e: Exception) {
                _userMessage.value = "Recalibration failed: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun updateAuditIssueStatus(issueId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateAuditIssueStatus(issueId, newStatus)
            refreshNextBestAction()
            _userMessage.value = "Issue status updated to $newStatus."
        }
    }

    fun resolveAuditIssue(issueId: String) {
        updateAuditIssueStatus(issueId, "RESOLVED")
    }

    fun refreshNextBestAction() {
        viewModelScope.launch {
            _nextBestAction.value = repository.getNextBestAction()
        }
    }

    fun updateProfile(
        fullName: String,
        headline: String,
        bio: String,
        location: String,
        education: String,
        experienceYears: Float,
        targetRole: String,
        targetIndustry: String,
        targetSalary: String,
        targetCompanyTier: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            val updated = current.copy(
                fullName = fullName,
                headline = headline,
                bio = bio,
                location = location,
                education = education,
                experienceYears = experienceYears,
                targetRole = targetRole,
                targetIndustry = targetIndustry,
                targetSalary = targetSalary,
                targetCompanyTier = targetCompanyTier
            )
            repository.updateProfile(updated)
            refreshNextBestAction()
            _userMessage.value = "Profile updated and calibrated for $targetRole."
        }
    }

    fun runCareerAnalysis(targetRole: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                repository.recalibrateSkillGaps(targetRole)
                refreshNextBestAction()
                _userMessage.value = "Career readiness calibrated successfully."
            } catch (e: Exception) {
                _userMessage.value = "Analysis failed: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun addOrUpdateSkill(skillName: String, category: String, level: Int) {
        viewModelScope.launch {
            repository.addOrUpdateUserSkill(
                UserSkill(
                    skillName = skillName,
                    category = category,
                    proficiencyLevel = level,
                    verified = level >= 4
                )
            )
            refreshNextBestAction()
            _userMessage.value = "Skill '$skillName' updated (Level $level/5)."
        }
    }

    fun deleteSkill(skill: UserSkill) {
        viewModelScope.launch {
            repository.deleteUserSkill(skill)
            refreshNextBestAction()
            _userMessage.value = "Skill '${skill.skillName}' removed."
        }
    }

    fun generateRoadmap(targetRole: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                repository.generateRoadmapForRole(targetRole)
                refreshNextBestAction()
                _userMessage.value = "Generated 3-phase trajectory for $targetRole."
            } catch (e: Exception) {
                _userMessage.value = "Failed to generate roadmap: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun toggleRoadmapTask(taskId: Long) {
        viewModelScope.launch {
            repository.toggleRoadmapItem(taskId)
            refreshNextBestAction()
        }
    }

    fun analyzeResume(text: String, filename: String = "Uploaded_Resume.pdf") {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val audit = repository.analyzeResumeText(text, filename)
                refreshNextBestAction()
                _userMessage.value = "Resume audit completed. Score: ${audit.overallScore}%."
            } catch (e: Exception) {
                _userMessage.value = "Resume analysis failed: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun startInterview(difficulty: String = "Intermediate") {
        viewModelScope.launch {
            val profile = userProfile.value
            val role = profile?.targetRole ?: "Full Stack Engineer"
            val session = repository.startInterviewSession(role, difficulty)
            _activeInterviewSession.value = session
            _currentInterviewQuestionIndex.value = 0
            _interviewAnswers.value = emptyList()
            _lastEvaluation.value = null
        }
    }

    fun submitInterviewAnswer(answerText: String) {
        val session = _activeInterviewSession.value ?: return
        val qIndex = _currentInterviewQuestionIndex.value
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val answer = repository.submitInterviewAnswer(session.id, qIndex, answerText)
                _lastEvaluation.value = answer
                _interviewAnswers.value = _interviewAnswers.value + answer

                if (qIndex + 1 < BenchmarkCatalog.INTERVIEW_QUESTIONS.size) {
                    _currentInterviewQuestionIndex.value = qIndex + 1
                } else {
                    _userMessage.value = "Interview completed! Overall score: ${session.overallScore}%."
                }
                refreshNextBestAction()
            } catch (e: Exception) {
                _userMessage.value = "Error evaluating answer: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun exitActiveInterview() {
        _activeInterviewSession.value = null
        _currentInterviewQuestionIndex.value = 0
        _lastEvaluation.value = null
    }

    fun addPortfolioProject(
        title: String,
        description: String,
        repoUrl: String,
        liveUrl: String,
        status: String,
        technologies: String,
        skillsTargeted: String
    ) {
        viewModelScope.launch {
            repository.addProject(
                PortfolioProject(
                    title = title,
                    description = description,
                    repositoryUrl = repoUrl,
                    liveUrl = liveUrl,
                    status = status,
                    technologies = technologies,
                    skillsTargeted = skillsTargeted
                )
            )
            refreshNextBestAction()
            _userMessage.value = "Project '$title' added to portfolio."
        }
    }

    fun deletePortfolioProject(project: PortfolioProject) {
        viewModelScope.launch {
            repository.deleteProject(project)
            refreshNextBestAction()
            _userMessage.value = "Project deleted."
        }
    }

    fun toggleLearningCompleted(resource: LearningResource) {
        viewModelScope.launch {
            repository.toggleLearningCompleted(resource)
            refreshNextBestAction()
        }
    }

    fun toggleIntegration(provider: String, username: String) {
        viewModelScope.launch {
            repository.toggleIntegration(provider, username)
            refreshNextBestAction()
            _userMessage.value = "Integration state updated."
        }
    }

    // === FEATURE 1: JOB DESCRIPTION MATCHER ===
    fun selectJobPosting(posting: TargetJobPosting) {
        _selectedJobPosting.value = posting
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val match = repository.recalculateJobMatch(posting.id)
                _activeJobMatch.value = match
            } catch (e: Exception) {
                _userMessage.value = "Error matching job: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun matchCustomJobDescription(
        company: String,
        title: String,
        level: String,
        minExp: Float,
        jdText: String
    ) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val match = repository.analyzeCustomJobDescription(
                    company = company,
                    title = title,
                    level = level,
                    minExp = minExp,
                    jdText = jdText
                )
                _activeJobMatch.value = match
                _userMessage.value = "Calculated ${match.matchScore}% match against $company."
            } catch (e: Exception) {
                _userMessage.value = "Error analyzing JD: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    // === FEATURE 2: AI RESUME BULLET REWRITER ===
    fun analyzeResumeBullet(bulletText: String) {
        val role = userProfile.value?.targetRole ?: "Full Stack Engineer"
        val analysis = repository.analyzeBullet(bulletText, role)
        _bulletAnalysis.value = analysis
    }

    fun clearBulletAnalysis() {
        _bulletAnalysis.value = null
    }

    fun applyBulletRewrite(originalBullet: String, newBulletText: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                repository.applyBulletReplacement(originalBullet, newBulletText)
                _bulletAnalysis.value = null
                _userMessage.value = "Applied high-impact X-Y-Z bullet rewrite to resume!"
                refreshNextBestAction()
            } catch (e: Exception) {
                _userMessage.value = "Error updating resume: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    // === FEATURE 3: CONVERSATIONAL PROBING INTERVIEW ===
    fun startConversationalInterview(session: InterviewSession) {
        _activeInterviewSession.value = session
        _conversationalMessages.value = listOf(
            ConversationMessage(
                id = "init_msg",
                sender = "AI",
                content = """
                    Welcome to the Deep Technical Probing Interview session for **${session.roleTarget}** (${session.difficulty} Level).
                    
                    I will evaluate your initial system design and coding choices, and then pose context-aware follow-up challenges on concurrency, failure recovery, and architectural trade-offs.
                    
                    **Round 1 Question**:
                    ${BenchmarkCatalog.INTERVIEW_QUESTIONS.firstOrNull()?.questionText ?: "Describe how you architected your highest throughput distributed service, and how you handled data consistency across multiple services."}
                """.trimIndent(),
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun submitConversationalTurn(sessionId: String, question: String, answerText: String, isFollowUp: Boolean) {
        viewModelScope.launch {
            if (answerText.isBlank()) return@launch
            
            // Add user message
            val userMsg = ConversationMessage(
                id = "user_${System.currentTimeMillis()}",
                sender = "USER",
                content = answerText,
                timestamp = System.currentTimeMillis()
            )
            _conversationalMessages.value = _conversationalMessages.value + userMsg

            _isAnalyzing.value = true
            try {
                val (aiReply, score) = repository.processInterviewProbingTurn(
                    sessionId = sessionId,
                    question = question,
                    userAnswer = answerText,
                    isFollowUp = isFollowUp
                )
                _conversationalMessages.value = _conversationalMessages.value + aiReply
            } catch (e: Exception) {
                _userMessage.value = "Error processing turn: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
}

