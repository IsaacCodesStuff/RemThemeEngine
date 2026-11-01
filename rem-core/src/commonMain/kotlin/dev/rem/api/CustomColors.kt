package dev.rem.api

/** A caller-defined color palette to generate alongside a Rem theme. */
data class CustomColorDefinition(
    /** Stable, case-sensitive identifier used to look up this color in the generated theme. */
    val id: String,
    /** Source color encoded as `0xAARRGGBB`; alpha will be ignored during generation. */
    val source: Argb,
    /** Whether to harmonize this color's hue toward the theme seed before palette generation. */
    val harmonizeWithSeed: Boolean = false,
) {
    init {
        require(id.isNotBlank()) { "Custom color id must not be blank" }
        require(id == id.trim()) { "Custom color id must not have leading or trailing whitespace" }
    }
}

/** The semantic roles generated for one custom color in one theme mode. */
data class CustomColorRoles(
    val color: Argb,
    val onColor: Argb,
    val colorContainer: Argb,
    val onColorContainer: Argb,
)

/** Light and dark semantic roles generated for one custom color. */
data class CustomColorSchemes(
    val light: CustomColorRoles,
    val dark: CustomColorRoles,
)

/** Standard Rem schemes together with custom color roles indexed by definition ID. */
data class ThemeWithCustomColors(
    val schemes: ThemeSchemes,
    val customColors: Map<String, CustomColorSchemes>,
)

/** Checks definition IDs without modifying or retaining the caller's list. */
internal fun validateCustomColorDefinitions(customColors: List<CustomColorDefinition>) {
    val ids = mutableSetOf<String>()
    for (definition in customColors) {
        require(ids.add(definition.id)) {
            "Duplicate custom color id: ${definition.id}"
        }
    }
}
