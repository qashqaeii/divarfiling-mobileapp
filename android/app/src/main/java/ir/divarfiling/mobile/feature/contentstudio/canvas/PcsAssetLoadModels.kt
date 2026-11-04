package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.graphics.Bitmap

enum class PcsAssetKind {
    PROPERTY_IMAGE,
    OVERLAY,
    BRAND_LOGO,
}

enum class PcsAssetFailureReason {
    INVALID_URL,
    HTTP,
    NETWORK,
    DECODE,
    EMPTY_RESPONSE,
}

data class PcsAssetFailure(
    val kind: PcsAssetKind,
    val reason: PcsAssetFailureReason,
    val host: String? = null,
    val httpStatus: Int? = null,
    val imageIndex: Int? = null,
)

data class PcsLoadedAssets(
    val propertyImages: List<Bitmap?> = emptyList(),
    val overlay: Bitmap? = null,
    val brandLogo: Bitmap? = null,
    val failures: List<PcsAssetFailure> = emptyList(),
    val missingPropertyIndices: Set<Int> = emptySet(),
) {
    val overlayFailed: Boolean get() = failures.any { it.kind == PcsAssetKind.OVERLAY }

    val hasPropertyImageFailures: Boolean get() = missingPropertyIndices.isNotEmpty()

    /** @deprecated Use granular failure flags; rendering continues with placeholders / fallback. */
    val hasCriticalFailure: Boolean get() = false

    val failedUrls: List<String> get() = emptyList()
}
