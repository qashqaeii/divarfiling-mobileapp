package ir.divarfiling.mobile.core.util

/**
 * نرمال‌سازی شماره موبایل ایران مطابق accounts.phone_utils.normalize_phone
 * بدون حدس شماره اشتباه.
 */
object PhoneNormalizer {

    fun normalize(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val translated = buildString(raw.length) {
            for (ch in raw.trim()) {
                when (ch) {
                    in '0'..'9' -> append(ch)
                    in '۰'..'۹' -> append('0' + (ch - '۰'))
                    in '٠'..'٩' -> append('0' + (ch - '٠'))
                    '+', in 'A'..'Z', in 'a'..'z' -> append(ch)
                    else -> Unit
                }
            }
        }.replace(" ", "").replace("-", "").replace("(", "").replace(")", "")
        var s = translated.filter { it.isDigit() || it == '+' }
        s = s.replace(" ", "")
        return when {
            s.startsWith("+98") -> "0" + s.drop(3)
            s.startsWith("0098") -> "0" + s.drop(4)
            s.startsWith("98") && s.length == 12 -> "0" + s.drop(2)
            s.startsWith("9") && s.length == 10 -> "0$s"
            else -> s
        }
    }

    fun isValidIranMobile(phone: String?): Boolean =
        normalize(phone).matches(Regex("^09\\d{9}$"))

    fun digitsForMessaging(phone: String?): String {
        val normalized = normalize(phone)
        return if (normalized.startsWith("0")) "98${normalized.drop(1)}" else normalized.filter { it.isDigit() }
    }
}
