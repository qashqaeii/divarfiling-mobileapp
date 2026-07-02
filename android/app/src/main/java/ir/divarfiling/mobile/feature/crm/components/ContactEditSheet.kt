package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.feature.crm.CrmConstants
import ir.divarfiling.mobile.feature.crm.CrmTypeProfiles

data class ContactEditMoneyState(
    val budgetMin: String = "",
    val budgetMax: String = "",
    val depositMin: String = "",
    val depositMax: String = "",
    val rentMin: String = "",
    val rentMax: String = "",
)

data class ContactEditPrefsState(
    val propertyType: String = "",
    val rooms: String = "",
    val minArea: String = "",
    val maxArea: String = "",
    val areas: String = "",
)

data class ContactEditBuilderState(
    val buyBudgetMin: String = "",
    val buyBudgetMax: String = "",
    val buyMinArea: String = "",
    val buyMaxArea: String = "",
    val buyAreas: String = "",
    val buyPropertyTypes: String = "ویلا, کلنگی, زمین",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactEditSheet(
    name: String,
    phone: String,
    status: String,
    customerType: String,
    priority: String,
    money: ContactEditMoneyState,
    prefs: ContactEditPrefsState,
    builder: ContactEditBuilderState = ContactEditBuilderState(),
    notes: String,
    isSubmitting: Boolean,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onCustomerTypeChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onBudgetMinChange: (String) -> Unit,
    onBudgetMaxChange: (String) -> Unit,
    onDepositMinChange: (String) -> Unit,
    onDepositMaxChange: (String) -> Unit,
    onRentMinChange: (String) -> Unit,
    onRentMaxChange: (String) -> Unit,
    onPropertyTypeChange: (String) -> Unit,
    onRoomsChange: (String) -> Unit,
    onMinAreaChange: (String) -> Unit,
    onMaxAreaChange: (String) -> Unit,
    onAreasChange: (String) -> Unit,
    onBuilderBuyBudgetMinChange: (String) -> Unit = {},
    onBuilderBuyBudgetMaxChange: (String) -> Unit = {},
    onBuilderBuyMinAreaChange: (String) -> Unit = {},
    onBuilderBuyMaxAreaChange: (String) -> Unit = {},
    onBuilderBuyAreasChange: (String) -> Unit = {},
    onBuilderBuyTypesChange: (String) -> Unit = {},
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val profile = remember(customerType) {
        CrmTypeProfiles.profileFor(customerType.ifBlank { CrmConstants.CUSTOMER_TYPES.first() })
    }
    val showBudget = CrmTypeProfiles.showsBudget(profile.moneyMode)
    val showRent = CrmTypeProfiles.showsRent(profile.moneyMode)
    val showBuilderBuy = CrmTypeProfiles.showsBuilderBuy(profile.moneyMode)

    DfSheetScaffold(
        title = "ویرایش مخاطب",
        subtitle = "اطلاعات تماس و پروفایل مالی بر اساس نوع مخاطب",
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
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("شماره موبایل") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            DfDropdown(
                label = "نوع مخاطب",
                value = customerType.ifBlank { CrmConstants.CUSTOMER_TYPES.first() },
                options = CrmConstants.CUSTOMER_TYPES,
                enabled = !isSubmitting,
                onSelect = onCustomerTypeChange,
            )
            DfDropdown(
                label = "وضعیت",
                value = status.ifBlank { CrmConstants.STATUSES.first() },
                options = CrmConstants.STATUSES,
                enabled = !isSubmitting,
                onSelect = onStatusChange,
            )
            DfDropdown(
                label = "اولویت",
                value = priority.ifBlank { CrmConstants.PRIORITIES[1] },
                options = CrmConstants.PRIORITIES,
                enabled = !isSubmitting,
                onSelect = onPriorityChange,
            )
        }

        DfSheetSection(title = if (showBuilderBuy) "خط فروش — آپارتمان" else "اطلاعات مالی") {
            Text(
                text = profile.sectionHint,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = DfColors.TextMuted,
            )
            if (showBudget) {
                ContactMoneyRangeRow(
                    minValue = money.budgetMin,
                    maxValue = money.budgetMax,
                    minLabel = profile.budgetLabels.first,
                    maxLabel = profile.budgetLabels.second,
                    enabled = !isSubmitting,
                    onMinChange = onBudgetMinChange,
                    onMaxChange = onBudgetMaxChange,
                )
            }
            if (showRent) {
                ContactMoneyRangeRow(
                    minValue = money.depositMin,
                    maxValue = money.depositMax,
                    minLabel = profile.depositLabels.first,
                    maxLabel = profile.depositLabels.second,
                    enabled = !isSubmitting,
                    onMinChange = onDepositMinChange,
                    onMaxChange = onDepositMaxChange,
                )
                ContactMoneyRangeRow(
                    minValue = money.rentMin,
                    maxValue = money.rentMax,
                    minLabel = profile.rentLabels.first,
                    maxLabel = profile.rentLabels.second,
                    enabled = !isSubmitting,
                    onMinChange = onRentMinChange,
                    onMaxChange = onRentMaxChange,
                )
            }
        }

        if (showBuilderBuy) {
            DfSheetSection(title = "تأمین پروژه — خرید زمین و کلنگی") {
                Text(
                    text = "بودجه و منطقه خرید ویلا، کلنگی و زمین برای توسعه پروژه",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = DfColors.TextMuted,
                )
                ContactMoneyRangeRow(
                    minValue = builder.buyBudgetMin,
                    maxValue = builder.buyBudgetMax,
                    minLabel = "بودجه خرید از",
                    maxLabel = "بودجه خرید تا",
                    enabled = !isSubmitting,
                    onMinChange = onBuilderBuyBudgetMinChange,
                    onMaxChange = onBuilderBuyBudgetMaxChange,
                )
                ContactMoneyRangeRow(
                    minValue = builder.buyMinArea,
                    maxValue = builder.buyMaxArea,
                    minLabel = "متراژ خرید از",
                    maxLabel = "متراژ خرید تا",
                    enabled = !isSubmitting,
                    onMinChange = onBuilderBuyMinAreaChange,
                    onMaxChange = onBuilderBuyMaxAreaChange,
                )
                OutlinedTextField(
                    value = builder.buyAreas,
                    onValueChange = onBuilderBuyAreasChange,
                    label = { Text("محله‌های هدف خرید") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                )
                OutlinedTextField(
                    value = builder.buyPropertyTypes,
                    onValueChange = onBuilderBuyTypesChange,
                    label = { Text("انواع ملک هدف خرید") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                    placeholder = { Text("ویلا, کلنگی, زمین") },
                )
            }
        }

        DfSheetSection(title = if (showBuilderBuy) "مشخصات واحد فروش" else "ترجیحات ملک") {
            DfDropdown(
                label = "نوع ملک",
                value = prefs.propertyType.ifBlank { "—" },
                options = listOf("—") + CrmConstants.PROPERTY_TYPES,
                enabled = !isSubmitting,
                onSelect = { onPropertyTypeChange(if (it == "—") "" else it) },
            )
            OutlinedTextField(
                value = prefs.rooms,
                onValueChange = onRoomsChange,
                label = { Text("اتاق") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
                placeholder = { Text("مثلاً ۲") },
            )
            ContactMoneyRangeRow(
                minValue = prefs.minArea,
                maxValue = prefs.maxArea,
                minLabel = "متراژ از",
                maxLabel = "متراژ تا",
                enabled = !isSubmitting,
                onMinChange = onMinAreaChange,
                onMaxChange = onMaxAreaChange,
            )
            OutlinedTextField(
                value = prefs.areas,
                onValueChange = onAreasChange,
                label = { Text(if (showBuilderBuy) "محله‌های فروش / پروژه" else "محله‌های مورد نظر") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                enabled = !isSubmitting,
                placeholder = { Text("ونک، نیاوران، …") },
            )
        }

        DfSheetSection(title = "یادداشت") {
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("یادداشت داخلی") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                enabled = !isSubmitting,
                placeholder = { Text("نیازها، محدودیت‌ها یا نکات پیگیری…") },
            )
        }
    }
}

@Composable
private fun ContactMoneyRangeRow(
    minValue: String,
    maxValue: String,
    minLabel: String,
    maxLabel: String,
    enabled: Boolean,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = minValue,
            onValueChange = onMinChange,
            label = { Text(minLabel) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = enabled,
            placeholder = { Text("از", color = DfColors.TextMuted) },
        )
        OutlinedTextField(
            value = maxValue,
            onValueChange = onMaxChange,
            label = { Text(maxLabel) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = enabled,
            placeholder = { Text("تا", color = DfColors.TextMuted) },
        )
    }
}
