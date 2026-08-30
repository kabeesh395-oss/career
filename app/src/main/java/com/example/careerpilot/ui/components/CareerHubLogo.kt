package com.example.careerpilot.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.careerpilot.ui.theme.AccentCyanLight
import com.example.careerpilot.ui.theme.Dimens
import com.example.careerpilot.ui.theme.TextMuted
import com.example.careerpilot.ui.theme.TextPrimary

/**
 * Canonical CareerHub logo component.
 * Always references the single approved ic_careerhub_logo asset.
 */
@Composable
fun CareerHubLogo(
    modifier: Modifier = Modifier,
    iconOnly: Boolean = false,
    size: Dp = 32.dp,
    showSubtitle: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_careerhub_logo),
            contentDescription = "CareerHub Logo",
            modifier = Modifier.size(size)
        )

        if (!iconOnly) {
            Column(verticalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Career",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = (-0.02).sp
                    )
                    Text(
                        text = "Hub",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentCyanLight,
                        letterSpacing = (-0.02).sp
                    )
                }
                if (showSubtitle) {
                    Text(
                        text = "PROFESSIONAL NETWORK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }
    }
}
