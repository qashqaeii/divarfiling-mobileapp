package ir.divarfiling.mobile.core.design

import ir.divarfiling.mobile.core.network.LinkedListingDto
import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.core.network.ListingDto

/**
 * پیام حرفه‌ای برای ارسال به مشتری — بدون لینک دیوار و بدون برندینگ.
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
        totalFloors = null,
        pricePerSqm = null,
        advertiserType = listing.advertiserType,
        businessType = listing.businessType,
        description = null,
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
        totalFloors = listing.totalFloors,
        pricePerSqm = listing.pricePerSqm,
        advertiserType = listing.advertiserType,
        businessType = listing.businessType,
        description = listing.description,
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
            totalFloors = null,
            pricePerSqm = null,
            advertiserType = null,
            businessType = null,
            description = null,
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
        totalFloors: String?,
        pricePerSqm: Long?,
        advertiserType: String?,
        businessType: String?,
        description: String?,
        note: String,
    ): String {
        val lines = mutableListOf<String>()
        val headline = title?.trim().orEmpty().ifBlank { "فایل پیشنهادی" }
        lines += "🏠 $headline"

        val location = listOfNotNull(district?.trim(), city?.trim()).filter { it.isNotBlank() }.joinToString("، ")
        if (location.isNotBlank()) lines += "📍 $location"

        price?.takeIf { it > 0 }?.let { lines += "💰 قیمت کل: ${FormatUtils.formatPriceToman(it)}" }
        deposit?.takeIf { it > 0 }?.let { lines += "🔑 ودیعه: ${FormatUtils.formatPriceShort(it)}" }
        rent?.takeIf { it > 0 }?.let { lines += "📅 اجاره ماهانه: ${FormatUtils.formatPriceShort(it)}" }

        val specs = mutableListOf<String>()
        area?.let { specs += FormatUtils.formatArea(it) }
        rooms?.let { specs += FormatUtils.formatRooms(it) }
        yearBuilt?.takeIf { it.isNotBlank() }?.let { specs += "ساخت $it" }
        floor?.takeIf { it.isNotBlank() }?.let { specs += "طبقه $it" }
        totalFloors?.takeIf { it.isNotBlank() }?.let { specs += "از $it طبقه" }
        if (specs.isNotEmpty()) lines += "📐 ${specs.joinToString(" · ")}"

        pricePerSqm?.takeIf { it > 0 }?.let {
            lines += "📊 هر متر: ${FormatUtils.formatPriceToman(it)}"
        }
        advertiserType?.takeIf { it.isNotBlank() }?.let { lines += "👤 $it" }
        businessType?.takeIf { it.isNotBlank() }?.let { lines += "🏷 $it" }

        description?.trim()?.takeIf { it.isNotBlank() }?.let { desc ->
            val short = if (desc.length > 120) desc.take(117) + "…" else desc
            lines += "\n💬 $short"
        }
        note.trim().takeIf { it.isNotBlank() }?.let { lines += "\n📝 $it" }

        lines += "\n— پیشنهاد ویژه مشاور شما"
        return lines.joinToString("\n")
    }
}
