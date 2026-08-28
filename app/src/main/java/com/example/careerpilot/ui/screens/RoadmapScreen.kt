package com.example.careerpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.model.RoadmapItem
import com.example.careerpilot.ui.components.GlassCard
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.components.StatusBadge
import com.example.careerpilot.ui.theme.*
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

    val phases = items.groupBy { "Phase ${it.phaseNumber}: ${it.phaseTitle}" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title & Summary
        item {
            Column {
                Text(
                    text = "Personalized 3-Phase Roadmap",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = roadmap?.title ?: "Milestone execution trajectory",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentCyan
                )
            }
        }

        // Progress Card
        item {
            GlassCard(
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
                        color = TextPrimary
                    )
                    Text(
                        text = "${roadmap?.completedTasks ?: 0} / ${roadmap?.totalTasks ?: 0} Tasks (${roadmap?.progressPercent?.toInt() ?: 0}%)",
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryBlueGlow
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(BgMuted)
                ) {
                    val progress = (roadmap?.progressPercent ?: 0f) / 100f
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(PrimaryBlue, AccentPurple, AccentCyan)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        val role = profile?.targetRole ?: "Full Stack Engineer"
                        viewModel.generateRoadmap(role)
                    },
                    enabled = !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = TextPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Regenerating...")
                    } else {
                        Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Regenerate Roadmap from Skill Gaps")
                    }
                }
            }
        }

        // Phases and Deliverables
        if (items.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No active roadmap milestones. Click 'Regenerate Roadmap' to generate.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
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
    val borderColor = if (item.isCompleted) SuccessGreen.copy(alpha = 0.4f) else BorderSubtle

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        borderColor = borderColor,
        backgroundColor = if (item.isCompleted) BgCard.copy(alpha = 0.5f) else BgCard
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (item.isCompleted) "Completed" else "Pending",
                    tint = if (item.isCompleted) SuccessGreen else TextMuted,
                    modifier = Modifier.size(22.dp)
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
                        color = if (item.isCompleted) TextSecondary else TextPrimary
                    )
                    StatusBadge(text = "${item.estimatedHours}h", statusType = if (item.isCompleted) "completed" else "primary")
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(text = item.category, statusType = "neutral")
                    if (item.isCompleted) {
                        Text(
                            text = "✓ Completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessGreen
                        )
                    }
                }
            }
        }
    }
}
