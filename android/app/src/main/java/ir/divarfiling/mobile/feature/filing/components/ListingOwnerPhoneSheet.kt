package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.util.PhoneNormalizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingOwnerPhoneSheet(
    name: String,
    phone: String,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    onCall: (String) -> Unit,
    onSms: (String) -> Unit = {},
    onWhatsApp: (String) -> Unit = {},
    onBale: (String) -> Unit = {},
    onDismiss: () -> Unit,
    divarUrl: String? = null,
    onOpenDivar: (() -> Unit)? = null,
) {
    DfSheetScaffold(
        title = "مالک آگهی",
        subtitle = "نام و شماره تماس مالک را برای پیگیری سریع نگه دارید",
        icon = DfIcons.Phone,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSaving) "در حال ذخیره…" else "ذخیره شماره",
                onPrimary = onSave,
                primaryEnabled = !isSaving,
                isSubmitting = isSaving,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "اطلاعات مالک") {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("نام مالک") },
                placeholder = { Text("مثلاً علی رضایی") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving,
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { onPhoneChange(PhoneNormalizer.normalize(it)) },
                label = { Text("شماره موبایل مالک") },
                placeholder = { Text("مثلاً ۰۹۱۲۱۲۳۴۵۶۷") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving,
            )
            if (onOpenDivar != null && !divarUrl.isNullOrBlank()) {
                DfGlassTextButton(
                    text = "از دیوار ببین",
                    onClick = onOpenDivar,
                )
            }
            if (phone.isNotBlank()) {
                ListingOwnerContactActions(
                    onCall = { onCall(phone) },
                    onSms = { onSms(phone) },
                    onWhatsApp = { onWhatsApp(phone) },
                    onBale = { onBale(phone) },
                )
            }
        }
    }
}
