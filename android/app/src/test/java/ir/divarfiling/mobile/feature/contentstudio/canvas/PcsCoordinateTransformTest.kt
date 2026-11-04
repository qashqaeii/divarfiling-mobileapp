package ir.divarfiling.mobile.feature.contentstudio.canvas

import org.junit.Assert.assertEquals
import org.junit.Test

class PcsCoordinateTransformTest {

    @Test
    fun fitCenter_preservesUniformScale() {
        val t = PcsCoordinateTransform.fitCenter(1080f, 1920f, 540f, 960f)
        assertEquals(0.5f, t.scale, 0.001f)
        val rect = t.mapRect(100f, 200f, 400f, 100f)
        assertEquals(50f, rect.left, 0.001f)
        assertEquals(100f, rect.top, 0.001f)
        assertEquals(200f, rect.width, 0.001f)
    }
}
