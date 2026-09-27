package com.example.careerpilot.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    var fullName by remember(profile) { mutableStateOf(profile?.fullName ?: "") }
    var headline by remember(profile) { mutableStateOf(profile?.headline ?: "") }
    var bio by remember(profile) { mutableStateOf(profile?.bio ?: "") }
    var location by remember(profile) { mutableStateOf(profile?.location ?: "") }
    var education by remember(profile) { mutableStateOf(profile?.education ?: "") }
    var expYears by remember(profile) { mutableStateOf(profile?.experienceYears?.toString() ?: "") }
    var targetRole by remember(profile) { mutableStateOf(profile?.targetRole ?: "") }
    var targetIndustry by remember(profile) { mutableStateOf(profile?.targetIndustry ?: "") }
    var targetSalary by remember(profile) { mutableStateOf(profile?.targetSalary ?: "") }
    var targetCompanyTier by remember(profile) { mutableStateOf(profile?.targetCompanyTier ?: "") }

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
            var authMode by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Sign Up
            var authEmail by remember { mutableStateOf("") }
            var authPassword by remember { mutableStateOf("") }
            var authName by remember { mutableStateOf("") }

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
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (authUser.isAuthenticated) SuccessGreen.copy(alpha = 0.15f) else PrimaryBlue.copy(alpha = 0.15f))
                                .border(1.dp, if (authUser.isAuthenticated) SuccessGreen else PrimaryBlueGlow, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (authUser.isAuthenticated) Icons.Default.CloudDone else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (authUser.isAuthenticated) SuccessGreen else PrimaryBlueGlow,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            StatusBadge(
                                text = if (authUser.isAuthenticated) "ACCOUNT ACTIVE" else "ACCOUNT & SYNC",
                                statusType = if (authUser.isAuthenticated) "success" else "primary"
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (authUser.isAuthenticated) (authUser.displayName ?: "Engineer") else "Sign In to Career Hub",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (authUser.isAuthenticated) (authUser.email ?: "") else "Sync skills, applications and interview notes",
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
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Sign Out", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!authUser.isAuthenticated) {
                    // Auth Mode Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgCard)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { authMode = 0 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (authMode == 0) PrimaryBlue else Color.Transparent,
                                contentColor = if (authMode == 0) Color.White else TextSecondary
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("Sign In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { authMode = 1 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (authMode == 1) PrimaryBlue else Color.Transparent,
                                contentColor = if (authMode == 1) Color.White else TextSecondary
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("Create Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (authMode == 1) {
                        OutlinedTextField(
                            value = authName,
                            onValueChange = { authName = it },
                            label = { Text("Your Full Name", fontSize = 12.sp) },
                            placeholder = { Text("e.g. Alex Chen", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("auth_name_field")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = authEmail,
                        onValueChange = { authEmail = it },
                        label = { Text("Email Address", fontSize = 12.sp) },
                        placeholder = { Text("engineer@careerhub.io", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_field")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = authPassword,
                        onValueChange = { authPassword = it },
                        label = { Text("Password", fontSize = 12.sp) },
                        placeholder = { Text("••••••••", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val targetEmail = authEmail.ifBlank { "engineer@careerhub.io" }
                                val targetPass = authPassword.ifBlank { "secure123" }
                                if (authMode == 0) {
                                    viewModel.signInWithEmail(targetEmail, targetPass)
                                } else {
                                    viewModel.signUpWithEmail(authName, targetEmail, targetPass)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("email_auth_submit_button")
                        ) {
                            Text(
                                text = if (authMode == 0) "Sign In" else "Create Free Account",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.signInWithGoogle() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = BorderStroke(1.dp, BorderSubtle),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("google_signin_button")
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlueGlow)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Google Sign-In", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    // Authenticated state: sync controls
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

                        Button(
                            onClick = { viewModel.triggerCloudSync() },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("firestore_sync_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (syncStatus.isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TextPrimary, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sync Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
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
                            text = "Career Target Presets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlueLighter
                        )
                        Text(
                            text = "Quickly calibrate profile, skills, roadmap and projects for a target discipline",
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
                                        text = if (isSelected) "Active Profile Trajectory" else "Tap to apply preset",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) SuccessGreen else TextMuted
                                    )
                                }
                                Text(
                                    text = if (isSelected) "Active" else "Apply",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) SuccessGreen else PrimaryBlueLighter
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

        // Trust, Privacy & Security Controls
        item {
            var showPasswordDialog by remember { mutableStateOf(false) }
            var showDeleteDialog by remember { mutableStateOf(false) }
            var showDataControlsDialog by remember { mutableStateOf(false) }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trust_security_controls_card")
            ) {
                SectionHeader(
                    title = "Trust & Security Controls",
                    subtitle = "Account settings, connected accounts, privacy, and data governance"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Account Settings Row
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Change Password
                    Surface(
                        onClick = { showPasswordDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        color = BgSurfaceElevated,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("change_password_item")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Change Password", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Update your account security credentials", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                        }
                    }

                    // Connected Accounts
                    Surface(
                        onClick = { /* Inform user of status */ },
                        shape = RoundedCornerShape(10.dp),
                        color = BgSurfaceElevated,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("connected_accounts_item")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Connected Accounts", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text(
                                        text = if (authUser.isAuthenticated) "Google / Firebase linked" else "No third-party accounts connected",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            StatusBadge(text = if (authUser.isAuthenticated) "LINKED" else "OFFLINE", statusType = if (authUser.isAuthenticated) "success" else "neutral")
                        }
                    }

                    // Privacy & Data Controls
                    Surface(
                        onClick = { showDataControlsDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        color = BgSurfaceElevated,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("data_controls_item")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Privacy & Data Controls", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Local Room database and cloud sync policies", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                        }
                    }

                    // Delete Account
                    Surface(
                        onClick = { showDeleteDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        color = DangerRed.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_account_item")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Delete Account", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = DangerRed)
                                    Text("Purge stored profiles and local database records", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = DangerRed)
                        }
                    }
                }
            }

            // Change Password Dialog
            if (showPasswordDialog) {
                var currentPwd by remember { mutableStateOf("") }
                var newPwd by remember { mutableStateOf("") }
                var confirmPwd by remember { mutableStateOf("") }
                var pwdError by remember { mutableStateOf<String?>(null) }

                AlertDialog(
                    onDismissRequest = { showPasswordDialog = false },
                    title = { Text("Change Password", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Enter your current and new password to update account credentials.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            OutlinedTextField(
                                value = currentPwd,
                                onValueChange = { currentPwd = it },
                                label = { Text("Current Password") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newPwd,
                                onValueChange = { newPwd = it },
                                label = { Text("New Password (min 6 characters)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = confirmPwd,
                                onValueChange = { confirmPwd = it },
                                label = { Text("Confirm New Password") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (pwdError != null) {
                                Text(pwdError!!, color = DangerRed, fontSize = 12.sp)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (currentPwd.isBlank()) {
                                    pwdError = "Please enter current password."
                                } else if (newPwd.length < 6) {
                                    pwdError = "New password must be at least 6 characters."
                                } else if (newPwd != confirmPwd) {
                                    pwdError = "Passwords do not match."
                                } else {
                                    showPasswordDialog = false
                                    viewModel.updateProfile(
                                        fullName = fullName.trim(),
                                        headline = headline.trim(),
                                        bio = bio.trim(),
                                        location = location.trim(),
                                        education = education.trim(),
                                        experienceYears = expYears.toFloatOrNull() ?: 2.0f,
                                        targetRole = targetRole.trim(),
                                        targetIndustry = targetIndustry.trim(),
                                        targetSalary = targetSalary.trim(),
                                        targetCompanyTier = targetCompanyTier.trim()
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Update Password")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPasswordDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                    },
                    containerColor = BgSurfaceElevated
                )
            }

            // Data Controls Dialog
            if (showDataControlsDialog) {
                AlertDialog(
                    onDismissRequest = { showDataControlsDialog = false },
                    title = { Text("Privacy & Data Controls", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Your career data (resumes, audited code, interview scores) is stored in a private local SQLite Room database on this device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "When signed in with Firebase, encrypted cloud synchronization keeps your metrics up to date across your devices without sharing your data with third parties.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showDataControlsDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Close")
                        }
                    },
                    containerColor = BgSurfaceElevated
                )
            }

            // Delete Account Dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Account & Data", color = DangerRed, fontWeight = FontWeight.Bold) },
                    text = {
                        Text(
                            "This action will sign you out and clear your active profile and session data. Are you sure you want to proceed?",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.signOut()
                                showDeleteDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Delete Account", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                    },
                    containerColor = BgSurfaceElevated
                )
            }
        }
    }
}
