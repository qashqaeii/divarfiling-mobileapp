package ir.divarfiling.mobile.feature.crm

import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.DealDto
import ir.divarfiling.mobile.core.network.LinkedListingDto

enum class ContactNbaKind {
    Call,
    RegisterNeed,
    ContinueDeal,
    ViewSuggestions,
    SendFile,
    AddFollowUp,
}

data class ContactNba(
    val kind: ContactNbaKind,
    val title: String,
    val subtitle: String,
    val cta: String,
    val tone: DfStatusTone,
    val dealId: Long? = null,
)

fun resolveContactNba(
    contact: ContactDto,
    deals: List<DealDto>,
    linkedListings: List<LinkedListingDto>,
): ContactNba {
    val overdue = DateUtils.isOverdue(contact.nextFollowUpAt)
    if (overdue && !contact.phone.isNullOrBlank()) {
        return ContactNba(
            kind = ContactNbaKind.Call,
            title = "پیگیری معوق",
            subtitle = "الان باید تماس بگیرید",
            cta = "تماس",
            tone = DfStatusTone.Warning,
        )
    }
    if (!hasRegisteredNeed(contact) && CrmConstants.isMatchEligible(contact.customerType)) {
        return ContactNba(
            kind = ContactNbaKind.RegisterNeed,
            title = "نیاز ثبت نشده",
            subtitle = "بودجه، محله یا نوع ملک را کامل کنید",
            cta = "ثبت نیاز",
            tone = DfStatusTone.Info,
        )
    }
    val activeDeal = deals.firstOrNull { deal ->
        val stage = deal.stage.orEmpty()
        stage.isNotBlank() && stage != "بسته‌شده" && stage != "از دست رفته"
    }
    if (activeDeal != null) {
        return ContactNba(
            kind = ContactNbaKind.ContinueDeal,
            title = "معامله فعال",
            subtitle = activeDeal.title,
            cta = "ادامه معامله",
            tone = DfStatusTone.Success,
            dealId = activeDeal.id,
        )
    }
    if (CrmConstants.isMatchEligible(contact.customerType)) {
        return ContactNba(
            kind = ContactNbaKind.ViewSuggestions,
            title = "فایل‌های پیشنهادی",
            subtitle = "ملک‌های هم‌خوان با نیاز این مخاطب",
            cta = "مشاهده پیشنهادها",
            tone = DfStatusTone.Info,
        )
    }
    if (linkedListings.isNotEmpty()) {
        return ContactNba(
            kind = ContactNbaKind.SendFile,
            title = "فایل مرتبط",
            subtitle = "ارسال فایل برای این مخاطب",
            cta = "ارسال",
            tone = DfStatusTone.Success,
        )
    }
    if (contact.nextFollowUpAt.isNullOrBlank()) {
        return ContactNba(
            kind = ContactNbaKind.AddFollowUp,
            title = "بدون پیگیری",
            subtitle = "یک یادآور ثبت کنید تا کار از قلم نیفتد",
            cta = "ثبت پیگیری",
            tone = DfStatusTone.Warning,
        )
    }
    return ContactNba(
        kind = ContactNbaKind.Call,
        title = "تماس بعدی",
        subtitle = contact.nextFollowUpAt?.let { DateUtils.formatRelativeTimeUntil(it) }
            ?: "با مخاطب در ارتباط بمانید",
        cta = "تماس",
        tone = DfStatusTone.Info,
    )
}

private fun hasRegisteredNeed(contact: ContactDto): Boolean {
    fun positive(value: Long?) = value != null && value > 0
    fun positiveInt(value: Int?) = value != null && value > 0
    return positive(contact.budgetMin) ||
        positive(contact.budgetMax) ||
        positive(contact.depositMin) ||
        positive(contact.rentMin) ||
        positiveInt(contact.minArea) ||
        positiveInt(contact.maxArea) ||
        !contact.areas.isNullOrBlank() ||
        !contact.propertyType.isNullOrBlank() ||
        !contact.rooms.isNullOrBlank()
}
