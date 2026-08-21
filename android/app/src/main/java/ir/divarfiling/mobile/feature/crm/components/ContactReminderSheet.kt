package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDateTimeSelector
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection

val ReminderRecurrenceOptions = listOf(
    "" to "یک‌بار",
    "daily" to "روزانه",
    "weekly" to "هفتگی",
    "biweekly" to "دو هفته‌ای",
    "monthly" to "ماهانه",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContactReminderSheet(
    title: String,
    note: String,
    dueMillis: Long,
    recurrence: String = "",
    isSubmitting: Boolean,
    sheetTitle: String = "یادآور جدید",
    primaryText: String = "ثبت یادآور",
    onDelete: (() -> Unit)? = null,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDueChange: (Long) -> Unit,
    onRecurrenceChange: (String) -> Unit = {},
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
) {
    DfSheetScaffold(
        title = sheetTitle,
        subtitle = "زمان پیگیری را تنظیم کنید",
        icon = DfIcons.AlarmClock,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ثبت…" else primaryText,
                onPrimary = onSubmit,
                primaryEnabled = !isSubmitting && title.isNotBlank(),
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "جزئیات یادآور") {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("عنوان") },
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("یادداشت (اختیاری)") },
                enabled = !isSubmitting,
            )
            DfDateTimeSelector(
                millis = dueMillis,
                onChange = onDueChange,
                enabled = !isSubmitting,
            )
        }
        DfSheetSection(title = "تکرار") {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ReminderRecurrenceOptions.forEach { (value, label) ->
                    FilterChip(
                        selected = recurrence == value,
                        onClick = { onRecurrenceChange(value) },
                        enabled = !isSubmitting,
                        label = {
                            Text(label, style = AppTypography.labelSmall)
                        },
                        shape = RoundedCornerShape(999.dp),
                    )
                }
            }
            Text(
                text = "پس از انجام یادآور تکرارشونده، نوبت بعدی خودکار ساخته می‌شود.",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
                modifier = Modifier.padding(top = 4.dp),
            )
            onDelete?.let {
                DfGlassTextButton(
                    text = "حذف یادآور",
                    onClick = it,
                )
            }
        }
    }
}
