package dev.rem

import com.materialkolor.blend.Blend
import com.materialkolor.contrast.Contrast
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
import dev.rem.api.CustomColorDefinition
import dev.rem.api.CustomColorRoles
import dev.rem.api.CustomColorSchemes
import dev.rem.api.Rem
import dev.rem.api.SchemeOptions
import dev.rem.api.ThemeMode
import dev.rem.api.ThemeStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CustomColorGenerationTest {
    @Test
    fun customRolesMatchMcuReferencesForAllStylesModesAndContrastLevels() {
        val definition = CustomColorDefinition("accent", CUSTOM_SOURCE)
        val contrastLevels = listOf(-1.0, 0.0, 1.0)

        for (style in ThemeStyle.entries) {
            for (contrastLevel in contrastLevels) {
                val options = SchemeOptions(style = style, contrastLevel = contrastLevel)
                val actual = Rem.createTheme(SEED, listOf(definition), options)

                assertEquals(Rem.createSchemes(SEED, options), actual.schemes, "$style / $contrastLevel")
                for (mode in ThemeMode.entries) {
                    val actualRoles = actual.customColors.getValue("accent").forMode(mode)
                    val expectedRoles = referenceCustomColorRoles(
                        seed = SEED,
                        definition = definition,
                        mode = mode,
                        options = options,
                    )

                    assertEquals(expectedRoles, actualRoles, "$style / $mode / $contrastLevel")
                    assertRolesOpaque(actualRoles)

                    assertEquals(
                        roleContrast(expectedRoles.onColor, expectedRoles.color),
                        roleContrast(actualRoles.onColor, actualRoles.color),
                        0.000001,
                        "onColor / color contrast: $style / $mode / $contrastLevel",
                    )
                    assertEquals(
                        roleContrast(expectedRoles.onColorContainer, expectedRoles.colorContainer),
                        roleContrast(actualRoles.onColorContainer, actualRoles.colorContainer),
                        0.000001,
                        "onColorContainer / colorContainer contrast: $style / $mode / $contrastLevel",
                    )
                }
            }
        }
    }

    @Test
    fun customForegroundRolesRespondToContrastLevel() {
        val definition = CustomColorDefinition("accent", CUSTOM_SOURCE)

        for (mode in ThemeMode.entries) {
            val lowContrast = Rem.createTheme(
                SEED,
                listOf(definition),
                SchemeOptions(ThemeStyle.TONAL_SPOT, contrastLevel = -1.0),
            ).customColors.getValue("accent").forMode(mode)
            val highContrast = Rem.createTheme(
                SEED,
                listOf(definition),
                SchemeOptions(ThemeStyle.TONAL_SPOT, contrastLevel = 1.0),
            ).customColors.getValue("accent").forMode(mode)

            assertNotEquals(lowContrast.onColor, highContrast.onColor, "$mode onColor")
            assertNotEquals(lowContrast.onColorContainer, highContrast.onColorContainer, "$mode onColorContainer")
        }
    }

    @Test
    fun harmonizationUsesOriginalSeedOnceBeforePaletteCreation() {
        val definition = CustomColorDefinition(
            id = "harmonized",
            source = TRANSLUCENT_CUSTOM_SOURCE,
            harmonizeWithSeed = true,
        )
        val options = SchemeOptions(style = ThemeStyle.TONAL_SPOT, contrastLevel = 0.0)
        val actual = Rem.createTheme(TRANSLUCENT_SEED, listOf(definition), options)

        val expected = referenceCustomColorRoles(
            seed = TRANSLUCENT_SEED,
            definition = definition,
            mode = ThemeMode.LIGHT,
            options = options,
        )
        val unharmonized = referenceCustomColorRoles(
            seed = TRANSLUCENT_SEED,
            definition = definition.copy(harmonizeWithSeed = false),
            mode = ThemeMode.LIGHT,
            options = options,
        )
        val wrongDirection = referenceCustomColorRoles(
            seed = TRANSLUCENT_SEED,
            definition = definition,
            mode = ThemeMode.LIGHT,
            options = options,
            reverseHarmonizationDirection = true,
        )

        assertEquals(expected, actual.customColors.getValue("harmonized").light)
        assertNotEquals(unharmonized, expected)
        assertNotEquals(wrongDirection, expected)
        assertEquals(
            referenceCustomColorRoles(TRANSLUCENT_SEED, definition, ThemeMode.DARK, options),
            actual.customColors.getValue("harmonized").dark,
        )
    }

    @Test
    fun customSourceAlphaIsIgnoredAndAllGeneratedRolesAreOpaque() {
        val colorsWithSameRgb = listOf(
            CustomColorDefinition("alpha-low", Argb.fromLong(0x0134_5678L)),
            CustomColorDefinition("alpha-high", Argb.fromLong(0xFE34_5678L)),
            CustomColorDefinition("opaque", Argb.fromLong(0xFF34_5678L)),
        )
        val actual = Rem.createTheme(SEED, colorsWithSameRgb)

        val expectedRoles = actual.customColors.getValue("opaque")
        assertEquals(expectedRoles, actual.customColors.getValue("alpha-low"))
        assertEquals(expectedRoles, actual.customColors.getValue("alpha-high"))
        assertRolesOpaque(expectedRoles.light)
        assertRolesOpaque(expectedRoles.dark)
    }

    @Test
    fun multipleDefinitionsPreserveIdsOrderAndIndependentExpectedRoles() {
        val definitions = listOf(
            CustomColorDefinition("success", Argb.fromLong(0xFF2E_7D32L)),
            CustomColorDefinition("Success", Argb.fromLong(0xFF2E_7D32L)),
            CustomColorDefinition("warning", Argb.fromLong(0xFFFF_8F00L), harmonizeWithSeed = true),
        )
        val options = SchemeOptions(style = ThemeStyle.EXPRESSIVE, contrastLevel = 0.4)
        val actual = Rem.createTheme(SEED, definitions, options)

        assertEquals(listOf("success", "Success", "warning"), actual.customColors.keys.toList())
        for (definition in definitions) {
            val actualSchemes = actual.customColors.getValue(definition.id)
            assertEquals(
                referenceCustomColorRoles(SEED, definition, ThemeMode.LIGHT, options),
                actualSchemes.light,
                "${definition.id} light",
            )
            assertEquals(
                referenceCustomColorRoles(SEED, definition, ThemeMode.DARK, options),
                actualSchemes.dark,
                "${definition.id} dark",
            )
        }
        assertEquals(actual.customColors.getValue("success"), actual.customColors.getValue("Success"))
    }

    @Test
    fun repeatedCustomThemeGenerationIsDeterministic() {
        val definitions = listOf(
            CustomColorDefinition("success", Argb.fromLong(0xFF2E_7D32L), harmonizeWithSeed = true),
            CustomColorDefinition("warning", Argb.fromLong(0xFFFF_8F00L)),
        )
        val options = SchemeOptions(style = ThemeStyle.FIDELITY, contrastLevel = -0.65)

        assertEquals(
            Rem.createTheme(SEED, definitions, options),
            Rem.createTheme(SEED, definitions.toList(), options),
        )
    }

    private fun referenceCustomColorRoles(
        seed: Argb,
        definition: CustomColorDefinition,
        mode: ThemeMode,
        options: SchemeOptions,
        reverseHarmonizationDirection: Boolean = false,
    ): CustomColorRoles {
        val opaqueSeed = seed.value or OPAQUE_ALPHA
        val opaqueCustomSource = definition.source.value or OPAQUE_ALPHA
        val paletteSource = if (!definition.harmonizeWithSeed) {
            opaqueCustomSource
        } else if (reverseHarmonizationDirection) {
            Blend.harmonize(opaqueSeed, opaqueCustomSource) or OPAQUE_ALPHA
        } else {
            Blend.harmonize(opaqueCustomSource, opaqueSeed) or OPAQUE_ALPHA
        }
        val customPalette = TonalPalette.fromInt(paletteSource)
        val customScheme = referenceScheme(seed, mode, options).copy(primaryPalette = customPalette)

        return CustomColorRoles(
            color = Argb.fromInt(customScheme.primary),
            onColor = Argb.fromInt(customScheme.onPrimary),
            colorContainer = Argb.fromInt(customScheme.primaryContainer),
            onColorContainer = Argb.fromInt(customScheme.onPrimaryContainer),
        )
    }

    private fun referenceScheme(seed: Argb, mode: ThemeMode, options: SchemeOptions): DynamicScheme {
        val sourceColor = Hct.fromInt(seed.value or OPAQUE_ALPHA)
        val isDark = mode == ThemeMode.DARK
        val spec = ColorSpec.SpecVersion.SPEC_2025
        return when (options.style) {
            ThemeStyle.TONAL_SPOT -> SchemeTonalSpot(sourceColor, isDark, options.contrastLevel, spec)
            ThemeStyle.FIDELITY -> SchemeFidelity(sourceColor, isDark, options.contrastLevel, spec)
            ThemeStyle.CONTENT -> SchemeContent(sourceColor, isDark, options.contrastLevel, spec)
            ThemeStyle.EXPRESSIVE -> SchemeExpressive(sourceColor, isDark, options.contrastLevel, spec)
            ThemeStyle.MONOCHROME -> SchemeMonochrome(sourceColor, isDark, options.contrastLevel, spec)
            ThemeStyle.VIBRANT -> SchemeVibrant(sourceColor, isDark, options.contrastLevel, spec)
        }
    }

    private fun CustomColorSchemes.forMode(mode: ThemeMode) =
        if (mode == ThemeMode.LIGHT) light else dark

    private fun roleContrast(foreground: Argb, background: Argb): Double = Contrast.ratioOfTones(
        Hct.fromInt(foreground.value).tone,
        Hct.fromInt(background.value).tone,
    )

    private fun assertRolesOpaque(roles: CustomColorRoles) {
        assertTrue(
            listOf(roles.color, roles.onColor, roles.colorContainer, roles.onColorContainer)
                .all { it.value ushr 24 == 0xFF },
        )
    }

    private companion object {
        val SEED = Argb.fromLong(0xFF67_50A4L)
        val TRANSLUCENT_SEED = Argb.fromLong(0x2267_50A4L)
        val CUSTOM_SOURCE = Argb.fromLong(0xFF2E_7D32L)
        val TRANSLUCENT_CUSTOM_SOURCE = Argb.fromLong(0x4034_5678L)
        const val OPAQUE_ALPHA = -0x1000000
    }
}
