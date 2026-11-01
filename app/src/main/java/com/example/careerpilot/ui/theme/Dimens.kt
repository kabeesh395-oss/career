package com.example.careerpilot.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * CareerPilot Design Tokens - Spacing, Corner Radii, Sizing
 * 4px base unit, consistent scale
 * All UI dimensions must reference these tokens.
 */
object Dimens {

    // ── Spacing Scale (4px base) ────────────────────────────────────
    val Space0: Dp = 0.dp
    val Space1: Dp = 4.dp
    val Space2: Dp = 8.dp
    val Space3: Dp = 12.dp
    val Space4: Dp = 16.dp
    val Space5: Dp = 20.dp
    val Space6: Dp = 24.dp
    val Space8: Dp = 32.dp
    val Space10: Dp = 40.dp
    val Space12: Dp = 48.dp
    val Space16: Dp = 64.dp

    // ── Backward-compatible aliases (old screen references) ──────────
    val screenHorizontal: Dp = Space4
    val screenTop: Dp = Space4
    val screenBottom: Dp = Space12
    val sectionSpacing: Dp = Space4
    val itemSpacing: Dp = Space3
    val SpaceLg: Dp = Space4
    val SpaceMd: Dp = Space3
    val SpaceSm: Dp = Space2
    val SpaceXs: Dp = Space1
    val SpaceXl: Dp = Space5
    val SpaceXxl: Dp = Space6
    val SpaceXxxl: Dp = Space8
    val SpaceHuge: Dp = Space12

    // ── Semantic Spacing ────────────────────────────────────────────
    val ContentHorizontalPadding: Dp = Space4
    val ContentVerticalPadding: Dp = Space4
    val ContentTopPadding: Dp = Space4
    val ContentBottomPadding: Dp = Space12
    val SectionSpacing: Dp = Space6
    val ItemSpacing: Dp = Space3
    val GroupSpacing: Dp = Space4
    val InlineSpacing: Dp = Space2

    // ── Corner Radii ────────────────────────────────────────────────
    val RadiusNone: Dp = Space0
    val RadiusXs: Dp = 4.dp
    val RadiusSm: Dp = 6.dp
    val RadiusMd: Dp = 8.dp
    val RadiusLg: Dp = 12.dp
    val RadiusXl: Dp = 16.dp
    val RadiusFull: Dp = 999.dp

    // Button radius matches input radius for visual harmony
    val ButtonRadius: Dp = RadiusSm

    // ── Component Sizing ────────────────────────────────────────────
    val ButtonHeight: Dp = 40.dp
    val ButtonHeightSm: Dp = 32.dp
    val ButtonHeightLg: Dp = 48.dp

    val InputHeight: Dp = 40.dp
    val InputHeightSm: Dp = 32.dp

    val IconXs: Dp = 12.dp
    val IconSm: Dp = 16.dp
    val IconMd: Dp = 20.dp
    val IconLg: Dp = 24.dp
    val IconXl: Dp = 32.dp

    val TouchTargetMin: Dp = 44.dp

    val AvatarXs: Dp = 24.dp
    val AvatarSm: Dp = 32.dp
    val AvatarMd: Dp = 40.dp
    val AvatarLg: Dp = 48.dp
    val AvatarXl: Dp = 64.dp

    // ── Card & Container ────────────────────────────────────────────
    val CardPadding: Dp = Space4
    val CardPaddingSm: Dp = Space3
    val CardBorderWidth: Dp = 1.dp

    // ── Badge ───────────────────────────────────────────────────────
    val BadgePaddingHorizontal: Dp = Space2
    val BadgePaddingVertical: Dp = Space0
    val BadgeRadius: Dp = RadiusXs

    // ── Divider ─────────────────────────────────────────────────────
    val DividerThickness: Dp = 1.dp

    // ── Shadow/Elevation (subtle, single level) ─────────────────────
    // Use SurfaceElevated color instead of shadows where possible
    val ElevationLow: Dp = 1.dp
    val ElevationMedium: Dp = 4.dp
    val ElevationHigh: Dp = 8.dp
}
