package ir.divarfiling.mobile.feature.contentstudio

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLoadedAssets

data class ContentStudioAssetUiPatch(
    val overlayLoadFailed: Boolean = false,
    val assetNotice: String? = null,
)

object ContentStudioAssetUiMapper {

    fun mapLoadedAssets(assets: PcsLoadedAssets): ContentStudioAssetUiPatch {
        val notices = buildList {
            if (assets.overlayFailed) {
                add("قالب گرافیکی (Overlay) بارگذاری نشد — می‌توانید دوباره تلاش کنید.")
            }
            if (assets.hasPropertyImageFailures) {
                add("برخی تصاویر ملک در دسترس نبودند؛ جای خالی نمایش داده می‌شود.")
            }
        }
        return ContentStudioAssetUiPatch(
            overlayLoadFailed = assets.overlayFailed,
            assetNotice = notices.joinToString(" ").takeIf { it.isNotBlank() },
        )
    }

    /** Session/API 404 must not be conflated with Coil asset failures. */
    fun isSessionLevelNotFound(errorCode: String?): Boolean {
        val code = errorCode?.uppercase().orEmpty()
        return code == "NOT_FOUND" || code == "HTTP_404"
    }

    fun exportBlockedMessage(assets: PcsLoadedAssets): String? {
        if (assets.propertyImages.isEmpty()) return null
        val allMissing = assets.propertyImages.all { it == null }
        if (!allMissing) return null
        if (assets.missingPropertyIndices.size != assets.propertyImages.size) return null
        if (assets.propertyImages.size <= 1) return null
        return "هیچ تصویر ملک برای خروجی در دسترس نیست."
    }
}
