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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.careerpilot.ui.screens.*
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Dashboard)
    object Audit : Screen("audit", "Audit", Icons.Default.Shield)
    object Resume : Screen("resume", "Resume ATS", Icons.Default.Description)
    object Interview : Screen("interview", "Mock AI", Icons.Default.RecordVoiceOver)
    object Hub : Screen("hub", "Career Hub", Icons.Default.Hub)
    object Career : Screen("career", "Skill Gaps", Icons.Default.Assessment)
    object Roadmap : Screen("roadmap", "Roadmap", Icons.Default.Timeline)
    object Projects : Screen("projects", "Projects", Icons.Default.Code)
    object Learning : Screen("learning", "Learning", Icons.Default.MenuBook)
    object Integrations : Screen("integrations", "Sync", Icons.Default.Sync)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

val primaryNavItems = listOf(
    Screen.Dashboard,
    Screen.Audit,
    Screen.Resume,
    Screen.Interview,
    Screen.Hub
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerPilotApp(
    viewModel: CareerViewModel = viewModel()
) {
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
                                .clip(CircleShape)
                                .background(PrimaryBlue.copy(alpha = 0.2f))
                                .border(1.dp, PrimaryBlueGlow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = PrimaryBlueGlow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "CareerPilot AI",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Intelligence & Career Trajectory Engine",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 9.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgSurface,
                    titleContentColor = TextPrimary
                ),
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = PrimaryBlueGlow
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = BgSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(1.dp, BorderSubtle, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                primaryNavItems.forEach { screen ->
                    val selected = currentRoute == screen.route || 
                        (screen == Screen.Hub && listOf("career", "roadmap", "projects", "learning", "integrations", "profile", "hub").contains(currentRoute))
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
                            selectedIconColor = TextPrimary,
                            selectedTextColor = PrimaryBlueGlow,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_${screen.route}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BgBase
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
        }
    }
}
