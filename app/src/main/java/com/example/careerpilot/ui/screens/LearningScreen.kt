package com.example.careerpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.model.LearningResource
import com.example.careerpilot.ui.components.GlassCard
import com.example.careerpilot.ui.components.SectionHeader
import com.example.careerpilot.ui.components.StatusBadge
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel

@Composable
fun LearningScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val resources by viewModel.learningResources.collectAsState()

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
                    text = "Curated Learning Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "High-impact resources mapped to target competencies",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        item {
            val completedCount = resources.count { it.isCompleted }
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Learning Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "$completedCount of ${resources.size} modules completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    StatusBadge(
                        text = "${if (resources.isNotEmpty()) (completedCount * 100 / resources.size) else 0}%",
                        statusType = "primary"
                    )
                }
            }
        }

        items(resources) { res ->
            LearningResourceCard(
                resource = res,
                onToggle = { viewModel.toggleLearningCompleted(res) }
            )
        }
    }
}

@Composable
private fun LearningResourceCard(
    resource: LearningResource,
    onToggle: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        borderColor = if (resource.isCompleted) SuccessGreen.copy(alpha = 0.4f) else BorderSubtle,
        backgroundColor = if (resource.isCompleted) BgCard.copy(alpha = 0.5f) else BgCard
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = if (resource.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (resource.isCompleted) "Completed" else "Incomplete",
                    tint = if (resource.isCompleted) SuccessGreen else TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = resource.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (resource.isCompleted) TextSecondary else TextPrimary
                    )
                    StatusBadge(text = "${resource.estimatedMinutes}m", statusType = "neutral")
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Provider: ${resource.provider}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentCyan
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(text = resource.category, statusType = "primary")
                    StatusBadge(text = resource.difficulty, statusType = "neutral")
                }
            }
        }
    }
}
