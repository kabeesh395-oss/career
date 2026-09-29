package com.example.careerpilot.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = DarkTextPrimary,
    primaryContainer = DarkBgCard,
    onPrimaryContainer = PrimaryBlueLighter,
    secondary = AccentCyan,
    onSecondary = DarkBgBase,
    secondaryContainer = DarkBgCardHover,
    onSecondaryContainer = AccentCyanLight,
    tertiary = AccentPurple,
    background = DarkBgBase,
    onBackground = DarkTextPrimary,
    surface = DarkBgSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkBgCard,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerLow = DarkBgCard,
    surfaceContainer = DarkBgCardHover,
    surfaceContainerHigh = DarkBgSurfaceElevated,
    outline = DarkBorderSubtle,
    outlineVariant = DarkBorderMedium,
    error = DangerRed,
    onError = DarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = LightBgSurfaceElevated,
    onPrimaryContainer = PrimaryBlue,
    secondary = AccentCyan,
    onSecondary = Color.White,
    secondaryContainer = LightBgCardHover,
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = AccentIndigo,
    background = LightBgBase,
    onBackground = LightTextPrimary,
    surface = LightBgSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightBgCard,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = LightBgCard,
    surfaceContainerHigh = LightBgSurfaceElevated,
    outline = LightBorderSubtle,
    outlineVariant = LightBorderMedium,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun CareerPilotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val barColor = if (darkTheme) DarkBgBase else LightBgBase
            window.statusBarColor = barColor.toArgb()
            window.navigationBarColor = barColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

