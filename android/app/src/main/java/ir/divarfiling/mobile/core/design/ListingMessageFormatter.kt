package ir.divarfiling.mobile.core.design

import ir.divarfiling.mobile.core.network.LinkedListingDto
import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.core.network.ListingDto

/**
 * پیام حرفه‌ای و مینیمال برای ارسال به مشتری — بدون لینک دیوار.
 */
object ListingMessageFormatter {

    fun fromListing(listing: ListingDto, note: String = ""): String = build(
        title = listing.title,
        price = listing.price,
        deposit = listing.deposit,
        rent = listing.rent,
        area = listing.area,
        rooms = listing.rooms,
        district = listing.district,
        city = listing.city,
        yearBuilt = null,
        floor = null,
        advertiserType = listing.advertiserType,
        note = note,
    )

    fun fromDetail(listing: ListingDetailDto, note: String = ""): String = build(
        title = listing.title,
        price = listing.price,
        deposit = listing.deposit,
        rent = listing.rent,
        area = listing.area,
        rooms = listing.rooms,
        district = listing.district,
        city = listing.city,
        yearBuilt = listing.yearBuilt,
        floor = listing.floor,
        advertiserType = listing.advertiserType,
        note = note,
    )

    fun fromLinked(listing: LinkedListingDto, note: String = ""): String {
        val priceLong = listing.price?.filter { it.isDigit() }?.toLongOrNull()
        val areaInt = listing.area?.filter { it.isDigit() }?.toIntOrNull()
        return build(
            title = listing.title,
            price = priceLong,
            deposit = null,
            rent = null,
            area = areaInt,
            rooms = null,
            district = null,
            city = null,
            yearBuilt = null,
            floor = null,
            advertiserType = null,
            note = note.ifBlank { listing.notes.orEmpty() },
        )
    }

    private fun build(
        title: String?,
        price: Long?,
        deposit: Long?,
        rent: Long?,
        area: Int?,
        rooms: Int?,
        district: String?,
        city: String?,
        yearBuilt: String?,
        floor: String?,
        advertiserType: String?,
        note: String,
    ): String {
        val lines = mutableListOf<String>()
        lines += "🏠 ${title?.trim().orEmpty().ifBlank { "فایل پیشنهادی" }}"
        val location = listOfNotNull(district?.trim(), city?.trim()).filter { it.isNotBlank() }.joinToString("، ")
        if (location.isNotBlank()) lines += "📍 $location"
        price?.takeIf { it > 0 }?.let { lines += "💰 ${FormatUtils.formatPriceToman(it)}" }
        deposit?.takeIf { it > 0 }?.let { lines += "🔑 ودیعه: ${FormatUtils.formatPriceShort(it)}" }
        rent?.takeIf { it > 0 }?.let { lines += "📅 اجاره: ${FormatUtils.formatPriceShort(it)}" }
        area?.let { lines += "📐 ${FormatUtils.formatArea(it)}" }
        rooms?.let { lines += "🛏 ${FormatUtils.formatRooms(it)}" }
        yearBuilt?.takeIf { it.isNotBlank() }?.let { lines += "🏗 ساخت: $it" }
        floor?.takeIf { it.isNotBlank() }?.let { lines += "🏢 طبقه: $it" }
        advertiserType?.takeIf { it.isNotBlank() }?.let { lines += "👤 $it" }
        note.trim().takeIf { it.isNotBlank() }?.let { lines += "\n📝 $it" }
        lines += "\n— پیشنهاد مشاور شما | Divar Filing"
        return lines.joinToString("\n")
    }
}
