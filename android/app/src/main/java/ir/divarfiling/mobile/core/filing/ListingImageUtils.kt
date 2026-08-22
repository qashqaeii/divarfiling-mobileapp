package ir.divarfiling.mobile.core.filing

import ir.divarfiling.mobile.core.image.DivarImageUrlUtils
import ir.divarfiling.mobile.core.image.ImageUrlFormatter
import ir.divarfiling.mobile.core.network.ListingDetailDto

object ListingImageUtils {
    fun buildGalleryUrls(listing: ListingDetailDto): List<String> =
        buildGalleryUrls(listing.thumbnailUrl, listing.images)

    fun buildGalleryUrls(thumbnailUrl: String?, images: List<String>): List<String> {
        val candidates = buildList {
            thumbnailUrl?.takeIf { it.isNotBlank() }?.let { add(it) }
            addAll(images.filter { it.isNotBlank() })
        }
        return DivarImageUrlUtils.deduplicate(
            candidates.mapNotNull { ImageUrlFormatter.normalize(it) },
        )
    }

    /** URLهای پشتیبان کاور دیتاست — بدون تکرار thumbnail اصلی. */
    fun datasetCoverFallbackUrls(thumbnailUrl: String?, thumbnailUrls: List<String>): List<String> {
        val primary = thumbnailUrl?.trim().orEmpty()
        return thumbnailUrls
            .map { it.trim() }
            .filter { it.isNotBlank() && it != primary }
    }
}
