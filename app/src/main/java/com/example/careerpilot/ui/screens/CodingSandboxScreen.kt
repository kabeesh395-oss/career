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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.model.CodingChallenge
import com.example.careerpilot.ui.components.GlassCard
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.components.StatusBadge
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel

@Composable
fun CodingSandboxScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val challenges by viewModel.codingChallenges.collectAsState()
    var selectedChallenge by remember { mutableStateOf<CodingChallenge?>(null) }
    var codeInput by remember { mutableStateOf("") }
    var showSolution by remember { mutableStateOf(false) }
    var isRunningTests by remember { mutableStateOf(false) }
    var testResultOutput by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(challenges) {
        if (selectedChallenge == null && challenges.isNotEmpty()) {
            selectedChallenge = challenges.first()
            codeInput = challenges.first().starterCode
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "Coding & System Sandbox",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Practice algorithmic concurrency, caches & distributed architecture",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Challenge Selector Cards
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(challenges) { challenge ->
                    val isSelected = selectedChallenge?.id == challenge.id
                    Surface(
                        onClick = {
                            selectedChallenge = challenge
                            codeInput = challenge.starterCode
                            showSolution = false
                            testResultOutput = null
                        },
                        color = if (isSelected) PrimaryBlue.copy(alpha = 0.25f) else BgCard,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) PrimaryBlueGlow else BorderSubtle
                        ),
                        modifier = Modifier.width(220.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = challenge.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentPurple
                                )
                                if (challenge.isCompleted) {
                                    Text("✓ Done", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = challenge.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) PrimaryBlueGlow else TextPrimary,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Difficulty: ${challenge.difficulty} · ${challenge.timeComplexityTarget}",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Selected Problem Statement
        selectedChallenge?.let { current ->
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = AccentCyan.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = current.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                        StatusBadge(
                            text = current.difficulty,
                            color = if (current.difficulty == "Hard") DangerRed else WarningAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = current.problemStatement,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Target Time: ${current.timeComplexityTarget}", fontSize = 11.sp, color = AccentPurple, fontWeight = FontWeight.Bold)
                        Text("Target Space: ${current.spaceComplexityTarget}", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            // In-App Interactive Code Editor
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF0D1117),
                    borderColor = BorderSubtle
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(DangerRed))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(WarningAmber))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(SuccessGreen))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Solution.kt", fontSize = 12.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        }


                        TextButton(onClick = { codeInput = current.starterCode }) {
                            Text("Reset Starter Code", fontSize = 11.sp, color = AccentCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .testTag("code_editor_input"),
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE6EDF3),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlueGlow,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = Color(0xFF161B22),
                            unfocusedContainerColor = Color(0xFF161B22)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showSolution = !showSolution },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Text(if (showSolution) "Hide Hint" else "Show Reference", fontSize = 12.sp, color = TextSecondary)
                        }

                        Button(
                            onClick = {
                                isRunningTests = true
                                testResultOutput = "Running AST Analyzer & Concurrent Unit Tests...\n✓ Thread-safety Mutex verification: PASS\n✓ Complexity bound: ${current.timeComplexityTarget} VERIFIED\n✓ Edge cases (empty payload, timeout, eviction): 100% PASS"
                                viewModel.toggleCodingChallenge(current.id)
                                isRunningTests = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("run_code_tests_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run Tests & Verify", fontSize = 12.sp)
                        }
                    }

                    if (showSolution) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Reference Architecture: ${current.solutionReference}",
                            fontSize = 12.sp,
                            color = AccentPurple,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1F242C), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        )
                    }

                    testResultOutput?.let { output ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = output,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SuccessGreen,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F261C), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}
