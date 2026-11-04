package ir.divarfiling.mobile.feature.contentstudio.editor

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLayer
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneDocument

data class PcsLayerTreeRow(
    val layer: PcsLayer,
    val depth: Int,
    val parentGroupId: String?,
    val isGroup: Boolean,
)

object PcsLayerTreeBuilder {

    fun buildRows(
        scene: PcsSceneDocument,
        expandedGroupIds: Set<String>,
    ): List<PcsLayerTreeRow> {
        val order = scene.layerOrder.ifEmpty { scene.layers.map { it.id } }
        val byId = scene.layers.associateBy { it.id }
        val out = ArrayList<PcsLayerTreeRow>()
        fun appendLayer(layer: PcsLayer, depth: Int, parentId: String?) {
            val isGroup = layer.type == "group"
            out.add(PcsLayerTreeRow(layer, depth, parentId, isGroup))
            if (isGroup && layer.id in expandedGroupIds) {
                layer.children.orEmpty().forEach { child ->
                    appendLayer(child, depth + 1, layer.id)
                }
            }
        }
        order.forEach { id -> byId[id]?.let { appendLayer(it, 0, null) } }
        return out
    }

    fun collectGroupIds(scene: PcsSceneDocument): List<String> {
        return scene.layers.filter { it.type == "group" }.map { it.id }
    }
}
