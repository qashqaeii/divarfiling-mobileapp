package ir.divarfiling.mobile.feature.contentstudio.editor

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLayer
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PcsAlignmentSnapTest {

    @Test
    fun snapMoveAlignsToCanvasCenter() {
        val moving = PcsLayer(id = "m", type = "text", x = 520f, y = 100f, width = 40f, height = 40f)
        val scene = PcsSceneDocument(layers = listOf(moving), layerOrder = listOf("m"))
        val result = PcsAlignmentSnap.snapMove(
            moving,
            rawDx = 0f,
            rawDy = 0f,
            scene = scene,
            canvasWidth = 1080f,
            canvasHeight = 1920f,
            thresholdScene = 12f,
            enabled = true,
        )
        assertTrue(result.guides.isNotEmpty())
        assertEquals(540f - (520f + 20f), result.dx, 0.01f)
    }
}
