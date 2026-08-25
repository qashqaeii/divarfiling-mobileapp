package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfJalaliDateField
import ir.divarfiling.mobile.core.design.components.DfDateTimePresets
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.feature.crm.ContactTypeVisuals
import ir.divarfiling.mobile.feature.crm.CrmConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactQuickLeadSheet(
    name: String,
    phone: String,
    customerType: String,
    source: String,
    notes: String,
    email: String = "",
    city: String = "",
    district: String = "",
    job: String = "",
    companyName: String = "",
    nationalId: String = "",
    leaseDeadline: String = "",
    money: ContactEditMoneyState = ContactEditMoneyState(),
    prefs: ContactEditPrefsState = ContactEditPrefsState(),
    builder: ContactEditBuilderState = ContactEditBuilderState(),
    matchingTolerancePercent: Int = 20,
    activeChannels: Set<String> = emptySet(),
    socialLinks: Map<String, String> = emptyMap(),
    catalogNeighborhoods: List<String> = emptyList(),
    isSubmitting: Boolean,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onCustomerTypeChange: (String) -> Unit,
    onSourceChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onEmailChange: (String) -> Unit = {},
    onCityChange: (String) -> Unit = {},
    onDistrictChange: (String) -> Unit = {},
    onJobChange: (String) -> Unit = {},
    onCompanyNameChange: (String) -> Unit = {},
    onNationalIdChange: (String) -> Unit = {},
    onLeaseDeadlineChange: (String) -> Unit = {},
    onBudgetMinChange: (String) -> Unit = {},
    onBudgetMaxChange: (String) -> Unit = {},
    onDepositMinChange: (String) -> Unit = {},
    onDepositMaxChange: (String) -> Unit = {},
    onRentMinChange: (String) -> Unit = {},
    onRentMaxChange: (String) -> Unit = {},
    onPropertyTypeChange: (String) -> Unit = {},
    onRoomsChange: (String) -> Unit = {},
    onRoomsMinChange: (String) -> Unit = {},
    onRoomsMaxChange: (String) -> Unit = {},
    onMinAreaChange: (String) -> Unit = {},
    onMaxAreaChange: (String) -> Unit = {},
    onAreasChange: (String) -> Unit = {},
    onYearMinChange: (String) -> Unit = {},
    onYearMaxChange: (String) -> Unit = {},
    onFloorMinChange: (String) -> Unit = {},
    onFloorMaxChange: (String) -> Unit = {},
    onWantParkingChange: (Boolean) -> Unit = {},
    onWantStorageChange: (Boolean) -> Unit = {},
    onWantElevatorChange: (Boolean) -> Unit = {},
    onBuilderBuyBudgetMinChange: (String) -> Unit = {},
    onBuilderBuyBudgetMaxChange: (String) -> Unit = {},
    onBuilderBuyMinAreaChange: (String) -> Unit = {},
    onBuilderBuyMaxAreaChange: (String) -> Unit = {},
    onBuilderBuyAreasChange: (String) -> Unit = {},
    onBuilderBuyTypesChange: (String) -> Unit = {},
    onMatchingToleranceChange: (Int) -> Unit = {},
    onToggleChannel: (String, Boolean) -> Unit = { _, _ -> },
    onSocialLinkChange: (String, String) -> Unit = { _, _ -> },
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val selectedType = customerType.ifBlank { "سرنخ" }
    val typeVisual = ContactTypeVisuals.visualFor(selectedType)

    DfSheetScaffold(
        title = "مخاطب جدید",
        subtitle = "ثبت کامل مخاطب با نقش، مالی، ترجیحات و کانال پیام",
        icon = typeVisual.icon,
        iconContainerColor = typeVisual.container,
        iconTint = typeVisual.accent,
        bodyHeightFraction = 0.92f,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ثبت…" else "ثبت مخاطب جدید",
                onPrimary = onSubmit,
                primaryEnabled = !isSubmitting && name.isNotBlank() && phone.isNotBlank(),
                isSubmitting = isSubmitting,
                secondaryText = "انصراف",
                onSecondary = onDismiss,
            )
        },
    ) {
        ContactTypeSelectorSection(
            selectedType = selectedType,
            enabled = !isSubmitting,
            onTypeChange = onCustomerTypeChange,
        )

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

        DfSheetSection(title = "اطلاعات تکمیلی") {
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("ایمیل") },
                placeholder = { Text("اختیاری") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                OutlinedTextField(
                    value = city,
                    onValueChange = onCityChange,
                    label = { Text("شهر") },
                    placeholder = { Text("اختیاری") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
                OutlinedTextField(
                    value = district,
                    onValueChange = onDistrictChange,
                    label = { Text("منطقه") },
                    placeholder = { Text("اختیاری") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
            }
            OutlinedTextField(
                value = job,
                onValueChange = onJobChange,
                label = { Text("شغل") },
                placeholder = { Text("اختیاری") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = companyName,
                onValueChange = onCompanyNameChange,
                label = { Text("نام شرکت") },
                placeholder = { Text("اختیاری") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = nationalId,
                onValueChange = onNationalIdChange,
                label = { Text("کد ملی") },
                placeholder = { Text("اختیاری") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            if (CrmConstants.showsLeaseDeadline(selectedType)) {
                DfJalaliDateField(
                    value = leaseDeadline,
                    onValueChange = onLeaseDeadlineChange,
                    label = "مهلت اجاره",
                    placeholder = "اختیاری — تقویم شمسی",
                    hint = "تاریخ پایان قرارداد اجاره مورد نظر",
                    enabled = !isSubmitting,
                    presets = DfDateTimePresets.leaseDeadlineShortcuts(),
                )
            }
        }

        ContactMoneyFormSection(
            customerType = selectedType,
            money = money,
            builder = builder,
            enabled = !isSubmitting,
            onBudgetMinChange = onBudgetMinChange,
            onBudgetMaxChange = onBudgetMaxChange,
            onDepositMinChange = onDepositMinChange,
            onDepositMaxChange = onDepositMaxChange,
            onRentMinChange = onRentMinChange,
            onRentMaxChange = onRentMaxChange,
            onBuilderBuyBudgetMinChange = onBuilderBuyBudgetMinChange,
            onBuilderBuyBudgetMaxChange = onBuilderBuyBudgetMaxChange,
            onBuilderBuyMinAreaChange = onBuilderBuyMinAreaChange,
            onBuilderBuyMaxAreaChange = onBuilderBuyMaxAreaChange,
            onBuilderBuyAreasChange = onBuilderBuyAreasChange,
            onBuilderBuyTypesChange = onBuilderBuyTypesChange,
            catalogNeighborhoods = catalogNeighborhoods,
        )

        ContactPropertyPrefsFormSection(
            customerType = selectedType,
            prefs = prefs,
            matchingTolerancePercent = matchingTolerancePercent,
            enabled = !isSubmitting,
            onPropertyTypeChange = onPropertyTypeChange,
            onRoomsChange = onRoomsChange,
            onRoomsMinChange = onRoomsMinChange,
            onRoomsMaxChange = onRoomsMaxChange,
            onMinAreaChange = onMinAreaChange,
            onMaxAreaChange = onMaxAreaChange,
            onAreasChange = onAreasChange,
            onYearMinChange = onYearMinChange,
            onYearMaxChange = onYearMaxChange,
            onFloorMinChange = onFloorMinChange,
            onFloorMaxChange = onFloorMaxChange,
            onWantParkingChange = onWantParkingChange,
            onWantStorageChange = onWantStorageChange,
            onWantElevatorChange = onWantElevatorChange,
            onMatchingToleranceChange = onMatchingToleranceChange,
            catalogNeighborhoods = catalogNeighborhoods,
        )

        ContactChannelsFormSection(
            phone = phone,
            activeChannels = activeChannels,
            socialLinks = socialLinks,
            enabled = !isSubmitting,
            onToggleChannel = onToggleChannel,
            onSocialLinkChange = onSocialLinkChange,
        )

        DfSheetSection(title = "یادداشت اولیه") {
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("توضیح کوتاه برای پیگیری") },
                placeholder = { Text("اختیاری") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = !isSubmitting,
            )
        }
    }
}
