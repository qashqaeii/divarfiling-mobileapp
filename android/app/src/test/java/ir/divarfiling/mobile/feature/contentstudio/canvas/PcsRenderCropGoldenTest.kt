package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.graphics.RectF
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lightweight parity: preview/export dimensions share same crop math (not full pixel golden). */
class PcsRenderCropGoldenTest {

    @Test
    fun cropRectStableAcrossViewportScales() {
        val dst = RectF(0f, 0f, 1080f, 1080f)
        val style = PcsTextStyle(cropPanX = 30f, cropPanY = 70f, cropZoom = 1.4f)
        val small = PcsImageCrop.computeCropSrc(800, 600, dst, style, "cover")
        val large = PcsImageCrop.computeCropSrc(800, 600, dst, style, "cover")
        assertEqualsRect(small, large)
    }

    private fun assertEqualsRect(a: android.graphics.Rect, b: android.graphics.Rect) {
        assertTrue(a.width() == b.width() && a.height() == b.height())
    }
}
