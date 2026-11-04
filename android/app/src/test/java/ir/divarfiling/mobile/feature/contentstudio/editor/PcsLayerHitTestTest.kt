package ir.divarfiling.mobile.feature.contentstudio.editor

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLayer
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.sqrt

class PcsLayerHitTestTest {

    @Test
    fun hitTestRotatedLayerInsideBounds() {
        val layer = PcsLayer(
            id = "r",
            type = "text",
            x = 100f,
            y = 100f,
            width = 100f,
            height = 40f,
            rotation = 90f,
        )
        val scene = PcsSceneDocument(layers = listOf(layer), layerOrder = listOf("r"))
        val cx = 150f
        val cy = 120f
        assertEquals("r", PcsLayerHitTest.hitTestLayer(scene, cx, cy))
    }

    @Test
    fun hitTestSkipsHiddenLayers() {
        val scene = PcsSceneDocument(
            layers = listOf(
                PcsLayer(id = "hidden", type = "shape", visible = false, x = 0f, y = 0f, width = 200f, height = 200f),
                PcsLayer(id = "visible", type = "shape", x = 0f, y = 0f, width = 50f, height = 50f),
            ),
            layerOrder = listOf("hidden", "visible"),
        )
        assertEquals("visible", PcsLayerHitTest.hitTestLayer(scene, 10f, 10f))
    }

    @Test
    fun hitTestOverlappingPicksTopPaintOrder() {
        val scene = PcsSceneDocument(
            layers = listOf(
                PcsLayer(id = "back", type = "shape", x = 0f, y = 0f, width = 200f, height = 200f),
                PcsLayer(id = "front", type = "text", x = 40f, y = 40f, width = 80f, height = 40f),
            ),
            layerOrder = listOf("back", "front"),
        )
        assertEquals("front", PcsLayerHitTest.hitTestLayer(scene, 50f, 50f))
    }

    @Test
    fun lockedLayerStillSelectable() {
        val scene = PcsSceneDocument(
            layers = listOf(
                PcsLayer(id = "locked", type = "text", locked = true, x = 0f, y = 0f, width = 100f, height = 40f),
            ),
            layerOrder = listOf("locked"),
        )
        assertEquals("locked", PcsLayerHitTest.hitTestLayer(scene, 10f, 10f))
    }

    @Test
    fun groupChildHitTest() {
        val child = PcsLayer(id = "child", type = "image", x = 10f, y = 10f, width = 80f, height = 80f)
        val group = PcsLayer(id = "g", type = "group", children = listOf(child))
        val scene = PcsSceneDocument(layers = listOf(group), layerOrder = listOf("g"))
        assertEquals("child", PcsLayerHitTest.hitTestLayer(scene, 20f, 20f))
    }

    @Test
    fun resizeHandleDetectedNearCorner() {
        val layer = PcsLayer(id = "a", type = "shape", x = 0f, y = 0f, width = 100f, height = 100f)
        val scene = PcsSceneDocument(layers = listOf(layer), layerOrder = listOf("a"))
        val handle = PcsLayerHitTest.hitTestHandle(scene, "a", 4f, 4f, touchRadiusScene = 12f)
        assertEquals(PcsHandleKind.ResizeTopLeft, handle)
    }

    @Test
    fun snapAngleSnapsTo45() {
        assertEquals(45f, PcsLayerHitTest.snapAngle(43f, true), 0.01f)
    }

    @Test
    fun sceneDeltaToLocalWithRotation() {
        val layer = PcsLayer(id = "a", type = "shape", x = 0f, y = 0f, width = 100f, height = 100f, rotation = 90f)
        val (ldx, ldy) = PcsLayerHitTest.sceneDeltaToLocal(layer, 10f, 0f)
        assertEquals(10f, sqrt(ldx * ldx + ldy * ldy), 0.5f)
    }
}
