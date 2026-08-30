package com.example.careerpilot.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.careerpilot.ui.theme.AccentCyanGlow
import com.example.careerpilot.ui.theme.TextMuted
import com.example.careerpilot.ui.theme.TextPrimary

/**
 * Standard Approved CareerHub Logo Component
 * Renders the exact approved constellation logo emblem and brand typography
 */
@Composable
fun CareerHubLogo(
    modifier: Modifier = Modifier,
    iconOnly: Boolean = false,
    size: Dp = 36.dp,
    showSubtitle: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Exact Emblem Asset
        Image(
            painter = painterResource(id = R.drawable.ic_careerhub_logo),
            contentDescription = "CareerHub Logo",
            modifier = Modifier.size(size)
        )

        if (!iconOnly) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Career",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontSize = (size.value * 0.46f).sp,
                        letterSpacing = (-0.02).sp,
                        lineHeight = (size.value * 0.52f).sp
                    )
                    Text(
                        text = "Hub",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentCyanGlow, // #38BDF8 matching the approved brand spec
                        fontSize = (size.value * 0.46f).sp,
                        letterSpacing = (-0.02).sp,
                        lineHeight = (size.value * 0.52f).sp
                    )
                }
                if (showSubtitle) {
                    Text(
                        text = "PROFESSIONAL NETWORK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        fontSize = (size.value * 0.22f).coerceAtLeast(8f).sp,
                        letterSpacing = 1.2.sp,
                        lineHeight = (size.value * 0.26f).sp
                    )
                }
            }
        }
    }
}
