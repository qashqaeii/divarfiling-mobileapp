package ir.divarfiling.mobile.core.filing

import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.core.network.ListingDto

object ListingAdvertiserUtils {
    const val SIGNAL_CONSULTANT = "consultant"
    const val SIGNAL_GENUINE_PERSONAL = "genuine_personal"
    const val SIGNAL_DISGUISED = "disguised_consultant"

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

    fun isGenuinePersonal(signal: String?): Boolean =
        signal?.trim() == SIGNAL_GENUINE_PERSONAL

    fun isGenuinePersonal(listing: ListingDto): Boolean =
        isGenuinePersonal(listing.advertiserSignal)

    fun isGenuinePersonal(listing: ListingDetailDto): Boolean =
        isGenuinePersonal(listing.advertiserSignal)

    fun isDisguisedConsultant(signal: String?): Boolean =
        signal?.trim() == SIGNAL_DISGUISED

    fun isDisguisedConsultant(listing: ListingDto): Boolean =
        isDisguisedConsultant(listing.advertiserSignal)

    fun isDisguisedConsultant(listing: ListingDetailDto): Boolean =
        isDisguisedConsultant(listing.advertiserSignal)

    fun sortPersonalFirst(listings: List<ListingDto>): List<ListingDto> =
        listings.sortedWith(
            compareBy<ListingDto> { listingSortRank(it) }
                .thenBy { if (isConsultant(it)) 1 else 0 },
        )

    private fun listingSortRank(listing: ListingDto): Int = when {
        isGenuinePersonal(listing) -> 0
        isDisguisedConsultant(listing) -> 2
        isConsultant(listing) -> 3
        else -> 1
    }

    fun badgeLabel(listing: ListingDto): String = when {
        isConsultant(listing) -> "مشاور"
        else -> "شخصی"
    }

    fun badgeLabel(listing: ListingDetailDto): String = when {
        isConsultant(listing) -> "مشاور"
        else -> "شخصی"
    }

    fun signalBadgeLabel(listing: ListingDto): String? =
        signalBadgeLabel(listing.advertiserSignal, listing.advertiserSignalLabel)

    fun signalBadgeLabel(listing: ListingDetailDto): String? =
        signalBadgeLabel(listing.advertiserSignal, listing.advertiserSignalLabel)

    fun signalBadgeLabel(signal: String?, serverLabel: String? = null): String? {
        serverLabel?.takeIf { it.isNotBlank() }?.let { return it }
        return when (signal?.trim()) {
            SIGNAL_GENUINE_PERSONAL -> "مالک واقعی"
            SIGNAL_DISGUISED -> "مشاور پنهان"
            else -> null
        }
    }
}
