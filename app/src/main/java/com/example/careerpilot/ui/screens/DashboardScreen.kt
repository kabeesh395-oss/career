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
import com.example.careerpilot.ui.animation.bouncyClickable
import com.example.careerpilot.ui.components.*
import com.example.careerpilot.ui.theme.*
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

    // Live Roadmap Progress Calculations
    val totalRoadmapTasks = if (roadmapItems.isNotEmpty()) roadmapItems.size else (roadmap?.totalTasks ?: 0)
    val completedRoadmapTasks = if (roadmapItems.isNotEmpty()) roadmapItems.count { it.isCompleted } else (roadmap?.completedTasks ?: 0)
    val roadmapPercent = if (totalRoadmapTasks > 0) {
        ((completedRoadmapTasks.toFloat() / totalRoadmapTasks.toFloat()) * 100f).toInt()
    } else (roadmap?.progressPercent?.toInt() ?: 0)

    val animatedRoadmapProgress by animateFloatAsState(
        targetValue = (roadmapPercent.coerceIn(0, 100)) / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "roadmapProgressAnim"
    )

    val nextPendingRoadmapTask = roadmapItems.firstOrNull { !it.isCompleted }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ContentHorizontalPadding),
        contentPadding = PaddingValues(top = Dimens.ContentTopPadding, bottom = Dimens.ContentBottomPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        // User Greeting Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpaceSm)
            ) {
                Text(
                    text = "Welcome back, ${profile?.fullName?.split(" ")?.firstOrNull() ?: "Engineer"}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceXxs))
                Text(
                    text = "Target: ${profile?.targetRole ?: "Full Stack Engineer"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryBlueLighter
                )
            }
        }

        // Readiness Score Summary Card (No giant circular gauge, just a clean overview card)
        item {
            CareerCard(
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
                            text = "CAREER READINESS SUMMARY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                        Text(
                            text = if (profile?.readinessScore != null) "${profile?.readinessScore}% Match Readiness" else "Not calculated yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                        Text(
                            text = "Based on verified skills, projects, and target role benchmarks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier.padding(start = Dimens.SpaceMd),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularScoreGauge(
                            score = profile?.readinessScore ?: 0,
                            size = 72.dp,
                            strokeWidth = 5.dp,
                            label = "MATCH",
                            primaryColor = if ((profile?.readinessScore ?: 0) >= 80) SuccessGreen else PrimaryBlue
                        )
                    }
                }
            }
        }

        // Demerits & Red Flag Audit Banner (Styled Cleanly)
        item {
            CareerCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_audit_banner"),
                borderColor = if (auditSummary.criticalCount > 0) DangerRed.copy(alpha = 0.4f) else BorderSubtle,
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
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(Dimens.AvatarMd)
                                .clip(CircleShape)
                                .background(
                                    if (auditSummary.criticalCount > 0) DangerRed.copy(alpha = 0.12f)
                                    else PrimaryBlue.copy(alpha = 0.12f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (auditSummary.criticalCount > 0) Icons.Default.GppMaybe else Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = if (auditSummary.criticalCount > 0) DangerRedLight else PrimaryBlueLighter,
                                modifier = Modifier.size(Dimens.IconLg)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
                            ) {
                                Text(
                                    text = "Red Flag Audit",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (auditSummary.totalDemerits < 0) {
                                    Surface(
                                        color = DangerRed.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(Dimens.BadgeRadius)
                                    ) {
                                        Text(
                                            text = "${auditSummary.totalDemerits} pts",
                                            color = DangerRedLight,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = Dimens.BadgePaddingHorizontal, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(Dimens.SpaceXxs))
                            Text(
                                text = if (auditSummary.netAuditScore != null) {
                                    "Net: ${auditSummary.netAuditScore}% • ${auditSummary.criticalCount} Critical, ${auditSummary.highCount} High"
                                } else {
                                    "No issues audited yet"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Audit Center",
                        tint = TextSecondary,
                        modifier = Modifier.size(Dimens.IconMd)
                    )
                }
            }
        }

        // Next Best Action Card (Using CareerCardHighlight)
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
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
                        ) {
                            StatusBadge(text = "RECOMMENDED FOCUS", statusType = "primary")
                            StatusBadge(text = nextAction!!.priority, statusType = nextAction!!.priority)
                        }
                        Text(
                            text = "~${nextAction!!.estimatedMinutes} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpaceMd))

                    Text(
                        text = nextAction!!.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                    Text(
                        text = nextAction!!.whyItMatters,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(Dimens.SpaceSm))
                    Text(
                        text = "Evidence: ${nextAction!!.evidence}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpaceLg))
                    Button(
                        onClick = { onNavigate(nextAction!!.targetRoute) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(Dimens.RadiusSm),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.ButtonHeight)
                            .testTag("nba_cta_button")
                    ) {
                        Text(text = nextAction!!.ctaText, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.IconSm)
                        )
                    }
                }
            }
        }

        // Visual Roadmap Progress Tracker
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
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
                        ) {
                            StatusBadge(text = "CAREER ROADMAP", statusType = "primary")
                            Text(
                                text = roadmap?.title ?: "Milestone Progression",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                        Text(
                            text = "Roadmap Completion",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    TextButton(
                        onClick = { onNavigate("roadmap") }
                    ) {
                        Text("View Full →", style = MaterialTheme.typography.labelLarge, color = PrimaryBlueLighter)
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceMd))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
                    ) {
                        Text(
                            text = "$roadmapPercent%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (roadmapPercent == 100) SuccessGreen else AccentCyan
                        )
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Surface(
                        color = BgSurface,
                        shape = RoundedCornerShape(Dimens.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                            modifier = Modifier.padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceSm)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (roadmapPercent == 100) SuccessGreen else if (roadmapPercent > 0) AccentCyan else WarningAmber)
                            )
                            Text(
                                text = "$completedRoadmapTasks of $totalRoadmapTasks Tasks",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceSm))

                CareerProgressBar(progress = animatedRoadmapProgress)

                // Up Next Milestone Task Quick Action
                if (nextPendingRoadmapTask != null) {
                    Spacer(modifier = Modifier.height(Dimens.SpaceMd))
                    Surface(
                        color = BgSurface,
                        shape = RoundedCornerShape(Dimens.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.SpaceMd),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "UP NEXT • PHASE ${nextPendingRoadmapTask.phaseNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(Dimens.SpaceXxs))
                                Text(
                                    text = nextPendingRoadmapTask.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                            }
                            IconButton(
                                onClick = { viewModel.toggleRoadmapTask(nextPendingRoadmapTask.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Mark completed",
                                    tint = PrimaryBlueLighter,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Hub Navigation (2-Column Grid matching HubScreen styling)
        item {
            SectionHeader(
                title = "Engineering Toolkit",
                subtitle = "Quick access to your core workspace modules"
            )
        }

        item {
            val quickLinks = listOf(
                Triple("Roadmap", Icons.Default.Timeline, "roadmap"),
                Triple("Skills", Icons.Default.Assessment, "career"),
                Triple("Resume Audit", Icons.Default.Description, "resume"),
                Triple("Mock Interview", Icons.Default.RecordVoiceOver, "interview"),
                Triple("Market Intel", Icons.Default.TravelExplore, "market"),
                Triple("Code Sandbox", Icons.Default.Terminal, "sandbox"),
                Triple("Integrations", Icons.Default.Sync, "integrations"),
                Triple("Profile Settings", Icons.Default.Person, "profile")
            )

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)) {
                val rows = (quickLinks.size + 1) / 2
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)
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

        // Recent Activity Feed
        item {
            SectionHeader(
                title = "Activity History",
                subtitle = "Timeline of updates, audits and milestones"
            )
        }

        if (recentEvents.isEmpty()) {
            item {
                CareerCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No recent activity recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(recentEvents.take(5)) { event ->
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
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpaceXxs))
                            Text(
                                text = event.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = timeFormat.format(Date(event.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
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
    val shape = RoundedCornerShape(Dimens.RadiusMd)
    Row(
        modifier = modifier
            .clip(shape)
            .background(BgCard)
            .border(Dimens.CardBorderWidth, BorderSubtle, shape)
            .clickable { onClick() }
            .padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(Dimens.RadiusSm))
                .background(PrimaryBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PrimaryBlueLighter,
                modifier = Modifier.size(Dimens.IconMd)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
