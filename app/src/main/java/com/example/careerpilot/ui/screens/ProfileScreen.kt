package com.example.careerpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.repository.BenchmarkCatalog
import com.example.careerpilot.ui.animation.*
import com.example.careerpilot.ui.components.*
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel

@Composable
fun ProfileScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val authUser by viewModel.authUserState.collectAsState()
    val syncStatus by viewModel.cloudSyncStatus.collectAsState()

    var fullName by remember(profile) { mutableStateOf(profile?.fullName ?: "Alex Chen") }
    var headline by remember(profile) { mutableStateOf(profile?.headline ?: "Aspiring Senior Full Stack Engineer") }
    var bio by remember(profile) { mutableStateOf(profile?.bio ?: "Passionate engineer focusing on modern architectures.") }
    var location by remember(profile) { mutableStateOf(profile?.location ?: "San Francisco, CA") }
    var education by remember(profile) { mutableStateOf(profile?.education ?: "B.S. Computer Science") }
    var expYears by remember(profile) { mutableStateOf(profile?.experienceYears?.toString() ?: "2.5") }
    var targetRole by remember(profile) { mutableStateOf(profile?.targetRole ?: "Full Stack Engineer") }
    var targetIndustry by remember(profile) { mutableStateOf(profile?.targetIndustry ?: "Fintech & Cloud Infrastructure") }
    var targetSalary by remember(profile) { mutableStateOf(profile?.targetSalary ?: "$140,000 - $175,000") }
    var targetCompanyTier by remember(profile) { mutableStateOf(profile?.targetCompanyTier ?: "Tier-1 Tech / Scaleups") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Career Profile & Calibration",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Set your background and career target objectives",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Firebase Auth & Cloud Firestore Persistence Hero
        item {
            AnimatedGlowingGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firebase_auth_firestore_card"),
                backgroundColor = BgSurfaceElevated
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
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (authUser.isAuthenticated) SuccessGreen.copy(alpha = 0.2f) else PrimaryBlue.copy(alpha = 0.2f))
                                .border(1.dp, if (authUser.isAuthenticated) SuccessGreen else PrimaryBlueGlow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (authUser.isAuthenticated) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = if (authUser.isAuthenticated) SuccessGreen else PrimaryBlueGlow,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            PulsingLiveBadge(
                                text = if (authUser.isAuthenticated) "FIREBASE AUTH CONNECTED" else "CLOUD SYNC READY",
                                color = if (authUser.isAuthenticated) SuccessGreen else AccentCyan
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = authUser.displayName ?: "Local User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = authUser.email ?: "local.dev@careerpilot.io",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (authUser.isAuthenticated) {
                        OutlinedButton(
                            onClick = { viewModel.signOut() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.bouncyClickable { viewModel.signOut() }
                        ) {
                            Text("Sign Out", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cloud Firestore Persistence:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                        Text(
                            text = syncStatus.syncStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!authUser.isAuthenticated) {
                        Button(
                            onClick = { viewModel.signInWithGoogle() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .bouncyClickable { viewModel.signInWithGoogle() }
                                .testTag("google_signin_button")
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign in with Google", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { viewModel.triggerCloudSync() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (authUser.isAuthenticated) SuccessGreen else BgCardHover
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .bouncyClickable { viewModel.triggerCloudSync() }
                            .testTag("firestore_sync_button")
                    ) {
                        if (syncStatus.isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TextPrimary, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync Firestore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 1-Click Career Starter Presets
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("career_starter_presets_card"),
                borderColor = AccentCyan.copy(alpha = 0.4f),
                backgroundColor = BgSurfaceElevated
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "⚡ Instant Career Presets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                        Text(
                            text = "1-click auto-setup profile, skills, roadmap & projects",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val presets = listOf(
                    "Full Stack Engineer",
                    "Android Mobile Engineer",
                    "AI / Machine Learning Engineer",
                    "DevOps / Cloud Architect"
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { presetRole ->
                        val isSelected = targetRole.equals(presetRole, ignoreCase = true)
                        Surface(
                            onClick = {
                                viewModel.applyCareerStarterTemplate(presetRole)
                            },
                            color = if (isSelected) PrimaryBlue.copy(alpha = 0.2f) else BgCard,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) PrimaryBlueGlow else BorderSubtle
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = presetRole,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PrimaryBlueGlow else TextPrimary
                                    )
                                    Text(
                                        text = if (isSelected) "Active Profile Trajectory" else "Tap to apply 1-click preset",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) SuccessGreen else TextMuted
                                    )
                                }
                                Text(
                                    text = if (isSelected) "Active ✓" else "Apply ⚡",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) SuccessGreen else AccentCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Personal & Target Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = headline,
                    onValueChange = { headline = it },
                    label = { Text("Professional Headline") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetRole,
                    onValueChange = { targetRole = it },
                    label = { Text("Target Engineering Role") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_target_role_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = expYears,
                        onValueChange = { expYears = it },
                        label = { Text("Experience (Years)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = education,
                    onValueChange = { education = it },
                    label = { Text("Education / Highest Degree") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetIndustry,
                    onValueChange = { targetIndustry = it },
                    label = { Text("Target Industry") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetSalary,
                    onValueChange = { targetSalary = it },
                    label = { Text("Target Compensation Range") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetCompanyTier,
                    onValueChange = { targetCompanyTier = it },
                    label = { Text("Target Company Tier") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Executive Summary / Bio") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val parsedYears = expYears.toFloatOrNull() ?: 2.0f
                        viewModel.updateProfile(
                            fullName = fullName.trim(),
                            headline = headline.trim(),
                            bio = bio.trim(),
                            location = location.trim(),
                            education = education.trim(),
                            experienceYears = parsedYears,
                            targetRole = targetRole.trim(),
                            targetIndustry = targetIndustry.trim(),
                            targetSalary = targetSalary.trim(),
                            targetCompanyTier = targetCompanyTier.trim()
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_profile_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Profile & Recalibrate")
                }
            }
        }
    }
}
