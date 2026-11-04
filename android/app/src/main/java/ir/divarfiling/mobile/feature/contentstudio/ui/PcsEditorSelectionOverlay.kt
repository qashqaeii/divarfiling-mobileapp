package ir.divarfiling.mobile.feature.contentstudio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import ir.divarfiling.mobile.feature.contentstudio.editor.PcsLayerHitTest
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLayer
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsPosterViewportTransform
import ir.divarfiling.mobile.feature.contentstudio.editor.PcsGuideLine
import ir.divarfiling.mobile.feature.contentstudio.editor.PcsLayerEditorRules
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import android.graphics.Paint as AndroidPaint
import kotlin.math.max

@Composable
fun PcsEditorSelectionOverlay(
    selectedLayer: PcsLayer?,
    viewport: PcsPosterViewportTransform.Viewport?,
    sceneWidth: Int,
    sceneHeight: Int,
    activeGuides: List<PcsGuideLine>,
    rotateHintDegrees: Float?,
    modifier: Modifier = Modifier,
) {
    if (selectedLayer == null || viewport == null) return
    val bounds = PcsLayerHitTest.layerBounds(selectedLayer)
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val scale = viewport.fitScale
            fun sceneToScreen(sx: Float, sy: Float): Offset {
                return Offset(
                    viewport.offsetLeft + sx * scale,
                    viewport.offsetTop + sy * scale,
                )
            }
            activeGuides.forEach { guide ->
                when (guide.orientation) {
                    PcsGuideLine.Orientation.Vertical -> {
                        val x = viewport.offsetLeft + guide.positionScene * scale
                        drawLine(
                            Color(0xFF2563EB),
                            Offset(x, viewport.offsetTop),
                            Offset(x, viewport.offsetTop + viewport.drawnHeight),
                            strokeWidth = 1.5f,
                        )
                    }
                    PcsGuideLine.Orientation.Horizontal -> {
                        val y = viewport.offsetTop + guide.positionScene * scale
                        drawLine(
                            Color(0xFF2563EB),
                            Offset(viewport.offsetLeft, y),
                            Offset(viewport.offsetLeft + viewport.drawnWidth, y),
                            strokeWidth = 1.5f,
                        )
                    }
                }
            }
            val topLeft = sceneToScreen(bounds.x, bounds.y)
            val w = bounds.width * scale
            val h = bounds.height * scale
            val cx = topLeft.x + w / 2f
            val cy = topLeft.y + h / 2f
            rotate(bounds.rotation, pivot = Offset(cx, cy)) {
                drawRect(
                    color = Color(0xFF2563EB),
                    topLeft = topLeft,
                    size = androidx.compose.ui.geometry.Size(w, h),
                    style = Stroke(width = 2f),
                )
                if (PcsLayerEditorRules.canTransform(selectedLayer)) {
                    val handleR = 6f
                    listOf(
                        topLeft,
                        Offset(topLeft.x + w, topLeft.y),
                        Offset(topLeft.x + w, topLeft.y + h),
                        Offset(topLeft.x, topLeft.y + h),
                    ).forEach { pt ->
                        drawCircle(Color.White, handleR, pt)
                        drawCircle(Color(0xFF2563EB), handleR, pt, style = Stroke(2f))
                    }
                    if (PcsLayerEditorRules.supportsRotate(selectedLayer)) {
                        val rotPt = Offset(cx, topLeft.y - 28f)
                        drawLine(Color(0xFF2563EB), Offset(cx, topLeft.y), rotPt, 2f)
                        drawCircle(Color(0xFF2563EB), 7f, rotPt)
                    }
                }
            }
            rotateHintDegrees?.let { deg ->
                val label = "${deg.toInt()}°"
                drawIntoCanvas { canvas ->
                    val paint = AndroidPaint().apply {
                        color = Color(0xFF2563EB).toArgb()
                        textSize = 28f
                        isAntiAlias = true
                    }
                    canvas.nativeCanvas.drawText(label, cx + 12f, topLeft.y - 36f, paint)
                }
            }
        }
    }
}
