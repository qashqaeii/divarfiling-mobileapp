package ir.divarfiling.mobile.core.filing

import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.core.network.ListingDto

object ListingAdvertiserUtils {
    private val consultantKeywords = setOf(
        "premium-panel", "premium_panel", "premium", "business", "consultant",
        "agency", "بنگاه", "مشاور", "آژانس", "مشاور املاک",
    )

    fun isConsultant(advertiserType: String?, businessType: String?): Boolean {
        val adv = advertiserType?.trim()?.lowercase().orEmpty()
        val biz = businessType?.trim()?.lowercase().orEmpty()
        if (adv.isBlank() && biz.isBlank()) return false
        return consultantKeywords.any { adv.contains(it) || biz.contains(it) }
    }

    fun isConsultant(listing: ListingDto): Boolean =
        isConsultant(listing.advertiserType, listing.businessType)

    fun isConsultant(listing: ListingDetailDto): Boolean =
        isConsultant(listing.advertiserType, listing.businessType)

    fun sortPersonalFirst(listings: List<ListingDto>): List<ListingDto> =
        listings.sortedWith(compareBy { if (isConsultant(it)) 1 else 0 })

    fun badgeLabel(listing: ListingDto): String = when {
        isConsultant(listing) -> "مشاور"
        else -> "شخصی"
    }

    fun badgeLabel(listing: ListingDetailDto): String = when {
        isConsultant(listing) -> "مشاور"
        else -> "شخصی"
    }
}
