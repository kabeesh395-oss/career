package com.example.careerpilot.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-end animations & micro-interactions inspired by modern UI animation libraries (animmasterlib).
 */

/**
 * 1. Bouncy Clickable Modifier with Spring Physics
 */
fun Modifier.bouncyClickable(
    pressedScale: Float = 0.95f,
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
 * 2. Animated Gradient Border Sweep
 */
fun Modifier.animatedGradientBorder(
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.5.dp,
    colors: List<Color> = listOf(
        PrimaryBlueGlow,
        AccentCyan,
        AccentPurple,
        WarningAmber,
        PrimaryBlueGlow
    ),
    durationMillis: Int = 4000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "borderTransition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "borderAngle"
    )

    this
        .clip(shape)
        .drawBehind {
            rotate(angle) {
                drawCircle(
                    brush = Brush.sweepGradient(colors),
                    radius = size.maxDimension
                )
            }
        }
        .padding(borderWidth)
        .clip(shape)
}

/**
 * 3. Shimmer Sweep Effect for Loading / Highlighting
 */
fun Modifier.shimmerSweep(
    shimmerColors: List<Color> = listOf(
        Color.White.copy(alpha = 0.0f),
        Color.White.copy(alpha = 0.18f),
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
        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnimation - 500f, translateAnimation - 500f),
            end = Offset(translateAnimation, translateAnimation)
        )
        drawRect(brush = brush)
    }
}

/**
 * 4. Pulsing Live Radar / AI Orb Component
 */
@Composable
fun PulsingAiOrb(
    modifier: Modifier = Modifier,
    baseColor: Color = PrimaryBlueGlow,
    secondaryColor: Color = AccentCyan,
    size: Dp = 72.dp,
    isPulsing: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbTransition")

    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale1"
    )

    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha1"
    )

    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale2"
    )

    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha2"
    )

    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRotation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (isPulsing) {
            // Outer Wave 2
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale2)
                    .clip(CircleShape)
                    .background(secondaryColor.copy(alpha = pulseAlpha2))
            )
            // Outer Wave 1
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale1)
                    .clip(CircleShape)
                    .background(baseColor.copy(alpha = pulseAlpha1))
            )
        }

        // Rotating Core
        Canvas(modifier = Modifier.size(size * 0.7f)) {
            rotate(innerRotation) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(
                            baseColor,
                            secondaryColor,
                            AccentPurple,
                            baseColor
                        )
                    )
                )
            }
        }

        // Inner Bright Center
        Box(
            modifier = Modifier
                .size(size * 0.45f)
                .clip(CircleShape)
                .background(Color(0xFF0F172A))
                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
        )
    }
}

/**
 * 5. Animated Number Counter with Easing
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
 * 6. Particle Confetti Celebration Effect
 */
data class Particle(
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val rotationSpeed: Float
)

@Composable
fun CelebrationParticleBurst(
    modifier: Modifier = Modifier,
    particleCount: Int = 40,
    isActive: Boolean = true
) {
    if (!isActive) return

    val colors = listOf(
        PrimaryBlueGlow,
        AccentCyan,
        AccentPurple,
        SuccessGreen,
        WarningAmber,
        Color(0xFFFF6B6B)
    )

    var particles by remember {
        mutableStateOf(
            List(particleCount) {
                val angle = Random.nextFloat() * 2 * Math.PI
                val speed = Random.nextFloat() * 12f + 4f
                Particle(
                    x = 0f,
                    y = 0f,
                    velocityX = (cos(angle) * speed).toFloat(),
                    velocityY = (sin(angle) * speed - 6f).toFloat(),
                    size = Random.nextFloat() * 8f + 4f,
                    color = colors.random(),
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = Random.nextFloat() * 10f - 5f
                )
            }
        )
    }

    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isActive) {
        if (isActive) {
            val startTime = System.currentTimeMillis()
            val duration = 1800f
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                progress = (elapsed / duration).coerceIn(0f, 1f)
                if (progress >= 1f) break
                kotlinx.coroutines.delay(16)
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val alpha = (1f - progress).coerceIn(0f, 1f)

        particles.forEach { p ->
            val curX = centerX + p.velocityX * (progress * 60)
            val curY = centerY + p.velocityY * (progress * 60) + 0.5f * 9.8f * (progress * 15) * (progress * 15)
            val curRot = p.rotation + p.rotationSpeed * (progress * 60)

            rotate(curRot, pivot = Offset(curX, curY)) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(curX - p.size / 2, curY - p.size / 2),
                    size = androidx.compose.ui.geometry.Size(p.size, p.size * 1.5f)
                )
            }
        }
    }
}

/**
 * 7. Staggered Entrance Slide-Up Composable
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
            initialOffsetY = { 60 }
        ),
        modifier = modifier
    ) {
        content()
    }
}
