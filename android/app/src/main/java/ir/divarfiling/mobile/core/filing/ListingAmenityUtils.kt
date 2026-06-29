package ir.divarfiling.mobile.core.filing

import androidx.compose.ui.graphics.vector.ImageVector
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.network.ListingDto

data class ListingAmenity(
    val label: String,
    val icon: ImageVector,
)

object ListingAmenityUtils {

    fun buildAmenities(listing: ListingDto, limit: Int = 6): List<ListingAmenity> {
        val items = linkedSetOf<ListingAmenity>()

        listing.area?.let {
            items.add(ListingAmenity(FormatUtils.formatArea(it), DfIcons.Ruler))
        }
        listing.rooms?.let {
            items.add(ListingAmenity(FormatUtils.formatRooms(it), DfIcons.Bed))
        }
        listing.floor?.takeIf { it.isNotBlank() }?.let {
            items.add(ListingAmenity("طبقه $it", DfIcons.Building))
        }
        listing.yearBuilt?.takeIf { it.isNotBlank() }?.let {
            items.add(ListingAmenity("ساخت $it", DfIcons.Calendar))
        }

        listing.featureHighlights.forEach { highlight ->
            val amenity = highlight.toAmenity()
            if (items.none { it.label == amenity.label }) {
                items.add(amenity)
            }
        }

        return items.take(limit)
    }

    private fun String.toAmenity(): ListingAmenity {
        val normalized = trim()
        val icon = when {
            contains("پارکینگ", ignoreCase = true) -> DfIcons.Car
            contains("انباری", ignoreCase = true) -> DfIcons.Database
            contains("آسانسور", ignoreCase = true) -> DfIcons.Layers
            contains("بالکن", ignoreCase = true) -> DfIcons.Compass
            contains("سرویس", ignoreCase = true) -> DfIcons.Bath
            contains("گرمایش", ignoreCase = true) -> DfIcons.Zap
            contains("سرمایش", ignoreCase = true) -> DfIcons.Cloud
            contains("کف", ignoreCase = true) -> DfIcons.LayoutGrid
            contains("کابینت", ignoreCase = true) -> DfIcons.LayoutList
            contains("سال", ignoreCase = true) -> DfIcons.Calendar
            contains("طبقه", ignoreCase = true) -> DfIcons.Building
            contains("اتاق", ignoreCase = true) -> DfIcons.Bed
            contains("متر", ignoreCase = true) -> DfIcons.Ruler
            else -> DfIcons.Check
        }
        val label = normalized
            .removePrefix("مناسب: ")
            .let { if (it.length > 22) it.take(20) + "…" else it }
        return ListingAmenity(label, icon)
    }
}
