package dev.rem.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.rem.api.Argb
import dev.rem.api.ColorScheme
import dev.rem.api.Rem
import dev.rem.api.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class Material3ColorSchemeTest {
    @Test
    fun mapsAllRolesAndPreservesArgbValuesWithoutChangingSource() {
        val source = fixtureScheme()
        val original = source.copy()

        val actual = source.toMaterial3ColorScheme()

        assertRole(source.primary, actual.primary)
        assertRole(source.onPrimary, actual.onPrimary)
        assertRole(source.primaryContainer, actual.primaryContainer)
        assertRole(source.onPrimaryContainer, actual.onPrimaryContainer)
        assertRole(source.inversePrimary, actual.inversePrimary)
        assertRole(source.secondary, actual.secondary)
        assertRole(source.onSecondary, actual.onSecondary)
        assertRole(source.secondaryContainer, actual.secondaryContainer)
        assertRole(source.onSecondaryContainer, actual.onSecondaryContainer)
        assertRole(source.tertiary, actual.tertiary)
        assertRole(source.onTertiary, actual.onTertiary)
        assertRole(source.tertiaryContainer, actual.tertiaryContainer)
        assertRole(source.onTertiaryContainer, actual.onTertiaryContainer)
        assertRole(source.background, actual.background)
        assertRole(source.onBackground, actual.onBackground)
        assertRole(source.surface, actual.surface)
        assertRole(source.onSurface, actual.onSurface)
        assertRole(source.surfaceVariant, actual.surfaceVariant)
        assertRole(source.onSurfaceVariant, actual.onSurfaceVariant)
        assertRole(source.surfaceTint, actual.surfaceTint)
        assertRole(source.inverseSurface, actual.inverseSurface)
        assertRole(source.inverseOnSurface, actual.inverseOnSurface)
        assertRole(source.error, actual.error)
        assertRole(source.onError, actual.onError)
        assertRole(source.errorContainer, actual.errorContainer)
        assertRole(source.onErrorContainer, actual.onErrorContainer)
        assertRole(source.outline, actual.outline)
        assertRole(source.outlineVariant, actual.outlineVariant)
        assertRole(source.scrim, actual.scrim)
        assertRole(source.surfaceBright, actual.surfaceBright)
        assertRole(source.surfaceDim, actual.surfaceDim)
        assertRole(source.surfaceContainer, actual.surfaceContainer)
        assertRole(source.surfaceContainerHigh, actual.surfaceContainerHigh)
        assertRole(source.surfaceContainerHighest, actual.surfaceContainerHighest)
        assertRole(source.surfaceContainerLow, actual.surfaceContainerLow)
        assertRole(source.surfaceContainerLowest, actual.surfaceContainerLowest)
        assertRole(source.primaryFixed, actual.primaryFixed)
        assertRole(source.primaryFixedDim, actual.primaryFixedDim)
        assertRole(source.onPrimaryFixed, actual.onPrimaryFixed)
        assertRole(source.onPrimaryFixedVariant, actual.onPrimaryFixedVariant)
        assertRole(source.secondaryFixed, actual.secondaryFixed)
        assertRole(source.secondaryFixedDim, actual.secondaryFixedDim)
        assertRole(source.onSecondaryFixed, actual.onSecondaryFixed)
        assertRole(source.onSecondaryFixedVariant, actual.onSecondaryFixedVariant)
        assertRole(source.tertiaryFixed, actual.tertiaryFixed)
        assertRole(source.tertiaryFixedDim, actual.tertiaryFixedDim)
        assertRole(source.onTertiaryFixed, actual.onTertiaryFixed)
        assertRole(source.onTertiaryFixedVariant, actual.onTertiaryFixedVariant)

        assertEquals(original, source)
    }

    @Test
    fun convertsGeneratedLightAndDarkSchemes() {
        val schemes = Rem.createSchemes(Argb.fromInt(0xFF67_50A4.toInt()))

        val light = schemes.light.toMaterial3ColorScheme()
        val dark = schemes.dark.toMaterial3ColorScheme()

        assertRole(schemes.light.primary, light.primary)
        assertRole(schemes.light.surfaceContainer, light.surfaceContainer)
        assertRole(schemes.dark.primary, dark.primary)
        assertRole(schemes.dark.surfaceContainer, dark.surfaceContainer)
        assertNotEquals(light.primary, dark.primary)
        assertEquals(
            Rem.createScheme(Argb.fromInt(0xFF67_50A4.toInt()), ThemeMode.DARK),
            schemes.dark,
        )
    }

    private fun assertRole(expected: Argb, actual: Color) {
        assertEquals(expected.value, actual.toArgb())
    }

    private fun fixtureScheme() = ColorScheme(
        primary = roleColor(0),
        onPrimary = roleColor(1),
        primaryContainer = roleColor(2),
        onPrimaryContainer = roleColor(3),
        inversePrimary = roleColor(4),
        secondary = roleColor(5),
        onSecondary = roleColor(6),
        secondaryContainer = roleColor(7),
        onSecondaryContainer = roleColor(8),
        tertiary = roleColor(9),
        onTertiary = roleColor(10),
        tertiaryContainer = roleColor(11),
        onTertiaryContainer = roleColor(12),
        background = roleColor(13),
        onBackground = roleColor(14),
        surface = roleColor(15),
        onSurface = roleColor(16),
        surfaceVariant = roleColor(17),
        onSurfaceVariant = roleColor(18),
        surfaceTint = roleColor(19),
        inverseSurface = roleColor(20),
        inverseOnSurface = roleColor(21),
        error = roleColor(22),
        onError = roleColor(23),
        errorContainer = roleColor(24),
        onErrorContainer = roleColor(25),
        outline = roleColor(26),
        outlineVariant = roleColor(27),
        scrim = roleColor(28),
        surfaceBright = roleColor(29),
        surfaceDim = roleColor(30),
        surfaceContainer = roleColor(31),
        surfaceContainerHigh = roleColor(32),
        surfaceContainerHighest = roleColor(33),
        surfaceContainerLow = roleColor(34),
        surfaceContainerLowest = roleColor(35),
        primaryFixed = roleColor(36),
        primaryFixedDim = roleColor(37),
        onPrimaryFixed = roleColor(38),
        onPrimaryFixedVariant = roleColor(39),
        secondaryFixed = roleColor(40),
        secondaryFixedDim = roleColor(41),
        onSecondaryFixed = roleColor(42),
        onSecondaryFixedVariant = roleColor(43),
        tertiaryFixed = roleColor(44),
        tertiaryFixedDim = roleColor(45),
        onTertiaryFixed = roleColor(46),
        onTertiaryFixedVariant = roleColor(47),
    )

    private fun roleColor(index: Int): Argb {
        val alpha = index * 17 % 255
        val red = (index * 53 + 11) and 0xFF
        val green = (index * 97 + 29) and 0xFF
        val blue = (index * 131 + 47) and 0xFF
        return Argb.fromInt((alpha shl 24) or (red shl 16) or (green shl 8) or blue)
    }
}
