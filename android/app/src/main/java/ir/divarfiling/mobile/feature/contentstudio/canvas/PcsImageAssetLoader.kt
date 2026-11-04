package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import coil.ImageLoader
import coil.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PcsLoadedAssets(
    val propertyImages: List<Bitmap?> = emptyList(),
    val overlay: Bitmap? = null,
    val brandLogo: Bitmap? = null,
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
        brandLogoUrl: String?,
        scene: PcsSceneDocument?,
    ): PcsLoadedAssets = withContext(Dispatchers.IO) {
        val indices = collectImageIndices(scene).ifEmpty { setOf(0) }
        val urls = buildList {
            property.images.forEach { if (it.isNotBlank()) add(it) }
            if (isEmpty() && property.coverImage.isNotBlank()) add(property.coverImage)
        }
        val failed = mutableListOf<String>()
        val propertyImages: List<Bitmap?> = if (urls.isEmpty()) {
            emptyList()
        } else {
            urls.mapIndexed { index, url ->
                if (index !in indices) null
                else loadBitmap(url) ?: run {
                    failed.add(url)
                    null
                }
            }
        }
        val overlay = overlayUrl.takeIf { it.isNotBlank() }?.let { url ->
            loadBitmap(url) ?: run {
                failed.add(url)
                null
            }
        }
        val logo = brandLogoUrl?.takeIf { it.isNotBlank() }?.let { loadBitmap(it) }
        PcsLoadedAssets(
            propertyImages = propertyImages,
            overlay = overlay,
            brandLogo = logo,
            failedUrls = failed.distinct(),
        )
    }

    private fun collectImageIndices(scene: PcsSceneDocument?): Set<Int> {
        if (scene == null) return emptySet()
        val out = mutableSetOf<Int>()
        fun walk(layer: PcsLayer) {
            layer.imageIndex?.let { out.add(it) }
            layer.children?.forEach { walk(it) }
        }
        scene.layers.forEach { walk(it) }
        return out
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
