package com.example.careerpilot.ui.screens

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.model.JobApplication
import com.example.careerpilot.data.repository.GeneratedOutreachLetter
import com.example.careerpilot.ui.components.GlassCard
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.components.StatusBadge
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel

@Composable
fun ApplicationTrackerScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val applications by viewModel.jobApplications.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilterStage by remember { mutableStateOf("ALL") }
    var outreachApp by remember { mutableStateOf<JobApplication?>(null) }
    val clipboardManager = LocalClipboardManager.current
    var copiedToast by remember { mutableStateOf<String?>(null) }

    // Dialog form state
    var companyInput by remember { mutableStateOf("") }
    var roleTitleInput by remember { mutableStateOf("") }
    var stageInput by remember { mutableStateOf("APPLIED") }
    var locationInput by remember { mutableStateOf("San Francisco, CA (Hybrid)") }
    var salaryInput by remember { mutableStateOf("$160,000 - $190,000") }
    var notesInput by remember { mutableStateOf("") }
    var interviewDateInput by remember { mutableStateOf("Upcoming") }

    val stages = listOf("ALL", "WISHLIST", "APPLIED", "SCREENING", "TECHNICAL", "OFFER", "REJECTED")

    val filteredApplications = if (selectedFilterStage == "ALL") {
        applications
    } else {
        applications.filter { it.stage.equals(selectedFilterStage, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Job Application CRM",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Track interview stages, salary benchmarks & deadlines",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_application_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Job")
                }
            }
        }

        // Summary Pipeline Metrics
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Pipeline Conversion Velocity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PipelineMetricBadge("Active", "${applications.filter { it.stage != "REJECTED" }.size}", PrimaryBlueGlow)
                    PipelineMetricBadge("Screening", "${applications.filter { it.stage == "SCREENING" }.size}", WarningAmber)
                    PipelineMetricBadge("Technical", "${applications.filter { it.stage == "TECHNICAL" }.size}", AccentPurple)
                    PipelineMetricBadge("Offers", "${applications.filter { it.stage == "OFFER" }.size}", SuccessGreen)
                }

            }
        }

        // Salary Negotiation Benchmark Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = SuccessGreen.copy(alpha = 0.4f),
                backgroundColor = BgSurfaceElevated
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
                    Column {
                        Text(
                            text = "Compensation Benchmarker",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Target Band: $155,000 - $195,000 · Leverage equity grants & sign-on multipliers",
                            style = MaterialTheme.typography.bodySmall,
                            color = SuccessGreen
                        )
                    }
                }
            }
        }

        // Stage Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(stages) { stage ->
                    val isSelected = selectedFilterStage == stage
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilterStage = stage },
                        label = { Text(stage, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = TextPrimary,
                            containerColor = BgCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) PrimaryBlueGlow else BorderSubtle,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }
        }

        // Applications List
        if (applications.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkOutline,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No Applications in Pipeline",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Track your job applications, interviews, and recruiter follow-ups. Tap '+ New' to log your first target position.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else if (filteredApplications.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No applications found in '$selectedFilterStage' stage.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(filteredApplications) { app ->
                ApplicationCard(
                    app = app,
                    onStageChange = { newStage -> viewModel.updateApplicationStage(app, newStage) },
                    onDelete = { viewModel.deleteApplication(app) },
                    onOutreachClick = { outreachApp = app },
                    onReminderClick = { viewModel.scheduleInterviewReminder(app, "24h") }
                )
            }
        }
    }

    // AI Recruiter Outreach & Cover Letter Modal
    outreachApp?.let { app ->
        val outreachData = remember(app) {
            viewModel.generateOutreachForApplication(app)
        }

        AlertDialog(
            onDismissRequest = { outreachApp = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = AccentCyan)
                    Text("AI Recruiter Outreach & Cover Letter", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Target: ${app.company} · ${app.roleTitle}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("LinkedIn InMail Script", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(outreachData.linkedInInMail))
                                    copiedToast = "✓ LinkedIn InMail copied to clipboard!"
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy InMail", fontSize = 10.sp)
                            }
                        }

                        Text(
                            text = outreachData.linkedInInMail,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BgSurface, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tailored Cover Letter", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(outreachData.tailoredCoverLetter))
                                    copiedToast = "✓ Cover letter copied to clipboard!"
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Letter", fontSize = 10.sp)
                            }
                        }

                        Text(
                            text = outreachData.tailoredCoverLetter,
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BgSurface, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        )
                    }

                    copiedToast?.let { toast ->
                        item {
                            Text(
                                text = toast,
                                fontSize = 11.sp,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        outreachApp = null
                        copiedToast = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Done")
                }
            }
        )
    }

    // Add Application Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Track New Opportunity", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = companyInput,
                        onValueChange = { companyInput = it },
                        label = { Text("Company Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = roleTitleInput,
                        onValueChange = { roleTitleInput = it },
                        label = { Text("Role Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = salaryInput,
                        onValueChange = { salaryInput = it },
                        label = { Text("Compensation Target") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Recruiter & Interview Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (companyInput.isNotBlank() && roleTitleInput.isNotBlank()) {
                            viewModel.addJobApplication(
                                company = companyInput.trim(),
                                roleTitle = roleTitleInput.trim(),
                                stage = stageInput,
                                location = locationInput.trim(),
                                salaryOffered = salaryInput.trim(),
                                notes = notesInput.trim(),
                                interviewDate = interviewDateInput.trim()
                            )
                            showAddDialog = false
                            companyInput = ""
                            roleTitleInput = ""
                            notesInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Save to Pipeline")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PipelineMetricBadge(label: String, count: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
private fun ApplicationCard(
    app: JobApplication,
    onStageChange: (String) -> Unit,
    onDelete: () -> Unit,
    onOutreachClick: () -> Unit,
    onReminderClick: () -> Unit
) {
    val stageColor = when (app.stage) {
        "OFFER" -> SuccessGreen
        "TECHNICAL" -> AccentPurple
        "SCREENING" -> WarningAmber
        "REJECTED" -> DangerRed
        else -> PrimaryBlueGlow
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.company,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = app.roleTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentCyan
                )
            }

            StatusBadge(text = app.stage, color = stageColor)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Text(text = app.location, fontSize = 12.sp, color = TextSecondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                Text(text = app.salaryOffered, fontSize = 12.sp, color = SuccessGreen)
            }
        }

        if (app.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Notes: ${app.notes}",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgSurfaceElevated, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick AI Outreach & Reminder Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onOutreachClick,
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("AI InMail & Letter", fontSize = 11.sp, color = AccentCyan)
            }

            OutlinedButton(
                onClick = onReminderClick,
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Push Reminder", fontSize = 11.sp, color = WarningAmber)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Stage Transitions & Delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (app.stage != "TECHNICAL") {
                    FilledTonalButton(
                        onClick = { onStageChange("TECHNICAL") },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Move to Tech Round", fontSize = 10.sp)
                    }
                }
                if (app.stage != "OFFER") {
                    Button(
                        onClick = { onStageChange("OFFER") },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Offer Received 🎉", fontSize = 10.sp)
                    }
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}
