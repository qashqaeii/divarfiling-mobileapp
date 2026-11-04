package ir.divarfiling.mobile.feature.contentstudio.editor

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsBinding
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLayer
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneDocument
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsTextStyle
import kotlin.math.max
import kotlin.math.min

object PcsLayerTransformOps {

    private const val MIN_W = 8f
    private const val MIN_H = 8f

    fun resizeLayer(
        layer: PcsLayer,
        handle: PcsHandleKind,
        localDx: Float,
        localDy: Float,
    ): PcsLayer {
        var x = layer.x
        var y = layer.y
        var w = max(layer.width, MIN_W)
        var h = max(layer.height, MIN_H)
        val aspect = w / h
        val keepAspect = PcsLayerEditorRules.maintainsAspectOnCornerResize(layer) &&
            handle in setOf(
                PcsHandleKind.ResizeTopLeft,
                PcsHandleKind.ResizeTopRight,
                PcsHandleKind.ResizeBottomRight,
                PcsHandleKind.ResizeBottomLeft,
            )
        when (handle) {
            PcsHandleKind.ResizeTopLeft -> {
                x += localDx
                y += localDy
                w -= localDx
                h -= localDy
            }
            PcsHandleKind.ResizeTop -> {
                y += localDy
                h -= localDy
            }
            PcsHandleKind.ResizeTopRight -> {
                y += localDy
                w += localDx
                h -= localDy
            }
            PcsHandleKind.ResizeRight -> w += localDx
            PcsHandleKind.ResizeBottomRight -> {
                w += localDx
                h += localDy
            }
            PcsHandleKind.ResizeBottom -> h += localDy
            PcsHandleKind.ResizeBottomLeft -> {
                x += localDx
                w -= localDx
                h += localDy
            }
            PcsHandleKind.ResizeLeft -> {
                x += localDx
                w -= localDx
            }
            else -> return layer
        }
        if (w < MIN_W) {
            x -= (MIN_W - w)
            w = MIN_W
        }
        if (h < MIN_H) {
            y -= (MIN_H - h)
            h = MIN_H
        }
        if (keepAspect) {
            val newAspect = w / h
            if (newAspect > aspect) {
                h = w / aspect
            } else {
                w = h * aspect
            }
            w = max(w, MIN_W)
            h = max(h, MIN_H)
        }
        return layer.copy(x = x, y = y, width = w, height = h)
    }

    fun clampCropStyle(style: PcsTextStyle?): PcsTextStyle {
        val base = style ?: PcsTextStyle()
        return base.copy(
            cropPanX = (base.cropPanX ?: 50f).coerceIn(0f, 100f),
            cropPanY = (base.cropPanY ?: 50f).coerceIn(0f, 100f),
            cropZoom = (base.cropZoom ?: 1f).coerceIn(0.5f, 3f),
            objectFit = base.objectFit?.takeIf { it in setOf("cover", "contain") } ?: "cover",
            brightness = base.brightness?.coerceIn(40f, 160f),
            blur = base.blur?.coerceIn(0f, 12f),
            opacity = base.opacity?.coerceIn(0f, 1f),
        )
    }

    fun mergeStylePatch(existing: PcsTextStyle?, patch: PcsTextStyle): PcsTextStyle {
        val e = existing ?: PcsTextStyle()
        return clampCropStyle(
            e.copy(
                fontKey = patch.fontKey ?: e.fontKey,
                fontSize = patch.fontSize ?: e.fontSize,
                fontWeight = patch.fontWeight ?: e.fontWeight,
                fill = patch.fill ?: e.fill,
                align = patch.align ?: e.align,
                direction = patch.direction ?: e.direction,
                lineHeight = patch.lineHeight ?: e.lineHeight,
                background = patch.background ?: e.background,
                borderRadius = patch.borderRadius ?: e.borderRadius,
                objectFit = patch.objectFit ?: e.objectFit,
                cropPanX = patch.cropPanX ?: e.cropPanX,
                cropPanY = patch.cropPanY ?: e.cropPanY,
                cropZoom = patch.cropZoom ?: e.cropZoom,
                fontStyle = patch.fontStyle ?: e.fontStyle,
                textDecoration = patch.textDecoration ?: e.textDecoration,
                brightness = patch.brightness ?: e.brightness,
                blur = patch.blur ?: e.blur,
                opacity = patch.opacity ?: e.opacity,
            ),
        )
    }
}

object PcsSceneLayerTree {
    fun updateLayer(scene: PcsSceneDocument, layerId: String, transform: (PcsLayer) -> PcsLayer): PcsSceneDocument {
        return scene.copy(
            layers = scene.layers.map { updateInTree(it, layerId, transform) ?: it },
        )
    }

    fun replaceLayer(scene: PcsSceneDocument, layerId: String, updated: PcsLayer): PcsSceneDocument {
        return updateLayer(scene, layerId) { updated }
    }

    private fun updateInTree(layer: PcsLayer, id: String, transform: (PcsLayer) -> PcsLayer): PcsLayer? {
        if (layer.id == id) return transform(layer)
        val children = layer.children ?: return null
        var changed = false
        val mapped = children.map { child ->
            updateInTree(child, id, transform)?.also { changed = true } ?: child
        }
        return if (changed) layer.copy(children = mapped) else null
    }

    fun findLayer(scene: PcsSceneDocument, layerId: String): PcsLayer? =
        PcsLayerHitTest.findLayer(scene, layerId)
}
