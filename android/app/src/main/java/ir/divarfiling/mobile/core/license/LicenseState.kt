package ir.divarfiling.mobile.core.license

data class LicenseState(
    val valid: Boolean = false,
    val plan: String? = null,
    val expiresAt: String? = null,
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
}

object ExtractLightLimits {
    const val MAX_ITEMS = 100
    const val MAX_CONCURRENT = 2
    const val MIN_DELAY_MS = 1000L
}
