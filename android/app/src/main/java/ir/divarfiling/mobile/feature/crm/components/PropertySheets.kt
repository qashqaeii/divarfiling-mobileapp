package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import ir.divarfiling.mobile.core.design.components.DfSheetAdvancedBlock
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetOptionRow
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.components.DfMoneyField
import ir.divarfiling.mobile.core.util.PhoneNormalizer
import ir.divarfiling.mobile.feature.crm.CrmConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyCreateSheet(
    title: String,
    city: String,
    district: String,
    dealMode: String,
    propertyType: String,
    area: String,
    salePrice: String,
    deposit: String,
    rent: String,
    notes: String,
    isSubmitting: Boolean,
    onTitleChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onDealModeChange: (String) -> Unit,
    onPropertyTypeChange: (String) -> Unit,
    onAreaChange: (String) -> Unit,
    onSalePriceChange: (String) -> Unit,
    onDepositChange: (String) -> Unit,
    onRentChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isRent = dealMode.contains("اجاره") || dealMode.contains("رهن")
    var showAdvanced by remember { mutableStateOf(false) }

    DfSheetScaffold(
        title = "فایل شخصی جدید",
        subtitle = "عنوان، موقعیت و قیمت کافی است؛ جزئیات بعداً تکمیل می‌شود",
        icon = DfIcons.Building,
        onClose = onDismiss,
        scrollable = true,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ثبت…" else "ثبت فایل",
                onPrimary = onSubmit,
                primaryEnabled = !isSubmitting && title.isNotBlank(),
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "اطلاعات اصلی") {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("عنوان ملک") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("مثلاً آپارتمان ۱۰۰ متری سعادت‌آباد") },
                enabled = !isSubmitting,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                OutlinedTextField(
                    value = city,
                    onValueChange = onCityChange,
                    label = { Text("شهر") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
                OutlinedTextField(
                    value = district,
                    onValueChange = onDistrictChange,
                    label = { Text("محله") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
            }
            OutlinedTextField(
                value = area,
                onValueChange = onAreaChange,
                label = { Text("متراژ (متر)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }

        DfSheetSection(title = "نوع معامله") {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                CrmConstants.PROPERTY_DEAL_MODES.forEach { mode ->
                    DfSheetOptionRow(
                        label = mode,
                        selected = mode == dealMode,
                        onClick = { if (!isSubmitting) onDealModeChange(mode) },
                        icon = if (mode.contains("فروش")) DfIcons.Home else DfIcons.Tag,
                    )
                }
            }
        }

        DfSheetSection(title = "قیمت‌گذاری") {
            if (isRent) {
                DfMoneyField(
                    value = deposit,
                    onValueChange = onDepositChange,
                    label = "رهن",
                    enabled = !isSubmitting,
                )
                DfMoneyField(
                    value = rent,
                    onValueChange = onRentChange,
                    label = "اجاره ماهانه",
                    enabled = !isSubmitting,
                )
            } else {
                DfMoneyField(
                    value = salePrice,
                    onValueChange = onSalePriceChange,
                    label = "قیمت فروش",
                    enabled = !isSubmitting,
                )
            }
        }

        DfSheetAdvancedBlock(
            title = if (showAdvanced) "بستن جزئیات بیشتر" else "نوع ملک و یادداشت",
            expanded = showAdvanced,
            onToggle = { showAdvanced = !showAdvanced },
        ) {
            DfSheetSection(title = "نوع ملک") {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    CrmConstants.PROPERTY_TYPES.forEach { type ->
                        DfSheetOptionRow(
                            label = type,
                            selected = type == propertyType,
                            onClick = { if (!isSubmitting) onPropertyTypeChange(type) },
                            icon = PropertyFilters.propertyTypeIcon(type),
                        )
                    }
                }
            }
            DfSheetSection(title = "یادداشت") {
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("یادداشت داخلی") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("شرایط ویژه، یادآوری تماس یا توضیحات ملک…") },
                    enabled = !isSubmitting,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyEditSheet(
    title: String,
    city: String,
    district: String,
    neighborhood: String,
    dealMode: String,
    propertyType: String,
    transactionStatus: String,
    area: String,
    rooms: String,
    salePrice: String,
    deposit: String,
    rent: String,
    address: String,
    notes: String,
    isSubmitting: Boolean,
    floor: String = "",
    buildYear: String = "",
    amenities: String = "",
    hasParking: Boolean = false,
    hasStorage: Boolean = false,
    hasElevator: Boolean = false,
    isVacant: Boolean = false,
    ownerName: String = "",
    ownerPhone: String = "",
    onTitleChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onNeighborhoodChange: (String) -> Unit,
    onDealModeChange: (String) -> Unit,
    onPropertyTypeChange: (String) -> Unit,
    onTransactionStatusChange: (String) -> Unit,
    onAreaChange: (String) -> Unit,
    onRoomsChange: (String) -> Unit,
    onSalePriceChange: (String) -> Unit,
    onDepositChange: (String) -> Unit,
    onRentChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onFloorChange: (String) -> Unit = {},
    onBuildYearChange: (String) -> Unit = {},
    onAmenitiesChange: (String) -> Unit = {},
    onParkingChange: (Boolean) -> Unit = {},
    onStorageChange: (Boolean) -> Unit = {},
    onElevatorChange: (Boolean) -> Unit = {},
    onVacantChange: (Boolean) -> Unit = {},
    onOwnerNameChange: (String) -> Unit = {},
    onOwnerPhoneChange: (String) -> Unit = {},
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isRent = dealMode.contains("اجاره") || dealMode.contains("رهن")

    DfSheetScaffold(
        title = "ویرایش فایل شخصی",
        subtitle = "به‌روزرسانی اطلاعات ملک و وضعیت معامله",
        icon = DfIcons.Building,
        iconContainerColor = DfColors.BlueLight,
        iconTint = DfColors.Blue,
        onClose = onDismiss,
        scrollable = true,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ذخیره…" else "ذخیره تغییرات",
                onPrimary = onSubmit,
                primaryEnabled = !isSubmitting && title.isNotBlank(),
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "اطلاعات اصلی") {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("عنوان") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
        }

        DfSheetSection(title = "نوع معامله") {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                CrmConstants.PROPERTY_DEAL_MODES.forEach { mode ->
                    DfSheetOptionRow(
                        label = mode,
                        selected = mode == dealMode,
                        onClick = { if (!isSubmitting) onDealModeChange(mode) },
                        icon = if (mode.contains("فروش")) DfIcons.Home else DfIcons.Tag,
                    )
                }
            }
        }

        DfSheetSection(title = "نوع ملک") {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                CrmConstants.PROPERTY_TYPES.forEach { type ->
                    DfSheetOptionRow(
                        label = type,
                        selected = type == propertyType,
                        onClick = { if (!isSubmitting) onPropertyTypeChange(type) },
                        icon = PropertyFilters.propertyTypeIcon(type),
                    )
                }
            }
        }

        DfSheetSection(title = "وضعیت معامله") {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                CrmConstants.PROPERTY_TX_STATUSES.forEach { status ->
                    DfSheetOptionRow(
                        label = status,
                        selected = status == transactionStatus,
                        onClick = { if (!isSubmitting) onTransactionStatusChange(status) },
                        icon = PropertyFilters.txStatusIcon(status),
                    )
                }
            }
        }

        DfSheetSection(title = "موقعیت") {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                OutlinedTextField(
                    value = city,
                    onValueChange = onCityChange,
                    label = { Text("شهر") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
                OutlinedTextField(
                    value = district,
                    onValueChange = onDistrictChange,
                    label = { Text("محله") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
            }
            OutlinedTextField(
                value = neighborhood,
                onValueChange = onNeighborhoodChange,
                label = { Text("منطقه / محله دقیق") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = address,
                onValueChange = onAddressChange,
                label = { Text("آدرس") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                enabled = !isSubmitting,
            )
        }

        DfSheetSection(title = "مشخصات و قیمت") {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                OutlinedTextField(
                    value = area,
                    onValueChange = onAreaChange,
                    label = { Text("متراژ") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
                OutlinedTextField(
                    value = rooms,
                    onValueChange = onRoomsChange,
                    label = { Text("اتاق") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
            }
            if (isRent) {
                DfMoneyField(
                    value = deposit,
                    onValueChange = onDepositChange,
                    label = "رهن",
                    enabled = !isSubmitting,
                )
                DfMoneyField(
                    value = rent,
                    onValueChange = onRentChange,
                    label = "اجاره",
                    enabled = !isSubmitting,
                )
            } else {
                DfMoneyField(
                    value = salePrice,
                    onValueChange = onSalePriceChange,
                    label = "قیمت فروش",
                    enabled = !isSubmitting,
                )
            }
        }

        var showAdvanced by remember { mutableStateOf(false) }
        DfSheetAdvancedBlock(
            title = if (showAdvanced) "بستن جزئیات ملک" else "مشخصات تکمیلی ملک",
            expanded = showAdvanced,
            onToggle = { showAdvanced = !showAdvanced },
        ) {
            DfSheetSection(title = "ساختار، مالک و امکانات") {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    OutlinedTextField(
                        value = floor,
                        onValueChange = onFloorChange,
                        label = { Text("طبقه") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !isSubmitting,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = buildYear,
                        onValueChange = onBuildYearChange,
                        label = { Text("سال ساخت") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !isSubmitting,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                OutlinedTextField(
                    value = amenities,
                    onValueChange = onAmenitiesChange,
                    label = { Text("امکانات") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                )
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = onOwnerNameChange,
                    label = { Text("نام مالک") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
                OutlinedTextField(
                    value = ownerPhone,
                    onValueChange = { onOwnerPhoneChange(PhoneNormalizer.normalize(it)) },
                    label = { Text("شماره مالک") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
                AmenityToggleRow("پارکینگ", hasParking, !isSubmitting, onParkingChange)
                AmenityToggleRow("انباری", hasStorage, !isSubmitting, onStorageChange)
                AmenityToggleRow("آسانسور", hasElevator, !isSubmitting, onElevatorChange)
                AmenityToggleRow("تخلیه / خالی", isVacant, !isSubmitting, onVacantChange)
            }
        }

        DfSheetSection(title = "یادداشت") {
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("یادداشت داخلی") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !isSubmitting,
            )
        }
    }
}

@Composable
private fun AmenityToggleRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}
