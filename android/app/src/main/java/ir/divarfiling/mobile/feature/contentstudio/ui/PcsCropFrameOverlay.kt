package ir.divarfiling.mobile.feature.contentstudio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLayer
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsPosterViewportTransform
import ir.divarfiling.mobile.feature.contentstudio.editor.PcsLayerHitTest
import kotlin.math.max

@Composable
fun PcsCropFrameOverlay(
    layer: PcsLayer,
    viewport: PcsPosterViewportTransform.Viewport?,
    modifier: Modifier = Modifier,
) {
    if (viewport == null) return
    val bounds = PcsLayerHitTest.layerBounds(layer)
    Canvas(modifier.fillMaxSize()) {
        val scale = viewport.fitScale
        val topLeft = Offset(
            viewport.offsetLeft + bounds.x * scale,
            viewport.offsetTop + bounds.y * scale,
        )
        val w = bounds.width * scale
        val h = bounds.height * scale
        val cx = topLeft.x + w / 2f
        val cy = topLeft.y + h / 2f
        rotate(bounds.rotation, pivot = Offset(cx, cy)) {
            drawRect(Color.Black.copy(alpha = 0.35f))
            drawRect(
                color = Color.White,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(w, h),
                style = Stroke(3f),
            )
            drawRect(
                color = Color(0x802563EB),
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(w, h),
            )
        }
    }
}
