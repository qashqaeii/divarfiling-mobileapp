package ir.divarfiling.mobile.feature.share

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.components.ShareToggleRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicShareSettingsSheet(
    consultantName: String,
    consultantPhone: String,
    welcomeMessage: String,
    isActive: Boolean,
    showDivarLink: Boolean,
    showFullAddress: Boolean,
    showInternalNotes: Boolean,
    isSubmitting: Boolean,
    onConsultantNameChange: (String) -> Unit,
    onConsultantPhoneChange: (String) -> Unit,
    onWelcomeMessageChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onShowDivarLinkChange: (Boolean) -> Unit,
    onShowFullAddressChange: (Boolean) -> Unit,
    onShowInternalNotesChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    DfSheetScaffold(
        title = "تنظیمات صفحه عمومی",
        subtitle = "اطلاعات مشاور و حریم نمایش برای لینک مشتری",
        icon = DfIcons.Settings,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ذخیره…" else "ذخیره تنظیمات",
                onPrimary = onSave,
                primaryEnabled = !isSubmitting,
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "اطلاعات مشاور") {
            OutlinedTextField(
                value = consultantName,
                onValueChange = onConsultantNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("نام مشاور") },
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = consultantPhone,
                onValueChange = onConsultantPhoneChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("شماره تماس مشاور") },
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = welcomeMessage,
                onValueChange = onWelcomeMessageChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("پیام خوش‌آمد") },
                placeholder = { Text("مثلاً برای بازدید با من هماهنگ کنید") },
                minLines = 3,
                enabled = !isSubmitting,
            )
        }
        DfSheetSection(title = "تنظیمات نمایش") {
            ShareToggleRow(
                title = "صفحه عمومی فعال باشد",
                subtitle = "در صورت خاموش‌بودن، لینک مشتری غیرفعال می‌شود",
                checked = isActive,
                onCheckedChange = onIsActiveChange,
            )
            ShareToggleRow(
                title = "نمایش لینک دیوار",
                subtitle = "لینک آگهی اصلی هم در صفحه مشتری نمایش داده شود",
                checked = showDivarLink,
                onCheckedChange = onShowDivarLinkChange,
            )
            ShareToggleRow(
                title = "نمایش آدرس کامل",
                subtitle = "آدرس دقیق ملک به مشتری نشان داده شود",
                checked = showFullAddress,
                onCheckedChange = onShowFullAddressChange,
            )
            ShareToggleRow(
                title = "نمایش یادداشت داخلی",
                subtitle = "یادداشت داخلی پرونده هم برای مشتری قابل مشاهده باشد",
                checked = showInternalNotes,
                onCheckedChange = onShowInternalNotesChange,
            )
        }
    }
}
