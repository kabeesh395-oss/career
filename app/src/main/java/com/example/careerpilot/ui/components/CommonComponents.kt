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
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.theme.Dimens

// ── Cards ─────────────────────────────────────────────────────────

/**
 * Standard CareerHub card surface.
 * Use for all card-like groupings throughout the app.
 */
@Composable
fun CareerCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Dimens.RadiusMd),
    borderColor: Color = BorderSubtle,
    backgroundColor: Color = BgCard,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .clip(shape)
        .background(backgroundColor)
        .border(Dimens.CardBorderWidth, borderColor, shape)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
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
    shape: Shape = RoundedCornerShape(Dimens.RadiusMd),
    borderColor: Color = PrimaryBlue.copy(alpha = 0.35f),
    backgroundColor: Color = BgCardHover,
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

// Backward-compatible aliases
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Dimens.RadiusMd),
    borderColor: Color = BorderSubtle,
    backgroundColor: Color = BgCard,
    glowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) = CareerCard(modifier, shape, borderColor, backgroundColor, onClick, content)

@Composable
fun GradientGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Dimens.RadiusMd),
    startColor: Color = PrimaryBlue.copy(alpha = 0.08f),
    endColor: Color = BgCard,
    borderColor: Color = PrimaryBlue.copy(alpha = 0.3f),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) = CareerCard(modifier, shape, borderColor, BgCard, onClick, content)

@Composable
fun AnimatedGlowingGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Dimens.RadiusMd),
    backgroundColor: Color = BgCardHover,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) = CareerCardHighlight(modifier, shape, backgroundColor = backgroundColor, onClick = onClick, content = content)

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
            fontWeight = FontWeight.ExtraBold
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
            DangerRed.copy(alpha = 0.12f), DangerRedLight, DangerRed.copy(alpha = 0.3f)
        )
        "medium", "warning", "in_progress" -> Triple(
            WarningAmber.copy(alpha = 0.12f), WarningAmberLight, WarningAmber.copy(alpha = 0.3f)
        )
        "success", "completed", "verified", "low", "resolved" -> Triple(
            SuccessGreen.copy(alpha = 0.12f), SuccessGreenLight, SuccessGreen.copy(alpha = 0.3f)
        )
        "primary", "active" -> Triple(
            PrimaryBlue.copy(alpha = 0.12f), PrimaryBlueLighter, PrimaryBlue.copy(alpha = 0.3f)
        )
        "accent" -> Triple(
            AccentPurple.copy(alpha = 0.12f), AccentPurple, AccentPurple.copy(alpha = 0.3f)
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
            .border(1.dp, color.copy(alpha = 0.3f), shape)
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

    val shape = RoundedCornerShape(Dimens.BadgeRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), shape)
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
        animationSpec = tween(durationMillis = 800),
        label = "scoreProgress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            drawCircle(
                color = BorderSubtle,
                style = Stroke(width = strokePx)
            )
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
            TextButton(
                onClick = onActionClick,
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryBlueLighter)
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

// ── Empty State ───────────────────────────────────────────────────

/**
 * Standard empty state — NOT wrapped in a card.
 * Use when a feature has no data to display.
 */
@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.SpaceXxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.AvatarLg)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlueLighter,
                modifier = Modifier.size(Dimens.IconLg)
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
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Dimens.SpaceLg)
        )
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(Dimens.SpaceLg))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(Dimens.RadiusSm)
            ) {
                Text(text = actionLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
