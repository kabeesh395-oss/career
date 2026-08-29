package com.example.careerpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.careerpilot.data.model.SkillSprint
import com.example.careerpilot.ui.components.GlassCard
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.components.StatusBadge
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel

@Composable
fun SkillSprintsScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val sprints by viewModel.skillSprints.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Weekly Skill Sprints & Proof Badges",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Complete 7-day micro-challenges & earn verifiable GitHub credentials",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Active Sprints List
        items(sprints) { sprint ->
            SprintCard(
                sprint = sprint,
                onToggleMilestone = { idx -> viewModel.toggleSprintMilestone(sprint.id, idx) },
                onClaim = { viewModel.claimSprintReward(sprint.id) }
            )
        }
    }
}

@Composable
private fun SprintCard(
    sprint: SkillSprint,
    onToggleMilestone: (Int) -> Unit,
    onClaim: () -> Unit
) {
    val progress = if (sprint.milestoneTasks.isNotEmpty()) {
        sprint.completedMilestones.toFloat() / sprint.milestoneTasks.size.toFloat()
    } else 0f

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sprint.sprintTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Target Focus: ${sprint.targetSkill} · Day ${sprint.currentDay} of ${sprint.durationDays}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentCyan
                )
            }

            StatusBadge(
                text = "+${sprint.rewardXp} XP",
                color = AccentPurple
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = sprint.description,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Bar
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${sprint.completedMilestones} of ${sprint.milestoneTasks.size} Milestones Completed",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueGlow
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryBlueGlow,
                trackColor = BorderSubtle
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Milestone Checklist
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            sprint.milestoneTasks.forEachIndexed { idx, task ->
                val isDone = idx < sprint.completedMilestones
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onToggleMilestone(idx) }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isDone) SuccessGreen else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = task,
                        fontSize = 12.sp,
                        color = if (isDone) TextPrimary else TextSecondary,
                        fontWeight = if (isDone) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Reward & Verification Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                Text(
                    text = sprint.badgeName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarningAmber
                )
            }


            if (sprint.isClaimed) {
                Text(
                    text = "Badge Claimed & Verified ✓",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            } else {
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("claim_sprint_button_${sprint.id}")
                ) {
                    Text("Submit GitHub Proof", fontSize = 11.sp)
                }
            }
        }
    }
}
