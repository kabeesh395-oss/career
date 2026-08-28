package com.example.careerpilot.data.repository

import com.example.careerpilot.data.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object AuditEngine {

    val DEFAULT_CONFIG = AuditPenaltyConfig()

    /**
     * Evaluates candidate profile, skills, projects, resume audits, interview performance,
     * and external telemetry integrations to produce a deduplicated, deterministic audit.
     */
    fun evaluateCandidate(
        profile: UserProfile?,
        skills: List<UserSkill>,
        projects: List<PortfolioProject>,
        latestResume: ResumeAudit?,
        interviewAnswers: List<InterviewAnswer>,
        integrations: List<IntegrationAccount>,
        existingIssues: List<AuditIssue> = emptyList(),
        config: AuditPenaltyConfig = DEFAULT_CONFIG
    ): Pair<List<AuditIssue>, AuditScoreSummary> {
        val targetRole = profile?.targetRole ?: "Full Stack Engineer"
        val experienceYears = profile?.experienceYears ?: 2.0f
        val isStudentOrJunior = experienceYears < 2.0f

        val existingStatusMap = existingIssues.associate { it.id to it.status }
        val generatedIssues = mutableListOf<AuditIssue>()

        // -------------------------------------------------------------
        // 1. PRODUCTION EVIDENCE DIMENSION
        // -------------------------------------------------------------
        val allProjectText = projects.joinToString(" ") { "${it.title} ${it.description} ${it.technologies} ${it.skillsTargeted}" }.lowercase()
        val hasCompletedProject = projects.any { it.status.equals("completed", ignoreCase = true) }
        val hasLiveDeployment = projects.any { it.liveUrl.isNotBlank() && it.liveUrl.startsWith("http") }
        val hasRepo = projects.any { it.repositoryUrl.isNotBlank() }

        val hasCicdOrTesting = allProjectText.contains("ci/cd") || allProjectText.contains("github actions") ||
                allProjectText.contains("testing") || allProjectText.contains("unit test") ||
                allProjectText.contains("test") || allProjectText.contains("tdd") ||
                allProjectText.contains("docker") || allProjectText.contains("container")

        val hasMonitoringOrMetrics = allProjectText.contains("prometheus") || allProjectText.contains("grafana") ||
                allProjectText.contains("datadog") || allProjectText.contains("telemetry") ||
                allProjectText.contains("metric") || allProjectText.contains("latency") ||
                allProjectText.contains("p99") || allProjectText.contains("benchmark")

        // Rule A: Production Deployment Evidence
        if (!hasLiveDeployment) {
            val status = existingStatusMap["audit_prod_deployment"] ?: "OPEN"
            generatedIssues.add(
                AuditIssue(
                    id = "audit_prod_deployment",
                    ruleId = "RULE_PROD_DEPLOYMENT",
                    category = "Production Evidence",
                    title = "No Live Production Deployment Evidence",
                    severity = if (isStudentOrJunior) "MEDIUM" else "HIGH",
                    scoreImpact = if (status == "RESOLVED") 0 else config.missingDeploymentEvidence,
                    evidence = if (projects.isEmpty()) "No portfolio projects recorded." else "None of the ${projects.size} projects contain a verified live deployment URL.",
                    explanation = "Tier-1 engineering hiring teams expect observable live demonstrators rather than code confined to local localhost setups.",
                    recommendedFix = "Deploy at least one portfolio service to a live host (e.g. Cloud Run, Vercel, AWS, or Fly.io) and record the live URL.",
                    estimatedEffort = "1–2 hours",
                    verificationRequirement = "Add a valid HTTPS live demonstrator URL to a completed portfolio project.",
                    status = status,
                    evidenceStatus = if (projects.isEmpty()) "EVIDENCE_UNAVAILABLE" else "UNVERIFIED",
                    confidence = "HIGH",
                    targetRoute = "projects",
                    ctaText = "Deploy Project"
                )
            )
        }

        // Rule B: CI/CD & Automated Testing Deficit
        if (!hasCicdOrTesting) {
            val status = existingStatusMap["audit_prod_cicd"] ?: "OPEN"
            generatedIssues.add(
                AuditIssue(
                    id = "audit_prod_cicd",
                    ruleId = "RULE_PROD_CICD_AUTOMATION",
                    category = "Production Evidence",
                    title = "Production CI/CD & Automated Testing Deficit",
                    severity = if (isStudentOrJunior) "MEDIUM" else "HIGH",
                    scoreImpact = if (status == "RESOLVED") 0 else config.missingCicdTesting,
                    evidence = "No CI/CD pipelines, GitHub Actions, Docker builds, or automated test coverage benchmarks detected in project descriptions.",
                    explanation = "Modern engineering standards mandate automated regression safeguards and containerized delivery pipelines.",
                    recommendedFix = "Implement a GitHub Actions workflow with unit/integration testing steps and document test coverage metrics.",
                    estimatedEffort = "2–3 hours",
                    verificationRequirement = "Include Docker/CI-CD or testing keywords and repository links in your project portfolio.",
                    status = status,
                    evidenceStatus = "UNVERIFIED",
                    confidence = "HIGH",
                    targetRoute = "projects",
                    ctaText = "Configure CI/CD"
                )
            )
        }

        // -------------------------------------------------------------
        // 2. RESUME QUALITY & ATS COMPLIANCE
        // -------------------------------------------------------------
        if (latestResume == null) {
            val status = existingStatusMap["audit_res_missing"] ?: "OPEN"
            generatedIssues.add(
                AuditIssue(
                    id = "audit_res_missing",
                    ruleId = "RULE_RES_AUDIT_MISSING",
                    category = "Resume Quality",
                    title = "No ATS Resume Audit On Record",
                    severity = "HIGH",
                    scoreImpact = if (status == "RESOLVED") 0 else -4,
                    evidence = "No resume has been uploaded or evaluated against $targetRole benchmarks.",
                    explanation = "Without an ATS parsing audit, formatting incompatibilities and keyword deficiencies cannot be diagnosed.",
                    recommendedFix = "Paste or upload your latest technical resume in the ATS Resume module to run automated extraction.",
                    estimatedEffort = "15 minutes",
                    verificationRequirement = "Run an ATS Resume Audit to generate keyword and impact benchmarks.",
                    status = status,
                    evidenceStatus = "EVIDENCE_UNAVAILABLE",
                    confidence = "HIGH",
                    targetRoute = "resume",
                    ctaText = "Upload Resume"
                )
            )
        } else {
            val resumeLower = latestResume.rawText.lowercase()
            val hasMetrics = resumeLower.contains("%") || resumeLower.contains("$") ||
                    resumeLower.contains("increased") || resumeLower.contains("reduced") ||
                    resumeLower.contains("scaled") || resumeLower.contains("optimized") ||
                    resumeLower.contains("million") || resumeLower.contains("latency")

            if (!hasMetrics || latestResume.impactScore < 70) {
                val status = existingStatusMap["audit_res_metrics"] ?: "OPEN"
                generatedIssues.add(
                    AuditIssue(
                        id = "audit_res_metrics",
                        ruleId = "RULE_RES_METRICS_XYZ",
                        category = "Resume Quality",
                        title = "Missing Measurable Impact & Metrics",
                        severity = "HIGH",
                        scoreImpact = if (status == "RESOLVED") 0 else config.missingResumeMetrics,
                        evidence = "Resume experience bullets lack quantifiable business metrics, throughput numbers, or latency figures.",
                        explanation = "Top-tier recruiters screen for outcomes rather than task lists. Phrases like 'Built an AI app' lack proof of engineering scope.",
                        recommendedFix = "Apply Google's X-Y-Z formula: 'Accomplished [X] as measured by [Y], by doing [Z]' (e.g. 'Reduced p99 query latency by 45% using Redis caching').",
                        estimatedEffort = "45 minutes",
                        verificationRequirement = "Re-analyze resume containing explicit percentages, time savings, or scale figures.",
                        status = status,
                        evidenceStatus = "PARTIALLY_VERIFIED",
                        confidence = "HIGH",
                        targetRoute = "resume",
                        ctaText = "Refine Resume"
                    )
                )
            }

            if (latestResume.brevityScore < 70) {
                val status = existingStatusMap["audit_res_brevity"] ?: "OPEN"
                generatedIssues.add(
                    AuditIssue(
                        id = "audit_res_brevity",
                        ruleId = "RULE_ATS_FORMAT_BREVITY",
                        category = "ATS Compatibility",
                        title = "Resume Word Count & Density Imbalance",
                        severity = "LOW",
                        scoreImpact = if (status == "RESOLVED") 0 else config.atsStructureDeficit,
                        evidence = "Resume brevity score is ${latestResume.brevityScore}%. Text length deviates from optimal 350–700 word density.",
                        explanation = "Overly brief resumes fail keyword parsing; overly verbose multi-page text gets deprioritized by automated ATS scanners.",
                        recommendedFix = "Condense redundant sections, eliminate generic bullet points, and focus on high-signal technical deliverables.",
                        estimatedEffort = "30 minutes",
                        verificationRequirement = "Calibrate resume length to a concise single-page structure (350–700 words).",
                        status = status,
                        evidenceStatus = "VERIFIED",
                        confidence = "MEDIUM",
                        targetRoute = "resume",
                        ctaText = "Adjust Length"
                    )
                )
            }
        }

        // -------------------------------------------------------------
        // 3. SKILL DEPTH VS BREADTH & VERIFICATION
        // -------------------------------------------------------------
        val advancedKeywords = listOf(
            "distributed systems", "system design", "sharding", "replication",
            "consensus", "fault tolerance", "observability", "microservices",
            "caching", "database internals"
        )

        val unverifiedAdvancedSkills = skills.filter { userSkill ->
            val nameLower = userSkill.skillName.lowercase()
            val isAdvanced = advancedKeywords.any { nameLower.contains(it) } || userSkill.proficiencyLevel >= 4
            isAdvanced && !userSkill.verified && projects.none { it.technologies.lowercase().contains(nameLower) || it.skillsTargeted.lowercase().contains(nameLower) }
        }

        if (unverifiedAdvancedSkills.isNotEmpty() && !isStudentOrJunior) {
            val status = existingStatusMap["audit_skill_unverified"] ?: "OPEN"
            val skillNames = unverifiedAdvancedSkills.take(3).joinToString(", ") { it.skillName }
            generatedIssues.add(
                AuditIssue(
                    id = "audit_skill_unverified",
                    ruleId = "RULE_SKILL_UNVERIFIED_ADVANCED",
                    category = "Skill Depth",
                    title = "Unverified Advanced Competency Claims",
                    severity = "MEDIUM",
                    scoreImpact = if (status == "RESOLVED") 0 else config.unverifiedAdvancedSkill,
                    evidence = "Claims proficiency in $skillNames without corresponding portfolio repository proof or code evidence.",
                    explanation = "Listing senior-level concepts without implementation proof creates credibility skepticism during technical deep-dives.",
                    recommendedFix = "Build a dedicated code sample, link a GitHub repository, or complete an AI Mock Interview focusing on $skillNames.",
                    estimatedEffort = "3–4 hours",
                    verificationRequirement = "Tag projects with these competencies or verify proficiency via technical interview assessments.",
                    status = status,
                    evidenceStatus = "UNVERIFIED",
                    confidence = "HIGH",
                    targetRoute = "career",
                    ctaText = "Verify Skills"
                )
            )
        }

        // -------------------------------------------------------------
        // 4. GITHUB / REPOSITORY TELEMETRY EVIDENCE
        // -------------------------------------------------------------
        val githubIntegration = integrations.find { it.provider == "github" }
        val isGithubConnected = githubIntegration?.isConnected == true

        if (!isGithubConnected || !hasRepo) {
            val status = existingStatusMap["audit_github_proof"] ?: "OPEN"
            generatedIssues.add(
                AuditIssue(
                    id = "audit_github_proof",
                    ruleId = "RULE_GITHUB_ACTIVITY_DEFICIT",
                    category = "GitHub Evidence",
                    title = "Missing GitHub Repository Proof",
                    severity = "HIGH",
                    scoreImpact = if (status == "RESOLVED") 0 else config.missingGithubEvidence,
                    evidence = if (!isGithubConnected) "GitHub profile is not connected." else "No public repositories linked to your active portfolio projects.",
                    explanation = "Engineering interviewers look at commit consistency, clean code organization, and README documentation.",
                    recommendedFix = "Sync your GitHub account in External Integrations and link public repository URLs to your portfolio deliverables.",
                    estimatedEffort = "15 minutes",
                    verificationRequirement = "Connect active GitHub profile and ensure repositories have documentation and commit history.",
                    status = status,
                    evidenceStatus = if (!isGithubConnected) "EVIDENCE_UNAVAILABLE" else "UNVERIFIED",
                    confidence = "HIGH",
                    targetRoute = "integrations",
                    ctaText = "Connect GitHub"
                )
            )
        }

        // -------------------------------------------------------------
        // 5. MOCK INTERVIEW & SYSTEM DESIGN AUDIT
        // -------------------------------------------------------------
        if (interviewAnswers.isEmpty()) {
            val status = existingStatusMap["audit_interview_missing"] ?: "OPEN"
            generatedIssues.add(
                AuditIssue(
                    id = "audit_interview_missing",
                    ruleId = "RULE_INTERVIEW_TELEMETRY_DEFICIT",
                    category = "Mock Interview",
                    title = "No Technical Interview Telemetry Recorded",
                    severity = if (isStudentOrJunior) "MEDIUM" else "HIGH",
                    scoreImpact = if (status == "RESOLVED") 0 else config.weakInterviewTradeoffs,
                    evidence = "No completed mock technical interview sessions recorded.",
                    explanation = "Interview readiness cannot be validated without simulated technical communication and problem-solving attempts.",
                    recommendedFix = "Complete at least one AI Mock Interview session to establish verbal baseline clarity and technical depth scores.",
                    estimatedEffort = "15–20 minutes",
                    verificationRequirement = "Submit responses to technical simulation questions in Mock AI module.",
                    status = status,
                    evidenceStatus = "EVIDENCE_UNAVAILABLE",
                    confidence = "HIGH",
                    targetRoute = "interview",
                    ctaText = "Start Mock Session"
                )
            )
        } else {
            val avgTechnical = interviewAnswers.map { it.technicalScore }.average()
            val avgClarity = interviewAnswers.map { it.clarityScore }.average()

            // System Design Specific Audit
            val systemDesignAnswers = interviewAnswers.filter { it.category.contains("System Design", ignoreCase = true) || it.category.contains("Architecture", ignoreCase = true) }
            val hasWeakSystemDesign = systemDesignAnswers.any { it.technicalScore < 70 } || (systemDesignAnswers.isEmpty() && !isStudentOrJunior)

            if (hasWeakSystemDesign && !isStudentOrJunior) {
                val status = existingStatusMap["audit_interview_sysdesign"] ?: "OPEN"
                generatedIssues.add(
                    AuditIssue(
                        id = "audit_interview_sysdesign",
                        ruleId = "RULE_INTERVIEW_SYSTEM_DESIGN_DEPTH",
                        category = "System Design",
                        title = "Weak System Design & Trade-Off Articulation",
                        severity = "CRITICAL",
                        scoreImpact = if (status == "RESOLVED") 0 else config.weakSystemDesignDepth,
                        evidence = if (systemDesignAnswers.isEmpty()) "No system design assessment recorded for Senior/Staff tier role." else "System design answer scored below rubric expectation (average technical score ${avgTechnical.roundToInt()}%).",
                        explanation = "Target role ($targetRole) requires structured discussion of latency, availability, consistency (CAP theorem), caching, and failure recovery.",
                        recommendedFix = "Practice answering distributed system questions focusing on trade-offs (e.g. read-heavy vs write-heavy, async queues, idempotency keys).",
                        estimatedEffort = "1–2 hours",
                        verificationRequirement = "Score >= 80% on a System Design Mock Interview question.",
                        status = status,
                        evidenceStatus = if (systemDesignAnswers.isEmpty()) "EVIDENCE_UNAVAILABLE" else "PARTIALLY_VERIFIED",
                        confidence = "HIGH",
                        targetRoute = "interview",
                        ctaText = "Practice System Design"
                    )
                )
            } else if (avgTechnical < 70 && interviewAnswers.isNotEmpty()) {
                val status = existingStatusMap["audit_interview_depth"] ?: "OPEN"
                generatedIssues.add(
                    AuditIssue(
                        id = "audit_interview_depth",
                        ruleId = "RULE_INTERVIEW_GENERAL_DEPTH",
                        category = "Mock Interview",
                        title = "Technical Articulation Below Target Threshold",
                        severity = "MEDIUM",
                        scoreImpact = if (status == "RESOLVED") 0 else config.weakInterviewTradeoffs,
                        evidence = "Recent interview answers scored average technical depth of ${avgTechnical.roundToInt()}% (target: >= 75%).",
                        explanation = "Responses missed key technical keywords such as connection pooling, query plans, or immutable state flows.",
                        recommendedFix = "Review feedback rubrics on past interview attempts and practice structured answers using STAR framework.",
                        estimatedEffort = "45 minutes",
                        verificationRequirement = "Submit improved mock interview answers exceeding 75% technical score.",
                        status = status,
                        evidenceStatus = "VERIFIED",
                        confidence = "HIGH",
                        targetRoute = "interview",
                        ctaText = "Retake Interview"
                    )
                )
            }
        }

        // Deduplication: Filter duplicate ruleIds if any accidentally collided
        val deduplicatedIssues = generatedIssues.distinctBy { it.ruleId }

        // Calculate Totals & Summary
        val openIssues = deduplicatedIssues.filter { it.status != "RESOLVED" }
        val totalDemerits = openIssues.sumOf { it.scoreImpact }
        val baseReadiness = profile?.readinessScore ?: 76
        val netAuditScore = max(0, min(100, baseReadiness + totalDemerits))

        val criticalCount = openIssues.count { it.severity == "CRITICAL" }
        val highCount = openIssues.count { it.severity == "HIGH" }
        val mediumCount = openIssues.count { it.severity == "MEDIUM" }
        val lowCount = openIssues.count { it.severity == "LOW" }
        val resolvedCount = deduplicatedIssues.count { it.status == "RESOLVED" }

        // Evidence coverage calculation: ratio of verified or present data points
        var evidencePoints = 0
        val maxPoints = 6
        if (projects.isNotEmpty()) evidencePoints++
        if (hasLiveDeployment) evidencePoints++
        if (latestResume != null) evidencePoints++
        if (isGithubConnected) evidencePoints++
        if (interviewAnswers.isNotEmpty()) evidencePoints++
        if (skills.any { it.verified }) evidencePoints++

        val coveragePercent = ((evidencePoints.toFloat() / maxPoints.toFloat()) * 100f).roundToInt()
        val profileConfidence = when {
            coveragePercent >= 75 -> "HIGH"
            coveragePercent >= 45 -> "MEDIUM"
            else -> "LOW"
        }

        val summary = AuditScoreSummary(
            readinessScore = baseReadiness,
            netAuditScore = netAuditScore,
            totalDemerits = totalDemerits,
            criticalCount = criticalCount,
            highCount = highCount,
            mediumCount = mediumCount,
            lowCount = lowCount,
            resolvedCount = resolvedCount,
            totalIssuesCount = deduplicatedIssues.size,
            evidenceCoveragePercent = coveragePercent,
            profileConfidence = profileConfidence,
            isOfflineEvaluated = true,
            lastEvaluatedAt = System.currentTimeMillis()
        )

        return Pair(deduplicatedIssues, summary)
    }
}
