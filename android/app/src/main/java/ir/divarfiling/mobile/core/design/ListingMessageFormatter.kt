package ir.divarfiling.mobile.core.design

import ir.divarfiling.mobile.core.network.LinkedListingDto
import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.core.network.ListingDto

/**
 * پیام حرفه‌ای برای ارسال به مشتری — بدون لینک دیوار و بدون برندینگ.
 */
object ListingMessageFormatter {

    fun fromListing(listing: ListingDto, note: String = ""): String =
        DossierShareFormatter.fromListing(
            listing,
            DossierShareOptions(customNote = note),
        )

    fun fromDetail(listing: ListingDetailDto, note: String = ""): String =
        DossierShareFormatter.fromDetail(
            listing,
            DossierShareOptions(customNote = note),
        )

    fun fromLinked(listing: LinkedListingDto, note: String = ""): String {
        val priceLong = listing.price?.filter { it.isDigit() }?.toLongOrNull()
        val areaInt = listing.area?.filter { it.isDigit() }?.toIntOrNull()
        return DossierShareFormatter.fromListing(
            ListingDto(
                token = listing.token,
                title = listing.title,
                price = priceLong,
                area = areaInt,
                district = null,
                city = null,
            ),
            DossierShareOptions(
                customNote = note.ifBlank { listing.notes.orEmpty() },
            ),
        )
    }
}
