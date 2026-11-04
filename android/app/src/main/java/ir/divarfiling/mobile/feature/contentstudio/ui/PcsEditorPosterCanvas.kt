package ir.divarfiling.mobile.feature.contentstudio.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import android.graphics.Bitmap
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLayer
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsPosterViewportTransform
import ir.divarfiling.mobile.feature.contentstudio.editor.PcsCanvasInteractionMode
import ir.divarfiling.mobile.feature.contentstudio.editor.PcsGuideLine
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun PcsEditorPosterCanvas(
    bitmap: Bitmap?,
    sceneWidth: Int,
    sceneHeight: Int,
    interactionMode: PcsCanvasInteractionMode,
    selectedLayer: PcsLayer?,
    activeGuides: List<PcsGuideLine>,
    rotateHintDegrees: Float?,
    onTapScene: (x: Float, y: Float) -> Unit,
    onDragSceneStart: (x: Float, y: Float) -> Unit,
    onDragScene: (dx: Float, dy: Float) -> Unit,
    onDragSceneEnd: () -> Unit,
    onCropPan: (dx: Float, dy: Float) -> Unit = { _, _ -> },
    onCropZoom: (pinchFactor: Float) -> Unit = {},
    showCropFrame: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var userScale by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    fun viewport(): PcsPosterViewportTransform.Viewport? =
        PcsPosterViewportTransform.computeViewport(
            boxSize.width,
            boxSize.height,
            sceneWidth,
            sceneHeight,
            userScale,
            pan.x,
            pan.y,
        )

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { boxSize = it }
            .pointerInput(interactionMode, boxSize, sceneWidth, sceneHeight) {
                coroutineScope {
                    when (interactionMode) {
                        PcsCanvasInteractionMode.EditLayer -> {
                            launch {
                                detectTapGestures { tap ->
                                    val vp = viewport() ?: return@detectTapGestures
                                    val mapped = PcsPosterViewportTransform.screenToScene(
                                        tap.x,
                                        tap.y,
                                        vp,
                                        sceneWidth,
                                        sceneHeight,
                                    ) ?: return@detectTapGestures
                                    onTapScene(mapped.first, mapped.second)
                                }
                            }
                            launch {
                                detectDragGestures(
                                    onDragStart = { start ->
                                        val vp = viewport() ?: return@detectDragGestures
                                        val mapped = PcsPosterViewportTransform.screenToScene(
                                            start.x,
                                            start.y,
                                            vp,
                                            sceneWidth,
                                            sceneHeight,
                                        )
                                        if (mapped != null) {
                                            onDragSceneStart(mapped.first, mapped.second)
                                        }
                                    },
                                    onDragEnd = { onDragSceneEnd() },
                                    onDragCancel = { onDragSceneEnd() },
                                ) { change, dragAmount ->
                                    change.consume()
                                    val vp = viewport() ?: return@detectDragGestures
                                    val (dx, dy) = PcsPosterViewportTransform.screenDeltaToScene(
                                        dragAmount.x,
                                        dragAmount.y,
                                        vp,
                                    )
                                    onDragScene(dx, dy)
                                }
                            }
                        }
                        PcsCanvasInteractionMode.CropImage -> {
                            launch {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val vp = viewport() ?: return@detectDragGestures
                                    val (dx, dy) = PcsPosterViewportTransform.screenDeltaToScene(
                                        dragAmount.x,
                                        dragAmount.y,
                                        vp,
                                    )
                                    onCropPan(dx, dy)
                                }
                            }
                            launch {
                                detectTransformGestures { _, _, zoom, _ ->
                                    onCropZoom(zoom)
                                }
                            }
                        }
                        PcsCanvasInteractionMode.PanZoom -> {
                            launch {
                                detectTransformGestures { _, panChange, zoom, _ ->
                                    userScale = (userScale * zoom).coerceIn(1f, 4f)
                                    pan = if (userScale > 1f) pan + panChange else Offset.Zero
                                    if (userScale <= 1f) pan = Offset.Zero
                                }
                            }
                            launch {
                                detectTapGestures(
                                    onDoubleTap = {
                                        userScale = 1f
                                        pan = Offset.Zero
                                    },
                                )
                            }
                        }
                    }
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
                        scaleX = userScale
                        scaleY = userScale
                        translationX = pan.x
                        translationY = pan.y
                    },
            )
            PcsEditorSelectionOverlay(
                selectedLayer = if (showCropFrame) null else selectedLayer,
                viewport = viewport(),
                sceneWidth = sceneWidth,
                sceneHeight = sceneHeight,
                activeGuides = activeGuides,
                rotateHintDegrees = rotateHintDegrees,
                modifier = Modifier.fillMaxSize(),
            )
            if (showCropFrame && selectedLayer != null) {
                PcsCropFrameOverlay(
                    layer = selectedLayer,
                    viewport = viewport(),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
