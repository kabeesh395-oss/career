package com.example.careerpilot.ui.theme

import androidx.compose.ui.graphics.Color

// ── Core Backgrounds ──────────────────────────────────────────────
val BgBase = Color(0xFF090D16)
val BgSurface = Color(0xFF0F172A)
val BgCard = Color(0xFF131D33)
val BgCardHover = Color(0xFF1B2742)
val BgMuted = Color(0xFF1E293B)
val BgSurfaceElevated = Color(0xFF16223D)

// ── Borders ───────────────────────────────────────────────────────
val BorderSubtle = Color(0xFF1E293B)
val BorderMedium = Color(0xFF334155)
val BorderHighlight = Color(0xFF3B82F6)

// ── Primary ───────────────────────────────────────────────────────
val PrimaryBlue = Color(0xFF3B82F6)
val PrimaryBlueLighter = Color(0xFF60A5FA)

// ── Accent Colors ─────────────────────────────────────────────────
val AccentCyan = Color(0xFF06B6D4)
val AccentCyanLight = Color(0xFF38BDF8)
val AccentPurple = Color(0xFF8B5CF6)
val AccentIndigo = Color(0xFF6366F1)

// ── Status Colors ─────────────────────────────────────────────────
val SuccessGreen = Color(0xFF10B981)
val SuccessGreenLight = Color(0xFF34D399)
val WarningAmber = Color(0xFFF59E0B)
val WarningAmberLight = Color(0xFFFBBF24)
val DangerRed = Color(0xFFEF4444)
val DangerRedLight = Color(0xFFF87171)

// ── Text ──────────────────────────────────────────────────────────
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// ── Backward-compatible aliases (to be removed as screens are updated) ──
val PrimaryBlueGlow = PrimaryBlueLighter
val AccentCyanGlow = AccentCyanLight
val AccentPurpleGlow = AccentPurple
val SuccessGreenGlow = SuccessGreenLight
val WarningAmberGlow = WarningAmberLight
val DangerRedGlow = DangerRedLight
val AccentGreen = SuccessGreen
val AccentAmber = WarningAmber
val AccentRed = DangerRed

// Glass aliases (backward compatibility — will be removed)
val GlassSurfaceBase = Color(0xE60D1526)
val GlassCardBase = Color(0xCC111C33)
val GlassCardHover = Color(0xE0172645)
val GlassElevated = BgSurfaceElevated
val GlassHighlight = Color(0x14FFFFFF)
val GlassOverlay = Color(0x08FFFFFF)
val BorderGlassSpecular = Color(0x3393C5FD)
val BorderFrosted = Color(0x1FFFFFFF)
