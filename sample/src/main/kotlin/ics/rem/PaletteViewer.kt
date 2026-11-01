package ics.rem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ColorScheme as Material3ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.rem.api.CustomColorSchemes
import dev.rem.api.CustomColorRoles
import dev.rem.api.ThemeMode

internal enum class PaletteGroup(val title: String) {
    PRIMARY("Primary"),
    SECONDARY("Secondary"),
    TERTIARY("Tertiary"),
    SURFACE("Surface"),
    ERROR("Error"),
    OUTLINE("Outline"),
}

internal data class PaletteRole(
    val name: String,
    val color: Color,
    val group: PaletteGroup,
)

internal data class PaletteSwatch(
    val name: String,
    val color: Color,
)

/** The single ordered mapping used by both sample palette views. */
internal fun Material3ColorScheme.toPaletteRoles(): List<PaletteRole> = listOf(
    PaletteRole("primary", primary, PaletteGroup.PRIMARY),
    PaletteRole("onPrimary", onPrimary, PaletteGroup.PRIMARY),
    PaletteRole("primaryContainer", primaryContainer, PaletteGroup.PRIMARY),
    PaletteRole("onPrimaryContainer", onPrimaryContainer, PaletteGroup.PRIMARY),
    PaletteRole("inversePrimary", inversePrimary, PaletteGroup.PRIMARY),
    PaletteRole("primaryFixed", primaryFixed, PaletteGroup.PRIMARY),
    PaletteRole("primaryFixedDim", primaryFixedDim, PaletteGroup.PRIMARY),
    PaletteRole("onPrimaryFixed", onPrimaryFixed, PaletteGroup.PRIMARY),
    PaletteRole("onPrimaryFixedVariant", onPrimaryFixedVariant, PaletteGroup.PRIMARY),
    PaletteRole("secondary", secondary, PaletteGroup.SECONDARY),
    PaletteRole("onSecondary", onSecondary, PaletteGroup.SECONDARY),
    PaletteRole("secondaryContainer", secondaryContainer, PaletteGroup.SECONDARY),
    PaletteRole("onSecondaryContainer", onSecondaryContainer, PaletteGroup.SECONDARY),
    PaletteRole("secondaryFixed", secondaryFixed, PaletteGroup.SECONDARY),
    PaletteRole("secondaryFixedDim", secondaryFixedDim, PaletteGroup.SECONDARY),
    PaletteRole("onSecondaryFixed", onSecondaryFixed, PaletteGroup.SECONDARY),
    PaletteRole("onSecondaryFixedVariant", onSecondaryFixedVariant, PaletteGroup.SECONDARY),
    PaletteRole("tertiary", tertiary, PaletteGroup.TERTIARY),
    PaletteRole("onTertiary", onTertiary, PaletteGroup.TERTIARY),
    PaletteRole("tertiaryContainer", tertiaryContainer, PaletteGroup.TERTIARY),
    PaletteRole("onTertiaryContainer", onTertiaryContainer, PaletteGroup.TERTIARY),
    PaletteRole("tertiaryFixed", tertiaryFixed, PaletteGroup.TERTIARY),
    PaletteRole("tertiaryFixedDim", tertiaryFixedDim, PaletteGroup.TERTIARY),
    PaletteRole("onTertiaryFixed", onTertiaryFixed, PaletteGroup.TERTIARY),
    PaletteRole("onTertiaryFixedVariant", onTertiaryFixedVariant, PaletteGroup.TERTIARY),
    PaletteRole("surface", surface, PaletteGroup.SURFACE),
    PaletteRole("onSurface", onSurface, PaletteGroup.SURFACE),
    PaletteRole("background", background, PaletteGroup.SURFACE),
    PaletteRole("onBackground", onBackground, PaletteGroup.SURFACE),
    PaletteRole("surfaceVariant", surfaceVariant, PaletteGroup.SURFACE),
    PaletteRole("onSurfaceVariant", onSurfaceVariant, PaletteGroup.SURFACE),
    PaletteRole("surfaceTint", surfaceTint, PaletteGroup.SURFACE),
    PaletteRole("inverseSurface", inverseSurface, PaletteGroup.SURFACE),
    PaletteRole("inverseOnSurface", inverseOnSurface, PaletteGroup.SURFACE),
    PaletteRole("surfaceBright", surfaceBright, PaletteGroup.SURFACE),
    PaletteRole("surfaceDim", surfaceDim, PaletteGroup.SURFACE),
    PaletteRole("surfaceContainer", surfaceContainer, PaletteGroup.SURFACE),
    PaletteRole("surfaceContainerHigh", surfaceContainerHigh, PaletteGroup.SURFACE),
    PaletteRole("surfaceContainerHighest", surfaceContainerHighest, PaletteGroup.SURFACE),
    PaletteRole("surfaceContainerLow", surfaceContainerLow, PaletteGroup.SURFACE),
    PaletteRole("surfaceContainerLowest", surfaceContainerLowest, PaletteGroup.SURFACE),
    PaletteRole("scrim", scrim, PaletteGroup.SURFACE),
    PaletteRole("error", error, PaletteGroup.ERROR),
    PaletteRole("onError", onError, PaletteGroup.ERROR),
    PaletteRole("errorContainer", errorContainer, PaletteGroup.ERROR),
    PaletteRole("onErrorContainer", onErrorContainer, PaletteGroup.ERROR),
    PaletteRole("outline", outline, PaletteGroup.OUTLINE),
    PaletteRole("outlineVariant", outlineVariant, PaletteGroup.OUTLINE),
)

@Composable
internal fun CompactPalettePreview(
    roles: List<PaletteRole>,
    onOpenViewer: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Generated palette", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
            Text(
                "A quick view of the active Material 3 colors.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.large,
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PaletteGroup.entries.forEach { group ->
                    val role = roles.first { it.group == group }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(group.title, modifier = Modifier.weight(0.85f), style = MaterialTheme.typography.labelLarge)
                        PaletteSwatchTile(
                            swatch = PaletteSwatch(role.name, role.color),
                            modifier = Modifier.weight(2f),
                            compact = true,
                        )
                    }
                }
                OutlinedButton(onClick = onOpenViewer, modifier = Modifier.fillMaxWidth()) {
                    Text("View all 48 colors")
                }
            }
        }
    }
}

@Composable
internal fun PaletteViewerDialog(
    roles: List<PaletteRole>,
    customColors: Map<String, CustomColorSchemes>,
    themeMode: ThemeMode,
    exportInProgress: Boolean,
    exportMessage: String?,
    onExportPng: () -> Unit,
    onExportTxt: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Material 3 palette", style = MaterialTheme.typography.headlineSmall)
                        Text("48 semantic color roles", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onExportPng,
                        enabled = !exportInProgress,
                        modifier = Modifier.weight(1f),
                    ) { Text("Export PNG") }
                    OutlinedButton(
                        onClick = onExportTxt,
                        enabled = !exportInProgress,
                        modifier = Modifier.weight(1f),
                    ) { Text("Export TXT") }
                }
                if (exportInProgress || exportMessage != null) {
                    Text(
                        text = if (exportInProgress) "Preparing palette export…" else exportMessage.orEmpty(),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .semantics { liveRegion = LiveRegionMode.Polite },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 164.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PaletteGroup.entries.forEach { group ->
                        val groupRoles = roles.filter { it.group == group }
                        item(
                            key = "header-${group.name}",
                            span = { GridItemSpan(maxLineSpan) },
                        ) {
                            Text(
                                group.title,
                                modifier = Modifier.padding(top = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        items(items = groupRoles, key = { it.name }) { role ->
                            PaletteSwatchTile(PaletteSwatch(role.name, role.color))
                        }
                    }
                    if (customColors.isNotEmpty()) {
                        item(
                            key = "custom-header",
                            span = { GridItemSpan(maxLineSpan) },
                        ) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                Text("Custom semantic colors", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Separate from the standard Material 3 roles.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        customColorSwatches(customColors, themeMode).forEach { swatch ->
                            item(key = "custom-${swatch.name}") {
                                PaletteSwatchTile(swatch)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaletteSwatchTile(
    swatch: PaletteSwatch,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(if (compact) 6.dp else 8.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.height(if (compact) 32.dp else 42.dp).weight(0.7f),
                color = swatch.color,
                shape = MaterialTheme.shapes.small,
            ) {}
            Column(modifier = Modifier.weight(2.3f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(swatch.name, style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium)
                Text(
                    swatch.color.toArgb().toHexString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

internal fun customColorSwatches(
    customColors: Map<String, CustomColorSchemes>,
    themeMode: ThemeMode,
): List<PaletteSwatch> = buildList {
    customColors.forEach { (id, schemes) ->
        val roles: CustomColorRoles = if (themeMode == ThemeMode.DARK) schemes.dark else schemes.light
        add(PaletteSwatch("$id.color", roles.color.toComposeColor()))
        add(PaletteSwatch("$id.onColor", roles.onColor.toComposeColor()))
        add(PaletteSwatch("$id.colorContainer", roles.colorContainer.toComposeColor()))
        add(PaletteSwatch("$id.onColorContainer", roles.onColorContainer.toComposeColor()))
    }
}

private fun androidx.compose.ui.graphics.Color.toHexString(): String =
    "#%08X".format(toArgb().toLong() and 0xFFFFFFFFL)

private fun dev.rem.api.Argb.toComposeColor(): Color = Color(value)
