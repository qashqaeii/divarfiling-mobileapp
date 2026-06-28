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
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.feature.crm.CrmConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealCreateSheet(
    contacts: List<ContactDto>,
    stages: List<String>,
    selectedContactId: Long?,
    selectedStage: String,
    title: String,
    amount: String,
    notes: String,
    isSubmitting: Boolean,
    onContactSelect: (Long) -> Unit,
    onStageChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val contactNames = contacts.map { it.fullName }
    val selectedName = contacts.firstOrNull { it.id == selectedContactId }?.fullName.orEmpty()
    val stageOptions = stages.ifEmpty { CrmConstants.DEAL_STAGES }

    DfSheetScaffold(
        title = "معامله جدید",
        subtitle = "فرصت فروش را به مخاطب و مرحله فروش متصل کنید",
        icon = DfIcons.Handshake,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ثبت…" else "ثبت معامله",
                onPrimary = onSubmit,
                primaryEnabled = !isSubmitting && selectedContactId != null && title.isNotBlank(),
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "اطلاعات معامله") {
            if (contacts.isEmpty()) {
                Text(
                    text = "ابتدا در بخش مخاطبین، یک مشتری ثبت کنید",
                    style = ir.divarfiling.mobile.core.design.AppTypography.labelSmall,
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
                label = { Text("عنوان معامله") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("مثلاً فروش آپارتمان ۱۲۰ متری") },
                enabled = !isSubmitting,
            )
            DfDropdown(
                label = "مرحله فروش",
                value = selectedStage.ifBlank { stageOptions.first() },
                options = stageOptions,
                enabled = !isSubmitting,
                onSelect = onStageChange,
            )
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("مبلغ تقریبی (تومان)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("اختیاری") },
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("یادداشت") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                placeholder = { Text("جزئیات مذاکره یا یادآوری…") },
                enabled = !isSubmitting,
            )
        }
    }
}
