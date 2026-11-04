package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.max

object PcsImageCrop {
    fun computeCropSrc(bmp: Bitmap, dst: RectF, style: PcsTextStyle?, objectFit: String?): Rect =
        computeCropSrc(bmp.width, bmp.height, dst, style, objectFit)

    fun computeCropSrc(
        bmpWidth: Int,
        bmpHeight: Int,
        dst: RectF,
        style: PcsTextStyle?,
        objectFit: String?,
    ): Rect {
        val fit = (objectFit ?: style?.objectFit ?: "cover").lowercase()
        val zoom = style?.cropZoom ?: 1f
        val panX = (style?.cropPanX ?: 50f) / 100f
        val panY = (style?.cropPanY ?: 50f) / 100f
        val bw = bmpWidth.toFloat()
        val bh = bmpHeight.toFloat()
        if (bw <= 0f || bh <= 0f || dst.width() <= 0f || dst.height() <= 0f) {
            return Rect(0, 0, bmpWidth.coerceAtLeast(1), bmpHeight.coerceAtLeast(1))
        }
        val scale = when (fit) {
            "contain" -> minOf(dst.width() / bw, dst.height() / bh) * zoom
            else -> max(dst.width() / bw, dst.height() / bh) * zoom
        }
        val sw = dst.width() / scale
        val sh = dst.height() / scale
        var left = (bw - sw) * panX
        var top = (bh - sh) * panY
        left = left.coerceIn(0f, max(0f, bw - sw))
        top = top.coerceIn(0f, max(0f, bh - sh))
        val right = (left + sw).coerceAtMost(bw)
        val bottom = (top + sh).coerceAtMost(bh)
        return Rect(
            left.toInt().coerceIn(0, bmpWidth),
            top.toInt().coerceIn(0, bmpHeight),
            right.toInt().coerceIn(0, bmpWidth),
            bottom.toInt().coerceIn(0, bmpHeight),
        )
    }
}
