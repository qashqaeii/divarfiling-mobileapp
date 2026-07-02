package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDateTimePickerPanel
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactReminderSheet(
    title: String,
    note: String,
    dueMillis: Long,
    isSubmitting: Boolean,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDueChange: (Long) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val dueLabel = DateUtils.formatJalaliDateTimeFromMillis(dueMillis)

    DfSheetScaffold(
        title = "یادآور جدید",
        subtitle = "زمان پیگیری بعدی را برای مخاطب تنظیم کنید",
        icon = DfIcons.AlarmClock,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ثبت…" else "ثبت یادآور",
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
            DfGlassTextButton(
                text = "زمان: $dueLabel",
                onClick = { showPicker = true },
            )
            AnimatedVisibility(
                visible = showPicker,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                DfDateTimePickerPanel(
                    dueMillis = dueMillis,
                    onDueChange = onDueChange,
                    onCancel = { showPicker = false },
                    onFinished = { showPicker = false },
                )
            }
        }
    }
}
