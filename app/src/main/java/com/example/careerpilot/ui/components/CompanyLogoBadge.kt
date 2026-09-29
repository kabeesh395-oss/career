package com.example.careerpilot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careerpilot.ui.theme.BorderSubtle
import com.example.careerpilot.ui.theme.TextPrimary

/**
 * Company & Platform Logo Badge
 *
 * Visually displays the offering organization / company logo for recommended courses
 * and certifications (e.g. Google, AWS, Meta, DeepLearning.AI, Microsoft, O'Reilly, JetBrains, Coursera).
 */
@Composable
fun CompanyLogoBadge(
    company: String,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    val normalized = company.lowercase()
    val shape = RoundedCornerShape((size.value * 0.28f).dp)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        when {
            // ── Google / Android ──────────────────────────────────────────
            normalized.contains("google") || normalized.contains("android") -> {
                Surface(
                    color = Color.White,
                    shape = shape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "G",
                            fontWeight = FontWeight.Black,
                            fontSize = (size.value * 0.58f).sp,
                            color = Color(0xFF4285F4)
                        )
                    }
                }
            }

            // ── Amazon / AWS ──────────────────────────────────────────────
            normalized.contains("aws") || normalized.contains("amazon") -> {
                Surface(
                    color = Color(0xFF232F3E),
                    shape = shape,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "AWS",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = (size.value * 0.36f).sp,
                            color = Color(0xFFFF9900),
                            letterSpacing = (-0.5).sp
                        )
                    }
                }
            }

            // ── Meta / Facebook ───────────────────────────────────────────
            normalized.contains("meta") || normalized.contains("facebook") -> {
                Surface(
                    color = Color(0xFF0668E1),
                    shape = shape,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "M",
                            fontWeight = FontWeight.Black,
                            fontSize = (size.value * 0.55f).sp,
                            color = Color.White
                        )
                    }
                }
            }

            // ── Microsoft / Azure ─────────────────────────────────────────
            normalized.contains("microsoft") || normalized.contains("azure") -> {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = shape,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 2x2 colored squares logo
                    Column(
                        modifier = Modifier.size((size.value * 0.58f).dp),
                        verticalArrangement = Arrangement.spacedBy(1.5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                            Box(modifier = Modifier.size((size.value * 0.25f).dp).background(Color(0xFFF25022)))
                            Box(modifier = Modifier.size((size.value * 0.25f).dp).background(Color(0xFF7FBA00)))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                            Box(modifier = Modifier.size((size.value * 0.25f).dp).background(Color(0xFF00A4EF)))
                            Box(modifier = Modifier.size((size.value * 0.25f).dp).background(Color(0xFFFFB900)))
                        }
                    }
                }
            }

            // ── DeepLearning.AI ───────────────────────────────────────────
            normalized.contains("deeplearning") || normalized.contains("deep learning") -> {
                Surface(
                    color = Color(0xFFFF6F61),
                    shape = shape,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "AI",
                            fontWeight = FontWeight.Black,
                            fontSize = (size.value * 0.42f).sp,
                            color = Color.White
                        )
                    }
                }
            }

            // ── O'Reilly / DDIA ───────────────────────────────────────────
            normalized.contains("o'reilly") || normalized.contains("oreilly") || normalized.contains("data-intensive") -> {
                Surface(
                    color = Color(0xFFD31145),
                    shape = shape,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "O'R",
                            fontWeight = FontWeight.Bold,
                            fontSize = (size.value * 0.38f).sp,
                            color = Color.White
                        )
                    }
                }
            }

            // ── JetBrains / Kotlin ────────────────────────────────────────
            normalized.contains("jetbrains") || normalized.contains("kotlin") -> {
                Surface(
                    color = Color(0xFF000000),
                    shape = shape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F2FE)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "JB",
                            fontWeight = FontWeight.Black,
                            fontSize = (size.value * 0.42f).sp,
                            color = Color(0xFFF355DA)
                        )
                    }
                }
            }

            // ── Coursera ──────────────────────────────────────────────────
            normalized.contains("coursera") -> {
                Surface(
                    color = Color(0xFF0056D2),
                    shape = shape,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "C",
                            fontWeight = FontWeight.Black,
                            fontSize = (size.value * 0.6f).sp,
                            color = Color.White
                        )
                    }
                }
            }

            // ── Database / PostgreSQL ─────────────────────────────────────
            normalized.contains("postgres") || normalized.contains("database") || normalized.contains("sql") || normalized.contains("index") -> {
                Surface(
                    color = Color(0xFF336791),
                    shape = shape,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Database",
                            tint = Color.White,
                            modifier = Modifier.size((size.value * 0.58f).dp)
                        )
                    }
                }
            }

            // ── Academic / Harvard / Stanford / edX ────────────────────────
            normalized.contains("harvard") || normalized.contains("stanford") || normalized.contains("edx") || normalized.contains("mit") -> {
                Surface(
                    color = Color(0xFFA51C30),
                    shape = shape,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "University",
                            tint = Color.White,
                            modifier = Modifier.size((size.value * 0.58f).dp)
                        )
                    }
                }
            }

            // ── Fallback Branded Monogram ─────────────────────────────────
            else -> {
                val initials = company.split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .map { it.first().uppercase() }
                    .joinToString("")
                    .ifEmpty { "C" }

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = shape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            fontWeight = FontWeight.Bold,
                            fontSize = (size.value * 0.42f).sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
