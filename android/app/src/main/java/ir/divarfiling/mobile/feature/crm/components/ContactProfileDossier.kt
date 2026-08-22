package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DossierGroupPanel
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.feature.crm.CrmTypeProfiles
import ir.divarfiling.mobile.feature.crm.MoneyMode

@Composable
fun ContactProfileDossier(
    contact: ContactDto,
    modifier: Modifier = Modifier,
) {
    val profile = CrmTypeProfiles.profileFor(contact.customerType)
    val sections = buildContactDossierSections(contact, profile.moneyMode)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        sections.forEach { section ->
            DossierGroupPanel(
                title = section.title,
                rows = section.rows,
                iconRes = section.iconRes,
                icon = section.icon,
                accent = section.accent,
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
        }
    }
}

private data class ContactDossierSection(
    val title: String,
    val rows: List<Pair<String, String>>,
    val iconRes: Int? = null,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val accent: androidx.compose.ui.graphics.Color = DfColors.Purple,
)

private fun buildContactDossierSections(
    contact: ContactDto,
    moneyMode: MoneyMode,
): List<ContactDossierSection> {
    val sections = mutableListOf<ContactDossierSection>()

    val identityRows = buildList {
        contact.customerType?.takeIf { it.isNotBlank() }?.let { add("نوع مخاطب" to it) }
        contact.status?.takeIf { it.isNotBlank() }?.let { add("وضعیت" to it) }
        contact.priority?.takeIf { it.isNotBlank() }?.let { add("اولویت" to it) }
        contact.source?.takeIf { it.isNotBlank() }?.let { add("منبع" to it) }
        contact.phone?.takeIf { it.isNotBlank() }?.let { add("تلفن" to it) }
        contact.phoneAlt?.takeIf { it.isNotBlank() }?.let { add("تلفن دوم" to it) }
        contact.email?.takeIf { it.isNotBlank() }?.let { add("ایمیل" to it) }
        contact.city?.takeIf { it.isNotBlank() }?.let { add("شهر" to it) }
        contact.district?.takeIf { it.isNotBlank() }?.let { add("منطقه" to it) }
        contact.createdAt?.let { ts ->
            DateUtils.formatJalaliDateTime(ts)?.let { add("تاریخ ثبت" to it) }
        }
        contact.updatedAt?.let { ts ->
            DateUtils.formatJalaliDateTime(ts)?.let { add("آخرین بروزرسانی" to it) }
        }
        contact.nextFollowUpAt?.let { ts ->
            DateUtils.formatJalaliDateTime(ts)?.let { add("پیگیری بعدی" to it) }
        }
        if (contact.matchEligible) add("تطبیق هوشمند" to "فعال")
        contact.matchingTolerancePercent?.takeIf { it > 0 }?.let {
            add("تحمل تطبیق" to "${DateUtils.toPersianDigits(it.toString())}٪")
        }
    }
    if (identityRows.isNotEmpty()) {
        sections.add(
            ContactDossierSection(
                title = "اطلاعات پایه",
                rows = identityRows,
                iconRes = DfDecorIcons.User,
                accent = DfColors.Blue,
            ),
        )
    }

    val moneyRows = buildList {
        if (contact.isBuilder || contact.customerType == "سازنده") {
            formatMoneyRange(contact.budgetMin, contact.budgetMax)?.let { add("بودجه فروش آپارتمان" to it) }
            formatMoneyRange(contact.builderBuyBudgetMin, contact.builderBuyBudgetMax)?.let {
                add("بودجه خرید پروژه" to it)
            }
        } else if (CrmTypeProfiles.showsBudget(moneyMode)) {
            formatMoneyRange(contact.budgetMin, contact.budgetMax)?.let {
                add(profileBudgetLabel(contact.customerType) to it)
            }
        }
        if (CrmTypeProfiles.showsRent(moneyMode)) {
            formatMoneyRange(contact.depositMin, contact.depositMax)?.let { add("رهن" to it) }
            formatMoneyRange(contact.rentMin, contact.rentMax)?.let { add("اجاره" to it) }
        }
    }
    if (moneyRows.isNotEmpty()) {
        sections.add(
            ContactDossierSection(
                title = "محدوده مالی",
                rows = moneyRows,
                iconRes = DfDecorIcons.Coins,
                accent = DfColors.Green,
            ),
        )
    }

    val preferenceRows = buildList {
        contact.propertyType?.takeIf { it.isNotBlank() }?.let { add("نوع ملک" to it) }
        formatAreaRange(contact.minArea, contact.maxArea)?.let { add("متراژ" to it) }
        contact.rooms?.takeIf { it.isNotBlank() }?.let { add("اتاق" to it) }
        formatIntRange(contact.roomsMin, contact.roomsMax, suffix = " اتاق")?.let { add("بازه اتاق" to it) }
        formatIntRange(contact.yearMin, contact.yearMax)?.let { add("سال ساخت" to it) }
        formatIntRange(contact.floorMin, contact.floorMax, suffix = " طبقه")?.let { add("طبقه" to it) }
        contact.areas?.takeIf { it.isNotBlank() }?.let { add("مناطق/محله" to it) }
        contact.district?.takeIf { it.isNotBlank() }?.let { add("منطقه" to it) }
        val amenityWants = buildList {
            if (contact.wantParking) add("پارکینگ")
            if (contact.wantStorage) add("انباری")
            if (contact.wantElevator) add("آسانسور")
        }
        if (amenityWants.isNotEmpty()) add("امکانات مورد نظر" to amenityWants.joinToString("، "))
    }
    if (preferenceRows.isNotEmpty()) {
        sections.add(
            ContactDossierSection(
                title = "ترجیحات ملک",
                rows = preferenceRows,
                iconRes = DfDecorIcons.Building,
                accent = DfColors.Purple,
            ),
        )
    }

    if (contact.isBuilder || contact.customerType == "سازنده") {
        val builderRows = buildList {
            contact.builderBuyPropertyTypes?.takeIf { it.isNotBlank() }?.let { add("نوع پروژه خرید" to it) }
            formatAreaRange(contact.builderBuyMinArea, contact.builderBuyMaxArea)?.let { add("متراژ خرید" to it) }
            contact.builderBuyAreas?.takeIf { it.isNotBlank() }?.let { add("مناطق خرید" to it) }
        }
        if (builderRows.isNotEmpty()) {
            sections.add(
                ContactDossierSection(
                    title = "پروفایل سازنده",
                    rows = builderRows,
                    icon = DfIcons.Building,
                    accent = DfColors.Amber,
                ),
            )
        }
    }

    contact.notes?.takeIf { it.isNotBlank() }?.let { notes ->
        sections.add(
            ContactDossierSection(
                title = "یادداشت‌ها",
                rows = listOf("توضیحات" to notes),
                iconRes = DfDecorIcons.StickyNote,
                accent = DfColors.TextMuted,
            ),
        )
    }

    return sections
}

private fun profileBudgetLabel(customerType: String?): String {
    return when (customerType) {
        "خریدار", "متقاضی خرید", "سرمایه‌گذار" -> "بودجه خرید"
        "فروشنده", "فروشنده ملک", "مالک", "موجر" -> "قیمت فروش"
        else -> "بودجه"
    }
}

private fun formatMoneyRange(min: Long?, max: Long?): String? {
    val lo = min?.takeIf { it > 0 }
    val hi = max?.takeIf { it > 0 }
    return when {
        lo != null && hi != null -> "${FormatUtils.formatPriceShort(lo)} — ${FormatUtils.formatPriceShort(hi)}"
        lo != null -> "از ${FormatUtils.formatPriceShort(lo)}"
        hi != null -> "تا ${FormatUtils.formatPriceShort(hi)}"
        else -> null
    }
}

private fun formatAreaRange(min: Int?, max: Int?): String? =
    formatIntRange(min, max, suffix = " متر")

private fun formatIntRange(min: Int?, max: Int?, suffix: String = ""): String? {
    val lo = min?.takeIf { it > 0 }
    val hi = max?.takeIf { it > 0 }
    return when {
        lo != null && hi != null -> "${lo}$suffix — ${hi}$suffix"
        lo != null -> "از ${lo}$suffix"
        hi != null -> "تا ${hi}$suffix"
        else -> null
    }
}
