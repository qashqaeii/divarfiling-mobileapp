package ir.divarfiling.mobile.feature.contentstudio.canvas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PcsPosterViewportTransformTest {

    @Test
    fun screenToSceneUsesFitCenterAndZoom() {
        val vp = PcsPosterViewportTransform.computeViewport(
            containerWidth = 400,
            containerHeight = 800,
            sceneWidth = 1080,
            sceneHeight = 1920,
            userScale = 2f,
            panX = 0f,
            panY = 0f,
        )
        assertNotNull(vp)
        val center = PcsPosterViewportTransform.screenToScene(200f, 400f, vp!!, 1080, 1920)
        assertNotNull(center)
        assertEquals(540f, center!!.first, 1f)
        assertEquals(960f, center.second, 1f)
    }

    @Test
    fun screenDeltaScalesByFitScale() {
        val vp = PcsPosterViewportTransform.computeViewport(400, 400, 1080, 1080, 1f, 0f, 0f)!!
        val (dx, dy) = PcsPosterViewportTransform.screenDeltaToScene(10f, 0f, vp)
        assertEquals(10f / vp.fitScale, dx, 0.01f)
        assertEquals(0f, dy, 0.01f)
    }
}
