package dev.rem.api

/** Material-inspired palette personality used to derive a scheme from its seed. */
enum class ThemeStyle {
    /** Calm, moderately chromatic palettes. Maps to MCU's `TONAL_SPOT` variant. */
    TONAL_SPOT,

    /** Keeps the source color prominent in containers. Maps to MCU's `FIDELITY` variant. */
    FIDELITY,

    /** Maps directly to MCU's `CONTENT` variant. */
    CONTENT,

    /** Playful palettes with greater hue movement. Maps to MCU's `EXPRESSIVE` variant. */
    EXPRESSIVE,

    /** Grayscale palette personality. Maps to MCU's `MONOCHROME` variant. */
    MONOCHROME,

    /** High-chroma palette personality. Maps to MCU's `VIBRANT` variant. */
    VIBRANT,
}
