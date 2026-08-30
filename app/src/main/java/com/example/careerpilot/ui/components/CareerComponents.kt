package com.example.careerpilot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.careerpilot.ui.theme.*
import com.example.careerpilot.ui.theme.Dimens

// ── List Items ────────────────────────────────────────────────────

/**
 * Standard list row with icon, title, subtitle, and optional trailing content.
 */
@Composable
fun CareerListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = PrimaryBlueLighter,
    iconBackgroundColor: Color = PrimaryBlue.copy(alpha = 0.12f),
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = Dimens.SpaceMd, horizontal = Dimens.SpaceLg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(Dimens.AvatarMd)
                    .clip(RoundedCornerShape(Dimens.RadiusSm))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(Dimens.IconMd)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(Dimens.SpaceXxs))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        trailing?.invoke()
    }
}

// ── Chips ─────────────────────────────────────────────────────────

/**
 * Standard filter chip for tab-like selection rows.
 */
@Composable
fun CareerChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Dimens.RadiusSm)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (selected) PrimaryBlue.copy(alpha = 0.15f) else BgCard
            )
            .border(
                Dimens.CardBorderWidth,
                if (selected) PrimaryBlue.copy(alpha = 0.4f) else BorderSubtle,
                shape
            )
            .clickable { onClick() }
            .padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceSm),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) TextPrimary else TextSecondary
        )
    }
}

// ── Progress ──────────────────────────────────────────────────────

/**
 * Horizontal progress bar with consistent styling.
 */
@Composable
fun CareerProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = PrimaryBlue,
    trackColor: Color = BgSurface,
    height: androidx.compose.ui.unit.Dp = 8.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f).coerceAtLeast(0.02f))
                .clip(RoundedCornerShape(height / 2))
                .background(color)
        )
    }
}

// ── Loading State ─────────────────────────────────────────────────

/**
 * Centered loading indicator with optional label.
 */
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.SpaceXxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = PrimaryBlue,
            strokeWidth = 3.dp,
            modifier = Modifier.size(Dimens.AvatarMd)
        )
        if (message != null) {
            Spacer(modifier = Modifier.height(Dimens.SpaceMd))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

// ── Error State ───────────────────────────────────────────────────

/**
 * Error display with retry action.
 */
@Composable
fun ErrorState(
    message: String = "Something went wrong.",
    onRetry: (() -> Unit)? = null,
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
                .background(DangerRed.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = DangerRedLight,
                modifier = Modifier.size(Dimens.IconLg)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.SpaceMd))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(Dimens.SpaceLg))
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(Dimens.RadiusSm),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlueLighter)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.IconSm)
                )
                Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                Text("Retry", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
