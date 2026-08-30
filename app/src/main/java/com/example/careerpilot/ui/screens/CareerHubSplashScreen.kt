package com.example.careerpilot.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CareerHubSplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animationStage by remember { mutableIntStateOf(0) }

    // Logo scale and rotation transition
    val logoScale = remember { Animatable(0.2f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoRotation = remember { Animatable(-45f) }

    // Glow pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "hubGlow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    LaunchedEffect(Unit) {
        // Stage 1: Logo springs into center
        logoAlpha.animateTo(1f, tween(400))
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        logoRotation.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        
        delay(150)
        animationStage = 1 // Title enters

        delay(250)
        animationStage = 2 // Subtitle & features enter

        delay(1600)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSplashFinished
            )
            .testTag("career_hub_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Background ambient glow particles / concentric radar rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f - 40f)
            
            // Outer halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PrimaryBlue.copy(alpha = pulseAlpha * 0.35f), Color.Transparent),
                    center = center,
                    radius = size.width * 0.75f
                ),
                center = center,
                radius = size.width * 0.75f
            )

            // Tech grid radar circles
            drawCircle(
                color = BorderSubtle.copy(alpha = 0.4f),
                center = center,
                radius = 120.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = BorderSubtle.copy(alpha = 0.25f),
                center = center,
                radius = 180.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Animated Approved Hub Emblem
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_careerhub_logo),
                    contentDescription = "CareerHub Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Name Animation: Approved "Career" + "Hub" Wordmark
            AnimatedVisibility(
                visible = animationStage >= 1,
                enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { 30 }, animationSpec = tween(400))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Career",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontSize = 32.sp,
                            letterSpacing = (-0.02).sp
                        )
                        Text(
                            text = "Hub",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentCyanGlow,
                            fontSize = 32.sp,
                            letterSpacing = (-0.02).sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "PROFESSIONAL NETWORK",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subtitle & Feature Tags Animation
            AnimatedVisibility(
                visible = animationStage >= 2,
                enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { 20 }, animationSpec = tween(400))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryBlue.copy(alpha = 0.12f))
                                .border(1.dp, PrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("SKILL MATRIX", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueGlow)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SuccessGreen.copy(alpha = 0.12f))
                                .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("ATS AUDIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreenGlow)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentCyan.copy(alpha = 0.12f))
                                .border(1.dp, AccentCyan.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("INTERVIEWS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Enter Button / Tap to continue
                    Button(
                        onClick = onSplashFinished,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .testTag("career_hub_enter_btn")
                            .height(44.dp)
                    ) {
                        Text("Enter Career Hub", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
