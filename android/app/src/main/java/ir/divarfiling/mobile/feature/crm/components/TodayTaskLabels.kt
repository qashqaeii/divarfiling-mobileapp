package ir.divarfiling.mobile.feature.crm.components

import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.network.TodayItemDto

object TodayTaskLabels {

    fun typeLabel(type: String?): String = when (type?.lowercase()) {
        "reminder" -> "یادآور"
        "follow_up", "followup" -> "پیگیری"
        "call" -> "تماس"
        "visit" -> "بازدید"
        else -> type?.replace('_', ' ')?.takeIf { it.isNotBlank() } ?: "کار"
    }

    fun dueLabel(item: TodayItemDto): String? {
        item.reminder?.dueAt?.let { return formatDueDateTime(it) }
        item.contact?.nextFollowUpAt?.let { return formatDueDateTime(it) }
        return null
    }

    fun formatDueDateTime(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return DateUtils.formatJalaliDateTime(value) ?: DateUtils.formatJalaliDate(value)
    }

    fun titleLabel(item: TodayItemDto): String =
        item.reminder?.title?.takeIf { it.isNotBlank() }
            ?: item.contact?.fullName
            ?: "—"

    fun contactName(item: TodayItemDto): String =
        item.contact?.fullName ?: item.reminder?.title ?: "—"

    fun formatDisplayDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return DateUtils.todayJalaliLabel()
        return DateUtils.formatJalaliDate(isoDate) ?: DateUtils.todayJalaliLabel()
    }
}
