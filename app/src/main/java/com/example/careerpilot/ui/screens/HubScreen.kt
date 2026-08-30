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
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.theme.Dimens

private data class HubItem(
    val title: String,
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
        HubItem("Skill Matrix", Icons.Default.Assessment, "career", PrimaryBlueLighter, PrimaryBlue.copy(alpha = 0.12f)),
        HubItem("Roadmap", Icons.Default.Timeline, "roadmap", SuccessGreenLight, SuccessGreen.copy(alpha = 0.12f)),
        HubItem("Market Intel", Icons.Default.TravelExplore, "market", AccentCyanLight, AccentCyan.copy(alpha = 0.12f)),
        HubItem("Audit Center", Icons.Default.Shield, "audit", WarningAmberLight, WarningAmber.copy(alpha = 0.12f))
    )

    val practiceSection = listOf(
        HubItem("Code Sandbox", Icons.Default.Terminal, "sandbox", AccentPurple, AccentPurple.copy(alpha = 0.12f)),
        HubItem("Skill Sprints", Icons.Default.EmojiEvents, "sprints", WarningAmberLight, WarningAmber.copy(alpha = 0.12f)),
        HubItem("Peer Mocks", Icons.Default.People, "peers", AccentCyanLight, AccentCyan.copy(alpha = 0.12f)),
        HubItem("Negotiator", Icons.Default.MonetizationOn, "negotiator", SuccessGreenLight, SuccessGreen.copy(alpha = 0.12f))
    )

    val portfolioSection = listOf(
        HubItem("Projects", Icons.Default.Code, "projects", PrimaryBlueLighter, PrimaryBlue.copy(alpha = 0.12f)),
        HubItem("Learning", Icons.Default.MenuBook, "learning", AccentPurple, AccentPurple.copy(alpha = 0.12f)),
        HubItem("Applications", Icons.Default.WorkOutline, "applications", AccentCyanLight, AccentCyan.copy(alpha = 0.12f)),
        HubItem("Export", Icons.Default.FileDownload, "export", TextSecondary, BgMuted)
    )

    val settingsSection = listOf(
        HubItem("Profile", Icons.Default.Person, "profile", PrimaryBlueLighter, PrimaryBlue.copy(alpha = 0.12f)),
        HubItem("Integrations", Icons.Default.Sync, "integrations", SuccessGreenLight, SuccessGreen.copy(alpha = 0.12f))
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
    val columns = 2
    val rows = (items.size + columns - 1) / columns

    Column(verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)
            ) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    if (index < items.size) {
                        HubGridItem(
                            item = items[index],
                            onClick = { onNavigate(items[index].route) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
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
    val shape = RoundedCornerShape(Dimens.RadiusMd)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(shape)
            .background(BgCard)
            .border(Dimens.CardBorderWidth, BorderSubtle, shape)
            .clickable { onClick() }
            .padding(vertical = Dimens.SpaceLg, horizontal = Dimens.SpaceMd)
            .testTag("hub_item_${item.route}")
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.AvatarMd)
                .clip(RoundedCornerShape(Dimens.RadiusSm))
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
        Spacer(modifier = Modifier.height(Dimens.SpaceSm))
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
