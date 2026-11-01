package com.example.careerpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.model.RoadmapItem
import com.example.careerpilot.ui.components.EmptyStateCard
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.components.StatusBadge
import com.example.careerpilot.ui.theme.DesignSystem
import com.example.careerpilot.ui.viewmodel.CareerViewModel

@Composable
fun RoadmapScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val roadmap by viewModel.activeRoadmap.collectAsState()
    val items by viewModel.roadmapItems.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    // Group items by phase
    val phases = items.groupBy { it.phaseTitle }

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
        // Header
        item {
            Column {
                Text(
                    text = "Personalized Roadmap",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DesignSystem.Colors.TextPrimary
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                Text(
                    text = roadmap?.title ?: "Milestone execution trajectory",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DesignSystem.Colors.PrimaryLight
                )
            }
        }

        // Progress Card
        item {
            CareerCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("roadmap_progress_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Overall Roadmap Velocity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DesignSystem.Colors.TextPrimary
                    )
                    Text(
                        text = "${roadmap?.completedTasks ?: 0} / ${roadmap?.totalTasks ?: 0} Tasks (${roadmap?.progressPercent?.toInt() ?: 0}%)",
                        style = MaterialTheme.typography.labelLarge,
                        color = DesignSystem.Colors.PrimaryLight
                    )
                }

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DesignSystem.Sizing.DividerThickness * 4)
                        .clip(RoundedCornerShape(DesignSystem.Shape.RadiusXs))
                        .background(DesignSystem.Colors.BgSurface)
                ) {
                    val progress = (roadmap?.progressPercent ?: 0f) / 100f
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .background(DesignSystem.Colors.Primary)
                    )
                }

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))

                CareerButton(
                    text = if (isAnalyzing) "Generating Roadmap..." else "Generate Roadmap from Skill Gaps",
                    onClick = {
                        val role = profile?.targetRole ?: "Full Stack Engineer"
                        viewModel.generateRoadmap(role)
                    },
                    enabled = !isAnalyzing,
                    variant = CareerButton.ButtonVariant.Primary,
                    loading = isAnalyzing,
                    icon = Icons.Default.Autorenew,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Phases and Deliverables
        if (items.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.AltRoute,
                    title = "No Roadmap Milestones Yet",
                    description = "A career roadmap breaks down your learning trajectory into 3 clear execution phases targeting ${profile?.targetRole ?: "your target role"}.",
                    actionLabel = "Generate Roadmap",
                    onActionClick = {
                        val role = profile?.targetRole ?: "Full Stack Engineer"
                        viewModel.generateRoadmap(role)
                    }
                )
            }
        } else {
            phases.forEach { (phaseTitle, phaseTasks) ->
                item {
                    SectionHeader(
                        title = phaseTitle,
                        subtitle = "${phaseTasks.count { it.isCompleted }}/${phaseTasks.size} milestones completed"
                    )
                }

                items(phaseTasks) { task ->
                    RoadmapTaskCard(
                        item = task,
                        onToggle = { viewModel.toggleRoadmapTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RoadmapTaskCard(
    item: RoadmapItem,
    onToggle: () -> Unit
) {
    val borderColor = if (item.isCompleted) DesignSystem.Colors.Success.copy(alpha = 0.4f) else DesignSystem.Colors.BorderDefault

    CareerCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space2)
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(DesignSystem.Sizing.AvatarMd)
            ) {
                Icon(
                    imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (item.isCompleted) "Completed" else "Pending",
                    tint = if (item.isCompleted) DesignSystem.Colors.Success else DesignSystem.Colors.TextMuted,
                    modifier = Modifier.size(DesignSystem.Sizing.IconMd)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isCompleted) DesignSystem.Colors.TextSecondary else DesignSystem.Colors.TextPrimary
                    )
                    StatusBadge(text = "${item.estimatedHours}h", statusType = if (item.isCompleted) "completed" else "primary")
                }

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = DesignSystem.Colors.TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space2),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(text = item.category, statusType = "neutral")
                    if (item.isCompleted) {
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = DesignSystem.Colors.Success
                        )
                    }
                }
            }
        }
    }
}
