package ir.divarfiling.mobile.feature.contentstudio.editor

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLayer
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsTextStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PcsLayerTransformOpsTest {

    @Test
    fun resizeEnforcesMinimumSize() {
        val layer = PcsLayer(id = "a", type = "shape", x = 10f, y = 10f, width = 20f, height = 20f)
        val resized = PcsLayerTransformOps.resizeLayer(
            layer,
            PcsHandleKind.ResizeTopLeft,
            localDx = 20f,
            localDy = 20f,
        )
        assertTrue(resized.width >= 8f)
        assertTrue(resized.height >= 8f)
    }

    @Test
    fun clampCropStyleWithinContract() {
        val style = PcsLayerTransformOps.clampCropStyle(
            PcsTextStyle(cropPanX = 200f, cropPanY = -5f, cropZoom = 9f, brightness = 10f, blur = 99f),
        )
        assertEquals(100f, style.cropPanX!!, 0.01f)
        assertEquals(0f, style.cropPanY!!, 0.01f)
        assertEquals(3f, style.cropZoom!!, 0.01f)
        assertEquals(40f, style.brightness!!, 0.01f)
        assertEquals(12f, style.blur!!, 0.01f)
    }
}
