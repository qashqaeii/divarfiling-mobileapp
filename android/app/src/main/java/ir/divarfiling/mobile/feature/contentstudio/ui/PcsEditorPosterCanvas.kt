package ir.divarfiling.mobile.feature.contentstudio.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import ir.divarfiling.mobile.feature.contentstudio.editor.PcsCanvasInteractionMode

@Composable
fun PcsEditorPosterCanvas(
    bitmap: Bitmap?,
    sceneWidth: Int,
    sceneHeight: Int,
    interactionMode: PcsCanvasInteractionMode,
    onTapScene: (Float, Float) -> Unit,
    onDragScene: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { boxSize = it }
            .pointerInput(interactionMode) {
                if (interactionMode == PcsCanvasInteractionMode.EditLayer) {
                    detectTapGestures { tap ->
                        mapTapToScene(tap, boxSize, sceneWidth, sceneHeight, scale, offset)?.let(onTapScene)
                    }
                    detectDragGestures { change, drag ->
                        change.consume()
                        val factor = sceneWidth.toFloat() / boxSize.width.coerceAtLeast(1)
                        onDragScene(drag.x * factor / scale, drag.y * factor / scale)
                    }
                } else {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 4f)
                        offset = if (scale > 1f) offset + pan else Offset.Zero
                    }
                    detectTapGestures(onDoubleTap = {
                        scale = 1f
                        offset = Offset.Zero
                    })
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
            )
        }
    }
}

private fun mapTapToScene(
    tap: Offset,
    boxSize: IntSize,
    sceneW: Int,
    sceneH: Int,
    scale: Float,
    offset: Offset,
): Offset? {
    if (boxSize.width == 0 || boxSize.height == 0) return null
    val fitScale = minOf(boxSize.width / sceneW.toFloat(), boxSize.height / sceneH.toFloat()) * scale
    val drawnW = sceneW * fitScale
    val drawnH = sceneH * fitScale
    val left = (boxSize.width - drawnW) / 2f + offset.x
    val top = (boxSize.height - drawnH) / 2f + offset.y
    val x = (tap.x - left) / fitScale
    val y = (tap.y - top) / fitScale
    if (x < 0 || y < 0 || x > sceneW || y > sceneH) return null
    return Offset(x, y)
}
