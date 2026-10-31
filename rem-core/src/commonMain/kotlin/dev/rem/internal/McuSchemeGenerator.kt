package dev.rem.internal

import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.dynamiccolor.DynamicScheme
import com.materialkolor.hct.Hct
import com.materialkolor.scheme.SchemeContent
import com.materialkolor.scheme.SchemeExpressive
import com.materialkolor.scheme.SchemeFidelity
import com.materialkolor.scheme.SchemeMonochrome
import com.materialkolor.scheme.SchemeTonalSpot
import com.materialkolor.scheme.SchemeVibrant
import dev.rem.api.Argb
import dev.rem.api.ColorScheme
import dev.rem.api.SchemeOptions
import dev.rem.api.ThemeMode
import dev.rem.api.ThemeStyle

/** The only production boundary that depends on MCU types. */
internal object McuSchemeGenerator {
    fun createScheme(seed: Argb, mode: ThemeMode, options: SchemeOptions): ColorScheme {
        val opaqueSeed = seed.value or OPAQUE_ALPHA
        val sourceColor = Hct.fromInt(opaqueSeed)
        val scheme = createMcuScheme(sourceColor, mode, options)

        return ColorScheme(
            primary = scheme.primary.toRemArgb(),
            onPrimary = scheme.onPrimary.toRemArgb(),
            primaryContainer = scheme.primaryContainer.toRemArgb(),
            onPrimaryContainer = scheme.onPrimaryContainer.toRemArgb(),
            inversePrimary = scheme.inversePrimary.toRemArgb(),
            secondary = scheme.secondary.toRemArgb(),
            onSecondary = scheme.onSecondary.toRemArgb(),
            secondaryContainer = scheme.secondaryContainer.toRemArgb(),
            onSecondaryContainer = scheme.onSecondaryContainer.toRemArgb(),
            tertiary = scheme.tertiary.toRemArgb(),
            onTertiary = scheme.onTertiary.toRemArgb(),
            tertiaryContainer = scheme.tertiaryContainer.toRemArgb(),
            onTertiaryContainer = scheme.onTertiaryContainer.toRemArgb(),
            background = scheme.background.toRemArgb(),
            onBackground = scheme.onBackground.toRemArgb(),
            surface = scheme.surface.toRemArgb(),
            onSurface = scheme.onSurface.toRemArgb(),
            surfaceVariant = scheme.surfaceVariant.toRemArgb(),
            onSurfaceVariant = scheme.onSurfaceVariant.toRemArgb(),
            surfaceTint = scheme.surfaceTint.toRemArgb(),
            inverseSurface = scheme.inverseSurface.toRemArgb(),
            inverseOnSurface = scheme.inverseOnSurface.toRemArgb(),
            error = scheme.error.toRemArgb(),
            onError = scheme.onError.toRemArgb(),
            errorContainer = scheme.errorContainer.toRemArgb(),
            onErrorContainer = scheme.onErrorContainer.toRemArgb(),
            outline = scheme.outline.toRemArgb(),
            outlineVariant = scheme.outlineVariant.toRemArgb(),
            scrim = scheme.scrim.toRemArgb(),
            surfaceBright = scheme.surfaceBright.toRemArgb(),
            surfaceDim = scheme.surfaceDim.toRemArgb(),
            surfaceContainer = scheme.surfaceContainer.toRemArgb(),
            surfaceContainerHigh = scheme.surfaceContainerHigh.toRemArgb(),
            surfaceContainerHighest = scheme.surfaceContainerHighest.toRemArgb(),
            surfaceContainerLow = scheme.surfaceContainerLow.toRemArgb(),
            surfaceContainerLowest = scheme.surfaceContainerLowest.toRemArgb(),
            primaryFixed = scheme.primaryFixed.toRemArgb(),
            primaryFixedDim = scheme.primaryFixedDim.toRemArgb(),
            onPrimaryFixed = scheme.onPrimaryFixed.toRemArgb(),
            onPrimaryFixedVariant = scheme.onPrimaryFixedVariant.toRemArgb(),
            secondaryFixed = scheme.secondaryFixed.toRemArgb(),
            secondaryFixedDim = scheme.secondaryFixedDim.toRemArgb(),
            onSecondaryFixed = scheme.onSecondaryFixed.toRemArgb(),
            onSecondaryFixedVariant = scheme.onSecondaryFixedVariant.toRemArgb(),
            tertiaryFixed = scheme.tertiaryFixed.toRemArgb(),
            tertiaryFixedDim = scheme.tertiaryFixedDim.toRemArgb(),
            onTertiaryFixed = scheme.onTertiaryFixed.toRemArgb(),
            onTertiaryFixedVariant = scheme.onTertiaryFixedVariant.toRemArgb(),
        )
    }

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
