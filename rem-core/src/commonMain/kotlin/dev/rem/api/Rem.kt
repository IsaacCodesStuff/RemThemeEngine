package dev.rem.api

import dev.rem.internal.McuImageSeedExtractor
import dev.rem.internal.McuSchemeGenerator

/** Entry point for image seed extraction and dynamic color generation. */
object Rem {
    /**
     * Extracts a theme seed from decoded pixels encoded as `0xAARRGGBB`.
     *
     * When pixels come from an image, supply them in row-major order. Fully
     * transparent pixels are ignored; non-transparent pixels use their RGB
     * channels at full weight because MCU quantization does not composite or
     * weight alpha. Throws [IllegalArgumentException] for empty/all-transparent
     * input or when MCU scoring finds no eligible theme color.
     */
    fun extractSeedColor(pixels: IntArray): Argb = McuImageSeedExtractor.extractSeedColor(pixels)

    /** Generates one scheme in the explicitly selected [mode]. */
    fun createScheme(
        seed: Argb,
        mode: ThemeMode,
        options: SchemeOptions = SchemeOptions(),
    ): ColorScheme = McuSchemeGenerator.createScheme(seed, mode, options)

    /** Generates light and dark schemes with the same seed, style, and contrast level. */
    fun createSchemes(
        seed: Argb,
        options: SchemeOptions = SchemeOptions(),
    ): ThemeSchemes = ThemeSchemes(
        light = createScheme(seed, ThemeMode.LIGHT, options),
        dark = createScheme(seed, ThemeMode.DARK, options),
    )

    /**
     * Generates a light/dark theme with additional caller-defined color roles.
     *
     * Custom colors are indexed by their case-sensitive definition IDs. An empty [customColors]
     * list returns the regular light/dark schemes with an empty custom color map.
     */
    fun createTheme(
        seed: Argb,
        customColors: List<CustomColorDefinition>,
        options: SchemeOptions = SchemeOptions(),
    ): ThemeWithCustomColors {
        validateCustomColorDefinitions(customColors)
        return McuSchemeGenerator.createTheme(seed, customColors, options)
    }
}
