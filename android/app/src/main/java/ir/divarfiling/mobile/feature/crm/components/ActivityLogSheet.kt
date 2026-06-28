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
    isSubmitting: Boolean,
    onTypeChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
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
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !isSubmitting,
            )
        }
    }
}
