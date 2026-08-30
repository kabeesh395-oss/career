package com.example.careerpilot.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.ui.theme.*

/**
 * CareerHub Animation Utilities
 *
 * Functional animations only. No decorative glow, particles, or ambient effects.
 */

/**
 * Subtle press-feedback modifier with spring physics.
 */
fun Modifier.bouncyClickable(
    pressedScale: Float = 0.98f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bouncyScale"
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
 * Shimmer loading sweep effect.
 */
fun Modifier.shimmerSweep(
    shimmerColors: List<Color> = listOf(
        Color.White.copy(alpha = 0.0f),
        Color.White.copy(alpha = 0.08f),
        Color.White.copy(alpha = 0.0f)
    ),
    durationMillis: Int = 1800
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
 * Animated number counter with easing.
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
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
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
 * Staggered entrance animation for list items.
 */
@Composable
fun StaggeredAnimatedItem(
    index: Int,
    modifier: Modifier = Modifier,
    delayPerIndex: Int = 60,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index * delayPerIndex).toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetY = { 40 }
        ),
        modifier = modifier
    ) {
        content()
    }
}
