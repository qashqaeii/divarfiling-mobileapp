package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.util.Log
import ir.divarfiling.mobile.core.image.ImageUrlFormatter
import java.io.IOException
import java.net.URI
import coil.network.HttpException as CoilHttpException
import retrofit2.HttpException as RetrofitHttpException

object PcsAssetDiagnostics {
    private const val TAG = "PcsAssetLoader"

    fun hostForLog(raw: String?): String? {
        val normalized = PcsMediaUrlResolver.normalizeAssetUrl(raw) ?: return null
        return runCatching { URI(normalized).host?.takeIf { it.isNotBlank() } }.getOrNull()
    }

    fun logFailure(
        kind: PcsAssetKind,
        reason: PcsAssetFailureReason,
        host: String?,
        httpStatus: Int?,
        templateId: String,
        imageIndex: Int?,
        decodeMessage: String?,
    ) {
        val indexPart = imageIndex?.let { " imageIndex=$it" }.orEmpty()
        val statusPart = httpStatus?.let { " httpStatus=$it" }.orEmpty()
        val hostPart = host?.let { " host=$it" }.orEmpty()
        val decodePart = decodeMessage?.let { msg ->
            val safe = msg.take(120).replace(Regex("https?://\\S+"), "<url>")
            " decode=$safe"
        }.orEmpty()
        val templatePart = templateId.takeIf { it.isNotBlank() }?.let { " template=$it" }.orEmpty()
        Log.w(
            TAG,
            "asset load failed type=$kind reason=$reason$hostPart$statusPart$indexPart$templatePart$decodePart",
        )
    }

    fun failureReason(throwable: Throwable?): PcsAssetFailureReason {
        if (throwable == null) return PcsAssetFailureReason.EMPTY_RESPONSE
        if (throwable is IOException) return PcsAssetFailureReason.NETWORK
        val httpCode = httpStatusOf(throwable)
        if (httpCode != null) return PcsAssetFailureReason.HTTP
        return PcsAssetFailureReason.DECODE
    }

    fun httpStatusOf(throwable: Throwable?): Int? {
        if (throwable == null) return null
        when (throwable) {
            is RetrofitHttpException -> return throwable.code()
            is CoilHttpException -> return throwable.response.code
        }
        val message = throwable.message.orEmpty()
        val match = Regex("HTTP \\((\\d+)\\)").find(message)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    fun normalizedHost(raw: String?): String? = hostForLog(raw)
}

object PcsMediaUrlResolver {
    fun normalizeAssetUrl(raw: String?): String? {
        val normalized = ImageUrlFormatter.normalize(raw) ?: return null
        return upgradeKnownOriginsToHttps(normalized)
    }

    fun resolveOverlayUrl(overlayUrl: String, overlayPath: String): String {
        overlayUrl.trim().takeIf { it.isNotBlank() }?.let { raw ->
            return normalizeAssetUrl(raw).orEmpty()
        }
        val path = overlayPath.trim().trimStart('/')
        if (path.isBlank()) return ""
        val staticPath = when {
            path.startsWith("static/") -> "/$path"
            path.startsWith("media/") -> "/$path"
            else -> "/static/$path"
        }
        return normalizeAssetUrl(staticPath).orEmpty()
    }

    private fun upgradeKnownOriginsToHttps(url: String): String {
        if (!url.startsWith("http://")) return url
        val host = runCatching { URI(url).host?.lowercase() }.getOrNull() ?: return url
        return if (host == "divarfiling.ir" || host.endsWith(".divarfiling.ir")) {
            "https://${url.removePrefix("http://")}"
        } else {
            url
        }
    }
}
