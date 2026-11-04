package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.graphics.Bitmap
import android.graphics.RectF
import org.junit.Assert.assertEquals
import org.junit.Test

class PcsImageCropContainTest {

    @Test
    fun containSourceAspectMatchesDestinationSlot() {
        val bmp = Bitmap.createBitmap(1600, 900, Bitmap.Config.ARGB_8888)
        val dst = RectF(0f, 0f, 400f, 400f)
        val src = PcsImageCrop.computeCropSrc(bmp, dst, null, "contain")
        val srcAspect = src.width().toFloat() / src.height().coerceAtLeast(1)
        val dstAspect = dst.width() / dst.height()
        assertEquals(dstAspect, srcAspect, 0.05f)
        bmp.recycle()
    }
}
