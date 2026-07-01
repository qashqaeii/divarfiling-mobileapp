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
}
