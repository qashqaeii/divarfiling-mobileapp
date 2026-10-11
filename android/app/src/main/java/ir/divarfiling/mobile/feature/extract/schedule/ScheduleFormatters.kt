package ir.divarfiling.mobile.feature.extract.schedule

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors

internal fun scheduleIntervalIcon(hours: Double): ImageVector = when {
    hours <= 0.25 -> DfIcons.Zap
    hours <= 0.5 -> DfIcons.Timer
    hours <= 2.0 -> DfIcons.Clock
    hours <= 6.0 -> DfIcons.Cloud
    hours <= 12.0 -> DfIcons.Moon
    else -> DfIcons.Calendar
}

internal fun scheduleIntervalAccent(hours: Double): Color = when {
    hours <= 0.25 -> DfColors.Amber
    hours <= 0.5 -> DfColors.Purple
    hours <= 2.0 -> DfColors.Blue
    hours <= 6.0 -> DfColors.Green
    hours <= 12.0 -> DfColors.Pink
    else -> DfColors.PurpleDark
}

internal fun scheduleIntervalLabel(hours: Double): String = when (hours) {
    0.25 -> "هر ۱۵ دقیقه"
    0.5 -> "هر ۳۰ دقیقه"
    1.0 -> "هر ۱ ساعت"
    2.0 -> "هر ۲ ساعت"
    6.0 -> "هر ۶ ساعت"
    12.0 -> "هر ۱۲ ساعت"
    24.0 -> "هر ۲۴ ساعت"
    else -> if (hours >= 1) {
        val value = if (hours % 1.0 == 0.0) hours.toInt().toString() else hours.toString()
        "هر ${DateUtils.toPersianDigits(value)} ساعت"
    } else {
        "هر ${DateUtils.toPersianDigits((hours * 60).toInt().toString())} دقیقه"
    }
}

internal fun scheduleDateTimeLabel(iso: String): String =
    DateUtils.formatForDisplay(iso)

internal fun scheduleRelativeLabel(iso: String?): String? =
    iso?.let { DateUtils.formatRelativeTimeAgo(it) }

internal fun scheduleNextRunLabel(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return DateUtils.formatRelativeTimeUntil(iso)
        ?: DateUtils.formatRelativeTimeAgo(iso).takeIf { it != "همین الان" }
        ?: "سررسید شده"
}

internal data class ScheduleStatusStyle(
    val label: String,
    val color: Color,
    val background: Color,
)

@Composable
internal fun scheduleStatusStyle(status: String): ScheduleStatusStyle = when (status) {
    "success" -> ScheduleStatusStyle("موفق", DfThemeColors.success(), DfThemeColors.successContainer())
    "failed" -> ScheduleStatusStyle("ناموفق", DfThemeColors.error(), DfThemeColors.errorContainer())
    "running" -> ScheduleStatusStyle("در حال اجرا", DfThemeColors.info(), DfThemeColors.infoContainer())
    "queued" -> ScheduleStatusStyle("در صف", DfThemeColors.warning(), DfThemeColors.warningContainer())
    "skipped_limit" -> ScheduleStatusStyle("سقف روزانه", DfThemeColors.warning(), DfThemeColors.warningContainer())
    else -> ScheduleStatusStyle(status, DfThemeColors.textMuted(), DfThemeColors.surfaceVariant())
}
