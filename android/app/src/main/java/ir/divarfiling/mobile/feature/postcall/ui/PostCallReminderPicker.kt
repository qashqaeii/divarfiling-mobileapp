package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfDateTimePreset
import ir.divarfiling.mobile.core.design.components.DfDateTimePresets
import ir.divarfiling.mobile.core.design.components.DfDateTimeSelector
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.feature.postcall.PostCallLogic
import java.time.Instant
import java.time.ZoneId

@Composable
fun PostCallReminderPicker(
    selectedMillis: Long,
    enabled: Boolean,
    savedAtMillis: Long?,
    onQuickOneHour: () -> Unit,
    onQuickTomorrow: () -> Unit,
    onReminderTimeChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val zone = remember { ZoneId.systemDefault() }
    val presets = remember(zone) {
        listOf(
            DfDateTimePreset("one_hour", "۱ ساعت دیگر") {
                PostCallLogic.ReminderScheduling.oneHourFromNow(zone = zone).toEpochMilli()
            },
            DfDateTimePreset("tomorrow", "فردا") {
                PostCallLogic.ReminderScheduling.tomorrowMorning(zone = zone).toEpochMilli()
            },
        ) + DfDateTimePresets.defaultShortcuts(zone).filter { it.id != "today" }
    }
    var customMillis by remember(selectedMillis) { mutableLongStateOf(selectedMillis) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            DfSecondaryButton(
                text = "۱ ساعت دیگر",
                onClick = onQuickOneHour,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
            DfSecondaryButton(
                text = "فردا صبح",
                onClick = onQuickTomorrow,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
        }
        DfDateTimeSelector(
            millis = customMillis,
            onChange = {
                customMillis = it
                onReminderTimeChange(it)
            },
            enabled = enabled,
            presets = presets,
            dateLabel = "تاریخ پیگیری",
            timeLabel = "ساعت",
        )
        savedAtMillis?.let { ms ->
            Text(
                "یادآور ثبت‌شده: ${DateUtils.formatJalaliDateFromMillis(ms)}، ${DateUtils.formatTimeFromMillis(ms)}",
                style = AppTypography.labelSmall,
                color = DfThemeColors.textSecondary(),
                modifier = Modifier.padding(top = AppSpacing.xxs),
            )
        }
    }
}
