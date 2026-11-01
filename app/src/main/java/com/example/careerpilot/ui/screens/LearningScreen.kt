package com.example.careerpilot.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.careerpilot.data.model.LearningResource
import com.example.careerpilot.ui.components.*
import com.example.careerpilot.ui.theme.DesignSystem
import com.example.careerpilot.ui.viewmodel.CareerViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LearningScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val resources by viewModel.learningResources.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var activeStudyResource by remember { mutableStateOf<LearningResource?>(null) }

    val context = LocalContext.current

    val totalCount = resources.size
    val inProgressCount = resources.count { it.status == "IN_PROGRESS" }
    val completedCount = resources.count { it.status == "COMPLETED" || it.isCompleted }
    val totalMinutesLogged = resources.sumOf { it.studyMinutesSpent }
    val overallPercent = if (totalCount > 0) (completedCount * 100) / totalCount else 0

    val filteredResources = remember(resources, selectedFilter) {
        when (selectedFilter) {
            "ALL" -> resources
            "IN_PROGRESS" -> resources.filter { it.status == "IN_PROGRESS" }
            "COMPLETED" -> resources.filter { it.status == "COMPLETED" || it.isCompleted }
            "NOT_STARTED" -> resources.filter { it.status == "NOT_STARTED" }
            "Architecture" -> resources.filter { it.category.equals("Architecture", ignoreCase = true) }
            "Mobile" -> resources.filter { it.category.equals("Mobile", ignoreCase = true) }
            "Databases" -> resources.filter { it.category.equals("Databases", ignoreCase = true) }
            "AI & ML" -> resources.filter { it.category.equals("AI & ML", ignoreCase = true) }
            else -> resources
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = DesignSystem.Spacing.ContentHorizontal),
        contentPadding = PaddingValues(vertical = DesignSystem.Spacing.ContentTop),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Section)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Curated Learning Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DesignSystem.Colors.TextPrimary
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                Text(
                    text = "Structured competency pathways with tracked progress and verified mastery",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DesignSystem.Colors.TextSecondary
                )
            }
        }

        // Overview Metric Card
        item {
            CareerCard(
                modifier = Modifier.fillMaxWidth()
            ) {
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
                                color = DesignSystem.Colors.TextPrimary
                            )
                            Text(
                                text = "$completedCount completed • $inProgressCount in progress • $totalMinutesLogged mins logged",
                                style = MaterialTheme.typography.bodySmall,
                                color = DesignSystem.Colors.TextSecondary
                            )
                        }
                        StatusBadge(
                            text = "$overallPercent% Mastered",
                            statusType = if (overallPercent >= 50) "success" else "primary"
                        )
                    }

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))

                    CareerProgressBar(
                        progress = overallPercent / 100f,
                        color = DesignSystem.Colors.Primary,
                        trackColor = DesignSystem.Colors.BgSurface,
                        height = 8.dp
                    )

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricCard(label = "Total Modules", value = "$totalCount", detail = "Modules", accentColor = DesignSystem.Colors.Primary)
                        MetricCard(label = "In Progress", value = "$inProgressCount", detail = "Active", accentColor = DesignSystem.Colors.Warning)
                        MetricCard(label = "Completed", value = "$completedCount", detail = "Finished", accentColor = DesignSystem.Colors.Success)
                        MetricCard(label = "Study Time", value = "$totalMinutesLogged", detail = "Minutes", accentColor = DesignSystem.Colors.Primary)
                    }
                }
            }
        }

        // Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf(
                    "ALL" to "All ($totalCount)",
                    "IN_PROGRESS" to "In Progress ($inProgressCount)",
                    "COMPLETED" to "Completed ($completedCount)",
                    "NOT_STARTED" to "Not Started (${totalCount - inProgressCount - completedCount})",
                    "Architecture" to "Architecture",
                    "Mobile" to "Mobile",
                    "Databases" to "Databases",
                    "AI & ML" to "AI & ML"
                )
                items(filters) { (key, label) ->
                    CareerChip(
                        text = label,
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        modifier = Modifier
                    )
                }
            }
        }

        // Learning Resource List
        if (filteredResources.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.MenuBook,
                    title = "No Learning Modules Available",
                    description = "Technical study modules, architectural deep dives, and concept quizzes directly verify your proficiencies in distributed systems, Android, and databases.",
                    actionLabel = "Refresh Modules",
                    onActionClick = { viewModel.initializeDefaultDataIfEmpty() }
                )
            }
        } else {
            items(filteredResources, key = { it.id }) { resource ->
                LearningResourceFunctionalCard(
                    resource = resource,
                    onStart = {
                        viewModel.startLearningResource(resource.id)
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
private fun MetricCard(
    label: String,
    value: String,
    detail: String,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(DesignSystem.Sizing.IconSm)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            // Decorative dot - minimal
        }
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DesignSystem.Colors.TextMuted
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
        isCompleted -> DesignSystem.Colors.Success.copy(alpha = 0.5f)
        isInProgress -> DesignSystem.Colors.Primary.copy(alpha = 0.5f)
        else -> DesignSystem.Colors.BorderSubtle
    }

    CareerCard(
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
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(text = resource.category, statusType = "primary")
                    StatusBadge(text = resource.resourceType, statusType = "neutral")
                    val difficultyStatusType = when {
                        resource.difficulty.contains("Beginner", ignoreCase = true) -> "success"
                        resource.difficulty.contains("Advanced", ignoreCase = true) || resource.difficulty.contains("Hard", ignoreCase = true) -> "warning"
                        else -> "primary"
                    }
                    StatusBadge(text = resource.difficulty, statusType = difficultyStatusType)
                }

                when {
                    isCompleted -> StatusBadge(text = "COMPLETED", statusType = "success")
                    isInProgress -> StatusBadge(text = "IN PROGRESS", statusType = "primary")
                    else -> StatusBadge(text = "NOT STARTED", statusType = "neutral")
                }
            }

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))

            // Title
            Text(
                text = resource.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DesignSystem.Colors.TextPrimary
            )

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))

            // Provider Offering & Company Logo & Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    CompanyLogoBadge(company = resource.provider, size = 22.dp)
                    Text(
                        text = "Offered by ${resource.provider}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DesignSystem.Colors.PrimaryLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space0)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = DesignSystem.Colors.TextMuted,
                        modifier = Modifier.size(DesignSystem.Sizing.IconSm)
                    )
                    Text(
                        text = "${resource.estimatedMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = DesignSystem.Colors.TextSecondary
                    )
                }
            }

            if (resource.contentSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                Text(
                    text = resource.contentSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = DesignSystem.Colors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Progress Bar if started or completed
            if (isInProgress || isCompleted) {
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isCompleted) "Verified Mastery (100%)" else "Progress: ${resource.progressPercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted) DesignSystem.Colors.Success else DesignSystem.Colors.PrimaryLight
                        )
                        Text(
                            text = "${resource.studyMinutesSpent}m logged",
                            style = MaterialTheme.typography.labelSmall,
                            color = DesignSystem.Colors.TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space0))
                    CareerProgressBar(
                        progress = resource.progressPercent / 100f,
                        color = if (isCompleted) DesignSystem.Colors.Success else DesignSystem.Colors.Primary,
                        trackColor = DesignSystem.Colors.BgSurface,
                        height = 6.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))

            // Action Buttons based on actual functional state
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    isCompleted -> {
                        CareerButton(
                            text = "Review Material",
                            onClick = onOpenStudyDialog,
                            variant = CareerButton.ButtonVariant.Secondary,
                            icon = Icons.Default.Verified,
                            modifier = Modifier.weight(1f)
                        )
                        CareerButton(
                            text = "Open",
                            onClick = onOpenExternalLink,
                            variant = CareerButton.ButtonVariant.Outline,
                            icon = Icons.Default.OpenInNew,
                            modifier = Modifier.defaultMinSize(minHeight = DesignSystem.Sizing.ButtonHeightSm)
                        )
                    }
                    isInProgress -> {
                        CareerButton(
                            text = "Continue & Verify",
                            onClick = onOpenStudyDialog,
                            variant = CareerButton.ButtonVariant.Primary,
                            icon = Icons.Default.PlayArrow,
                            modifier = Modifier.weight(1f)
                        )
                        CareerButton(
                            text = "Open",
                            onClick = onOpenExternalLink,
                            variant = CareerButton.ButtonVariant.Outline,
                            icon = Icons.Default.OpenInNew,
                            modifier = Modifier.defaultMinSize(minHeight = DesignSystem.Sizing.ButtonHeightSm)
                        )
                    }
                    else -> {
                        CareerButton(
                            text = "Start Learning Resource",
                            onClick = onStart,
                            variant = CareerButton.ButtonVariant.Primary,
                            icon = Icons.Default.PlayArrow,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LearningStudyDialog(
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
                .clip(RoundedCornerShape(DesignSystem.Shape.RadiusLg)),
            color = DesignSystem.Colors.BgSurface,
            border = androidx.compose.foundation.BorderStroke(DesignSystem.Sizing.CardBorderWidth, DesignSystem.Colors.BorderDefault)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DesignSystem.Spacing.Space5)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(text = resource.category, statusType = "primary")
                            StatusBadge(text = resource.resourceType, statusType = "neutral")
                        }
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                        Text(
                            text = resource.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.TextPrimary,
                            maxLines = 2
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = DesignSystem.Colors.TextMuted)
                    }
                }

                CareerDivider(modifier = Modifier.padding(vertical = DesignSystem.Spacing.Space2))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)
                ) {
                    // Resource Details Card
                    CareerCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Item)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Offered By:", style = MaterialTheme.typography.bodySmall, color = DesignSystem.Colors.TextSecondary)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1)
                                ) {
                                    CompanyLogoBadge(company = resource.provider, size = 20.dp)
                                    Text(text = resource.provider, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = DesignSystem.Colors.PrimaryLight)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Skill Tags:", style = MaterialTheme.typography.bodySmall, color = DesignSystem.Colors.TextSecondary)
                                Text(text = resource.skillTags, style = MaterialTheme.typography.bodySmall, color = DesignSystem.Colors.TextPrimary)
                            }

                            if (resource.contentSummary.isNotBlank()) {
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                                Text(
                                    text = resource.contentSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DesignSystem.Colors.TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))
                            CareerButton(
                                text = "Open Full Learning Material Online",
                                onClick = onOpenLink,
                                variant = CareerButton.ButtonVariant.Secondary,
                                icon = Icons.Default.OpenInNew,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Study Time Logging & Progress Slider
                    CareerCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space2)) {
                            Text(
                                text = "Track Reading & Study Time",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DesignSystem.Colors.TextPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1)
                            ) {
                                CareerButton(
                                    text = "+15m Logged",
                                    onClick = {
                                        additionalTimeLogged += 15
                                        currentProgress = minOf(95, currentProgress + 15)
                                        onUpdateProgress(15, currentProgress, studyNotes)
                                    },
                                    variant = CareerButton.ButtonVariant.Secondary,
                                    icon = Icons.Default.Timer,
                                    modifier = Modifier.weight(1f)
                                )
                                CareerButton(
                                    text = "+30m Logged",
                                    onClick = {
                                        additionalTimeLogged += 30
                                        currentProgress = minOf(95, currentProgress + 30)
                                        onUpdateProgress(30, currentProgress, studyNotes)
                                    },
                                    variant = CareerButton.ButtonVariant.Secondary,
                                    icon = Icons.Default.Timer,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Study Progress", style = MaterialTheme.typography.labelSmall, color = DesignSystem.Colors.TextSecondary)
                                Text(text = "$currentProgress% (${resource.studyMinutesSpent + additionalTimeLogged}m total logged)", style = MaterialTheme.typography.labelSmall, color = DesignSystem.Colors.PrimaryLight)
                            }

                            Slider(
                                value = currentProgress.toFloat(),
                                onValueChange = {
                                    currentProgress = it.toInt()
                                    onUpdateProgress(0, currentProgress, studyNotes)
                                },
                                valueRange = 0f..95f,
                                colors = SliderDefaults.colors(
                                    thumbColor = DesignSystem.Colors.Primary,
                                    activeTrackColor = DesignSystem.Colors.Primary,
                                    inactiveTrackColor = DesignSystem.Colors.BgSurface
                                )
                            )
                        }
                    }

                    // Comprehension Verification Quiz (Strict Requirement for Completion)
                    CareerCard(
                        modifier = Modifier
                            .fillMaxWidth(),
                        borderColor = if (quizSuccess) DesignSystem.Colors.Success.copy(alpha = 0.6f) else if (quizError) DesignSystem.Colors.Danger.copy(alpha = 0.6f) else DesignSystem.Colors.BorderDefault
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space2)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Comprehension Verification",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DesignSystem.Colors.TextPrimary
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
                                color = DesignSystem.Colors.TextPrimary
                            )

                            // Quiz options
                            optionsList.forEachIndexed { index, optionText ->
                                val isSelected = selectedOption == index
                                val isCorrectOption = index == resource.quizCorrectIndex

                                val optionBorderColor = when {
                                    quizSuccess && isCorrectOption -> DesignSystem.Colors.Success
                                    quizError && isSelected -> DesignSystem.Colors.Danger
                                    isSelected -> DesignSystem.Colors.Primary
                                    else -> DesignSystem.Colors.BorderDefault
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(DesignSystem.Shape.RadiusMd))
                                        .background(if (isSelected) DesignSystem.Colors.PrimaryContainer else DesignSystem.Colors.BgCard)
                                        .border(DesignSystem.Sizing.CardBorderWidth, optionBorderColor, RoundedCornerShape(DesignSystem.Shape.RadiusMd))
                                        .clickable(enabled = !quizSuccess) {
                                            selectedOption = index
                                            quizError = false
                                        }
                                        .padding(DesignSystem.Spacing.Space3),
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
                                        colors = RadioButtonDefaults.colors(selectedColor = DesignSystem.Colors.Primary)
                                    )
                                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.Space1))
                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DesignSystem.Colors.TextPrimary
                                    )
                                }
                            }

                            if (quizError) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1)
                                ) {
                                    Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = DesignSystem.Colors.Danger, modifier = Modifier.size(DesignSystem.Sizing.IconMd))
                                    Text(
                                        text = "Incorrect answer. Please review the material concept and try again.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DesignSystem.Colors.Danger
                                    )
                                }
                            }

                            if (quizSuccess) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1)
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = DesignSystem.Colors.Success, modifier = Modifier.size(DesignSystem.Sizing.IconMd))
                                    Text(
                                        text = "Comprehension verified! +${resource.estimatedMinutes} XP competency recorded.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DesignSystem.Colors.Success
                                    )
                                }
                            }

                            if (!quizSuccess) {
                                CareerButton(
                                    text = "Verify & Complete Module",
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
                                    variant = CareerButton.ButtonVariant.Success,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = DesignSystem.Sizing.TouchTargetMin)
                                )
                            }
                        }
                    }

                    // Personal Notes
                    CareerCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Item)) {
                            Text(
                                text = "Personal Study Notes & Key Takeaways",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DesignSystem.Colors.TextPrimary
                            )
                            CareerTextField(
                                value = studyNotes,
                                onValueChange = {
                                    studyNotes = it
                                    onUpdateProgress(0, currentProgress, studyNotes)
                                },
                                placeholder = "Record key technical concepts, interview talking points, or architectural trade-offs...",
                                singleLine = false,
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))

                // Footer
                CareerButton(
                    text = "Close Study Session",
                    onClick = onDismiss,
                    variant = CareerButton.ButtonVariant.Ghost,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
