package ir.divarfiling.mobile.feature.crm

enum class MoneyMode {
    Budget,
    Rent,
    Both,
    Flexible,
}

data class TypeProfile(
    val moneyMode: MoneyMode,
    val budgetLabels: Pair<String, String> = "بودجه از" to "بودجه تا",
    val depositLabels: Pair<String, String> = "ودیعه از" to "ودیعه تا",
    val rentLabels: Pair<String, String> = "اجاره از" to "اجاره تا",
    val sectionHint: String = "محدوده مالی بر اساس نوع مخاطب",
)

object CrmTypeProfiles {
    private val profiles: Map<String, TypeProfile> = mapOf(
        "خریدار" to TypeProfile(
            moneyMode = MoneyMode.Budget,
            budgetLabels = "بودجه از" to "بودجه تا",
            sectionHint = "محدوده قیمت خرید (تومان)",
        ),
        "فروشنده" to TypeProfile(
            moneyMode = MoneyMode.Budget,
            budgetLabels = "قیمت فروش از" to "قیمت فروش تا",
            sectionHint = "محدوده قیمت پیشنهادی فروش",
        ),
        "فروشنده ملک" to TypeProfile(
            moneyMode = MoneyMode.Budget,
            budgetLabels = "قیمت فروش از" to "قیمت فروش تا",
            sectionHint = "قیمت فروش ملک",
        ),
        "مالک" to TypeProfile(
            moneyMode = MoneyMode.Both,
            budgetLabels = "قیمت فروش از" to "قیمت فروش تا",
            depositLabels = "ودیعه از" to "ودیعه تا",
            rentLabels = "اجاره ماهانه از" to "اجاره ماهانه تا",
            sectionHint = "فروش یا رهن و اجاره",
        ),
        "مستاجر" to TypeProfile(
            moneyMode = MoneyMode.Rent,
            depositLabels = "ودیعه از" to "ودیعه تا",
            rentLabels = "اجاره ماهانه از" to "اجاره ماهانه تا",
            sectionHint = "محدوده رهن و اجاره مورد نظر",
        ),
        "موجر" to TypeProfile(
            moneyMode = MoneyMode.Rent,
            depositLabels = "ودیعه درخواستی از" to "ودیعه درخواستی تا",
            rentLabels = "اجاره درخواستی از" to "اجاره درخواستی تا",
            sectionHint = "ودیعه و اجاره اعلامی موجر",
        ),
        "متقاضی خرید" to TypeProfile(
            moneyMode = MoneyMode.Budget,
            budgetLabels = "بودجه از" to "بودجه تا",
            sectionHint = "محدوده بودجه خرید",
        ),
        "متقاضی اجاره" to TypeProfile(
            moneyMode = MoneyMode.Rent,
            depositLabels = "ودیعه از" to "ودیعه تا",
            rentLabels = "اجاره ماهانه از" to "اجاره ماهانه تا",
            sectionHint = "محدوده رهن و اجاره",
        ),
        "سرمایه‌گذار" to TypeProfile(
            moneyMode = MoneyMode.Budget,
            budgetLabels = "سرمایه از" to "سرمایه تا",
            sectionHint = "محدوده سرمایه‌گذاری",
        ),
        "سرنخ" to TypeProfile(
            moneyMode = MoneyMode.Flexible,
            sectionHint = "اطلاعات مالی اختیاری",
        ),
        "اشخاص حقوقی" to TypeProfile(
            moneyMode = MoneyMode.Flexible,
            sectionHint = "اطلاعات مالی شرکت",
        ),
    )

    fun profileFor(customerType: String?): TypeProfile =
        profiles[customerType] ?: profiles.getValue("سرنخ")

    fun showsBudget(mode: MoneyMode): Boolean =
        mode == MoneyMode.Budget || mode == MoneyMode.Both || mode == MoneyMode.Flexible

    fun showsRent(mode: MoneyMode): Boolean =
        mode == MoneyMode.Rent || mode == MoneyMode.Both || mode == MoneyMode.Flexible
}
