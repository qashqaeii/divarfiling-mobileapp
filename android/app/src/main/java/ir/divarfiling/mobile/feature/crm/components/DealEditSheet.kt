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
import ir.divarfiling.mobile.core.network.PropertyDto

private const val NO_PROPERTY = "بدون ملک"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealEditSheet(
    title: String,
    amount: String,
    commissionRate: String,
    notes: String,
    stages: List<String>,
    selectedStage: String,
    properties: List<PropertyDto>,
    selectedPropertyId: Long?,
    isSubmitting: Boolean,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCommissionRateChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onStageChange: (String) -> Unit,
    onPropertySelect: (Long?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val propertyOptions = listOf(NO_PROPERTY) + properties.map { it.title }
    val selectedPropertyLabel = properties.firstOrNull { it.id == selectedPropertyId }?.title ?: NO_PROPERTY

    DfSheetScaffold(
        title = "ویرایش معامله",
        subtitle = "عنوان، مبلغ، ملک، کمیسیون و مرحله فروش را به‌روز کنید",
        icon = DfIcons.Handshake,
        iconContainerColor = DfColors.PurpleContainer,
        iconTint = DfColors.Purple,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ذخیره…" else "ذخیره تغییرات",
                onPrimary = onSave,
                primaryEnabled = !isSubmitting && title.isNotBlank(),
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "جزئیات") {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("عنوان") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("مبلغ (تومان)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = commissionRate,
                onValueChange = onCommissionRateChange,
                label = { Text("نرخ کمیسیون (٪)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
        }

        DfSheetSection(title = "ملک مرتبط") {
            if (properties.isEmpty()) {
                Text(
                    text = "فایل شخصی برای اتصال موجود نیست",
                    style = ir.divarfiling.mobile.core.design.AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            } else {
                DfDropdown(
                    label = "ملک",
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

        DfSheetSection(title = "مرحله فروش") {
            DealStageOptionList(
                stages = stages,
                selectedStage = selectedStage,
                onStageSelect = onStageChange,
                enabled = !isSubmitting,
            )
        }

        DfSheetSection(title = "یادداشت") {
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("یادداشت") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !isSubmitting,
            )
        }
    }
}
