package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.graphics.Bitmap
import android.graphics.RectF
import org.junit.Assert.assertTrue
import org.junit.Test

class PcsImageCropBoundsTest {

    @Test
    fun cropPanClampedInsideBitmap() {
        val bmp = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        val rect = RectF(0f, 0f, 400f, 300f)
        val src = PcsImageCrop.computeCropSrc(
            bmp,
            rect,
            PcsTextStyle(cropPanX = 100f, cropPanY = 100f, cropZoom = 3f),
            "cover",
        )
        assertTrue(src.left >= 0)
        assertTrue(src.top >= 0)
        assertTrue(src.right <= bmp.width)
        assertTrue(src.bottom <= bmp.height)
        bmp.recycle()
    }
}
