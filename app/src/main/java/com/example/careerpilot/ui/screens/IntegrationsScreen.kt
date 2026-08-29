package com.example.careerpilot.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.careerpilot.data.model.IntegrationAccount
import com.example.careerpilot.data.remote.github.GitHubRepoItem
import com.example.careerpilot.ui.components.GlassCard
import com.example.careerpilot.ui.components.StatusBadge
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IntegrationsScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val integrations by viewModel.integrations.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val context = LocalContext.current

    val githubIntegration = integrations.find { it.provider == "github" }
    val linkedinIntegration = integrations.find { it.provider == "linkedin" }

    var githubInputUsername by remember(githubIntegration?.username) {
        mutableStateOf(githubIntegration?.username ?: "")
    }
    var linkedinInputUsername by remember(linkedinIntegration?.username) {
        mutableStateOf(linkedinIntegration?.username ?: "")
    }

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
                    text = "Live telemetry integration with GitHub API to verify code contributions and public repositories",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // ==========================================
        // REAL GITHUB INTEGRATION CARD
        // ==========================================
        item {
            val status = githubIntegration?.connectionStatus ?: "NOT_CONNECTED"
            val isConnected = githubIntegration?.isConnected == true && status == "CONNECTED"
            val isChecking = status == "CHECKING" || isAnalyzing
            val isNotFound = status == "NOT_FOUND" || status == "INVALID"
            val isRateLimited = status == "RATE_LIMITED"
            val isError = status == "ERROR"

            val cardBorderColor = when {
                isConnected -> SuccessGreen.copy(alpha = 0.5f)
                isChecking -> AccentCyan.copy(alpha = 0.5f)
                isNotFound || isError -> DangerRed.copy(alpha = 0.5f)
                isRateLimited -> AccentAmber.copy(alpha = 0.5f)
                else -> BorderSubtle
            }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("github_integration_card"),
                borderColor = cardBorderColor
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BgCard),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = "GitHub",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "GitHub Integration",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "api.github.com Telemetry",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Connection Status Badge
                        when {
                            isConnected -> StatusBadge(text = "CONNECTED", statusType = "success")
                            isChecking -> StatusBadge(text = "CHECKING...", statusType = "primary")
                            isNotFound -> StatusBadge(text = "NOT FOUND (404)", statusType = "danger")
                            isRateLimited -> StatusBadge(text = "RATE LIMITED", statusType = "warning")
                            isError -> StatusBadge(text = "ERROR", statusType = "danger")
                            else -> StatusBadge(text = "NOT CONNECTED", statusType = "neutral")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Checking / Loading State
                    if (isChecking) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = AccentCyan
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Verifying username with api.github.com...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AccentCyan
                            )
                        }
                    }

                    // Connected State: Profile, Avatar & Real Telemetry
                    if (isConnected && githubIntegration != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (githubIntegration.avatarUrl.isNotBlank()) {
                                AsyncImage(
                                    model = githubIntegration.avatarUrl,
                                    contentDescription = "GitHub Avatar",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, AccentCyan, CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = githubIntegration.displayName.ifBlank { githubIntegration.username },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "@${githubIntegration.username}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AccentCyan
                                )
                                if (githubIntegration.bio.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = githubIntegration.bio,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/${githubIntegration.username}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Non-fatal
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "View Profile on GitHub",
                                    tint = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Real Stats Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BgCard)
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            GitHubMetricItem(label = "Public Repos", value = "${githubIntegration.publicReposCount}")
                            GitHubMetricItem(label = "Followers", value = "${githubIntegration.followersCount}")
                            GitHubMetricItem(label = "Following", value = "${githubIntegration.followingCount}")
                            GitHubMetricItem(label = "Public Gists", value = "${githubIntegration.publicGistsCount}")
                        }

                        // Top Real Repositories Section
                        val repos = remember(githubIntegration.topRepositoriesJson) {
                            parseTopRepos(githubIntegration.topRepositoriesJson)
                        }

                        if (repos.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Verified Public Repositories",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                repos.forEach { repo ->
                                    GitHubRepoCard(
                                        repo = repo,
                                        onOpenUrl = {
                                            if (repo.url.isNotBlank()) {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(repo.url))
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    // Non-fatal
                                                }
                                            }
                                        },
                                        onImportToPortfolio = {
                                            viewModel.importGitHubRepoToPortfolio(repo)
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (githubIntegration.lastSyncedAt > 0L) {
                            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                            Text(
                                text = "Last Synced: ${sdf.format(Date(githubIntegration.lastSyncedAt))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Refresh & Disconnect Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.refreshGitHubData() },
                                colors = ButtonDefaults.buttonColors(containerColor = BgCard),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Refresh Telemetry", color = AccentCyan, fontSize = 13.sp)
                            }

                            Button(
                                onClick = { viewModel.disconnectGitHub() },
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LinkOff,
                                    contentDescription = null,
                                    tint = DangerRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Disconnect", color = DangerRed, fontSize = 13.sp)
                            }
                        }
                    }

                    // Not Connected / Error / Form Input State
                    if (!isConnected && !isChecking) {
                        if (isNotFound) {
                            Surface(
                                color = DangerRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                                    Text(
                                        text = githubIntegration?.errorMessage ?: "GitHub user not found. Please check spelling.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DangerRed
                                    )
                                }
                            }
                        }

                        if (isRateLimited) {
                            Surface(
                                color = AccentAmber.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))
                                    Text(
                                        text = githubIntegration?.errorMessage ?: "GitHub API rate limit reached. Retry in 1-2 minutes.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AccentAmber
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = githubInputUsername,
                            onValueChange = { githubInputUsername = it },
                            label = { Text("GitHub Username (e.g. torvalds, google)") },
                            placeholder = { Text("Enter public GitHub username") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = TextMuted)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("github_username_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.connectGitHub(githubInputUsername)
                            },
                            enabled = githubInputUsername.isNotBlank() && !isChecking,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("github_connect_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verify & Connect via GitHub API", fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // ==========================================
        // LINKEDIN INTEGRATION CARD
        // ==========================================
        item {
            val isConnected = linkedinIntegration?.isConnected == true
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (isConnected) SuccessGreen.copy(alpha = 0.4f) else BorderSubtle
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BgCard),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Work,
                                    contentDescription = "LinkedIn",
                                    tint = AccentPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "LinkedIn Profile",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Professional network sync",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        StatusBadge(
                            text = if (isConnected) "LINKED" else "NOT LINKED",
                            statusType = if (isConnected) "success" else "neutral"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = linkedinInputUsername,
                        onValueChange = { linkedinInputUsername = it },
                        label = { Text("LinkedIn Public Handle / Vanity URL") },
                        placeholder = { Text("e.g. alex-chen-dev") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = TextMuted)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.toggleIntegration("linkedin", linkedinInputUsername)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isConnected) BgCard else PrimaryBlue
                        ),
                        border = if (isConnected) androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle) else null,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.LinkOff else Icons.Default.Link,
                            contentDescription = null,
                            tint = if (isConnected) TextSecondary else TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isConnected) "Disconnect LinkedIn" else "Save & Link Profile",
                            color = if (isConnected) TextSecondary else TextPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GitHubMetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AccentCyan
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
private fun GitHubRepoCard(
    repo: GitHubRepoItem,
    onOpenUrl: () -> Unit,
    onImportToPortfolio: () -> Unit
) {
    Surface(
        color = BgCard,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = repo.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                if (repo.language.isNotBlank() && repo.language != "Code") {
                    StatusBadge(text = repo.language, statusType = "primary")
                }
            }

            if (repo.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = repo.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(14.dp))
                        Text(text = "${repo.stars}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Text(text = "${repo.forks}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onOpenUrl,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "View Repo",
                            tint = AccentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Button(
                        onClick = onImportToPortfolio,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add to Portfolio", color = AccentCyan, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

private fun parseTopRepos(json: String): List<GitHubRepoItem> {
    if (json.isBlank() || json == "[]") return emptyList()
    val list = mutableListOf<GitHubRepoItem>()
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                GitHubRepoItem(
                    name = obj.optString("name", "repo"),
                    description = obj.optString("description", ""),
                    url = obj.optString("url", ""),
                    stars = obj.optInt("stars", 0),
                    forks = obj.optInt("forks", 0),
                    language = obj.optString("language", "Code"),
                    updatedAt = obj.optString("updatedAt", "")
                )
            )
        }
    } catch (e: Exception) {
        // Return whatever parsed
    }
    return list
}
