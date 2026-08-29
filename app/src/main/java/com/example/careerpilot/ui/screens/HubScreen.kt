package com.example.careerpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.ui.animation.bouncyClickable
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel

enum class HubTab(val title: String, val icon: ImageVector) {
    MARKET("Market Insights", Icons.Default.TravelExplore),
    SKILLS("Skill Matrix", Icons.Default.Assessment),
    APPLICATIONS("Applications", Icons.Default.WorkOutline),
    COMPENSATION("Offer Negotiator", Icons.Default.MonetizationOn),
    EXPORTS("Export Portfolio", Icons.Default.FileDownload),
    SANDBOX("Code Sandbox", Icons.Default.Terminal),
    SPRINTS("Skill Sprints", Icons.Default.EmojiEvents),
    PEERS("Peer Mocks", Icons.Default.People),
    ROADMAP("Roadmap", Icons.Default.Timeline),
    PROJECTS("Projects", Icons.Default.Code),
    LEARNING("Learning", Icons.Default.MenuBook),
    INTEGRATIONS("Sync Integrations", Icons.Default.Sync),
    PROFILE("Profile & Settings", Icons.Default.Person)
}

@Composable
fun HubScreen(
    viewModel: CareerViewModel,
    initialTab: HubTab = HubTab.SKILLS,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Modern Horizontal Scrollable Tab Selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgSurface)
                .drawBehind {
                    drawLine(
                        color = BorderSubtle,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(HubTab.values()) { _, tab ->
                    val isSelected = selectedTab == tab
                    val shape = RoundedCornerShape(8.dp)

                    Box(
                        modifier = Modifier
                            .clip(shape)
                            .background(
                                if (isSelected) PrimaryBlue.copy(alpha = 0.15f)
                                else BgCard
                            )
                            .border(
                                1.dp,
                                if (isSelected) PrimaryBlue.copy(alpha = 0.4f) else BorderSubtle,
                                shape
                            )
                            .bouncyClickable { selectedTab = tab }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("hub_tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = if (isSelected) PrimaryBlueGlow else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Selected Subscreen Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedTab) {
                HubTab.MARKET -> MarketIntelligenceScreen(viewModel = viewModel)
                HubTab.SKILLS -> CareerAnalysisScreen(viewModel = viewModel)
                HubTab.APPLICATIONS -> ApplicationTrackerScreen(viewModel = viewModel)
                HubTab.COMPENSATION -> SalaryNegotiatorScreen(viewModel = viewModel)
                HubTab.EXPORTS -> ExportCenterScreen(viewModel = viewModel)
                HubTab.SANDBOX -> CodingSandboxScreen(viewModel = viewModel)
                HubTab.SPRINTS -> SkillSprintsScreen(viewModel = viewModel)
                HubTab.PEERS -> PeerMockScreen(viewModel = viewModel)
                HubTab.ROADMAP -> RoadmapScreen(viewModel = viewModel)
                HubTab.PROJECTS -> ProjectsScreen(viewModel = viewModel)
                HubTab.LEARNING -> LearningScreen(viewModel = viewModel)
                HubTab.INTEGRATIONS -> IntegrationsScreen(viewModel = viewModel)
                HubTab.PROFILE -> ProfileScreen(viewModel = viewModel)
            }
        }
    }
}
