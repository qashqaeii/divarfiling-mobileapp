package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.graphics.RectF
import org.junit.Assert.assertEquals
import org.junit.Test

class PcsImageCropContainTest {

    @Test
    fun containSourceAspectMatchesDestinationSlot() {
        val dst = RectF(0f, 0f, 400f, 400f)
        val src = PcsImageCrop.computeCropSrc(1600, 900, dst, null, "contain")
        val srcAspect = src.width().toFloat() / src.height().coerceAtLeast(1)
        val dstAspect = dst.width() / dst.height()
        assertEquals(dstAspect, srcAspect, 0.05f)
    }
}
