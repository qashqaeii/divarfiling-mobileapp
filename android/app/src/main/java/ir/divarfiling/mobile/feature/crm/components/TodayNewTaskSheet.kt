package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.components.DfDateTimeSelector
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.network.ContactDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayNewTaskSheet(
    contacts: List<ContactDto>,
    selectedContactId: Long?,
    title: String,
    dueMillis: Long,
    isSubmitting: Boolean,
    onContactSelect: (Long) -> Unit,
    onTitleChange: (String) -> Unit,
    onDueChange: (Long) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val contactNames = contacts.map { it.fullName }
    val selectedName = contacts.firstOrNull { it.id == selectedContactId }?.fullName.orEmpty()

    DfSheetScaffold(
        title = "کار جدید",
        subtitle = "یادآور یا پیگیری جدید برای مخاطب ثبت کنید",
        icon = DfIcons.ListTodo,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ثبت…" else "ثبت کار",
                onPrimary = onSubmit,
                primaryEnabled = !isSubmitting && selectedContactId != null && title.isNotBlank(),
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "جزئیات کار") {
            if (contacts.isEmpty()) {
                Text(
                    text = "ابتدا یک مخاطب در CRM ثبت کنید",
                    style = AppTypography.labelSmall,
                    color = DfColors.Amber,
                )
            } else {
                DfDropdown(
                    label = "مخاطب",
                    value = selectedName.ifBlank { "انتخاب مخاطب" },
                    options = contactNames,
                    enabled = !isSubmitting,
                    onSelect = { name ->
                        contacts.firstOrNull { it.fullName == name }?.id?.let(onContactSelect)
                    },
                )
            }
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("عنوان کار") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("مثلاً پیگیری تماس") },
                enabled = !isSubmitting,
            )
            DfDateTimeSelector(
                millis = dueMillis,
                onChange = onDueChange,
                enabled = !isSubmitting,
            )
        }
    }
}
