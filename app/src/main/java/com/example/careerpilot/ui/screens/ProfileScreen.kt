package com.example.careerpilot.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.repository.BenchmarkCatalog
import com.example.careerpilot.ui.animation.*
import com.example.careerpilot.ui.components.*
import com.example.careerpilot.ui.theme.DesignSystem
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

    var authMode by remember { mutableIntStateOf(0) }
    var authEmail by remember { mutableStateOf("") }
    var authPassword by remember { mutableStateOf("") }
    var authName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var authEmailError by remember { mutableStateOf<String?>(null) }
    var authPasswordError by remember { mutableStateOf<String?>(null) }
    var authNameError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    // Sync status visual
    val syncColor = when (syncStatus) {
        com.example.careerpilot.data.firebase.CloudSyncStatus.IDLE -> DesignSystem.Colors.Success
        com.example.careerpilot.data.firebase.CloudSyncStatus.SYNCING -> DesignSystem.Colors.Primary
        com.example.careerpilot.data.firebase.CloudSyncStatus.ERROR -> DesignSystem.Colors.Danger
        com.example.careerpilot.data.firebase.CloudSyncStatus.OFFLINE -> DesignSystem.Colors.TextMuted
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = DesignSystem.Spacing.ContentHorizontal),
        contentPadding = PaddingValues(
            top = DesignSystem.Spacing.ContentTop,
            bottom = DesignSystem.Spacing.ContentBottom
        ),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Career Profile & Calibration",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DesignSystem.Colors.TextPrimary
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                Text(
                    text = "Set your background and career target objectives",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DesignSystem.Colors.TextSecondary
                )
            }
        }

        // Firebase Auth & Cloud Firestore Persistence Hero
        item {
            AnimatedGlowingGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firebase_auth_firestore_card"),
                backgroundColor = DesignSystem.Colors.BgSurfaceElevated
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(DesignSystem.Sizing.AvatarMd)
                                .clip(RoundedCornerShape(DesignSystem.Shape.RadiusMd))
                                .background(
                                    if (authUser.isAuthenticated) DesignSystem.Colors.SuccessContainer else DesignSystem.Colors.PrimaryContainer
                                )
                                .border(
                                    DesignSystem.Sizing.CardBorderWidth,
                                    if (authUser.isAuthenticated) DesignSystem.Colors.Success else DesignSystem.Colors.PrimaryLight,
                                    RoundedCornerShape(DesignSystem.Shape.RadiusMd)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (authUser.isAuthenticated) Icons.Default.CloudDone else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (authUser.isAuthenticated) DesignSystem.Colors.Success else DesignSystem.Colors.PrimaryLight,
                                modifier = Modifier.size(DesignSystem.Sizing.IconMd)
                            )
                        }

                        Column {
                            StatusBadge(
                                text = if (authUser.isAuthenticated) "ACCOUNT ACTIVE" else "ACCOUNT & SYNC",
                                statusType = if (authUser.isAuthenticated) "success" else "primary"
                            )
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                            Text(
                                text = if (authUser.isAuthenticated) (authUser.displayName ?: "Engineer") else "Sign In to Career Hub",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DesignSystem.Colors.TextPrimary
                            )
                            Text(
                                text = if (authUser.isAuthenticated) (authUser.email ?: "") else "Sync skills, applications and interview notes",
                                style = MaterialTheme.typography.bodySmall,
                                color = DesignSystem.Colors.TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (authUser.isAuthenticated) {
                        CareerButton(
                            text = "Sign Out",
                            onClick = { viewModel.signOut() },
                            variant = CareerButton.ButtonVariant.Outline,
                            modifier = Modifier.defaultMinSize(minHeight = DesignSystem.Sizing.ButtonHeightSm)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                if (!authUser.isAuthenticated) {
                    // Auth Mode Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(DesignSystem.Shape.RadiusMd))
                            .background(DesignSystem.Colors.BgCard)
                            .padding(DesignSystem.Spacing.Space1),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline)
                    ) {
                        CareerButton(
                            text = if (authMode == 0) "Sign In" else "Sign In",
                            onClick = {
                                authMode = 0
                                authEmailError = null
                                authPasswordError = null
                                authNameError = null
                            },
                            variant = if (authMode == 0) CareerButton.ButtonVariant.Primary else CareerButton.ButtonVariant.Secondary,
                            modifier = Modifier.weight(1f)
                        )
                        CareerButton(
                            text = "Create Account",
                            onClick = {
                                authMode = 1
                                authEmailError = null
                                authPasswordError = null
                                authNameError = null
                            },
                            variant = if (authMode == 1) CareerButton.ButtonVariant.Primary else CareerButton.ButtonVariant.Secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))

                    if (authMode == 1) {
                        CareerTextField(
                            value = authName,
                            onValueChange = {
                                authName = it
                                if (authNameError != null && it.isNotBlank()) authNameError = null
                            },
                            label = "Your Full Name",
                            placeholder = "e.g. Alex Morgan",
                            singleLine = true,
                            isError = authNameError != null,
                            errorMessage = authNameError,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth().testTag("auth_name_field")
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))
                    }

                    CareerTextField(
                        value = authEmail,
                        onValueChange = {
                            authEmail = it
                            if (authEmailError != null && it.isNotBlank()) authEmailError = null
                        },
                        label = "Email Address",
                        placeholder = "engineer@careerhub.io",
                        singleLine = true,
                        isError = authEmailError != null,
                        errorMessage = authEmailError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_field")
                    )

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))

                    CareerTextField(
                        value = authPassword,
                        onValueChange = {
                            authPassword = it
                            if (authPasswordError != null && it.isNotBlank()) authPasswordError = null
                        },
                        label = "Password",
                        placeholder = "••••••••",
                        singleLine = true,
                        isError = authPasswordError != null,
                        errorMessage = authPasswordError,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = Icons.Default.VisibilityOff,
                        onTrailingClick = { passwordVisible = !passwordVisible },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_field")
                    )

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Inline)
                    ) {
                        CareerButton(
                            text = if (authMode == 0) "Sign In" else "Create Free Account",
                            onClick = {
                                val emailClean = authEmail.trim()
                                val passClean = authPassword.trim()
                                val nameClean = authName.trim()

                                var hasError = false
                                if (authMode == 1 && nameClean.isBlank()) {
                                    authNameError = "Please enter your name"
                                    hasError = true
                                } else {
                                    authNameError = null
                                }

                                if (emailClean.isBlank()) {
                                    authEmailError = "Email is required"
                                    hasError = true
                                } else if (!emailClean.contains("@") || !emailClean.contains(".")) {
                                    authEmailError = "Please enter a valid email address"
                                    hasError = true
                                } else {
                                    authEmailError = null
                                }

                                if (passClean.isBlank()) {
                                    authPasswordError = "Password is required"
                                    hasError = true
                                } else if (passClean.length < 6) {
                                    authPasswordError = "Password must be at least 6 characters"
                                    hasError = true
                                } else {
                                    authPasswordError = null
                                }

                                if (!hasError) {
                                    focusManager.clearFocus()
                                    if (authMode == 0) {
                                        viewModel.signInWithEmail(emailClean, passClean)
                                    } else {
                                        viewModel.signUpWithEmail(nameClean, emailClean, passClean)
                                    }
                                }
                            },
                            variant = CareerButton.ButtonVariant.Primary,
                            loading = false,
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = DesignSystem.Sizing.TouchTargetMin)
                                .testTag("email_auth_submit_button")
                        )

                        CareerButton(
                            text = "Google Sign-In",
                            onClick = { viewModel.signInWithGoogle() },
                            variant = CareerButton.ButtonVariant.Outline,
                            icon = Icons.Default.AccountCircle,
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = DesignSystem.Sizing.TouchTargetMin)
                                .testTag("google_signin_button")
                        )
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
                                color = DesignSystem.Colors.PrimaryLight
                            )
                            Text(
                                text = syncStatus.syncStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = DesignSystem.Colors.TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        CareerButton(
                            text = if (syncStatus.isSyncing) "Syncing…" else "Sync Now",
                            onClick = { viewModel.triggerCloudSync() },
                            enabled = !syncStatus.isSyncing,
                            variant = CareerButton.ButtonVariant.Success,
                            icon = Icons.Default.Sync,
                            loading = syncStatus.isSyncing,
                            modifier = Modifier.testTag("firestore_sync_button")
                        )
                    }
                }
            }
        }

        // 1-Click Career Starter Presets
        item {
            CareerCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("career_starter_presets_card"),
                borderColor = DesignSystem.Colors.PrimaryLight.copy(alpha = 0.4f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)) {
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
                                color = DesignSystem.Colors.PrimaryLight
                            )
                            Text(
                                text = "Quickly calibrate profile, skills, roadmap and projects for a target discipline",
                                style = MaterialTheme.typography.bodySmall,
                                color = DesignSystem.Colors.TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                    val presets = listOf(
                        "Full Stack Engineer",
                        "Android Mobile Engineer",
                        "AI / Machine Learning Engineer",
                        "DevOps / Cloud Architect"
                    )

                    presets.forEach { presetRole ->
                        val isSelected = targetRole.equals(presetRole, ignoreCase = true)
                        CareerCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = if (isSelected) DesignSystem.Colors.PrimaryLight else DesignSystem.Colors.BorderSubtle,
                            backgroundColor = if (isSelected) DesignSystem.Colors.PrimaryContainer else DesignSystem.Colors.BgCard,
                            onClick = { viewModel.applyCareerStarterTemplate(presetRole) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(DesignSystem.Spacing.Space4),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = presetRole,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) DesignSystem.Colors.PrimaryLight else DesignSystem.Colors.TextPrimary
                                    )
                                    Text(
                                        text = if (isSelected) "Active Profile Trajectory" else "Tap to apply preset",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) DesignSystem.Colors.Success else DesignSystem.Colors.TextMuted
                                    )
                                }
                                Text(
                                    text = if (isSelected) "Active" else "Apply",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) DesignSystem.Colors.Success else DesignSystem.Colors.PrimaryLight
                                )
                            }
                        }
                    }
                }
            }
        }

        // 1. Personal Information Card
        item {
            CareerCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)) {
                    SectionHeader(title = "Personal Information", subtitle = "Profile identity and public summary")

                    CareerTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Full Name",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_fullname_input")
                    )

                    CareerTextField(
                        value = headline,
                        onValueChange = { headline = it },
                        label = "Professional Headline",
                        placeholder = "e.g. Senior Backend & Distributed Systems Engineer",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_headline_input")
                    )

                    CareerTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = "Executive Summary / Bio",
                        singleLine = false,
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth().testTag("profile_bio_input")
                    )
                }
            }
        }

        // 2. Education & Background Card
        item {
            CareerCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)) {
                    SectionHeader(title = "Education & Background", subtitle = "Credentials, industry tenure, and location")

                    CareerTextField(
                        value = education,
                        onValueChange = { education = it },
                        label = "Education / Highest Degree",
                        placeholder = "e.g. B.S. in Computer Science",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_education_input")
                    )

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)
                    ) {
                        CareerTextField(
                            value = expYears,
                            onValueChange = { expYears = it },
                            label = "Experience (Years)",
                            placeholder = "e.g. 4.5",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("profile_experience_input")
                        )
                        CareerTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = "Location",
                            placeholder = "e.g. Remote / US",
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("profile_location_input")
                        )
                    }
                }
            }
        }

        // 3. Career Target Preferences Card
        item {
            CareerCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)) {
                    SectionHeader(title = "Target Career Preferences", subtitle = "Target role, compensation, and company tier")

                    CareerTextField(
                        value = targetRole,
                        onValueChange = { targetRole = it },
                        label = "Target Role",
                        placeholder = "e.g. Staff SRE",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_targetrole_input")
                    )

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)
                    ) {
                        CareerTextField(
                            value = targetIndustry,
                            onValueChange = { targetIndustry = it },
                            label = "Target Industry",
                            placeholder = "e.g. FinTech",
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("profile_industry_input")
                        )
                        CareerTextField(
                            value = targetCompanyTier,
                            onValueChange = { targetCompanyTier = it },
                            label = "Company Tier",
                            placeholder = "e.g. Top Tech",
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("profile_tier_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Group))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Group)
                    ) {
                        CareerTextField(
                            value = targetSalary,
                            onValueChange = { targetSalary = it },
                            label = "Target Salary",
                            placeholder = "e.g. $180k",
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("profile_salary_input")
                        )
                    }
                }
            }
        }

        // Save button
        item {
            CareerButton(
                text = "Save Profile",
                onClick = {
                    viewModel.updateProfile(
                        fullName = fullName.trim(),
                        headline = headline.trim(),
                        bio = bio.trim(),
                        location = location.trim(),
                        education = education.trim(),
                        experienceYears = expYears.toIntOrNull() ?: 0,
                        targetRole = targetRole.trim(),
                        targetIndustry = targetIndustry.trim(),
                        targetSalary = targetSalary.trim(),
                        targetCompanyTier = targetCompanyTier.trim()
                    )
                },
                variant = CareerButton.ButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
