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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.model.UserProfile
import com.example.careerpilot.data.repository.CheatSheetProvider
import com.example.careerpilot.ui.components.GlassCard
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.components.StatusBadge
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel

@Composable
fun ExportCenterScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val profileState by viewModel.userProfile.collectAsState()
    val skills by viewModel.userSkills.collectAsState()
    val roadmap by viewModel.activeRoadmap.collectAsState()
    val roadmapItems by viewModel.roadmapItems.collectAsState()

    val profile = profileState ?: UserProfile(
        fullName = "Alex Chen",
        email = "alex.chen@dev.io",
        location = "San Francisco, CA",
        targetRole = "Senior Full Stack Engineer",
        bio = "Senior Software Engineer specializing in distributed backend systems, microservices, and mobile architectures.",
        education = "B.S. in Computer Science"
    )

    var selectedTab by remember { mutableIntStateOf(0) } // 0: System Design Cheat Sheets, 1: ATS Resume Export, 2: Career Roadmap Export
    val tabTitles = listOf("System Design Cheats", "ATS Plain Resume", "Roadmap Export")
    var copiedMessage by remember { mutableStateOf<String?>(null) }

    val cheatSheets = CheatSheetProvider.CHEAT_SHEETS

    // Clean plain ATS text generator
    val atsPlainText = remember(profile, skills) {
        buildString {
            appendLine("${profile.fullName.uppercase()} — ${profile.targetRole}")
            appendLine("${profile.email} | ${profile.location} | linkedin.com/in/alexchen | github.com/alexchen")
            appendLine()
            appendLine("PROFESSIONAL SUMMARY")
            appendLine(profile.bio)
            appendLine()
            appendLine("TECHNICAL SKILLS")
            appendLine("Core: " + skills.joinToString(", ") { it.skillName })
            appendLine("Architectures: Microservices, Distributed Systems, REST, gRPC, CI/CD, Event-Driven Kafka")
            appendLine()
            appendLine("EXPERIENCE")
            appendLine("Senior Software Engineer | CloudScale Systems (2022 – Present)")
            appendLine("• Architected high-throughput microservices handling 40M+ daily transactions with 99.99% availability.")
            appendLine("• Reduced p99 database query latency by 45% by restructuring PostgreSQL indexes and Redis cache clustering.")
            appendLine("• Built reactive Android mobile client interfaces in Jetpack Compose with offline-first Room persistence.")
            appendLine("• Automated multi-region CI/CD deployment pipelines with Docker and Kubernetes, reducing release time by 60%.")
            appendLine()
            appendLine("EDUCATION")
            appendLine("${profile.education} — University of California (GPA: 3.8/4.0)")
        }
    }

    // Roadmap Export text
    val roadmapExportText = remember(profile, roadmap, roadmapItems) {
        buildString {
            appendLine("# CAREER PILOT — PERSONALIZED ROADMAP")
            appendLine("Target Role: ${profile.targetRole}")
            appendLine("Overall Progress: ${((roadmap?.progressPercent ?: 0.65f) * 100).toInt()}%")
            appendLine()
            appendLine("## MILESTONES & PHASES")
            roadmapItems.forEach { item ->
                val status = if (item.isCompleted) "[X]" else "[ ]"
                appendLine("$status Phase ${item.phaseNumber}: ${item.title} (${item.estimatedHours}h)")
                appendLine("    Description: ${item.description}")
            }
        }
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
            Column {
                Text(
                    text = "Export & Architecture Cheat Sheets",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1-click ATS resumes, architecture blueprints & milestone cheat sheets",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Tab Selector
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BgCard,
                contentColor = PrimaryBlueGlow,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PrimaryBlueGlow
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            copiedMessage = null
                        },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) PrimaryBlueGlow else TextSecondary
                            )
                        }
                    )
                }
            }
        }

        copiedMessage?.let { msg ->
            item {
                Text(
                    text = msg,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SuccessGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // System Design Cheat Sheets
                items(cheatSheets) { sheet ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sheet.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentCyan
                                )
                                Text(
                                    text = sheet.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AccentPurple
                                )
                            }

                            Button(
                                onClick = {
                                    val cheatText = "${sheet.title}\n\nKey Concept:\n${sheet.keyConcept}\n\nArchitecture Pattern:\n${sheet.architecturePattern}\n\nTrade-offs:\n${sheet.keyTradeoffs.joinToString("\n")}\n\nTalking Points:\n${sheet.interviewTalkingPoints.joinToString("\n")}"
                                    clipboardManager.setText(AnnotatedString(cheatText))
                                    copiedMessage = "✓ Copied '${sheet.title}' cheat sheet to clipboard!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = sheet.keyConcept,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Architecture Blueprint:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text(
                            text = sheet.architecturePattern,
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BgSurface, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Interview Talking Points:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        sheet.interviewTalkingPoints.forEach { point ->
                            Text(text = "• $point", fontSize = 11.sp, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = sheet.codeSnippetOrFormula,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = PrimaryBlueGlow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0D1117), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        )
                    }
                }
            }

            1 -> {
                // ATS Plain Resume Exporter
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Clean ATS Text Preview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(atsPlainText))
                                    copiedMessage = "✓ Copied clean ATS resume to clipboard!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("copy_ats_resume_button")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy ATS Text")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = atsPlainText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = TextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0D1117), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )
                    }
                }
            }

            2 -> {
                // Roadmap Export
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Career Roadmap Markdown Export",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(roadmapExportText))
                                    copiedMessage = "✓ Copied career roadmap markdown to clipboard!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("copy_roadmap_export_button")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Markdown")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = roadmapExportText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = TextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0D1117), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
