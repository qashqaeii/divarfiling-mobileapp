package ir.divarfiling.mobile.feature.filing

/**
 * گزینه‌های انتخابی ویژگی آگهی — هم‌تراز با فرم ورود ملک CRM در جنگو.
 * اگر API فیلد `choices` بفرستد همان اولویت دارد؛ در غیر این صورت این کاتالوگ استفاده می‌شود.
 */
object ListingFeatureChoices {
    private val direction = listOf("شمالی", "جنوبی", "شرقی", "غربی")
    private val flooring = listOf("سرامیک", "پارکت", "پارکت چوبی", "لمینت", "کفپوش PVC", "موکت", "موزاییک")
    private val bathroom = listOf("ایرانی", "فرنگی", "هردو")
    private val cooling = listOf("کولر آبی", "کولر گازی", "داکت اسپلیت", "اسپلیت", "فن‌کویل", "پنکه")
    private val heating = listOf("بخاری", "شوفاژ", "فن‌کویل", "از کف", "داکت اسپلیت", "اسپلیت", "شومینه")
    private val hotWater = listOf("آبگرمکن", "موتورخانه", "پکیج")
    private val kitchen = listOf("اپن", "جزیره", "نیمه‌اپن", "بسته")
    private val suitable = listOf("خانواده", "مجرد", "هر دو")
    private val cabinet = listOf("MDF", "های‌گلاس", "ممبران", "فلزی", "چوب", "سایر")
    private val deed = listOf("تک‌برگ", "قولنامه‌ای", "منگوله‌دار", "سایر")
    private val facade = listOf("سنگ", "آجر", "سیمان", "کامپوزیت", "چوب", "شیشه", "سایر")
    private val occupancy = listOf("تخلیه", "در اختیار مالک", "در اختیار مستاجر")
    private val docStatus = listOf("آزاد", "در رهن", "وکالتی", "سایر")
    private val currentStatus = listOf("تخلیه", "فعال", "در حال بهره‌برداری", "نیاز به تعمیر", "سایر")
    private val buildingPos = listOf("نبش", "میانی", "گذری", "بر اصلی", "داخل کوچه", "سایر")
    private val usage = listOf(
        "اداری", "موقعیت اداری", "تجاری", "پزشکی", "آموزشی",
        "صنعتی", "خدماتی", "ورزشی", "فرهنگی", "مذهبی", "سایر",
    )
    private val unitsPerFloor = (1..20).map { it.toString() }
    private val totalFloors = (1..30).map { it.toString() }
    private val count0to10 = (0..10).map { it.toString() }
    private val contractMonths = (1..36).map { it.toString() }
    private val phoneLines = (0..20).map { it.toString() }

    private val catalog: Map<String, List<String>> = mapOf(
        "جهت ساختمان" to direction,
        "جنس کف" to flooring,
        "سرویس بهداشتی" to bathroom,
        "سرمایش" to cooling,
        "گرمایش" to heating,
        "تأمین‌کننده آب گرم" to hotWater,
        "نوع آشپزخانه" to kitchen,
        "مناسب برای" to suitable,
        "جنس کابینت" to cabinet,
        "نوع سند" to deed,
        "نمای ساختمان" to facade,
        "وضعیت سکونت" to occupancy,
        "وضعیت واحد" to occupancy,
        "وضعیت سند" to docStatus,
        "وضعیت فعلی" to currentStatus,
        "موقعیت ساختمان" to buildingPos,
        "نوع کاربری ملک" to usage,
        "تعداد واحد در طبقه" to unitsPerFloor,
        "تعداد کل طبقات ساختمان" to totalFloors,
        "تعداد سرویس بهداشتی" to count0to10,
        "تعداد پارکینگ" to count0to10,
        "تعداد حمام" to count0to10,
        "حداقل مدت قرارداد" to contractMonths,
        "تعداد خط تلفن" to phoneLines,
    )

    fun choicesFor(key: String, apiChoices: List<String> = emptyList()): List<String> {
        if (apiChoices.isNotEmpty()) return apiChoices
        return catalog[key].orEmpty()
    }
}
