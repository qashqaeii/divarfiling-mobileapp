package ir.divarfiling.mobile.feature.contentstudio.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.min

/** Uniform scale from reference canvas (e.g. 1080×1920) to viewport — no per-layer layout drift. */
data class PcsViewportTransform(
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float,
    val referenceWidth: Float,
    val referenceHeight: Float,
) {
    fun mapRect(x: Float, y: Float, width: Float, height: Float): MappedRect {
        return MappedRect(
            left = offsetX + x * scale,
            top = offsetY + y * scale,
            width = width * scale,
            height = height * scale,
        )
    }

    fun mapPoint(x: Float, y: Float): Offset {
        return Offset(offsetX + x * scale, offsetY + y * scale)
    }
}

data class MappedRect(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
)

object PcsCoordinateTransform {
    fun fitCenter(
        referenceWidth: Float,
        referenceHeight: Float,
        viewportWidth: Float,
        viewportHeight: Float,
    ): PcsViewportTransform {
        val scale = min(viewportWidth / referenceWidth, viewportHeight / referenceHeight)
        val drawnW = referenceWidth * scale
        val drawnH = referenceHeight * scale
        val offsetX = (viewportWidth - drawnW) / 2f
        val offsetY = (viewportHeight - drawnH) / 2f
        return PcsViewportTransform(
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY,
            referenceWidth = referenceWidth,
            referenceHeight = referenceHeight,
        )
    }
}
