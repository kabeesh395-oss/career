package com.example.careerpilot.ui.components

import androidx.compose.animation.core.*
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
import com.example.careerpilot.ui.theme.Dimens

// ── Cards ─────────────────────────────────────────────────────────

/**
 * Standard CareerPilot card surface with crisp border and responsive interaction.
 */
@Composable
fun CareerCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    borderColor: Color = BorderSubtle,
    backgroundColor: Color = BgCard,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .clip(shape)
        .background(backgroundColor)
        .border(Dimens.CardBorderWidth, borderColor, shape)
        .then(if (onClick != null) Modifier.subtleClickable(onClick = onClick) else Modifier)
        .padding(Dimens.CardPadding)

    Column(
        modifier = cardModifier,
        content = content
    )
}

/**
 * Highlighted card for primary actions or key insights.
 */
@Composable
fun CareerCardHighlight(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    borderColor: Color = PrimaryBlue.copy(alpha = 0.5f),
    backgroundColor: Color = BgCard,
    accentGlow: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    CareerCard(
        modifier = modifier,
        shape = shape,
        borderColor = borderColor,
        backgroundColor = backgroundColor,
        onClick = onClick,
        content = content
    )
}

/**
 * Clean surface card (formerly GlassCard, now clean professional dark surface).
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    borderColor: Color = BorderSubtle,
    backgroundColor: Color = BgCard,
    glowColor: Color? = null,
    enableSheen: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    CareerCard(
        modifier = modifier,
        shape = shape,
        borderColor = borderColor,
        backgroundColor = backgroundColor,
        onClick = onClick,
        content = content
    )
}

@Composable
fun GradientGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    startColor: Color = BgCard,
    endColor: Color = BgCard,
    borderColor: Color = BorderSubtle,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    CareerCard(
        modifier = modifier,
        shape = shape,
        borderColor = borderColor,
        backgroundColor = BgCard,
        onClick = onClick,
        content = content
    )
}

@Composable
fun AnimatedGlowingGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = BgCard,
    accentColor: Color = PrimaryBlue,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    CareerCard(
        modifier = modifier,
        shape = shape,
        borderColor = BorderMedium,
        backgroundColor = backgroundColor,
        onClick = onClick,
        content = content
    )
}

@Composable
fun MotionGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    baseColor: Color = BgCard,
    borderColor: Color = BorderSubtle,
    glowColor: Color? = null,
    sheenDurationMillis: Int = 0,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    CareerCard(
        modifier = modifier,
        shape = shape,
        borderColor = borderColor,
        backgroundColor = baseColor,
        onClick = onClick,
        content = content
    )
}

// ── Metrics ───────────────────────────────────────────────────────

/**
 * Compact metric display card.
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
        modifier = modifier.testTag("metric_${label.lowercase().replace(' ', '_')}"),
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
        Spacer(modifier = Modifier.height(Dimens.SpaceSm))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(Dimens.SpaceXs))
        Text(
            text = detail,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            maxLines = 1
        )
    }
}

// ── Badges ────────────────────────────────────────────────────────

@Composable
fun StatusBadge(
    text: String,
    statusType: String = "neutral",
    modifier: Modifier = Modifier
) {
    val (bgColor, txtColor, bdColor) = when (statusType.lowercase()) {
        "urgent", "danger", "high", "critical" -> Triple(
            DangerRed.copy(alpha = 0.12f), DangerRedLight, DangerRed.copy(alpha = 0.35f)
        )
        "medium", "warning", "in_progress" -> Triple(
            WarningAmber.copy(alpha = 0.12f), WarningAmberLight, WarningAmber.copy(alpha = 0.35f)
        )
        "success", "completed", "verified", "low", "resolved" -> Triple(
            SuccessGreen.copy(alpha = 0.12f), SuccessGreenLight, SuccessGreen.copy(alpha = 0.35f)
        )
        "primary", "active" -> Triple(
            PrimaryBlue.copy(alpha = 0.12f), PrimaryBlueLighter, PrimaryBlue.copy(alpha = 0.35f)
        )
        else -> Triple(BgMuted, TextSecondary, BorderSubtle)
    }

    val shape = RoundedCornerShape(Dimens.BadgeRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, bdColor, shape)
            .padding(horizontal = Dimens.BadgePaddingHorizontal, vertical = Dimens.BadgePaddingVertical),
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
    val shape = RoundedCornerShape(Dimens.BadgeRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), shape)
            .padding(horizontal = Dimens.BadgePaddingHorizontal, vertical = Dimens.BadgePaddingVertical),
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
    color: Color = PrimaryBlue,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Dimens.BadgeRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), shape)
            .padding(horizontal = Dimens.BadgePaddingHorizontal, vertical = Dimens.BadgePaddingVertical),
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
    primaryColor: Color = PrimaryBlue,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (score.coerceIn(0, 100)) / 100f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "scoreProgress"
    )

    val discColor = BgSurface.copy(alpha = 0.5f)
    val trackColor = BorderSubtle

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
                color = TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
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
    color: Color = TextMuted
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
                color = TextPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(Dimens.SpaceXxs))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        if (actionText != null && onActionClick != null) {
            val cleanText = actionText.replace("→", "").replace(">", "").replace("⚡", "").trim()
            TextButton(
                onClick = onActionClick,
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryBlueLighter),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = cleanText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
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
            .padding(vertical = Dimens.SpaceXl, horizontal = Dimens.SpaceMd),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BgSurfaceElevated)
                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlueLighter,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.SpaceMd))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Dimens.SpaceXs))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Dimens.SpaceLg)
        )
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(Dimens.SpaceLg))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(Dimens.RadiusSm),
                    contentPadding = PaddingValues(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceSm),
                    modifier = Modifier.defaultMinSize(minHeight = Dimens.ButtonHeight)
                ) {
                    Text(text = actionLabel, fontWeight = FontWeight.SemiBold)
                }
                if (secondaryActionLabel != null && onSecondaryActionClick != null) {
                    OutlinedButton(
                        onClick = onSecondaryActionClick,
                        shape = RoundedCornerShape(Dimens.RadiusSm),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium),
                        contentPadding = PaddingValues(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceSm),
                        modifier = Modifier.defaultMinSize(minHeight = Dimens.ButtonHeight)
                    ) {
                        Text(text = secondaryActionLabel, fontWeight = FontWeight.Normal)
                    }
                }
            }
        }
    }
}
