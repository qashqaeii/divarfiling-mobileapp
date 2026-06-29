package ir.divarfiling.mobile.core.filing

import ir.divarfiling.mobile.core.image.ImageUrlFormatter
import ir.divarfiling.mobile.core.network.ListingDetailDto

object ListingImageUtils {
    private val divarHostHints = listOf(
        "divarcdn",
        "divar.ir",
        "webimage.divar",
    )

    fun isDivarImageUrl(url: String): Boolean {
        val lower = url.lowercase()
        return divarHostHints.any { lower.contains(it) }
    }

    fun buildGalleryUrls(listing: ListingDetailDto): List<String> =
        buildGalleryUrls(listing.thumbnailUrl, listing.images)

    fun buildGalleryUrls(thumbnailUrl: String?, images: List<String>): List<String> {
        val candidates = buildList {
            thumbnailUrl?.takeIf { it.isNotBlank() }?.let { add(it) }
            addAll(images.filter { it.isNotBlank() })
        }
            .mapNotNull { ImageUrlFormatter.normalize(it) }
            .distinct()

        if (candidates.isEmpty()) return emptyList()

        val divarUrls = candidates.filter(::isDivarImageUrl)
        val otherUrls = candidates.filterNot(::isDivarImageUrl)
        return when {
            divarUrls.isNotEmpty() -> divarUrls + otherUrls
            thumbnailUrl != null -> {
                ImageUrlFormatter.normalize(thumbnailUrl)?.let { listOf(it) + otherUrls.filter { it != thumbnailUrl } }
                    ?: candidates
            }
            else -> candidates
        }
    }
}
