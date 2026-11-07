package com.example.careerpilot.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.ui.components.*
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.theme.DesignSystem
import com.example.careerpilot.ui.theme.Dimens
import com.example.careerpilot.ui.viewmodel.CareerViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: CareerViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val roadmap by viewModel.activeRoadmap.collectAsState()
    val roadmapItems by viewModel.roadmapItems.collectAsState()
    val nextAction by viewModel.nextBestAction.collectAsState()
    val recentEvents by viewModel.recentAnalytics.collectAsState()
    val auditSummary by viewModel.auditSummary.collectAsState()
    val latestResumeAudit by viewModel.latestResumeAudit.collectAsState()
    val userSkills by viewModel.userSkills.collectAsState()
    val projects by viewModel.projects.collectAsState()

    // Real Roadmap Progress Calculations strictly from stored data
    val totalRoadmapTasks = if (roadmapItems.isNotEmpty()) roadmapItems.size else (roadmap?.totalTasks ?: 0)
    val completedRoadmapTasks = if (roadmapItems.isNotEmpty()) roadmapItems.count { it.isCompleted } else (roadmap?.completedTasks ?: 0)
    val roadmapPercent = if (totalRoadmapTasks > 0) {
        ((completedRoadmapTasks.toFloat() / totalRoadmapTasks.toFloat()) * 100f).toInt()
    } else (roadmap?.progressPercent?.toInt() ?: 0)

    val animatedRoadmapProgress by animateFloatAsState(
        targetValue = (roadmapPercent.coerceIn(0, 100)) / 100f,
        animationSpec = tween(durationMillis = 600),
        label = "roadmapProgressAnim"
    )

    val nextPendingRoadmapTask = roadmapItems.firstOrNull { !it.isCompleted }
    val hasReadinessScore = profile?.readinessScore != null

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
        // User Greeting Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DesignSystem.Spacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Workspace",
                        style = DesignSystem.TypographyTokens.bodySmall,
                        color = DesignSystem.Colors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxs))
                    Text(
                        text = profile?.fullName?.split(" ")?.firstOrNull() ?: "Engineer",
                        style = DesignSystem.TypographyTokens.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = DesignSystem.Colors.TextPrimary
                    )
                }

                Surface(
                    color = DesignSystem.Colors.Primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(DesignSystem.Shapes.radiusSm),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Colors.Primary.copy(alpha = 0.35f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkOutline,
                            contentDescription = null,
                            tint = DesignSystem.Colors.PrimaryLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = profile?.targetRole ?: "Full Stack Engineer",
                            style = DesignSystem.TypographyTokens.labelSmall,
                            color = DesignSystem.Colors.PrimaryLight,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Real Career Readiness Summary or Empty State
        item {
            if (hasReadinessScore) {
                CareerCardHighlight(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onNavigate("career") }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CAREER READINESS INDEX",
                                style = DesignSystem.TypographyTokens.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DesignSystem.Colors.TextSecondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                            Text(
                                text = "${profile!!.readinessScore}% Match Readiness",
                                style = DesignSystem.TypographyTokens.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = DesignSystem.Colors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                            Text(
                                text = "Calibrated against verified skills, project audits, and target role benchmarks.",
                                style = DesignSystem.TypographyTokens.bodySmall,
                                color = DesignSystem.Colors.TextSecondary
                            )

                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))

                            // Strictly real verified sub-metrics
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (latestResumeAudit != null) {
                                    StatusBadge(text = "ATS: ${latestResumeAudit!!.overallScore}%", statusType = "primary")
                                } else {
                                    StatusBadge(text = "No Resume Audit", statusType = "neutral")
                                }
                                StatusBadge(text = "${userSkills.size} Skills", statusType = if (userSkills.isNotEmpty()) "success" else "neutral")
                                if (projects.isNotEmpty()) {
                                    StatusBadge(text = "${projects.size} Projects", statusType = "primary")
                                }
                            }
                        }

                        Box(
                            modifier = Modifier.padding(start = DesignSystem.Spacing.md),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularScoreGauge(
                                score = profile!!.readinessScore!!,
                                size = 96.dp,
                                strokeWidth = 7.dp,
                                label = "READINESS",
                                primaryColor = if (profile!!.readinessScore!! >= 80) DesignSystem.Colors.Success else DesignSystem.Colors.Primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "View Skill Matrix & Rubric",
                                style = DesignSystem.TypographyTokens.labelSmall,
                                color = DesignSystem.Colors.PrimaryLight,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = DesignSystem.Colors.PrimaryLight,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            } else {
                // Honest, useful empty state for un-evaluated user
                CareerCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = BorderMedium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = DesignSystem.Spacing.xs)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(DesignSystem.Shapes.radiusSm))
                                    .background(DesignSystem.Colors.Primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = null,
                                    tint = DesignSystem.Colors.PrimaryLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Career Readiness Not Evaluated",
                                    style = DesignSystem.TypographyTokens.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DesignSystem.Colors.TextPrimary
                                )
                                Text(
                                    text = "Target: ${profile?.targetRole ?: "Full Stack Engineer"}",
                                    style = DesignSystem.TypographyTokens.labelSmall,
                                    color = DesignSystem.Colors.TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))

                        Text(
                            text = "Complete your career assessment or upload your resume to calibrate your readiness index against role rubrics.",
                            style = DesignSystem.TypographyTokens.bodySmall,
                            color = DesignSystem.Colors.TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onNavigate("career") },
                                colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Colors.Primary),
                                shape = RoundedCornerShape(DesignSystem.Shapes.radiusSm),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Run Assessment", fontWeight = FontWeight.SemiBold)
                            }
                            OutlinedButton(
                                onClick = { onNavigate("resume") },
                                shape = RoundedCornerShape(DesignSystem.Shapes.radiusSm),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DesignSystem.Colors.TextPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Colors.BorderMedium),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Audit Resume")
                            }
                        }
                    }
                }
            }
        }

        // Real Red Flag / Demerit Audit Status Banner
        item {
            val hasIssues = auditSummary.totalIssuesCount > 0
            val isEvaluated = auditSummary.hasEvaluatedData || hasReadinessScore
            CareerCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_audit_banner"),
                borderColor = if (auditSummary.criticalCount > 0) DesignSystem.Colors.Error.copy(alpha = 0.45f) else DesignSystem.Colors.BorderSubtle,
                backgroundColor = if (auditSummary.criticalCount > 0) Color(0xFF1E1424) else DesignSystem.Colors.Card,
                onClick = { onNavigate("audit") }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.md)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(DesignSystem.Components.avatarMd)
                                .clip(RoundedCornerShape(DesignSystem.Shapes.radiusSm))
                                .background(
                                    if (auditSummary.criticalCount > 0) DesignSystem.Colors.Error.copy(alpha = 0.12f)
                                    else DesignSystem.Colors.Primary.copy(alpha = 0.12f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (auditSummary.criticalCount > 0) Icons.Default.GppMaybe else Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = if (auditSummary.criticalCount > 0) DesignSystem.Colors.ErrorLight else DesignSystem.Colors.PrimaryLight,
                                modifier = Modifier.size(DesignSystem.Components.iconLg)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
                            ) {
                                Text(
                                    text = "Audit Center",
                                    style = DesignSystem.TypographyTokens.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DesignSystem.Colors.TextPrimary
                                )
                                if (isEvaluated && auditSummary.totalDemerits < 0) {
                                    Surface(
                                        color = DesignSystem.Colors.Error.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(DesignSystem.Shapes.radiusXs)
                                    ) {
                                        Text(
                                            text = "${auditSummary.totalDemerits} pts",
                                            color = DesignSystem.Colors.ErrorLight,
                                            style = DesignSystem.TypographyTokens.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxs))
                            Text(
                                text = if (!isEvaluated) {
                                    "No audit run yet: Check for resume and skill gaps"
                                } else if (auditSummary.criticalCount > 0 || auditSummary.highCount > 0) {
                                    "${auditSummary.criticalCount} Critical, ${auditSummary.highCount} High priority issues found"
                                } else if (hasIssues) {
                                    "${auditSummary.totalIssuesCount} minor observations found"
                                } else {
                                    "No critical risks detected"
                                },
                                style = DesignSystem.TypographyTokens.bodySmall,
                                color = DesignSystem.Colors.TextSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Audit Center",
                        tint = DesignSystem.Colors.TextSecondary,
                        modifier = Modifier.size(DesignSystem.Components.iconMd)
                    )
                }
            }
        }

        // Next Best Action (if active)
        if (nextAction != null) {
            item {
                CareerCardHighlight(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("next_best_action_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
                        ) {
                            StatusBadge(text = "RECOMMENDED ACTION", statusType = "primary")
                            StatusBadge(text = nextAction!!.priority, statusType = nextAction!!.priority)
                        }
                        Text(
                            text = "~${nextAction!!.estimatedMinutes} min",
                            style = DesignSystem.TypographyTokens.labelSmall,
                            color = DesignSystem.Colors.TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))

                    Text(
                        text = nextAction!!.title,
                        style = DesignSystem.TypographyTokens.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DesignSystem.Colors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                    Text(
                        text = nextAction!!.whyItMatters,
                        style = DesignSystem.TypographyTokens.bodyMedium,
                        color = DesignSystem.Colors.TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                    Text(
                        text = "Evidence: ${nextAction!!.evidence}",
                        style = DesignSystem.TypographyTokens.bodySmall,
                        color = DesignSystem.Colors.TextMuted
                    )
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.lg))
                    Button(
                        onClick = { onNavigate(nextAction!!.targetRoute) },
                        colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Colors.Primary),
                        shape = RoundedCornerShape(DesignSystem.Shapes.radiusSm),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DesignSystem.Components.buttonHeight)
                            .testTag("nba_cta_button")
                    ) {
                        Text(text = nextAction!!.ctaText, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.sm))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(DesignSystem.Components.iconSm)
                        )
                    }
                }
            }
        }

        // Real Roadmap Progress Tracker or Useful Empty State
        item {
            CareerCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_roadmap_progress_tracker")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
                        ) {
                            StatusBadge(text = "ROADMAP", statusType = "primary")
                            Text(
                                text = roadmap?.title ?: "Milestone Progression",
                                style = DesignSystem.TypographyTokens.labelSmall,
                                color = DesignSystem.Colors.TextSecondary,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))
                        Text(
                            text = "Milestone Progression",
                            style = DesignSystem.TypographyTokens.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.TextPrimary
                        )
                    }

                    TextButton(
                        onClick = { onNavigate("roadmap") },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "View All",
                                style = DesignSystem.TypographyTokens.labelMedium,
                                color = DesignSystem.Colors.PrimaryLight,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = DesignSystem.Colors.PrimaryLight,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))

                if (totalRoadmapTasks > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$completedRoadmapTasks of $totalRoadmapTasks Completed",
                            style = DesignSystem.TypographyTokens.bodySmall,
                            color = DesignSystem.Colors.TextSecondary
                        )
                        Text(
                            text = "$roadmapPercent%",
                            style = DesignSystem.TypographyTokens.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.PrimaryLight
                        )
                    }

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.xs))

                    CareerProgressBar(
                        progress = animatedRoadmapProgress,
                        color = DesignSystem.Colors.Primary,
                        trackColor = DesignSystem.Colors.Muted,
                        height = 6.dp
                    )

                    if (nextPendingRoadmapTask != null) {
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                        Surface(
                            color = DesignSystem.Colors.Surface,
                            shape = RoundedCornerShape(DesignSystem.Shapes.radiusSm),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Colors.BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(DesignSystem.Spacing.md),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "UP NEXT: PHASE ${nextPendingRoadmapTask.phaseNumber}",
                                        style = DesignSystem.TypographyTokens.labelSmall,
                                        color = DesignSystem.Colors.PrimaryLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxs))
                                    Text(
                                        text = nextPendingRoadmapTask.title,
                                        style = DesignSystem.TypographyTokens.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = DesignSystem.Colors.TextPrimary,
                                        maxLines = 1
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.toggleRoadmapTask(nextPendingRoadmapTask.id) },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Mark completed",
                                        tint = DesignSystem.Colors.PrimaryLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Clean roadmap empty state
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = DesignSystem.Spacing.sm)
                    ) {
                        Text(
                            text = "No roadmap milestones generated yet.",
                            style = DesignSystem.TypographyTokens.bodyMedium,
                            color = DesignSystem.Colors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.md))
                        Button(
                            onClick = { onNavigate("roadmap") },
                            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Colors.Primary),
                            shape = RoundedCornerShape(DesignSystem.Shapes.radiusSm)
                        ) {
                            Text("Generate Roadmap")
                        }
                    }
                }
            }
        }

        // Quick Navigation Grid (Engineering Workspace)
        item {
            SectionHeader(
                title = "Engineering Workspace",
                subtitle = "Core modules and preparation tools"
            )
        }

        item {
            val quickLinks = listOf(
                Triple("Resume Audit", Icons.Default.Description, "resume"),
                Triple("Mock Interview", Icons.Default.RecordVoiceOver, "interview"),
                Triple("Skill Matrix", Icons.Default.Assessment, "career"),
                Triple("Roadmap", Icons.Default.Timeline, "roadmap"),
                Triple("Opportunities", Icons.Default.Stars, "opportunities"),
                Triple("Projects Hub", Icons.Default.Code, "projects"),
                Triple("Code Sandbox", Icons.Default.Terminal, "sandbox"),
                Triple("Market Intel", Icons.Default.TravelExplore, "market"),
                Triple("Integrations", Icons.Default.Sync, "integrations"),
                Triple("Export Center", Icons.Default.FileDownload, "export")
            )
            Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.itemSpacing)) {
                val rows = (quickLinks.size + 1) / 2
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.itemSpacing)
                    ) {
                        for (col in 0 until 2) {
                            val index = row * 2 + col
                            if (index < quickLinks.size) {
                                val link = quickLinks[index]
                                QuickLinkCard(
                                    title = link.first,
                                    icon = link.second,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onNavigate(link.third) }
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Activity History Feed (Real Stored Events Only)
        item {
            SectionHeader(
                title = "Activity History",
                subtitle = "Timeline of actual audits, updates, and milestones"
            )
        }

        if (recentEvents.isEmpty()) {
            item {
                CareerCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = DesignSystem.Spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = DesignSystem.Colors.TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.sm))
                        Text(
                            text = "No activity recorded yet",
                            style = DesignSystem.TypographyTokens.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxs))
                        Text(
                            text = "Actions, audits, and completions will be recorded here.",
                            style = DesignSystem.TypographyTokens.bodySmall,
                            color = DesignSystem.Colors.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(recentEvents.take(6)) { event ->
                val timeFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                CareerCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.eventName,
                                style = DesignSystem.TypographyTokens.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = DesignSystem.Colors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxs))
                            Text(
                                text = event.detail,
                                style = DesignSystem.TypographyTokens.bodySmall,
                                color = DesignSystem.Colors.TextSecondary
                            )
                        }
                        Text(
                            text = timeFormat.format(Date(event.timestamp)),
                            style = DesignSystem.TypographyTokens.labelSmall,
                            color = DesignSystem.Colors.TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickLinkCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .clip(shape)
            .background(BgCard)
            .border(1.dp, BorderSubtle, shape)
            .clickable(onClick = onClick)
            .padding(
                horizontal = DesignSystem.Spacing.md,
                vertical = DesignSystem.Spacing.sm
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DesignSystem.Colors.Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = DesignSystem.Colors.PrimaryLight,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = DesignSystem.TypographyTokens.labelMedium,
                color = DesignSystem.Colors.TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = DesignSystem.Colors.TextMuted,
            modifier = Modifier.size(14.dp)
        )
    }
}
