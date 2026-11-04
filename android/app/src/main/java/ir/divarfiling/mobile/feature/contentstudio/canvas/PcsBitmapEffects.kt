package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.graphics.Bitmap
import kotlin.math.max
import kotlin.math.min

object PcsBitmapEffects {

    fun configureImagePaint(paint: android.graphics.Paint, style: PcsTextStyle?, alpha: Int) {
        paint.alpha = alpha
        brightnessColorFilter(style?.brightness)?.let { paint.colorFilter = it }
    }

    fun brightnessColorFilter(brightness: Float?): android.graphics.ColorMatrixColorFilter? {
        val b = brightness ?: 100f
        if (b == 100f) return null
        val scale = (b / 100f).coerceIn(0.4f, 1.6f)
        val matrix = android.graphics.ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, 0f,
                0f, scale, 0f, 0f, 0f,
                0f, 0f, scale, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
        return android.graphics.ColorMatrixColorFilter(matrix)
    }

    fun blurredBitmapSource(src: Bitmap, blurRadius: Float): Bitmap {
        if (blurRadius <= 0.5f || src.isRecycled) return src
        val radius = blurRadius.coerceIn(1f, 12f)
        val scale = if (max(src.width, src.height) > 900) 0.45f else 1f
        val w = max(1, (src.width * scale).toInt())
        val h = max(1, (src.height * scale).toInt())
        val small = Bitmap.createScaledBitmap(src, w, h, true)
        val out = small.copy(Bitmap.Config.ARGB_8888, true)
        val passes = min(3, (radius / 3f).toInt().coerceAtLeast(1))
        repeat(passes) { boxBlur3x3(out) }
        if (small !== src && !small.isRecycled) small.recycle()
        return out
    }

    private fun boxBlur3x3(bitmap: Bitmap) {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 3 || h < 3) return
        val inPx = IntArray(w * h)
        bitmap.getPixels(inPx, 0, w, 0, 0, w, h)
        val outPx = IntArray(w * h)
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                var a = 0
                var r = 0
                var g = 0
                var b = 0
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val p = inPx[(y + dy) * w + (x + dx)]
                        a += (p shr 24) and 0xFF
                        r += (p shr 16) and 0xFF
                        g += (p shr 8) and 0xFF
                        b += p and 0xFF
                    }
                }
                outPx[y * w + x] =
                    ((a / 9) shl 24) or ((r / 9) shl 16) or ((g / 9) shl 8) or (b / 9)
            }
        }
        for (x in 0 until w) {
            outPx[x] = inPx[x]
            outPx[(h - 1) * w + x] = inPx[(h - 1) * w + x]
        }
        for (y in 0 until h) {
            outPx[y * w] = inPx[y * w]
            outPx[y * w + (w - 1)] = inPx[y * w + (w - 1)]
        }
        bitmap.setPixels(outPx, 0, w, 0, 0, w, h)
    }
}
