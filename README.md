# Rem

Rem is a Kotlin Multiplatform dynamic theming engine. It is intended to generate
Material-inspired color schemes from seed colors or image pixels without relying
on a UI framework or an operating system's native dynamic-color feature.

The initial library module is `rem-core`, with Android, JVM desktop, iOS device
ARM64, and iOS simulator ARM64 targets. The core is kept independent of Compose
and other UI frameworks.

Rem uses Google's Material Color Utilities through the standalone
`com.materialkolor:material-color-utilities:6.0.0` artifact. Seed-based color
generation and seed extraction from caller-decoded ARGB pixels are available
in `rem-core`; Rem does not decode image files.

## Usage

```kotlin
import dev.rem.api.Argb
import dev.rem.api.Rem
import dev.rem.api.SchemeOptions
import dev.rem.api.ThemeMode
import dev.rem.api.ThemeStyle

val seed = Argb.fromLong(0xFF6750A4L) // 0xAARRGGBB
val light = Rem.createScheme(
    seed = seed,
    mode = ThemeMode.LIGHT,
    options = SchemeOptions(
        style = ThemeStyle.TONAL_SPOT,
        contrastLevel = 0.0,
    ),
)
val lightAndDark = Rem.createSchemes(seed, SchemeOptions(style = ThemeStyle.FIDELITY))
```

See [docs/color-generation.md](docs/color-generation.md) for the API contract,
style mappings, role meanings, contrast behavior, pixel input contract, and
current limitations.

See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for dependency provenance
and licensing information. See [docs/dependency-review.md](docs/dependency-review.md)
for the MCU target and image-processing API review.
