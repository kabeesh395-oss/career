package com.example.careerpilot.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * CareerHub Design Tokens — Spacing, Corner Radii, Sizing
 *
 * All UI dimensions must reference these tokens.
 * Do not use arbitrary dp values in screen composables.
 */
object Dimens {

    // ── Spacing Scale ─────────────────────────────────────────────
    val SpaceXxs: Dp = 2.dp
    val SpaceXs: Dp = 4.dp
    val SpaceSm: Dp = 8.dp
    val SpaceMd: Dp = 12.dp
    val SpaceLg: Dp = 16.dp
    val SpaceXl: Dp = 20.dp
    val SpaceXxl: Dp = 24.dp
    val SpaceXxxl: Dp = 32.dp
    val SpaceHuge: Dp = 48.dp

    // ── Content Layout ────────────────────────────────────────────
    /** Standard horizontal padding for screen content. */
    val ContentHorizontalPadding: Dp = 16.dp

    /** Vertical padding at top of scrollable content. */
    val ContentTopPadding: Dp = 16.dp

    /** Bottom padding to clear bottom navigation. */
    val ContentBottomPadding: Dp = 96.dp

    /** Spacing between sections in a LazyColumn. */
    val SectionSpacing: Dp = 16.dp

    /** Spacing between items within a section. */
    val ItemSpacing: Dp = 12.dp

    // ── Corner Radii ──────────────────────────────────────────────
    val RadiusSm: Dp = 8.dp
    val RadiusMd: Dp = 12.dp
    val RadiusLg: Dp = 16.dp
    val RadiusFull: Dp = 100.dp  // pill/circle

    // ── Component Sizing ──────────────────────────────────────────
    val ButtonHeight: Dp = 48.dp
    val ButtonHeightSmall: Dp = 36.dp

    val IconSm: Dp = 16.dp
    val IconMd: Dp = 20.dp
    val IconLg: Dp = 24.dp
    val IconXl: Dp = 32.dp

    val TouchTargetMin: Dp = 48.dp

    val AvatarSm: Dp = 32.dp
    val AvatarMd: Dp = 40.dp
    val AvatarLg: Dp = 56.dp

    // ── Card Sizing ───────────────────────────────────────────────
    val CardPadding: Dp = 16.dp
    val CardBorderWidth: Dp = 1.dp

    // ── Badge ─────────────────────────────────────────────────────
    val BadgePaddingHorizontal: Dp = 8.dp
    val BadgePaddingVertical: Dp = 4.dp
    val BadgeRadius: Dp = 6.dp
}
