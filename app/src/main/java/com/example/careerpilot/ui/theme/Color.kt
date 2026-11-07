package com.example.careerpilot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Dark Mode Neutral Backgrounds & Surfaces ─────────────────────
val DarkBgBase = Color(0xFF0A0E17)
val DarkBgBaseDark = Color(0xFF070A10)
val DarkBgBaseCosmic = Color(0xFF0A0E17)
val DarkBgBaseNavy = Color(0xFF0D121F)
val DarkBgSurface = Color(0xFF101726)
val DarkBgCard = Color(0xFF141D30)
val DarkBgCardHover = Color(0xFF1C2842)
val DarkBgMuted = Color(0xFF1E293B)
val DarkBgSurfaceElevated = Color(0xFF18233A)

val DarkBorderSubtle = Color(0xFF1E2B45)
val DarkBorderMedium = Color(0xFF334466)
val DarkBorderHighlight = Color(0xFF3B82F6)

val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)

// ── Light Mode Neutral Backgrounds & Surfaces (Crisp, High-Contrast) ───
val LightBgBase = Color(0xFFF8FAFC)
val LightBgBaseDark = Color(0xFFF1F5F9)
val LightBgBaseCosmic = Color(0xFFF8FAFC)
val LightBgBaseNavy = Color(0xFFEDF2F7)
val LightBgSurface = Color(0xFFFFFFFF)
val LightBgCard = Color(0xFFFFFFFF)
val LightBgCardHover = Color(0xFFF1F5F9)
val LightBgMuted = Color(0xFFF1F5F9)
val LightBgSurfaceElevated = Color(0xFFF8FAFC)

val LightBorderSubtle = Color(0xFFE2E8F0)
val LightBorderMedium = Color(0xFFCBD5E1)
val LightBorderHighlight = Color(0xFF2563EB)

val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextMuted = Color(0xFF64748B)

// ── Dynamic Color Accessors (Depend upon user default mobile condition) ──
val BgBase: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkBgBase else LightBgBase

val BgBaseDark: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkBgBaseDark else LightBgBaseDark

val BgBaseCosmic: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkBgBaseCosmic else LightBgBaseCosmic

val BgBaseNavy: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkBgBaseNavy else LightBgBaseNavy

val BgSurface: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkBgSurface else LightBgSurface

val BgCard: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkBgCard else LightBgCard

val BgCardHover: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkBgCardHover else LightBgCardHover

val BgMuted: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkBgMuted else LightBgMuted

val BgSurfaceElevated: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkBgSurfaceElevated else LightBgSurfaceElevated

// ── Dynamic Borders ─────────────────────────────────────────────
val BorderSubtle: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkBorderSubtle else LightBorderSubtle

val BorderMedium: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkBorderMedium else LightBorderMedium

val BorderHighlight: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkBorderHighlight else LightBorderHighlight

// ── Dynamic Text Hierarchy ──────────────────────────────────────
val TextPrimary: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkTextPrimary else LightTextPrimary

val TextSecondary: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary

val TextMuted: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkTextMuted else LightTextMuted

// ── Single Primary Brand Accent (Engineering Blue) ──────────────
val PrimaryBlue = Color(0xFF2563EB)
val PrimaryBlueLighter = Color(0xFF3B82F6)

// ── Secondary Brand Accents (Restrained) ────────────────────────
val AccentCyan = Color(0xFF0284C7)
val AccentCyanLight = Color(0xFF38BDF8)
val AccentPurple = Color(0xFF3B82F6)
val AccentIndigo = Color(0xFF2563EB)

// ── Semantic Status Colors (Only for genuine states) ────────────
val SuccessGreen = Color(0xFF10B981)
val SuccessGreenLight = Color(0xFF34D399)
val WarningAmber = Color(0xFFF59E0B)
val WarningAmberLight = Color(0xFFFBBF24)
val DangerRed = Color(0xFFEF4444)
val DangerRedLight = Color(0xFFF87171)

// ── Backward-compatible aliases ─────────────────────────────────
val PrimaryBlueGlow = PrimaryBlueLighter
val AccentCyanGlow = AccentCyanLight
val AccentPurpleGlow = PrimaryBlueLighter
val SuccessGreenGlow = SuccessGreenLight
val WarningAmberGlow = WarningAmberLight
val DangerRedGlow = DangerRedLight
val AccentGreen = SuccessGreen
val AccentAmber = WarningAmber
val AccentRed = DangerRed

// Clean Surface aliases
val GlassSurfaceBase: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xF2101726) else Color(0xF2FFFFFF)

val GlassCardBase: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xF2141D30) else Color(0xF2FFFFFF)

val GlassCardHover: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkBgCardHover else LightBgCardHover

val GlassElevated: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkBgSurfaceElevated else LightBgSurfaceElevated

val GlassHighlight = Color(0x0AFFFFFF)
val GlassOverlay = Color(0x05FFFFFF)

val BorderGlassSpecular: Color
    @Composable get() = if (isSystemInDarkTheme()) DarkBorderSubtle else LightBorderSubtle

val BorderFrosted: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0x14FFFFFF) else Color(0x14000000)

// ── Restrained Flat/Minimal Gradients ───────────────────────────
val GradientHeroGauge = listOf(PrimaryBlue, PrimaryBlueLighter)
val GradientSuccess = listOf(SuccessGreen, SuccessGreen)
val GradientWarning = listOf(WarningAmber, WarningAmber)
val GradientDanger = listOf(DangerRed, DangerRed)


