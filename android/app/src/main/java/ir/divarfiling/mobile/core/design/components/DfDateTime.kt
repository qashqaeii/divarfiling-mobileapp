package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class DfDateTimePickerMode { Date, Time, Both }

data class DfDateTimePreset(
    val id: String,
    val label: String,
    val millisProvider: () -> Long,
)

object DfDateTimePresets {
    fun defaultShortcuts(zone: ZoneId = ZoneId.systemDefault()): List<DfDateTimePreset> {
        val keepTime: (LocalDate) -> Long = { date ->
            val now = Instant.now().atZone(zone)
            date.atTime(now.hour, now.minute).atZone(zone).toInstant().toEpochMilli()
        }
        return listOf(
            DfDateTimePreset("today", "امروز") {
                keepTime(LocalDate.now(zone))
            },
            DfDateTimePreset("tomorrow", "فردا") {
                keepTime(LocalDate.now(zone).plusDays(1))
            },
            DfDateTimePreset("in3days", "۳ روز دیگر") {
                keepTime(LocalDate.now(zone).plusDays(3))
            },
            DfDateTimePreset("week", "۱ هفته") {
                keepTime(LocalDate.now(zone).plusDays(7))
            },
        )
    }
}

/**
 * انتخاب تاریخ و ساعت شمسی بدون اسکرول تو در تو.
 * Material TimePicker عمداً استفاده نمی‌شود چون داخل BottomSheet اسکرول‌پذیر crash می‌دهد.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DfDateTimeSelector(
    millis: Long,
    onChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    allowClear: Boolean = false,
    onClear: (() -> Unit)? = null,
    presets: List<DfDateTimePreset> = DfDateTimePresets.defaultShortcuts(),
    dateLabel: String = "تاریخ",
    timeLabel: String = "ساعت",
) {
    var pickerMode by remember { mutableStateOf<DfDateTimePickerMode?>(null) }
    val zone = ZoneId.systemDefault()
    val dateText = DateUtils.formatJalaliDateFromMillis(millis, zone)
    val timeText = DateUtils.formatTimeFromMillis(millis, zone)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Surface(
                onClick = { if (enabled) pickerMode = DfDateTimePickerMode.Date },
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = AppShapes.Chip,
                color = DfColors.PurpleContainer.copy(alpha = 0.55f),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(dateLabel, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                    Text(
                        dateText,
                        style = AppTypography.bodyDescription,
                        fontWeight = FontWeight.SemiBold,
                        color = DfColors.TextPrimary,
                    )
                }
            }
            Surface(
                onClick = { if (enabled) pickerMode = DfDateTimePickerMode.Time },
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = AppShapes.Chip,
                color = DfColors.PurpleContainer.copy(alpha = 0.55f),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(timeLabel, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                    Text(
                        timeText,
                        style = AppTypography.bodyDescription,
                        fontWeight = FontWeight.SemiBold,
                        color = DfColors.TextPrimary,
                    )
                }
            }
        }
        if (presets.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                presets.forEach { preset ->
                    FilterChip(
                        selected = false,
                        onClick = { if (enabled) onChange(preset.millisProvider()) },
                        enabled = enabled,
                        label = { Text(preset.label, style = AppTypography.labelSmall) },
                        shape = RoundedCornerShape(999.dp),
                    )
                }
                if (allowClear && onClear != null) {
                    FilterChip(
                        selected = false,
                        onClick = { if (enabled) onClear() },
                        enabled = enabled,
                        label = { Text("پاک کردن", style = AppTypography.labelSmall) },
                        shape = RoundedCornerShape(999.dp),
                    )
                }
            }
        }
    }

    pickerMode?.let { mode ->
        DfDateTimePickerDialog(
            millis = millis,
            mode = mode,
            onConfirm = {
                onChange(it)
                pickerMode = null
            },
            onDismiss = { pickerMode = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfDateTimePickerDialog(
    millis: Long,
    mode: DfDateTimePickerMode,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val zone = ZoneId.systemDefault()
    val (initJy, initJm, initJd) = DateUtils.millisToJalali(millis, zone)
    val local = Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime()
    var jalaliYear by remember(millis) { mutableIntStateOf(initJy) }
    var jalaliMonth by remember(millis) { mutableIntStateOf(initJm) }
    var jalaliDay by remember(millis) { mutableIntStateOf(initJd) }
    var hour by remember(millis) { mutableIntStateOf(local.hour) }
    var minute by remember(millis) { mutableIntStateOf(local.minute) }

    val title = when (mode) {
        DfDateTimePickerMode.Date -> "انتخاب تاریخ شمسی"
        DfDateTimePickerMode.Time -> "انتخاب ساعت"
        DfDateTimePickerMode.Both -> "انتخاب تاریخ و ساعت"
    }
    val previewMillis = DateUtils.jalaliDateTimeToMillis(
        jalaliYear, jalaliMonth, jalaliDay.coerceIn(1, DateUtils.jalaliDaysInMonth(jalaliYear, jalaliMonth)),
        hour, minute, zone,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = true),
        title = {
            Text(
                text = title,
                style = AppTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = DfColors.TextPrimary,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = "انتخاب‌شده: ${DateUtils.formatJalaliDateTimeFromMillis(previewMillis, zone)}",
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                if (mode != DfDateTimePickerMode.Time) {
                    DfJalaliDateFields(
                        year = jalaliYear,
                        month = jalaliMonth,
                        day = jalaliDay,
                        onYearChange = { jalaliYear = it },
                        onMonthChange = { month ->
                            jalaliMonth = month
                            val maxDay = DateUtils.jalaliDaysInMonth(jalaliYear, month)
                            if (jalaliDay > maxDay) jalaliDay = maxDay
                        },
                        onDayChange = { jalaliDay = it },
                    )
                }
                if (mode != DfDateTimePickerMode.Date) {
                    DfClockFields(
                        hour = hour,
                        minute = minute,
                        onHourChange = { hour = it },
                        onMinuteChange = { minute = it },
                    )
                }
            }
        },
        confirmButton = {
            DfPrimaryButton(
                text = "تأیید",
                onClick = {
                    val maxDay = DateUtils.jalaliDaysInMonth(jalaliYear, jalaliMonth)
                    onConfirm(
                        DateUtils.jalaliDateTimeToMillis(
                            jalaliYear,
                            jalaliMonth,
                            jalaliDay.coerceIn(1, maxDay),
                            hour.coerceIn(0, 23),
                            minute.coerceIn(0, 59),
                            zone,
                        ),
                    )
                },
            )
        },
        dismissButton = {
            DfGlassTextButton(text = "انصراف", onClick = onDismiss)
        },
    )
}

@Composable
internal fun DfJalaliDateFields(
    year: Int,
    month: Int,
    day: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentJy = DateUtils.millisToJalali(System.currentTimeMillis()).first
    val yearOptions = (currentJy - 1..currentJy + 5).map {
        DateUtils.toPersianDigits(it.toString())
    }
    val monthOptions = DateUtils.jalaliMonthNames
    val maxDay = DateUtils.jalaliDaysInMonth(year, month)
    val dayOptions = (1..maxDay).map { DateUtils.toPersianDigits(it.toString()) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        DfDropdown(
            label = "سال",
            value = DateUtils.toPersianDigits(year.toString()),
            options = yearOptions,
            enabled = true,
            onSelect = { selected -> parsePersianInt(selected)?.let(onYearChange) },
        )
        DfDropdown(
            label = "ماه",
            value = DateUtils.jalaliMonthName(month),
            options = monthOptions,
            enabled = true,
            onSelect = { name ->
                val index = monthOptions.indexOf(name)
                if (index >= 0) onMonthChange(index + 1)
            },
        )
        DfDropdown(
            label = "روز",
            value = DateUtils.toPersianDigits(day.toString()),
            options = dayOptions,
            enabled = true,
            onSelect = { selected -> parsePersianInt(selected)?.let(onDayChange) },
        )
    }
}

@Composable
private fun DfClockFields(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hourOptions = (0..23).map { DateUtils.toPersianDigits("%02d".format(it)) }
    val minuteOptions = (0..59).map { DateUtils.toPersianDigits("%02d".format(it)) }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        DfDropdown(
            label = "ساعت",
            value = DateUtils.toPersianDigits("%02d".format(hour)),
            options = hourOptions,
            enabled = true,
            onSelect = { selected -> parsePersianInt(selected)?.let { onHourChange(it.coerceIn(0, 23)) } },
            modifier = Modifier.weight(1f),
        )
        DfDropdown(
            label = "دقیقه",
            value = DateUtils.toPersianDigits("%02d".format(minute)),
            options = minuteOptions,
            enabled = true,
            onSelect = { selected -> parsePersianInt(selected)?.let { onMinuteChange(it.coerceIn(0, 59)) } },
            modifier = Modifier.weight(1f),
        )
    }
}

internal fun parsePersianInt(value: String): Int? {
    val digits = buildString {
        for (ch in value) {
            when (ch) {
                in '۰'..'۹' -> append("۰۱۲۳۴۵۶۷۸۹".indexOf(ch))
                in '0'..'9' -> append(ch - '0')
                else -> return null
            }
        }
    }
    return digits.toIntOrNull()
}
