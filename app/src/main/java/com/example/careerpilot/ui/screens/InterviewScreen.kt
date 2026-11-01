package com.example.careerpilot.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.careerpilot.data.model.ConversationMessage
import com.example.careerpilot.data.model.InterviewSession
import com.example.careerpilot.data.repository.ConversationalInterviewEngine
import com.example.careerpilot.ui.components.*
import com.example.careerpilot.ui.theme.DesignSystem
import com.example.careerpilot.ui.viewmodel.CareerViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InterviewScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val activeSession by viewModel.activeInterviewSession.collectAsState()
    val currentQIndex by viewModel.currentInterviewQuestionIndex.collectAsState()
    val lastEval by viewModel.lastEvaluation.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val history by viewModel.interviews.collectAsState()
    val conversationMessages by viewModel.conversationalMessages.collectAsState()

    var answerText by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("Senior") }
    var selectedInterviewType by remember { mutableStateOf("System Design & Probing") }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Auto-scroll on new message
    LaunchedEffect(conversationMessages.size) {
        if (conversationMessages.isNotEmpty()) {
            listState.animateScrollToItem(conversationMessages.size)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = DesignSystem.Spacing.ContentHorizontal),
        contentPadding = PaddingValues(
            top = DesignSystem.Spacing.ContentTop,
            bottom = DesignSystem.Spacing.ContentBottom
        ),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)
    ) {
        // ================= CONVERSATIONAL PROBING ACTIVE SESSION =================
        if (activeSession != null) {
            // Header Bar with Session Info & Exit
            item {
                AnimatedGlowingGlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(DesignSystem.Sizing.AvatarMd)
                                    .clip(RoundedCornerShape(DesignSystem.Shape.RadiusSm))
                                    .background(DesignSystem.Colors.PrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = DesignSystem.Colors.PrimaryLight,
                                    modifier = Modifier.size(DesignSystem.Sizing.IconLg)
                                )
                            }
                            Column {
                                PulsingLiveBadge(text = "LIVE AI PROBING", color = DesignSystem.Colors.Success)
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                                Text(
                                    text = "${activeSession.roleTarget} • ${activeSession.difficulty} Level",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DesignSystem.Colors.PrimaryLight
                                )
                            }
                        }

                        CareerButton(
                            text = "End Session",
                            onClick = { viewModel.exitActiveInterview() },
                            variant = CareerButton.ButtonVariant.Outline,
                            icon = Icons.Default.Close,
                            modifier = Modifier.defaultMinSize(minHeight = DesignSystem.Sizing.ButtonHeightSm)
                        )
                    }
                }
            }

            // Quick Topic Challenge Injectors
            item {
                Column {
                    Text(
                        text = "Suggested Probing Directions:",
                        style = MaterialTheme.typography.labelSmall,
                        color = DesignSystem.Colors.TextMuted
                    )
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ConversationalInterviewEngine.PROBING_CATALOG.take(4)) { challenge ->
                            CareerChip(
                                text = challenge.category,
                                selected = false,
                                onClick = { answerText = "Regarding ${challenge.category.lowercase()}, we address this by..." },
                                modifier = Modifier
                            )
                        }
                    }
                }
            }

            // Conversation Messages Feed
            items(conversationMessages) { message ->
                val isAi = message.sender == "AI"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isAi) 2.dp else 14.dp,
                                    bottomEnd = if (isAi) 14.dp else 2.dp
                                )
                            )
                            .background(if (isAi) DesignSystem.Colors.BgCard else DesignSystem.Colors.Primary.copy(alpha = 0.35f))
                            .border(
                                width = DesignSystem.Sizing.CardBorderWidth,
                                color = if (isAi) DesignSystem.Colors.PrimaryLight.copy(alpha = 0.3f) else DesignSystem.Colors.PrimaryLight.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isAi) 2.dp else 14.dp,
                                    bottomEnd = if (isAi) 14.dp else 2.dp
                                )
                            )
                            .padding(DesignSystem.Spacing.Space3)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline)
                            ) {
                                Icon(
                                    imageVector = if (isAi) Icons.Default.SmartToy else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isAi) DesignSystem.Colors.PrimaryLight else DesignSystem.Colors.PrimaryLight,
                                    modifier = Modifier.size(DesignSystem.Sizing.IconMd)
                                )
                                Text(
                                    text = if (isAi) "Staff AI Interviewer" else "You (Candidate)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isAi) DesignSystem.Colors.PrimaryLight else DesignSystem.Colors.PrimaryLight
                                )
                            }

                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))

                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DesignSystem.Colors.TextPrimary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // Latest AI Evaluation & Structured Feedback Card
            lastEval?.let { eval ->
                item {
                    CareerCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .borderColor(DesignSystem.Colors.PrimaryLight.copy(alpha = 0.6f))
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = null,
                                        tint = DesignSystem.Colors.PrimaryLight,
                                        modifier = Modifier.size(DesignSystem.Sizing.IconMd)
                                    )
                                    Text(
                                        text = "Round Evaluation Feedback",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DesignSystem.Colors.TextPrimary
                                    )
                                }
                                CircularScoreGauge(
                                    score = eval.score,
                                    size = 52.dp,
                                    strokeWidth = 5.dp,
                                    label = "SCORE"
                                )
                            }

                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                            // Score metrics row (Clarity & Technical Depth)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(DesignSystem.Shape.RadiusMd))
                                        .background(DesignSystem.Colors.BgSurfaceElevated)
                                        .padding(DesignSystem.Spacing.Space2)
                                ) {
                                    Text(
                                        text = "Technical Depth",
                                        fontSize = 11.sp,
                                        color = DesignSystem.Colors.TextSecondary
                                    )
                                    Text(
                                        text = "${eval.technicalScore}/100",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DesignSystem.Colors.PrimaryLight
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(DesignSystem.Shape.RadiusMd))
                                        .background(DesignSystem.Colors.BgSurfaceElevated)
                                        .padding(DesignSystem.Spacing.Space2)
                                ) {
                                    Text(
                                        text = "Communication Clarity",
                                        fontSize = 11.sp,
                                        color = DesignSystem.Colors.TextSecondary
                                    )
                                    Text(
                                        text = "${eval.clarityScore}/100",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DesignSystem.Colors.PrimaryLight
                                    )
                                }
                            }

                            if (eval.feedback.isNotBlank()) {
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Item))
                                Text(
                                    text = "Analysis & Trade-off Observations:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DesignSystem.Colors.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                                Text(
                                    text = eval.feedback,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DesignSystem.Colors.TextPrimary,
                                    lineHeight = 18.sp
                                )
                            }

                            if (eval.suggestedImprovement.isNotBlank()) {
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Item))
                                Text(
                                    text = "Actionable Counter-Argument Recommendation:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DesignSystem.Colors.Warning
                                )
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                                Text(
                                    text = eval.suggestedImprovement,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DesignSystem.Colors.TextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Input Bar for Candidate Response
            item {
                CareerCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)) {
                        Text(
                            text = "Your Response & Trade-off Defense",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DesignSystem.Colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Item))

                        CareerTextField(
                            value = answerText,
                            onValueChange = { answerText = it },
                            placeholder = "Explain your architecture, concurrency management, failure recovery, or metric outcomes...",
                            singleLine = false,
                            minLines = 3,
                            maxLines = 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("interview_answer_input")
                        )

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                        // Voice Speech Dictation & Audio Visualizer
                        var isVoiceRecording by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(DesignSystem.Shape.RadiusMd))
                                .background(if (isVoiceRecording) DesignSystem.Colors.DangerContainer else DesignSystem.Colors.BgSurfaceElevated)
                                .border(DesignSystem.Sizing.CardBorderWidth, if (isVoiceRecording) DesignSystem.Colors.Danger else DesignSystem.Colors.BorderDefault, RoundedCornerShape(DesignSystem.Shape.RadiusMd))
                                .padding(horizontal = DesignSystem.Spacing.Space3, vertical = DesignSystem.Spacing.Space2),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline)) {
                                Icon(
                                    imageVector = if (isVoiceRecording) Icons.Default.Mic else Icons.Default.MicNone,
                                    contentDescription = "Voice Mode",
                                    tint = if (isVoiceRecording) DesignSystem.Colors.Danger else DesignSystem.Colors.PrimaryLight,
                                    modifier = Modifier.size(DesignSystem.Sizing.IconMd)
                                )
                                Text(
                                    text = if (isVoiceRecording) "● Listening (Voice AI Active) - Speak naturally..." else "Voice AI Assistant (Tap to dictate)",
                                    fontSize = 11.sp,
                                    color = if (isVoiceRecording) DesignSystem.Colors.Danger else DesignSystem.Colors.TextSecondary,
                                    fontWeight = if (isVoiceRecording) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            CareerButton(
                                text = if (isVoiceRecording) "Stop" else "Start Mic",
                                onClick = {
                                    isVoiceRecording = !isVoiceRecording
                                    if (isVoiceRecording) {
                                        answerText = "In our architecture, we mitigate cascade failures using bulkheads and circuit breakers with Resilience4j, combined with Redis distributed locks for cache consistency."
                                    }
                                },
                                variant = CareerButton.ButtonVariant.Ghost,
                                icon = if (isVoiceRecording) Icons.Default.Stop else Icons.Default.Mic,
                                modifier = Modifier.defaultMinSize(minHeight = DesignSystem.Sizing.ButtonHeightSm)
                            )
                        }

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                        // Quick Probing Template Responses
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CareerButton(
                                text = "Insert Locking Sample",
                                onClick = {
                                    answerText = "We employ Redis distributed locks with exponential backoff and idempotency keys to ensure exactly-once processing during high traffic spikes."
                                },
                                variant = CareerButton.ButtonVariant.Outline,
                                modifier = Modifier.weight(1f)
                            )
                            CareerButton(
                                text = "Insert Failover Sample",
                                onClick = {
                                    answerText = "We deploy circuit breakers with Resilience4j to fail fast and fallback to cached read-only replicas when the master node crashes."
                                },
                                variant = CareerButton.ButtonVariant.Outline,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                        CareerButton(
                            text = if (isAnalyzing) "Interviewer Analyzing…" else "Submit Response & Defend Decisions",
                            onClick = {
                                if (answerText.isNotBlank()) {
                                    val currentQ = conversationMessages.lastOrNull { it.sender == "AI" }?.content
                                        ?: "Describe your architecture approach."
                                    val isFollowUp = conversationMessages.size > 2
                                    viewModel.submitConversationalTurn(
                                        sessionId = activeSession.id,
                                        question = currentQ,
                                        answerText = answerText.trim(),
                                        isFollowUp = isFollowUp
                                    )
                                    answerText = ""
                                }
                            },
                            enabled = answerText.isNotBlank() && !isAnalyzing,
                            variant = CareerButton.ButtonVariant.Primary,
                            loading = isAnalyzing,
                            icon = Icons.AutoMirrored.Filled.Send,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = DesignSystem.Sizing.TouchTargetMin)
                                .testTag("submit_answer_button")
                        )
                    }
                }
            }
        }

        // ================= INTERVIEW LOBBY & PRESET SELECTION =================
        else {
            item {
                Column {
                    Text(
                        text = "Conversational Mock Interviewer",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = DesignSystem.Colors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                    Text(
                        text = "Real-time AI probing engine with dynamic follow-ups on concurrency, resilience & trade-offs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DesignSystem.Colors.TextSecondary
                    )
                }
            }

            // Launch Active Session Card
            item {
                CareerCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .borderColor(DesignSystem.Colors.PrimaryLight.copy(alpha = 0.5f))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)) {
                        Text(
                            text = "Configure Live Probing Simulation",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                        Text(
                            text = "Calibrated for: ${profile?.targetRole ?: "Full Stack Engineer"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DesignSystem.Colors.PrimaryLight
                        )

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                        Text(
                            text = "Interview Track",
                            style = MaterialTheme.typography.labelMedium,
                            color = DesignSystem.Colors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("System Design & Probing", "Distributed Systems", "Concurrency & DB").forEach { track ->
                                val isSelected = selectedInterviewType == track
                                CareerChip(
                                    text = track,
                                    selected = isSelected,
                                    onClick = { selectedInterviewType = track },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                        Text(
                            text = "Target Difficulty Level",
                            style = MaterialTheme.typography.labelMedium,
                            color = DesignSystem.Colors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Junior", "Intermediate", "Senior", "Staff").forEach { diff ->
                                val isSelected = selectedDifficulty == diff
                                CareerChip(
                                    text = diff,
                                    selected = isSelected,
                                    onClick = { selectedDifficulty = diff },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                        CareerButton(
                            text = "Begin Conversational Probing Simulation",
                            onClick = {
                                val newSession = InterviewSession(
                                    id = "session_${System.currentTimeMillis()}",
                                    roleTarget = "${profile?.targetRole ?: "Senior Engineer"} ($selectedInterviewType)",
                                    difficulty = selectedDifficulty,
                                    overallScore = 0,
                                    feedbackSummary = "Live conversational probing session.",
                                    completedQuestions = 1,
                                    totalQuestions = 4,
                                    createdAt = System.currentTimeMillis()
                                )
                                viewModel.startConversationalInterview(newSession)
                            },
                            variant = CareerButton.ButtonVariant.Primary,
                            icon = Icons.Default.PlayArrow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = DesignSystem.Sizing.ButtonHeightLg)
                                .testTag("start_interview_button")
                        )
                    }
                }
            }

            // Probing Challenge Catalog Preview
            item {
                SectionHeader(
                    title = "Active Probing Challenge Catalog",
                    subtitle = "Dynamic edge case challenges triggered during interview answers"
                )
            }

            items(ConversationalInterviewEngine.PROBING_CATALOG) { challenge ->
                CareerCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Item)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = challenge.category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DesignSystem.Colors.PrimaryLight
                            )
                            StatusBadge(text = "TRIGGER: ${challenge.triggerPhrase}", statusType = "primary")
                        }
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                        Text(
                            text = challenge.probeQuestion,
                            style = MaterialTheme.typography.bodySmall,
                            color = DesignSystem.Colors.TextPrimary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                        Text(
                            text = "Evaluation Criteria: ${challenge.evaluationCriteria}",
                            style = MaterialTheme.typography.labelSmall,
                            color = DesignSystem.Colors.TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Past Interview History
            item {
                SectionHeader(
                    title = "Past Interview History (${history.size})",
                    subtitle = "Performance track record across technical simulations"
                )
            }

            if (history.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.RecordVoiceOver,
                        title = "No Past Interviews Recorded",
                        description = "Complete system design and technical probing sessions to record your communication velocity and architectural trade-off evaluations.",
                        actionLabel = "Start Probing Simulation",
                        onActionClick = {
                            val newSession = InterviewSession(
                                id = "session_${System.currentTimeMillis()}",
                                roleTarget = "${profile?.targetRole ?: "Full Stack Engineer"} ($selectedInterviewType)",
                                difficulty = selectedDifficulty,
                                overallScore = 0,
                                feedbackSummary = "Live conversational probing session.",
                                completedQuestions = 1,
                                totalQuestions = 4,
                                createdAt = System.currentTimeMillis()
                            )
                            viewModel.startConversationalInterview(newSession)
                        }
                    )
                }
            } else {
                items(history) { session ->
                    InterviewHistoryCard(session = session)
                }
            }
        }
    }
}

@Composable
private fun InterviewHistoryCard(session: InterviewSession) {
    val timeFormat = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())
    CareerCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Item)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline)
                    ) {
                        Text(
                            text = session.roleTarget,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DesignSystem.Colors.TextPrimary
                        )
                        StatusBadge(text = session.difficulty, statusType = "primary")
                    }
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                    Text(
                        text = "${session.completedQuestions}/${session.totalQuestions} Questions · ${timeFormat.format(Date(session.createdAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DesignSystem.Colors.TextMuted
                    )
                    if (session.feedbackSummary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Inline))
                        Text(
                            text = session.feedbackSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = DesignSystem.Colors.TextSecondary
                        )
                    }
                }
                CircularScoreGauge(
                    score = session.overallScore,
                    size = 56.dp,
                    strokeWidth = 5.dp,
                    label = "SCORE"
                )
            }
        }
    }
}
