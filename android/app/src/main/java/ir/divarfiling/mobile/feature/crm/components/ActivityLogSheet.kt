package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.feature.crm.CrmConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogSheet(
    activityType: String,
    content: String,
    selectedStatus: String,
    isSubmitting: Boolean,
    onTypeChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val statusPlaceholder = "بدون تغییر"
    DfSheetScaffold(
        title = "ثبت فعالیت",
        subtitle = "تماس، بازدید یا پیگیری را در تایم‌لاین مخاطب ثبت کنید",
        icon = DfIcons.Clock,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ثبت…" else "ثبت فعالیت",
                onPrimary = onSubmit,
                primaryEnabled = !isSubmitting,
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "نوع و توضیحات") {
            DfDropdown(
                label = "نوع فعالیت",
                value = activityType,
                options = CrmConstants.QUICK_ACTIVITY_TYPES.map { it.first },
                enabled = !isSubmitting,
                onSelect = onTypeChange,
            )
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                label = { Text("توضیحات") },
                placeholder = { Text(activityPlaceholder(activityType)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !isSubmitting,
            )
            DfDropdown(
                label = "وضعیت مخاطب بعد از ثبت",
                value = selectedStatus.ifBlank { statusPlaceholder },
                options = listOf(statusPlaceholder) + CrmConstants.STATUSES,
                enabled = !isSubmitting,
                onSelect = { onStatusChange(if (it == statusPlaceholder) "" else it) },
            )
        }
    }
}

private fun activityPlaceholder(type: String): String = when (type) {
    "تماس" -> "مثلاً تماس برقرار شد و قرار شد فردا فایل‌ها ارسال شود"
    "واتساپ" -> "مثلاً فایل‌ها ارسال شد و منتظر بازخورد هستیم"
    "پیامک" -> "مثلاً پیام معرفی و زمان پیگیری بعدی ارسال شد"
    "بازدید" -> "مثلاً بازدید انجام شد، مشتری نور و پلان را پسندید"
    "پیگیری" -> "مثلاً نیاز مشتری مرور شد و قرار شد عصر تماس بگیریم"
    "جلسه" -> "مثلاً جلسه حضوری برگزار شد و موارد تصمیم‌گیری ثبت شد"
    else -> "جزئیات این فعالیت را بنویسید"
}
