package ir.divarfiling.mobile.feature.crm

import ir.divarfiling.mobile.feature.filing.ListingFeatureChoices

enum class FeatureWidget { Select, Chips, Toggle, Number, Date }

data class FeatureFieldDef(
    val key: String,
    val label: String = key,
    val widget: FeatureWidget,
    val choices: List<String> = emptyList(),
)

data class FeatureGroupDef(
    val id: String,
    val title: String,
    val keys: List<String>,
)

data class FeatureProfileDef(
    val id: String,
    val title: String,
    val hint: String,
    val groups: List<FeatureGroupDef>,
)

object PropertyFeatureSchema {
    private val commercialTypes = setOf("اداری", "مغازه")
    private val rentModes = setOf("اجاره", "رهن و اجاره")

    private val toggleKeys = setOf(
        "بالکن", "حیوان خانگی مجاز", "بازسازی شده", "استخر", "سونا", "جکوزی", "مبله",
        "وام دارد", "قابل معاوضه", "اتاق مدیریت", "اتاق کنفرانس", "فضای پذیرش",
        "آبدارخانه", "تابلوخور", "ورودی مجزا", "نگهبانی",
    )

    private val dateKeys = setOf("تاریخ تخلیه")

    private val selectKeys = setOf(
        "تعداد واحد در طبقه", "تعداد کل طبقات ساختمان", "تعداد سرویس بهداشتی",
        "تعداد پارکینگ", "تعداد حمام", "حداقل مدت قرارداد", "تعداد خط تلفن",
    )

    fun fieldDef(key: String): FeatureFieldDef {
        val widget = when {
            key in toggleKeys -> FeatureWidget.Toggle
            key in dateKeys -> FeatureWidget.Date
            key in selectKeys -> FeatureWidget.Select
            ListingFeatureChoices.choicesFor(key).isNotEmpty() -> FeatureWidget.Chips
            else -> FeatureWidget.Number
        }
        val choices = when (widget) {
            FeatureWidget.Select, FeatureWidget.Chips -> ListingFeatureChoices.choicesFor(key)
            else -> emptyList()
        }
        return FeatureFieldDef(key = key, widget = widget, choices = choices)
    }

    fun resolveProfile(dealMode: String, propertyType: String): String = when {
        propertyType in commercialTypes -> "commercial"
        dealMode in rentModes -> "rent"
        else -> "sale"
    }

    fun profileFor(dealMode: String, propertyType: String): FeatureProfileDef =
        profiles.getValue(resolveProfile(dealMode, propertyType))

    fun groupsFor(dealMode: String, propertyType: String): List<FeatureGroupDef> =
        profileFor(dealMode, propertyType).groups

    private val rentGroups = listOf(
        FeatureGroupDef("rent_building", "ساختمان و طبقه", listOf("تعداد واحد در طبقه", "تعداد کل طبقات ساختمان", "جهت ساختمان", "بالکن")),
        FeatureGroupDef("rent_comfort", "رفاهی و تاسیسات", listOf("جنس کف", "سرویس بهداشتی", "سرمایش", "گرمایش", "تأمین‌کننده آب گرم", "نوع آشپزخانه", "جنس کابینت")),
        FeatureGroupDef("rent_terms", "شرایط اجاره", listOf("مناسب برای", "حیوان خانگی مجاز", "مبله", "بازسازی شده", "حداقل مدت قرارداد")),
        FeatureGroupDef("rent_luxury", "امکانات لوکس", listOf("استخر", "سونا", "جکوزی")),
    )

    private val saleGroups = listOf(
        FeatureGroupDef("sale_building", "ساختمان و نما", listOf("تعداد واحد در طبقه", "تعداد کل طبقات ساختمان", "جهت ساختمان", "بالکن", "نمای ساختمان")),
        FeatureGroupDef("sale_comfort", "رفاهی و تاسیسات", listOf("جنس کف", "سرویس بهداشتی", "سرمایش", "گرمایش", "تأمین‌کننده آب گرم", "نوع آشپزخانه", "جنس کابینت")),
        FeatureGroupDef("sale_counts", "تعداد و ظرفیت", listOf("تعداد پارکینگ", "تعداد حمام", "تعداد سرویس بهداشتی")),
        FeatureGroupDef("sale_legal", "سند و وضعیت", listOf("نوع سند", "وضعیت سکونت", "بازسازی شده", "وام دارد", "قابل معاوضه")),
        FeatureGroupDef("sale_luxury", "امکانات لوکس", listOf("استخر", "جکوزی", "سونا")),
    )

    private val commercialGroups = listOf(
        FeatureGroupDef("com_status", "وضعیت و موقعیت", listOf("نوع کاربری ملک", "وضعیت سند", "وضعیت فعلی", "موقعیت ساختمان", "تعداد کل طبقات ساختمان")),
        FeatureGroupDef("com_utilities", "تاسیسات", listOf("گرمایش", "سرمایش", "تعداد سرویس بهداشتی", "تعداد خط تلفن")),
        FeatureGroupDef("com_spaces", "فضاها و امکانات", listOf("اتاق مدیریت", "اتاق کنفرانس", "فضای پذیرش", "آبدارخانه", "تابلوخور", "ورودی مجزا", "نگهبانی")),
    )

    private val profiles = mapOf(
        "rent" to FeatureProfileDef("rent", "ویژگی‌های اجاره", "مخصوص رهن و اجاره", rentGroups),
        "sale" to FeatureProfileDef("sale", "سایر ویژگی‌ها و امکانات", "مخصوص فروش", saleGroups),
        "commercial" to FeatureProfileDef("commercial", "ویژگی‌های اداری / تجاری", "مخصوص ملک اداری و تجاری", commercialGroups),
    )
}
