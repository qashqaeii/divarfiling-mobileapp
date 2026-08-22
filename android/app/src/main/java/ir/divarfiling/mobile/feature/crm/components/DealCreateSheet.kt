package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.feature.crm.CrmConstants

private const val NO_PROPERTY = "بدون ملک"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealCreateSheet(
    contacts: List<ContactDto>,
    properties: List<PropertyDto>,
    stages: List<String>,
    selectedContactId: Long?,
    selectedPropertyId: Long?,
    selectedStage: String,
    title: String,
    amount: String,
    commissionRate: String,
    notes: String,
    isSubmitting: Boolean,
    onContactSelect: (Long) -> Unit,
    onPropertySelect: (Long?) -> Unit,
    onStageChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCommissionRateChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val contactNames = contacts.map { it.fullName }
    val selectedName = contacts.firstOrNull { it.id == selectedContactId }?.fullName.orEmpty()
    val propertyOptions = listOf(NO_PROPERTY) + properties.map { it.title }
    val selectedPropertyLabel = properties.firstOrNull { it.id == selectedPropertyId }?.title ?: NO_PROPERTY
    val stageOptions = stages.ifEmpty { CrmConstants.DEAL_STAGES }
    val resolvedStage = selectedStage.ifBlank { stageOptions.first() }

    DfSheetScaffold(
        title = "معامله جدید",
        subtitle = "فرصت فروش جدید را با مخاطب، ملک و مرحله فروش ثبت کنید",
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
        DfSheetSection(title = "مخاطب") {
            if (contacts.isEmpty()) {
                Text(
                    text = "ابتدا در بخش مخاطبین، یک مشتری ثبت کنید",
                    style = AppTypography.labelSmall,
                    color = DfColors.Amber,
                )
            } else {
                DfDropdown(
                    label = "انتخاب مخاطب",
                    value = selectedName.ifBlank { "مخاطب را انتخاب کنید" },
                    options = contactNames,
                    enabled = !isSubmitting,
                    onSelect = { name ->
                        contacts.firstOrNull { it.fullName == name }?.id?.let(onContactSelect)
                    },
                )
            }
        }

        DfSheetSection(title = "ملک مرتبط") {
            if (properties.isEmpty()) {
                Text(
                    text = "فایل شخصی ثبت‌شده‌ای ندارید — می‌توانید بعداً ملک را متصل کنید",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            } else {
                DfDropdown(
                    label = "انتخاب ملک (اختیاری)",
                    value = selectedPropertyLabel,
                    options = propertyOptions,
                    enabled = !isSubmitting,
                    onSelect = { label ->
                        if (label == NO_PROPERTY) {
                            onPropertySelect(null)
                        } else {
                            properties.firstOrNull { it.title == label }?.id?.let(onPropertySelect)
                        }
                    },
                )
            }
        }

        DfSheetSection(title = "جزئیات معامله") {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("عنوان معامله") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("مثلاً فروش آپارتمان ۱۲۰ متری") },
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("مبلغ تقریبی (تومان)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("اختیاری — برای پیش‌بینی درآمد") },
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = commissionRate,
                onValueChange = onCommissionRateChange,
                label = { Text("نرخ کمیسیون (٪)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("مثلاً ۲") },
                enabled = !isSubmitting,
            )
        }

        DfSheetSection(title = "مرحله فروش") {
            Text(
                text = "احتمال بسته‌شدن بر اساس مرحله انتخاب‌شده محاسبه می‌شود",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
            DealStageOptionList(
                stages = stageOptions,
                selectedStage = resolvedStage,
                onStageSelect = onStageChange,
                enabled = !isSubmitting,
            )
        }

        DfSheetSection(title = "یادداشت") {
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("یادداشت داخلی") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text("جزئیات مذاکره، یادآوری تماس یا شرایط خاص…") },
                enabled = !isSubmitting,
            )
        }
    }
}
