package dev.rem.compose

import androidx.compose.material3.ColorScheme as Material3ColorScheme
import androidx.compose.ui.graphics.Color
import dev.rem.api.Argb
import dev.rem.api.ColorScheme as RemColorScheme

/** Converts Rem's 48-role scheme to the corresponding Compose Material 3 scheme. */
fun RemColorScheme.toMaterial3ColorScheme(): Material3ColorScheme = Material3ColorScheme(
    primary = primary.toComposeColor(),
    onPrimary = onPrimary.toComposeColor(),
    primaryContainer = primaryContainer.toComposeColor(),
    onPrimaryContainer = onPrimaryContainer.toComposeColor(),
    inversePrimary = inversePrimary.toComposeColor(),
    secondary = secondary.toComposeColor(),
    onSecondary = onSecondary.toComposeColor(),
    secondaryContainer = secondaryContainer.toComposeColor(),
    onSecondaryContainer = onSecondaryContainer.toComposeColor(),
    tertiary = tertiary.toComposeColor(),
    onTertiary = onTertiary.toComposeColor(),
    tertiaryContainer = tertiaryContainer.toComposeColor(),
    onTertiaryContainer = onTertiaryContainer.toComposeColor(),
    background = background.toComposeColor(),
    onBackground = onBackground.toComposeColor(),
    surface = surface.toComposeColor(),
    onSurface = onSurface.toComposeColor(),
    surfaceVariant = surfaceVariant.toComposeColor(),
    onSurfaceVariant = onSurfaceVariant.toComposeColor(),
    surfaceTint = surfaceTint.toComposeColor(),
    inverseSurface = inverseSurface.toComposeColor(),
    inverseOnSurface = inverseOnSurface.toComposeColor(),
    error = error.toComposeColor(),
    onError = onError.toComposeColor(),
    errorContainer = errorContainer.toComposeColor(),
    onErrorContainer = onErrorContainer.toComposeColor(),
    outline = outline.toComposeColor(),
    outlineVariant = outlineVariant.toComposeColor(),
    scrim = scrim.toComposeColor(),
    surfaceBright = surfaceBright.toComposeColor(),
    surfaceDim = surfaceDim.toComposeColor(),
    surfaceContainer = surfaceContainer.toComposeColor(),
    surfaceContainerHigh = surfaceContainerHigh.toComposeColor(),
    surfaceContainerHighest = surfaceContainerHighest.toComposeColor(),
    surfaceContainerLow = surfaceContainerLow.toComposeColor(),
    surfaceContainerLowest = surfaceContainerLowest.toComposeColor(),
    primaryFixed = primaryFixed.toComposeColor(),
    primaryFixedDim = primaryFixedDim.toComposeColor(),
    onPrimaryFixed = onPrimaryFixed.toComposeColor(),
    onPrimaryFixedVariant = onPrimaryFixedVariant.toComposeColor(),
    secondaryFixed = secondaryFixed.toComposeColor(),
    secondaryFixedDim = secondaryFixedDim.toComposeColor(),
    onSecondaryFixed = onSecondaryFixed.toComposeColor(),
    onSecondaryFixedVariant = onSecondaryFixedVariant.toComposeColor(),
    tertiaryFixed = tertiaryFixed.toComposeColor(),
    tertiaryFixedDim = tertiaryFixedDim.toComposeColor(),
    onTertiaryFixed = onTertiaryFixed.toComposeColor(),
    onTertiaryFixedVariant = onTertiaryFixedVariant.toComposeColor(),
)

private fun Argb.toComposeColor(): Color = Color(value)
