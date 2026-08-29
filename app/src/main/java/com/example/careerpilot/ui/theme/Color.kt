package com.example.careerpilot.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core Deep Obsidian & Slate Backgrounds
val BgBase = Color(0xFF090D16)
val BgSurface = Color(0xFF0F172A)
val BgCard = Color(0xFF131D33)
val BgCardHover = Color(0xFF1B2742)
val BgMuted = Color(0xFF1E293B)

// Refined Glassmorphism & Translucent Surface Fills
val GlassSurfaceBase = Color(0xE60D1526)
val GlassCardBase = Color(0xCC111C33)
val GlassCardHover = Color(0xE0172645)
val GlassElevated = Color(0xE616223D)
val GlassHighlight = Color(0x14FFFFFF)
val GlassOverlay = Color(0x08FFFFFF)

// Precision Hairline & Card Borders
val BorderSubtle = Color(0xFF1E293B)
val BorderGlassSpecular = Color(0x3393C5FD)
val BorderHighlight = Color(0xFF3B82F6)
val BorderFrosted = Color(0x1FFFFFFF)

// Focused Accents (Tailwind & Linear inspired)
val PrimaryBlue = Color(0xFF3B82F6)
val PrimaryBlueGlow = Color(0xFF60A5FA)
val AccentCyan = Color(0xFF06B6D4)
val AccentCyanGlow = Color(0xFF38BDF8)
val AccentPurple = Color(0xFF8B5CF6)
val AccentPurpleGlow = Color(0xFFA78BFA)
val AccentIndigo = Color(0xFF6366F1)

// Status Colors
val SuccessGreen = Color(0xFF10B981)
val SuccessGreenGlow = Color(0xFF34D399)
val WarningAmber = Color(0xFFF59E0B)
val WarningAmberGlow = Color(0xFFFBBF24)
val DangerRed = Color(0xFFEF4444)
val DangerRedGlow = Color(0xFFF87171)

val AccentGreen = SuccessGreen
val AccentAmber = WarningAmber
val AccentRed = DangerRed
val BgSurfaceElevated = GlassElevated

// High Contrast Modern Typography
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Glass Gradients
val GlassCardGradient = Brush.verticalGradient(
    listOf(
        Color(0x22FFFFFF),
        Color(0x08FFFFFF),
        Color(0x00FFFFFF)
    )
)

val GlassBorderGradient = Brush.linearGradient(
    listOf(
        Color(0x66FFFFFF),
        Color(0x2293C5FD),
        Color(0x0DFFFFFF)
    )
)

val GlassPrimaryGlowGradient = Brush.linearGradient(
    listOf(
        Color(0x333B82F6),
        Color(0x1A06B6D4),
        Color(0x080B0F19)
    )
)
