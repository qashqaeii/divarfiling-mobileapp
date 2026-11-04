package ir.divarfiling.mobile.feature.contentstudio.canvas

import org.junit.Assert.assertEquals
import org.junit.Test

class PcsImageCropContainTest {

    @Test
    fun containSourceAspectMatchesDestinationSlot() {
        val dstW = 400f
        val dstH = 400f
        val src = PcsImageCropMath.computeCropSrc(1600, 900, dstW, dstH, null, "contain")
        val srcAspect = src.width().toFloat() / src.height().coerceAtLeast(1)
        val dstAspect = dstW / dstH
        assertEquals(dstAspect, srcAspect, 0.05f)
    }
}
