package ir.divarfiling.mobile.core.filing

import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.core.network.ListingDto

object ListingSpecUtils {

    fun boolFeatureLabel(value: Boolean?): String = when (value) {
        true -> "دارد"
        false -> "ندارد"
        null -> "—"
    }

    /** برچسب کوتاه برای چیپ کارت لیست — فقط وقتی مقدار مشخص است. */
    fun boolAmenityChipLabel(name: String, value: Boolean?): String? = when (value) {
        true -> name
        false -> "$name: ندارد"
        null -> null
    }

    fun statusLabel(listing: ListingDetailDto): String =
        listing.unitStatus?.takeIf { it.isNotBlank() }
            ?: if (listing.isExpired) "منقضی" else "فعال"

    fun statusLabel(listing: ListingDto): String =
        listing.unitStatus?.takeIf { it.isNotBlank() } ?: "فعال"
}
