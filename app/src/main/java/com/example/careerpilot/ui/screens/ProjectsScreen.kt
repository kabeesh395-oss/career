package com.example.careerpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.model.PortfolioProject
import com.example.careerpilot.ui.components.GlassCard
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.components.StatusBadge
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel

@Composable
fun ProjectsScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Portfolio Project Hub",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Showcase production systems proving your technical competencies",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_project_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Project")
                }
            }
        }

        // Projects List
        if (projects.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No portfolio projects added yet. Click 'Add Project' to record your deliverables.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PortfolioProjectCard(
    project: PortfolioProject,
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = project.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(text = project.status, statusType = if (project.status == "completed") "success" else "warning")
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = project.description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Technologies Chips
        if (project.technologies.isNotBlank()) {
            val techs = project.technologies.split(",").map { it.trim() }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                techs.forEach { tech ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BgSurface)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(text = tech, style = MaterialTheme.typography.labelSmall, color = PrimaryBlueGlow)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (project.repositoryUrl.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                    Text(project.repositoryUrl, style = MaterialTheme.typography.labelSmall, color = AccentCyan)
                }
            }
            if (project.liveUrl.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    Text(project.liveUrl, style = MaterialTheme.typography.labelSmall, color = SuccessGreen)
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
        title = { Text("Add Portfolio Project", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description & Problem Solved") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tech,
                    onValueChange = { tech = it },
                    label = { Text("Tech Stack (e.g. Kotlin, Redis, Docker)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = repo,
                    onValueChange = { repo = it },
                    label = { Text("Repository URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = live,
                    onValueChange = { live = it },
                    label = { Text("Live URL (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), desc.trim(), repo.trim(), live.trim(), status, tech.trim(), skills.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Add Project")
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
