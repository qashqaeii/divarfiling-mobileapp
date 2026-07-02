package ir.divarfiling.mobile.feature.crm

object CrmConstants {
    val STATUSES = listOf(
        "جدید",
        "در حال پیگیری",
        "بازدید انجام شد",
        "قرارداد",
        "بایگانی",
    )

    val CUSTOMER_TYPES = listOf(
        "خریدار",
        "فروشنده",
        "مالک",
        "مستاجر",
        "موجر",
        "سرمایه‌گذار",
        "سرنخ",
        "متقاضی خرید",
        "متقاضی اجاره",
        "فروشنده ملک",
        "اشخاص حقوقی",
    )

    val PRIORITIES = listOf("بالا", "متوسط", "پایین")

    val QUICK_ACTIVITY_TYPES = listOf(
        "تماس" to "تماس تلفنی",
        "واتساپ" to "پیام واتساپ",
        "پیامک" to "پیامک",
        "بازدید" to "بازدید ملک",
        "پیگیری" to "پیگیری مشتری",
        "جلسه" to "جلسه حضوری",
        "یادداشت" to "یادداشت",
    )

    val DEAL_STAGES = listOf(
        "سرنخ", "مذاکره", "بازدید", "پیش‌قرارداد", "قرارداد", "بسته‌شده", "از دست رفته",
    )

    val PROPERTY_TX_STATUSES = listOf(
        "فعال", "در مذاکره", "قرارداد", "فروخته‌شده", "اجاره‌رفته", "بایگانی",
    )

    val PROPERTY_DEAL_MODES = listOf("فروش", "رهن و اجاره", "اجاره", "پیش‌فروش")

    val PROPERTY_TYPES = listOf(
        "آپارتمان", "ویلا", "کلنگی", "اداری", "مغازه", "زمین", "سایر",
    )

    val MATCH_ELIGIBLE_TYPES = setOf(
        "خریدار",
        "مستاجر",
        "متقاضی خرید",
        "متقاضی اجاره",
        "سرنخ",
        "سرمایه‌گذار",
    )

    fun isMatchEligible(customerType: String?): Boolean =
        customerType != null && customerType in MATCH_ELIGIBLE_TYPES
}
