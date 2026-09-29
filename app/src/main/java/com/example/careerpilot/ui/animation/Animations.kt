package com.example.careerpilot.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.ui.theme.*

/**
 * CareerPilot Animation & Transition Utilities
 *
 * Clean, lightweight, and responsive interaction tokens.
 * Unnecessary continuous micro-animations and heavy canvas rendering have been eliminated.
 */

/**
 * Tactile subtle press-feedback modifier without excessive bouncy spring physics.
 */
fun Modifier.subtleClickable(
    pressedScale: Float = 0.985f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = 80, easing = LinearOutSlowInEasing),
        label = "subtleScale"
    )
    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

/**
 * Backward-compatible alias for click interactions.
 */
fun Modifier.bouncyClickable(
    pressedScale: Float = 0.985f,
    onClick: () -> Unit
): Modifier = subtleClickable(pressedScale = pressedScale, onClick = onClick)

/**
 * Shimmer loading sweep effect for skeleton loading states.
 */
fun Modifier.shimmerSweep(
    shimmerColors: List<Color> = listOf(
        Color.White.copy(alpha = 0.0f),
        Color.White.copy(alpha = 0.05f),
        Color.White.copy(alpha = 0.0f)
    ),
    durationMillis: Int = 1400
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    this.drawBehind {
        val brush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = shimmerColors,
            start = androidx.compose.ui.geometry.Offset(translateAnimation - 500f, translateAnimation - 500f),
            end = androidx.compose.ui.geometry.Offset(translateAnimation, translateAnimation)
        )
        drawRect(brush = brush)
    }
}

/**
 * Animated number counter with smooth easing.
 */
@Composable
fun AnimatedStatCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    textColor: Color = TextPrimary,
    fontSize: Dp = 24.dp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    val animatedCount by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "statCounter"
    )
    Text(
        text = "$prefix$animatedCount$suffix",
        color = textColor,
        fontSize = fontSize.value.sp,
        fontWeight = fontWeight,
        modifier = modifier
    )
}

/**
 * Staggered entrance transition for list items.
 */
@Composable
fun StaggeredAnimatedItem(
    index: Int,
    modifier: Modifier = Modifier,
    delayPerIndex: Int = 30,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index * delayPerIndex).toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
            animationSpec = tween(200, easing = FastOutSlowInEasing),
            initialOffsetY = { 20 }
        ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Subtle pulse scale modifier for active indicators.
 */
fun Modifier.pulseAnimation(
    minScale: Float = 0.97f,
    maxScale: Float = 1.03f,
    durationMillis: Int = 1200
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    this.scale(scale)
}

// ── Clean Surface Styling (Replacing decorative glassmorphism) ────────

/**
 * Clean production-grade surface modifier.
 * Provides moderate corner radius, crisp border, and responsive touch feedback.
 */
fun Modifier.motionGlassSurface(
    shape: Shape = RoundedCornerShape(12.dp),
    baseColor: Color = DarkBgCard,
    borderColor: Color = DarkBorderSubtle,
    sheenColor: Color = Color.Transparent,
    accentGlow: Color? = null,
    enableSheen: Boolean = false,
    sheenDurationMillis: Int = 0,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.985f else 1f,
        animationSpec = tween(durationMillis = 80, easing = LinearOutSlowInEasing),
        label = "surfacePressScale"
    )
    this
        .scale(scale)
        .clip(shape)
        .background(baseColor)
        .border(1.dp, borderColor, shape)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else Modifier
        )
}

/**
 * Backward-compatible helper for surface gleam (now clean border).
 */
fun Modifier.motionGlassBorderGleam(
    shape: Shape = RoundedCornerShape(12.dp),
    accentColor: Color = PrimaryBlue
): Modifier = this

/**
 * Clean neutral app backdrop without heavy canvas shaders or continuous animations.
 * Extremely high-performance, battery-friendly, and responsive.
 */
@Composable
fun MotionGlassAuroraBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
    ) {
        content()
    }
}
