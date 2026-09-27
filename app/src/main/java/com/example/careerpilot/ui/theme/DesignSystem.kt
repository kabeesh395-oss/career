package com.example.careerpilot.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * CareerPilot Unified Design System
 *
 * Central source of truth for design tokens across the application:
 * - [Colors]: Semantic and brand color palette
 * - [Spacing]: Consistent 4/8dp spatial grid scale and layout metrics
 * - [Shapes]: Corner radii and Shape definitions
 * - [TypographyTokens]: Standardized type scale hierarchy
 * - [Components]: Sizing, icons, buttons, cards, and interactive component variants
 */
object DesignSystem {

    // ── COLORS TOKEN PALETTE ──────────────────────────────────────────
    object Colors {
        // Core Canvas & Surfaces
        val Background: Color = BgBase
        val Surface: Color = BgSurface
        val SurfaceElevated: Color = BgSurfaceElevated
        val Card: Color = BgCard
        val CardHover: Color = BgCardHover
        val Muted: Color = BgMuted

        // Primary Brand
        val Primary: Color = PrimaryBlue
        val PrimaryLight: Color = PrimaryBlueLighter
        val PrimaryContainer: Color = BgCard

        // Accent Palette
        val Cyan: Color = AccentCyan
        val CyanLight: Color = AccentCyanLight
        val Purple: Color = PrimaryBlue
        val Indigo: Color = AccentIndigo

        // Status Feedback
        val Success: Color = SuccessGreen
        val SuccessLight: Color = SuccessGreenLight
        val Warning: Color = WarningAmber
        val WarningLight: Color = WarningAmberLight
        val Error: Color = DangerRed
        val ErrorLight: Color = DangerRedLight

        // Semantic Text
        val TextPrimary: Color = com.example.careerpilot.ui.theme.TextPrimary
        val TextSecondary: Color = com.example.careerpilot.ui.theme.TextSecondary
        val TextMuted: Color = com.example.careerpilot.ui.theme.TextMuted

        // Borders & Dividers
        val BorderSubtle: Color = com.example.careerpilot.ui.theme.BorderSubtle
        val BorderMedium: Color = com.example.careerpilot.ui.theme.BorderMedium
        val BorderHighlight: Color = com.example.careerpilot.ui.theme.BorderHighlight
    }

    // ── SPACING & METRICS TOKEN SCALE ────────────────────────────────
    object Spacing {
        val xxs: Dp = 2.dp
        val xs: Dp = 4.dp
        val sm: Dp = 8.dp
        val md: Dp = 12.dp
        val lg: Dp = 16.dp
        val xl: Dp = 20.dp
        val xxl: Dp = 24.dp
        val xxxl: Dp = 32.dp
        val huge: Dp = 48.dp

        // Canonical Screen Layout Tokens
        val screenHorizontal: Dp = 16.dp
        val screenTop: Dp = 16.dp
        val screenBottom: Dp = 96.dp
        val sectionSpacing: Dp = 16.dp
        val itemSpacing: Dp = 12.dp
        val cardPadding: Dp = 16.dp
    }

    // ── SHAPES & CORNER RADII ─────────────────────────────────────────
    object Shapes {
        val radiusXs: Dp = 4.dp
        val radiusSm: Dp = 8.dp
        val radiusMd: Dp = 12.dp
        val radiusLg: Dp = 16.dp
        val radiusXl: Dp = 20.dp
        val radiusPill: Dp = 10.dp

        val shapeXs: Shape = RoundedCornerShape(radiusXs)
        val shapeSm: Shape = RoundedCornerShape(radiusSm)
        val shapeMd: Shape = RoundedCornerShape(radiusMd)
        val shapeLg: Shape = RoundedCornerShape(radiusLg)
        val shapeXl: Shape = RoundedCornerShape(radiusXl)
        val shapePill: Shape = RoundedCornerShape(radiusPill)
        val shapeCircle: Shape = CircleShape
    }

    // ── TYPOGRAPHY TOKENS ─────────────────────────────────────────────
    object TypographyTokens {
        val headlineLarge: TextStyle get() = Typography.headlineLarge
        val headlineMedium: TextStyle get() = Typography.headlineMedium
        val headlineSmall: TextStyle get() = Typography.headlineSmall

        val titleLarge: TextStyle get() = Typography.titleLarge
        val titleMedium: TextStyle get() = Typography.titleMedium
        val titleSmall: TextStyle get() = Typography.titleSmall

        val bodyLarge: TextStyle get() = Typography.bodyLarge
        val bodyMedium: TextStyle get() = Typography.bodyMedium
        val bodySmall: TextStyle get() = Typography.bodySmall

        val labelLarge: TextStyle get() = Typography.labelLarge
        val labelMedium: TextStyle get() = Typography.labelMedium
        val labelSmall: TextStyle get() = Typography.labelSmall

        // Standalone Font Size & Line Height Tokens
        val fontXxs: TextUnit = 9.sp
        val fontXs: TextUnit = 10.sp
        val fontSm: TextUnit = 11.sp
        val fontMd: TextUnit = 13.sp
        val fontBase: TextUnit = 14.sp
        val fontLg: TextUnit = 16.sp
        val fontXl: TextUnit = 18.sp
        val font2xl: TextUnit = 20.sp
        val font3xl: TextUnit = 24.sp
        val font4xl: TextUnit = 28.sp
    }

    // ── COMPONENT VARIANTS & SPECIFICATIONS ───────────────────────────
    object Components {
        // Touch & Target Metrics
        val touchTargetMin: Dp = 48.dp
        val buttonHeight: Dp = 48.dp
        val buttonHeightSmall: Dp = 36.dp

        // Icons
        val iconXs: Dp = 14.dp
        val iconSm: Dp = 16.dp
        val iconMd: Dp = 20.dp
        val iconLg: Dp = 24.dp
        val iconXl: Dp = 32.dp

        // Avatars & Badges
        val avatarSm: Dp = 32.dp
        val avatarMd: Dp = 40.dp
        val avatarLg: Dp = 56.dp
        val badgePaddingHorizontal: Dp = 8.dp
        val badgePaddingVertical: Dp = 4.dp
        val badgeRadius: Dp = 6.dp

        // Gauge / Circle Metric Sizes
        val gaugeSizeSm: Dp = 54.dp
        val gaugeSizeMd: Dp = 72.dp
        val gaugeSizeLg: Dp = 96.dp
        val gaugeSizeXl: Dp = 100.dp
        val gaugeStrokeSm: Dp = 5.dp
        val gaugeStrokeMd: Dp = 8.dp

        // Borders
        val borderThin: Dp = 1.dp
        val borderMedium: Dp = 1.5.dp
        val borderThick: Dp = 2.dp
    }
}
