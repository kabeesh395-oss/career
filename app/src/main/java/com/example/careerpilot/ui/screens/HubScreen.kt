package com.example.careerpilot.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.ui.animation.bouncyClickable
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.theme.DesignSystem

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
        HubItem("Skill Matrix", "Target role calibration", Icons.Default.Psychology, "career", DesignSystem.Colors.PrimaryLight, DesignSystem.Colors.PrimaryContainer),
        HubItem("Roadmap", "Phased milestones", Icons.Default.Map, "roadmap", DesignSystem.Colors.Success, DesignSystem.Colors.SuccessContainer),
        HubItem("Resume", "ATS keyword scan", Icons.Default.Description, "resume", DesignSystem.Colors.Warning, DesignSystem.Colors.WarningContainer),
        HubItem("Interview", "Mock sessions", Icons.Default.RecordVoiceOver, "interview", DesignSystem.Colors.AccentCyan, DesignSystem.Colors.AccentCyan.copy(alpha = 0.15f))
    )

    val practiceSection = listOf(
        HubItem("Projects", "Portfolio tracker", Icons.Default.Folder, "projects", DesignSystem.Colors.AccentPurple, DesignSystem.Colors.AccentPurple.copy(alpha = 0.15f)),
        HubItem("Learning", "Skill tracks", Icons.Default.School, "learning", DesignSystem.Colors.Success, DesignSystem.Colors.SuccessContainer),
        HubItem("Integrations", "GitHub sync", Icons.Default.Code, "integrations", DesignSystem.Colors.Danger, DesignSystem.Colors.DangerContainer),
        HubItem("Market", "Opportunities", Icons.Default.TravelExplore, "market", DesignSystem.Colors.PrimaryLight, DesignSystem.Colors.PrimaryContainer)
    )

    val portfolioSection = listOf(
        HubItem("Analytics", "Career insights", Icons.Default.BarChart, "analytics", DesignSystem.Colors.AccentCyan, DesignSystem.Colors.AccentCyan.copy(alpha = 0.15f)),
        HubItem("Peer Mock", "Practice partners", Icons.Default.Groups, "peers", DesignSystem.Colors.AccentPurple, DesignSystem.Colors.AccentPurple.copy(alpha = 0.15f)),
        HubItem("Sprints", "Focused drills", Icons.Default.Timer, "sprints", DesignSystem.Colors.Warning, DesignSystem.Colors.WarningContainer),
        HubItem("Negotiator", "Salary coaching", Icons.Default.AttachMoney, "negotiator", DesignSystem.Colors.Success, DesignSystem.Colors.SuccessContainer)
    )

    val settingsSection = listOf(
        HubItem("Profile", "Account & preferences", Icons.Default.Person, "profile", DesignSystem.Colors.TextMuted, DesignSystem.Colors.BgMuted),
        HubItem("Export", "Data portability", Icons.Default.Download, "export", DesignSystem.Colors.TextMuted, DesignSystem.Colors.BgMuted),
        HubItem("Sandbox", "Code playground", Icons.Default.DeveloperMode, "sandbox", DesignSystem.Colors.PrimaryLight, DesignSystem.Colors.PrimaryContainer),
        HubItem("Opportunities", "Job board", Icons.Default.WorkOutline, "opportunities", DesignSystem.Colors.AccentCyan, DesignSystem.Colors.AccentCyan.copy(alpha = 0.15f))
    )

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = DesignSystem.Spacing.ContentHorizontal,
                vertical = DesignSystem.Spacing.ContentVertical
            ),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Section)
    ) {
        // Hero
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))
                Text(
                    text = "CareerPilot",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = DesignSystem.Colors.TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                Text(
                    text = "Your career, systematized",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DesignSystem.Colors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Career section
        item { SectionHeader(title = "Career Intelligence") }
        item { HubGrid(items = careerSection, onNavigate = onNavigate) }

        // Practice section
        item { SectionHeader(title = "Practice & Build") }
        item { HubGrid(items = practiceSection, onNavigate = onNavigate) }

        // Portfolio section
        item { SectionHeader(title = "Portfolio") }
        item { HubGrid(items = portfolioSection, onNavigate = onNavigate) }

        // Settings section
        item { SectionHeader(title = "Settings") }
        item { HubGrid(items = settingsSection, onNavigate = onNavigate) }

        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space8))
    }
}

@Composable
private fun HubGrid(
    items: List<HubItem>,
    onNavigate: (String) -> Unit
) {
    LazyVerticalGrid(
        cells = GridCells.Fixed(2),
        contentPadding = PaddingValues(0.dp),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.ItemSpacing),
        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.ItemSpacing)
    ) {
        items(items) { item ->
            HubGridItem(
                item = item,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}

@Composable
private fun HubGridItem(
    item: HubItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(DesignSystem.Shape.RadiusLg)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DesignSystem.Colors.BgCard)
            .border(DesignSystem.Sizing.CardBorderWidth, DesignSystem.Colors.BorderDefault, shape)
            .bouncyClickable(onClick = onClick)
            .padding(DesignSystem.Spacing.Space3)
            .testTag("hub_item_${item.route}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space2)
        ) {
            Box(
                modifier = Modifier
                    .size(DesignSystem.Sizing.AvatarLg)
                    .clip(RoundedCornerShape(DesignSystem.Shape.RadiusMd))
                    .background(item.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconTint,
                    modifier = Modifier.size(DesignSystem.Sizing.IconLg)
                )
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = DesignSystem.Colors.TextPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = DesignSystem.Colors.TextMuted,
                maxLines = 2,
                minLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                lineHeight = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
