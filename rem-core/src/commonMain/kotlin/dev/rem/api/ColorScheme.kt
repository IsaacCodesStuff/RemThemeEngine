package dev.rem.api

/**
 * Material 3 semantic colors generated for one theme mode.
 *
 * This framework-independent value model follows the current Material 3
 * `ColorScheme` role set. AndroidX has deprecated older constructor overloads
 * as new role groups were added; Rem does not model those constructors or
 * their binary-compatibility behavior.
 */
data class ColorScheme(
    val primary: Argb,
    val onPrimary: Argb,
    val primaryContainer: Argb,
    val onPrimaryContainer: Argb,
    val inversePrimary: Argb,
    val secondary: Argb,
    val onSecondary: Argb,
    val secondaryContainer: Argb,
    val onSecondaryContainer: Argb,
    val tertiary: Argb,
    val onTertiary: Argb,
    val tertiaryContainer: Argb,
    val onTertiaryContainer: Argb,
    val background: Argb,
    val onBackground: Argb,
    val surface: Argb,
    val onSurface: Argb,
    val surfaceVariant: Argb,
    val onSurfaceVariant: Argb,
    val surfaceTint: Argb,
    val inverseSurface: Argb,
    val inverseOnSurface: Argb,
    val error: Argb,
    val onError: Argb,
    val errorContainer: Argb,
    val onErrorContainer: Argb,
    val outline: Argb,
    val outlineVariant: Argb,
    val scrim: Argb,
    val surfaceBright: Argb,
    val surfaceDim: Argb,
    val surfaceContainer: Argb,
    val surfaceContainerHigh: Argb,
    val surfaceContainerHighest: Argb,
    val surfaceContainerLow: Argb,
    val surfaceContainerLowest: Argb,
    val primaryFixed: Argb,
    val primaryFixedDim: Argb,
    val onPrimaryFixed: Argb,
    val onPrimaryFixedVariant: Argb,
    val secondaryFixed: Argb,
    val secondaryFixedDim: Argb,
    val onSecondaryFixed: Argb,
    val onSecondaryFixedVariant: Argb,
    val tertiaryFixed: Argb,
    val tertiaryFixedDim: Argb,
    val onTertiaryFixed: Argb,
    val onTertiaryFixedVariant: Argb,
)
