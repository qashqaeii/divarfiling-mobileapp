package ir.divarfiling.mobile.feature.contentstudio.editor

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLayer
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PcsSceneEditorTest {
    @Test
    fun moveLayerUpdatesCoordinates() {
        val scene = PcsSceneDocument(
            layers = listOf(
                PcsLayer(id = "a", type = "text", x = 10f, y = 20f, width = 100f, height = 40f),
            ),
            layerOrder = listOf("a"),
        )
        val moved = PcsSceneMutator.moveLayer(scene, "a", 5f, -3f)
        val layer = moved.layers.first()
        assertEquals(15f, layer.x)
        assertEquals(17f, layer.y)
    }

    @Test
    fun hitTestSelectsTopMostLayer() {
        val scene = PcsSceneDocument(
            layers = listOf(
                PcsLayer(id = "back", type = "shape", x = 0f, y = 0f, width = 200f, height = 200f),
                PcsLayer(id = "front", type = "text", x = 50f, y = 50f, width = 80f, height = 40f),
            ),
            layerOrder = listOf("back", "front"),
        )
        assertEquals("front", PcsSceneMutator.hitTestLayer(scene, 60f, 60f))
    }

    @Test
    fun undoRestoresPreviousScene() {
        val history = PcsSceneHistory()
        val v1 = PcsSceneDocument(layers = listOf(PcsLayer(id = "a", type = "text", x = 1f, y = 1f, width = 1f, height = 1f)))
        val v2 = PcsSceneMutator.moveLayer(v1, "a", 10f, 0f)
        history.pushBeforeChange(v1)
        val restored = history.undo(v2)
        assertNotNull(restored)
        assertEquals(1f, restored!!.layers.first().x)
    }
}
