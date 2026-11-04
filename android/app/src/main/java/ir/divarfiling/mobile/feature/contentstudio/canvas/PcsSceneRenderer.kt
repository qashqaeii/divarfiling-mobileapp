package ir.divarfiling.mobile.feature.contentstudio.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlin.math.max

/**
 * Compose preview renderer — shared coordinate model with export pipeline (PcsCoordinateTransform).
 * Image/template_frame layers require Coil/async paint in screen layer (placeholder rect here).
 */
@Composable
fun PcsSceneCanvasView(
    scene: PcsSceneDocument,
    property: PcsStudioProperty,
    posterConfHeadline: String,
    brandFooter: String,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val refW = (scene.canvas?.width ?: 1080).toFloat()
    val refH = (scene.canvas?.height ?: 1920).toFloat()
    val order = scene.layerOrder.ifEmpty { scene.layers.map { it.id } }
    val layerMap = remember(scene) { scene.layers.associateBy { it.id } }

    Canvas(modifier = modifier.fillMaxSize()) {
        val transform = PcsCoordinateTransform.fitCenter(refW, refH, size.width, size.height)
        for (layerId in order) {
            val layer = layerMap[layerId] ?: continue
            if (!layer.visible) continue
            drawLayer(layer, transform, property, posterConfHeadline, brandFooter, textMeasurer)
        }
    }
}

private fun DrawScope.drawLayer(
    layer: PcsLayer,
    transform: PcsViewportTransform,
    property: PcsStudioProperty,
    posterConfHeadline: String,
    brandFooter: String,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    val rect = transform.mapRect(layer.x, layer.y, max(layer.width, 1f), max(layer.height, 1f))
    val alpha = layer.opacity.coerceIn(0f, 1f)
    rotate(layer.rotation, pivot = Offset(rect.left + rect.width / 2f, rect.top + rect.height / 2f)) {
        when (layer.type) {
            "shape" -> {
                val color = parseColor(layer.style?.fill ?: layer.fill?.toString()) ?: Color.Gray
                drawRoundRect(
                    color = color.copy(alpha = alpha),
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height),
                    cornerRadius = CornerRadius(layer.borderRadius ?: 0f),
                )
            }
            "text", "bound_text" -> {
                val text = PcsContentResolver.resolveLayerText(layer, property, posterConfHeadline, brandFooter)
                if (text.isBlank()) return@rotate
                val style = layer.style
                val fontSize = (style?.fontSize ?: 32f) * transform.scale
                val fill = parseColor(style?.fill) ?: Color.White
                val align = when (style?.align?.lowercase()) {
                    "center" -> TextAlign.Center
                    "left" -> TextAlign.Left
                    else -> TextAlign.Right
                }
                val textLayout = textMeasurer.measure(
                    text = text,
                    style = TextStyle(
                        color = fill.copy(alpha = alpha),
                        fontSize = fontSize.sp,
                        textAlign = align,
                    ),
                    maxLines = 12,
                    constraints = androidx.compose.ui.unit.Constraints(maxWidth = rect.width.toInt()),
                )
                drawText(
                    textLayout,
                    topLeft = Offset(rect.left, rect.top),
                )
            }
            "image", "template_frame", "logo", "decor" -> {
                drawRoundRect(
                    color = Color(0x22FFFFFF),
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height),
                    cornerRadius = CornerRadius(4f),
                )
            }
            "group" -> {
                layer.children?.forEach { child ->
                    drawLayer(child, transform, property, posterConfHeadline, brandFooter, textMeasurer)
                }
            }
        }
    }
}

private fun parseColor(raw: String?): Color? {
    if (raw.isNullOrBlank()) return null
    val s = raw.trim()
    return try {
        when {
            s.startsWith("#") -> Color(android.graphics.Color.parseColor(s))
            s.startsWith("rgba") -> Color.Gray
            else -> Color(android.graphics.Color.parseColor(s))
        }
    } catch (_: IllegalArgumentException) {
        null
    }
}
