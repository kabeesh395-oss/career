package com.example.careerpilot.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.ui.animation.*
import com.example.careerpilot.ui.components.*
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketIntelligenceScreen(
    viewModel: CareerViewModel,
    onNavigate: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val searchResult by viewModel.searchGroundedResult.collectAsState()
    val isSearching by viewModel.isSearchingGrounding.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf("Live Comp Benchmarks") }

    val presets = listOf(
        "Live Comp Benchmarks",
        "Google Interview Loops",
        "OpenAI / Anthropic Bar",
        "Meta E5/E6 Rounds",
        "High-Demand AI Stacks"
    )

    // Load initial data if empty
    LaunchedEffect(Unit) {
        if (searchResult == null) {
            val role = userProfile?.targetRole ?: "Staff Software Engineer"
            viewModel.fetchLiveCompensationIntel(role = role)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card
        item {
            AnimatedGlowingGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("market_hero_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(Dimens.RadiusSm))
                                .background(AccentCyan.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TravelExplore,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(Dimens.IconLg)
                            )
                        }
                        Column {
                            PulsingLiveBadge(text = "SEARCH GROUNDING ACTIVE", color = AccentCyan)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Market Intelligence Hub",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Powered by gemini-3.5-flash with googleSearch tool",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Real-time tech hiring bars, 2026 compensation bands, and verified interview questions grounded in live Google Search results.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }

        // Search Input Bar
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("market_search_input"),
                        placeholder = { Text("Ask any tech market or interview question...", fontSize = 13.sp, color = TextMuted) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryBlueGlow)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlueGlow,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                viewModel.querySearchGrounding(searchQuery)
                            }
                        },
                        enabled = searchQuery.isNotBlank() && !isSearching,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .bouncyClickable {
                                if (searchQuery.isNotBlank()) viewModel.querySearchGrounding(searchQuery)
                            }
                            .testTag("market_search_button")
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("Query")
                        }
                    }
                }
            }
        }

        // Quick Preset Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presets) { preset ->
                    val isSelected = selectedPreset == preset
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) PrimaryBlue.copy(alpha = 0.3f) else BgCard,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) PrimaryBlueGlow else BorderSubtle
                        ),
                        modifier = Modifier
                            .bouncyClickable {
                                selectedPreset = preset
                                when (preset) {
                                    "Live Comp Benchmarks" -> viewModel.fetchLiveCompensationIntel()
                                    "Google Interview Loops" -> viewModel.fetchCompanyInterviewIntel("Google")
                                    "OpenAI / Anthropic Bar" -> viewModel.fetchCompanyInterviewIntel("OpenAI & Anthropic")
                                    "Meta E5/E6 Rounds" -> viewModel.fetchCompanyInterviewIntel("Meta")
                                    "High-Demand AI Stacks" -> viewModel.fetchLiveTrendingTechSkills()
                                }
                            }
                            .testTag("preset_chip_$preset")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = when (preset) {
                                    "Live Comp Benchmarks" -> Icons.Default.MonetizationOn
                                    "Google Interview Loops" -> Icons.Default.Business
                                    "High-Demand AI Stacks" -> Icons.Default.Code
                                    else -> Icons.Default.TravelExplore
                                },
                                contentDescription = null,
                                tint = if (isSelected) PrimaryBlueGlow else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = preset,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Search Results & Grounded Evidence
        item {
            if (isSearching) {
                LoadingState(
                    message = "Grounding query with Google Search...\nRetrieving verified compensation bands, interview questions, and tech trends..."
                )
            } else if (searchResult != null) {
                val result = searchResult!!
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("grounded_result_card"),
                    borderColor = PrimaryBlueGlow.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusBadge(
                                text = if (result.isLiveSearch) "LIVE GOOGLE SEARCH" else "VERIFIED BENCHMARK",
                                statusType = if (result.isLiveSearch) "primary" else "success"
                            )
                            Text(
                                text = result.timestamp,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }

                        IconButton(
                            onClick = { viewModel.querySearchGrounding(result.query) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PrimaryBlueGlow, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grounded Search Queries Executed
                    if (result.searchQueriesTriggered.isNotEmpty()) {
                        Text(
                            text = "Google Search Queries Triggered:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            result.searchQueriesTriggered.take(2).forEach { query ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PrimaryBlue.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "🔍 $query", color = TextSecondary, fontSize = 10.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Main Text Content with Structured Markdown Rendering
                    FormattedMarkdownText(
                        text = result.summary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Grounded Sources & Citations
                    if (result.sources.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = BorderSubtle)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Verified Web Citations & Sources (${result.sources.size}):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        result.sources.forEach { source ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BgSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .bouncyClickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Handle intent error
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(PrimaryBlue.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Language,
                                                contentDescription = null,
                                                tint = PrimaryBlueGlow,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = source.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = source.url,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted,
                                                maxLines = 1,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Icon(
                                        Icons.Default.OpenInNew,
                                        contentDescription = "Open",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Next steps CTA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigate("interview") },
                            modifier = Modifier
                                .weight(1f)
                                .bouncyClickable { onNavigate("interview") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlueGlow)
                        ) {
                            Text("Launch Mock Interview", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onNavigate("negotiator") },
                            modifier = Modifier
                                .weight(1f)
                                .bouncyClickable { onNavigate("negotiator") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Simulate Offer", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
