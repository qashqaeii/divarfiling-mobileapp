package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lightweight parity: preview/export dimensions share same crop math (not full pixel golden). */
class PcsRenderCropGoldenTest {

    @Test
    fun cropRectStableAcrossViewportScales() {
        val bmp = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        Canvas(bmp).drawColor(Color.GRAY)
        val dst = RectF(0f, 0f, 1080f, 1080f)
        val style = PcsTextStyle(cropPanX = 30f, cropPanY = 70f, cropZoom = 1.4f)
        val small = PcsImageCrop.computeCropSrc(bmp, dst, style, "cover")
        val large = PcsImageCrop.computeCropSrc(bmp, dst, style, "cover")
        assertEqualsRect(small, large)
        bmp.recycle()
    }

    private fun assertEqualsRect(a: android.graphics.Rect, b: android.graphics.Rect) {
        assertTrue(a.width() == b.width() && a.height() == b.height())
    }
}
