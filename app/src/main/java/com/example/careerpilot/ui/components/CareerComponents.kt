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
import com.example.careerpilot.ui.theme.DesignSystem

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
    iconTint: Color = DesignSystem.Colors.PrimaryLight,
    iconBackgroundColor: Color = DesignSystem.Colors.PrimaryContainer,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = DesignSystem.Spacing.Space3, horizontal = DesignSystem.Spacing.Space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space3)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(DesignSystem.Sizing.AvatarMd)
                    .clip(RoundedCornerShape(DesignSystem.Shape.RadiusSm))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(DesignSystem.Sizing.IconMd)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = DesignSystem.Colors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = DesignSystem.Colors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        trailing?.invoke()
    }
}

// ── Chips ──────────────────────────────────────────────────────────

/**
 * Standard filter chip for tab-like selection rows with clean feedback.
 */
@Composable
fun CareerChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(DesignSystem.Shape.RadiusSm)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (selected) DesignSystem.Colors.PrimaryContainer else DesignSystem.Colors.BgCard
            )
            .border(
                DesignSystem.Sizing.CardBorderWidth,
                if (selected) DesignSystem.Colors.Primary else DesignSystem.Colors.BorderDefault,
                shape
            )
            .clickable { onClick() }
            .padding(horizontal = DesignSystem.Spacing.Space4, vertical = DesignSystem.Spacing.Space2),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) DesignSystem.Colors.TextPrimary else DesignSystem.Colors.TextSecondary
        )
    }
}

// ── Progress ───────────────────────────────────────────────────────

/**
 * Horizontal progress bar with clean single accent.
 */
@Composable
fun CareerProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = DesignSystem.Colors.Primary,
    trackColor: Color = DesignSystem.Colors.BgSurface,
    height: androidx.compose.ui.unit.Dp = 6.dp
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
                .fillMaxWidth(progress.coerceIn(0f, 1f).coerceAtLeast(0.01f))
                .clip(RoundedCornerShape(height / 2))
                .background(color)
        )
    }
}

// ── Loading State ──────────────────────────────────────────────────

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
            .padding(vertical = DesignSystem.Spacing.Space12),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = DesignSystem.Colors.Primary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(DesignSystem.Sizing.AvatarMd)
        )
        if (message != null) {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = DesignSystem.Colors.TextSecondary
            )
        }
    }
}

// ── Empty State ────────────────────────────────────────────────────

/**
 * Empty state with illustration, message, and optional action.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = DesignSystem.Spacing.Space12, horizontal = DesignSystem.Spacing.Space4),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(DesignSystem.Sizing.AvatarXl)
                .clip(CircleShape)
                .background(DesignSystem.Colors.PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DesignSystem.Colors.Primary,
                modifier = Modifier.size(DesignSystem.Sizing.IconXl)
            )
        }
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space4))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = DesignSystem.Colors.TextPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space2))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = DesignSystem.Colors.TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 3
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space4))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(DesignSystem.Shape.Button),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DesignSystem.Colors.Primary,
                    contentColor = DesignSystem.Colors.OnPrimary
                ),
                contentPadding = PaddingValues(
                    horizontal = DesignSystem.Spacing.Space6,
                    vertical = DesignSystem.Spacing.Space2
                )
            ) {
                Text(text = actionText, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ── Error State ────────────────────────────────────────────────────

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
            .padding(vertical = DesignSystem.Spacing.Space12),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(DesignSystem.Sizing.AvatarLg)
                .clip(CircleShape)
                .background(DesignSystem.Colors.DangerContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = DesignSystem.Colors.Danger,
                modifier = Modifier.size(DesignSystem.Sizing.IconLg)
            )
        }
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space3))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = DesignSystem.Colors.TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space4))
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(DesignSystem.Shape.Button),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DesignSystem.Colors.Primary
                ),
                contentPadding = PaddingValues(
                    horizontal = DesignSystem.Spacing.Space6,
                    vertical = DesignSystem.Spacing.Space2
                ),
                modifier = Modifier.defaultMinSize(minHeight = DesignSystem.Sizing.ButtonHeight)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(DesignSystem.Sizing.IconSm)
                    )
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.Space2))
                    Text("Retry", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Section Header ─────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = DesignSystem.Spacing.ContentHorizontal,
                vertical = DesignSystem.Spacing.Space2
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = DesignSystem.Colors.TextMuted,
            letterSpacing = 0.5.sp
        )
        action?.invoke()
    }
}

// ── Card ───────────────────────────────────────────────────────────

@Composable
fun CareerCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(DesignSystem.Shape.RadiusLg)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DesignSystem.Colors.BgCard)
            .border(DesignSystem.Sizing.CardBorderWidth, DesignSystem.Colors.BorderDefault, shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(DesignSystem.Spacing.Space4)
    ) {
        content()
    }
}

// ── Card Highlight (for featured/primary content) ──────────────────

@Composable
fun CareerCardHighlight(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(DesignSystem.Shape.RadiusLg)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DesignSystem.Colors.PrimaryContainer)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(DesignSystem.Spacing.Space4)
    ) {
        content()
    }
}

// ── Badge ──────────────────────────────────────────────────────────

@Composable
fun CareerBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.Neutral
) {
    val (background, textColor) = when (variant) {
        BadgeVariant.Primary -> DesignSystem.Colors.PrimaryContainer to DesignSystem.Colors.Primary
        BadgeVariant.Success -> DesignSystem.Colors.SuccessContainer to DesignSystem.Colors.Success
        BadgeVariant.Warning -> DesignSystem.Colors.WarningContainer to DesignSystem.Colors.Warning
        BadgeVariant.Danger -> DesignSystem.Colors.DangerContainer to DesignSystem.Colors.Danger
        BadgeVariant.Neutral -> DesignSystem.Colors.BgMuted to DesignSystem.Colors.TextSecondary
    }

    Box(
        modifier = modifier
            .background(background)
            .clip(RoundedCornerShape(DesignSystem.Sizing.BadgeRadius))
            .padding(
                horizontal = DesignSystem.Sizing.BadgePaddingHorizontal,
                vertical = DesignSystem.Sizing.BadgePaddingVertical
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

enum class BadgeVariant {
    Primary, Success, Warning, Danger, Neutral
}

// ── Divider ────────────────────────────────────────────────────────

@Composable
fun CareerDivider(
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Divider(
        modifier = modifier.fillMaxWidth(),
        color = DesignSystem.Colors.BorderSubtle,
        thickness = DesignSystem.Sizing.DividerThickness
    )
}

// ── Button Variants ────────────────────────────────────────────────

@Composable
fun CareerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    icon: ImageVector? = null,
    loading: Boolean = false
) {
    val (containerColor, contentColor, borderColor) = when (variant) {
        ButtonVariant.Primary ->
            DesignSystem.Colors.Primary to DesignSystem.Colors.OnPrimary to null
        ButtonVariant.Secondary ->
            DesignSystem.Colors.BgCard to DesignSystem.Colors.TextPrimary to DesignSystem.Colors.BorderDefault
        ButtonVariant.Outline ->
            Color.Transparent to DesignSystem.Colors.Primary to DesignSystem.Colors.Primary
        ButtonVariant.Ghost ->
            Color.Transparent to DesignSystem.Colors.TextPrimary to null
        ButtonVariant.Danger ->
            DesignSystem.Colors.Danger to DesignSystem.Colors.OnPrimary to null
    }

    val buttonColors = if (variant == ButtonVariant.Outline) {
        ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            border = androidx.compose.foundation.BorderStroke(DesignSystem.Sizing.CardBorderWidth, borderColor!!)
        )
    } else if (variant == ButtonVariant.Ghost) {
        ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    } else if (variant == ButtonVariant.Secondary) {
        ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = DesignSystem.Sizing.ButtonHeight),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(DesignSystem.Shape.Button),
        colors = buttonColors,
        contentPadding = PaddingValues(
            horizontal = DesignSystem.Spacing.Space5,
            vertical = DesignSystem.Spacing.Space2
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(
                horizontal = if (icon != null) DesignSystem.Spacing.Space2 else DesignSystem.Spacing.Space0
            )
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = contentColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(DesignSystem.Sizing.IconSm)
                )
            } else {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(DesignSystem.Sizing.IconSm)
                    )
                }
                if (icon != null) Spacer(modifier = Modifier.width(DesignSystem.Spacing.Space2))
                Text(
                    text = if (loading) "Loading..." else text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

enum class ButtonVariant {
    Primary, Secondary, Outline, Ghost, Danger
}

// ── Input Field ────────────────────────────────────────────────────

@Composable
fun CareerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: androidx.compose.ui.text.input.KeyboardOptions = androidx.compose.ui.text.input.KeyboardOptions.Default,
    keyboardActions: androidx.compose.ui.text.input.KeyboardActions = androidx.compose.ui.text.input.KeyboardActions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
) {
    var interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource()
    val focused = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
        containerColor = DesignSystem.Colors.BgSurface,
        focusedContainerColor = DesignSystem.Colors.BgSurface,
        textColor = DesignSystem.Colors.TextPrimary,
        placeholderColor = DesignSystem.Colors.TextMuted,
        labelColor = DesignSystem.Colors.TextSecondary,
        focusedLabelColor = DesignSystem.Colors.Primary,
        leadingIconColor = DesignSystem.Colors.TextMuted,
        focusedLeadingIconColor = DesignSystem.Colors.Primary,
        trailingIconColor = DesignSystem.Colors.TextMuted,
        focusedTrailingIconColor = DesignSystem.Colors.Primary,
        errorContainerColor = DesignSystem.Colors.DangerContainer,
        errorTextColor = DesignSystem.Colors.Danger,
        focusedErrorTextColor = DesignSystem.Colors.Danger,
        disabledContainerColor = DesignSystem.Colors.BgMuted,
        disabledTextColor = DesignSystem.Colors.TextMuted
    )

    val shape = RoundedCornerShape(DesignSystem.Shape.RadiusMd)

    androidx.compose.material3.TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(DesignSystem.Sizing.InputHeight),
        label = { label?.let { Text(text = it, style = MaterialTheme.typography.bodySmall, color = DesignSystem.Colors.TextSecondary) } },
        placeholder = { Text(text = placeholder, style = MaterialTheme.typography.bodyMedium, color = DesignSystem.Colors.TextMuted) },
        leadingIcon = { leadingIcon?.let { Icon(imageVector = it, contentDescription = null, tint = DesignSystem.Colors.TextMuted) } },
        trailingIcon = { trailingIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = DesignSystem.Colors.TextMuted,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onTrailingClick?.invoke() }
                    .padding(DesignSystem.Spacing.Space3)
            )
        } },
        isError = isError,
        supportingText = { if (isError && errorMessage != null) Text(text = errorMessage, style = MaterialTheme.typography.bodySmall, color = DesignSystem.Colors.Danger) },
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        colors = colors,
        shape = shape
    )
}

// ── Portfolio Project Card ────────────────────────────────────────

@Composable
fun PortfolioProjectCard(
    project: com.example.careerpilot.data.model.PortfolioProject,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(DesignSystem.Shape.RadiusLg)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DesignSystem.Colors.BgCard)
            .border(DesignSystem.Sizing.CardBorderWidth, DesignSystem.Colors.BorderDefault, shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.Space4)
                .verticalArrangement(Arrangement.spacedBy(DesignSystem.Spacing.Space2))
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DesignSystem.Colors.TextPrimary,
                    maxLines = 2
                )
                if (project.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
                    Text(
                        text = project.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = DesignSystem.Colors.TextSecondary,
                        maxLines = 2
                    )
                }
            }
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Space1)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete project",
                        tint = DesignSystem.Colors.Danger,
                        modifier = Modifier.size(DesignSystem.Sizing.IconMd)
                    )
                }
            }
        }

        if (project.repositoryUrl.isNotBlank() || project.technologies.isNotBlank()) {
            CareerDivider(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Space1))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = DesignSystem.Spacing.Space3),
                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (project.repositoryUrl.isNotBlank()) {
                    TextButton(
                        onClick = { /* open repo */ },
                        colors = ButtonDefaults.textButtonColors(contentColor = DesignSystem.Colors.Primary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Space1)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(DesignSystem.Sizing.IconSm)
                            )
                            Text(
                                text = "View Repository",
                                style = MaterialTheme.typography.labelSmall,
                                color = DesignSystem.Colors.Primary
                            )
                        }
                    }
                }
                if (project.technologies.isNotBlank()) {
                    CareerBadge(
                        text = project.technologies,
                        variant = CareerBadge.BadgeVariant.Neutral
                    )
                }
            }
        }
    }
}
