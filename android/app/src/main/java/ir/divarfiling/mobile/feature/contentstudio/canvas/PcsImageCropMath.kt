package ir.divarfiling.mobile.feature.contentstudio.canvas

import kotlin.math.max

/** JVM-friendly crop rect (unit tests — no android.graphics stubs). */
data class PcsCropRect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    fun width(): Int = (right - left).coerceAtLeast(0)

    fun height(): Int = (bottom - top).coerceAtLeast(0)
}

object PcsImageCropMath {
    fun computeCropSrc(
        bmpWidth: Int,
        bmpHeight: Int,
        dstWidth: Float,
        dstHeight: Float,
        style: PcsTextStyle?,
        objectFit: String?,
    ): PcsCropRect {
        val fit = (objectFit ?: style?.objectFit ?: "cover").lowercase()
        val zoom = style?.cropZoom ?: 1f
        val panX = (style?.cropPanX ?: 50f) / 100f
        val panY = (style?.cropPanY ?: 50f) / 100f
        val bw = bmpWidth.toFloat()
        val bh = bmpHeight.toFloat()
        if (bw <= 0f || bh <= 0f || dstWidth <= 0f || dstHeight <= 0f) {
            return PcsCropRect(0, 0, bmpWidth.coerceAtLeast(1), bmpHeight.coerceAtLeast(1))
        }
        val scale = when (fit) {
            "contain" -> minOf(dstWidth / bw, dstHeight / bh) * zoom
            else -> max(dstWidth / bw, dstHeight / bh) * zoom
        }
        val sw = dstWidth / scale
        val sh = dstHeight / scale
        var left = (bw - sw) * panX
        var top = (bh - sh) * panY
        left = left.coerceIn(0f, max(0f, bw - sw))
        top = top.coerceIn(0f, max(0f, bh - sh))
        val right = (left + sw).coerceAtMost(bw)
        val bottom = (top + sh).coerceAtMost(bh)
        return PcsCropRect(
            left = left.toInt().coerceIn(0, bmpWidth),
            top = top.toInt().coerceIn(0, bmpHeight),
            right = right.toInt().coerceIn(0, bmpWidth),
            bottom = bottom.toInt().coerceIn(0, bmpHeight),
        )
    }
}
