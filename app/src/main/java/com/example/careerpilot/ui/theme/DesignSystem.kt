package com.example.careerpilot.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

/**
 * DesignSystem - Single access point for all design tokens
 * Usage: DesignSystem.Colors.Primary, DesignSystem.Typography.bodyLarge, DesignSystem.Spacing.Space4
 */
object DesignSystem {

    object Colors {
        // Backgrounds
        val BgBase: Color = com.example.careerpilot.ui.theme.BgBase
        val BgSurface: Color = com.example.careerpilot.ui.theme.BgSurface
        val BgCard: Color = com.example.careerpilot.ui.theme.BgCard
        val BgCardHover: Color = com.example.careerpilot.ui.theme.BgCardHover
        val BgMuted: Color = com.example.careerpilot.ui.theme.BgMuted

        // Borders
        val BorderSubtle: Color = com.example.careerpilot.ui.theme.BorderSubtle
        val BorderDefault: Color = com.example.careerpilot.ui.theme.BorderDefault
        val BorderStrong: Color = com.example.careerpilot.ui.theme.BorderStrong

        // Text
        val TextPrimary: Color = com.example.careerpilot.ui.theme.TextPrimary
        val TextSecondary: Color = com.example.careerpilot.ui.theme.TextSecondary
        val TextMuted: Color = com.example.careerpilot.ui.theme.TextMuted
        val TextInverse: Color = com.example.careerpilot.ui.theme.TextInverse

        // Brand
        val Primary: Color = com.example.careerpilot.ui.theme.Primary
        val PrimaryHover: Color = com.example.careerpilot.ui.theme.PrimaryHover
        val PrimaryLight: Color = com.example.careerpilot.ui.theme.PrimaryLight
        val PrimaryContainer: Color = com.example.careerpilot.ui.theme.PrimaryContainer
        val OnPrimary: Color = com.example.careerpilot.ui.theme.OnPrimary

        // Status
        val Success: Color = com.example.careerpilot.ui.theme.Success
        val SuccessContainer: Color = com.example.careerpilot.ui.theme.SuccessContainer
        val Warning: Color = com.example.careerpilot.ui.theme.Warning
        val WarningContainer: Color = com.example.careerpilot.ui.theme.WarningContainer
        val Danger: Color = com.example.careerpilot.ui.theme.Danger
        val DangerContainer: Color = com.example.careerpilot.ui.theme.DangerContainer

        // Glass
        val GlassSurface: Color = com.example.careerpilot.ui.theme.GlassSurface
        val GlassCard: Color = com.example.careerpilot.ui.theme.GlassCard
        val GlassBorder: Color = com.example.careerpilot.ui.theme.GlassBorder
        val GlassBorderStrong: Color = com.example.careerpilot.ui.theme.GlassBorderStrong

        // Surface
        val SurfaceVariant: Color = com.example.careerpilot.ui.theme.SurfaceVariant
        val SurfaceElevated: Color = com.example.careerpilot.ui.theme.SurfaceElevated
        val Overlay: Color = com.example.careerpilot.ui.theme.Overlay
    }

    object Typography {
        val displayLarge: TextStyle = com.example.careerpilot.ui.theme.Typography.displayLarge
        val displayMedium: TextStyle = com.example.careerpilot.ui.theme.Typography.displayMedium
        val displaySmall: TextStyle = com.example.careerpilot.ui.theme.Typography.displaySmall
        val headlineLarge: TextStyle = com.example.careerpilot.ui.theme.Typography.headlineLarge
        val headlineMedium: TextStyle = com.example.careerpilot.ui.theme.Typography.headlineMedium
        val headlineSmall: TextStyle = com.example.careerpilot.ui.theme.Typography.headlineSmall
        val titleLarge: TextStyle = com.example.careerpilot.ui.theme.Typography.titleLarge
        val titleMedium: TextStyle = com.example.careerpilot.ui.theme.Typography.titleMedium
        val titleSmall: TextStyle = com.example.careerpilot.ui.theme.Typography.titleSmall
        val bodyLarge: TextStyle = com.example.careerpilot.ui.theme.Typography.bodyLarge
        val bodyMedium: TextStyle = com.example.careerpilot.ui.theme.Typography.bodyMedium
        val bodySmall: TextStyle = com.example.careerpilot.ui.theme.Typography.bodySmall
        val labelLarge: TextStyle = com.example.careerpilot.ui.theme.Typography.labelLarge
        val labelMedium: TextStyle = com.example.careerpilot.ui.theme.Typography.labelMedium
        val labelSmall: TextStyle = com.example.careerpilot.ui.theme.Typography.labelSmall
    }

    object Spacing {
        val Space0: Dp = com.example.careerpilot.ui.theme.Dimens.Space0
        val Space1: Dp = com.example.careerpilot.ui.theme.Dimens.Space1
        val Space2: Dp = com.example.careerpilot.ui.theme.Dimens.Space2
        val Space3: Dp = com.example.careerpilot.ui.theme.Dimens.Space3
        val Space4: Dp = com.example.careerpilot.ui.theme.Dimens.Space4
        val Space5: Dp = com.example.careerpilot.ui.theme.Dimens.Space5
        val Space6: Dp = com.example.careerpilot.ui.theme.Dimens.Space6
        val Space8: Dp = com.example.careerpilot.ui.theme.Dimens.Space8
        val Space10: Dp = com.example.careerpilot.ui.theme.Dimens.Space10
        val Space12: Dp = com.example.careerpilot.ui.theme.Dimens.Space12
        val Space16: Dp = com.example.careerpilot.ui.theme.Dimens.Space16

        val ContentHorizontal: Dp = com.example.careerpilot.ui.theme.Dimens.ContentHorizontalPadding
        val ContentVertical: Dp = com.example.careerpilot.ui.theme.Dimens.ContentVerticalPadding
        val ContentTop: Dp = com.example.careerpilot.ui.theme.Dimens.ContentTopPadding
        val ContentBottom: Dp = com.example.careerpilot.ui.theme.Dimens.ContentBottomPadding
        val Section: Dp = com.example.careerpilot.ui.theme.Dimens.SectionSpacing
        val Item: Dp = com.example.careerpilot.ui.theme.Dimens.ItemSpacing
        val Group: Dp = com.example.careerpilot.ui.theme.Dimens.GroupSpacing
        val Inline: Dp = com.example.careerpilot.ui.theme.Dimens.InlineSpacing
    }

    object Shape {
        val RadiusNone: Dp = com.example.careerpilot.ui.theme.Dimens.RadiusNone
        val RadiusXs: Dp = com.example.careerpilot.ui.theme.Dimens.RadiusXs
        val RadiusSm: Dp = com.example.careerpilot.ui.theme.Dimens.RadiusSm
        val RadiusMd: Dp = com.example.careerpilot.ui.theme.Dimens.RadiusMd
        val RadiusLg: Dp = com.example.careerpilot.ui.theme.Dimens.RadiusLg
        val RadiusXl: Dp = com.example.careerpilot.ui.theme.Dimens.RadiusXl
        val RadiusFull: Dp = com.example.careerpilot.ui.theme.Dimens.RadiusFull
        val Button: Dp = com.example.careerpilot.ui.theme.Dimens.ButtonRadius
    }

    object Sizing {
        val ButtonHeight: Dp = com.example.careerpilot.ui.theme.Dimens.ButtonHeight
        val ButtonHeightSm: Dp = com.example.careerpilot.ui.theme.Dimens.ButtonHeightSm
        val ButtonHeightLg: Dp = com.example.careerpilot.ui.theme.Dimens.ButtonHeightLg
        val InputHeight: Dp = com.example.careerpilot.ui.theme.Dimens.InputHeight
        val InputHeightSm: Dp = com.example.careerpilot.ui.theme.Dimens.InputHeightSm
        val IconXs: Dp = com.example.careerpilot.ui.theme.Dimens.IconXs
        val IconSm: Dp = com.example.careerpilot.ui.theme.Dimens.IconSm
        val IconMd: Dp = com.example.careerpilot.ui.theme.Dimens.IconMd
        val IconLg: Dp = com.example.careerpilot.ui.theme.Dimens.IconLg
        val IconXl: Dp = com.example.careerpilot.ui.theme.Dimens.IconXl
        val TouchTargetMin: Dp = com.example.careerpilot.ui.theme.Dimens.TouchTargetMin
        val AvatarXs: Dp = com.example.careerpilot.ui.theme.Dimens.AvatarXs
        val AvatarSm: Dp = com.example.careerpilot.ui.theme.Dimens.AvatarSm
        val AvatarMd: Dp = com.example.careerpilot.ui.theme.Dimens.AvatarMd
        val AvatarLg: Dp = com.example.careerpilot.ui.theme.Dimens.AvatarLg
        val AvatarXl: Dp = com.example.careerpilot.ui.theme.Dimens.AvatarXl
        val CardPadding: Dp = com.example.careerpilot.ui.theme.Dimens.CardPadding
        val CardPaddingSm: Dp = com.example.careerpilot.ui.theme.Dimens.CardPaddingSm
        val CardBorderWidth: Dp = com.example.careerpilot.ui.theme.Dimens.CardBorderWidth
        val BadgePaddingHorizontal: Dp = com.example.careerpilot.ui.theme.Dimens.BadgePaddingHorizontal
        val BadgePaddingVertical: Dp = com.example.careerpilot.ui.theme.Dimens.BadgePaddingVertical
        val BadgeRadius: Dp = com.example.careerpilot.ui.theme.Dimens.BadgeRadius
        val DividerThickness: Dp = com.example.careerpilot.ui.theme.Dimens.DividerThickness
        val ElevationLow: Dp = com.example.careerpilot.ui.theme.Dimens.ElevationLow
        val ElevationMedium: Dp = com.example.careerpilot.ui.theme.Dimens.ElevationMedium
        val ElevationHigh: Dp = com.example.careerpilot.ui.theme.Dimens.ElevationHigh
    }

    // Token aliases for quick access
    val Colors = Colors
    val Typography = Typography
    val Spacing = Spacing
    val Shape = Shape
    val Sizing = Sizing
}
