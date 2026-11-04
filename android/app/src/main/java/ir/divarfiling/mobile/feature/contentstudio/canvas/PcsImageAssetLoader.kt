package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        templateId: String = "",
    ): PcsLoadedAssets = withContext(Dispatchers.IO) {
        val indices = collectImageIndices(scene).ifEmpty { setOf(0) }
        val urls = buildList {
            property.images.forEach { if (it.isNotBlank()) add(it) }
            if (isEmpty() && property.coverImage.isNotBlank()) add(property.coverImage)
        }
        val failures = mutableListOf<PcsAssetFailure>()
        val missingIndices = mutableSetOf<Int>()
        val propertyImages: List<Bitmap?> = if (urls.isEmpty()) {
            emptyList()
        } else {
            urls.mapIndexed { index, url ->
                if (index !in indices) {
                    null
                } else {
                    when (val outcome = loadBitmap(url, PcsAssetKind.PROPERTY_IMAGE, templateId, index)) {
                        is LoadOutcome.Success -> outcome.bitmap
                        is LoadOutcome.Failure -> {
                            failures.add(outcome.failure)
                            missingIndices.add(index)
                            null
                        }
                    }
                }
            }
        }
        val overlay = overlayUrl.takeIf { it.isNotBlank() }?.let { url ->
            when (val outcome = loadBitmap(url, PcsAssetKind.OVERLAY, templateId, imageIndex = null)) {
                is LoadOutcome.Success -> outcome.bitmap
                is LoadOutcome.Failure -> {
                    failures.add(outcome.failure)
                    null
                }
            }
        }
        val logo = brandLogoUrl?.takeIf { it.isNotBlank() }?.let { url ->
            when (val outcome = loadBitmap(url, PcsAssetKind.BRAND_LOGO, templateId, imageIndex = null)) {
                is LoadOutcome.Success -> outcome.bitmap
                is LoadOutcome.Failure -> {
                    failures.add(outcome.failure)
                    null
                }
            }
        }
        PcsLoadedAssets(
            propertyImages = propertyImages,
            overlay = overlay,
            brandLogo = logo,
            failures = failures,
            missingPropertyIndices = missingIndices,
        )
    }

    private sealed class LoadOutcome {
        data class Success(val bitmap: Bitmap) : LoadOutcome()
        data class Failure(val failure: PcsAssetFailure) : LoadOutcome()
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

    private suspend fun loadBitmap(
        url: String,
        kind: PcsAssetKind,
        templateId: String,
        imageIndex: Int?,
    ): LoadOutcome {
        val normalized = PcsMediaUrlResolver.normalizeAssetUrl(url)
        if (normalized.isNullOrBlank()) {
            val failure = PcsAssetFailure(
                kind = kind,
                reason = PcsAssetFailureReason.INVALID_URL,
                imageIndex = imageIndex,
            )
            PcsAssetDiagnostics.logFailure(kind, failure.reason, null, null, templateId, imageIndex, null)
            return LoadOutcome.Failure(failure)
        }
        val host = PcsAssetDiagnostics.hostForLog(normalized)
        val request = ImageRequest.Builder(context)
            .data(normalized)
            .allowHardware(false)
            .build()
        return when (val result = imageLoader.execute(request)) {
            is SuccessResult -> {
                val bitmap = drawableToBitmap(result.drawable)
                if (bitmap == null) {
                    val failure = PcsAssetFailure(
                        kind = kind,
                        reason = PcsAssetFailureReason.DECODE,
                        host = host,
                        imageIndex = imageIndex,
                    )
                    PcsAssetDiagnostics.logFailure(
                        kind,
                        failure.reason,
                        host,
                        null,
                        templateId,
                        imageIndex,
                        "bitmap decode",
                    )
                    LoadOutcome.Failure(failure)
                } else {
                    LoadOutcome.Success(bitmap)
                }
            }
            is ErrorResult -> {
                val reason = PcsAssetDiagnostics.failureReason(result.throwable)
                val httpStatus = PcsAssetDiagnostics.httpStatusOf(result.throwable)
                val failure = PcsAssetFailure(
                    kind = kind,
                    reason = reason,
                    host = host,
                    httpStatus = httpStatus,
                    imageIndex = imageIndex,
                )
                PcsAssetDiagnostics.logFailure(
                    kind,
                    reason,
                    host,
                    httpStatus,
                    templateId,
                    imageIndex,
                    result.throwable.message,
                )
                LoadOutcome.Failure(failure)
            }
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            val source = drawable.bitmap
            if (source != null && !source.isRecycled) {
                return source.copy(Bitmap.Config.ARGB_8888, false)
            }
        }
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        return runCatching {
            if (width > 0 && height > 0) {
                drawable.toBitmap(width = width, height = height, config = Bitmap.Config.ARGB_8888)
            } else {
                drawable.toBitmap(config = Bitmap.Config.ARGB_8888)
            }
        }.getOrNull()
    }
}
