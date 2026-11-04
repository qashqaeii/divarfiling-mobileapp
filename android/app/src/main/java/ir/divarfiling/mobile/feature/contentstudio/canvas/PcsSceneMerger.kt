package ir.divarfiling.mobile.feature.contentstudio.canvas

/**
 * Manifest scene is structural source of truth; draft contributes user overrides only.
 */
object PcsSceneMerger {

    fun mergeManifestWithDraft(base: PcsSceneDocument, draft: PcsSceneDocument?): PcsSceneDocument {
        if (draft == null) return copyScene(base)
        val draftById = draft.layers.associateBy { it.id }
        val mergedLayers = base.layers.map { baseLayer ->
            mergeLayer(baseLayer, draftById[baseLayer.id])
        }
        val order = when {
            draft.layerOrder.isNotEmpty() && draft.layerOrder.toSet() == mergedLayers.map { it.id }.toSet() ->
                draft.layerOrder
            base.layerOrder.isNotEmpty() -> base.layerOrder
            else -> mergedLayers.map { it.id }
        }
        return base.copy(
            layers = mergedLayers,
            layerOrder = order,
            canvas = draft.canvas?.copy() ?: base.canvas?.copy(),
            schemaVersion = base.schemaVersion,
        )
    }

    private fun mergeLayer(base: PcsLayer, draft: PcsLayer?): PcsLayer {
        if (draft == null) return copyLayer(base)
        return base.copy(
            x = draft.x,
            y = draft.y,
            width = draft.width,
            height = draft.height,
            rotation = draft.rotation,
            opacity = draft.opacity,
            visible = draft.visible,
            locked = draft.locked,
            text = draft.text ?: base.text,
            binding = draft.binding ?: base.binding,
            style = mergeStyle(base.style, draft.style),
            imageIndex = draft.imageIndex ?: base.imageIndex,
            contentContract = base.contentContract ?: draft.contentContract,
            templateRole = base.templateRole ?: draft.templateRole,
            assetRef = base.assetRef ?: draft.assetRef,
            objectFit = draft.objectFit ?: base.objectFit,
            name = draft.name ?: base.name,
            children = base.children?.map { child ->
                mergeLayer(child, draft.children?.find { it.id == child.id })
            } ?: draft.children?.map { copyLayer(it) },
        )
    }

    private fun mergeStyle(base: PcsTextStyle?, draft: PcsTextStyle?): PcsTextStyle? {
        if (draft == null) return base?.copy()
        if (base == null) return draft.copy()
        return base.copy(
            fontKey = draft.fontKey ?: base.fontKey,
            fontSize = draft.fontSize ?: base.fontSize,
            fontWeight = draft.fontWeight ?: base.fontWeight,
            fill = draft.fill ?: base.fill,
            align = draft.align ?: base.align,
            direction = draft.direction ?: base.direction,
            lineHeight = draft.lineHeight ?: base.lineHeight,
            background = draft.background ?: base.background,
            borderRadius = draft.borderRadius ?: base.borderRadius,
            objectFit = draft.objectFit ?: base.objectFit,
            cropPanX = draft.cropPanX ?: base.cropPanX,
            cropPanY = draft.cropPanY ?: base.cropPanY,
            cropZoom = draft.cropZoom ?: base.cropZoom,
            textPreset = draft.textPreset ?: base.textPreset,
        )
    }

    private fun copyScene(scene: PcsSceneDocument): PcsSceneDocument = scene.copy(
        layers = scene.layers.map { copyLayer(it) },
        layerOrder = scene.layerOrder.toList(),
        canvas = scene.canvas?.copy(),
    )

    private fun copyLayer(layer: PcsLayer): PcsLayer = layer.copy(
        style = layer.style?.copy(),
        binding = layer.binding?.copy(),
        contentContract = layer.contentContract?.copy(),
        children = layer.children?.map { copyLayer(it) },
    )
}
