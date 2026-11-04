package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextDirectionHeuristics
import androidx.core.content.res.ResourcesCompat
import coil.ImageLoader
import coil.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.R
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context

data class PcsLoadedAssets(
    val propertyImages: List<Bitmap> = emptyList(),
    val overlay: Bitmap? = null,
    val failedUrls: List<String> = emptyList(),
) {
    val hasCriticalFailure: Boolean get() = failedUrls.isNotEmpty()
}

@Singleton
class PcsImageAssetLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader,
) {
    suspend fun load(
        property: PcsStudioProperty,
        overlayUrl: String,
    ): PcsLoadedAssets = withContext(Dispatchers.IO) {
        val failed = mutableListOf<String>()
        val urls = buildList {
            property.images.forEach { if (it.isNotBlank()) add(it) }
            if (isEmpty() && property.coverImage.isNotBlank()) add(property.coverImage)
        }
        val images = urls.mapNotNull { url ->
            loadBitmap(url)?.also { } ?: run {
                failed.add(url)
                null
            }
        }
        val overlay = overlayUrl.takeIf { it.isNotBlank() }?.let { url ->
            loadBitmap(url) ?: run {
                failed.add(url)
                null
            }
        }
        PcsLoadedAssets(propertyImages = images, overlay = overlay, failedUrls = failed)
    }

    private suspend fun loadBitmap(url: String): Bitmap? {
        val request = ImageRequest.Builder(context).data(url).allowHardware(false).build()
        val result = imageLoader.execute(request)
        val drawable = result.drawable ?: return null
        return Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888,
        ).also { bmp ->
            Canvas(bmp).apply { drawable.setBounds(0, 0, width, height); drawable.draw(this) }
        }
    }
}

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
    fun render(input: PcsRenderInput, width: Int, height: Int): Result<Bitmap> {
        if (input.assets.hasCriticalFailure) {
            return Result.failure(IllegalStateException("بارگذاری تصویر ناموفق بود"))
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
            drawLayer(canvas, layer, input, scale)
        }
        canvas.restore()
        return Result.success(bitmap)
    }

    private fun drawLayer(canvas: Canvas, layer: PcsLayer, input: PcsRenderInput, viewportScale: Float) {
        val rect = RectF(layer.x, layer.y, layer.x + max(layer.width, 1f), layer.y + max(layer.height, 1f))
        val alpha = (layer.opacity.coerceIn(0f, 1f) * 255).toInt()
        canvas.save()
        canvas.rotate(layer.rotation, rect.centerX(), rect.centerY())
        when (layer.type) {
            "shape" -> drawShape(canvas, layer, rect, alpha)
            "image" -> drawImageLayer(canvas, layer, rect, alpha, input.assets.propertyImages)
            "template_frame" -> drawTemplateFrame(canvas, rect, alpha, input.assets.overlay)
            "text", "bound_text" -> drawTextLayer(canvas, layer, rect, alpha, input, viewportScale)
            "logo", "decor" -> drawImageLayer(canvas, layer, rect, alpha, input.assets.propertyImages)
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
        images: List<Bitmap>,
    ) {
        val idx = layer.imageIndex ?: 0
        val bmp = images.getOrNull(idx) ?: images.firstOrNull() ?: return
        val style = layer.style
        val radius = style?.borderRadius ?: layer.borderRadius ?: 0f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.alpha = alpha }
        val src = computeCropSrc(bmp, rect, style)
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

    private fun computeCropSrc(bmp: Bitmap, dst: RectF, style: PcsTextStyle?): RectF {
        val zoom = style?.cropZoom ?: 1f
        val panX = (style?.cropPanX ?: 50f) / 100f
        val panY = (style?.cropPanY ?: 50f) / 100f
        val bw = bmp.width.toFloat()
        val bh = bmp.height.toFloat()
        val scale = max(dst.width() / bw, dst.height() / bh) * zoom
        val sw = dst.width() / scale
        val sh = dst.height() / scale
        var left = (bw - sw) * panX
        var top = (bh - sh) * panY
        left = left.coerceIn(0f, max(0f, bw - sw))
        top = top.coerceIn(0f, max(0f, bh - sh))
        return RectF(left, top, left + sw, top + sh)
    }

    private fun drawTextLayer(
        canvas: Canvas,
        layer: PcsLayer,
        rect: RectF,
        alpha: Int,
        input: PcsRenderInput,
        viewportScale: Float,
    ) {
        val text = PcsContentResolver.resolveLayerText(
            layer,
            input.property,
            input.posterConfHeadline,
            input.brandFooter,
        )
        if (text.isBlank()) return
        val style = layer.style
        val fontSize = (style?.fontSize ?: 32f)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = PcsColorParser.parseColor(style?.fill, android.graphics.Color.WHITE)
            this.alpha = alpha
            textSize = fontSize
            typeface = when {
                (style?.fontWeight ?: 400) >= 800 -> ResourcesCompat.getFont(context, R.font.vazirmatn_bold)
                (style?.fontWeight ?: 400) >= 600 -> ResourcesCompat.getFont(context, R.font.vazirmatn_semibold)
                else -> ResourcesCompat.getFont(context, R.font.vazirmatn_regular)
            }
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
            .obtain(text, 0, text.length, textPaint, rect.width().toInt())
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
