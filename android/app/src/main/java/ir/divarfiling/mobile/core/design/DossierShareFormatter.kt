package ir.divarfiling.mobile.core.design

import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.core.network.ListingDto
import ir.divarfiling.mobile.core.network.PropertyDto

data class DossierShareOptions(
    val customNote: String = "",
    val includeDivarLink: Boolean = false,
    val includeAddress: Boolean = false,
    val includeInternalNotes: Boolean = false,
    val includeAmenities: Boolean = true,
    val footer: String = DEFAULT_FOOTER,
) {
    companion object {
        const val DEFAULT_FOOTER = "پیشنهاد ویژه مشاور شما"
        const val PERSONAL_FOOTER = "فایل شخصی · فایلینگ دیوار"
    }
}

enum class DossierShareKind {
    FILING,
    PERSONAL,
}

object DossierShareFormatter {

    fun fromListing(listing: ListingDto, options: DossierShareOptions = DossierShareOptions()): String = build(
        title = listing.title,
        price = listing.price,
        deposit = listing.deposit,
        rent = listing.rent,
        area = listing.area,
        rooms = listing.rooms,
        district = listing.district,
        city = listing.city,
        neighborhood = null,
        yearBuilt = listing.yearBuilt,
        floor = listing.floor,
        totalFloors = null,
        pricePerSqm = listing.pricePerSqm,
        advertiserType = listing.advertiserType,
        businessType = listing.businessType,
        transactionType = listing.transactionType,
        description = null,
        hasParking = listing.hasParking,
        hasStorage = listing.hasStorage,
        hasElevator = listing.hasElevator,
        featureHighlights = listing.featureHighlights,
        propertyType = null,
        dealMode = listing.transactionType,
        transactionStatus = listing.unitStatus,
        address = null,
        internalNotes = null,
        link = listing.shareLink,
        options = options,
    )

    fun fromDetail(listing: ListingDetailDto, options: DossierShareOptions = DossierShareOptions()): String = build(
        title = listing.title,
        price = listing.price,
        deposit = listing.deposit,
        rent = listing.rent,
        area = listing.area,
        rooms = listing.rooms,
        district = listing.district,
        city = listing.city,
        neighborhood = null,
        yearBuilt = listing.yearBuilt,
        floor = listing.floor,
        totalFloors = listing.totalFloors,
        pricePerSqm = listing.pricePerSqm,
        advertiserType = listing.advertiserType,
        businessType = listing.businessType,
        transactionType = listing.unitStatus,
        description = listing.description,
        hasParking = listing.hasParking,
        hasStorage = listing.hasStorage,
        hasElevator = listing.hasElevator,
        featureHighlights = emptyList(),
        propertyType = listing.businessType,
        dealMode = null,
        transactionStatus = listing.unitStatus,
        address = null,
        internalNotes = null,
        link = listing.shareLink,
        options = options,
    )

    fun fromProperty(property: PropertyDto, options: DossierShareOptions = DossierShareOptions()): String = build(
        title = property.title,
        price = property.salePrice,
        deposit = property.deposit,
        rent = property.rent,
        area = property.area?.toInt(),
        rooms = property.rooms?.filter { it.isDigit() }?.toIntOrNull(),
        district = property.district,
        city = property.city,
        neighborhood = property.neighborhood,
        yearBuilt = property.buildYear?.toString(),
        floor = property.floor?.toString(),
        totalFloors = property.totalFloors?.toString(),
        pricePerSqm = null,
        advertiserType = null,
        businessType = null,
        transactionType = property.dealMode,
        description = null,
        hasParking = property.hasParking,
        hasStorage = property.hasStorage,
        hasElevator = property.hasElevator,
        featureHighlights = emptyList(),
        propertyType = property.propertyType,
        dealMode = property.dealMode,
        transactionStatus = property.transactionStatus,
        address = property.address,
        internalNotes = property.notes,
        link = property.link,
        options = options,
    )

    private fun build(
        title: String?,
        price: Long?,
        deposit: Long?,
        rent: Long?,
        area: Int?,
        rooms: Int?,
        district: String?,
        city: String?,
        neighborhood: String?,
        yearBuilt: String?,
        floor: String?,
        totalFloors: String?,
        pricePerSqm: Long?,
        advertiserType: String?,
        businessType: String?,
        transactionType: String?,
        description: String?,
        hasParking: Boolean?,
        hasStorage: Boolean?,
        hasElevator: Boolean?,
        featureHighlights: List<String>,
        propertyType: String?,
        dealMode: String?,
        transactionStatus: String?,
        address: String?,
        internalNotes: String?,
        link: String?,
        options: DossierShareOptions,
    ): String {
        val lines = mutableListOf<String>()
        val headline = title?.trim().orEmpty().ifBlank { "پرونده املاک" }
        lines += "🏠 $headline"

        val location = listOfNotNull(
            neighborhood?.trim()?.takeIf { it.isNotBlank() },
            district?.trim()?.takeIf { it.isNotBlank() },
            city?.trim()?.takeIf { it.isNotBlank() },
        ).joinToString("، ")
        if (location.isNotBlank()) lines += "📍 $location"

        dealMode?.takeIf { it.isNotBlank() }?.let { lines += "🤝 معامله: $it" }
        propertyType?.takeIf { it.isNotBlank() }?.let { lines += "🏷 نوع ملک: $it" }
        transactionStatus?.takeIf { it.isNotBlank() }?.let { lines += "📌 وضعیت: $it" }

        price?.takeIf { it > 0 }?.let { lines += "💰 قیمت کل: ${FormatUtils.formatPriceToman(it)}" }
        deposit?.takeIf { it > 0 }?.let { lines += "🔑 ودیعه: ${FormatUtils.formatPriceShort(it)}" }
        rent?.takeIf { it > 0 }?.let { lines += "📅 اجاره ماهانه: ${FormatUtils.formatPriceShort(it)}" }

        val specs = mutableListOf<String>()
        area?.let { specs += FormatUtils.formatArea(it) }
        rooms?.let { specs += FormatUtils.formatRooms(it) }
        yearBuilt?.takeIf { it.isNotBlank() }?.let { specs += "ساخت $it" }
        formatFloor(floor, totalFloors)?.let { specs += it }
        if (specs.isNotEmpty()) lines += "📐 ${specs.joinToString(" · ")}"

        pricePerSqm?.takeIf { it > 0 }?.let {
            lines += "📊 هر متر: ${FormatUtils.formatPriceToman(it)}"
        }

        if (options.includeAmenities) {
            amenityLine(hasParking, hasStorage, hasElevator, featureHighlights)?.let {
                lines += "✨ $it"
            }
        }

        advertiserType?.takeIf { it.isNotBlank() }?.let { lines += "👤 آگهی‌دهنده: $it" }
        businessType?.takeIf { it.isNotBlank() }?.let { lines += "🏢 کاربری: $it" }
        transactionType?.takeIf { it.isNotBlank() && it != dealMode }?.let { lines += "📋 $it" }

        description?.trim()?.takeIf { it.isNotBlank() }?.let { desc ->
            val short = if (desc.length > 160) desc.take(157) + "…" else desc
            lines += "\n💬 $short"
        }

        if (options.includeAddress) {
            address?.trim()?.takeIf { it.isNotBlank() }?.let { lines += "\n🗺 آدرس: $it" }
        }

        if (options.includeInternalNotes) {
            internalNotes?.trim()?.takeIf { it.isNotBlank() }?.let { lines += "\n📝 یادداشت: $it" }
        }

        options.customNote.trim().takeIf { it.isNotBlank() }?.let { lines += "\n✍️ $it" }

        if (options.includeDivarLink) {
            link?.trim()?.takeIf { it.isNotBlank() }?.let { lines += "\n🔗 $it" }
        }

        lines += "\n— ${options.footer}"
        return lines.joinToString("\n")
    }

    private fun formatFloor(floor: String?, totalFloors: String?): String? {
        val f = floor?.trim().orEmpty()
        val t = totalFloors?.trim().orEmpty()
        return when {
            f.isNotBlank() && t.isNotBlank() -> "طبقه $f از $t"
            f.isNotBlank() -> "طبقه $f"
            else -> null
        }
    }

    private fun amenityLine(
        hasParking: Boolean?,
        hasStorage: Boolean?,
        hasElevator: Boolean?,
        highlights: List<String>,
    ): String? {
        val items = mutableListOf<String>()
        if (hasParking == true) items += "پارکینگ"
        if (hasStorage == true) items += "انباری"
        if (hasElevator == true) items += "آسانسور"
        highlights.filter { it.isNotBlank() }.take(4).forEach { highlight ->
            if (highlight !in items) items += highlight
        }
        return items.takeIf { it.isNotEmpty() }?.joinToString(" · ")
    }
}
