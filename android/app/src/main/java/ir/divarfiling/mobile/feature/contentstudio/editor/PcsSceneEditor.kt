package ir.divarfiling.mobile.feature.contentstudio.editor

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsBinding
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLayer
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneDocument
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsTextStyle
import kotlin.math.abs
import kotlin.math.max

enum class PcsCanvasInteractionMode {
    PanZoom,
    EditLayer,
}

class PcsSceneHistory(private val maxDepth: Int = 40) {
    private val undoStack = ArrayDeque<PcsSceneDocument>()
    private val redoStack = ArrayDeque<PcsSceneDocument>()

    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun pushBeforeChange(current: PcsSceneDocument) {
        undoStack.addLast(PcsSceneMutator.copyScene(current))
        while (undoStack.size > maxDepth) undoStack.removeFirst()
        redoStack.clear()
    }

    fun undo(current: PcsSceneDocument): PcsSceneDocument? {
        val prev = undoStack.removeLastOrNull() ?: return null
        redoStack.addLast(PcsSceneMutator.copyScene(current))
        return prev
    }

    fun redo(current: PcsSceneDocument): PcsSceneDocument? {
        val next = redoStack.removeLastOrNull() ?: return null
        undoStack.addLast(PcsSceneMutator.copyScene(current))
        return next
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}

object PcsSceneMutator {
    fun copyScene(scene: PcsSceneDocument): PcsSceneDocument = scene.copy(
        layers = scene.layers.map { copyLayer(it) },
        layerOrder = scene.layerOrder.toList(),
        canvas = scene.canvas?.copy(),
    )

    fun mergeBaseWithDraft(base: PcsSceneDocument, draft: PcsSceneDocument?): PcsSceneDocument {
        if (draft == null) return copyScene(base)
        return copyScene(draft)
    }

    private fun copyLayer(layer: PcsLayer): PcsLayer = layer.copy(
        style = layer.style?.copy(),
        binding = layer.binding?.copy(),
        contentContract = layer.contentContract?.copy(),
        children = layer.children?.map { copyLayer(it) },
    )

    fun updateLayer(scene: PcsSceneDocument, layerId: String, transform: (PcsLayer) -> PcsLayer): PcsSceneDocument {
        return copyScene(scene).copy(
            layers = scene.layers.map { layer ->
                if (layer.id == layerId) transform(copyLayer(layer)) else copyLayer(layer)
            },
        )
    }

    fun moveLayer(scene: PcsSceneDocument, layerId: String, dx: Float, dy: Float): PcsSceneDocument {
        return updateLayer(scene, layerId) { it.copy(x = it.x + dx, y = it.y + dy) }
    }

    fun resizeLayer(scene: PcsSceneDocument, layerId: String, dw: Float, dh: Float): PcsSceneDocument {
        return updateLayer(scene, layerId) {
            it.copy(width = max(8f, it.width + dw), height = max(8f, it.height + dh))
        }
    }

    fun rotateLayer(scene: PcsSceneDocument, layerId: String, deltaDeg: Float): PcsSceneDocument {
        return updateLayer(scene, layerId) { it.copy(rotation = it.rotation + deltaDeg) }
    }

    fun setLayerVisible(scene: PcsSceneDocument, layerId: String, visible: Boolean): PcsSceneDocument {
        return updateLayer(scene, layerId) { it.copy(visible = visible) }
    }

    fun setLayerLocked(scene: PcsSceneDocument, layerId: String, locked: Boolean): PcsSceneDocument {
        return updateLayer(scene, layerId) { it.copy(locked = locked) }
    }

    fun reorderLayer(scene: PcsSceneDocument, layerId: String, direction: Int): PcsSceneDocument {
        val order = scene.layerOrder.ifEmpty { scene.layers.map { it.id } }.toMutableList()
        val idx = order.indexOf(layerId)
        if (idx < 0) return copyScene(scene)
        val newIdx = (idx + direction).coerceIn(0, order.lastIndex)
        if (newIdx == idx) return copyScene(scene)
        order.removeAt(idx)
        order.add(newIdx, layerId)
        return copyScene(scene).copy(layerOrder = order)
    }

    fun setLayerTextDetached(scene: PcsSceneDocument, layerId: String, text: String): PcsSceneDocument {
        return updateLayer(scene, layerId) { layer ->
            val binding = layer.binding ?: PcsBinding(source = "poster", field = "headline", mode = "linked")
            layer.copy(
                text = text,
                binding = binding.copy(mode = "detached"),
            )
        }
    }

    fun setLayerStyle(scene: PcsSceneDocument, layerId: String, style: PcsTextStyle): PcsSceneDocument {
        return updateLayer(scene, layerId) { it.copy(style = style) }
    }

    fun setImageIndex(scene: PcsSceneDocument, layerId: String, index: Int): PcsSceneDocument {
        return updateLayer(scene, layerId) { it.copy(imageIndex = index.coerceAtLeast(0)) }
    }

    fun hitTestLayer(scene: PcsSceneDocument, x: Float, y: Float): String? {
        val order = scene.layerOrder.ifEmpty { scene.layers.map { it.id } }
        for (id in order.asReversed()) {
            val layer = scene.layers.find { it.id == id } ?: continue
            if (!layer.visible || layer.locked) continue
            if (containsPoint(layer, x, y)) return layer.id
        }
        return null
    }

    private fun containsPoint(layer: PcsLayer, x: Float, y: Float): Boolean {
        val left = layer.x
        val top = layer.y
        val right = layer.x + max(layer.width, 1f)
        val bottom = layer.y + max(layer.height, 1f)
        if (abs(layer.rotation) < 0.01f) {
            return x in left..right && y in top..bottom
        }
        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f
        val rad = Math.toRadians(-layer.rotation.toDouble())
        val dx = x - cx
        val dy = y - cy
        val rx = (dx * kotlin.math.cos(rad) - dy * kotlin.math.sin(rad) + cx)
        val ry = (dx * kotlin.math.sin(rad) + dy * kotlin.math.cos(rad) + cy)
        return rx in left..right && ry in top..bottom
    }
}
