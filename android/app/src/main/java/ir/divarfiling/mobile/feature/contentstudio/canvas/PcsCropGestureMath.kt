package ir.divarfiling.mobile.feature.contentstudio.canvas

import kotlin.math.max

/**
 * Maps canvas drag/pinch to Django-valid cropPanX/Y (0–100) and cropZoom (0.5–3).
 * Operates in layer-local space so rotated slots stay consistent with [PcsImageCrop].
 */
object PcsCropGestureMath {

    fun panFromLocalDelta(
        layerWidth: Float,
        layerHeight: Float,
        deltaLocalX: Float,
        deltaLocalY: Float,
        currentPanX: Float,
        currentPanY: Float,
    ): Pair<Float, Float> {
        val w = max(layerWidth, 1f)
        val h = max(layerHeight, 1f)
        val dxPct = deltaLocalX / w * 100f
        val dyPct = deltaLocalY / h * 100f
        val nx = (currentPanX - dxPct).coerceIn(0f, 100f)
        val ny = (currentPanY - dyPct).coerceIn(0f, 100f)
        return nx to ny
    }

    fun zoomFromPinch(currentZoom: Float, pinchFactor: Float): Float {
        return (currentZoom * pinchFactor).coerceIn(0.5f, 3f)
    }

    fun defaultPan(): Pair<Float, Float> = 50f to 50f

    fun defaultZoom(): Float = 1f
}
