package ir.divarfiling.mobile.core.network

import retrofit2.HttpException

fun Throwable.toUserMessage(default: String): String {
    val http = this as? HttpException
    if (http != null) {
        return when (http.code()) {
            401 -> "نشست شما منقضی شده است. دوباره وارد شوید."
            403 -> "دسترسی به این عملیات مجاز نیست."
            404 -> "منبع درخواستی یافت نشد."
            in 500..599 -> "خطای سرور. لطفاً بعداً دوباره تلاش کنید."
            else -> http.message()?.takeIf { it.isNotBlank() } ?: default
        }
    }
    return message?.takeIf { it.isNotBlank() } ?: default
}
