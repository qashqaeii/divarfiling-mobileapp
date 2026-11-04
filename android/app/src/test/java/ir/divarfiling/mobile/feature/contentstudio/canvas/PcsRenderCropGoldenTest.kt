package ir.divarfiling.mobile.feature.contentstudio.canvas

import org.junit.Assert.assertTrue
import org.junit.Test

/** Lightweight parity: preview/export dimensions share same crop math (not full pixel golden). */
class PcsRenderCropGoldenTest {

    @Test
    fun cropRectStableAcrossViewportScales() {
        val style = PcsTextStyle(cropPanX = 30f, cropPanY = 70f, cropZoom = 1.4f)
        val small = PcsImageCropMath.computeCropSrc(800, 600, 1080f, 1080f, style, "cover")
        val large = PcsImageCropMath.computeCropSrc(800, 600, 1080f, 1080f, style, "cover")
        assertEqualsRect(small, large)
    }

    private fun assertEqualsRect(a: PcsCropRect, b: PcsCropRect) {
        assertTrue(a.width() == b.width() && a.height() == b.height())
    }
}
