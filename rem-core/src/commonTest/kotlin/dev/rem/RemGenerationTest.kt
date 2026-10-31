package dev.rem

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
import dev.rem.api.Rem
import dev.rem.api.SchemeOptions
import dev.rem.api.ThemeMode
import dev.rem.api.ThemeStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RemGenerationTest {
    @Test
    fun everySupportedStyleAndModeMatchesMcuReference() {
        for (style in ThemeStyle.entries) {
            for (mode in ThemeMode.entries) {
                val options = SchemeOptions(style = style, contrastLevel = 0.35)
                val actual = Rem.createScheme(SEED, mode, options)
                val expected = referenceScheme(SEED, mode, options)

                assertEquals(expected.toRemColorScheme(), actual, "$style / $mode")
                assertOpaque(actual)
            }
        }
    }

    @Test
    fun pairedGenerationUsesOneConfigurationForBothModes() {
        val options = SchemeOptions(ThemeStyle.FIDELITY, contrastLevel = -0.4)
        val pair = Rem.createSchemes(SEED, options)

        assertEquals(Rem.createScheme(SEED, ThemeMode.LIGHT, options), pair.light)
        assertEquals(Rem.createScheme(SEED, ThemeMode.DARK, options), pair.dark)
        assertNotEquals(pair.light, pair.dark)
    }

    @Test
    fun argbUsesUnsignedAarrggbbRepresentation() {
        val color = Argb.fromLong(0xF012_3456L)

        assertEquals(0xF012_3456.toInt(), color.value)
        assertEquals(0xF012_3456L, color.toLong())
        assertEquals(Argb.fromInt(0xF012_3456.toInt()), color)
    }

    @Test
    fun seedAlphaIsIgnoredAndGeneratedRolesAreOpaque() {
        val translucentSeed = Argb.fromInt(0x1234_5678)
        val opaqueSeed = Argb.fromInt(0xFF34_5678.toInt())

        assertEquals(
            Rem.createScheme(opaqueSeed, ThemeMode.LIGHT),
            Rem.createScheme(translucentSeed, ThemeMode.LIGHT),
        )
        assertOpaque(Rem.createScheme(translucentSeed, ThemeMode.DARK))
    }

    @Test
    fun contrastAcceptsDocumentedInclusiveRangeAndRejectsInvalidValues() {
        assertEquals(-1.0, SchemeOptions(contrastLevel = -1.0).contrastLevel)
        assertEquals(1.0, SchemeOptions(contrastLevel = 1.0).contrastLevel)
        assertFailsWith<IllegalArgumentException> { SchemeOptions(contrastLevel = -1.0001) }
        assertFailsWith<IllegalArgumentException> { SchemeOptions(contrastLevel = 1.0001) }
        assertFailsWith<IllegalArgumentException> { SchemeOptions(contrastLevel = Double.NaN) }
        assertFailsWith<IllegalArgumentException> { SchemeOptions(contrastLevel = Double.POSITIVE_INFINITY) }
    }

    @Test
    fun longArgbFactoryRejectsValuesOutsideUnsigned32BitRange() {
        assertFailsWith<IllegalArgumentException> { Argb.fromLong(-1L) }
        assertFailsWith<IllegalArgumentException> { Argb.fromLong(0x1_0000_0000L) }
    }

    @Test
    fun tonalSpotHasStableRepresentative2025Outputs() {
        val actual = Rem.createScheme(SEED, ThemeMode.LIGHT)

        // Golden values from MCU 6.0.0, SPEC_2025, seed 0xFF6750A4.
        assertEquals(0xFF65_5789.toInt(), actual.primary.value)
        assertEquals(0xFFFD_F7FF.toInt(), actual.onPrimary.value)
        assertEquals(0xFFFD_F7FE.toInt(), actual.surface.value)
    }

    private fun referenceScheme(seed: Argb, mode: ThemeMode, options: SchemeOptions): DynamicScheme {
        val source = Hct.fromInt(seed.value or OPAQUE_ALPHA)
        val isDark = mode == ThemeMode.DARK
        val contrast = options.contrastLevel
        val spec = ColorSpec.SpecVersion.SPEC_2025
        return when (options.style) {
            ThemeStyle.TONAL_SPOT -> SchemeTonalSpot(source, isDark, contrast, spec)
            ThemeStyle.FIDELITY -> SchemeFidelity(source, isDark, contrast, spec)
            ThemeStyle.CONTENT -> SchemeContent(source, isDark, contrast, spec)
            ThemeStyle.EXPRESSIVE -> SchemeExpressive(source, isDark, contrast, spec)
            ThemeStyle.MONOCHROME -> SchemeMonochrome(source, isDark, contrast, spec)
            ThemeStyle.VIBRANT -> SchemeVibrant(source, isDark, contrast, spec)
        }
    }

    private fun DynamicScheme.toRemColorScheme() = ColorScheme(
        primary = Argb.fromInt(primary),
        onPrimary = Argb.fromInt(onPrimary),
        primaryContainer = Argb.fromInt(primaryContainer),
        onPrimaryContainer = Argb.fromInt(onPrimaryContainer),
        inversePrimary = Argb.fromInt(inversePrimary),
        secondary = Argb.fromInt(secondary),
        onSecondary = Argb.fromInt(onSecondary),
        secondaryContainer = Argb.fromInt(secondaryContainer),
        onSecondaryContainer = Argb.fromInt(onSecondaryContainer),
        tertiary = Argb.fromInt(tertiary),
        onTertiary = Argb.fromInt(onTertiary),
        tertiaryContainer = Argb.fromInt(tertiaryContainer),
        onTertiaryContainer = Argb.fromInt(onTertiaryContainer),
        background = Argb.fromInt(background),
        onBackground = Argb.fromInt(onBackground),
        surface = Argb.fromInt(surface),
        onSurface = Argb.fromInt(onSurface),
        surfaceVariant = Argb.fromInt(surfaceVariant),
        onSurfaceVariant = Argb.fromInt(onSurfaceVariant),
        surfaceTint = Argb.fromInt(surfaceTint),
        inverseSurface = Argb.fromInt(inverseSurface),
        inverseOnSurface = Argb.fromInt(inverseOnSurface),
        error = Argb.fromInt(error),
        onError = Argb.fromInt(onError),
        errorContainer = Argb.fromInt(errorContainer),
        onErrorContainer = Argb.fromInt(onErrorContainer),
        outline = Argb.fromInt(outline),
        outlineVariant = Argb.fromInt(outlineVariant),
        scrim = Argb.fromInt(scrim),
        surfaceBright = Argb.fromInt(surfaceBright),
        surfaceDim = Argb.fromInt(surfaceDim),
        surfaceContainer = Argb.fromInt(surfaceContainer),
        surfaceContainerHigh = Argb.fromInt(surfaceContainerHigh),
        surfaceContainerHighest = Argb.fromInt(surfaceContainerHighest),
        surfaceContainerLow = Argb.fromInt(surfaceContainerLow),
        surfaceContainerLowest = Argb.fromInt(surfaceContainerLowest),
        primaryFixed = Argb.fromInt(primaryFixed),
        primaryFixedDim = Argb.fromInt(primaryFixedDim),
        onPrimaryFixed = Argb.fromInt(onPrimaryFixed),
        onPrimaryFixedVariant = Argb.fromInt(onPrimaryFixedVariant),
        secondaryFixed = Argb.fromInt(secondaryFixed),
        secondaryFixedDim = Argb.fromInt(secondaryFixedDim),
        onSecondaryFixed = Argb.fromInt(onSecondaryFixed),
        onSecondaryFixedVariant = Argb.fromInt(onSecondaryFixedVariant),
        tertiaryFixed = Argb.fromInt(tertiaryFixed),
        tertiaryFixedDim = Argb.fromInt(tertiaryFixedDim),
        onTertiaryFixed = Argb.fromInt(onTertiaryFixed),
        onTertiaryFixedVariant = Argb.fromInt(onTertiaryFixedVariant),
    )

    private fun assertOpaque(scheme: ColorScheme) {
        val values = listOf(
            scheme.primary, scheme.onPrimary, scheme.primaryContainer, scheme.onPrimaryContainer,
            scheme.inversePrimary,
            scheme.secondary, scheme.onSecondary, scheme.secondaryContainer, scheme.onSecondaryContainer,
            scheme.tertiary, scheme.onTertiary, scheme.tertiaryContainer, scheme.onTertiaryContainer,
            scheme.background, scheme.onBackground, scheme.surface, scheme.onSurface,
            scheme.surfaceVariant, scheme.onSurfaceVariant, scheme.surfaceTint,
            scheme.inverseSurface, scheme.inverseOnSurface,
            scheme.error, scheme.onError, scheme.errorContainer, scheme.onErrorContainer,
            scheme.outline, scheme.outlineVariant, scheme.scrim,
            scheme.surfaceBright, scheme.surfaceDim, scheme.surfaceContainer,
            scheme.surfaceContainerHigh, scheme.surfaceContainerHighest,
            scheme.surfaceContainerLow, scheme.surfaceContainerLowest,
            scheme.primaryFixed, scheme.primaryFixedDim, scheme.onPrimaryFixed,
            scheme.onPrimaryFixedVariant, scheme.secondaryFixed, scheme.secondaryFixedDim,
            scheme.onSecondaryFixed, scheme.onSecondaryFixedVariant,
            scheme.tertiaryFixed, scheme.tertiaryFixedDim, scheme.onTertiaryFixed,
            scheme.onTertiaryFixedVariant,
        )
        assertTrue(values.all { it.value ushr 24 == 0xFF })
    }

    private companion object {
        val SEED = Argb.fromInt(0xFF6750A4.toInt())
        const val OPAQUE_ALPHA = -0x1000000
    }
}
