package dev.rem.api

/** Configuration shared by single-scheme and paired-scheme generation. */
data class SchemeOptions(
    val style: ThemeStyle = ThemeStyle.TONAL_SPOT,
    val contrastLevel: Double = 0.0,
) {
    init {
        require(contrastLevel.isFinite() && contrastLevel in MIN_CONTRAST..MAX_CONTRAST) {
            "contrastLevel must be finite and between -1.0 and 1.0 inclusive"
        }
    }

    private companion object {
        const val MIN_CONTRAST = -1.0
        const val MAX_CONTRAST = 1.0
    }
}
