package ir.divarfiling.mobile.core.license

import ir.divarfiling.mobile.core.design.DateUtils
import kotlin.math.roundToInt

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
        get() {
            val days = resolvedDaysRemaining
            return when {
                !valid -> "نیاز به تمدید"
                days == null -> "بدون تاریخ انقضا"
                days <= 0 -> "منقضی شده"
                days == 1 -> "۱ روز تا انقضا"
                days <= 7 -> "$days روز تا انقضا"
                else -> "$days روز باقی‌مانده"
            }
        }

    /** ۰..۱ — سهم زمان باقی‌مانده تا انقضا */
    val expiryProgress: Float
        get() {
            val days = resolvedDaysRemaining ?: return if (valid) 1f else 0f
            if (days <= 0) return 0f
            val fullScaleDays = 90
            return (days / fullScaleDays.toFloat()).coerceIn(0f, 1f)
        }

    val expiryProgressPercent: Int
        get() = (expiryProgress * 100).roundToInt().coerceIn(0, 100)

    private val resolvedDaysRemaining: Int?
        get() {
            daysRemaining?.let { return it }
            return DateUtils.daysUntilExpiry(expiresAt)
        }

    val expiryTintHealthy: Boolean
        get() {
            val days = resolvedDaysRemaining
            return valid && (days == null || days > 14)
        }
}

object ExtractLightLimits {
    const val MAX_ITEMS = 100
    const val MAX_CONCURRENT = 2
    const val MIN_DELAY_MS = 1000L
}
