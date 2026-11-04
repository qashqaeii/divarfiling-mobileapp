package ir.divarfiling.mobile.feature.contentstudio.canvas

import kotlin.math.min

/**
 * FitCenter poster mapping: container → scene coordinates (matches ContentScale.Fit + user pan/zoom).
 */
object PcsPosterViewportTransform {

    data class Viewport(
        val fitScale: Float,
        val drawnWidth: Float,
        val drawnHeight: Float,
        val offsetLeft: Float,
        val offsetTop: Float,
        val userScale: Float,
    )

    fun computeViewport(
        containerWidth: Int,
        containerHeight: Int,
        sceneWidth: Int,
        sceneHeight: Int,
        userScale: Float,
        panX: Float,
        panY: Float,
    ): Viewport? {
        if (containerWidth <= 0 || containerHeight <= 0 || sceneWidth <= 0 || sceneHeight <= 0) {
            return null
        }
        val baseFit = min(
            containerWidth / sceneWidth.toFloat(),
            containerHeight / sceneHeight.toFloat(),
        )
        val fitScale = baseFit * userScale.coerceIn(1f, 4f)
        val drawnWidth = sceneWidth * fitScale
        val drawnHeight = sceneHeight * fitScale
        val offsetLeft = (containerWidth - drawnWidth) / 2f + panX
        val offsetTop = (containerHeight - drawnHeight) / 2f + panY
        return Viewport(fitScale, drawnWidth, drawnHeight, offsetLeft, offsetTop, userScale)
    }

    fun screenToScene(
        screenX: Float,
        screenY: Float,
        viewport: Viewport,
        sceneWidth: Int,
        sceneHeight: Int,
    ): Pair<Float, Float>? {
        val x = (screenX - viewport.offsetLeft) / viewport.fitScale
        val y = (screenY - viewport.offsetTop) / viewport.fitScale
        if (x < 0f || y < 0f || x > sceneWidth || y > sceneHeight) return null
        return x to y
    }

    fun screenDeltaToScene(
        deltaX: Float,
        deltaY: Float,
        viewport: Viewport,
    ): Pair<Float, Float> {
        return (deltaX / viewport.fitScale) to (deltaY / viewport.fitScale)
    }
}
