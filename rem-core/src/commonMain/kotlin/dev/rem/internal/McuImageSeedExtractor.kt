package dev.rem.internal

import com.materialkolor.quantize.QuantizerCelebi
import com.materialkolor.score.Score
import dev.rem.api.Argb

/** MCU quantization and scoring boundary for decoded image pixels. */
internal object McuImageSeedExtractor {
    fun extractSeedColor(pixels: IntArray): Argb {
        require(pixels.isNotEmpty()) { "pixels must not be empty" }

        val visiblePixelCount = pixels.count { it ushr 24 != 0 }
        require(visiblePixelCount > 0) { "pixels must contain at least one non-transparent pixel" }

        // MCU quantization operates on RGB and gives each input entry equal weight. Normalize alpha
        // so identical RGB values with different alpha values merge into the same quantizer entry.
        val opaquePixels = IntArray(visiblePixelCount)
        var outputIndex = 0
        for (pixel in pixels) {
            if (pixel ushr 24 != 0) {
                opaquePixels[outputIndex++] = pixel or OPAQUE_ALPHA
            }
        }

        val quantized = QuantizerCelebi.quantize(opaquePixels, MAX_COLORS)
        val topScoredColor = Score.score(
            colorsToPopulation = quantized,
            desired = 1,
            fallbackColorArgb = null,
        ).firstOrNull()

        requireNotNull(topScoredColor) {
            "pixels do not contain a color eligible for a theme seed"
        }
        return Argb.fromInt(topScoredColor)
    }

    private const val MAX_COLORS = 128
    private const val OPAQUE_ALPHA = -0x1000000
}
