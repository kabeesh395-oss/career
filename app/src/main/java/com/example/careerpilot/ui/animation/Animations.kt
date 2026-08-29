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
 * High-end Glassmorphism & Atmospheric Animation Engine
 */

/**
 * Clean Modern Dark Canvas with subtle ambient depth
 */
@Composable
fun AmbientGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .drawBehind {
                val w = size.width
                val h = size.height

                // Subtle deep top-down gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF090D16),
                            Color(0xFF06090F)
                        ),
                        startY = 0f,
                        endY = h
                    )
                )

                // Very gentle ambient top-right glow for depth
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryBlue.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.85f, h * 0.1f),
                        radius = w * 0.7f
                    ),
                    center = Offset(w * 0.85f, h * 0.1f),
                    radius = w * 0.7f
                )
            }
    ) {
        content()
    }
}

/**
 * 1. Bouncy Clickable Modifier with Spring Physics
 */
fun Modifier.bouncyClickable(
    pressedScale: Float = 0.96f,
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
 * 2. Elegant Precision Focus Border
 */
fun Modifier.animatedGradientBorder(
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.dp,
    colors: List<Color> = listOf(
        PrimaryBlue.copy(alpha = 0.6f),
        AccentCyan.copy(alpha = 0.4f),
        PrimaryBlue.copy(alpha = 0.2f)
    ),
    durationMillis: Int = 3000
): Modifier = composed {
    this
        .clip(shape)
        .border(borderWidth, Brush.linearGradient(colors), shape)
}

/**
 * 3. Shimmer Sweep Effect for Loading / Highlighting
 */
fun Modifier.shimmerSweep(
    shimmerColors: List<Color> = listOf(
        Color.White.copy(alpha = 0.0f),
        Color.White.copy(alpha = 0.12f),
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
 * 4. Refined Audio Waveform / Live Voice Indicator
 */
@Composable
fun PulsingAiOrb(
    modifier: Modifier = Modifier,
    baseColor: Color = PrimaryBlueGlow,
    secondaryColor: Color = AccentCyan,
    size: Dp = 64.dp,
    isPulsing: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbTransition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (isPulsing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(baseColor.copy(alpha = pulseAlpha))
            )
        }

        // Clean modern inner icon container
        Box(
            modifier = Modifier
                .size(size * 0.7f)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(PrimaryBlue.copy(alpha = 0.3f), BgCard)
                    )
                )
                .border(1.dp, BorderHighlight.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Audio wave lines
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val heights = listOf(0.4f, 0.8f, 1f, 0.7f, 0.4f)
                heights.forEachIndexed { idx, h ->
                    val waveHeight by infiniteTransition.animateFloat(
                        initialValue = (size.value * 0.2f * h),
                        targetValue = (size.value * 0.45f * h),
                        animationSpec = infiniteRepeatable(
                            animation = tween(600 + (idx * 120), easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "wave_$idx"
                    )
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(waveHeight.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isPulsing) PrimaryBlueGlow else TextMuted)
                    )
                }
            }
        }
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
