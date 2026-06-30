package ir.divarfiling.mobile.core.filing

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.network.ListingDto

data class ListingAmenity(
    val label: String,
    val icon: ImageVector? = null,
    @DrawableRes val iconRes: Int? = null,
)

object ListingAmenityUtils {

    fun buildAmenities(listing: ListingDto, limit: Int = 8): List<ListingAmenity> {
        val items = linkedSetOf<ListingAmenity>()

        addCoreAmenity(items, "پارکینگ", listing.hasParking, iconRes = DfDecorIcons.Car)
        addCoreAmenity(items, "انباری", listing.hasStorage, iconRes = DfDecorIcons.Storage)
        addCoreAmenity(items, "آسانسور", listing.hasElevator, iconRes = DfDecorIcons.Elevator)

        listing.area?.let {
            items.add(ListingAmenity(FormatUtils.formatArea(it), iconRes = DfDecorIcons.Ruler))
        }
        listing.rooms?.let {
            items.add(ListingAmenity(FormatUtils.formatRooms(it), icon = DfIcons.Bed))
        }
        listing.floor?.takeIf { it.isNotBlank() }?.let {
            items.add(ListingAmenity("طبقه $it", iconRes = DfDecorIcons.Building))
        }
        listing.yearBuilt?.takeIf { it.isNotBlank() }?.let {
            items.add(ListingAmenity("ساخت $it", iconRes = DfDecorIcons.Calendar))
        }

        listing.featureHighlights.forEach { highlight ->
            val amenity = highlight.toAmenity()
            if (items.none { it.label == amenity.label }) {
                items.add(amenity)
            }
        }

        return items.take(limit)
    }

    private fun addCoreAmenity(
        items: MutableSet<ListingAmenity>,
        name: String,
        value: Boolean?,
        icon: ImageVector? = null,
        @DrawableRes iconRes: Int? = null,
    ) {
        val label = ListingSpecUtils.boolAmenityChipLabel(name, value) ?: return
        items.add(ListingAmenity(label, icon = icon, iconRes = iconRes))
    }

    private fun String.toAmenity(): ListingAmenity {
        val normalized = trim()
        val iconRes = when {
            contains("پارکینگ", ignoreCase = true) -> DfDecorIcons.Car
            contains("انباری", ignoreCase = true) -> DfDecorIcons.Storage
            contains("آسانسور", ignoreCase = true) -> DfDecorIcons.Elevator
            contains("گرمایش", ignoreCase = true) -> DfDecorIcons.Zap
            contains("کف", ignoreCase = true) -> DfDecorIcons.LayoutGrid
            contains("سال", ignoreCase = true) -> DfDecorIcons.Calendar
            contains("طبقه", ignoreCase = true) -> DfDecorIcons.Building
            contains("متر", ignoreCase = true) -> DfDecorIcons.Ruler
            else -> null
        }
        val icon = when {
            iconRes != null -> null
            contains("بالکن", ignoreCase = true) -> DfIcons.Compass
            contains("سرویس", ignoreCase = true) -> DfIcons.Bath
            contains("سرمایش", ignoreCase = true) -> DfIcons.Cloud
            contains("کابینت", ignoreCase = true) -> DfIcons.LayoutList
            contains("اتاق", ignoreCase = true) -> DfIcons.Bed
            else -> DfIcons.Check
        }
        val label = normalized
            .removePrefix("مناسب: ")
            .let { if (it.length > 22) it.take(20) + "…" else it }
        return ListingAmenity(label, icon = icon, iconRes = iconRes)
    }
}
