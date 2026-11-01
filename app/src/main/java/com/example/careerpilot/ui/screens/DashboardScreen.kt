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
import com.example.careerpilot.ui.theme.DesignSystem
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
            .padding(horizontal = DesignSystem.Spacing.ContentHorizontal),
        contentPadding = PaddingValues(
            top = DesignSystem.Spacing.ContentTop,
            bottom = DesignSystem.Spacing.ContentBottom
        ),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Section)
    ) {
        // User Greeting Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DesignSystem.Spacing.Space1),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Workspace",
                        style = MaterialTheme.typography.bodySmall,
                        color = DesignSystem.Colors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                    Text(
                        text = profile?.fullName?.split(" ")?.firstOrNull() ?: "Engineer",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = DesignSystem.Colors.TextPrimary
                    )
                }

                Surface(
                    color = DesignSystem.Colors.PrimaryContainer,
                    shape = RoundedCornerShape(DesignSystem.Shape.RadiusSm)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space2),
                        modifier = Modifier.padding(
                            horizontal = DesignSystem.Spacing.Space3,
                            vertical = DesignSystem.Spacing.Space1
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkOutline,
                            contentDescription = null,
                            tint = DesignSystem.Colors.Primary,
                            modifier = Modifier.size(DesignSystem.Sizing.IconSm)
                        )
                        Text(
                            text = profile?.targetRole ?: "Full Stack Engineer",
                            style = MaterialTheme.typography.labelSmall,
                            color = DesignSystem.Colors.Primary,
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
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DesignSystem.Colors.TextSecondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                            Text(
                                text = "${profile!!.readinessScore}% Match Readiness",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = DesignSystem.Colors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                            Text(
                                text = "Calibrated against verified skills, project audits, and target role benchmarks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DesignSystem.Colors.TextSecondary
                            )

                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))

                            // Strictly real verified sub-metrics
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space2),
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
                            modifier = Modifier.padding(start = DesignSystem.Spacing.Space4),
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

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1)
                        ) {
                            Text(
                                text = "View Skill Matrix & Rubric",
                                style = MaterialTheme.typography.labelSmall,
                                color = DesignSystem.Colors.PrimaryLight,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = DesignSystem.Colors.PrimaryLight,
                                modifier = Modifier.size(DesignSystem.Sizing.IconSm)
                            )
                        }
                    }
                }
            } else {
                // Honest, useful empty state for un-evaluated user
                CareerCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = DesignSystem.Colors.BorderDefault
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = DesignSystem.Spacing.Space1)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space2)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(DesignSystem.Shape.RadiusSm))
                                    .background(DesignSystem.Colors.PrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = null,
                                    tint = DesignSystem.Colors.Primary,
                                    modifier = Modifier.size(DesignSystem.Sizing.IconMd)
                                )
                            }
                            Column {
                                Text(
                                    text = "Career Readiness Not Evaluated",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DesignSystem.Colors.TextPrimary
                                )
                                Text(
                                    text = "Target: ${profile?.targetRole ?: "Full Stack Engineer"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DesignSystem.Colors.TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))

                        Text(
                            text = "Complete your career assessment or upload your resume to calibrate your readiness index against role rubrics.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DesignSystem.Colors.TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CareerButton(
                                text = "Run Assessment",
                                onClick = { onNavigate("career") },
                                variant = CareerButton.ButtonVariant.Primary,
                                modifier = Modifier.weight(1f)
                            )
                            CareerButton(
                                text = "Audit Resume",
                                onClick = { onNavigate("resume") },
                                variant = CareerButton.ButtonVariant.Outline,
                                modifier = Modifier.weight(1f)
                            )
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
                borderColor = if (auditSummary.criticalCount > 0) DesignSystem.Colors.Danger.copy(alpha = 0.45f) else DesignSystem.Colors.BorderSubtle,
                backgroundColor = if (auditSummary.criticalCount > 0) DesignSystem.Colors.DangerContainer else DesignSystem.Colors.BgCard,
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
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space3)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(DesignSystem.Sizing.AvatarMd)
                                .clip(RoundedCornerShape(DesignSystem.Shape.RadiusSm))
                                .background(
                                    if (auditSummary.criticalCount > 0) DesignSystem.Colors.DangerContainer else DesignSystem.Colors.PrimaryContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (auditSummary.criticalCount > 0) Icons.Default.GppMaybe else Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = if (auditSummary.criticalCount > 0) DesignSystem.Colors.Danger else DesignSystem.Colors.Primary,
                                modifier = Modifier.size(DesignSystem.Sizing.IconLg)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space2)
                            ) {
                                Text(
                                    text = "Audit Center",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DesignSystem.Colors.TextPrimary
                                )
                                if (isEvaluated && auditSummary.totalDemerits < 0) {
                                    Surface(
                                        color = DesignSystem.Colors.DangerContainer,
                                        shape = RoundedCornerShape(DesignSystem.Shape.RadiusXs)
                                    ) {
                                        Text(
                                            text = "${auditSummary.totalDemerits} pts",
                                            color = DesignSystem.Colors.Danger,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Space2, vertical = DesignSystem.Spacing.Space0)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
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
                                style = MaterialTheme.typography.bodySmall,
                                color = DesignSystem.Colors.TextSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Audit Center",
                        tint = DesignSystem.Colors.TextMuted,
                        modifier = Modifier.size(DesignSystem.Sizing.IconMd)
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
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space2)
                        ) {
                            StatusBadge(text = "RECOMMENDED ACTION", statusType = "primary")
                            StatusBadge(text = nextAction!!.priority, statusType = nextAction!!.priority)
                        }
                        Text(
                            text = "~${nextAction!!.estimatedMinutes} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = DesignSystem.Colors.TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))

                    Text(
                        text = nextAction!!.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DesignSystem.Colors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                    Text(
                        text = nextAction!!.whyItMatters,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DesignSystem.Colors.TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))
                    Text(
                        text = "Evidence: ${nextAction!!.evidence}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DesignSystem.Colors.TextMuted
                    )
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space4))
                    CareerButton(
                        text = nextAction!!.ctaText,
                        onClick = { onNavigate(nextAction!!.targetRoute) },
                        variant = CareerButton.ButtonVariant.Primary,
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nba_cta_button")
                    )
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
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space2)
                        ) {
                            StatusBadge(text = "ROADMAP", statusType = "primary")
                            Text(
                                text = roadmap?.title ?: "Milestone Progression",
                                style = MaterialTheme.typography.labelSmall,
                                color = DesignSystem.Colors.TextSecondary,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                        Text(
                            text = "Milestone Progression",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.TextPrimary
                        )
                    }

                    TextButton(
                        onClick = { onNavigate("roadmap") },
                        contentPadding = PaddingValues(horizontal = DesignSystem.Spacing.Space2, vertical = DesignSystem.Spacing.Space1)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1)
                        ) {
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelMedium,
                                color = DesignSystem.Colors.PrimaryLight,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = DesignSystem.Colors.PrimaryLight,
                                modifier = Modifier.size(DesignSystem.Sizing.IconSm)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))

                if (totalRoadmapTasks > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$completedRoadmapTasks of $totalRoadmapTasks Completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = DesignSystem.Colors.TextSecondary
                        )
                        Text(
                            text = "$roadmapPercent%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.PrimaryLight
                        )
                    }

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))

                    CareerProgressBar(
                        progress = animatedRoadmapProgress,
                        color = DesignSystem.Colors.Primary,
                        trackColor = DesignSystem.Colors.BgSurface,
                        height = 6.dp
                    )

                    if (nextPendingRoadmapTask != null) {
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))
                        Surface(
                            color = DesignSystem.Colors.BgSurface,
                            shape = RoundedCornerShape(DesignSystem.Shape.RadiusSm),
                            border = androidx.compose.foundation.BorderStroke(DesignSystem.Sizing.CardBorderWidth, DesignSystem.Colors.BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(DesignSystem.Spacing.Space4),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "UP NEXT: PHASE ${nextPendingRoadmapTask.phaseNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DesignSystem.Colors.PrimaryLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                                    Text(
                                        text = nextPendingRoadmapTask.title,
                                        style = MaterialTheme.typography.bodyMedium,
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
                                        modifier = Modifier.size(DesignSystem.Sizing.IconMd)
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
                            .padding(vertical = DesignSystem.Spacing.Space2)
                    ) {
                        Text(
                            text = "No roadmap milestones generated yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DesignSystem.Colors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))
                        CareerButton(
                            text = "Generate Roadmap",
                            onClick = { onNavigate("roadmap") },
                            variant = CareerButton.ButtonVariant.Primary
                        )
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
            Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.ItemSpacing)) {
                val rows = (quickLinks.size + 1) / 2
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.ItemSpacing)
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
                            .padding(vertical = DesignSystem.Spacing.Space4),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = DesignSystem.Colors.TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))
                        Text(
                            text = "No activity recorded yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                        Text(
                            text = "Actions, audits, and completions will be recorded here.",
                            style = MaterialTheme.typography.bodySmall,
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
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = DesignSystem.Colors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                            Text(
                                text = event.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = DesignSystem.Colors.TextSecondary
                            )
                        }
                        Text(
                            text = timeFormat.format(Date(event.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
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
    val shape = RoundedCornerShape(DesignSystem.Shape.RadiusMd)
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = DesignSystem.Sizing.InputHeight)
            .clip(shape)
            .background(DesignSystem.Colors.BgCard)
            .border(DesignSystem.Sizing.CardBorderWidth, DesignSystem.Colors.BorderSubtle, shape)
            .clickable(onClick = onClick)
            .padding(
                horizontal = DesignSystem.Spacing.Space3,
                vertical = DesignSystem.Spacing.Space2
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space2),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(DesignSystem.Sizing.AvatarSm)
                    .clip(RoundedCornerShape(DesignSystem.Shape.RadiusSm))
                    .background(DesignSystem.Colors.PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = DesignSystem.Colors.Primary,
                    modifier = Modifier.size(DesignSystem.Sizing.IconMd)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = DesignSystem.Colors.TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = DesignSystem.Colors.TextMuted,
            modifier = Modifier.size(DesignSystem.Sizing.IconSm)
        )
    }
}
