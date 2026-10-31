package dev.rem

import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.dynamiccolor.DynamicScheme
import com.materialkolor.hct.Hct
import com.materialkolor.quantize.QuantizerCelebi
import com.materialkolor.scheme.SchemeContent
import com.materialkolor.scheme.SchemeExpressive
import com.materialkolor.scheme.SchemeFidelity
import com.materialkolor.scheme.SchemeMonochrome
import com.materialkolor.scheme.SchemeTonalSpot
import com.materialkolor.scheme.SchemeVibrant
import com.materialkolor.score.Score
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
    fun colorfulPixelCollectionMatchesMcuQuantizationAndScoring() {
        val pixels = colorfulPixels()

        assertEquals(referenceSeed(pixels), Rem.extractSeedColor(pixels))
    }

    @Test
    fun singleColorAndMultipleDistinctColorsMatchMcuReference() {
        val singleColor = IntArray(64) { 0xFF67_50A4.toInt() }
        val distinctColors = intArrayOf(
            *IntArray(40) { 0xFFE9_1E63.toInt() },
            *IntArray(30) { 0xFF00_9688.toInt() },
            *IntArray(20) { 0xFFFF_C107.toInt() },
        )

        assertEquals(referenceSeed(singleColor), Rem.extractSeedColor(singleColor))
        assertEquals(referenceSeed(distinctColors), Rem.extractSeedColor(distinctColors))
    }

    @Test
    fun fullyTransparentRgbDoesNotInfluenceSeed() {
        val visiblePixels = IntArray(24) { 0xFF00_9688.toInt() }
        val withTransparentPixels = IntArray(visiblePixels.size + 200) { index ->
            if (index < 200) 0x00FF_0000 else visiblePixels[index - 200]
        }

        assertEquals(Rem.extractSeedColor(visiblePixels), Rem.extractSeedColor(withTransparentPixels))
        assertEquals(referenceSeed(withTransparentPixels), Rem.extractSeedColor(withTransparentPixels))
    }

    @Test
    fun partialAlphaUsesRgbAtFullWeightAfterOpaqueNormalization() {
        val partiallyTransparent = intArrayOf(
            *IntArray(40) { 0x01FF_0000 },
            *IntArray(40) { 0x10FF_0000 },
            *IntArray(50) { 0xFF00_00FF.toInt() },
        )
        val fullWeightRgb = intArrayOf(
            *IntArray(80) { 0xFFFF_0000.toInt() },
            *IntArray(50) { 0xFF00_00FF.toInt() },
        )
        // This lower red population is a comparison fixture for alpha-weighted treatment
        // ((40 * 1 + 40 * 16) / 255 rounds to about 3); it is not another Rem implementation.
        val alphaWeightedComparison = intArrayOf(
            *IntArray(3) { 0xFFFF_0000.toInt() },
            *IntArray(50) { 0xFF00_00FF.toInt() },
        )

        val actual = Rem.extractSeedColor(partiallyTransparent)
        assertEquals(referenceSeed(fullWeightRgb), actual)
        assertNotEquals(referenceSeed(alphaWeightedComparison), actual)
    }

    @Test
    fun imageExtractionDoesNotModifyCallerPixels() {
        val pixels = intArrayOf(
            *colorfulPixels(),
            0x10FF_0000,
            0x8000_FF00.toInt(),
            0x0000_00FF,
        )
        val snapshot = pixels.copyOf()

        Rem.extractSeedColor(pixels)

        assertTrue(pixels.contentEquals(snapshot))
    }

    @Test
    fun emptyTransparentAndUnscorablePixelsFailPredictably() {
        assertFailsWith<IllegalArgumentException> { Rem.extractSeedColor(intArrayOf()) }
        assertFailsWith<IllegalArgumentException> {
            Rem.extractSeedColor(intArrayOf(0x00FF_0000, 0x0000_FF00))
        }
        assertFailsWith<IllegalArgumentException> {
            Rem.extractSeedColor(IntArray(32) { 0xFF77_7777.toInt() })
        }
    }

    @Test
    fun extractionIsDeterministicAndWorksWithPairedSchemeGeneration() {
        val pixels = colorfulPixels()
        val seed = Rem.extractSeedColor(pixels)

        assertEquals(seed, Rem.extractSeedColor(pixels.copyOf()))
        val schemes = Rem.createSchemes(seed)
        assertEquals(Rem.createScheme(seed, ThemeMode.LIGHT), schemes.light)
        assertEquals(Rem.createScheme(seed, ThemeMode.DARK), schemes.dark)
    }

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
    fun argbFactoriesRoundTripUnsignedBoundaryBitPatterns() {
        val boundaries = longArrayOf(
            0x0000_0000L,
            0xFFFF_FFFFL,
            0x8000_0000L,
            0x7FFF_FFFFL,
        )

        for (value in boundaries) {
            assertEquals(value, Argb.fromLong(value).toLong(), "fromLong: 0x${value.toString(16)}")
            assertEquals(value, Argb.fromInt(value.toInt()).toLong(), "fromInt: 0x${value.toString(16)}")
        }
        assertTrue(Argb.fromLong(0x8000_0000L).value < 0)
        assertTrue(Argb.fromLong(0x7FFF_FFFFL).value > 0)
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
        assertFailsWith<IllegalArgumentException> { SchemeOptions(contrastLevel = Double.NEGATIVE_INFINITY) }
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

    private fun referenceSeed(pixels: IntArray): Argb {
        val opaqueVisiblePixels = pixels
            .filter { it ushr 24 != 0 }
            .map { it or OPAQUE_ALPHA }
            .toIntArray()
        val quantized = QuantizerCelebi.quantize(opaqueVisiblePixels, 128)
        val color = Score.score(quantized, desired = 1, fallbackColorArgb = null).firstOrNull()
        return Argb.fromInt(requireNotNull(color))
    }

    private fun colorfulPixels(): IntArray = intArrayOf(
        *IntArray(90) { 0xFFE9_1E63.toInt() },
        *IntArray(70) { 0xFF00_9688.toInt() },
        *IntArray(40) { 0xFFFF_C107.toInt() },
        *IntArray(25) { 0xFF3F_51B5.toInt() },
    )

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
