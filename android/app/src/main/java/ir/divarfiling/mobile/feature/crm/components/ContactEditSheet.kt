package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.feature.crm.CrmConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactEditSheet(
    name: String,
    phone: String,
    status: String,
    customerType: String,
    priority: String,
    budget: String,
    notes: String,
    isSubmitting: Boolean,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onCustomerTypeChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onBudgetChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    DfSheetScaffold(
        title = "ویرایش مخاطب",
        subtitle = "اطلاعات تماس و وضعیت CRM را به‌روز کنید",
        icon = DfIcons.User,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ذخیره…" else "ذخیره تغییرات",
                onPrimary = onSave,
                primaryEnabled = !isSubmitting && name.isNotBlank() && phone.isNotBlank(),
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "اطلاعات اصلی") {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("نام و نام خانوادگی") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("شماره موبایل") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            DfDropdown(
                label = "وضعیت",
                value = status.ifBlank { CrmConstants.STATUSES.first() },
                options = CrmConstants.STATUSES,
                enabled = !isSubmitting,
                onSelect = onStatusChange,
            )
            DfDropdown(
                label = "نوع مشتری",
                value = customerType.ifBlank { CrmConstants.CUSTOMER_TYPES.first() },
                options = CrmConstants.CUSTOMER_TYPES,
                enabled = !isSubmitting,
                onSelect = onCustomerTypeChange,
            )
            DfDropdown(
                label = "اولویت",
                value = priority.ifBlank { CrmConstants.PRIORITIES[1] },
                options = CrmConstants.PRIORITIES,
                enabled = !isSubmitting,
                onSelect = onPriorityChange,
            )
        }
        DfSheetSection(title = "جزئیات تکمیلی") {
            OutlinedTextField(
                value = budget,
                onValueChange = onBudgetChange,
                label = { Text("بودجه (تومان)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("مثلاً ۵٬۰۰۰٬۰۰۰٬۰۰۰") },
            )
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("یادداشت داخلی") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                placeholder = { Text("نیازها، محدودیت‌ها یا نکات پیگیری…") },
            )
        }
    }
}
