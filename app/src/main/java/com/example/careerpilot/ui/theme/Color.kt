package com.example.careerpilot.ui.theme

import androidx.compose.ui.graphics.Color

// ── Core Neutral Backgrounds & Surfaces ─────────────────────────
val BgBase = Color(0xFF0A0E17)
val BgBaseDark = Color(0xFF070A10)
val BgBaseCosmic = Color(0xFF0A0E17)
val BgBaseNavy = Color(0xFF0D121F)
val BgSurface = Color(0xFF101726)
val BgCard = Color(0xFF141D30)
val BgCardHover = Color(0xFF1C2842)
val BgMuted = Color(0xFF1E293B)
val BgSurfaceElevated = Color(0xFF18233A)

// ── Borders ─────────────────────────────────────────────────────
val BorderSubtle = Color(0xFF1E2B45)
val BorderMedium = Color(0xFF334466)
val BorderHighlight = Color(0xFF3B82F6)

// ── Single Primary Brand Accent (Engineering Blue) ──────────────
val PrimaryBlue = Color(0xFF2563EB)
val PrimaryBlueLighter = Color(0xFF3B82F6)

// ── Secondary Brand Accents (Restrained) ────────────────────────
val AccentCyan = Color(0xFF38BDF8)
val AccentCyanLight = Color(0xFF7DD3FC)
val AccentPurple = Color(0xFF3B82F6) // Removed purple, aligned to primary brand
val AccentIndigo = Color(0xFF2563EB)

// ── Semantic Status Colors (Only for genuine states) ────────────
val SuccessGreen = Color(0xFF10B981)
val SuccessGreenLight = Color(0xFF34D399)
val WarningAmber = Color(0xFFF59E0B)
val WarningAmberLight = Color(0xFFFBBF24)
val DangerRed = Color(0xFFEF4444)
val DangerRedLight = Color(0xFFF87171)

// ── Text Hierarchy ──────────────────────────────────────────────
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

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
val GlassSurfaceBase = Color(0xF2101726)
val GlassCardBase = Color(0xF2141D30)
val GlassCardHover = Color(0xFF1C2842)
val GlassElevated = BgSurfaceElevated
val GlassHighlight = Color(0x0AFFFFFF)
val GlassOverlay = Color(0x05FFFFFF)
val BorderGlassSpecular = Color(0xFF1E2B45)
val BorderFrosted = Color(0x14FFFFFF)

// ── Restrained Flat/Minimal Gradients ───────────────────────────
val GradientCardBase = listOf(BgCard, BgCard)
val GradientCardHighlight = listOf(BgCard, BgCard)
val GradientHeroGauge = listOf(PrimaryBlue, PrimaryBlueLighter)
val GradientSuccess = listOf(SuccessGreen, SuccessGreen)
val GradientWarning = listOf(WarningAmber, WarningAmber)
val GradientDanger = listOf(DangerRed, DangerRed)
val GradientTopBar = listOf(BgSurface, BgSurface)

