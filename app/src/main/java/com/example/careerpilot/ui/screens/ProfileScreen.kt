package com.example.careerpilot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.careerpilot.data.repository.BenchmarkCatalog
import com.example.careerpilot.ui.components.GlassCard
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel

@Composable
fun ProfileScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()

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
