package ir.divarfiling.mobile.core.license

data class LicenseState(
    val valid: Boolean = false,
    val plan: String? = null,
    val expiresAt: String? = null,
    val daysRemaining: Int? = null,
    val expiringSoon: Boolean = false,
    val lightExtractEnabled: Boolean = false,
    val crmEnabled: Boolean = true,
    val filingEnabled: Boolean = true,
) {
    /** استخراج سبک فقط با لایسنس فعال و مجوز light_extract */
    val canUseLightExtract: Boolean
        get() = valid && lightExtractEnabled

    val licenseLabel: String
        get() = when {
            !valid -> "لایسنس فعال نیست"
            plan.isNullOrBlank() -> "لایسنس فعال"
            else -> "پلن $plan"
        }

    val expiryHeadline: String
        get() = when {
            !valid -> "نیاز به تمدید"
            daysRemaining == null -> "بدون تاریخ انقضا"
            daysRemaining <= 0 -> "منقضی شده"
            daysRemaining == 1 -> "۱ روز تا انقضا"
            daysRemaining <= 7 -> "$daysRemaining روز تا انقضا"
            else -> "$daysRemaining روز باقی‌مانده"
        }

    /** ۰..۱ — برای نوار پیشرفت لایسنس (فرض دوره ۳۶۵ روزه) */
    val expiryProgress: Float
        get() {
            val days = daysRemaining ?: return if (valid) 1f else 0f
            return (days / 365f).coerceIn(0f, 1f)
        }

    val expiryTintHealthy: Boolean
        get() = valid && (daysRemaining == null || daysRemaining > 14)
}

object ExtractLightLimits {
    const val MAX_ITEMS = 100
    const val MAX_CONCURRENT = 2
    const val MIN_DELAY_MS = 1000L
}
