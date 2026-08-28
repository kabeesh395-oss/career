package com.example.careerpilot.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.careerpilot.ui.components.GlassCard
import com.example.careerpilot.ui.components.MetricCard
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.components.StatusBadge
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
    val skills by viewModel.userSkills.collectAsState()
    val skillGaps by viewModel.skillGaps.collectAsState()
    val roadmap by viewModel.activeRoadmap.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val resume by viewModel.latestResumeAudit.collectAsState()
    val interviews by viewModel.interviews.collectAsState()
    val nextAction by viewModel.nextBestAction.collectAsState()
    val recentEvents by viewModel.recentAnalytics.collectAsState()
    val auditSummary by viewModel.auditSummary.collectAsState()

    val completedInterviews = interviews.filter { it.status == "completed" }
    val avgInterviewScore = if (completedInterviews.isNotEmpty()) {
        completedInterviews.map { it.overallScore }.average().toInt()
    } else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Header
        item {
            Column {
                Text(
                    text = "Welcome back, ${profile?.fullName?.split(" ")?.firstOrNull() ?: "Engineer"}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tracking toward: ${profile?.targetRole ?: "Full Stack Engineer"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentCyan
                )
            }
        }

        // Hero Banner Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.career_hero_banner_1787884489105),
                    contentDescription = "Career Trajectory Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    BgBase.copy(alpha = 0.88f),
                                    BgBase.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalArrangement = Arrangement.Center
                    ) {
                        StatusBadge(text = "AI CAREER INTELLIGENCE", statusType = "primary")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Accelerate to Staff & Lead Level",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Continuous skill calibration & ATS optimization",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Demerits & Red Flag Audit Banner
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("audit") }
                    .testTag("dashboard_audit_banner"),
                borderColor = if (auditSummary.criticalCount > 0) AccentRed.copy(alpha = 0.6f) else PrimaryBlueGlow.copy(alpha = 0.4f),
                backgroundColor = BgSurfaceElevated
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (auditSummary.criticalCount > 0) AccentRed.copy(alpha = 0.15f)
                                    else PrimaryBlue.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (auditSummary.criticalCount > 0) Icons.Default.GppMaybe else Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = if (auditSummary.criticalCount > 0) AccentRed else PrimaryBlueGlow,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Demerits & Red Flag Audit",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (auditSummary.totalDemerits < 0) {
                                    Surface(
                                        color = AccentRed.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${auditSummary.totalDemerits} pts",
                                            color = AccentRed,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Net Readiness: ${auditSummary.netAuditScore}% • ${auditSummary.criticalCount} Critical, ${auditSummary.highCount} High Issues",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Audit Center",
                        tint = PrimaryBlueGlow,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Next Best Action Card
        if (nextAction != null) {
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("next_best_action_card"),
                    borderColor = PrimaryBlueGlow.copy(alpha = 0.6f),
                    backgroundColor = BgCardHover
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusBadge(text = "NEXT BEST ACTION", statusType = "primary")
                            StatusBadge(text = nextAction!!.priority, statusType = nextAction!!.priority)
                        }
                        Text(
                            text = "~${nextAction!!.estimatedMinutes} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = nextAction!!.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = nextAction!!.whyItMatters,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Evidence: ${nextAction!!.evidence}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { onNavigate(nextAction!!.targetRoute) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("nba_cta_button")
                    ) {
                        Text(text = nextAction!!.ctaText, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Metrics 2x3 Grid
        item {
            SectionHeader(
                title = "Career Performance Metrics",
                subtitle = "Real-time readiness telemetry across 6 dimensions"
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        label = "Career Readiness",
                        value = "${profile?.readinessScore ?: 0}%",
                        detail = profile?.targetRole ?: "Not set",
                        accentColor = PrimaryBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("career") }
                    )
                    MetricCard(
                        label = "Roadmap Velocity",
                        value = if (roadmap != null) "${roadmap!!.completedTasks}/${roadmap!!.totalTasks}" else "—",
                        detail = if (roadmap != null) "${roadmap!!.progressPercent.toInt()}% complete" else "No active plan",
                        accentColor = SuccessGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("roadmap") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        label = "Skills Acquired",
                        value = "${skills.size}",
                        detail = "${skillGaps.count { it.priority == "high" }} high-priority gaps",
                        accentColor = AccentPurple,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("career") }
                    )
                    MetricCard(
                        label = "Mock Interviews",
                        value = "${completedInterviews.size}",
                        detail = if (completedInterviews.isNotEmpty()) "Avg score: $avgInterviewScore%" else "No sessions yet",
                        accentColor = AccentCyan,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("interview") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        label = "Portfolio Projects",
                        value = "${projects.size}",
                        detail = "${projects.count { it.status == "completed" }} in production",
                        accentColor = WarningAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("projects") }
                    )
                    MetricCard(
                        label = "Resume ATS Score",
                        value = if (resume != null) "${resume!!.overallScore}%" else "—",
                        detail = if (resume != null) "Audit: ${resume!!.targetRole}" else "No resume audit",
                        accentColor = DangerRed,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("resume") }
                    )
                }
            }
        }

        // Quick Hub Navigation
        item {
            SectionHeader(
                title = "Career Acceleration Hub",
                subtitle = "Core tools for career mastery"
            )
        }

        item {
            val quickLinks = listOf(
                Triple("Audit Center", Icons.Default.Shield, "audit"),
                Triple("Skill Gaps", Icons.Default.Assessment, "career"),
                Triple("Roadmap", Icons.Default.Timeline, "roadmap"),
                Triple("Mock AI", Icons.Default.RecordVoiceOver, "interview"),
                Triple("ATS Resume", Icons.Default.Description, "resume"),
                Triple("Projects", Icons.Default.Code, "projects"),
                Triple("Learning", Icons.Default.MenuBook, "learning"),
                Triple("Integrations", Icons.Default.Sync, "integrations"),
                Triple("Profile", Icons.Default.Person, "profile")
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickLinks) { (title, icon, route) ->
                    QuickHubItem(
                        title = title,
                        icon = icon,
                        onClick = { onNavigate(route) }
                    )
                }
            }
        }

        // Recent Activity Feed
        item {
            SectionHeader(
                title = "Recent Telemetry Events",
                subtitle = "Audit log of career milestones"
            )
        }

        if (recentEvents.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
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
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = BgCard.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.eventName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
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
private fun QuickHubItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 16.dp)
            .width(88.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PrimaryBlueGlow,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
