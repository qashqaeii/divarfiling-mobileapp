package ir.divarfiling.mobile.feature.contentstudio.editor

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLayer
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneDocument
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

enum class PcsHandleKind {
    None,
    Move,
    Rotate,
    ResizeTopLeft,
    ResizeTop,
    ResizeTopRight,
    ResizeRight,
    ResizeBottomRight,
    ResizeBottom,
    ResizeBottomLeft,
    ResizeLeft,
}

object PcsLayerHitTest {

    data class HitTarget(
        val layerId: String,
        val handle: PcsHandleKind = PcsHandleKind.Move,
    )

    fun hitTestLayer(scene: PcsSceneDocument, sceneX: Float, sceneY: Float): String? {
        for (entry in paintOrder(scene)) {
            val layer = entry.layer
            if (!PcsLayerEditorRules.canSelect(layer)) continue
            if (containsPoint(layer, sceneX, sceneY)) return layer.id
        }
        return null
    }

    fun hitTestHandle(
        scene: PcsSceneDocument,
        selectedLayerId: String,
        sceneX: Float,
        sceneY: Float,
        touchRadiusScene: Float,
    ): PcsHandleKind {
        val layer = findLayer(scene, selectedLayerId) ?: return PcsHandleKind.None
        if (!PcsLayerEditorRules.canTransform(layer)) return PcsHandleKind.None
        val local = sceneToLocal(layer, sceneX, sceneY)
        val w = max(layer.width, 1f)
        val h = max(layer.height, 1f)
        val r = touchRadiusScene
        if (PcsLayerEditorRules.supportsRotate(layer)) {
            val rotY = -h / 2f - 48f
            if (hypot(local.first - w / 2f, local.second - rotY) <= r * 1.4f) {
                return PcsHandleKind.Rotate
            }
        }
        val corners = listOf(
            PcsHandleKind.ResizeTopLeft to (0f to 0f),
            PcsHandleKind.ResizeTopRight to (w to 0f),
            PcsHandleKind.ResizeBottomRight to (w to h),
            PcsHandleKind.ResizeBottomLeft to (0f to h),
        )
        for ((kind, pt) in corners) {
            if (hypot(local.first - pt.first, local.second - pt.second) <= r) return kind
        }
        if (PcsLayerEditorRules.supportsResize(layer)) {
            val edges = listOf(
                PcsHandleKind.ResizeTop to (w / 2f to 0f),
                PcsHandleKind.ResizeBottom to (w / 2f to h),
                PcsHandleKind.ResizeLeft to (0f to h / 2f),
                PcsHandleKind.ResizeRight to (w to h / 2f),
            )
            for ((kind, pt) in edges) {
                if (layer.type in setOf("text", "bound_text") &&
                    kind in setOf(PcsHandleKind.ResizeTop, PcsHandleKind.ResizeBottom)
                ) {
                    continue
                }
                if (hypot(local.first - pt.first, local.second - pt.second) <= r) return kind
            }
        }
        if (containsPoint(layer, sceneX, sceneY)) return PcsHandleKind.Move
        return PcsHandleKind.None
    }

    fun paintOrder(scene: PcsSceneDocument): List<LayerEntry> {
        val order = scene.layerOrder.ifEmpty { scene.layers.map { it.id } }
        val byId = scene.layers.associateBy { it.id }
        val out = ArrayList<LayerEntry>()
        fun walk(layer: PcsLayer) {
            if (layer.type == "group") {
                layer.children.orEmpty().asReversed().forEach { walk(it) }
            } else {
                out.add(LayerEntry(layer))
            }
        }
        order.asReversed().forEach { id -> byId[id]?.let { walk(it) } }
        return out
    }

    data class LayerEntry(val layer: PcsLayer)

    fun findLayer(scene: PcsSceneDocument, layerId: String): PcsLayer? {
        scene.layers.forEach { found -> findInTree(found, layerId)?.let { return it } }
        return null
    }

    private fun findInTree(layer: PcsLayer, id: String): PcsLayer? {
        if (layer.id == id) return layer
        layer.children?.forEach { child ->
            findInTree(child, id)?.let { return it }
        }
        return null
    }

    fun containsPoint(layer: PcsLayer, sceneX: Float, sceneY: Float): Boolean {
        val local = sceneToLocal(layer, sceneX, sceneY)
        val w = max(layer.width, 1f)
        val h = max(layer.height, 1f)
        return local.first in 0f..w && local.second in 0f..h
    }

    fun sceneToLocal(layer: PcsLayer, sceneX: Float, sceneY: Float): Pair<Float, Float> {
        val cx = layer.x + max(layer.width, 1f) / 2f
        val cy = layer.y + max(layer.height, 1f) / 2f
        val rad = Math.toRadians(-layer.rotation.toDouble())
        val dx = sceneX - cx
        val dy = sceneY - cy
        val rx = (dx * cos(rad) - dy * sin(rad) + cx - layer.x).toFloat()
        val ry = (dx * sin(rad) + dy * cos(rad) + cy - layer.y).toFloat()
        return rx to ry
    }

    fun localToScene(layer: PcsLayer, localX: Float, localY: Float): Pair<Float, Float> {
        val cx = layer.x + max(layer.width, 1f) / 2f
        val cy = layer.y + max(layer.height, 1f) / 2f
        val lx = layer.x + localX
        val ly = layer.y + localY
        val rad = Math.toRadians(layer.rotation.toDouble())
        val dx = lx - cx
        val dy = ly - cy
        val sx = (dx * cos(rad) - dy * sin(rad) + cx).toFloat()
        val sy = (dx * sin(rad) + dy * cos(rad) + cy).toFloat()
        return sx to sy
    }

    fun layerBounds(layer: PcsLayer): LayerBounds {
        return LayerBounds(
            x = layer.x,
            y = layer.y,
            width = max(layer.width, 1f),
            height = max(layer.height, 1f),
            rotation = layer.rotation,
        )
    }

    data class LayerBounds(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val rotation: Float,
    ) {
        val centerX: Float get() = x + width / 2f
        val centerY: Float get() = y + height / 2f
    }

    fun angleForRotateHandle(layer: PcsLayer, sceneX: Float, sceneY: Float): Float {
        val cx = layer.x + max(layer.width, 1f) / 2f
        val cy = layer.y + max(layer.height, 1f) / 2f
        val deg = Math.toDegrees(atan2((sceneY - cy).toDouble(), (sceneX - cx).toDouble())).toFloat()
        return deg + 90f
    }

    fun sceneDeltaToLocal(layer: PcsLayer, sceneDx: Float, sceneDy: Float): Pair<Float, Float> {
        val rad = Math.toRadians(-layer.rotation.toDouble())
        val ldx = (sceneDx * cos(rad) - sceneDy * sin(rad)).toFloat()
        val ldy = (sceneDx * sin(rad) + sceneDy * cos(rad)).toFloat()
        return ldx to ldy
    }

    fun snapAngle(degrees: Float, enabled: Boolean): Float {
        if (!enabled) return degrees
        val snaps = floatArrayOf(0f, 45f, 90f, 135f, 180f, -45f, -90f, -135f)
        val normalized = ((degrees % 360f) + 360f) % 360f
        for (s in snaps) {
            val target = ((s % 360f) + 360f) % 360f
            if (abs(normalized - target) <= 4f) return s
        }
        return degrees
    }
}
