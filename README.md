# Rem

Rem is an unpublished Kotlin Multiplatform dynamic theming engine. It generates
Material-inspired color schemes from a seed color or decoded image pixels. Its
core is independent of Compose and other UI frameworks, and does not rely on a
platform's native dynamic-color feature.

## Features

- Light and dark schemes, generated individually or as a pair.
- Six styles: Tonal Spot, Fidelity, Content, Expressive, Monochrome, and Vibrant.
- A Rem-owned `ColorScheme` with 48 Material 3 semantic color roles.
- Configurable contrast level from `-1.0` to `1.0`.
- Image seed extraction through MCU quantization and scoring.
- Explicit MCU `SPEC_2025` generation behavior.

Rem uses Google's Material Color Utilities through the standalone
`com.materialkolor:material-color-utilities:6.0.0` artifact. Image decoding and
UI framework integration are left to applications and future adapters.

## Supported targets

The `rem-core` module configures Android, JVM desktop, iOS device ARM64, and iOS
simulator ARM64 targets. Configured targets do not by themselves establish
Swift interoperability or successful framework integration in an application.

## Quick start: seed-based generation

```kotlin
import dev.rem.api.Argb
import dev.rem.api.Rem
import dev.rem.api.SchemeOptions
import dev.rem.api.ThemeStyle

val seed = Argb.fromLong(0xFF6750A4L) // 0xAARRGGBB
val schemes = Rem.createSchemes(
    seed = seed,
    options = SchemeOptions(
        style = ThemeStyle.TONAL_SPOT,
        contrastLevel = 0.0,
    ),
)

val lightPrimary = schemes.light.primary
val darkPrimary = schemes.dark.primary
```

`createSchemes()` applies the same seed, style, and contrast level to both
modes. For a single mode, use `Rem.createScheme(seed, mode, options)`.
See [generation behavior and style mappings](docs/color-generation.md#api-behavior).

## Image-based generation

Rem accepts already-decoded `IntArray` pixels in `0xAARRGGBB` format. It does
not decode or resize images. Downsample large images in the caller when
appropriate; Rem currently sets no pixel-count limit.

```kotlin
import dev.rem.api.Rem

// Illustrative decoded pixels in 0xAARRGGBB format; a real array may come
// from the application's platform-specific image decoder.
val decodedPixels = intArrayOf(
    0xFF6750A4.toInt(),
    0xFF03A9F4.toInt(),
    0xFFFFC107.toInt(),
)
val imageSeed = Rem.extractSeedColor(decodedPixels)
val imageSchemes = Rem.createSchemes(imageSeed)
```

When pixels come from an image, supply them in row-major order. Fully
transparent pixels are ignored; pixels with any nonzero alpha contribute their
RGB at full weight. See [the pixel input and alpha contract](docs/color-generation.md#extracting-a-seed-from-image-pixels).

## Consuming color roles

Generated role values are Rem-owned `Argb` values, not Compose or Android UI
colors. Convert or adapt them at the boundary of your presentation layer.

```kotlin
val primary = schemes.light.primary
val textOnPrimary = schemes.light.onPrimary
val surface = schemes.light.surface
val emphasizedSurface = schemes.light.surfaceContainer
val modeIndependentAccent = schemes.light.primaryFixed
```

See [the full role list and Material 3 compatibility notes](docs/color-generation.md#color-roles-and-material-3-compatibility).

## Build and test from source

Run these commands from the repository root. They use the Gradle wrapper and
compile production sources for each target:

```shell
./gradlew :rem-core:jvmTest
./gradlew :rem-core:compileKotlinJvm
./gradlew :rem-core:compileKotlinAndroid
./gradlew :rem-core:compileKotlinIosArm64
./gradlew :rem-core:compileKotlinIosSimulatorArm64
```

The iOS source compilation tasks require a host/toolchain that supports the
Apple targets. Linking iOS frameworks and testing calls from Swift require
macOS/Xcode. Framework linking and Swift interoperability have not been
verified on the current Linux development host.

## Documentation

- [Color generation, styles, contrast, roles, and image input](docs/color-generation.md)
- [Material Color Utilities dependency review](docs/dependency-review.md)
- [Third-party notices and redistribution terms](THIRD_PARTY_NOTICES.md)
- [Project license](LICENSE)

## Development status

Rem is under development and has not been published. No Maven coordinates or
stable API compatibility guarantees have been established. Android and JVM
production-source compile tasks and JVM tests are configured. Configured iOS
targets do not imply that framework linking or Swift interoperability has
been tested.

## License

Rem is distributed under the [MIT License](LICENSE). See
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for third-party dependency
licenses and redistribution requirements.
