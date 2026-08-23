package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.feature.crm.ContactTypeVisuals
import ir.divarfiling.mobile.feature.crm.CrmConstants

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContactQuickLeadSheet(
    name: String,
    phone: String,
    customerType: String,
    source: String,
    notes: String,
    isSubmitting: Boolean,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onCustomerTypeChange: (String) -> Unit,
    onSourceChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val selectedType = customerType.ifBlank { "سرنخ" }
    val typeVisual = ContactTypeVisuals.visualFor(selectedType)
    val extraTypes = CrmConstants.CUSTOMER_TYPES.filterNot { it in ContactTypeVisuals.primaryTypes }

    DfSheetScaffold(
        title = "مخاطب جدید",
        subtitle = "ثبت کامل‌تر برای پیگیری روزانه",
        icon = typeVisual.icon,
        iconContainerColor = typeVisual.container,
        iconTint = typeVisual.accent,
        bodyHeightFraction = 0.74f,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ثبت…" else "ثبت مخاطب",
                onPrimary = onSubmit,
                primaryEnabled = !isSubmitting && name.isNotBlank() && phone.isNotBlank(),
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "نوع مخاطب") {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ContactTypeVisuals.primaryTypes.forEach { type ->
                    val visual = ContactTypeVisuals.visualFor(type)
                    val selected = selectedType == type
                    Surface(
                        onClick = { onCustomerTypeChange(type) },
                        enabled = !isSubmitting,
                        shape = AppShapes.Chip,
                        color = if (selected) visual.container else DfThemeColors.surfaceVariant(),
                        border = BorderStroke(
                            1.dp,
                            if (selected) visual.accent.copy(alpha = 0.4f) else DfThemeColors.outlineSubtle(),
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = visual.icon,
                                contentDescription = null,
                                tint = if (selected) visual.accent else DfThemeColors.textMuted(),
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = type,
                                style = AppTypography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) visual.accent else DfThemeColors.textSecondary(),
                            )
                        }
                    }
                }
            }
            if (extraTypes.isNotEmpty()) {
                DfDropdown(
                    label = "سایر نقش‌ها",
                    value = if (selectedType in extraTypes) selectedType else "انتخاب کنید",
                    options = extraTypes,
                    enabled = !isSubmitting,
                    onSelect = onCustomerTypeChange,
                )
            }
        }

        DfSheetSection(title = "اطلاعات تماس") {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("نام و نام خانوادگی") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
                leadingIcon = {
                    Icon(DfIcons.User, contentDescription = null, tint = DfThemeColors.textMuted())
                },
            )
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("شماره موبایل") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = {
                    Icon(DfIcons.Phone, contentDescription = null, tint = DfThemeColors.textMuted())
                },
            )
            DfDropdown(
                label = "منبع آشنایی",
                value = source.ifBlank { "موبایل" },
                options = CrmConstants.SOURCES,
                enabled = !isSubmitting,
                onSelect = onSourceChange,
            )
        }

        DfSheetSection(title = "یادداشت اولیه") {
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("توضیح کوتاه برای پیگیری") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = !isSubmitting,
            )
        }
    }
}
