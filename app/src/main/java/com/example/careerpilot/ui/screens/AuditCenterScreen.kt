package com.example.careerpilot.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.careerpilot.data.model.AuditIssue
import com.example.careerpilot.data.model.AuditScoreSummary
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditCenterScreen(
    viewModel: CareerViewModel,
    onNavigate: (String) -> Unit = {}
) {
    val auditIssues by viewModel.auditIssues.collectAsState()
    val auditSummary by viewModel.auditSummary.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSeverityFilter by remember { mutableStateOf("ALL") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedIssueForDetail by remember { mutableStateOf<AuditIssue?>(null) }
    var showExplainabilitySheet by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Filtering logic
    val filteredIssues = remember(auditIssues, searchQuery, selectedSeverityFilter, selectedCategoryFilter) {
        auditIssues.filter { issue ->
            val matchesSearch = searchQuery.isBlank() ||
                    issue.title.contains(searchQuery, ignoreCase = true) ||
                    issue.evidence.contains(searchQuery, ignoreCase = true) ||
                    issue.explanation.contains(searchQuery, ignoreCase = true) ||
                    issue.category.contains(searchQuery, ignoreCase = true) ||
                    issue.recommendedFix.contains(searchQuery, ignoreCase = true)

            val matchesSeverity = when (selectedSeverityFilter) {
                "ALL" -> true
                "RESOLVED" -> issue.status == "RESOLVED"
                else -> issue.status != "RESOLVED" && issue.severity.equals(selectedSeverityFilter, ignoreCase = true)
            }

            val matchesCategory = when (selectedCategoryFilter) {
                "ALL" -> true
                else -> issue.category.contains(selectedCategoryFilter, ignoreCase = true)
            }

            matchesSearch && matchesSeverity && matchesCategory
        }
    }

    Scaffold(
        containerColor = BgBase
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("audit_center_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title & Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Demerits & Red Flag Audit",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Deterministic evidence & hiring gap verification for ${userProfile?.targetRole ?: "Full Stack Engineer"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    FilledTonalButton(
                        onClick = { viewModel.recalibrateAudit() },
                        enabled = !isAnalyzing,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = BgSurfaceElevated,
                            contentColor = PrimaryBlueGlow
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("recalibrate_audit_button")
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = PrimaryBlueGlow
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Recalibrate",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Recalibrate", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Primary Audit Score Card
            item {
                AuditDashboardSummaryCard(
                    summary = auditSummary,
                    onExplainClick = { showExplainabilitySheet = true }
                )
            }

            // Severity Summary Counter Pills
            item {
                SeverityCounterRow(
                    summary = auditSummary,
                    selectedFilter = selectedSeverityFilter,
                    onSelectFilter = { selectedSeverityFilter = it }
                )
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audit_search_field"),
                    placeholder = {
                        Text(
                            "Search red flags, evidence, or technologies...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextMuted
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = TextMuted
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgSurface,
                        unfocusedContainerColor = BgSurface,
                        focusedBorderColor = PrimaryBlueGlow,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )
            }

            // Category Filter Chips
            item {
                val categories = listOf(
                    "ALL" to "All Categories",
                    "Production" to "Production Evidence",
                    "Resume" to "Resume Quality",
                    "Skill" to "Skill Depth",
                    "GitHub" to "GitHub Proof",
                    "System Design" to "System Design",
                    "Interview" to "Mock Interview",
                    "ATS" to "ATS Layout"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(categories) { (catKey, catLabel) ->
                        val isSelected = selectedCategoryFilter == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = catKey },
                            label = { Text(catLabel, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue.copy(alpha = 0.25f),
                                selectedLabelColor = PrimaryBlueGlow,
                                containerColor = BgSurface,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) PrimaryBlueGlow else BorderSubtle
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("category_chip_$catKey")
                        )
                    }
                }
            }

            // Header for Issue List with Count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Identified Findings (${filteredIssues.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "Tap finding for deep-dive",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            // Issue Cards List
            if (filteredIssues.isEmpty()) {
                item {
                    EmptyAuditStateCard(
                        hasFilters = searchQuery.isNotBlank() || selectedSeverityFilter != "ALL" || selectedCategoryFilter != "ALL",
                        onClearFilters = {
                            searchQuery = ""
                            selectedSeverityFilter = "ALL"
                            selectedCategoryFilter = "ALL"
                        }
                    )
                }
            } else {
                items(filteredIssues, key = { it.id }) { issue ->
                    AuditIssueCard(
                        issue = issue,
                        onInspectClick = { selectedIssueForDetail = issue },
                        onStartFix = { onNavigate(issue.targetRoute) },
                        onStatusChange = { newStatus ->
                            viewModel.updateAuditIssueStatus(issue.id, newStatus)
                        }
                    )
                }
            }

            // Bottom Safe Spacing
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Full Issue Detail Modal Dialog
    selectedIssueForDetail?.let { issue ->
        AuditIssueDetailDialog(
            issue = issue,
            onDismiss = { selectedIssueForDetail = null },
            onStartFix = {
                selectedIssueForDetail = null
                onNavigate(issue.targetRoute)
            },
            onStatusChange = { newStatus ->
                viewModel.updateAuditIssueStatus(issue.id, newStatus)
                selectedIssueForDetail = issue.copy(status = newStatus)
            }
        )
    }

    // Explainability Breakdown Dialog
    if (showExplainabilitySheet) {
        AuditExplainabilityDialog(
            summary = auditSummary,
            issues = auditIssues,
            onDismiss = { showExplainabilitySheet = false },
            onSelectIssue = { issue ->
                showExplainabilitySheet = false
                selectedIssueForDetail = issue
            }
        )
    }
}

@Composable
fun AuditDashboardSummaryCard(
    summary: AuditScoreSummary,
    onExplainClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audit_dashboard_summary_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BorderSubtle, PrimaryBlue.copy(alpha = 0.4f))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Row: Engine Status & Evidence Coverage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AccentGreen)
                    )
                    Text(
                        text = "Deterministic Engine (Offline)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = when (summary.profileConfidence) {
                        "HIGH" -> AccentGreen.copy(alpha = 0.15f)
                        "MEDIUM" -> AccentAmber.copy(alpha = 0.15f)
                        else -> AccentRed.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${summary.profileConfidence} Confidence",
                        color = when (summary.profileConfidence) {
                            "HIGH" -> AccentGreen
                            "MEDIUM" -> AccentAmber
                            else -> AccentRed
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Dual Score Section: Base Readiness vs Net Audit Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Net Audit Score Gauge
                Column {
                    Text(
                        text = "NET READINESS SCORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (summary.netAuditScore != null) "${summary.netAuditScore}" else "N/A",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = when {
                                summary.netAuditScore == null -> TextMuted
                                summary.netAuditScore >= 80 -> AccentGreen
                                summary.netAuditScore >= 65 -> PrimaryBlueGlow
                                else -> AccentAmber
                            }
                        )
                        Text(
                            text = if (summary.netAuditScore != null) "/ 100" else "Not evaluated",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                // Score Delta Callout
                Surface(
                    onClick = onExplainClick,
                    shape = RoundedCornerShape(12.dp),
                    color = if (summary.totalDemerits < 0) AccentRed.copy(alpha = 0.12f) else AccentGreen.copy(alpha = 0.12f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(
                                if (summary.totalDemerits < 0) AccentRed.copy(alpha = 0.4f) else AccentGreen.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    ),
                    modifier = Modifier.testTag("explain_score_delta_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (summary.totalDemerits < 0) "${summary.totalDemerits} Demerits" else "Zero Demerits",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.totalDemerits < 0) AccentRed else AccentGreen
                            )
                            Text(
                                text = "Base ${summary.readinessScore} → Net ${summary.netAuditScore}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Explain",
                            tint = if (summary.totalDemerits < 0) AccentRed else AccentGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Evidence Coverage Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Evidence Coverage",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${summary.evidenceCoveragePercent}% verified signals",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                LinearProgressIndicator(
                    progress = { summary.evidenceCoveragePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PrimaryBlueGlow,
                    trackColor = BgBase
                )
            }
        }
    }
}

@Composable
fun SeverityCounterRow(
    summary: AuditScoreSummary,
    selectedFilter: String,
    onSelectFilter: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SeverityPill(
            label = "Critical",
            count = summary.criticalCount,
            color = AccentRed,
            isSelected = selectedFilter == "CRITICAL",
            onClick = { onSelectFilter(if (selectedFilter == "CRITICAL") "ALL" else "CRITICAL") },
            modifier = Modifier.weight(1f)
        )

        SeverityPill(
            label = "High",
            count = summary.highCount,
            color = Color(0xFFF97316), // Orange
            isSelected = selectedFilter == "HIGH",
            onClick = { onSelectFilter(if (selectedFilter == "HIGH") "ALL" else "HIGH") },
            modifier = Modifier.weight(1f)
        )

        SeverityPill(
            label = "Medium",
            count = summary.mediumCount,
            color = AccentAmber,
            isSelected = selectedFilter == "MEDIUM",
            onClick = { onSelectFilter(if (selectedFilter == "MEDIUM") "ALL" else "MEDIUM") },
            modifier = Modifier.weight(1f)
        )

        SeverityPill(
            label = "Low",
            count = summary.lowCount,
            color = PrimaryBlueGlow,
            isSelected = selectedFilter == "LOW",
            onClick = { onSelectFilter(if (selectedFilter == "LOW") "ALL" else "LOW") },
            modifier = Modifier.weight(1f)
        )

        SeverityPill(
            label = "Resolved",
            count = summary.resolvedCount,
            color = AccentGreen,
            isSelected = selectedFilter == "RESOLVED",
            onClick = { onSelectFilter(if (selectedFilter == "RESOLVED") "ALL" else "RESOLVED") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SeverityPill(
    label: String,
    count: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.22f) else BgSurface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    if (isSelected) color else BorderSubtle,
                    if (isSelected) color else BorderSubtle
                )
            )
        ),
        modifier = modifier.testTag("severity_pill_$label")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (count > 0) color else TextMuted
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = if (isSelected) color else TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AuditIssueCard(
    issue: AuditIssue,
    onInspectClick: () -> Unit,
    onStartFix: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    val isResolved = issue.status == "RESOLVED"
    val severityColor = when (issue.severity) {
        "CRITICAL" -> AccentRed
        "HIGH" -> Color(0xFFF97316)
        "MEDIUM" -> AccentAmber
        "LOW" -> PrimaryBlueGlow
        else -> AccentGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspectClick() }
            .testTag("audit_issue_${issue.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isResolved) BgSurface.copy(alpha = 0.6f) else BgSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    if (isResolved) AccentGreen.copy(alpha = 0.4f) else severityColor.copy(alpha = 0.5f),
                    BorderSubtle
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Severity Pill, Category, Score Impact Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = if (isResolved) AccentGreen.copy(alpha = 0.15f) else severityColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isResolved) "RESOLVED" else issue.severity,
                            color = if (isResolved) AccentGreen else severityColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "• ${issue.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    color = if (isResolved) AccentGreen.copy(alpha = 0.15f) else AccentRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isResolved) "+0 (Resolved)" else "${issue.scoreImpact} pts",
                        color = if (isResolved) AccentGreen else AccentRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Issue Title
            Text(
                text = issue.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isResolved) TextSecondary else TextPrimary
            )

            // Evidence Finding Snippet
            Surface(
                color = BgBase,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Evidence",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = issue.evidence,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Bottom Actions & Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Effort Estimate
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
                        text = issue.estimatedEffort,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isResolved) {
                        Button(
                            onClick = onStartFix,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                contentColor = TextPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("start_fix_btn_${issue.id}")
                        ) {
                            Text(issue.ctaText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onStatusChange("OPEN") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BorderSubtle, BorderSubtle))),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Reopen", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuditIssueDetailDialog(
    issue: AuditIssue,
    onDismiss: () -> Unit,
    onStartFix: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    val severityColor = when (issue.severity) {
        "CRITICAL" -> AccentRed
        "HIGH" -> Color(0xFFF97316)
        "MEDIUM" -> AccentAmber
        "LOW" -> PrimaryBlueGlow
        else -> AccentGreen
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BgSurfaceElevated,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(BorderSubtle, severityColor.copy(alpha = 0.5f)))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("audit_issue_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header: Severity & Score
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = severityColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${issue.severity} • ${issue.category}",
                            color = severityColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                // Title
                Text(
                    text = issue.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Demerit Impact Row
                Surface(
                    color = BgBase,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Score Impact", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = if (issue.status == "RESOLVED") "0 pts (Resolved)" else "${issue.scoreImpact} Demerit Points",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (issue.status == "RESOLVED") AccentGreen else AccentRed
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Estimated Fix Time", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = issue.estimatedEffort,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Section 1: Why Detected (Evidence)
                AuditDetailSection(
                    title = "Why Detected (Evidence)",
                    icon = Icons.Default.Search,
                    content = issue.evidence,
                    badgeText = issue.evidenceStatus
                )

                // Section 2: Why This Matters
                AuditDetailSection(
                    title = "Hiring Bar Impact",
                    icon = Icons.Default.TrendingDown,
                    content = issue.explanation
                )

                // Section 3: Recommended Fix
                AuditDetailSection(
                    title = "Recommended Action Plan",
                    icon = Icons.Default.Build,
                    content = issue.recommendedFix
                )

                // Section 4: Verification Requirement
                AuditDetailSection(
                    title = "Verification Requirement",
                    icon = Icons.Default.Verified,
                    content = issue.verificationRequirement
                )

                // Status Lifecycle Selector
                Text(
                    text = "Lifecycle State",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("OPEN", "IN_PROGRESS", "VERIFICATION", "RESOLVED").forEach { st ->
                        val isCurr = issue.status == st
                        Surface(
                            onClick = { onStatusChange(st) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCurr) PrimaryBlue.copy(alpha = 0.25f) else BgBase,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        if (isCurr) PrimaryBlueGlow else BorderSubtle,
                                        if (isCurr) PrimaryBlueGlow else BorderSubtle
                                    )
                                )
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("status_selector_$st")
                        ) {
                            Text(
                                text = when (st) {
                                    "IN_PROGRESS" -> "In Prog"
                                    "VERIFICATION" -> "Verify"
                                    else -> st.lowercase().replaceFirstChar { it.uppercase() }
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isCurr) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurr) PrimaryBlueGlow else TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Action CTA
                Button(
                    onClick = onStartFix,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_start_fix_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${issue.ctaText} →",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AuditDetailSection(
    title: String,
    icon: ImageVector,
    content: String,
    badgeText: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlueGlow,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            badgeText?.let {
                Surface(
                    color = BgBase,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = it.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Surface(
            color = BgBase,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
fun AuditExplainabilityDialog(
    summary: AuditScoreSummary,
    issues: List<AuditIssue>,
    onDismiss: () -> Unit,
    onSelectIssue: (AuditIssue) -> Unit
) {
    val openIssues = issues.filter { it.status != "RESOLVED" }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BgSurfaceElevated,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(BorderSubtle, PrimaryBlue.copy(alpha = 0.5f)))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("audit_explainability_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Score Explainability Delta",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                // Math Formula Box
                Surface(
                    color = BgBase,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "DETERMINISTIC FORMULA",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Net Score = Base Readiness (${summary.readinessScore}) - Active Demerits (${kotlin.math.abs(summary.totalDemerits)}) = ${summary.netAuditScore}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueGlow
                        )

                        Text(
                            text = "Every point deduction is directly tied to missing evidence or unverified claims. Resolving an issue restores its full point value.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Text(
                    text = "Active Demerit Deductions (${openIssues.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                if (openIssues.isEmpty()) {
                    Text(
                        text = "No active demerits! Your profile is at 100% calibration integrity.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentGreen
                    )
                } else {
                    openIssues.forEach { issue ->
                        Surface(
                            onClick = { onSelectIssue(issue) },
                            color = BgBase,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = issue.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = issue.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }

                                Surface(
                                    color = AccentRed.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${issue.scoreImpact} pts",
                                        color = AccentRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmptyAuditStateCard(
    hasFilters: Boolean,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .testTag("empty_audit_state"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasFilters) Icons.Default.FilterListOff else Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = if (hasFilters) PrimaryBlueGlow else AccentGreen,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = if (hasFilters) "No Matching Red Flags" else "Zero Red Flags Detected!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = if (hasFilters)
                    "No audit findings match your current search or filter criteria. Try clearing filters."
                else
                    "Your profile demonstrates verified production evidence, solid technical articulation, and strong ATS compliance.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            if (hasFilters) {
                OutlinedButton(
                    onClick = onClearFilters,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlueGlow),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(PrimaryBlue, PrimaryBlueGlow)))
                ) {
                    Text("Clear Filters", fontSize = 12.sp)
                }
            }
        }
    }
}
