package com.example.careerpilot.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.model.BulletRewriteOption
import com.example.careerpilot.data.model.TargetJobPosting
import com.example.careerpilot.data.repository.ResumeBulletRewriter
import com.example.careerpilot.data.repository.ResumeParser
import com.example.careerpilot.data.repository.SamplePdfResume
import com.example.careerpilot.ui.components.CircularScoreGauge
import com.example.careerpilot.ui.components.CareerCardHighlight
import com.example.careerpilot.ui.components.EmptyStateCard
import com.example.careerpilot.ui.components.GlassCard
import com.example.careerpilot.ui.components.ResumeFilePickerDropZone
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.components.StatusBadge
import com.example.careerpilot.ui.theme.DesignSystem
import com.example.careerpilot.ui.theme.PrimaryBlueGlow
import com.example.careerpilot.ui.viewmodel.CareerViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResumeAuditScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    val latestAudit by viewModel.latestResumeAudit.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val jobPostings by viewModel.jobPostings.collectAsState()
    val selectedPosting by viewModel.selectedJobPosting.collectAsState()
    val activeJobMatch by viewModel.activeJobMatch.collectAsState()
    val bulletAnalysis by viewModel.bulletAnalysis.collectAsState()
    val uploadedPdf by viewModel.uploadedPdfInfo.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: ATS Audit, 1: Job Matcher, 2: X-Y-Z Bullet Rewriter, 3: Import Resume
    val tabTitles = listOf("ATS Audit", "Job Matcher", "X-Y-Z Rewriter", "PDF Upload")

    var importResumeText by remember { mutableStateOf(ResumeParser.SAMPLE_IMPORT_RESUMES.first()) }

    var resumeTextInput by remember {
        mutableStateOf("")
    }

    // PDF Document Picker Launcher
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importResumeFromPdfUri(uri, context)
        }
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
            .padding(horizontal = DesignSystem.Spacing.screenHorizontal),
        contentPadding = PaddingValues(
            top = DesignSystem.Spacing.screenTop,
            bottom = DesignSystem.Spacing.screenBottom
        ),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sectionSpacing)
    ) {
        // Title & Header
        item {
            Column {
                Text(
                    text = "Resume & Career Intelligence",
                    style = DesignSystem.TypographyTokens.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DesignSystem.Colors.TextPrimary
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                Text(
                    text = "ATS scoring, Target Job Matcher, and Google X-Y-Z bullet optimization",
                    style = DesignSystem.TypographyTokens.bodyMedium,
                    color = DesignSystem.Colors.TextSecondary
                )
            }
        }

        // Navigation Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DesignSystem.Colors.Card,
                contentColor = DesignSystem.Colors.PrimaryLight,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(DesignSystem.Shapes.shapeMd)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) DesignSystem.Colors.PrimaryLight else DesignSystem.Colors.TextSecondary,
                                fontSize = DesignSystem.TypographyTokens.fontMd
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.Assessment
                                    1 -> Icons.Default.WorkOutline
                                    2 -> Icons.Default.AutoFixHigh
                                    else -> Icons.Default.FileUpload
                                },
                                contentDescription = title,
                                modifier = Modifier.size(DesignSystem.Components.iconMd)
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
                    CareerCardHighlight(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("resume_score_card")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xl)
                        ) {
                            CircularScoreGauge(
                                score = latestAudit!!.overallScore,
                                size = 104.dp,
                                strokeWidth = 8.dp,
                                label = "ATS SCORE",
                                primaryColor = if (latestAudit!!.overallScore >= 80) DesignSystem.Colors.Success else DesignSystem.Colors.Primary
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                StatusBadge(text = "TARGET: ${latestAudit!!.targetRole}", statusType = "primary")
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                                Text(
                                    text = "ATS Compatibility Score",
                                    style = DesignSystem.TypographyTokens.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DesignSystem.Colors.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                                Text(
                                    text = "Analyzed against top-tier tech screening algorithms.",
                                    style = DesignSystem.TypographyTokens.bodySmall,
                                    color = DesignSystem.Colors.TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))

                        // 3 Dimensional Breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
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
                                style = DesignSystem.TypographyTokens.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DesignSystem.Colors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))

                            val skillsList = latestAudit!!.skillsDetected.split(",").map { it.trim() }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm),
                                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm),
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
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.md)
                    ) {
                        // Strengths
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            borderColor = DesignSystem.Colors.Success.copy(alpha = 0.3f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xs)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = DesignSystem.Colors.Success,
                                    modifier = Modifier.size(DesignSystem.Components.iconMd)
                                )
                                Text(
                                    "Strengths",
                                    style = DesignSystem.TypographyTokens.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DesignSystem.Colors.Success
                                )
                            }
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                            latestAudit!!.strengths.split("\n").filter { it.isNotBlank() }.forEach { str ->
                                Text(
                                    "• $str",
                                    style = DesignSystem.TypographyTokens.bodySmall,
                                    color = DesignSystem.Colors.TextPrimary,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                            }
                        }

                        // Weaknesses
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            borderColor = DesignSystem.Colors.Warning.copy(alpha = 0.3f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xs)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = DesignSystem.Colors.Warning,
                                    modifier = Modifier.size(DesignSystem.Components.iconMd)
                                )
                                Text(
                                    "Gaps to Fix",
                                    style = DesignSystem.TypographyTokens.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DesignSystem.Colors.Warning
                                )
                            }
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                            latestAudit!!.weaknesses.split("\n").filter { it.isNotBlank() }.forEach { weak ->
                                Text(
                                    "• $weak",
                                    style = DesignSystem.TypographyTokens.bodySmall,
                                    color = DesignSystem.Colors.TextPrimary,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                            }
                        }
                    }
                }

                // Recommendations
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = DesignSystem.Colors.Purple.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = "Actionable ATS Recommendations",
                            style = DesignSystem.TypographyTokens.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                        latestAudit!!.recommendations.split("\n").filter { it.isNotBlank() }.forEach { rec ->
                            Text(
                                "• $rec",
                                style = DesignSystem.TypographyTokens.bodyMedium,
                                color = DesignSystem.Colors.TextSecondary,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                        }
                    }
                }
            } else {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Description,
                        title = "No Resume Audited Yet",
                        description = "Upload your resume PDF or paste plain text below to calculate your ATS match score, detect missing keywords, and verify technical competencies for ${profile?.targetRole ?: "your target role"}.",
                        actionLabel = "Upload PDF",
                        onActionClick = { pdfPickerLauncher.launch("application/pdf") }
                    )
                }
            }

            // Integrated Native Resume File Picker & Dropzone
            item {
                ResumeFilePickerDropZone(
                    viewModel = viewModel,
                    onViewAuditReport = { /* already in Tab 0 */ },
                    onSwitchToRawText = { selectedTab = 0 }
                )
            }

            // Resume Text Input / Upload Field
            item {
                SectionHeader(
                    title = "Or Paste Resume Plain Text",
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
                            focusedTextColor = DesignSystem.Colors.TextPrimary,
                            unfocusedTextColor = DesignSystem.Colors.TextPrimary,
                            focusedBorderColor = DesignSystem.Colors.Primary,
                            unfocusedBorderColor = DesignSystem.Colors.BorderSubtle,
                            focusedContainerColor = DesignSystem.Colors.Surface,
                            unfocusedContainerColor = DesignSystem.Colors.Surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("resume_input_field")
                    )

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))

                    Button(
                        onClick = {
                            if (resumeTextInput.isNotBlank()) {
                                viewModel.analyzeResume(resumeTextInput.trim(), "My_Resume.pdf")
                            }
                        },
                        enabled = resumeTextInput.isNotBlank() && !isAnalyzing,
                        colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Colors.Primary),
                        shape = DesignSystem.Shapes.shapeSm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = DesignSystem.Components.buttonHeight)
                            .testTag("run_resume_audit_button")
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(DesignSystem.Components.iconSm),
                                color = DesignSystem.Colors.TextPrimary
                            )
                            Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                            Text("Analyzing Resume...")
                        } else {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                modifier = Modifier.size(DesignSystem.Components.iconMd)
                            )
                            Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
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
                        style = DesignSystem.TypographyTokens.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DesignSystem.Colors.TextPrimary
                    )
                    OutlinedButton(
                        onClick = { showCustomJdDialog = true },
                        shape = DesignSystem.Shapes.shapeSm,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DesignSystem.Colors.PrimaryLight)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(DesignSystem.Components.iconSm))
                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.xs))
                        Text("Paste Custom JD", fontSize = DesignSystem.TypographyTokens.fontSm)
                    }
                }
            }

            // Presets Horizontal Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(jobPostings) { posting ->
                        val isSelected = selectedPosting?.id == posting.id
                        Box(
                            modifier = Modifier
                                .clip(DesignSystem.Shapes.shapeMd)
                                .background(if (isSelected) DesignSystem.Colors.Primary.copy(alpha = 0.25f) else DesignSystem.Colors.Card)
                                .border(
                                    width = if (isSelected) DesignSystem.Components.borderMedium else DesignSystem.Components.borderThin,
                                    color = if (isSelected) DesignSystem.Colors.PrimaryLight else DesignSystem.Colors.BorderSubtle,
                                    shape = DesignSystem.Shapes.shapeMd
                                )
                                .clickable { viewModel.selectJobPosting(posting) }
                                .padding(DesignSystem.Spacing.md)
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
                                        color = if (isSelected) DesignSystem.Colors.PrimaryLight else DesignSystem.Colors.TextPrimary,
                                        fontSize = DesignSystem.TypographyTokens.fontBase
                                    )
                                    if (posting.isPreset) {
                                        Surface(
                                            color = DesignSystem.Colors.Purple.copy(alpha = 0.15f),
                                            shape = DesignSystem.Shapes.shapeXs
                                        ) {
                                            Text(
                                                text = "TOP TIER",
                                                color = DesignSystem.Colors.Purple,
                                                fontSize = DesignSystem.TypographyTokens.fontXxs,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(
                                                    horizontal = DesignSystem.Spacing.xs,
                                                    vertical = DesignSystem.Spacing.xxs
                                                )
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                                Text(
                                    text = posting.title,
                                    style = DesignSystem.TypographyTokens.bodySmall,
                                    color = DesignSystem.Colors.TextSecondary,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                                Text(
                                    text = "${posting.minYearsExperience}y+ exp • ${posting.location}",
                                    style = DesignSystem.TypographyTokens.labelSmall,
                                    color = DesignSystem.Colors.TextMuted,
                                    fontSize = DesignSystem.TypographyTokens.fontXs
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
                        borderColor = if (activeJobMatch!!.matchScore >= 80) DesignSystem.Colors.Success.copy(alpha = 0.5f)
                        else if (activeJobMatch!!.matchScore >= 60) DesignSystem.Colors.Warning.copy(alpha = 0.5f)
                        else DesignSystem.Colors.Error.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.lg)
                        ) {
                            CircularScoreGauge(
                                score = activeJobMatch!!.matchScore,
                                size = DesignSystem.Components.gaugeSizeLg,
                                strokeWidth = DesignSystem.Components.gaugeStrokeMd,
                                label = "JOB FIT"
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${activeJobMatch!!.company} | ${activeJobMatch!!.jobTitle}",
                                    style = DesignSystem.TypographyTokens.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DesignSystem.Colors.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                                Text(
                                    text = activeJobMatch!!.fitSummary,
                                    style = DesignSystem.TypographyTokens.bodySmall,
                                    color = DesignSystem.Colors.TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
                        HorizontalDivider(color = DesignSystem.Colors.BorderSubtle)
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))

                        // Matched vs Missing Keywords Breakdown
                        Text(
                            text = "Keyword & Skill Alignment",
                            style = DesignSystem.TypographyTokens.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))

                        // Matched Keywords
                        Text(
                            text = "Matched Keywords (${activeJobMatch!!.matchedKeywords.size})",
                            style = DesignSystem.TypographyTokens.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.Success
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xs),
                            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xs),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            activeJobMatch!!.matchedKeywords.forEach { kw ->
                                Surface(
                                    color = DesignSystem.Colors.Success.copy(alpha = 0.15f),
                                    shape = DesignSystem.Shapes.shapeXs,
                                    border = androidx.compose.foundation.BorderStroke(DesignSystem.Components.borderThin, DesignSystem.Colors.Success.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = kw,
                                        color = DesignSystem.Colors.Success,
                                        fontSize = DesignSystem.TypographyTokens.fontSm,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(
                                            horizontal = DesignSystem.Components.badgePaddingHorizontal,
                                            vertical = DesignSystem.Spacing.xxs
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))

                        // Missing Required Keywords
                        if (activeJobMatch!!.missingRequiredKeywords.isNotEmpty()) {
                            Text(
                                text = "Missing Required Keywords (${activeJobMatch!!.missingRequiredKeywords.size}): Critical ATS Risk",
                                style = DesignSystem.TypographyTokens.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DesignSystem.Colors.Error
                            )
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xs),
                                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xs),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                activeJobMatch!!.missingRequiredKeywords.forEach { kw ->
                                    Surface(
                                        color = DesignSystem.Colors.Error.copy(alpha = 0.15f),
                                        shape = DesignSystem.Shapes.shapeXs,
                                        border = androidx.compose.foundation.BorderStroke(DesignSystem.Components.borderThin, DesignSystem.Colors.Error.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = kw,
                                            color = DesignSystem.Colors.Error,
                                            fontSize = DesignSystem.TypographyTokens.fontSm,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(
                                                horizontal = DesignSystem.Components.badgePaddingHorizontal,
                                                vertical = DesignSystem.Spacing.xxs
                                            )
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
                        borderColor = DesignSystem.Colors.PrimaryLight.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "ATS Tailoring Strategy for ${activeJobMatch!!.company}",
                            style = DesignSystem.TypographyTokens.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                        activeJobMatch!!.atsRecommendations.forEach { rec ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
                            ) {
                                Text("•", color = DesignSystem.Colors.PrimaryLight, fontWeight = FontWeight.Bold)
                                Text(
                                    text = rec,
                                    style = DesignSystem.TypographyTokens.bodySmall,
                                    color = DesignSystem.Colors.TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                        }
                    }
                }
            }
        }

        // ================= TAB 2: GOOGLE X-Y-Z BULLET REWRITER =================
        else if (selectedTab == 2) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = DesignSystem.Colors.PrimaryLight.copy(alpha = 0.4f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
                    ) {
                        Icon(
                            Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            tint = DesignSystem.Colors.PrimaryLight,
                            modifier = Modifier.size(DesignSystem.Components.iconLg)
                        )
                        Text(
                            text = "Google X-Y-Z Formula Bullet Rewriter",
                            style = DesignSystem.TypographyTokens.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                    Text(
                        text = "Formula: 'Accomplished [X] as measured by [Y], by doing [Z]'. Turn passive task descriptions into metric-driven achievements.",
                        style = DesignSystem.TypographyTokens.bodySmall,
                        color = DesignSystem.Colors.TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))

                    Text(
                        text = "Try sample weak bullets:",
                        style = DesignSystem.TypographyTokens.labelSmall,
                        color = DesignSystem.Colors.TextMuted
                    )
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ResumeBulletRewriter.SAMPLE_WEAK_BULLETS) { sample ->
                            Surface(
                                color = DesignSystem.Colors.Surface,
                                shape = DesignSystem.Shapes.shapeSm,
                                border = androidx.compose.foundation.BorderStroke(DesignSystem.Components.borderThin, DesignSystem.Colors.BorderSubtle),
                                modifier = Modifier.clickable {
                                    bulletToAnalyzeInput = sample
                                    viewModel.analyzeResumeBullet(sample)
                                }
                            ) {
                                Text(
                                    text = sample.take(35) + "...",
                                    fontSize = DesignSystem.TypographyTokens.fontSm,
                                    color = DesignSystem.Colors.TextPrimary,
                                    modifier = Modifier.padding(
                                        horizontal = DesignSystem.Spacing.sm,
                                        vertical = DesignSystem.Spacing.xs
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))

                    OutlinedTextField(
                        value = bulletToAnalyzeInput,
                        onValueChange = { bulletToAnalyzeInput = it },
                        label = { Text("Resume Bullet Point") },
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DesignSystem.Colors.TextPrimary,
                            unfocusedTextColor = DesignSystem.Colors.TextPrimary,
                            focusedBorderColor = DesignSystem.Colors.Primary,
                            unfocusedBorderColor = DesignSystem.Colors.BorderSubtle,
                            focusedContainerColor = DesignSystem.Colors.Surface,
                            unfocusedContainerColor = DesignSystem.Colors.Surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))

                    Button(
                        onClick = { viewModel.analyzeResumeBullet(bulletToAnalyzeInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Colors.Primary),
                        shape = DesignSystem.Shapes.shapeSm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = DesignSystem.Components.buttonHeight)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(DesignSystem.Components.iconMd))
                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.xs))
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
                        borderColor = if (bulletAnalysis!!.weaknessFlags.isNotEmpty()) DesignSystem.Colors.Warning.copy(alpha = 0.5f) else DesignSystem.Colors.Success.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "Audit Diagnostics for this Bullet",
                            style = DesignSystem.TypographyTokens.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                        if (bulletAnalysis!!.weaknessFlags.isEmpty()) {
                            Text(
                                text = "No major weakness flags detected. Strong technical phrasing.",
                                style = DesignSystem.TypographyTokens.bodySmall,
                                color = DesignSystem.Colors.Success
                            )
                        } else {
                            bulletAnalysis!!.weaknessFlags.forEach { flag ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = DesignSystem.Colors.Warning,
                                        modifier = Modifier.size(DesignSystem.Components.iconSm)
                                    )
                                    Text(
                                        text = flag,
                                        style = DesignSystem.TypographyTokens.bodySmall,
                                        color = DesignSystem.Colors.TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                            }
                        }
                    }
                }

                // 3 Rewrite Options
                item {
                    Text(
                        text = "3 High-Impact X-Y-Z Formula Variants",
                        style = DesignSystem.TypographyTokens.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DesignSystem.Colors.TextPrimary
                    )
                }

                items(bulletAnalysis!!.options) { option ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = DesignSystem.Colors.PrimaryLight.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = DesignSystem.Colors.Primary.copy(alpha = 0.2f),
                                shape = DesignSystem.Shapes.shapeXs
                            ) {
                                Text(
                                    text = option.style.replace("_", " "),
                                    color = DesignSystem.Colors.PrimaryLight,
                                    fontSize = DesignSystem.TypographyTokens.fontSm,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        horizontal = DesignSystem.Spacing.sm,
                                        vertical = DesignSystem.Spacing.xxs
                                    )
                                )
                            }

                            Surface(
                                color = DesignSystem.Colors.Success.copy(alpha = 0.15f),
                                shape = DesignSystem.Shapes.shapeXs
                            ) {
                                Text(
                                    text = "Impact: ${option.impactScore}%",
                                    color = DesignSystem.Colors.Success,
                                    fontSize = DesignSystem.TypographyTokens.fontSm,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        horizontal = DesignSystem.Spacing.xs,
                                        vertical = DesignSystem.Spacing.xxs
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))

                        // Full Text
                        Text(
                            text = "• ${option.rewrittenText}",
                            style = DesignSystem.TypographyTokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = DesignSystem.Colors.TextPrimary,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))

                        // X-Y-Z Breakdown
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(DesignSystem.Shapes.shapeSm)
                                .background(DesignSystem.Colors.Surface)
                                .padding(DesignSystem.Spacing.sm)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xs)) {
                                Text(
                                    text = "[X] Accomplished: ${option.accomplishedX}",
                                    fontSize = DesignSystem.TypographyTokens.fontSm,
                                    color = DesignSystem.Colors.TextSecondary
                                )
                                Text(
                                    text = "[Y] Measured by: ${option.measuredByY}",
                                    fontSize = DesignSystem.TypographyTokens.fontSm,
                                    color = DesignSystem.Colors.Success
                                )
                                Text(
                                    text = "[Z] Action taken: ${option.actionZ}",
                                    fontSize = DesignSystem.TypographyTokens.fontSm,
                                    color = DesignSystem.Colors.PrimaryLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))

                        Button(
                            onClick = {
                                viewModel.applyBulletRewrite(
                                    originalBullet = bulletAnalysis!!.originalBullet,
                                    newBulletText = option.rewrittenText
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Colors.Primary),
                            shape = DesignSystem.Shapes.shapeSm,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = DesignSystem.Components.buttonHeight)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(DesignSystem.Components.iconSm))
                            Spacer(modifier = Modifier.width(DesignSystem.Spacing.xs))
                            Text("1-Tap Apply to Active Resume Draft")
                        }
                    }
                }
            }
        }

        // ================= TAB 3: PDF UPLOAD & INTELLIGENT PARSER =================
        else if (selectedTab == 3) {
            // Main Native PDF & Document Upload Drop-Zone Component
            item {
                ResumeFilePickerDropZone(
                    viewModel = viewModel,
                    onViewAuditReport = { selectedTab = 0 },
                    onSwitchToRawText = { /* target raw text field below */ }
                )
            }

            // Fallback Raw Text Importer
            item {
                SectionHeader(
                    title = "Or Paste Raw Resume Text",
                    subtitle = "Direct text parsing and skill recognition"
                )
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = DesignSystem.Colors.Purple.copy(alpha = 0.5f)
                ) {
                    OutlinedTextField(
                        value = importResumeText,
                        onValueChange = { importResumeText = it },
                        label = { Text("Paste Raw Resume or PDF Text Here") },
                        minLines = 6,
                        maxLines = 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_resume_text_field")
                    )

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))

                    Button(
                        onClick = {
                            if (importResumeText.isNotBlank()) {
                                viewModel.importResumeFromText(importResumeText)
                                selectedTab = 0
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Colors.Purple),
                        shape = DesignSystem.Shapes.shapeSm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = DesignSystem.Components.buttonHeight)
                            .testTag("parse_import_resume_button")
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(DesignSystem.Components.iconMd))
                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                        Text("Parse & Sync to User Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Custom Job Description Dialog
    if (showCustomJdDialog) {
        AlertDialog(
            onDismissRequest = { showCustomJdDialog = false },
            title = { Text("Paste Target Job Description", color = DesignSystem.Colors.TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
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
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Colors.Primary)
                ) {
                    Text("Compute Match")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomJdDialog = false }) {
                    Text("Cancel", color = DesignSystem.Colors.TextSecondary)
                }
            },
            containerColor = DesignSystem.Colors.Card
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
            .clip(DesignSystem.Shapes.shapeSm)
            .background(DesignSystem.Colors.Surface)
            .border(DesignSystem.Components.borderThin, DesignSystem.Colors.BorderSubtle, DesignSystem.Shapes.shapeSm)
            .padding(DesignSystem.Spacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score%",
                style = DesignSystem.TypographyTokens.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (score >= 80) DesignSystem.Colors.Success else DesignSystem.Colors.Warning
            )
            Text(
                text = label,
                style = DesignSystem.TypographyTokens.labelSmall,
                color = DesignSystem.Colors.TextSecondary,
                fontSize = DesignSystem.TypographyTokens.fontXxs
            )
        }
    }
}

