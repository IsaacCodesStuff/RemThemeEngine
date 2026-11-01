package dev.rem.internal

import com.materialkolor.blend.Blend
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.dynamiccolor.DynamicScheme
import com.materialkolor.hct.Hct
import com.materialkolor.palettes.TonalPalette
import com.materialkolor.scheme.SchemeContent
import com.materialkolor.scheme.SchemeExpressive
import com.materialkolor.scheme.SchemeFidelity
import com.materialkolor.scheme.SchemeMonochrome
import com.materialkolor.scheme.SchemeTonalSpot
import com.materialkolor.scheme.SchemeVibrant
import dev.rem.api.Argb
import dev.rem.api.ColorScheme
import dev.rem.api.CustomColorDefinition
import dev.rem.api.CustomColorRoles
import dev.rem.api.CustomColorSchemes
import dev.rem.api.SchemeOptions
import dev.rem.api.ThemeMode
import dev.rem.api.ThemeSchemes
import dev.rem.api.ThemeStyle
import dev.rem.api.ThemeWithCustomColors

/** The only production boundary that depends on MCU types. */
internal object McuSchemeGenerator {
    fun createScheme(seed: Argb, mode: ThemeMode, options: SchemeOptions): ColorScheme {
        return createDynamicScheme(seed, mode, options).toRemColorScheme()
    }

    fun createTheme(
        seed: Argb,
        customColors: List<CustomColorDefinition>,
        options: SchemeOptions,
    ): ThemeWithCustomColors {
        val lightScheme = createDynamicScheme(seed, ThemeMode.LIGHT, options)
        val darkScheme = createDynamicScheme(seed, ThemeMode.DARK, options)
        val opaqueSeed = seed.value or OPAQUE_ALPHA
        val generatedCustomColors = linkedMapOf<String, CustomColorSchemes>()

        for (definition in customColors) {
            val opaqueSource = definition.source.value or OPAQUE_ALPHA
            val paletteSource = if (definition.harmonizeWithSeed) {
                Blend.harmonize(opaqueSource, opaqueSeed) or OPAQUE_ALPHA
            } else {
                opaqueSource
            }
            val palette = TonalPalette.fromInt(paletteSource)
            val customLightScheme = lightScheme.copy(primaryPalette = palette)
            val customDarkScheme = darkScheme.copy(primaryPalette = palette)

            generatedCustomColors[definition.id] = CustomColorSchemes(
                light = customLightScheme.toCustomColorRoles(),
                dark = customDarkScheme.toCustomColorRoles(),
            )
        }

        return ThemeWithCustomColors(
            schemes = ThemeSchemes(
                light = lightScheme.toRemColorScheme(),
                dark = darkScheme.toRemColorScheme(),
            ),
            customColors = generatedCustomColors,
        )
    }

    private fun createDynamicScheme(
        seed: Argb,
        mode: ThemeMode,
        options: SchemeOptions,
    ): DynamicScheme {
        val sourceColor = Hct.fromInt(seed.value or OPAQUE_ALPHA)
        return createMcuScheme(sourceColor, mode, options)
    }

    private fun DynamicScheme.toRemColorScheme(): ColorScheme = ColorScheme(
        primary = primary.toRemArgb(),
        onPrimary = onPrimary.toRemArgb(),
        primaryContainer = primaryContainer.toRemArgb(),
        onPrimaryContainer = onPrimaryContainer.toRemArgb(),
        inversePrimary = inversePrimary.toRemArgb(),
        secondary = secondary.toRemArgb(),
        onSecondary = onSecondary.toRemArgb(),
        secondaryContainer = secondaryContainer.toRemArgb(),
        onSecondaryContainer = onSecondaryContainer.toRemArgb(),
        tertiary = tertiary.toRemArgb(),
        onTertiary = onTertiary.toRemArgb(),
        tertiaryContainer = tertiaryContainer.toRemArgb(),
        onTertiaryContainer = onTertiaryContainer.toRemArgb(),
        background = background.toRemArgb(),
        onBackground = onBackground.toRemArgb(),
        surface = surface.toRemArgb(),
        onSurface = onSurface.toRemArgb(),
        surfaceVariant = surfaceVariant.toRemArgb(),
        onSurfaceVariant = onSurfaceVariant.toRemArgb(),
        surfaceTint = surfaceTint.toRemArgb(),
        inverseSurface = inverseSurface.toRemArgb(),
        inverseOnSurface = inverseOnSurface.toRemArgb(),
        error = error.toRemArgb(),
        onError = onError.toRemArgb(),
        errorContainer = errorContainer.toRemArgb(),
        onErrorContainer = onErrorContainer.toRemArgb(),
        outline = outline.toRemArgb(),
        outlineVariant = outlineVariant.toRemArgb(),
        scrim = scrim.toRemArgb(),
        surfaceBright = surfaceBright.toRemArgb(),
        surfaceDim = surfaceDim.toRemArgb(),
        surfaceContainer = surfaceContainer.toRemArgb(),
        surfaceContainerHigh = surfaceContainerHigh.toRemArgb(),
        surfaceContainerHighest = surfaceContainerHighest.toRemArgb(),
        surfaceContainerLow = surfaceContainerLow.toRemArgb(),
        surfaceContainerLowest = surfaceContainerLowest.toRemArgb(),
        primaryFixed = primaryFixed.toRemArgb(),
        primaryFixedDim = primaryFixedDim.toRemArgb(),
        onPrimaryFixed = onPrimaryFixed.toRemArgb(),
        onPrimaryFixedVariant = onPrimaryFixedVariant.toRemArgb(),
        secondaryFixed = secondaryFixed.toRemArgb(),
        secondaryFixedDim = secondaryFixedDim.toRemArgb(),
        onSecondaryFixed = onSecondaryFixed.toRemArgb(),
        onSecondaryFixedVariant = onSecondaryFixedVariant.toRemArgb(),
        tertiaryFixed = tertiaryFixed.toRemArgb(),
        tertiaryFixedDim = tertiaryFixedDim.toRemArgb(),
        onTertiaryFixed = onTertiaryFixed.toRemArgb(),
        onTertiaryFixedVariant = onTertiaryFixedVariant.toRemArgb(),
    )

    private fun DynamicScheme.toCustomColorRoles() = CustomColorRoles(
        color = primary.toRemArgb(),
        onColor = onPrimary.toRemArgb(),
        colorContainer = primaryContainer.toRemArgb(),
        onColorContainer = onPrimaryContainer.toRemArgb(),
    )

    private fun createMcuScheme(
        sourceColor: Hct,
        mode: ThemeMode,
        options: SchemeOptions,
    ): DynamicScheme {
        val isDark = mode == ThemeMode.DARK
        val contrast = options.contrastLevel
        val specVersion = ColorSpec.SpecVersion.SPEC_2025

        return when (options.style) {
            ThemeStyle.TONAL_SPOT -> SchemeTonalSpot(sourceColor, isDark, contrast, specVersion)
            ThemeStyle.FIDELITY -> SchemeFidelity(sourceColor, isDark, contrast, specVersion)
            ThemeStyle.CONTENT -> SchemeContent(sourceColor, isDark, contrast, specVersion)
            ThemeStyle.EXPRESSIVE -> SchemeExpressive(sourceColor, isDark, contrast, specVersion)
            ThemeStyle.MONOCHROME -> SchemeMonochrome(sourceColor, isDark, contrast, specVersion)
            ThemeStyle.VIBRANT -> SchemeVibrant(sourceColor, isDark, contrast, specVersion)
        }
    }

    private fun Int.toRemArgb(): Argb = Argb.fromInt(this)

    private const val OPAQUE_ALPHA = -0x1000000
}
