package ir.divarfiling.mobile.core.filing

import ir.divarfiling.mobile.core.network.ListingDetailDto

object ListingSpecUtils {

    fun boolFeatureLabel(value: Boolean?): String = when (value) {
        true -> "دارد"
        false -> "ندارد"
        null -> "—"
    }

    fun statusLabel(listing: ListingDetailDto): String =
        listing.unitStatus?.takeIf { it.isNotBlank() }
            ?: if (listing.isExpired) "منقضی" else "فعال"
}
