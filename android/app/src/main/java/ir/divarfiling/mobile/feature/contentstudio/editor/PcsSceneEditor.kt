package ir.divarfiling.mobile.feature.contentstudio.editor

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneMerger
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsBinding
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLayer
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneDocument
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsTextStyle

enum class PcsCanvasInteractionMode {
    PanZoom,
    EditLayer,
    CropImage,
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
        return PcsSceneMerger.mergeManifestWithDraft(base, draft)
    }

    private fun copyLayer(layer: PcsLayer): PcsLayer = layer.copy(
        style = layer.style?.copy(),
        binding = layer.binding?.copy(),
        contentContract = layer.contentContract?.copy(),
        children = layer.children?.map { copyLayer(it) },
    )

    fun updateLayer(scene: PcsSceneDocument, layerId: String, transform: (PcsLayer) -> PcsLayer): PcsSceneDocument {
        return PcsSceneLayerTree.updateLayer(scene, layerId, transform)
    }

    fun moveLayer(scene: PcsSceneDocument, layerId: String, dx: Float, dy: Float): PcsSceneDocument {
        return updateLayer(scene, layerId) { it.copy(x = it.x + dx, y = it.y + dy) }
    }

    fun resizeLayerWithHandle(
        scene: PcsSceneDocument,
        layerId: String,
        handle: PcsHandleKind,
        localDx: Float,
        localDy: Float,
    ): PcsSceneDocument {
        return updateLayer(scene, layerId) { layer ->
            PcsLayerTransformOps.resizeLayer(layer, handle, localDx, localDy)
        }
    }

    fun setLayerRotation(scene: PcsSceneDocument, layerId: String, degrees: Float): PcsSceneDocument {
        return updateLayer(scene, layerId) { it.copy(rotation = degrees) }
    }

    fun rotateLayer(scene: PcsSceneDocument, layerId: String, deltaDeg: Float): PcsSceneDocument {
        return updateLayer(scene, layerId) { it.copy(rotation = it.rotation + deltaDeg) }
    }

    fun setLayerVisible(scene: PcsSceneDocument, layerId: String, visible: Boolean): PcsSceneDocument {
        val layer = PcsSceneLayerTree.findLayer(scene, layerId) ?: return copyScene(scene)
        if (!PcsLayerEditorRules.canToggleVisibility(layer)) return copyScene(scene)
        return updateLayer(scene, layerId) { it.copy(visible = visible) }
    }

    fun setLayerLocked(scene: PcsSceneDocument, layerId: String, locked: Boolean): PcsSceneDocument {
        val layer = PcsSceneLayerTree.findLayer(scene, layerId) ?: return copyScene(scene)
        if (!PcsLayerEditorRules.canToggleLock(layer)) return copyScene(scene)
        return updateLayer(scene, layerId) { it.copy(locked = locked) }
    }

    fun reorderLayer(scene: PcsSceneDocument, layerId: String, direction: Int): PcsSceneDocument {
        val layer = PcsSceneLayerTree.findLayer(scene, layerId) ?: return copyScene(scene)
        if (!PcsLayerEditorRules.canReorder(layer)) return copyScene(scene)
        val order = scene.layerOrder.ifEmpty { scene.layers.map { it.id } }.toMutableList()
        val idx = order.indexOf(layerId)
        if (idx < 0) return copyScene(scene)
        val newIdx = (idx + direction).coerceIn(0, order.lastIndex)
        if (newIdx == idx) return copyScene(scene)
        order.removeAt(idx)
        order.add(newIdx, layerId)
        return copyScene(scene).copy(layerOrder = order)
    }

    fun setLayerTextOverride(scene: PcsSceneDocument, layerId: String, text: String): PcsSceneDocument {
        return updateLayer(scene, layerId) { layer ->
            val binding = layer.binding ?: PcsBinding(source = "poster", field = "headline", mode = "linked")
            val mode = when (binding.mode) {
                "linked" -> "override"
                else -> binding.mode
            }
            layer.copy(text = text, binding = binding.copy(mode = mode))
        }
    }

    fun setLayerTextDetached(scene: PcsSceneDocument, layerId: String, text: String): PcsSceneDocument {
        return updateLayer(scene, layerId) { layer ->
            val binding = layer.binding ?: PcsBinding(source = "poster", field = "headline", mode = "linked")
            layer.copy(text = text, binding = binding.copy(mode = "detached"))
        }
    }

    fun restoreLayerTextLinked(scene: PcsSceneDocument, baseScene: PcsSceneDocument, layerId: String): PcsSceneDocument {
        val baseLayer = PcsSceneLayerTree.findLayer(baseScene, layerId) ?: return copyScene(scene)
        return updateLayer(scene, layerId) { layer ->
            layer.copy(
                text = baseLayer.text,
                binding = baseLayer.binding?.copy(mode = "linked") ?: layer.binding?.copy(mode = "linked"),
            )
        }
    }

    fun resetLayerFromBase(scene: PcsSceneDocument, baseScene: PcsSceneDocument, layerId: String): PcsSceneDocument {
        val baseLayer = PcsSceneLayerTree.findLayer(baseScene, layerId) ?: return copyScene(scene)
        return updateLayer(scene, layerId) { copyLayer(baseLayer) }
    }

    fun setLayerStyle(scene: PcsSceneDocument, layerId: String, style: PcsTextStyle): PcsSceneDocument {
        return updateLayer(scene, layerId) { layer ->
            layer.copy(style = PcsLayerTransformOps.mergeStylePatch(layer.style, style))
        }
    }

    fun setLayerOpacity(scene: PcsSceneDocument, layerId: String, opacity: Float): PcsSceneDocument {
        return updateLayer(scene, layerId) { it.copy(opacity = opacity.coerceIn(0f, 1f)) }
    }

    fun setImageIndex(scene: PcsSceneDocument, layerId: String, index: Int, maxIndex: Int): PcsSceneDocument {
        val safe = index.coerceIn(0, maxIndex.coerceAtLeast(0))
        return updateLayer(scene, layerId) { it.copy(imageIndex = safe) }
    }

    fun setObjectFit(scene: PcsSceneDocument, layerId: String, fit: String): PcsSceneDocument {
        val normalized = if (fit == "contain") "contain" else "cover"
        return updateLayer(scene, layerId) { layer ->
            layer.copy(
                objectFit = normalized,
                style = PcsLayerTransformOps.mergeStylePatch(
                    layer.style,
                    PcsTextStyle(objectFit = normalized),
                ),
            )
        }
    }

    fun setCropStyle(
        scene: PcsSceneDocument,
        layerId: String,
        panX: Float?,
        panY: Float?,
        zoom: Float?,
    ): PcsSceneDocument {
        return updateLayer(scene, layerId) { layer ->
            val patch = PcsTextStyle(
                cropPanX = panX,
                cropPanY = panY,
                cropZoom = zoom,
            )
            layer.copy(style = PcsLayerTransformOps.mergeStylePatch(layer.style, patch))
        }
    }

    fun hitTestLayer(scene: PcsSceneDocument, x: Float, y: Float): String? =
        PcsLayerHitTest.hitTestLayer(scene, x, y)
}
