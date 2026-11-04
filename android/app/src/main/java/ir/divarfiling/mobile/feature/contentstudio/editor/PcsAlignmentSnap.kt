package ir.divarfiling.mobile.feature.contentstudio.editor

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLayer
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneDocument
import kotlin.math.abs

data class PcsGuideLine(
    val orientation: Orientation,
    val positionScene: Float,
) {
    enum class Orientation { Vertical, Horizontal }
}

data class PcsSnapResult(
    val dx: Float,
    val dy: Float,
    val guides: List<PcsGuideLine>,
)

object PcsAlignmentSnap {

    fun snapMove(
        layer: PcsLayer,
        rawDx: Float,
        rawDy: Float,
        scene: PcsSceneDocument,
        canvasWidth: Float,
        canvasHeight: Float,
        thresholdScene: Float,
        enabled: Boolean,
    ): PcsSnapResult {
        if (!enabled) return PcsSnapResult(rawDx, rawDy, emptyList())
        val w = layer.width.coerceAtLeast(1f)
        val h = layer.height.coerceAtLeast(1f)
        var nx = layer.x + rawDx
        var ny = layer.y + rawDy
        val guides = mutableListOf<PcsGuideLine>()
        val left = nx
        val right = nx + w
        val top = ny
        val bottom = ny + h
        val cx = nx + w / 2f
        val cy = ny + h / 2f

        val vTargets = mutableListOf<Float>()
        val hTargets = mutableListOf<Float>()
        vTargets.add(0f)
        vTargets.add(canvasWidth / 2f)
        vTargets.add(canvasWidth)
        hTargets.add(0f)
        hTargets.add(canvasHeight / 2f)
        hTargets.add(canvasHeight)

        for (entry in PcsLayerHitTest.paintOrder(scene)) {
            val other = entry.layer
            if (other.id == layer.id || !other.visible) continue
            val ow = other.width.coerceAtLeast(1f)
            val oh = other.height.coerceAtLeast(1f)
            vTargets.add(other.x)
            vTargets.add(other.x + ow / 2f)
            vTargets.add(other.x + ow)
            hTargets.add(other.y)
            hTargets.add(other.y + oh / 2f)
            hTargets.add(other.y + oh)
        }

        var bestDx = rawDx
        var bestDy = rawDy
        var bestV = thresholdScene
        var bestH = thresholdScene

        for (target in vTargets) {
            listOf(left, cx, right).forEach { edge ->
                val delta = target - edge
                if (abs(delta) <= bestV) {
                    bestV = abs(delta)
                    bestDx = rawDx + delta
                    guides.removeAll { it.orientation == PcsGuideLine.Orientation.Vertical }
                    guides.add(PcsGuideLine(PcsGuideLine.Orientation.Vertical, target))
                }
            }
        }
        nx = layer.x + bestDx
        ny = layer.y + bestDy
        val nLeft = nx
        val nRight = nx + w
        val nTop = ny
        val nBottom = ny + h
        val nCx = nx + w / 2f
        val nCy = ny + h / 2f

        for (target in hTargets) {
            listOf(nTop, nCy, nBottom).forEach { edge ->
                val delta = target - edge
                if (abs(delta) <= bestH) {
                    bestH = abs(delta)
                    bestDy = rawDy + delta
                    guides.removeAll { it.orientation == PcsGuideLine.Orientation.Horizontal }
                    guides.add(PcsGuideLine(PcsGuideLine.Orientation.Horizontal, target))
                }
            }
        }
        return PcsSnapResult(bestDx, bestDy, guides.toList())
    }
}
