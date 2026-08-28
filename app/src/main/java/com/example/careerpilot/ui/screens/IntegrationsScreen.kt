package com.example.careerpilot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.model.IntegrationAccount
import com.example.careerpilot.ui.components.GlassCard
import com.example.careerpilot.ui.components.StatusBadge
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IntegrationsScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val integrations by viewModel.integrations.collectAsState()

    var githubUser by remember { mutableStateOf("alexchen-dev") }
    var linkedinUser by remember { mutableStateOf("alex-chen-tech") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text(
                    text = "External Integrations",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sync code repositories and professional profiles for continuous telemetry",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // GitHub Card
        item {
            val gh = integrations.find { it.provider == "github" }
            val isConnected = gh?.isConnected ?: true
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (isConnected) SuccessGreen.copy(alpha = 0.4f) else BorderSubtle
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GitHub Integration",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Sync commit frequency, public repositories, and language stats",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    StatusBadge(text = if (isConnected) "CONNECTED" else "DISCONNECTED", statusType = if (isConnected) "success" else "neutral")
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = githubUser,
                    onValueChange = { githubUser = it },
                    label = { Text("GitHub Username / Organization") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (gh?.details?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Telemetry: ${gh.details}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentCyan
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { viewModel.toggleIntegration("github", githubUser) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) BgMuted else PrimaryBlue
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isConnected) "Sync GitHub Repositories" else "Connect GitHub Account")
                }
            }
        }

        // LinkedIn Card
        item {
            val li = integrations.find { it.provider == "linkedin" }
            val isConnected = li?.isConnected ?: true
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (isConnected) SuccessGreen.copy(alpha = 0.4f) else BorderSubtle
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LinkedIn Professional Profile",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Sync endorsements, role trajectory, and recruiter keyword visibility",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    StatusBadge(text = if (isConnected) "CONNECTED" else "DISCONNECTED", statusType = if (isConnected) "success" else "neutral")
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = linkedinUser,
                    onValueChange = { linkedinUser = it },
                    label = { Text("LinkedIn Handle / Vanity URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (li?.details?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Telemetry: ${li.details}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentPurple
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { viewModel.toggleIntegration("linkedin", linkedinUser) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) BgMuted else PrimaryBlue
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isConnected) "Sync LinkedIn Profile" else "Connect LinkedIn Profile")
                }
            }
        }
    }
}
