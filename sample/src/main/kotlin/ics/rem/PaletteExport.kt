package ics.rem

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.rem.api.CustomColorSchemes
import dev.rem.api.ThemeMode
import java.io.IOException
import java.io.OutputStreamWriter

internal enum class PaletteExportFormat {
    PNG,
    TXT,
}

internal fun writePaletteExport(
    context: Context,
    destination: Uri,
    format: PaletteExportFormat,
    roles: List<PaletteRole>,
    customColors: Map<String, CustomColorSchemes>,
    themeMode: ThemeMode,
) {
    require(roles.size == 48) { "Expected 48 Material 3 roles, got ${roles.size}." }
    require(roles.map { it.name }.distinct().size == 48) { "Material 3 role mapping contains duplicate names." }

    when (format) {
        PaletteExportFormat.TXT -> {
            val stream = context.contentResolver.openOutputStream(destination)
                ?: throw IOException("Could not open the selected file for writing.")
            OutputStreamWriter(stream, Charsets.UTF_8).use { writer ->
                writer.write(buildPaletteText(roles, customColors, themeMode))
            }
        }

        PaletteExportFormat.PNG -> {
            val bitmap = renderPalettePng(roles, customColors, themeMode)
            try {
                val stream = context.contentResolver.openOutputStream(destination)
                    ?: throw IOException("Could not open the selected file for writing.")
                stream.use { output ->
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                        throw IOException("PNG encoding failed.")
                    }
                }
            } finally {
                // This export-only bitmap is never handed to Compose.
                bitmap.recycle()
            }
        }
    }
}

internal fun buildPaletteText(
    roles: List<PaletteRole>,
    customColors: Map<String, CustomColorSchemes>,
    themeMode: ThemeMode,
): String = buildString {
    appendLine("RemTheme palette")
    appendLine("Appearance: ${themeMode.name.lowercase().replaceFirstChar(Char::uppercase)}")
    appendLine()
    appendLine("Material 3 colors")

    PaletteGroup.entries.forEach { group ->
        appendLine()
        appendLine("${group.title}")
        roles.filter { it.group == group }.forEach { role ->
            appendLine("${role.name}: ${role.color.toArgb().toArgbHex()}")
        }
    }

    if (customColors.isNotEmpty()) {
        appendLine()
        appendLine("Custom semantic colors")
        exportedCustomColorSwatches(customColors, themeMode).forEach { swatch ->
            val separator = swatch.name.indexOf('.')
            val id = if (separator >= 0) swatch.name.substring(0, separator) else swatch.name
            val role = if (separator >= 0) swatch.name.substring(separator + 1) else "color"
            appendLine("${id.replaceFirstChar(Char::uppercase)} $role: ${swatch.color.toArgb().toArgbHex()}")
        }
    }
}

private fun renderPalettePng(
    roles: List<PaletteRole>,
    customColors: Map<String, CustomColorSchemes>,
    themeMode: ThemeMode,
): Bitmap {
    val backgroundColor = roles.first { it.name == "surface" }.color
    val textColor = roles.first { it.name == "onSurface" }.color
    val cardColor = roles.first { it.name == "surfaceContainer" }.color
    val accentColor = roles.first { it.name == "primary" }.color
    val custom = exportedCustomColorSwatches(customColors, themeMode)
    val leftGroups = listOf(
        PaletteGroup.PRIMARY,
        PaletteGroup.SECONDARY,
        PaletteGroup.TERTIARY,
        PaletteGroup.ERROR,
        PaletteGroup.OUTLINE,
    )
    val rightGroups = listOf(PaletteGroup.SURFACE)
    val canvasWidth = 1200
    val contentTop = 198
    val bottomPadding = 40
    val leftColumnHeight = paletteColumnHeight(leftGroups, roles)
    val rightColumnHeight = paletteColumnHeight(rightGroups, roles) +
        if (custom.isNotEmpty()) paletteCustomSectionHeight(custom) else 0
    val height = contentTop + maxOf(leftColumnHeight, rightColumnHeight) + bottomPadding
    val bitmap = Bitmap.createBitmap(canvasWidth, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(backgroundColor.toArgb())

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor.toArgb()
        textSize = 40f
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor.toArgb()
        textSize = 24f
        alpha = 200
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    val groupPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor.toArgb()
        textSize = 26f
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    val rolePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor.toArgb()
        textSize = 20f
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    val hexPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor.toArgb()
        textSize = 18f
        alpha = 220
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = cardColor.toArgb() }
    val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor.toArgb() }

    canvas.drawText("RemTheme", 56f, 78f, titlePaint)
    canvas.drawText(
        "${themeMode.name.lowercase().replaceFirstChar(Char::uppercase)} Material 3 palette",
        56f,
        118f,
        subtitlePaint,
    )
    canvas.drawRoundRect(RectF(56f, 140f, 1144f, 148f), 4f, 4f, accentPaint)

    val columnWidth = 532f
    val columnGap = 24f
    val leftColumnX = 56f
    val rightColumnX = leftColumnX + columnWidth + columnGap
    drawPaletteColumn(
        canvas = canvas,
        groups = leftGroups,
        roles = roles,
        customSwatches = emptyList(),
        x = leftColumnX,
        startY = contentTop.toFloat(),
        columnWidth = columnWidth,
        groupPaint = groupPaint,
        cardPaint = cardPaint,
        rolePaint = rolePaint,
        hexPaint = hexPaint,
    )
    drawPaletteColumn(
        canvas = canvas,
        groups = rightGroups,
        roles = roles,
        customSwatches = custom,
        x = rightColumnX,
        startY = contentTop.toFloat(),
        columnWidth = columnWidth,
        groupPaint = groupPaint,
        cardPaint = cardPaint,
        rolePaint = rolePaint,
        hexPaint = hexPaint,
    )
    return bitmap
}

private fun exportedCustomColorSwatches(
    customColors: Map<String, CustomColorSchemes>,
    themeMode: ThemeMode,
): List<PaletteSwatch> = customColorSwatches(customColors, themeMode)
    .filter { it.name.endsWith(".color") }

private fun paletteColumnHeight(groups: List<PaletteGroup>, roles: List<PaletteRole>): Int =
    groups.sumOf { group ->
        val roleCount = roles.count { it.group == group }
        48 + ((roleCount + 1) / 2) * 112
    }

private fun paletteCustomSectionHeight(customSwatches: List<PaletteSwatch>): Int =
    48 + ((customSwatches.size + 1) / 2) * 112

private fun drawPaletteColumn(
    canvas: Canvas,
    groups: List<PaletteGroup>,
    roles: List<PaletteRole>,
    customSwatches: List<PaletteSwatch>,
    x: Float,
    startY: Float,
    columnWidth: Float,
    groupPaint: Paint,
    cardPaint: Paint,
    rolePaint: Paint,
    hexPaint: Paint,
) {
    val cardGap = 12f
    val cardWidth = (columnWidth - cardGap) / 2f
    var y = startY
    groups.forEach { group ->
        canvas.drawText(group.title, x, y, groupPaint)
        y += 20f
        roles.filter { it.group == group }.chunked(2).forEach { row ->
            row.forEachIndexed { column, role ->
                drawExportTile(
                    canvas = canvas,
                    swatch = PaletteSwatch(role.name, role.color),
                    x = x + column * (cardWidth + cardGap),
                    y = y,
                    tileWidth = cardWidth,
                    cardPaint = cardPaint,
                    rolePaint = rolePaint,
                    hexPaint = hexPaint,
                )
            }
            y += 112f
        }
        y += 28f
    }

    if (customSwatches.isNotEmpty()) {
        canvas.drawText("Custom semantic colors", x, y, groupPaint)
        y += 20f
        customSwatches.chunked(2).forEach { row ->
            row.forEachIndexed { column, swatch ->
                drawExportTile(
                    canvas = canvas,
                    swatch = swatch.copy(name = displayCustomRoleName(swatch.name)),
                    x = x + column * (cardWidth + cardGap),
                    y = y,
                    tileWidth = cardWidth,
                    cardPaint = cardPaint,
                    rolePaint = rolePaint,
                    hexPaint = hexPaint,
                )
            }
            y += 112f
        }
    }
}

private fun drawExportTile(
    canvas: Canvas,
    swatch: PaletteSwatch,
    x: Float,
    y: Float,
    tileWidth: Float,
    cardPaint: Paint,
    rolePaint: Paint,
    hexPaint: Paint,
) {
    val tileHeight = 100f
    canvas.drawRoundRect(RectF(x, y, x + tileWidth, y + tileHeight), 18f, 18f, cardPaint)
    val swatchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = swatch.color.toArgb() }
    canvas.drawRoundRect(RectF(x + 16f, y + 18f, x + 80f, y + 82f), 12f, 12f, swatchPaint)
    val nameX = x + 98f
    val availableNameWidth = tileWidth - 98f - 12f
    val firstLineLength = rolePaint.breakText(swatch.name, true, availableNameWidth, null)
    if (firstLineLength < swatch.name.length) {
        canvas.drawText(swatch.name, 0, firstLineLength, nameX, y + 32f, rolePaint)
        canvas.drawText(swatch.name, firstLineLength, swatch.name.length, nameX, y + 55f, rolePaint)
        canvas.drawText(swatch.color.toArgb().toArgbHex(), nameX, y + 80f, hexPaint)
    } else {
        canvas.drawText(swatch.name, nameX, y + 43f, rolePaint)
        canvas.drawText(swatch.color.toArgb().toArgbHex(), nameX, y + 74f, hexPaint)
    }
}

private fun displayCustomRoleName(name: String): String {
    val separator = name.indexOf('.')
    if (separator < 0) return name
    val id = name.substring(0, separator).replaceFirstChar(Char::uppercase)
    val role = name.substring(separator + 1)
    return "$id · $role"
}

private fun Int.toArgbHex(): String = "#%08X".format(toLong() and 0xFFFFFFFFL)
