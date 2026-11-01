package com.example.careerpilot.ui.theme

import androidx.compose.ui.graphics.Color

// ── Neutral Scale (Stripe/Linear style) ─────────────────────────────
// 50 = lightest, 950 = darkest

// Dark Mode
val Dark50 = Color(0xFFEFF4FA)
val Dark100 = Color(0xFFD6E4F0)
val Dark200 = Color(0xFFADBED1)
val Dark300 = Color(0xFF8499B6)
val Dark400 = Color(0xFF5D759A)
val Dark500 = Color(0xFF425A7A)
val Dark600 = Color(0xFF31435E)
val Dark700 = Color(0xFF222E40)
val Dark800 = Color(0xFF161E30)
val Dark900 = Color(0xFF0E1320)
val Dark950 = Color(0xFF080A13)

// Light Mode
val Light50 = Color(0xFF0E1320)
val Light100 = Color(0xFF161E30)
val Light200 = Color(0xFF222E40)
val Light300 = Color(0xFF31435E)
val Light400 = Color(0xFF425A7A)
val Light500 = Color(0xFF5D759A)
val Light600 = Color(0xFF8499B6)
val Light700 = Color(0xFFADBED1)
val Light800 = Color(0xFFD6E4F0)
val Light900 = Color(0xFFEFF4FA)
val Light950 = Color(0xFFF8FAFC)

// ── Semantic Color Aliases ──────────────────────────────────────────
// Backgrounds
val BgBase = Color(0xFF080A13)        // Dark950 / Light950
val BgSurface = Color(0xFF0E1320)     // Dark900 / Light900
val BgCard = Color(0xFF161E30)        // Dark800 / Light800
val BgCardHover = Color(0xFF1E293B)   // Dark700 / Light700
val BgMuted = Color(0xFF222E40)       // Dark600 / Light600

// Borders
val BorderSubtle = Color(0xFF1E293B)  // Dark700 / Light700
val BorderDefault = Color(0xFF31435E) // Dark500 / Light500
val BorderStrong = Color(0xFF425A7A)  // Dark400 / Light400

// Text
val TextPrimary = Color(0xFFF8FAFC)   // Dark50 / Light50
val TextSecondary = Color(0xFF94A3B8) // Dark300 / Light300
val TextMuted = Color(0xFF64748B)     // Dark400 / Light400
val TextInverse = Color(0xFF080A13)   // Dark950 / Light950

// ── Brand Accent (Single primary - Linear blue) ─────────────────────
val Primary = Color(0xFF3B82F6)       // Primary 500
val PrimaryHover = Color(0xFF2563EB)  // Primary 600
val PrimaryLight = Color(0xFF60A5FA)  // Primary 400
val PrimaryContainer = Color(0xFF1E3A5F) // Primary 900 with alpha
val OnPrimary = Color(0xFFFFFFFF)

// ── Semantic Status (Only for genuine states) ───────────────────────
val Success = Color(0xFF10B981)
val SuccessContainer = Color(0xFF064E3B)
val Warning = Color(0xFFF59E0B)
val WarningContainer = Color(0xFF78350F)
val Danger = Color(0xFFEF4444)
val DangerContainer = Color(0xFF7F1D1D)

// ── Surface Variants ────────────────────────────────────────────────
val SurfaceVariant = Color(0xFF1E293B)
val SurfaceElevated = Color(0xFF222E40)
val Overlay = Color(0xBF080A13)       // 75% alpha

// ── Glass/Frosted ───────────────────────────────────────────────────
val GlassSurface = Color(0xE6161E30)      // 90% alpha
val GlassCard = Color(0xE61E293B)         // 90% alpha
val GlassBorder = Color(0x33FFFFFF)       // 20% white
val GlassBorderStrong = Color(0x4DFFFFFF) // 30% white

// ── Backward Compatibility ──────────────────────────────────────────
val PrimaryBlue = Primary
val PrimaryBlueLighter = PrimaryLight
val DarkTextPrimary = TextPrimary
val DarkTextSecondary = TextSecondary
val DarkTextMuted = TextMuted
val DarkBgBase = BgBase
val DarkBgSurface = BgSurface
val DarkBgCard = BgCard
val DarkBgCardHover = BgCardHover
val DarkBgMuted = BgMuted
val DarkBorderSubtle = BorderSubtle
val DarkBorderMedium = BorderDefault
val DarkBorderHighlight = Primary
val SuccessGreen = Success
val SuccessGreenLight = Color(0xFF34D399)
val WarningAmber = Warning
val WarningAmberLight = Color(0xFFFBBF24)
val DangerRed = Danger
val DangerRedLight = Color(0xFFF87171)
val AccentCyan = Color(0xFF06B6D4)
val AccentCyanLight = Color(0xFF22D3EE)
val AccentPurple = Color(0xFF8B5CF6)
val PrimaryBlueGlow = PrimaryLight
val AccentCyanGlow = AccentCyanLight
val AccentPurpleGlow = Color(0xFFA78BFA)
val SuccessGreenGlow = SuccessGreenLight
val WarningAmberGlow = WarningAmberLight
val DangerRedGlow = DangerRedLight
val AccentGreen = Success
val AccentAmber = Warning
val AccentRed = Danger
val AccentIndigo = Color(0xFF4F46E5)
val GlassSurfaceBase = GlassSurface
val GlassCardBase = GlassCard
val GlassCardHover = BgCardHover
val GlassElevated = SurfaceElevated
val GlassHighlight = Color(0x0AFFFFFF)
val GlassOverlay = Color(0x05FFFFFF)
val BorderGlassSpecular = GlassBorder
val BorderFrosted = GlassBorderStrong

// ── Gradients (Minimal, single-stop or subtle) ──────────────────────
val GradientPrimary = listOf(Primary, PrimaryHover)
val GradientSuccess = listOf(Success, Success)
val GradientWarning = listOf(Warning, Warning)
val GradientDanger = listOf(Danger, Danger)

// ── Backward-compatible aliases (old screen references) ──────────────
val BgSurfaceElevated: Color = SurfaceElevated
val BorderMedium: Color = BorderDefault
val BorderHighlight: Color = Primary
