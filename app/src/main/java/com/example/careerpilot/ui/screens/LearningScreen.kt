package com.example.careerpilot.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.careerpilot.data.model.LearningResource
import com.example.careerpilot.ui.components.GlassCard
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.components.StatusBadge
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val resources by viewModel.learningResources.collectAsState()
    val context = LocalContext.current

    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "IN_PROGRESS", "COMPLETED", "NOT_STARTED"
    var activeStudyResource by remember { mutableStateOf<LearningResource?>(null) }

    val filteredResources = remember(resources, selectedFilter) {
        when (selectedFilter) {
            "IN_PROGRESS" -> resources.filter { it.status == "IN_PROGRESS" }
            "COMPLETED" -> resources.filter { it.status == "COMPLETED" || it.isCompleted }
            "NOT_STARTED" -> resources.filter { it.status == "NOT_STARTED" && !it.isCompleted }
            "Architecture" -> resources.filter { it.category.equals("Architecture", ignoreCase = true) }
            "Mobile" -> resources.filter { it.category.equals("Mobile", ignoreCase = true) }
            "Databases" -> resources.filter { it.category.equals("Databases", ignoreCase = true) }
            "AI & ML" -> resources.filter { it.category.equals("AI & ML", ignoreCase = true) }
            else -> resources
        }
    }

    val totalCount = resources.size
    val inProgressCount = resources.count { it.status == "IN_PROGRESS" }
    val completedCount = resources.count { it.status == "COMPLETED" || it.isCompleted }
    val totalMinutesLogged = resources.sumOf { it.studyMinutesSpent }
    val overallPercent = if (totalCount > 0) (completedCount * 100) / totalCount else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Curated Learning Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Structured competency pathways with tracked progress and verified mastery",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Overview Metric Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Learning Mastery",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "$completedCount completed • $inProgressCount in progress • $totalMinutesLogged mins logged",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        StatusBadge(
                            text = "$overallPercent% Mastered",
                            statusType = if (overallPercent >= 50) "success" else "primary"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { overallPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AccentCyan,
                        trackColor = BgMuted,
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricSmallItem(label = "Total Modules", value = "$totalCount")
                        MetricSmallItem(label = "In Progress", value = "$inProgressCount", color = AccentAmber)
                        MetricSmallItem(label = "Completed", value = "$completedCount", color = SuccessGreen)
                        MetricSmallItem(label = "Study Time", value = "${totalMinutesLogged}m", color = PrimaryBlue)
                    }
                }
            }
        }

        // Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf(
                    "ALL" to "All (${resources.size})",
                    "IN_PROGRESS" to "In Progress ($inProgressCount)",
                    "COMPLETED" to "Completed ($completedCount)",
                    "NOT_STARTED" to "Not Started (${totalCount - inProgressCount - completedCount})",
                    "Architecture" to "Architecture",
                    "Mobile" to "Mobile",
                    "Databases" to "Databases",
                    "AI & ML" to "AI & ML"
                )
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.25f),
                            selectedLabelColor = AccentCyan,
                            containerColor = BgCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == key,
                            borderColor = if (selectedFilter == key) PrimaryBlue else BorderSubtle,
                            selectedBorderColor = AccentCyan
                        )
                    )
                }
            }
        }

        // Learning Resource List
        if (filteredResources.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No learning modules in this filter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(filteredResources, key = { it.id }) { resource ->
                LearningResourceFunctionalCard(
                    resource = resource,
                    onStart = {
                        viewModel.startLearningResource(resource.id)
                        // Open external link
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Non-fatal
                        }
                    },
                    onOpenStudyDialog = {
                        activeStudyResource = resource
                    },
                    onOpenExternalLink = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Non-fatal
                        }
                    },
                    onReset = {
                        viewModel.resetLearningResource(resource.id)
                    }
                )
            }
        }
    }

    // Active Interactive Study & Verification Dialog
    activeStudyResource?.let { res ->
        LearningStudyDialog(
            resource = res,
            onDismiss = { activeStudyResource = null },
            onUpdateProgress = { mins, pct, notes ->
                viewModel.updateLearningProgress(res.id, mins, pct, notes)
            },
            onVerifyCompletion = { quizIdx, notes, onResult ->
                viewModel.verifyAndCompleteLearning(res.id, quizIdx, notes, onResult)
            },
            onOpenLink = {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(res.url))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Non-fatal
                }
            }
        )
    }
}

@Composable
private fun MetricSmallItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
private fun LearningResourceFunctionalCard(
    resource: LearningResource,
    onStart: () -> Unit,
    onOpenStudyDialog: () -> Unit,
    onOpenExternalLink: () -> Unit,
    onReset: () -> Unit
) {
    val isCompleted = resource.status == "COMPLETED" || resource.isCompleted
    val isInProgress = resource.status == "IN_PROGRESS" && !isCompleted

    val borderColor = when {
        isCompleted -> SuccessGreen.copy(alpha = 0.5f)
        isInProgress -> AccentCyan.copy(alpha = 0.5f)
        else -> BorderSubtle
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("learning_card_${resource.id}"),
        borderColor = borderColor
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Status Badge & Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(text = resource.category, statusType = "primary")
                    StatusBadge(text = resource.resourceType, statusType = "neutral")
                    StatusBadge(text = resource.difficulty, statusType = "neutral")
                }

                when {
                    isCompleted -> StatusBadge(text = "COMPLETED", statusType = "success")
                    isInProgress -> StatusBadge(text = "IN PROGRESS", statusType = "primary")
                    else -> StatusBadge(text = "NOT STARTED", statusType = "neutral")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = resource.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Provider & Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Source: ${resource.provider}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentCyan
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${resource.estimatedMinutes} min estimated",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            if (resource.contentSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = resource.contentSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Progress Bar if started or completed
            if (isInProgress || isCompleted) {
                Spacer(modifier = Modifier.height(12.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isCompleted) "Verified Mastery (100%)" else "Progress: ${resource.progressPercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted) SuccessGreen else AccentCyan
                        )
                        Text(
                            text = "${resource.studyMinutesSpent}m logged",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { resource.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isCompleted) SuccessGreen else AccentCyan,
                        trackColor = BgMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons based on actual functional state
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    isCompleted -> {
                        Button(
                            onClick = onOpenStudyDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Review Material", color = SuccessGreen, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenExternalLink,
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
                            modifier = Modifier.size(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open URL",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    isInProgress -> {
                        Button(
                            onClick = onOpenStudyDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Continue & Verify", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenExternalLink,
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
                            modifier = Modifier.size(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open URL",
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    else -> {
                        Button(
                            onClick = onStart,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Learning Resource", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LearningStudyDialog(
    resource: LearningResource,
    onDismiss: () -> Unit,
    onUpdateProgress: (additionalMinutes: Int, newProgressPercent: Int, notes: String) -> Unit,
    onVerifyCompletion: (selectedQuizIndex: Int, notes: String, onResult: (Boolean) -> Unit) -> Unit,
    onOpenLink: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var quizError by remember { mutableStateOf(false) }
    var quizSuccess by remember { mutableStateOf(resource.isCompleted || resource.status == "COMPLETED") }
    var studyNotes by remember { mutableStateOf(resource.notes) }
    var currentProgress by remember { mutableStateOf(if (resource.progressPercent > 0) resource.progressPercent else 25) }
    var additionalTimeLogged by remember { mutableStateOf(0) }

    val optionsList = remember(resource.quizOptions) {
        if (resource.quizOptions.isNotBlank()) {
            resource.quizOptions.split("|")
        } else {
            listOf("Satisfies distributed consistency constraints", "Executes concurrently across cluster", "Fails gracefully with circuit breaker")
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(16.dp)),
            color = BgSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(text = resource.category, statusType = "primary")
                            StatusBadge(text = resource.resourceType, statusType = "neutral")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = resource.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderSubtle)

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Resource Details Card
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Provider / Publisher:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(text = resource.provider, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AccentCyan)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Skill Tags:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(text = resource.skillTags, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }

                            if (resource.contentSummary.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = resource.contentSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onOpenLink,
                                colors = ButtonDefaults.buttonColors(containerColor = BgCard),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Full Learning Material Online", color = AccentCyan, fontSize = 13.sp)
                            }
                        }
                    }

                    // Study Time Logging & Progress Slider
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Track Reading & Study Time",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        additionalTimeLogged += 15
                                        currentProgress = minOf(95, currentProgress + 15)
                                        onUpdateProgress(15, currentProgress, studyNotes)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BgMuted),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+15m Logged", fontSize = 12.sp, color = TextPrimary)
                                }

                                Button(
                                    onClick = {
                                        additionalTimeLogged += 30
                                        currentProgress = minOf(95, currentProgress + 30)
                                        onUpdateProgress(30, currentProgress, studyNotes)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BgMuted),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+30m Logged", fontSize = 12.sp, color = TextPrimary)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Study Progress", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(text = "$currentProgress% (${resource.studyMinutesSpent + additionalTimeLogged}m total logged)", style = MaterialTheme.typography.labelSmall, color = AccentCyan)
                            }

                            Slider(
                                value = currentProgress.toFloat(),
                                onValueChange = {
                                    currentProgress = it.toInt()
                                    onUpdateProgress(0, currentProgress, studyNotes)
                                },
                                valueRange = 0f..95f,
                                colors = SliderDefaults.colors(
                                    thumbColor = AccentCyan,
                                    activeTrackColor = AccentCyan,
                                    inactiveTrackColor = BgMuted
                                )
                            )
                        }
                    }

                    // Comprehension Verification Quiz (Strict Requirement for Completion)
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (quizSuccess) SuccessGreen.copy(alpha = 0.6f) else if (quizError) DangerRed.copy(alpha = 0.6f) else BorderSubtle
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Comprehension Verification",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (quizSuccess) {
                                    StatusBadge(text = "VERIFIED", statusType = "success")
                                } else {
                                    StatusBadge(text = "REQUIRED FOR COMPLETION", statusType = "warning")
                                }
                            }

                            Text(
                                text = if (resource.quizQuestion.isNotBlank()) resource.quizQuestion else "Confirm core architecture principle for this topic:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )

                            // Quiz options
                            optionsList.forEachIndexed { index, optionText ->
                                val isSelected = selectedOption == index
                                val isCorrectOption = index == resource.quizCorrectIndex

                                val optionBorderColor = when {
                                    quizSuccess && isCorrectOption -> SuccessGreen
                                    quizError && isSelected -> DangerRed
                                    isSelected -> AccentCyan
                                    else -> BorderSubtle
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else BgCard)
                                        .border(1.dp, optionBorderColor, RoundedCornerShape(8.dp))
                                        .clickable(enabled = !quizSuccess) {
                                            selectedOption = index
                                            quizError = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            if (!quizSuccess) {
                                                selectedOption = index
                                                quizError = false
                                            }
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = AccentCyan)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                }
                            }

                            if (quizError) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Incorrect answer. Please review the material concept and try again.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DangerRed
                                    )
                                }
                            }

                            if (quizSuccess) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Comprehension verified! +${resource.estimatedMinutes} XP competency recorded.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SuccessGreen
                                    )
                                }
                            }

                            if (!quizSuccess) {
                                Button(
                                    onClick = {
                                        selectedOption?.let { idx ->
                                            onVerifyCompletion(idx, studyNotes) { success ->
                                                if (success) {
                                                    quizSuccess = true
                                                    quizError = false
                                                    currentProgress = 100
                                                } else {
                                                    quizError = true
                                                    quizSuccess = false
                                                }
                                            }
                                        }
                                    },
                                    enabled = selectedOption != null,
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Verify & Complete Module")
                                }
                            }
                        }
                    }

                    // Personal Notes
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Personal Study Notes & Key Takeaways",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            OutlinedTextField(
                                value = studyNotes,
                                onValueChange = {
                                    studyNotes = it
                                    onUpdateProgress(0, currentProgress, studyNotes)
                                },
                                placeholder = { Text("Record key technical concepts, interview talking points, or architectural trade-offs...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = BgCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Study Session", color = TextSecondary)
                }
            }
        }
    }
}
