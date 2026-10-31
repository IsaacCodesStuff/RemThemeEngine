package dev.rem.api

import dev.rem.internal.McuSchemeGenerator

/** Entry point for seed-based dynamic color generation. */
object Rem {
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
}
