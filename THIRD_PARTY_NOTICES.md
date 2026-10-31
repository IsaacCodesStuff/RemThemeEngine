# Third-party notices

## Material Color Utilities 6.0.0

Rem depends on the standalone Kotlin Multiplatform artifact
`com.materialkolor:material-color-utilities:6.0.0`, published by the
MaterialKolor project. This artifact contains Google's Material Color Utilities
code, generated from a pinned upstream Kotlin revision.

The artifact's Maven metadata declares both the MIT License and Apache License,
Version 2.0. The MaterialKolor project is MIT-licensed; its generated Material
Color Utilities implementation is licensed under Apache 2.0. The MCU source
retains Google's copyright and Apache license notices. When redistributing the
dependency with Rem, preserve the upstream copyright, license, and NOTICE
material and identify changes to MCU-derived code. Rem does not modify or copy
MCU source.

Rem consumes only the standalone MCU artifact and does not bundle MaterialKolor's
Compose adapters. Its resolved common metadata also declares Kotlin standard
library and Poko annotation transitives; see the Gradle dependency report when
updating the pinned artifact.

Sources:

- [MaterialKolor project and licensing notes](https://github.com/jordond/MaterialKolor)
- [Material Color Utilities upstream](https://github.com/material-foundation/material-color-utilities)
- [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- [MIT License](https://opensource.org/license/mit)
