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
import com.example.careerpilot.ui.components.*
import com.example.careerpilot.ui.theme.DesignSystem
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
    var selectedRole by remember { mutableStateOf(profile?.targetRole ?: "Full Stack Engineer") }

    val rolesList = listOf(
        "Full Stack Engineer", "Frontend Engineer", "Backend Engineer",
        "DevOps Engineer", "Cloud Architect", "Data Engineer",
        "Machine Learning Engineer", "Security Engineer", "Mobile Engineer"
    )

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
                    text = "Career Analysis",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DesignSystem.Colors.TextPrimary
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                Text(
                    text = "Benchmark technical proficiencies against top-tier expectations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DesignSystem.Colors.TextSecondary
                )
            }
        }

        // Readiness Score Card
        item {
            CareerCardHighlight(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("career_readiness_card"),
                onClick = { /* skill matrix navigation */ }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space3)
                ) {
                    CircularScoreGauge(
                        score = profile?.readinessScore ?: 0,
                        size = 96.dp,
                        strokeWidth = 7.dp,
                        label = "READINESS"
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ROLE TARGET",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DesignSystem.Colors.TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                        Text(
                            text = profile?.targetRole ?: "Full Stack Engineer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                        Text(
                            text = "Based on ${skills.size} acquired skills and ${skillGaps.size} benchmark criteria.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DesignSystem.Colors.TextSecondary
                        )
                    }
                }
            }
        }

        // Target Role Switcher & Recalibrate
        item {
            CareerCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Benchmark Target Role",
                    style = MaterialTheme.typography.titleMedium,
                    color = DesignSystem.Colors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))

                var expanded by remember { mutableStateOf(false) }

                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(DesignSystem.Shape.RadiusMd),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = DesignSystem.Colors.BgSurface,
                            contentColor = DesignSystem.Colors.TextPrimary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DesignSystem.Colors.BorderDefault))
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
                        modifier = Modifier.background(DesignSystem.Colors.BgCard)
                    ) {
                        rolesList.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role, color = DesignSystem.Colors.TextPrimary) },
                                onClick = {
                                    selectedRole = role
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))
                CareerButton(
                    text = if (isAnalyzing) "Calibrating Readiness…" else "Recalibrate Skill Readiness",
                    onClick = { viewModel.runCareerAnalysis(selectedRole) },
                    enabled = !isAnalyzing,
                    variant = CareerButton.ButtonVariant.Primary,
                    loading = isAnalyzing,
                    icon = Icons.Default.Refresh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recalibrate_button")
                )
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
                EmptyStateCard(
                    icon = Icons.Default.Assessment,
                    title = "No Skill Gap Data",
                    description = "Click 'Recalibrate' to evaluate your skill profile against role benchmarks.",
                    actionLabel = "Recalibrate",
                    onActionClick = { viewModel.runCareerAnalysis(selectedRole) }
                )
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
    val priorityColor = when (gap.priority) {
        "high" -> DesignSystem.Colors.Danger
        "medium" -> DesignSystem.Colors.Warning
        else -> DesignSystem.Colors.TextMuted
    }
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
                    text = gap.skillName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DesignSystem.Colors.TextPrimary
                )
                Text(
                    text = gap.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = DesignSystem.Colors.TextMuted
                )
            }
            StatusBadge(text = gap.priority, statusType = gap.priority)
        }

        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))

        // Level Bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Current: Level ${gap.currentLevel}/5",
                style = MaterialTheme.typography.labelSmall,
                color = DesignSystem.Colors.PrimaryLight
            )
            Text(
                text = "Required: Level ${gap.requiredLevel}/5",
                style = MaterialTheme.typography.labelSmall,
                color = DesignSystem.Colors.TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))

        // Visual bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignSystem.Sizing.DividerThickness * 3)
                .clip(RoundedCornerShape(DesignSystem.Shape.RadiusXs))
                .background(DesignSystem.Colors.BgSurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(gap.currentLevel.toFloat() / 5f)
                    .background(priorityColor)
            )
        }

        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))
        Text(
            text = gap.recommendation,
            style = MaterialTheme.typography.bodySmall,
            color = DesignSystem.Colors.TextSecondary,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun AcquiredSkillItem(
    skill: UserSkill,
    onDelete: () -> Unit
) {
    CareerCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1)) {
                    Text(
                        text = skill.skillName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DesignSystem.Colors.TextPrimary
                    )
                    if (skill.verified) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = DesignSystem.Colors.Success,
                            modifier = Modifier.size(DesignSystem.Sizing.IconMd)
                        )
                    }
                }
                Text(
                    text = "${skill.category} · Proficiency: ${skill.proficiencyLevel}/5",
                    style = MaterialTheme.typography.bodySmall,
                    color = DesignSystem.Colors.TextMuted
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = DesignSystem.Colors.TextMuted,
                    modifier = Modifier.size(DesignSystem.Sizing.IconMd)
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
        title = { Text("Add / Update Skill", color = DesignSystem.Colors.TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space3)) {
                CareerTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Skill Name",
                    singleLine = true,
                    isError = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Proficiency Level: $level / 5",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DesignSystem.Colors.TextSecondary
                )
                Slider(
                    value = level.toFloat(),
                    onValueChange = { level = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = DesignSystem.Colors.Primary,
                        activeTrackColor = DesignSystem.Colors.Primary
                    )
                )
            }
        },
        confirmButton = {
            CareerButton(
                text = "Save",
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), category, level) },
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
