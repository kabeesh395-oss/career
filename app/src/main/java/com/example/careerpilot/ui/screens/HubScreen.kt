package com.example.careerpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.example.careerpilot.ui.animation.bouncyClickable
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.theme.Dimens

private data class HubItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val iconTint: Color,
    val iconBg: Color
)

@Composable
fun HubScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val careerSection = listOf(
        HubItem("Skill Matrix", "Target role competency benchmarks", Icons.Default.Assessment, "career", PrimaryBlueLighter, PrimaryBlue.copy(alpha = 0.12f)),
        HubItem("Roadmap", "Quarterly career milestones", Icons.Default.Timeline, "roadmap", SuccessGreenLight, SuccessGreen.copy(alpha = 0.12f)),
        HubItem("Market Intel", "Hiring & compensation trends", Icons.Default.TravelExplore, "market", AccentCyanLight, AccentCyan.copy(alpha = 0.12f)),
        HubItem("Audit Center", "Enterprise verification checks", Icons.Default.Shield, "audit", WarningAmberLight, WarningAmber.copy(alpha = 0.12f))
    )

    val practiceSection = listOf(
        HubItem("Code Sandbox", "Live syntax & algorithm lab", Icons.Default.Terminal, "sandbox", AccentPurple, AccentPurple.copy(alpha = 0.12f)),
        HubItem("Skill Sprints", "Time-boxed learning challenges", Icons.Default.EmojiEvents, "sprints", WarningAmberLight, WarningAmber.copy(alpha = 0.12f)),
        HubItem("Peer Mocks", "Collaborative mock sessions", Icons.Default.People, "peers", AccentCyanLight, AccentCyan.copy(alpha = 0.12f)),
        HubItem("Negotiator", "Salary & equity tactics", Icons.Default.MonetizationOn, "negotiator", SuccessGreenLight, SuccessGreen.copy(alpha = 0.12f))
    )

    val portfolioSection = listOf(
        HubItem("Opportunities", "Curated positions & tiers", Icons.Default.Stars, "opportunities", WarningAmberLight, WarningAmber.copy(alpha = 0.12f)),
        HubItem("Projects", "Portfolio code artifacts", Icons.Default.Code, "projects", PrimaryBlueLighter, PrimaryBlue.copy(alpha = 0.12f)),
        HubItem("Learning", "Certifications & pathways", Icons.Default.MenuBook, "learning", AccentPurple, AccentPurple.copy(alpha = 0.12f)),
        HubItem("Applications", "CRM pipeline & stages", Icons.Default.WorkOutline, "applications", AccentCyanLight, AccentCyan.copy(alpha = 0.12f)),
        HubItem("Export", "PDF & JSON reports", Icons.Default.FileDownload, "export", TextSecondary, BgMuted)
    )

    val settingsSection = listOf(
        HubItem("Profile", "Target preferences & background", Icons.Default.Person, "profile", PrimaryBlueLighter, PrimaryBlue.copy(alpha = 0.12f)),
        HubItem("Integrations", "Sync GitHub, LinkedIn, Cloud", Icons.Default.Sync, "integrations", SuccessGreenLight, SuccessGreen.copy(alpha = 0.12f))
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ContentHorizontalPadding),
        contentPadding = PaddingValues(
            top = Dimens.ContentTopPadding,
            bottom = Dimens.ContentBottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXl)
    ) {
        item {
            SectionHeader(title = "Career Analysis")
        }
        item {
            HubGrid(items = careerSection, onNavigate = onNavigate)
        }

        item {
            SectionHeader(title = "Practice & Prep")
        }
        item {
            HubGrid(items = practiceSection, onNavigate = onNavigate)
        }

        item {
            SectionHeader(title = "Portfolio")
        }
        item {
            HubGrid(items = portfolioSection, onNavigate = onNavigate)
        }

        item {
            SectionHeader(title = "Settings")
        }
        item {
            HubGrid(items = settingsSection, onNavigate = onNavigate)
        }
    }
}

@Composable
private fun HubGrid(
    items: List<HubItem>,
    onNavigate: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)) {
        var i = 0
        while (i < items.size) {
            if (i == items.size - 1) {
                // Balanced trailing item spans full width
                HubGridItem(
                    item = items[i],
                    onClick = { onNavigate(items[i].route) },
                    modifier = Modifier.fillMaxWidth()
                )
                i++
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)
                ) {
                    HubGridItem(
                        item = items[i],
                        onClick = { onNavigate(items[i].route) },
                        modifier = Modifier.weight(1f)
                    )
                    HubGridItem(
                        item = items[i + 1],
                        onClick = { onNavigate(items[i + 1].route) },
                        modifier = Modifier.weight(1f)
                    )
                }
                i += 2
            }
        }
    }
}

@Composable
private fun HubGridItem(
    item: HubItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(BgSurface)
            .border(1.dp, BorderSubtle, shape)
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("hub_item_${item.route}")
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(item.iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.iconTint,
                modifier = Modifier.size(Dimens.IconMd)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = TextSecondary,
            maxLines = 2,
            minLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            lineHeight = 15.sp
        )
    }
}
