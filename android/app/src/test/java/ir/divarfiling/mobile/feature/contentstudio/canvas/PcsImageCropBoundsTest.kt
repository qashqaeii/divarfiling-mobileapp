package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.graphics.RectF
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PcsImageCropBoundsTest {

    @Test
    fun cropPanClampedInsideBitmap() {
        val rect = RectF(0f, 0f, 400f, 300f)
        val src = PcsImageCrop.computeCropSrc(
            800,
            600,
            rect,
            PcsTextStyle(cropPanX = 100f, cropPanY = 100f, cropZoom = 3f),
            "cover",
        )
        assertTrue(src.left >= 0)
        assertTrue(src.top >= 0)
        assertTrue(src.right <= 800)
        assertTrue(src.bottom <= 600)
    }
}
