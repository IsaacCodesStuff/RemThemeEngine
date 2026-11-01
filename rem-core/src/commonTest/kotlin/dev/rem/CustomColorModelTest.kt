package dev.rem

import dev.rem.api.Argb
import dev.rem.api.CustomColorDefinition
import dev.rem.api.Rem
import dev.rem.api.SchemeOptions
import dev.rem.api.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CustomColorModelTest {
    @Test
    fun acceptsValidCaseSensitiveIds() {
        val lowerCase = CustomColorDefinition("success", COLOR)
        val upperCase = CustomColorDefinition("Success", COLOR)

        assertEquals("success", lowerCase.id)
        assertEquals("Success", upperCase.id)
        assertTrue(lowerCase.id != upperCase.id)
    }

    @Test
    fun rejectsBlankAndWhitespacePaddedIds() {
        assertFailsWith<IllegalArgumentException> { CustomColorDefinition("", COLOR) }
        assertFailsWith<IllegalArgumentException> { CustomColorDefinition(" \t\n", COLOR) }
        assertFailsWith<IllegalArgumentException> { CustomColorDefinition(" success", COLOR) }
        assertFailsWith<IllegalArgumentException> { CustomColorDefinition("success ", COLOR) }
    }

    @Test
    fun rejectsDuplicateExactIdsButAllowsCaseDistinctIds() {
        val repeated = listOf(
            CustomColorDefinition("success", COLOR),
            CustomColorDefinition("success", OTHER_COLOR),
        )
        assertFailsWith<IllegalArgumentException> {
            Rem.createTheme(SEED, repeated)
        }

        val caseDistinct = listOf(
            CustomColorDefinition("success", COLOR),
            CustomColorDefinition("Success", OTHER_COLOR),
        )
        val result = Rem.createTheme(SEED, caseDistinct)
        assertEquals(listOf("success", "Success"), result.customColors.keys.toList())
    }

    @Test
    fun allowsIdenticalSourceColorsUnderDifferentIds() {
        val definitions = listOf(
            CustomColorDefinition("success", COLOR),
            CustomColorDefinition("positive", COLOR),
        )

        val result = Rem.createTheme(SEED, definitions)
        assertEquals(listOf("success", "positive"), result.customColors.keys.toList())
        assertEquals(result.customColors.getValue("success"), result.customColors.getValue("positive"))
    }

    @Test
    fun emptyDefinitionsReturnOrdinarySchemesAndAnEmptyMap() {
        val options = SchemeOptions(contrastLevel = 0.25)
        val result = Rem.createTheme(SEED, emptyList(), options)

        assertEquals(Rem.createSchemes(SEED, options), result.schemes)
        assertTrue(result.customColors.isEmpty())
    }

    @Test
    fun validationDoesNotModifyOrRetainTheInputList() {
        val definitions = mutableListOf(CustomColorDefinition("success", COLOR))
        val snapshot = definitions.toList()

        val result = Rem.createTheme(SEED, definitions)
        assertEquals(snapshot, definitions)

        definitions.clear()
        assertTrue(definitions.isEmpty())
        assertTrue(result.customColors.containsKey("success"))
    }

    @Test
    fun emptyResultDoesNotRetainInputList() {
        val definitions = mutableListOf<CustomColorDefinition>()
        val result = Rem.createTheme(SEED, definitions)

        definitions += CustomColorDefinition("success", COLOR)
        assertTrue(result.customColors.isEmpty())
    }

    @Test
    fun emptyDefinitionsStillGenerateBothModes() {
        val result = Rem.createTheme(SEED, emptyList())

        assertEquals(Rem.createScheme(SEED, ThemeMode.LIGHT), result.schemes.light)
        assertEquals(Rem.createScheme(SEED, ThemeMode.DARK), result.schemes.dark)
    }

    private companion object {
        val SEED = Argb.fromLong(0xFF6750A4L)
        val COLOR = Argb.fromLong(0xFF2E7D32L)
        val OTHER_COLOR = Argb.fromLong(0xFFB3261EL)
    }
}
