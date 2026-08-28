package com.example.careerpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.model.BulletRewriteOption
import com.example.careerpilot.data.model.TargetJobPosting
import com.example.careerpilot.data.repository.ResumeBulletRewriter
import com.example.careerpilot.ui.components.CircularScoreGauge
import com.example.careerpilot.ui.components.GlassCard
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.components.StatusBadge
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResumeAuditScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val latestAudit by viewModel.latestResumeAudit.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val jobPostings by viewModel.jobPostings.collectAsState()
    val selectedPosting by viewModel.selectedJobPosting.collectAsState()
    val activeJobMatch by viewModel.activeJobMatch.collectAsState()
    val bulletAnalysis by viewModel.bulletAnalysis.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: ATS Audit, 1: Job Matcher, 2: X-Y-Z Bullet Rewriter
    val tabTitles = listOf("ATS Audit", "Job Matcher", "X-Y-Z Rewriter")

    var resumeTextInput by remember {
        mutableStateOf(
            """ALEX CHEN — Senior Full Stack & Mobile Engineer
alex.chen@dev.io | github.com/alexchen | linkedin.com/in/alexchen

EXPERIENCE
Software Engineer | CloudScale Systems (2022 – Present)
• Architected and developed high-throughput microservices in Kotlin and TypeScript handling 40M+ daily events.
• Reduced p99 database query latency by 45% by restructuring PostgreSQL indexes and implementing a Redis cache cluster.
• Built reactive cross-platform Android mobile interfaces using Jetpack Compose and Room local persistence with 99.9% crash-free sessions.
• Led migration of legacy build tools to automated CI/CD pipelines with multi-stage Docker containers, reducing build times by 60%.

PROJECTS
• Distributed Task Queue: High-throughput task orchestrator with Redis and Prometheus telemetry.
• Collaborative Canvas: Real-time whiteboard engine using WebSockets and CRDT conflict resolution.

SKILLS
Kotlin, TypeScript, Python, React, Jetpack Compose, Node.js, Express, PostgreSQL, Redis, Docker, Kubernetes, AWS, System Design, REST APIs, GraphQL, TDD, Unit Testing.

EDUCATION
B.S. in Computer Science — University of California (GPA: 3.8/4.0)"""
        )
    }

    // Custom Job Description Dialog State
    var showCustomJdDialog by remember { mutableStateOf(false) }
    var customCompany by remember { mutableStateOf("") }
    var customRoleTitle by remember { mutableStateOf("") }
    var customJdText by remember { mutableStateOf("") }
    var customMinExp by remember { mutableFloatStateOf(3.0f) }

    // Bullet Input State
    var bulletToAnalyzeInput by remember { mutableStateOf(ResumeBulletRewriter.SAMPLE_WEAK_BULLETS.first()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Header
        item {
            Column {
                Text(
                    text = "Resume & Career Intelligence",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ATS scoring, Target Job Matcher, and Google X-Y-Z bullet optimization",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Navigation Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BgCard,
                contentColor = PrimaryBlueGlow,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) PrimaryBlueGlow else TextSecondary,
                                fontSize = 13.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.Assessment
                                    1 -> Icons.Default.WorkOutline
                                    else -> Icons.Default.AutoFixHigh
                                },
                                contentDescription = title,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }

        // ================= TAB 0: ATS AUDIT & SCORING =================
        if (selectedTab == 0) {
            // Latest Audit Results Card (if exists)
            if (latestAudit != null) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("resume_score_card"),
                        borderColor = PrimaryBlueGlow.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            CircularScoreGauge(
                                score = latestAudit!!.overallScore,
                                size = 100.dp,
                                strokeWidth = 8.dp,
                                label = "ATS SCORE"
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                StatusBadge(text = "TARGET: ${latestAudit!!.targetRole}", statusType = "primary")
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "ATS Compatibility Score",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Analyzed against top-tier tech screening algorithms.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3 Dimensional Breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ScoreSubCard(
                                label = "Impact & Metrics",
                                score = latestAudit!!.impactScore,
                                modifier = Modifier.weight(1f)
                            )
                            ScoreSubCard(
                                label = "Brevity & Layout",
                                score = latestAudit!!.brevityScore,
                                modifier = Modifier.weight(1f)
                            )
                            ScoreSubCard(
                                label = "Formatting & Style",
                                score = latestAudit!!.styleScore,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Extracted Keywords
                if (latestAudit!!.skillsDetected.isNotBlank()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Detected Technical Keywords",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val skillsList = latestAudit!!.skillsDetected.split(",").map { it.trim() }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                skillsList.forEach { skill ->
                                    StatusBadge(text = skill, statusType = "primary")
                                }
                            }
                        }
                    }
                }

                // Strengths & Weaknesses
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Strengths
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            borderColor = SuccessGreen.copy(alpha = 0.3f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                                Text("Strengths", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            latestAudit!!.strengths.split("\n").filter { it.isNotBlank() }.forEach { str ->
                                Text("• $str", style = MaterialTheme.typography.bodySmall, color = TextPrimary, lineHeight = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                        // Weaknesses
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            borderColor = WarningAmber.copy(alpha = 0.3f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                                Text("Gaps to Fix", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WarningAmber)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            latestAudit!!.weaknesses.split("\n").filter { it.isNotBlank() }.forEach { weak ->
                                Text("• $weak", style = MaterialTheme.typography.bodySmall, color = TextPrimary, lineHeight = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                // Recommendations
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = AccentPurple.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = "💡 Actionable ATS Recommendations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        latestAudit!!.recommendations.split("\n").filter { it.isNotBlank() }.forEach { rec ->
                            Text("• $rec", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, lineHeight = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            // Resume Text Input / Upload Field
            item {
                SectionHeader(
                    title = "Upload or Edit Resume Text",
                    subtitle = "Audit resume against ${profile?.targetRole ?: "target role"} benchmarks"
                )
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = resumeTextInput,
                        onValueChange = { resumeTextInput = it },
                        label = { Text("Resume Plain Text") },
                        minLines = 6,
                        maxLines = 12,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = BgSurface,
                            unfocusedContainerColor = BgSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("resume_input_field")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (resumeTextInput.isNotBlank()) {
                                viewModel.analyzeResume(resumeTextInput.trim(), "My_Resume.pdf")
                            }
                        },
                        enabled = resumeTextInput.isNotBlank() && !isAnalyzing,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_resume_audit_button")
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TextPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing Resume...")
                        } else {
                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Instant ATS Audit")
                        }
                    }
                }
            }
        }

        // ================= TAB 1: TARGET JOB DESCRIPTION MATCHER =================
        else if (selectedTab == 1) {
            // Target Job Selector & Custom Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target Roles & Benchmarks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    OutlinedButton(
                        onClick = { showCustomJdDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlueGlow)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste Custom JD", fontSize = 12.sp)
                    }
                }
            }

            // Presets Horizontal Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(jobPostings) { posting ->
                        val isSelected = selectedPosting?.id == posting.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PrimaryBlue.copy(alpha = 0.25f) else BgCard)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) PrimaryBlueGlow else BorderSubtle,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.selectJobPosting(posting) }
                                .padding(12.dp)
                        ) {
                            Column(modifier = Modifier.width(170.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = posting.company,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PrimaryBlueGlow else TextPrimary,
                                        fontSize = 14.sp
                                    )
                                    if (posting.isPreset) {
                                        Surface(
                                            color = AccentPurple.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "TOP TIER",
                                                color = AccentPurple,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = posting.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${posting.minYearsExperience}y+ exp • ${posting.location}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Active Job Match Card
            if (activeJobMatch != null) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (activeJobMatch!!.matchScore >= 80) SuccessGreen.copy(alpha = 0.5f)
                        else if (activeJobMatch!!.matchScore >= 60) WarningAmber.copy(alpha = 0.5f)
                        else DangerRed.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularScoreGauge(
                                score = activeJobMatch!!.matchScore,
                                size = 96.dp,
                                strokeWidth = 8.dp,
                                label = "JOB FIT"
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${activeJobMatch!!.company} — ${activeJobMatch!!.jobTitle}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeJobMatch!!.fitSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = BorderSubtle)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Matched vs Missing Keywords Breakdown
                        Text(
                            text = "Keyword & Skill Alignment",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Matched Keywords
                        Text(
                            text = "✅ Matched Keywords (${activeJobMatch!!.matchedKeywords.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            activeJobMatch!!.matchedKeywords.forEach { kw ->
                                Surface(
                                    color = SuccessGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = kw,
                                        color = SuccessGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Missing Required Keywords
                        if (activeJobMatch!!.missingRequiredKeywords.isNotEmpty()) {
                            Text(
                                text = "⚠️ Missing Required Keywords (${activeJobMatch!!.missingRequiredKeywords.size}) — Critical ATS Risk",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DangerRed
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                activeJobMatch!!.missingRequiredKeywords.forEach { kw ->
                                    Surface(
                                        color = DangerRed.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = kw,
                                            color = DangerRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Recommendations for this target job
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = PrimaryBlueGlow.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "🎯 ATS Tailoring Strategy for ${activeJobMatch!!.company}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        activeJobMatch!!.atsRecommendations.forEach { rec ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("•", color = PrimaryBlueGlow, fontWeight = FontWeight.Bold)
                                Text(
                                    text = rec,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }

        // ================= TAB 2: GOOGLE X-Y-Z BULLET REWRITER =================
        else {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = PrimaryBlueGlow.copy(alpha = 0.4f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = PrimaryBlueGlow, modifier = Modifier.size(22.dp))
                        Text(
                            text = "Google X-Y-Z Formula Bullet Rewriter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Formula: 'Accomplished [X] as measured by [Y], by doing [Z]'. Turn passive task descriptions into metric-driven achievements.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Try sample weak bullets:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ResumeBulletRewriter.SAMPLE_WEAK_BULLETS) { sample ->
                            Surface(
                                color = BgSurface,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.clickable {
                                    bulletToAnalyzeInput = sample
                                    viewModel.analyzeResumeBullet(sample)
                                }
                            ) {
                                Text(
                                    text = sample.take(35) + "...",
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = bulletToAnalyzeInput,
                        onValueChange = { bulletToAnalyzeInput = it },
                        label = { Text("Resume Bullet Point") },
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = BgSurface,
                            unfocusedContainerColor = BgSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.analyzeResumeBullet(bulletToAnalyzeInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analyze & Generate X-Y-Z Rewrites")
                    }
                }
            }

            // Bullet Analysis & Rewrite Options
            if (bulletAnalysis != null) {
                // Weakness Flags
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (bulletAnalysis!!.weaknessFlags.isNotEmpty()) WarningAmber.copy(alpha = 0.5f) else SuccessGreen.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "🔍 Audit Diagnostics for this Bullet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (bulletAnalysis!!.weaknessFlags.isEmpty()) {
                            Text(
                                text = "✅ No major weakness flags detected. Strong technical phrasing.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SuccessGreen
                            )
                        } else {
                            bulletAnalysis!!.weaknessFlags.forEach { flag ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = flag,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                // 3 Rewrite Options
                item {
                    Text(
                        text = "3 High-Impact X-Y-Z Formula Variants",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                items(bulletAnalysis!!.options) { option ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = PrimaryBlueGlow.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = PrimaryBlue.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = option.style.replace("_", " "),
                                    color = PrimaryBlueGlow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                color = SuccessGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Impact: ${option.impactScore}%",
                                    color = SuccessGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Full Text
                        Text(
                            text = "• ${option.rewrittenText}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // X-Y-Z Breakdown
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BgSurface)
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "[X] Accomplished: ${option.accomplishedX}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "[Y] Measured by: ${option.measuredByY}",
                                    fontSize = 11.sp,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = "[Z] Action taken: ${option.actionZ}",
                                    fontSize = 11.sp,
                                    color = PrimaryBlueGlow
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.applyBulletRewrite(
                                    originalBullet = bulletAnalysis!!.originalBullet,
                                    newBulletText = option.rewrittenText
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("1-Tap Apply to Active Resume Draft")
                        }
                    }
                }
            }
        }
    }

    // Custom Job Description Dialog
    if (showCustomJdDialog) {
        AlertDialog(
            onDismissRequest = { showCustomJdDialog = false },
            title = { Text("Paste Target Job Description", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = customCompany,
                        onValueChange = { customCompany = it },
                        label = { Text("Company Name (e.g. Stripe, OpenAI)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customRoleTitle,
                        onValueChange = { customRoleTitle = it },
                        label = { Text("Role Title (e.g. Senior Backend Engineer)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customJdText,
                        onValueChange = { customJdText = it },
                        label = { Text("Job Description Text") },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customJdText.isNotBlank()) {
                            viewModel.matchCustomJobDescription(
                                company = customCompany,
                                title = customRoleTitle,
                                level = "Mid-Senior",
                                minExp = customMinExp,
                                jdText = customJdText
                            )
                            showCustomJdDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Compute Match")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomJdDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = BgCard
        )
    }
}

@Composable
private fun ScoreSubCard(
    label: String,
    score: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(BgSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (score >= 80) SuccessGreen else WarningAmber
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 9.sp
            )
        }
    }
}

