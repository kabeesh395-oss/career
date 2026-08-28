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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.model.SkillGap
import com.example.careerpilot.data.model.UserSkill
import com.example.careerpilot.data.repository.BenchmarkCatalog
import com.example.careerpilot.ui.components.CircularScoreGauge
import com.example.careerpilot.ui.components.GlassCard
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.components.StatusBadge
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel

@Composable
fun CareerAnalysisScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val skills by viewModel.userSkills.collectAsState()
    val skillGaps by viewModel.skillGaps.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    var showAddSkillDialog by remember { mutableStateOf(false) }
    var selectedRole by remember(profile?.targetRole) {
        mutableStateOf(profile?.targetRole ?: "Full Stack Engineer")
    }

    val rolesList = BenchmarkCatalog.ROLE_BENCHMARKS.keys.toList()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title & Role Selection
        item {
            Column {
                Text(
                    text = "Career & Skill Analysis",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Benchmark technical proficiencies against top-tier expectations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Readiness Score Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("career_readiness_card"),
                borderColor = PrimaryBlueGlow.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    CircularScoreGauge(
                        score = profile?.readinessScore ?: 0,
                        size = 110.dp,
                        strokeWidth = 9.dp,
                        label = "READINESS"
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        StatusBadge(text = "ROLE TARGET", statusType = "primary")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = profile?.targetRole ?: "Full Stack Engineer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Based on ${skills.size} acquired skills and ${skillGaps.size} benchmark criteria.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Target Role Switcher & Recalibrate
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Benchmark Target Role",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                var expanded by remember { mutableStateOf(false) }

                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BgSurface,
                            contentColor = TextPrimary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedRole)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(BgCard)
                    ) {
                        rolesList.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role, color = TextPrimary) },
                                onClick = {
                                    selectedRole = role
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { viewModel.runCareerAnalysis(selectedRole) },
                    enabled = !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recalibrate_button")
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = TextPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calibrating Readiness…")
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recalibrate Skill Readiness")
                    }
                }
            }
        }

        // Identified Skill Gaps
        item {
            SectionHeader(
                title = "Identified Skill Gaps (${skillGaps.size})",
                subtitle = "Prioritized competencies needed to reach 100% readiness"
            )
        }

        if (skillGaps.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No skill gap data recorded. Click 'Recalibrate' to evaluate.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(skillGaps) { gap ->
                SkillGapItem(gap = gap)
            }
        }

        // Acquired Skills List
        item {
            SectionHeader(
                title = "My Acquired Skills (${skills.size})",
                subtitle = "Self-reported and verified proficiencies",
                actionText = "+ Add Skill",
                onActionClick = { showAddSkillDialog = true }
            )
        }

        items(skills) { skill ->
            AcquiredSkillItem(
                skill = skill,
                onDelete = { viewModel.deleteSkill(skill) }
            )
        }
    }

    if (showAddSkillDialog) {
        AddSkillDialog(
            onDismiss = { showAddSkillDialog = false },
            onConfirm = { name, cat, lvl ->
                viewModel.addOrUpdateSkill(name, cat, lvl)
                showAddSkillDialog = false
            }
        )
    }
}

@Composable
private fun SkillGapItem(gap: SkillGap) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = when (gap.priority) {
            "high" -> DangerRed.copy(alpha = 0.3f)
            "medium" -> WarningAmber.copy(alpha = 0.3f)
            else -> BorderSubtle
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = gap.skillName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = gap.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            StatusBadge(text = gap.priority, statusType = gap.priority)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Level Bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Current: Level ${gap.currentLevel}/5",
                style = MaterialTheme.typography.labelSmall,
                color = AccentCyan
            )
            Text(
                text = "Required: Level ${gap.requiredLevel}/5",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Visual bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(BgMuted)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(gap.currentLevel.toFloat() / 5f)
                    .background(PrimaryBlue)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = gap.recommendation,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun AcquiredSkillItem(
    skill: UserSkill,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = BgCard.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = skill.skillName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (skill.verified) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "${skill.category} · Proficiency: ${skill.proficiencyLevel}/5",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddSkillDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, level: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Programming Languages") }
    var level by remember { mutableStateOf(3) }

    val categories = listOf("Programming Languages", "Frontend", "Backend", "Databases", "DevOps & Cloud", "Architecture", "AI & ML", "Mobile")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add / Update Skill", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Skill Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Proficiency Level: $level / 5", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Slider(
                    value = level.toFloat(),
                    onValueChange = { level = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(thumbColor = PrimaryBlue, activeTrackColor = PrimaryBlue)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), category, level) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = BgCard
    )
}
