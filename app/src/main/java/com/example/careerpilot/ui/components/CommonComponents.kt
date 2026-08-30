package com.example.careerpilot.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.ui.animation.*
import com.example.careerpilot.ui.theme.*

/**
 * Standard Native Slate Card
 * Clean, consistent enterprise surface with crisp hairline border
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    borderColor: Color = BorderSubtle,
    backgroundColor: Color = BgCard,
    glowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .then(
            if (onClick != null) Modifier.bouncyClickable { onClick() } else Modifier
        )
        .clip(shape)
        .background(backgroundColor)
        .border(1.dp, borderColor, shape)
        .padding(16.dp)

    Column(
        modifier = cardModifier,
        content = content
    )
}

/**
 * Surface Card with Focused Subtle Gradient Accent (Hero/Callout cards)
 */
@Composable
fun GradientGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    startColor: Color = PrimaryBlue.copy(alpha = 0.08f),
    endColor: Color = BgCard,
    borderColor: Color = PrimaryBlue.copy(alpha = 0.3f),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .then(
            if (onClick != null) Modifier.bouncyClickable { onClick() } else Modifier
        )
        .clip(shape)
        .background(
            Brush.verticalGradient(listOf(startColor, endColor))
        )
        .border(1.dp, borderColor, shape)
        .padding(16.dp)

    Column(
        modifier = cardModifier,
        content = content
    )
}

/**
 * Focused Highlight Card for Key Insights / Recommended Actions
 */
@Composable
fun AnimatedGlowingGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    backgroundColor: Color = BgCardHover,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .then(
                if (onClick != null) Modifier.bouncyClickable { onClick() } else Modifier
            )
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, PrimaryBlue.copy(alpha = 0.35f), shape)
            .padding(16.dp),
        content = content
    )
}

/**
 * Native Standard Metric Stat Display
 */
@Composable
fun MetricCard(
    label: String,
    value: String,
    detail: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .testTag("metric_${label.lowercase().replace(' ', '_')}")
            .then(
                if (onClick != null) Modifier.bouncyClickable { onClick() } else Modifier
            )
            .clip(shape)
            .background(BgCard)
            .border(1.dp, BorderSubtle, shape)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = detail,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            maxLines = 1
        )
    }
}

/**
 * Refined Pill Status Badge
 */
@Composable
fun StatusBadge(
    text: String,
    statusType: String = "neutral",
    modifier: Modifier = Modifier
) {
    val (bgColor, txtColor, borderColor) = when (statusType.lowercase()) {
        "urgent", "danger", "high", "critical" -> Triple(DangerRed.copy(alpha = 0.12f), DangerRedGlow, DangerRed.copy(alpha = 0.3f))
        "medium", "warning", "in_progress" -> Triple(WarningAmber.copy(alpha = 0.12f), WarningAmberGlow, WarningAmber.copy(alpha = 0.3f))
        "success", "completed", "verified", "low", "resolved" -> Triple(SuccessGreen.copy(alpha = 0.12f), SuccessGreenGlow, SuccessGreen.copy(alpha = 0.3f))
        "primary", "active" -> Triple(PrimaryBlue.copy(alpha = 0.12f), PrimaryBlueGlow, PrimaryBlue.copy(alpha = 0.3f))
        "accent" -> Triple(AccentPurple.copy(alpha = 0.12f), AccentPurpleGlow, AccentPurple.copy(alpha = 0.3f))
        else -> Triple(BgMuted, TextSecondary, BorderSubtle)
    }

    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = txtColor,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun StatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), shape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun PulsingLiveBadge(
    text: String,
    color: Color = AccentCyan,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseBadge")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), shape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha))
            )
            Text(
                text = text.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun CircularScoreGauge(
    score: Int,
    size: Dp = 96.dp,
    strokeWidth: Dp = 6.dp,
    label: String = "SCORE",
    primaryColor: Color = PrimaryBlue,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (score.coerceIn(0, 100)) / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "scoreProgress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            // Track
            drawCircle(
                color = Color(0xFF1E293B),
                style = Stroke(width = strokePx)
            )
            // Progress arc
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$score%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryBlueGlow)
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = BgCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.15f))
                    .border(1.dp, PrimaryBlue.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlueGlow,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = actionLabel, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
