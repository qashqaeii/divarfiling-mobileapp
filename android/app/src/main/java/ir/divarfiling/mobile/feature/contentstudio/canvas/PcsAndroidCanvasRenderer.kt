package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextDirectionHeuristics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

data class PcsRenderInput(
    val scene: PcsSceneDocument,
    val property: PcsStudioProperty,
    val assets: PcsLoadedAssets,
    val posterConfHeadline: String,
    val brandFooter: String,
    val canvasBackground: String,
)

@Singleton
class PcsAndroidCanvasRenderer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val supportedTypes = setOf(
        "shape", "image", "template_frame", "text", "bound_text", "logo", "decor", "group",
    )

    fun render(input: PcsRenderInput, width: Int, height: Int): Result<Bitmap> {
        if (input.assets.hasCriticalFailure) {
            return Result.failure(IllegalStateException("بارگذاری تصویر ناموفق بود"))
        }
        val unsupported = input.scene.layers.filter { it.type !in supportedTypes }.map { "${it.id}:${it.type}" }
        if (unsupported.isNotEmpty()) {
            return Result.failure(IllegalStateException("لایه پشتیبانی‌نشده: ${unsupported.first()}"))
        }
        val refW = (input.scene.canvas?.width ?: width).toFloat()
        val refH = (input.scene.canvas?.height ?: height).toFloat()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bg = input.canvasBackground.ifBlank { "#ffffff" }
        canvas.drawColor(PcsColorParser.parseColor(bg, android.graphics.Color.WHITE))
        val scale = minOf(width / refW, height / refH)
        val offsetX = (width - refW * scale) / 2f
        val offsetY = (height - refH * scale) / 2f
        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scale, scale)
        val order = input.scene.layerOrder.ifEmpty { input.scene.layers.map { it.id } }
        val byId = input.scene.layers.associateBy { it.id }
        for (id in order) {
            val layer = byId[id] ?: continue
            if (!layer.visible) continue
            if (layer.parentId != null) continue
            drawLayer(canvas, layer, input)
        }
        canvas.restore()
        return Result.success(bitmap)
    }

    private fun drawLayer(canvas: Canvas, layer: PcsLayer, input: PcsRenderInput) {
        val rect = RectF(layer.x, layer.y, layer.x + max(layer.width, 1f), layer.y + max(layer.height, 1f))
        val alpha = (layer.opacity.coerceIn(0f, 1f) * 255).toInt()
        canvas.save()
        canvas.rotate(layer.rotation, rect.centerX(), rect.centerY())
        when (layer.type) {
            "group" -> layer.children?.forEach { child ->
                if (child.visible) drawLayer(canvas, child, input)
            }
            "shape" -> drawShape(canvas, layer, rect, alpha)
            "image" -> drawImageLayer(canvas, layer, rect, alpha, input.assets.propertyImages)
            "template_frame" -> drawTemplateFrame(canvas, rect, alpha, input.assets.overlay)
            "text", "bound_text" -> drawTextLayer(canvas, layer, rect, alpha, input)
            "logo" -> drawLogoLayer(canvas, layer, rect, alpha, input.assets)
            "decor" -> drawDecorLayer(canvas, layer, rect, alpha, input.assets)
            else -> Unit
        }
        canvas.restore()
    }

    private fun drawShape(canvas: Canvas, layer: PcsLayer, rect: RectF, alpha: Int) {
        val style = layer.style
        val bg = style?.background ?: style?.fill
        val paint = PcsColorParser.parseBackgroundPaint(bg, rect.width(), rect.height(), android.graphics.Color.TRANSPARENT)
        paint.alpha = alpha
        val radius = style?.borderRadius ?: layer.borderRadius ?: 0f
        canvas.drawRoundRect(rect, radius, radius, paint)
    }

    private fun drawTemplateFrame(canvas: Canvas, rect: RectF, alpha: Int, overlay: Bitmap?) {
        val bmp = overlay ?: return
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.alpha = alpha }
        canvas.drawBitmap(bmp, null, rect, paint)
    }

    private fun drawImageLayer(
        canvas: Canvas,
        layer: PcsLayer,
        rect: RectF,
        alpha: Int,
        images: List<Bitmap?>,
    ) {
        val idx = layer.imageIndex ?: 0
        val bmp = images.getOrNull(idx) ?: images.firstNotNullOfOrNull { it } ?: return
        drawBitmapInRect(canvas, bmp, layer, rect, alpha)
    }

    private fun drawLogoLayer(canvas: Canvas, layer: PcsLayer, rect: RectF, alpha: Int, assets: PcsLoadedAssets) {
        val bmp = assets.brandLogo ?: assets.propertyImages.firstNotNullOfOrNull { it } ?: return
        drawBitmapInRect(canvas, bmp, layer, rect, alpha)
    }

    private fun drawDecorLayer(canvas: Canvas, layer: PcsLayer, rect: RectF, alpha: Int, assets: PcsLoadedAssets) {
        val bmp = assets.overlay ?: assets.propertyImages.firstNotNullOfOrNull { it } ?: return
        drawBitmapInRect(canvas, bmp, layer, rect, alpha)
    }

    private fun drawBitmapInRect(canvas: Canvas, bmp: Bitmap, layer: PcsLayer, rect: RectF, alpha: Int) {
        if (bmp.isRecycled) return
        val style = layer.style
        val radius = style?.borderRadius ?: layer.borderRadius ?: 0f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.alpha = alpha }
        val src = PcsImageCrop.computeCropSrc(bmp, rect, style, layer.objectFit)
        if (radius > 0f) {
            canvas.save()
            val path = Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) }
            canvas.clipPath(path)
            canvas.drawBitmap(bmp, src, rect, paint)
            canvas.restore()
        } else {
            canvas.drawBitmap(bmp, src, rect, paint)
        }
    }

    private fun drawTextLayer(
        canvas: Canvas,
        layer: PcsLayer,
        rect: RectF,
        alpha: Int,
        input: PcsRenderInput,
    ) {
        val text = PcsContentResolver.resolveLayerText(
            layer,
            input.property,
            input.posterConfHeadline,
            input.brandFooter,
        )
        if (text.isBlank()) return
        val style = layer.style
        val fontSize = style?.fontSize ?: 32f
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = PcsColorParser.parseColor(style?.fill, android.graphics.Color.WHITE)
            this.alpha = alpha
            textSize = fontSize
            typeface = PcsFontResolver.typeface(context, style?.fontKey, style?.fontWeight)
        }
        val bgRaw = style?.background
        if (!bgRaw.isNullOrBlank()) {
            val bgPaint = PcsColorParser.parseBackgroundPaint(bgRaw, rect.width(), rect.height(), android.graphics.Color.TRANSPARENT)
            bgPaint.alpha = alpha
            val radius = style.borderRadius ?: if (style.textPreset == "capsule") rect.height() / 2f else 0f
            canvas.drawRoundRect(rect, radius, radius, bgPaint)
        }
        val alignment = when (style?.align?.lowercase()) {
            "center" -> Layout.Alignment.ALIGN_CENTER
            "left" -> Layout.Alignment.ALIGN_NORMAL
            else -> Layout.Alignment.ALIGN_OPPOSITE
        }
        val staticLayout = StaticLayout.Builder
            .obtain(text, 0, text.length, textPaint, rect.width().toInt().coerceAtLeast(1))
            .setAlignment(alignment)
            .setLineSpacing(0f, style?.lineHeight ?: 1.2f)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .build()
        canvas.save()
        canvas.translate(rect.left, rect.top)
        staticLayout.draw(canvas)
        canvas.restore()
    }
}
