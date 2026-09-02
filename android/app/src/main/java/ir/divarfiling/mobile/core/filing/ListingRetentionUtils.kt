package ir.divarfiling.mobile.core.filing

import ir.divarfiling.mobile.core.network.ListingRetentionDto

object ListingRetentionUtils {
    fun isWarning(retention: ListingRetentionDto?): Boolean =
        retention?.status == "warning"

    fun countdownLabel(daysRemaining: Int?): String = when (daysRemaining) {
        null -> ""
        0 -> "امروز"
        1 -> "فردا"
        else -> "$daysRemaining روز دیگر"
    }

    fun warningMessage(daysRemaining: Int?): String = when (daysRemaining) {
        0 -> "این فایل امروز در پاک‌سازی دوره‌ای حذف می‌شود."
        1 -> "به‌دلیل نداشتن فعالیت اخیر، این فایل فردا از فایلینگ حذف می‌شود."
        else -> {
            val days = daysRemaining ?: 3
            "به‌دلیل نداشتن فعالیت اخیر، این فایل $days روز دیگر از فایلینگ حذف می‌شود."
        }
    }

    fun badgeText(daysRemaining: Int?): String = buildString {
        append("در آستانه پاک‌سازی")
        daysRemaining?.let { append(" · $it روز") }
    }
}
