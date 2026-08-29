package com.example.careerpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.data.repository.SalaryNegotiationEngine
import com.example.careerpilot.ui.animation.*
import com.example.careerpilot.ui.components.*
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.viewmodel.CareerViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SalaryNegotiatorScreen(
    viewModel: CareerViewModel,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var baseSalaryInput by remember { mutableStateOf("165000") }
    var equityGrantInput by remember { mutableStateOf("200000") }
    var signOnInput by remember { mutableStateOf("20000") }
    var bonusPercentInput by remember { mutableStateOf("15") }
    var selectedScenarioIndex by remember { mutableIntStateOf(0) }
    var showCopiedToast by remember { mutableStateOf(false) }
    var triggerCelebration by remember { mutableStateOf(false) }

    val baseSalary = baseSalaryInput.toDoubleOrNull() ?: 165000.0
    val equityGrant = equityGrantInput.toDoubleOrNull() ?: 200000.0
    val signOn = signOnInput.toDoubleOrNull() ?: 20000.0
    val bonusPercent = bonusPercentInput.toDoubleOrNull() ?: 15.0

    val breakdown = SalaryNegotiationEngine.calculateCompensation(
        baseSalary = baseSalary,
        equityGrant = equityGrant,
        signOn = signOn,
        bonusPercent = bonusPercent
    )

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    val scenarios = SalaryNegotiationEngine.SCENARIOS
    val activeScenario = scenarios[selectedScenarioIndex]

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Offer & Total Comp Negotiation Lab",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Model 4-year equity vesting, cash bonuses & counter-offer scenarios",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Total Comp Overview Hero with Animated Gradient Border
        item {
            AnimatedGlowingGlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = BgSurfaceElevated
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Year 1 Total Compensation (TC)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = currencyFormatter.format(breakdown.year1TotalComp),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SuccessGreen
                        )
                    }

                    PulsingLiveBadge(text = "TIER-1 TECH BAND", color = SuccessGreen)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CompSummaryItem("Base Salary", currencyFormatter.format(breakdown.baseSalary), TextPrimary)
                    CompSummaryItem("Annual RSU", currencyFormatter.format(breakdown.annualEquity), AccentCyan)
                    CompSummaryItem("Sign-On", currencyFormatter.format(breakdown.signOnBonus), WarningAmber)
                    CompSummaryItem("Annual Bonus", currencyFormatter.format(breakdown.annualBonusDollar), PrimaryBlueGlow)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "4-Year Total Value: ${currencyFormatter.format(breakdown.fourYearTotalComp)}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "Est. Monthly Pre-Tax: ${currencyFormatter.format(breakdown.estimatedMonthlyPreTax)}",
                        fontSize = 11.sp,
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Interactive Offer Parameters
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Interactive Offer Adjuster",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = baseSalaryInput,
                        onValueChange = { baseSalaryInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Base Salary ($)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = equityGrantInput,
                        onValueChange = { equityGrantInput = it.filter { c -> c.isDigit() } },
                        label = { Text("4-Yr RSU ($)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = signOnInput,
                        onValueChange = { signOnInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Sign-on Bonus ($)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = bonusPercentInput,
                        onValueChange = { bonusPercentInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Target Bonus (%)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // Scenario Selector Chips
        item {
            Text(
                text = "Counter-Offer Strategic Scenarios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(scenarios.indices.toList()) { idx ->
                    val isSelected = selectedScenarioIndex == idx
                    val scenario = scenarios[idx]
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedScenarioIndex = idx },
                        label = { Text(scenario.title, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = TextPrimary,
                            containerColor = BgCard,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        // Active Scenario Strategy & Script
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = PrimaryBlueGlow.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activeScenario.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                    StatusBadge(text = activeScenario.targetAdjustmentFormula, color = WarningAmber)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = activeScenario.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Strategic Leverage: ${activeScenario.strategicLeverage}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Recommendation: ${activeScenario.counterRecommendation}",
                    fontSize = 12.sp,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Verbal Phone Negotiation Script",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentPurple
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"${activeScenario.verbalScript}\"",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgSurface, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Formal Counter Email Template",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentPurple
                    )

                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(activeScenario.emailTemplate))
                            showCopiedToast = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("copy_counter_email_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Email", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = activeScenario.emailTemplate,
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgSurface, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                )

                if (showCopiedToast) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "✓ Counter-offer email copied to clipboard!",
                        fontSize = 11.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CompSummaryItem(label: String, value: String, color: Color) {
    Column {
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
