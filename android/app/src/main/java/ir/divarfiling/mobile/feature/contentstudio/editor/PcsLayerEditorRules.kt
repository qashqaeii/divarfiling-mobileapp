package ir.divarfiling.mobile.feature.contentstudio.editor

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLayer

object PcsLayerEditorRules {
    fun isStructureProtected(layer: PcsLayer): Boolean =
        layer.type == "template_frame"

    fun canSelect(layer: PcsLayer): Boolean =
        layer.visible && !isStructureProtected(layer)

    fun canTransform(layer: PcsLayer): Boolean =
        !layer.locked && !isStructureProtected(layer)

    fun canToggleVisibility(layer: PcsLayer): Boolean =
        !isStructureProtected(layer)

    fun canToggleLock(layer: PcsLayer): Boolean =
        !isStructureProtected(layer)

    fun canReorder(layer: PcsLayer): Boolean =
        !isStructureProtected(layer)

    fun supportsResize(layer: PcsLayer): Boolean =
        canTransform(layer) && layer.type in setOf("text", "bound_text", "image", "shape", "logo", "decor")

    fun maintainsAspectOnCornerResize(layer: PcsLayer): Boolean =
        layer.type in setOf("image", "logo", "decor")

    fun supportsRotate(layer: PcsLayer): Boolean =
        canTransform(layer) && layer.type !in setOf("template_frame")
}
