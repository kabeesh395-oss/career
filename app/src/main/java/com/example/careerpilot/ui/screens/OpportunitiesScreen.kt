package com.example.careerpilot.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.careerpilot.data.model.CareerOpportunity
import com.example.careerpilot.ui.components.BulletSeparator
import com.example.careerpilot.ui.components.CompanyLogoBadge
import com.example.careerpilot.ui.components.GlassCard
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.theme.Dimens
import com.example.careerpilot.ui.viewmodel.CareerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpportunitiesScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val opportunities by viewModel.opportunities.collectAsState()
    val selectedCategory by viewModel.selectedOpportunityCategory.collectAsState()
    val searchQuery by viewModel.opportunitySearchQuery.collectAsState()
    val selectedOpportunity by viewModel.selectedOpportunity.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var filterDifficulty by remember { mutableStateOf("ALL") }
    var onlyWithPrizeOrStipend by remember { mutableStateOf(false) }

    // Categories
    val categories = listOf(
        "ALL" to "All Opportunities",
        "CERTIFICATION" to "Certifications",
        "HACKATHON" to "Hackathons",
        "FELLOWSHIP" to "Fellowships",
        "OPEN_SOURCE" to "Open Source",
        "HIRING_CHALLENGE" to "Hiring Sprints",
        "BOOKMARKED" to "Saved"
    )

    // Filter opportunities
    val filteredList = remember(opportunities, selectedCategory, searchQuery, filterDifficulty, onlyWithPrizeOrStipend) {
        opportunities.filter { opp ->
            val matchesCategory = when (selectedCategory) {
                "ALL" -> true
                "BOOKMARKED" -> opp.status == "BOOKMARKED" || opp.status == "REGISTERED" || opp.status == "PREPARING"
                else -> opp.category.equals(selectedCategory, ignoreCase = true)
            }
            val matchesSearch = searchQuery.isBlank() ||
                    opp.title.contains(searchQuery, ignoreCase = true) ||
                    opp.providerOrHost.contains(searchQuery, ignoreCase = true) ||
                    opp.skillsTargeted.any { it.contains(searchQuery, ignoreCase = true) } ||
                    opp.description.contains(searchQuery, ignoreCase = true)

            val matchesDifficulty = filterDifficulty == "ALL" || opp.difficulty.equals(filterDifficulty, ignoreCase = true)

            val matchesPrize = !onlyWithPrizeOrStipend ||
                    (opp.costOrPrize.contains("$", ignoreCase = true) ||
                     opp.costOrPrize.contains("stipend", ignoreCase = true) ||
                     opp.costOrPrize.contains("prize", ignoreCase = true) ||
                     opp.costOrPrize.contains("reward", ignoreCase = true))

            matchesCategory && matchesSearch && matchesDifficulty && matchesPrize
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(Dimens.RadiusMd),
                modifier = Modifier.testTag("add_custom_opportunity_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Dimens.SpaceMd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Opportunity", modifier = Modifier.size(Dimens.IconMd))
                    Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                    Text("Add Custom", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.ContentHorizontalPadding),
            contentPadding = PaddingValues(
                top = Dimens.ContentTopPadding,
                bottom = Dimens.ContentBottomPadding + 72.dp
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
        ) {
            // Header Banner
            item {
                OpportunityHeaderBanner(
                    totalCount = opportunities.size,
                    savedCount = opportunities.count { it.status == "BOOKMARKED" || it.status == "REGISTERED" || it.status == "PREPARING" }
                )
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setOpportunitySearchQuery(it) },
                    placeholder = { Text("Search certs, hackathons, platforms, skills...", color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryBlueLighter)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setOpportunitySearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgCard,
                        unfocusedContainerColor = BgCard,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(Dimens.RadiusMd),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("opportunity_search_field")
                )
            }

            // Category Tabs
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                    contentPadding = PaddingValues(vertical = Dimens.SpaceXs)
                ) {
                    items(categories) { (catKey, catLabel) ->
                        val isSelected = selectedCategory == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setOpportunityCategoryFilter(catKey) },
                            label = {
                                Text(
                                    text = catLabel,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White,
                                containerColor = BgCard,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) PrimaryBlue else BorderSubtle
                            ),
                            shape = RoundedCornerShape(Dimens.RadiusFull),
                            modifier = Modifier.testTag("category_chip_$catKey")
                        )
                    }
                }
            }

            // Quick Filter Chips (Difficulty & Stipend)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Difficulty Selector
                    val difficulties = listOf("ALL" to "All Levels", "Beginner" to "Beginner", "Intermediate" to "Intermediate", "Advanced" to "Advanced")
                    var diffMenuExpanded by remember { mutableStateOf(false) }

                    Box {
                        AssistChip(
                            onClick = { diffMenuExpanded = true },
                            label = { Text("Level: $filterDifficulty", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp), tint = PrimaryBlueLighter)
                            },
                            colors = AssistChipDefaults.assistChipColors(containerColor = BgCard, labelColor = TextSecondary),
                            border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = BorderSubtle)
                        )
                        DropdownMenu(
                            expanded = diffMenuExpanded,
                            onDismissRequest = { diffMenuExpanded = false },
                            modifier = Modifier.background(BgCard)
                        ) {
                            difficulties.forEach { (dVal, dLabel) ->
                                DropdownMenuItem(
                                    text = { Text(dLabel, color = TextPrimary) },
                                    onClick = {
                                        filterDifficulty = dVal
                                        diffMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Cash / Stipend Toggle
                    FilterChip(
                        selected = onlyWithPrizeOrStipend,
                        onClick = { onlyWithPrizeOrStipend = !onlyWithPrizeOrStipend },
                        label = { Text("Cash / Stipend", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SuccessGreen.copy(alpha = 0.2f),
                            selectedLabelColor = SuccessGreenLight,
                            containerColor = BgCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = onlyWithPrizeOrStipend,
                            borderColor = if (onlyWithPrizeOrStipend) SuccessGreen else BorderSubtle
                        )
                    )
                }
            }

            // Results count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Showing ${filteredList.size} opportunit${if (filteredList.size == 1) "y" else "ies"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    if (searchQuery.isNotBlank() || selectedCategory != "ALL" || filterDifficulty != "ALL" || onlyWithPrizeOrStipend) {
                        Text(
                            text = "Reset Filters",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryBlueLighter,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    viewModel.setOpportunitySearchQuery("")
                                    viewModel.setOpportunityCategoryFilter("ALL")
                                    filterDifficulty = "ALL"
                                    onlyWithPrizeOrStipend = false
                                }
                                .padding(Dimens.SpaceXs)
                        )
                    }
                }
            }

            // Empty state
            if (filteredList.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.SpaceXl),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpaceMd))
                            Text(
                                text = "No opportunities found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpaceSm))
                            Text(
                                text = "Try adjusting your search keywords or switching category filters.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Opportunity Items
                items(filteredList, key = { it.id }) { opportunity ->
                    OpportunityCard(
                        opportunity = opportunity,
                        onViewDetails = { viewModel.selectOpportunity(opportunity) },
                        onToggleBookmark = { viewModel.toggleOpportunityBookmark(opportunity.id) },
                        onOpenUrl = { url ->
                            openBrowserUrl(context, url)
                        }
                    )
                }
            }
        }
    }

    // Detail BottomSheet / Dialog
    selectedOpportunity?.let { opp ->
        OpportunityDetailModal(
            opportunity = opp,
            onDismiss = { viewModel.selectOpportunity(null) },
            onUpdateStatus = { newStatus -> viewModel.updateOpportunityStatus(opp.id, newStatus) },
            onToggleBookmark = { viewModel.toggleOpportunityBookmark(opp.id) },
            onToggleReminder = { enabled -> viewModel.toggleOpportunityReminder(opp.id, enabled) },
            onSaveNotes = { notes -> viewModel.updateOpportunityNotes(opp.id, notes) }
        )
    }

    // Custom Opportunity Dialog
    if (showAddDialog) {
        AddCustomOpportunityDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, category, provider, officialUrl, regUrl, syllabusUrl, costPrize, difficulty, mode, desc, skills ->
                viewModel.addCustomOpportunity(
                    title = title,
                    category = category,
                    provider = provider,
                    officialUrl = officialUrl,
                    registrationUrl = regUrl,
                    syllabusUrl = syllabusUrl,
                    costOrPrize = costPrize,
                    difficulty = difficulty,
                    mode = mode,
                    description = desc,
                    skills = skills
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun OpportunityHeaderBanner(
    totalCount: Int,
    savedCount: Int
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.SpaceLg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = WarningAmberLight,
                            modifier = Modifier.size(Dimens.IconMd)
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                        Text(
                            text = "Credential & Hackathon Radar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                    Text(
                        text = "Curated certifications, prize hackathons, open-source grants & direct links to apply.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceMd))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
            ) {
                HeaderStatPill(
                    icon = Icons.Default.CheckCircle,
                    label = "Curated",
                    value = "$totalCount Available",
                    tint = PrimaryBlueLighter,
                    bg = PrimaryBlue.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                )
                HeaderStatPill(
                    icon = Icons.Default.Bookmark,
                    label = "Active / Saved",
                    value = "$savedCount Tracked",
                    tint = SuccessGreenLight,
                    bg = SuccessGreen.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                )
                HeaderStatPill(
                    icon = Icons.Default.Link,
                    label = "Direct Links",
                    value = "100% Verified",
                    tint = AccentCyanLight,
                    bg = AccentCyan.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HeaderStatPill(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Dimens.RadiusSm)
    Column(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, tint.copy(alpha = 0.25f), shape)
            .padding(horizontal = Dimens.SpaceSm, vertical = Dimens.SpaceSm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun OpportunityCard(
    opportunity: CareerOpportunity,
    onViewDetails: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val (categoryColor, categoryIcon) = when (opportunity.category.uppercase()) {
        "CERTIFICATION" -> PrimaryBlueLighter to Icons.Default.WorkspacePremium
        "HACKATHON" -> AccentPurple to Icons.Default.EmojiEvents
        "FELLOWSHIP" -> SuccessGreenLight to Icons.Default.School
        "OPEN_SOURCE" -> AccentCyanLight to Icons.Default.Code
        "HIRING_CHALLENGE" -> WarningAmberLight to Icons.Default.Speed
        else -> TextSecondary to Icons.Default.Stars
    }

    val isSaved = opportunity.status == "BOOKMARKED" || opportunity.status == "REGISTERED" || opportunity.status == "PREPARING"

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .testTag("opportunity_card_${opportunity.id}")
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceLg)) {
            // Category & Bookmark Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                    Text(
                        text = opportunity.category.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                    BulletSeparator(color = TextMuted)
                    Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                    CompanyLogoBadge(company = opportunity.providerOrHost, size = 18.dp)
                    Spacer(modifier = Modifier.width(Dimens.SpaceXs))
                    Text(
                        text = opportunity.providerOrHost,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Match Score Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Dimens.RadiusFull))
                            .background(SuccessGreen.copy(alpha = 0.15f))
                            .padding(horizontal = Dimens.SpaceSm, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${opportunity.matchScore}% Match",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreenLight
                        )
                    }

                    Spacer(modifier = Modifier.width(Dimens.SpaceSm))

                    // Bookmark Star
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isSaved) WarningAmberLight else TextMuted,
                            modifier = Modifier.size(Dimens.IconMd)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceSm))

            // Title
            Text(
                text = opportunity.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceXs))

            // Description
            Text(
                text = opportunity.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceMd))

            // Details pills (Prize/Cost, Schedule, Mode)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
            ) {
                DetailMiniBadge(
                    icon = Icons.Default.MonetizationOn,
                    text = opportunity.costOrPrize,
                    tint = SuccessGreenLight,
                    modifier = Modifier.weight(1f)
                )
                DetailMiniBadge(
                    icon = Icons.Default.Schedule,
                    text = opportunity.deadlineOrSchedule,
                    tint = AccentCyanLight,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceSm))

            // Skills Targeted
            if (opportunity.skillsTargeted.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
                    contentPadding = PaddingValues(vertical = Dimens.SpaceXxs)
                ) {
                    items(opportunity.skillsTargeted.take(4)) { skill ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Dimens.RadiusSm))
                                .background(BgMuted)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(Dimens.RadiusSm))
                                .padding(horizontal = Dimens.SpaceSm, vertical = 2.dp)
                        ) {
                            Text(
                                text = skill,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.SpaceMd))
            }

            // Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(Dimens.RadiusMd),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
                    contentPadding = PaddingValues(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceSm),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Details & Prep", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Button(
                    onClick = {
                        val targetUrl = if (opportunity.registrationUrl.isNotBlank()) opportunity.registrationUrl else opportunity.officialUrl
                        onOpenUrl(targetUrl)
                    },
                    shape = RoundedCornerShape(Dimens.RadiusMd),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    contentPadding = PaddingValues(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceSm),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (opportunity.category == "CERTIFICATION") "Official Exam" else "Apply / Visit",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpaceXs))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailMiniBadge(
    icon: ImageVector,
    text: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Dimens.RadiusSm)
    Row(
        modifier = modifier
            .clip(shape)
            .background(BgMuted)
            .border(1.dp, BorderSubtle, shape)
            .padding(horizontal = Dimens.SpaceSm, vertical = Dimens.SpaceXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(Dimens.SpaceXs))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpportunityDetailModal(
    opportunity: CareerOpportunity,
    onDismiss: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onSaveNotes: (String) -> Unit
) {
    val context = LocalContext.current
    var notesText by remember(opportunity.userNotes) { mutableStateOf(opportunity.userNotes) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BgSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = Dimens.RadiusLg, topEnd = Dimens.RadiusLg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ContentHorizontalPadding),
            contentPadding = PaddingValues(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg)
        ) {
            // Title & Host Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = opportunity.category.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueLighter
                            )
                            Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                            BulletSeparator(color = TextMuted)
                            Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                            Text(
                                text = opportunity.providerOrHost,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                        Text(
                            text = opportunity.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(onClick = onToggleBookmark) {
                        val isSaved = opportunity.status == "BOOKMARKED" || opportunity.status == "REGISTERED" || opportunity.status == "PREPARING"
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isSaved) WarningAmberLight else TextMuted
                        )
                    }
                }
            }

            // Direct Links Center (Primary Action Area)
            item {
                SectionHeader(title = "Required Links & Official Portals")
                Spacer(modifier = Modifier.height(Dimens.SpaceSm))
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                    // 1. Primary Official Link
                    LinkLauncherCard(
                        title = "Official Portal & Program Website",
                        subtitle = opportunity.officialUrl,
                        icon = Icons.Default.Public,
                        buttonLabel = "Open Site",
                        onOpen = { openBrowserUrl(context, opportunity.officialUrl) },
                        onCopy = { copyToClipboard(context, opportunity.officialUrl) }
                    )

                    // 2. Direct Registration Link (if present)
                    if (opportunity.registrationUrl.isNotBlank()) {
                        LinkLauncherCard(
                            title = if (opportunity.category == "CERTIFICATION") "Exam Registration & Scheduling Portal" else "Application & Registration Form",
                            subtitle = opportunity.registrationUrl,
                            icon = Icons.Default.AppRegistration,
                            buttonLabel = "Register Now",
                            accentColor = SuccessGreenLight,
                            onOpen = { openBrowserUrl(context, opportunity.registrationUrl) },
                            onCopy = { copyToClipboard(context, opportunity.registrationUrl) }
                        )
                    }

                    // 3. Syllabus / Rulebook / Docs Link (if present)
                    if (opportunity.syllabusOrDocsUrl.isNotBlank()) {
                        LinkLauncherCard(
                            title = if (opportunity.category == "CERTIFICATION") "Official Exam Guide & Syllabus (PDF / Docs)" else "Rules, Guidelines & API Documentation",
                            subtitle = opportunity.syllabusOrDocsUrl,
                            icon = Icons.Default.MenuBook,
                            buttonLabel = "View Guide",
                            accentColor = AccentPurple,
                            onOpen = { openBrowserUrl(context, opportunity.syllabusOrDocsUrl) },
                            onCopy = { copyToClipboard(context, opportunity.syllabusOrDocsUrl) }
                        )
                    }
                }
            }

            // Overview & Description
            item {
                SectionHeader(title = "Overview & Program Details")
                Spacer(modifier = Modifier.height(Dimens.SpaceSm))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(Dimens.SpaceMd)) {
                        Text(
                            text = opportunity.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )

                        if (opportunity.careerRoiSummary.isNotBlank()) {
                            Spacer(modifier = Modifier.height(Dimens.SpaceMd))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Dimens.RadiusSm))
                                    .background(SuccessGreen.copy(alpha = 0.12f))
                                    .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(Dimens.RadiusSm))
                                    .padding(Dimens.SpaceMd),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = SuccessGreenLight, modifier = Modifier.size(Dimens.IconMd))
                                Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                                Column {
                                    Text("Career & Salary ROI Impact", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SuccessGreenLight)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(opportunity.careerRoiSummary, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Key Specs Grid
            item {
                SectionHeader(title = "Key Specifications")
                Spacer(modifier = Modifier.height(Dimens.SpaceSm))
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
                    ) {
                        SpecCard(
                            label = "Cost / Reward",
                            value = opportunity.costOrPrize,
                            icon = Icons.Default.MonetizationOn,
                            tint = SuccessGreenLight,
                            modifier = Modifier.weight(1f)
                        )
                        SpecCard(
                            label = "Difficulty",
                            value = opportunity.difficulty,
                            icon = Icons.Default.SignalCellularAlt,
                            tint = PrimaryBlueLighter,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
                    ) {
                        SpecCard(
                            label = "Schedule",
                            value = opportunity.deadlineOrSchedule,
                            icon = Icons.Default.CalendarMonth,
                            tint = WarningAmberLight,
                            modifier = Modifier.weight(1f)
                        )
                        SpecCard(
                            label = "Mode & Format",
                            value = opportunity.mode,
                            icon = Icons.Default.LocationOn,
                            tint = AccentCyanLight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Key Domains / Exam Blueprint
            if (opportunity.keyDomains.isNotEmpty()) {
                item {
                    SectionHeader(title = if (opportunity.category == "CERTIFICATION") "Exam Domains & Weightage" else "Challenge Tracks & Rubric")
                    Spacer(modifier = Modifier.height(Dimens.SpaceSm))
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(Dimens.SpaceMd),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
                        ) {
                            opportunity.keyDomains.forEachIndexed { index, domain ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryBlue.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlueLighter
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                                    Text(
                                        text = domain,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Prerequisites & Target Roles
            if (opportunity.prerequisites.isNotBlank()) {
                item {
                    SectionHeader(title = "Prerequisites & Target Roles")
                    Spacer(modifier = Modifier.height(Dimens.SpaceSm))
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Dimens.SpaceMd)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = AccentCyanLight, modifier = Modifier.size(Dimens.IconSm))
                                Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                                Text("Recommended Background:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                            Text(opportunity.prerequisites, style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                            if (opportunity.targetRoles.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(Dimens.SpaceMd))
                                Text("Best Fit For Roles:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
                                    opportunity.targetRoles.forEach { role ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(Dimens.RadiusSm))
                                                .background(PrimaryBlue.copy(alpha = 0.12f))
                                                .padding(horizontal = Dimens.SpaceSm, vertical = 2.dp)
                                        ) {
                                            Text(role, style = MaterialTheme.typography.labelSmall, color = PrimaryBlueLighter)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Candidate Status & Progress Management
            item {
                SectionHeader(title = "Your Tracker & Notes")
                Spacer(modifier = Modifier.height(Dimens.SpaceSm))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(Dimens.SpaceMd),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
                    ) {
                        // Status Selector
                        Text("Current Candidate Status:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        val statusList = listOf(
                            "EXPLORING" to "Exploring",
                            "BOOKMARKED" to "Saved",
                            "REGISTERED" to "Registered",
                            "PREPARING" to "In Prep",
                            "COMPLETED" to "Completed"
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                            items(statusList) { (sKey, sLabel) ->
                                val isSelected = opportunity.status == sKey
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onUpdateStatus(sKey) },
                                    label = { Text(sLabel) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue,
                                        selectedLabelColor = Color.White,
                                        containerColor = BgMuted,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }

                        // Reminder Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Deadline & Registration Reminder", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                                Text("Notify before registration deadlines", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                            Switch(
                                checked = opportunity.reminderSet,
                                onCheckedChange = { onToggleReminder(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SuccessGreen)
                            )
                        }

                        // Personal Notes
                        OutlinedTextField(
                            value = notesText,
                            onValueChange = {
                                notesText = it
                                onSaveNotes(it)
                            },
                            label = { Text("Personal Study Notes / Team Links / Credentials") },
                            placeholder = { Text("e.g. Registered with team @Discord, scheduled exam for next month...", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BgBase,
                                unfocusedContainerColor = BgBase,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(Dimens.RadiusMd),
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkLauncherCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    buttonLabel: String,
    accentColor: Color = PrimaryBlueLighter,
    onOpen: () -> Unit,
    onCopy: () -> Unit
) {
    val shape = RoundedCornerShape(Dimens.RadiusMd)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(BgCard)
            .border(1.dp, BorderSubtle, shape)
            .padding(Dimens.SpaceMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(Dimens.IconMd))
            }
            Spacer(modifier = Modifier.width(Dimens.SpaceMd))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy link",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(Dimens.SpaceXs))
            Button(
                onClick = onOpen,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(Dimens.RadiusSm),
                contentPadding = PaddingValues(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceXs)
            ) {
                Text(buttonLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun SpecCard(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Dimens.RadiusMd)
    Row(
        modifier = modifier
            .clip(shape)
            .background(BgCard)
            .border(1.dp, BorderSubtle, shape)
            .padding(Dimens.SpaceMd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(Dimens.IconSm))
        }
        Spacer(modifier = Modifier.width(Dimens.SpaceSm))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AddCustomOpportunityDialog(
    onDismiss: () -> Unit,
    onAdd: (
        title: String,
        category: String,
        provider: String,
        officialUrl: String,
        regUrl: String,
        syllabusUrl: String,
        costPrize: String,
        difficulty: String,
        mode: String,
        desc: String,
        skills: List<String>
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("HACKATHON") }
    var officialUrl by remember { mutableStateOf("") }
    var regUrl by remember { mutableStateOf("") }
    var syllabusUrl by remember { mutableStateOf("") }
    var costPrize by remember { mutableStateOf("Free") }
    var difficulty by remember { mutableStateOf("Intermediate") }
    var mode by remember { mutableStateOf("Online / Remote") }
    var desc by remember { mutableStateOf("") }
    var skillsInput by remember { mutableStateOf("") }

    val categories = listOf("CERTIFICATION", "HACKATHON", "FELLOWSHIP", "OPEN_SOURCE", "HIRING_CHALLENGE")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            shape = RoundedCornerShape(Dimens.RadiusLg),
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceMd)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
            ) {
                item {
                    Text(
                        text = "Add Opportunity / Link",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Save external hackathon, cert exam, or fellowship link.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title / Competition Name *") },
                        placeholder = { Text("e.g. AWS Cloud Practitioner / ETHGlobal Paris") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = provider,
                        onValueChange = { provider = it },
                        label = { Text("Provider / Host *") },
                        placeholder = { Text("e.g. Amazon Web Services / Devpost") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Category:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = officialUrl,
                        onValueChange = { officialUrl = it },
                        label = { Text("Official Link / Website URL *") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = regUrl,
                        onValueChange = { regUrl = it },
                        label = { Text("Registration Link (Optional)") },
                        placeholder = { Text("https://.../register") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = syllabusUrl,
                        onValueChange = { syllabusUrl = it },
                        label = { Text("Syllabus / Rulebook Link (Optional)") },
                        placeholder = { Text("https://.../guide.pdf") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
                    ) {
                        OutlinedTextField(
                            value = costPrize,
                            onValueChange = { costPrize = it },
                            label = { Text("Cost / Prize") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = mode,
                            onValueChange = { mode = it },
                            label = { Text("Format / Mode") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = skillsInput,
                        onValueChange = { skillsInput = it },
                        label = { Text("Target Skills (Comma separated)") },
                        placeholder = { Text("React, Docker, AWS") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description & Notes") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                        Button(
                            onClick = {
                                if (title.isNotBlank() && officialUrl.isNotBlank()) {
                                    val skills = skillsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    onAdd(title, category, provider, officialUrl, regUrl, syllabusUrl, costPrize, difficulty, mode, desc, skills)
                                }
                            },
                            enabled = title.isNotBlank() && officialUrl.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Save Opportunity", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun openBrowserUrl(context: Context, url: String) {
    try {
        val targetUri = if (url.startsWith("http://") || url.startsWith("https://")) {
            Uri.parse(url)
        } else {
            Uri.parse("https://$url")
        }
        val intent = Intent(Intent.ACTION_VIEW, targetUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open link: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun copyToClipboard(context: Context, text: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Opportunity Link", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to copy link", Toast.LENGTH_SHORT).show()
    }
}
