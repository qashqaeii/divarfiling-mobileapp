package ir.divarfiling.mobile.feature.contentstudio.canvas

import org.junit.Assert.assertTrue
import org.junit.Test

class PcsImageCropBoundsTest {

    @Test
    fun cropPanClampedInsideBitmap() {
        val src = PcsImageCropMath.computeCropSrc(
            bmpWidth = 800,
            bmpHeight = 600,
            dstWidth = 400f,
            dstHeight = 300f,
            style = PcsTextStyle(cropPanX = 100f, cropPanY = 100f, cropZoom = 3f),
            objectFit = "cover",
        )
        assertTrue(src.left >= 0)
        assertTrue(src.top >= 0)
        assertTrue(src.right <= 800)
        assertTrue(src.bottom <= 600)
    }
}
