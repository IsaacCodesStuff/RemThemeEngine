package dev.rem.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.rem.api.Argb
import dev.rem.api.Rem

/** Compile-time fixture proving both generated modes fit Material 3's standard theme API. */
@Composable
internal fun MaterialThemeIntegrationFixture(dark: Boolean) {
    val schemes = Rem.createSchemes(Argb.fromLong(0xFF6750A4L))
    val remScheme = if (dark) schemes.dark else schemes.light

    MaterialTheme(colorScheme = remScheme.toMaterial3ColorScheme()) {
        Text("Material 3 composable inside Rem theme")
    }
}
