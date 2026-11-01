package com.example.careerpilot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.model.PortfolioProject
import com.example.careerpilot.ui.components.*
import com.example.careerpilot.ui.theme.DesignSystem
import com.example.careerpilot.ui.viewmodel.CareerViewModel

@Composable
fun ProjectsScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

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
        item {
            Column {
                Text(
                    text = "Portfolio Projects",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DesignSystem.Colors.TextPrimary
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                Text(
                    text = "Showcase production systems proving your technical competencies",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DesignSystem.Colors.TextSecondary
                )
            }
        }

        item {
            CareerCardHighlight(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showAddDialog = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add Portfolio Project",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                        Text(
                            text = "Document repositories, live demos, and architectural solutions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DesignSystem.Colors.TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Project",
                        tint = DesignSystem.Colors.PrimaryLight,
                        modifier = Modifier
                            .size(DesignSystem.Sizing.IconLg)
                            .clickable { showAddDialog = true }
                    )
                }
            }
        }

        // Projects List
        if (projects.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Code,
                    title = "No Portfolio Projects Documented",
                    description = "Showcase production systems, repositories, and architectural solutions proving your hands-on competencies for ${profile?.targetRole ?: "target roles"}.",
                    actionLabel = "Add Project",
                    onActionClick = { showAddDialog = true }
                )
            }
        } else {
            items(projects) { project ->
                PortfolioProjectCard(
                    project = project,
                    onDelete = { viewModel.deletePortfolioProject(project) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddProjectDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, repo, live, status, tech, skills ->
                viewModel.addPortfolioProject(title, desc, repo, live, status, tech, skills)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun PortfolioProjectCard(
    project: PortfolioProject,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DesignSystem.Colors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1)
                ) {
                    StatusBadge(
                        text = project.status,
                        statusType = if (project.status == "completed") "success" else "primary"
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(44.dp)) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = DesignSystem.Colors.TextMuted,
                    modifier = Modifier.size(DesignSystem.Sizing.IconMd)
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))
        Text(
            text = project.description,
            style = MaterialTheme.typography.bodyMedium,
            color = DesignSystem.Colors.TextSecondary,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))

        // Technologies Chips
        if (project.technologies.isNotBlank()) {
            val techs = project.technologies.split(",").map { it.trim() }
            androidx.compose.foundation.lazy.grid.GridCells.Fixed(1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1),
                verticalAlignment = Alignment.CenterVertically
            ) {
                techs.forEach { tech ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(DesignSystem.Shape.RadiusXs))
                            .background(DesignSystem.Colors.BgSurface)
                            .border(DesignSystem.Sizing.CardBorderWidth, DesignSystem.Colors.BorderDefault, RoundedCornerShape(DesignSystem.Shape.RadiusXs))
                            .padding(horizontal = DesignSystem.Spacing.Space2, vertical = DesignSystem.Spacing.Space0)
                    ) {
                        Text(text = tech, style = MaterialTheme.typography.labelSmall, color = DesignSystem.Colors.PrimaryLight)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))

        Row(
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space3),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (project.repositoryUrl.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1)) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = DesignSystem.Colors.AccentCyan, modifier = Modifier.size(DesignSystem.Sizing.IconMd))
                    Text(project.repositoryUrl, style = MaterialTheme.typography.labelSmall, color = DesignSystem.Colors.AccentCyan, maxLines = 1)
                }
            }
            if (project.liveUrl.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1)) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = DesignSystem.Colors.Success, modifier = Modifier.size(DesignSystem.Sizing.IconMd))
                    Text(project.liveUrl, style = MaterialTheme.typography.labelSmall, color = DesignSystem.Colors.Success, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun AddProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, repo: String, live: String, status: String, tech: String, skills: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var repo by remember { mutableStateOf("") }
    var live by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("in_progress") }
    var tech by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Portfolio Project", color = DesignSystem.Colors.TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space2)) {
                CareerTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Project Title",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                CareerTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = "Description & Problem Solved",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )
                CareerTextField(
                    value = tech,
                    onValueChange = { tech = it },
                    label = "Tech Stack (e.g. Kotlin, Redis, Docker)",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                CareerTextField(
                    value = repo,
                    onValueChange = { repo = it },
                    label = "Repository URL",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                CareerTextField(
                    value = live,
                    onValueChange = { live = it },
                    label = "Live URL (Optional)",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            CareerButton(
                text = "Add Project",
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), desc.trim(), repo.trim(), live.trim(), status, tech.trim(), skills.trim())
                    }
                },
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
