package ir.divarfiling.mobile.feature.contentstudio.canvas

import org.junit.Assert.assertEquals
import org.junit.Test

class PcsCropGestureMathTest {

    @Test
    fun panDeltaMovesWithinBounds() {
        val (x, y) = PcsCropGestureMath.panFromLocalDelta(200f, 300f, 20f, 0f, 50f, 50f)
        assertEquals(true, x in 0f..100f)
        assertEquals(true, y in 0f..100f)
        assertEquals(40f, x, 0.01f)
    }

    @Test
    fun zoomPinchClamped() {
        assertEquals(3f, PcsCropGestureMath.zoomFromPinch(2f, 2f), 0.01f)
        assertEquals(0.5f, PcsCropGestureMath.zoomFromPinch(0.4f, 1f), 0.01f)
    }
}
