package com.example.careerpilot.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.model.ConversationMessage
import com.example.careerpilot.data.model.InterviewSession
import com.example.careerpilot.data.repository.BenchmarkCatalog
import com.example.careerpilot.data.repository.ConversationalInterviewEngine
import com.example.careerpilot.ui.animation.*
import com.example.careerpilot.ui.components.*
import com.example.careerpilot.ui.theme.*
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
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            PulsingAiOrb(size = 46.dp, baseColor = PrimaryBlueGlow, secondaryColor = AccentCyan)
                            Column {
                                PulsingLiveBadge(text = "LIVE AI PROBING", color = SuccessGreen)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${activeSession!!.roleTarget} • ${activeSession!!.difficulty} Level",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AccentCyan
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.exitActiveInterview() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                            modifier = Modifier.bouncyClickable { viewModel.exitActiveInterview() }
                        ) {
                            Text("End Session", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Quick Topic Challenge Injectors
            item {
                Column {
                    Text(
                        text = "Suggested Probing Directions:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ConversationalInterviewEngine.PROBING_CATALOG.take(4)) { challenge ->
                            Surface(
                                color = BgSurface,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.clickable {
                                    answerText = "Regarding ${challenge.category.lowercase()}, we address this by..."
                                }
                            ) {
                                Text(
                                    text = challenge.category,
                                    fontSize = 11.sp,
                                    color = AccentPurple,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
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
                            .background(if (isAi) BgCard else PrimaryBlue.copy(alpha = 0.35f))
                            .border(
                                width = 1.dp,
                                color = if (isAi) PrimaryBlueGlow.copy(alpha = 0.3f) else PrimaryBlueGlow.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isAi) 2.dp else 14.dp,
                                    bottomEnd = if (isAi) 14.dp else 2.dp
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAi) Icons.Default.SmartToy else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isAi) PrimaryBlueGlow else AccentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isAi) "Staff AI Interviewer" else "You (Candidate)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isAi) PrimaryBlueGlow else AccentCyan
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // Interactive Input Bar for Candidate Response
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Your Response & Trade-off Defense",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = answerText,
                        onValueChange = { answerText = it },
                        placeholder = {
                            Text(
                                text = "Explain your architecture, concurrency management, failure recovery, or metric outcomes...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        minLines = 3,
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = BgSurface,
                            unfocusedContainerColor = BgSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("interview_answer_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Voice Speech Dictation & Audio Visualizer
                    var isVoiceRecording by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isVoiceRecording) DangerRed.copy(alpha = 0.15f) else BgSurfaceElevated)
                            .border(1.dp, if (isVoiceRecording) DangerRed else BorderSubtle, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = if (isVoiceRecording) Icons.Default.Mic else Icons.Default.MicNone,
                                contentDescription = "Voice Mode",
                                tint = if (isVoiceRecording) DangerRed else AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isVoiceRecording) "● Listening (Voice AI Active) - Speak naturally..." else "Voice AI Assistant (Tap to dictate)",
                                fontSize = 11.sp,
                                color = if (isVoiceRecording) DangerRed else TextSecondary,
                                fontWeight = if (isVoiceRecording) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                isVoiceRecording = !isVoiceRecording
                                if (isVoiceRecording) {
                                    answerText = "In our architecture, we mitigate cascade failures using bulkheads and circuit breakers with Resilience4j, combined with Redis distributed locks for cache consistency."
                                }
                            },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(if (isVoiceRecording) "Stop" else "Start Mic", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Probing Template Responses
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        OutlinedButton(
                            onClick = {
                                answerText = "We employ Redis distributed locks with exponential backoff and idempotency keys to ensure exactly-once processing during high traffic spikes."
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Insert Locking Sample", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = {
                                answerText = "We deploy circuit breakers with Resilience4j to fail fast and fallback to cached read-only replicas when the master node crashes."
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Insert Failover Sample", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (answerText.isNotBlank()) {
                                val currentQ = conversationMessages.lastOrNull { it.sender == "AI" }?.content
                                    ?: "Describe your architecture approach."
                                val isFollowUp = conversationMessages.size > 2
                                viewModel.submitConversationalTurn(
                                    sessionId = activeSession!!.id,
                                    question = currentQ,
                                    answerText = answerText.trim(),
                                    isFollowUp = isFollowUp
                                )
                                answerText = ""
                            }
                        },
                        enabled = answerText.isNotBlank() && !isAnalyzing,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_answer_button")
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Interviewer Analyzing & Formulating Challenge...")
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Submit Response & Defend Decisions", fontWeight = FontWeight.Bold)
                        }
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
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real-time AI probing engine with dynamic follow-ups on concurrency, resilience & trade-offs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Launch Active Session Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = PrimaryBlueGlow.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Configure Live Probing Simulation",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Calibrated for: ${profile?.targetRole ?: "Full Stack Engineer"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentCyan
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Interview Track",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("System Design & Probing", "Distributed Systems", "Concurrency & DB").forEach { track ->
                            val isSelected = selectedInterviewType == track
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedInterviewType = track },
                                label = { Text(track, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = TextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Target Difficulty Level",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Junior", "Intermediate", "Senior", "Staff").forEach { diff ->
                            val isSelected = selectedDifficulty == diff
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDifficulty = diff },
                                label = { Text(diff, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = TextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
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
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_interview_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Begin Conversational Probing Simulation", fontWeight = FontWeight.Bold)
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
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = challenge.category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueGlow
                        )
                        StatusBadge(text = "TRIGGER: ${challenge.triggerPhrase}", statusType = "primary")
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = challenge.probeQuestion,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Evaluation Criteria: ${challenge.evaluationCriteria}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
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
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No completed mock interviews yet. Click 'Begin Conversational Probing Simulation' above.",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
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
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = session.roleTarget,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    StatusBadge(text = session.difficulty, statusType = "primary")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${session.completedQuestions}/${session.totalQuestions} Questions · ${timeFormat.format(Date(session.createdAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                if (session.feedbackSummary.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = session.feedbackSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
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

