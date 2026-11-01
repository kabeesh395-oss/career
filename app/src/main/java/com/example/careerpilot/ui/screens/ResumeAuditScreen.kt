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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.model.BulletRewriteOption
import com.example.careerpilot.data.model.TargetJobPosting
import com.example.careerpilot.data.repository.ResumeBulletRewriter
import com.example.careerpilot.data.repository.ResumeParser
import com.example.careerpilot.data.repository.SamplePdfResume
import com.example.careerpilot.ui.components.CareerCardHighlight
import com.example.careerpilot.ui.components.CareerChip
import com.example.careerpilot.ui.components.CareerProgressBar
import com.example.careerpilot.ui.components.EmptyStateCard
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.components.StatusBadge
import com.example.careerpilot.ui.theme.DesignSystem
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

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("ATS Audit", "Job Matcher", "X-Y-Z Rewriter", "PDF Upload")

    var importResumeText by remember { mutableStateOf(ResumeParser.SAMPLE_IMPORT_RESUMES.first()) }
    var resumeTextInput by remember { mutableStateOf("") }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importResumeFromPdfUri(uri, context)
        }
    }

    var showCustomJdDialog by remember { mutableStateOf(false) }
    var customCompany by remember { mutableStateOf("") }
    var customRoleTitle by remember { mutableStateOf("") }
    var customJdText by remember { mutableStateOf("") }
    var customMinExp by remember { mutableFloatStateOf(3.0f) }
    var bulletToAnalyzeInput by remember { mutableStateOf(ResumeBulletRewriter.SAMPLE_WEAK_BULLETS.first()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = DesignSystem.Spacing.ContentHorizontal),
        contentPadding = PaddingValues(
            top = DesignSystem.Spacing.ContentTop,
            bottom = DesignSystem.Spacing.ContentBottom
        ),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)
    ) {
        // Title & Header
        item {
            Column {
                Text(
                    text = "Resume & Career Intelligence",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DesignSystem.Colors.TextPrimary
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                Text(
                    text = "ATS scoring, Target Job Matcher, and Google X-Y-Z bullet optimization",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DesignSystem.Colors.TextSecondary
                )
            }
        }

        // Navigation Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DesignSystem.Colors.BgCard,
                contentColor = DesignSystem.Colors.PrimaryLight,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(DesignSystem.Shape.RadiusMd))
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) DesignSystem.Colors.PrimaryLight else DesignSystem.Colors.TextSecondary
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
                                modifier = Modifier.size(DesignSystem.Sizing.IconMd)
                            )
                        }
                    )
                }
            }
        }

        // ================= TAB 0: ATS AUDIT & SCORING =================
        if (selectedTab == 0) {
            if (latestAudit == null) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Assessment,
                        title = "No Resume Audit Performed",
                        description = "Upload your resume PDF or paste resume text below to run an instant ATS compatibility analysis against industry benchmarks.",
                        actionLabel = "Go to PDF Upload",
                        onActionClick = { selectedTab = 3 }
                    )
                }
            }

            if (latestAudit != null) {
                item {
                    CareerCardHighlight(
                        modifier = Modifier.fillMaxWidth().testTag("resume_score_card")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)
                        ) {
                            CircularScoreGauge(
                                score = latestAudit.overallScore,
                                size = 96.dp,
                                strokeWidth = 7.dp,
                                label = "ATS SCORE",
                                primaryColor = if (latestAudit.overallScore >= 80) DesignSystem.Colors.Success else DesignSystem.Colors.Primary
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                StatusBadge(text = "TARGET: ${latestAudit.targetRole}", statusType = "primary")
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                                Text(
                                    text = "ATS Compatibility Score",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DesignSystem.Colors.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                                Text(
                                    text = "Analyzed against top-tier tech screening algorithms.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DesignSystem.Colors.TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Item)
                        ) {
                            ScoreSubCard(label = "Impact & Metrics", score = latestAudit.impactScore, modifier = Modifier.weight(1f))
                            ScoreSubCard(label = "Brevity & Layout", score = latestAudit.brevityScore, modifier = Modifier.weight(1f))
                            ScoreSubCard(label = "Formatting & Style", score = latestAudit.styleScore, modifier = Modifier.weight(1f))
                        }
                    }
                }

                if (latestAudit.skillsDetected.isNotBlank()) {
                    item {
                        CareerCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Item)) {
                                Text(
                                    text = "Detected Technical Keywords",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DesignSystem.Colors.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Item))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Item),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val skillsList = latestAudit.skillsDetected.split(",").map { it.trim() }
                                    items(skillsList) { skill ->
                                        StatusBadge(text = skill, statusType = "primary")
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)
                    ) {
                        CareerCard(
                            modifier = Modifier.weight(1f),
                            borderColor = DesignSystem.Colors.Success.copy(alpha = 0.4f)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Item)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = DesignSystem.Colors.Success,
                                        modifier = Modifier.size(DesignSystem.Sizing.IconMd)
                                    )
                                    Text(
                                        "Strengths",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DesignSystem.Colors.Success
                                    )
                                }
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Item))
                                latestAudit.strengths.split("\n").filter { it.isNotBlank() }.forEach { str ->
                                    Text(
                                        "• $str",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DesignSystem.Colors.TextPrimary,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                                }
                            }
                        }

                        CareerCard(
                            modifier = Modifier.weight(1f),
                            borderColor = DesignSystem.Colors.Warning.copy(alpha = 0.4f)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Item)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline)
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = DesignSystem.Colors.Warning,
                                        modifier = Modifier.size(DesignSystem.Sizing.IconMd)
                                    )
                                    Text(
                                        "Gaps to Fix",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DesignSystem.Colors.Warning
                                    )
                                }
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Item))
                                latestAudit.weaknesses.split("\n").filter { it.isNotBlank() }.forEach { weak ->
                                    Text(
                                        "• $weak",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DesignSystem.Colors.TextPrimary,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                                }
                            }
                        }
                    }
                }

                item {
                    CareerCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Item)) {
                            Text(
                                text = "Actionable ATS Recommendations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DesignSystem.Colors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Item))
                            latestAudit.recommendations.split("\n").filter { it.isNotBlank() }.forEach { rec ->
                                Text(
                                    "• $rec",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DesignSystem.Colors.TextSecondary,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                            }
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

            item {
                ResumeFilePickerDropZone(
                    viewModel = viewModel,
                    onViewAuditReport = { },
                    onSwitchToRawText = { selectedTab = 0 }
                )
            }

            item {
                SectionHeader(title = "Or Paste Resume Plain Text", subtitle = "Audit resume against ${profile?.targetRole ?: "target role"} benchmarks")
            }

            item {
                CareerCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)) {
                        CareerTextField(
                            value = resumeTextInput,
                            onValueChange = { resumeTextInput = it },
                            label = "Resume Plain Text",
                            singleLine = false,
                            minLines = 6,
                            maxLines = 12,
                            modifier = Modifier.fillMaxWidth().testTag("resume_input_field")
                        )

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                        CareerButton(
                            text = if (isAnalyzing) "Analyzing Resume…" else "Run Instant ATS Audit",
                            onClick = {
                                if (resumeTextInput.isNotBlank()) {
                                    viewModel.analyzeResume(resumeTextInput.trim(), "My_Resume.pdf")
                                }
                            },
                            enabled = resumeTextInput.isNotBlank() && !isAnalyzing,
                            variant = CareerButton.ButtonVariant.Primary,
                            loading = isAnalyzing,
                            icon = Icons.Default.Analytics,
                            modifier = Modifier.fillMaxWidth().testTag("run_resume_audit_button")
                        )
                    }
                }
            }
        }

        // ================= TAB 1: TARGET JOB DESCRIPTION MATCHER =================
        else if (selectedTab == 1) {
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
                        color = DesignSystem.Colors.TextPrimary
                    )
                    CareerButton(
                        text = "Paste Custom JD",
                        onClick = { showCustomJdDialog = true },
                        variant = CareerButton.ButtonVariant.Outline,
                        icon = Icons.Default.Add,
                        modifier = Modifier.defaultMinSize(minHeight = DesignSystem.Sizing.ButtonHeightSm)
                    )
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Item),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(jobPostings) { posting ->
                        val isSelected = selectedPosting?.id == posting.id
                        CareerCard(
                            modifier = Modifier
                                .width(170.dp)
                                .borderColor(if (isSelected) DesignSystem.Colors.PrimaryLight else DesignSystem.Colors.BorderSubtle)
                                .backgroundColor(if (isSelected) DesignSystem.Colors.PrimaryContainer else DesignSystem.Colors.BgCard)
                                .clickable { viewModel.selectJobPosting(posting) }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = posting.company,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) DesignSystem.Colors.PrimaryLight else DesignSystem.Colors.TextPrimary
                                    )
                                    if (posting.isPreset) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(DesignSystem.Shape.RadiusXs))
                                                .background(DesignSystem.Colors.Primary.copy(alpha = 0.15f))
                                                .padding(horizontal = DesignSystem.Spacing.Inline, vertical = DesignSystem.Spacing.Inline)
                                        ) {
                                            Text(
                                                text = "TOP TIER",
                                                color = DesignSystem.Colors.PrimaryLight,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Space1, vertical = DesignSystem.Space0)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                                Text(
                                    text = posting.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DesignSystem.Colors.TextSecondary,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Item))
                                Text(
                                    text = "${posting.minYearsExperience}y+ exp • ${posting.location}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DesignSystem.Colors.TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            if (activeJobMatch != null) {
                item {
                    val borderColor = when {
                        activeJobMatch.matchScore >= 80 -> DesignSystem.Colors.Success.copy(alpha = 0.5f)
                        activeJobMatch.matchScore >= 60 -> DesignSystem.Colors.Warning.copy(alpha = 0.5f)
                        else -> DesignSystem.Colors.Danger.copy(alpha = 0.5f)
                    }
                    CareerCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = borderColor
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)
                            ) {
                                CircularScoreGauge(
                                    score = activeJobMatch.matchScore,
                                    size = 96.dp,
                                    strokeWidth = 7.dp,
                                    label = "JOB FIT"
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${activeJobMatch.company} | ${activeJobMatch.jobTitle}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DesignSystem.Colors.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                                    Text(
                                        text = activeJobMatch.fitSummary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DesignSystem.Colors.TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))
                            CareerDivider()

                            Text(
                                text = "Keyword & Skill Alignment",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DesignSystem.Colors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Item))

                            Text(
                                text = "Matched Keywords (${activeJobMatch.matchedKeywords.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DesignSystem.Colors.Success
                            )
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(activeJobMatch.matchedKeywords) { kw ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(DesignSystem.Shape.RadiusXs))
                                            .background(DesignSystem.Colors.SuccessContainer)
                                            .padding(horizontal = DesignSystem.Spacing.Space2, vertical = DesignSystem.Spacing.Space0)
                                    ) {
                                        Text(
                                            text = kw,
                                            color = DesignSystem.Colors.Success,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Space2, vertical = DesignSystem.Spacing.Space0)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                            if (activeJobMatch.missingRequiredKeywords.isNotEmpty()) {
                                Text(
                                    text = "Missing Required Keywords (${activeJobMatch.missingRequiredKeywords.size}): Critical ATS Risk",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DesignSystem.Colors.Danger
                                )
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(activeJobMatch.missingRequiredKeywords) { kw ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(DesignSystem.Shape.RadiusXs))
                                                .background(DesignSystem.Colors.DangerContainer)
                                                .padding(horizontal = DesignSystem.Spacing.Space2, vertical = DesignSystem.Spacing.Space0)
                                        ) {
                                            Text(
                                                text = kw,
                                                color = DesignSystem.Colors.Danger,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Space2, vertical = DesignSystem.Spacing.Space0)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                            CareerCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Item)) {
                                    Text(
                                        text = "ATS Tailoring Strategy for ${activeJobMatch.company}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DesignSystem.Colors.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Item))
                                    activeJobMatch.atsRecommendations.forEach { rec ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline)
                                        ) {
                                            Text("•", color = DesignSystem.Colors.PrimaryLight, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = rec,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = DesignSystem.Colors.TextSecondary,
                                                lineHeight = 16.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= TAB 2: GOOGLE X-Y-Z BULLET REWRITER =================
        else if (selectedTab == 2) {
            item {
                CareerCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Item)
                        ) {
                            Icon(
                                Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = DesignSystem.Colors.PrimaryLight,
                                modifier = Modifier.size(DesignSystem.Sizing.IconLg)
                            )
                            Text(
                                text = "Google X-Y-Z Formula Bullet Rewriter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DesignSystem.Colors.TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                        Text(
                            text = "Formula: 'Accomplished [X] as measured by [Y], by doing [Z]'. Turn passive task descriptions into metric-driven achievements.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DesignSystem.Colors.TextSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                        Text(
                            text = "Try sample weak bullets:",
                            style = MaterialTheme.typography.labelSmall,
                            color = DesignSystem.Colors.TextMuted
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Item),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(ResumeBulletRewriter.SAMPLE_WEAK_BULLETS) { sample ->
                                CareerCard(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(DesignSystem.Shape.RadiusMd))
                                        .background(DesignSystem.Colors.BgSurface)
                                        .border(DesignSystem.Sizing.CardBorderWidth, DesignSystem.Colors.BorderDefault, RoundedCornerShape(DesignSystem.Shape.RadiusMd)),
                                    borderColor = DesignSystem.Colors.BorderDefault,
                                    onClick = {
                                        bulletToAnalyzeInput = sample
                                        viewModel.analyzeResumeBullet(sample)
                                    }
                                ) {
                                    Text(
                                        text = sample.take(35) + "...",
                                        fontSize = 11.sp,
                                        color = DesignSystem.Colors.TextPrimary,
                                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Space3, vertical = DesignSystem.Spacing.Space2)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                        CareerTextField(
                            value = bulletToAnalyzeInput,
                            onValueChange = { bulletToAnalyzeInput = it },
                            label = "Resume Bullet Point",
                            singleLine = false,
                            minLines = 3,
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                        CareerButton(
                            text = "Analyze & Generate X-Y-Z Rewrites",
                            onClick = { viewModel.analyzeResumeBullet(bulletToAnalyzeInput) },
                            variant = CareerButton.ButtonVariant.Primary,
                            icon = Icons.Default.Bolt,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (bulletAnalysis != null) {
                item {
                    val borderColor = if (bulletAnalysis.weaknessFlags.isNotEmpty()) DesignSystem.Colors.Warning.copy(alpha = 0.5f) else DesignSystem.Colors.Success.copy(alpha = 0.5f)
                    CareerCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = borderColor
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Item)) {
                            Text(
                                text = "Audit Diagnostics for this Bullet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DesignSystem.Colors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Item))
                            if (bulletAnalysis.weaknessFlags.isEmpty()) {
                                Text(
                                    text = "No major weakness flags detected. Strong technical phrasing.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DesignSystem.Colors.Success
                                )
                            } else {
                                bulletAnalysis.weaknessFlags.forEach { flag ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Item)
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = DesignSystem.Colors.Warning,
                                            modifier = Modifier.size(DesignSystem.Sizing.IconSm)
                                        )
                                        Text(
                                            text = flag,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DesignSystem.Colors.TextSecondary,
                                            lineHeight = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "3 High-Impact X-Y-Z Formula Variants",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DesignSystem.Colors.TextPrimary
                    )
                }

                items(bulletAnalysis.options) { option ->
                    CareerCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(DesignSystem.Shape.RadiusXs))
                                        .background(DesignSystem.Colors.PrimaryContainer)
                                        .padding(horizontal = DesignSystem.Spacing.Space3, vertical = DesignSystem.Spacing.Space0)
                                ) {
                                    Text(
                                        text = option.style.replace("_", " "),
                                        color = DesignSystem.Colors.PrimaryLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Space2, vertical = DesignSystem.Space0)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(DesignSystem.Shape.RadiusXs))
                                        .background(DesignSystem.Colors.SuccessContainer)
                                        .padding(horizontal = DesignSystem.Spacing.Space2, vertical = DesignSystem.Spacing.Space0)
                                ) {
                                    Text(
                                        text = "Impact: ${option.impactScore}%",
                                        color = DesignSystem.Colors.Success,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Space2, vertical = DesignSystem.Spacing.Space0)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Item))

                            Text(
                                text = "• ${option.rewrittenText}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = DesignSystem.Colors.TextPrimary,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Item))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(DesignSystem.Shape.RadiusMd))
                                    .background(DesignSystem.Colors.BgSurface)
                                    .padding(DesignSystem.Spacing.Space3)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline)) {
                                    Text(
                                        text = "[X] Accomplished: ${option.accomplishedX}",
                                        fontSize = 11.sp,
                                        color = DesignSystem.Colors.TextSecondary
                                    )
                                    Text(
                                        text = "[Y] Measured by: ${option.measuredByY}",
                                        fontSize = 11.sp,
                                        color = DesignSystem.Colors.Success
                                    )
                                    Text(
                                        text = "[Z] Action taken: ${option.actionZ}",
                                        fontSize = 11.sp,
                                        color = DesignSystem.Colors.PrimaryLight
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                            CareerButton(
                                text = "1-Tap Apply to Active Resume Draft",
                                onClick = {
                                    viewModel.applyBulletRewrite(
                                        originalBullet = bulletAnalysis.originalBullet,
                                        newBulletText = option.rewrittenText
                                    )
                                },
                                variant = CareerButton.ButtonVariant.Primary,
                                icon = Icons.Default.Check,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // ================= TAB 3: PDF UPLOAD & INTELLIGENT PARSER =================
        else if (selectedTab == 3) {
            item {
                ResumeFilePickerDropZone(
                    viewModel = viewModel,
                    onViewAuditReport = { selectedTab = 0 },
                    onSwitchToRawText = { }
                )
            }

            item {
                SectionHeader(title = "Or Paste Raw Resume Text", subtitle = "Direct text parsing and skill recognition")
            }

            item {
                CareerCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)) {
                        val charCount = importResumeText.length
                        val wordCount = if (importResumeText.isBlank()) 0 else importResumeText.trim().split("\\s+".toRegex()).size
                        val isTextSufficient = importResumeText.trim().length >= 20

                        CareerTextField(
                            value = importResumeText,
                            onValueChange = { importResumeText = it },
                            label = "Paste Raw Resume or PDF Text Here",
                            singleLine = false,
                            minLines = 6,
                            maxLines = 10,
                            modifier = Modifier.fillMaxWidth().testTag("import_resume_text_field"),
                            supportingText = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (!isTextSufficient) "Enter at least 20 characters to parse" else "Ready to parse",
                                        color = if (!isTextSufficient) DesignSystem.Colors.Warning else DesignSystem.Colors.Success,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "$wordCount words · $charCount chars",
                                        color = DesignSystem.Colors.TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                        CareerButton(
                            text = "Parse & Sync to User Profile",
                            onClick = {
                                if (isTextSufficient) {
                                    viewModel.importResumeFromText(importResumeText.trim())
                                    selectedTab = 0
                                }
                            },
                            enabled = isTextSufficient,
                            variant = CareerButton.ButtonVariant.Primary,
                            icon = Icons.Default.AutoFixHigh,
                            modifier = Modifier.fillMaxWidth().testTag("parse_import_resume_button")
                        )
                    }
                }
            }
        }

        // Custom Job Description Dialog
        if (showCustomJdDialog) {
            CustomJobDescriptionDialog(
                onDismiss = { showCustomJdDialog = false },
                customCompany = customCompany,
                customRoleTitle = customRoleTitle,
                customJdText = customJdText,
                customMinExp = customMinExp,
                onCompanyChange = { customCompany = it },
                onRoleTitleChange = { customRoleTitle = it },
                onJdTextChange = { customJdText = it },
                onMinExpChange = { customMinExp = it },
                onConfirm = {
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
                }
            )
        }
    }

    @Composable
    private fun ScoreSubCard(label: String, score: Int, modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(DesignSystem.Shape.RadiusMd))
                .background(DesignSystem.Colors.BgSurface)
                .border(DesignSystem.Sizing.CardBorderWidth, DesignSystem.Colors.BorderSubtle, RoundedCornerShape(DesignSystem.Shape.RadiusMd))
                .padding(DesignSystem.Spacing.Item),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$score%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (score >= 80) DesignSystem.Colors.Success else DesignSystem.Colors.Warning
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = DesignSystem.Colors.TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }

    @Composable
    private fun CustomJobDescriptionDialog(
        onDismiss: () -> Unit,
        customCompany: String,
        customRoleTitle: String,
        customJdText: String,
        customMinExp: Float,
        onCompanyChange: (String) -> Unit,
        onRoleTitleChange: (String) -> Unit,
        onJdTextChange: (String) -> Unit,
        onMinExpChange: (Float) -> Unit,
        onConfirm: () -> Unit
    ) {
        var company by remember(customCompany) { mutableStateOf(customCompany) }
        var roleTitle by remember(customRoleTitle) { mutableStateOf(customRoleTitle) }
        var jdText by remember(customJdText) { mutableStateOf(customJdText) }
        var minExp by remember(customMinExp) { mutableStateOf(customMinExp) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Paste Target Job Description", color = DesignSystem.Colors.TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)
                ) {
                    CareerTextField(
                        value = company,
                        onValueChange = { company = it; onCompanyChange(it) },
                        label = "Company Name (e.g. Stripe, OpenAI)",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    CareerTextField(
                        value = roleTitle,
                        onValueChange = { roleTitle = it; onRoleTitleChange(it) },
                        label = "Role Title (e.g. Senior Backend Engineer)",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    CareerTextField(
                        value = jdText,
                        onValueChange = { jdText = it; onJdTextChange(it) },
                        label = "Job Description Text",
                        singleLine = false,
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                    CareerTextField(
                        value = minExp.toString(),
                        onValueChange = { onMinExpChange(it.toFloatOrNull() ?: 3.0f) },
                        label = "Min Experience (Years)",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                CareerButton(
                    text = "Compute Match",
                    onClick = onConfirm,
                    variant = CareerButton.ButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = DesignSystem.Colors.TextSecondary)
                }
            },
            containerColor = DesignSystem.Colors.BgCard
        )
    }
}
