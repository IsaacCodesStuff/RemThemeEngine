# Material Color Utilities dependency review

## Selected artifact

Rem uses the standalone Kotlin Multiplatform artifact
`com.materialkolor:material-color-utilities:6.0.0`. It is separate from the
MaterialKolor Compose adapters and the version is pinned in
`gradle/libs.versions.toml`.

MaterialKolor 6.0 documents that the engine is generated from a pinned Google
MCU Kotlin revision and that its outputs are tested against Google's upstream
Java and Kotlin implementations for ARGB-for-ARGB matches. Treat this as
version-specific parity evidence, not a guarantee about untested APIs or later
releases. The engine's default color specification in 6.0 is `SPEC_2025`; Rem
explicitly selects `SPEC_2025` for generation and does not expose spec selection
as a public API.

The common scheme API includes Tonal Spot, Vibrant, Expressive, Fidelity,
Content, Monochrome, Neutral, Rainbow, and Fruit Salad variants. Version 6.0
also has a CMF style; Rem does not expose or implement that style or Android
17 QPR2 theme personalities.

## Verified target metadata

Gradle's published module metadata resolves Android, JVM, iOS device ARM64, and
iOS simulator ARM64 variants. The artifact also publishes JS, Wasm JS, and
macOS ARM64 variants. It does **not** publish an iOS x64 variant. Rem currently
configures only Android and JVM desktop targets; the artifact's Apple variants
are not used by Rem's build.

The artifact's common API metadata resolves through Rem's `commonMain`, and
Rem's common tests compile and run on JVM. An Android compile probe importing
MCU's HCT API also compiled against the published Android variant. The probe
was removed afterward.

## Image processing API

The MCU artifact exposes the APIs needed to select a seed from decoded pixels:

- `QuantizerCelebi.quantize(pixels: IntArray, maxColors: Int): Map<Int, Int>`
  has no `maxColors` default. It runs the Wu quantizer to initialize
  Wsmeans/K-means and returns ARGB color to population counts. MCU's extraction
  guidance calls 128 a reasonable `maxColors` value.
- `Score.score(colorsToPopulation: Map<Int, Int>, desired: Int = 4,
  fallbackColorArgb: Int? = 0xFF4285F4, filter: Boolean = true): List<Int>`
  ranks eligible colors best-first. It filters low-chroma and low-excited-hue
  colors by default; if none qualify, its default fallback is Google Blue. A
  null fallback allows an empty result. Both APIs are in common code.
- `Hct` and `TonalPalette` APIs are also available in common code.

Google's MCU guidance leaves image decoding to the caller. Rem can therefore
keep its input contract at decoded ARGB pixels and use the MCU quantizer and
scorer directly. The separate `material-kolor-palette` artifact is not needed
for that contract; it supplies higher-level image source/loading integration.
Revisit it only if Rem later chooses to accept image objects or encoded image
bytes itself.

Rem's current extraction contract accepts flat decoded `0xAARRGGBB` pixels in
row-major order. It discards fully transparent pixels and treats nonzero-alpha
pixels as opaque RGB because MCU quantization is alpha-agnostic. Empty or
all-transparent input, and input with no MCU-eligible scored colors, fail with
`IllegalArgumentException`. Rem quantizes to at most 128 colors, then requests
the single highest-scoring result with MCU's default filtering and no fallback
color. Image decoding and resizing remain the caller's responsibility.

In the resolved 6.0.0 source, quantization groups packed ARGB inputs before
converting their RGB channels to Lab; the Lab conversion ignores alpha.
Normalizing visible pixels to opaque before calling the quantizer prevents
identical RGB values with different input alpha from being counted as separate
quantizer entries. Wsmeans uses a fixed internal random seed, supporting
repeatable output for identical ordered input and dependency version.

## Toolchain compatibility

The project pins Kotlin Multiplatform 2.4.21, Android Gradle Plugin 9.3.1, and
Gradle 9.7.0. Kotlin's compatibility guide lists Gradle 9.7.0 and AGP 9.3.1
within the supported range for Kotlin Multiplatform 2.4.20–2.4.21. Gradle 9.7
also satisfies AGP 9.3's minimum Gradle version.

## Licensing

The artifact POM declares the MIT License and Apache License 2.0. MaterialKolor
identifies its own project code as MIT and the generated MCU implementation as
Apache 2.0. See [THIRD_PARTY_NOTICES.md](../THIRD_PARTY_NOTICES.md) for
redistribution guidance and source links.

## References

- [MaterialKolor 6.0.0 release notes](https://github.com/jordond/MaterialKolor/releases/tag/6.0.0)
- [MaterialKolor's MCU API reference](https://docs.materialkolor.com/material-color-utilities/index.html)
- [MCU image extraction guidance](https://github.com/material-foundation/material-color-utilities/blob/main/dev_guide/extracting_colors.md)
- [Kotlin Multiplatform compatibility guide](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html)
- [Android KMP library plugin setup](https://developer.android.com/kotlin/multiplatform/plugin)
