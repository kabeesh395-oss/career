package com.example.careerpilot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.careerpilot.ui.animation.*
import com.example.careerpilot.ui.screens.*
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Dashboard)
    object Audit : Screen("audit", "Audit", Icons.Default.Shield)
    object Resume : Screen("resume", "Resume", Icons.Default.Description)
    object Interview : Screen("interview", "Interviews", Icons.Default.RecordVoiceOver)
    object Market : Screen("market", "Market", Icons.Default.TravelExplore)
    object Hub : Screen("hub", "Career Hub", Icons.Default.Hub)
    object Career : Screen("career", "Skills", Icons.Default.Assessment)
    object Roadmap : Screen("roadmap", "Roadmap", Icons.Default.Timeline)
    object Projects : Screen("projects", "Projects", Icons.Default.Code)
    object Learning : Screen("learning", "Learning", Icons.Default.MenuBook)
    object Integrations : Screen("integrations", "Sync", Icons.Default.Sync)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Applications : Screen("applications", "Applications", Icons.Default.WorkOutline)
    object Sandbox : Screen("sandbox", "Sandbox", Icons.Default.Terminal)
    object Sprints : Screen("sprints", "Sprints", Icons.Default.EmojiEvents)
    object Peers : Screen("peers", "Peer Mocks", Icons.Default.People)
    object Negotiator : Screen("negotiator", "Negotiator", Icons.Default.MonetizationOn)
    object Export : Screen("export", "Export", Icons.Default.FileDownload)
}

val primaryNavItems = listOf(
    Screen.Dashboard,
    Screen.Resume,
    Screen.Interview,
    Screen.Hub
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerPilotApp(
    viewModel: CareerViewModel = viewModel()
) {
    var showSplashScreen by remember { mutableStateOf(true) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage by viewModel.userMessage.collectAsState()

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    if (showSplashScreen) {
        CareerHubSplashScreen(
            onSplashFinished = { showSplashScreen = false }
        )
    } else {
        AmbientGlassBackground {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PrimaryBlue.copy(alpha = 0.15f))
                                        .border(1.dp, PrimaryBlue.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Hub,
                                        contentDescription = null,
                                        tint = PrimaryBlueGlow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Career Hub",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Engineering Portfolio & Career OS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BgSurface,
                        titleContentColor = TextPrimary
                    ),
                    modifier = Modifier
                        .drawBehind {
                            drawLine(
                                color = BorderSubtle,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        },
                    actions = {
                        IconButton(
                            onClick = { viewModel.triggerCloudSync() },
                            modifier = Modifier
                                .testTag("topbar_sync_btn")
                                .bouncyClickable { viewModel.triggerCloudSync() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SuccessGreen.copy(alpha = 0.12f))
                                    .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Cloud Firestore Sync",
                                    tint = SuccessGreenGlow,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = { navController.navigate(Screen.Profile.route) },
                            modifier = Modifier.bouncyClickable { navController.navigate(Screen.Profile.route) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BgCard)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profile",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = BgSurface,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .drawBehind {
                            drawLine(
                                color = BorderSubtle,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                ) {
                    primaryNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route || 
                            (screen == Screen.Hub && listOf("career", "roadmap", "projects", "learning", "integrations", "profile", "hub", "applications", "sandbox", "sprints", "peers", "negotiator", "export").contains(currentRoute))
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlueGlow,
                                selectedTextColor = PrimaryBlueGlow,
                                indicatorColor = PrimaryBlue.copy(alpha = 0.15f),
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_${screen.route}")
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(Screen.Audit.route) {
                    AuditCenterScreen(
                        viewModel = viewModel,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(Screen.Resume.route) {
                    ResumeAuditScreen(viewModel = viewModel)
                }
                composable(Screen.Interview.route) {
                    InterviewScreen(viewModel = viewModel)
                }
                composable(Screen.Market.route) {
                    MarketIntelligenceScreen(
                        viewModel = viewModel,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(Screen.Hub.route) {
                    HubScreen(viewModel = viewModel)
                }
                composable(Screen.Career.route) {
                    CareerAnalysisScreen(viewModel = viewModel)
                }
                composable(Screen.Roadmap.route) {
                    RoadmapScreen(viewModel = viewModel)
                }
                composable(Screen.Projects.route) {
                    ProjectsScreen(viewModel = viewModel)
                }
                composable(Screen.Learning.route) {
                    LearningScreen(viewModel = viewModel)
                }
                composable(Screen.Integrations.route) {
                    IntegrationsScreen(viewModel = viewModel)
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(viewModel = viewModel)
                }
                composable(Screen.Applications.route) {
                    ApplicationTrackerScreen(viewModel = viewModel)
                }
                composable(Screen.Sandbox.route) {
                    CodingSandboxScreen(viewModel = viewModel)
                }
                composable(Screen.Sprints.route) {
                    SkillSprintsScreen(viewModel = viewModel)
                }
                composable(Screen.Peers.route) {
                    PeerMockScreen(viewModel = viewModel)
                }
                composable(Screen.Negotiator.route) {
                    SalaryNegotiatorScreen(viewModel = viewModel)
                }
                composable(Screen.Export.route) {
                    ExportCenterScreen(viewModel = viewModel)
                }
            }
        }
    }
}
}
