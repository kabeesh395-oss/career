package com.example.careerpilot.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.careerpilot.ui.theme.DesignSystem

// ── Metrics ───────────────────────────────────────────────────────

/**
 * Compact metric display card - simplified, clean
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
    CareerCard(
        modifier = modifier
            .testTag("metric_${label.lowercase().replace(' ', '_')}"),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = DesignSystem.Colors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        }
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = DesignSystem.Colors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
        Text(
            text = detail,
            style = MaterialTheme.typography.labelSmall,
            color = DesignSystem.Colors.TextMuted,
            maxLines = 1
        )
    }
}

// ── Badges ────────────────────────────────────────────────────────

/**
 * Semantic status badge with automatic color mapping.
 * Use CareerBadge directly for more control.
 */
@Composable
fun StatusBadge(
    text: String,
    statusType: String = "neutral",
    modifier: Modifier = Modifier
) {
    val variant = when (statusType.lowercase()) {
        "urgent", "danger", "high", "critical" -> CareerBadge.BadgeVariant.Danger
        "medium", "warning", "in_progress" -> CareerBadge.BadgeVariant.Warning
        "success", "completed", "verified", "low", "resolved" -> CareerBadge.BadgeVariant.Success
        "primary", "active" -> CareerBadge.BadgeVariant.Primary
        else -> CareerBadge.BadgeVariant.Neutral
    }
    CareerBadge(text = text, modifier = modifier, variant = variant)
}

/**
 * Custom color badge for non-semantic colors.
 */
@Composable
fun StatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(DesignSystem.Shape.RadiusXs)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.12f))
            .border(DesignSystem.Sizing.CardBorderWidth, color.copy(alpha = 0.35f), shape)
            .padding(
                horizontal = DesignSystem.Sizing.BadgePaddingHorizontal,
                vertical = DesignSystem.Sizing.BadgePaddingVertical
            ),
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

/**
 * Pulsing live indicator badge.
 */
@Composable
fun PulsingLiveBadge(
    text: String,
    color: Color = DesignSystem.Colors.Primary,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(DesignSystem.Shape.RadiusXs)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.12f))
            .border(DesignSystem.Sizing.CardBorderWidth, color.copy(alpha = 0.35f), shape)
            .padding(
                horizontal = DesignSystem.Sizing.BadgePaddingHorizontal,
                vertical = DesignSystem.Sizing.BadgePaddingVertical
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(color)
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

// ── Score Gauge ────────────────────────────────────────────────────

@Composable
fun CircularScoreGauge(
    score: Int,
    size: Dp = 96.dp,
    strokeWidth: Dp = 6.dp,
    label: String = "SCORE",
    primaryColor: Color = DesignSystem.Colors.Primary,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (score.coerceIn(0, 100)) / 100f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "scoreProgress"
    )

    val discColor = DesignSystem.Colors.BgSurface.copy(alpha = 0.5f)
    val trackColor = DesignSystem.Colors.BorderSubtle

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            // Ambient inner background disc
            drawCircle(
                color = discColor,
                radius = (size.toPx() / 2f) - (strokePx / 2f)
            )
            // Inactive subtle track ring
            drawCircle(
                color = trackColor,
                style = Stroke(width = strokePx)
            )
            // Active progress arc with clean primary accent
            if (animatedProgress > 0f) {
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$score%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DesignSystem.Colors.TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = DesignSystem.Colors.TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun BulletSeparator(
    modifier: Modifier = Modifier,
    size: Dp = 3.5.dp,
    color: Color = DesignSystem.Colors.TextMuted
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

// ── Section Header ────────────────────────────────────────────────

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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DesignSystem.Colors.TextPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = DesignSystem.Colors.TextSecondary
                )
            }
        }
        if (actionText != null && onActionClick != null) {
            val cleanText = actionText.replace("→", "").replace(">", "").replace("⚡", "").trim()
            TextButton(
                onClick = onActionClick,
                colors = ButtonDefaults.textButtonColors(contentColor = DesignSystem.Colors.PrimaryLight),
                contentPadding = PaddingValues(horizontal = DesignSystem.Spacing.Space2, vertical = DesignSystem.Spacing.Space1)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1)
                ) {
                    Text(
                        text = cleanText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(DesignSystem.Sizing.IconSm)
                    )
                }
            }
        }
    }
}

// ── Professional Empty State ──────────────────────────────────────

/**
 * Standard structured empty state:
 * 1. What is missing
 * 2. Why it matters
 * 3. What the user can do next
 */
@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = DesignSystem.Spacing.Space8, horizontal = DesignSystem.Spacing.Space3),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(DesignSystem.Sizing.AvatarLg)
                .clip(RoundedCornerShape(DesignSystem.Shape.RadiusMd))
                .background(DesignSystem.Colors.BgSurface)
                .border(DesignSystem.Sizing.CardBorderWidth, DesignSystem.Colors.BorderSubtle, RoundedCornerShape(DesignSystem.Shape.RadiusMd)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DesignSystem.Colors.PrimaryLight,
                modifier = Modifier.size(DesignSystem.Sizing.IconLg)
            )
        }
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DesignSystem.Colors.TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = DesignSystem.Colors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Space4)
        )
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space4))
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CareerButton(
                    text = actionLabel,
                    onClick = onActionClick,
                    variant = CareerButton.ButtonVariant.Primary
                )
                if (secondaryActionLabel != null && onSecondaryActionClick != null) {
                    CareerButton(
                        text = secondaryActionLabel,
                        onClick = onSecondaryActionClick,
                        variant = CareerButton.ButtonVariant.Outline
                    )
                }
            }
        }
    }
}

// ── Company Logo Badge (Backward-compatible wrapper) ──────────────

@Composable
fun CompanyLogoBadge(
    companyName: String,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = companyName.substring(0, minOf(2, companyName.length)).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = DesignSystem.Colors.TextPrimary
        )
    }
}

// ── GlassCard (Backward-compatible wrapper) ───────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = DesignSystem.Colors.BorderDefault,
    backgroundColor: Color = DesignSystem.Colors.BgCard,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(DesignSystem.Shape.RadiusLg)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor)
            .border(DesignSystem.Sizing.CardBorderWidth, borderColor, shape)
            .padding(DesignSystem.Spacing.Space4)
    ) {
        content()
    }
}

// ── AnimatedGlowingGlassCard (Backward-compatible wrapper) ────────

@Composable
fun AnimatedGlowingGlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = DesignSystem.Colors.BgCard,
    borderColor: Color = DesignSystem.Colors.BorderDefault,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(DesignSystem.Shape.RadiusLg)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor)
            .border(DesignSystem.Sizing.CardBorderWidth, borderColor, shape)
            .padding(DesignSystem.Spacing.Space4)
    ) {
        content()
    }
}
