package ir.divarfiling.mobile.core.network

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

data class ApiFailure(
    val message: String,
    val code: String? = null,
    val httpCode: Int? = null,
)

fun Throwable.toUserMessage(default: String): String = toApiFailure(default).message

fun Throwable.toApiFailure(default: String): ApiFailure {
    val http = this as? HttpException
    if (http != null) {
        val parsed = parseErrorBody(http)
        val mapped = mapApiError(parsed.code, parsed.message, http.code(), default)
        return ApiFailure(mapped, parsed.code, http.code())
    }
    if (this is IOException) {
        return ApiFailure("اتصال برقرار نشد. دوباره تلاش کنید.", "NETWORK_ERROR")
    }
    val text = message.orEmpty()
    if (text.isBlank() || looksLikeRawException(text)) {
        return ApiFailure(default, "ERROR")
    }
    return ApiFailure(text)
}

fun mapApiError(code: String?, serverMessage: String?, httpCode: Int?, default: String): String {
    val fromCode = when (code) {
        "AUTH_INVALID" -> "نام کاربری یا رمز عبور اشتباه است."
        "AUTH_EXPIRED" -> "نشست شما منقضی شده است. دوباره وارد شوید."
        "USER_NOT_FOUND" -> "حسابی با این شماره یافت نشد. ابتدا ثبت‌نام کنید."
        "PHONE_TAKEN" -> "این شماره قبلاً ثبت شده — وارد شوید."
        "OTP_INVALID" -> "کد تأیید نادرست است."
        "OTP_EXPIRED" -> "کد منقضی شده است. کد جدید درخواست کنید."
        "OTP_RATE_LIMITED" -> "تعداد درخواست زیاد است. کمی بعد دوباره تلاش کنید."
        "OTP_INVALID_PHONE" -> "شماره موبایل معتبر نیست."
        "OTP_CHALLENGE_INVALID" -> "مهلت تأیید به پایان رسیده. دوباره کد بگیرید."
        "VALIDATION_ERROR" -> serverMessage?.takeIf { it.isNotBlank() } ?: "اطلاعات واردشده معتبر نیست."
        "PLAN_NOT_FOUND" -> "این پلن دیگر در دسترس نیست."
        "PLAN_BLOCKED" -> serverMessage?.takeIf { it.isNotBlank() } ?: "امکان خرید این پلن وجود ندارد."
        "PHONE_VERIFY_REQUIRED" -> "برای خرید، ابتدا شماره موبایل حساب را تأیید کنید."
        "ORDER_NOT_FOUND" -> "سفارش یافت نشد."
        "PAYMENT_UNAVAILABLE", "GATEWAY_ERROR" -> "درگاه پرداخت در دسترس نیست. کمی بعد دوباره تلاش کنید."
        "LICENSE_REQUIRED" -> "برای ادامه به لایسنس فعال نیاز است."
        "NETWORK_ERROR" -> "اتصال برقرار نشد. دوباره تلاش کنید."
        else -> null
    }
    if (!fromCode.isNullOrBlank()) return fromCode
    if (!serverMessage.isNullOrBlank() && !looksLikeRawException(serverMessage)) {
        return serverMessage
    }
    return when (val code = httpCode) {
        401 -> "نشست شما منقضی شده است. دوباره وارد شوید."
        403 -> "دسترسی به این عملیات مجاز نیست."
        404 -> "منبع درخواستی یافت نشد."
        429 -> "تعداد درخواست زیاد است. کمی بعد دوباره تلاش کنید."
        else -> if (code != null && code in 500..599) {
            "خطای سرور. لطفاً بعداً دوباره تلاش کنید."
        } else {
            default
        }
    }
}

private data class ParsedError(val message: String?, val code: String?)

private fun parseErrorBody(http: HttpException): ParsedError {
    val raw = runCatching { http.response()?.errorBody()?.string().orEmpty() }.getOrDefault("")
    if (raw.isBlank()) return ParsedError(null, null)
    val json = runCatching { JSONObject(raw) }.getOrNull() ?: return ParsedError(null, null)
    val message = json.optString("error").takeIf { it.isNotBlank() }
        ?: json.optString("detail").takeIf { it.isNotBlank() }
    val code = json.optString("code").takeIf { it.isNotBlank() }
    return ParsedError(message, code)
}

private fun looksLikeRawException(text: String): Boolean {
    val lower = text.lowercase()
    return "exception" in lower ||
        "traceback" in lower ||
        "stacktrace" in lower ||
        lower.startsWith("java.") ||
        lower.startsWith("kotlin.") ||
        "errorcode=" in lower
}
