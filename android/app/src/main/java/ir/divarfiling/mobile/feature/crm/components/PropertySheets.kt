package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetOptionRow
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
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

    DfSheetScaffold(
        title = "فایل شخصی جدید",
        subtitle = "ثبت ملک در پرونده شخصی با جزئیات کامل",
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

        DfSheetSection(title = "قیمت‌گذاری") {
            if (isRent) {
                OutlinedTextField(
                    value = deposit,
                    onValueChange = onDepositChange,
                    label = { Text("رهن (تومان)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("مبلغ رهن") },
                    enabled = !isSubmitting,
                )
                OutlinedTextField(
                    value = rent,
                    onValueChange = onRentChange,
                    label = { Text("اجاره ماهانه (تومان)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("مبلغ اجاره") },
                    enabled = !isSubmitting,
                )
            } else {
                OutlinedTextField(
                    value = salePrice,
                    onValueChange = onSalePriceChange,
                    label = { Text("قیمت فروش (تومان)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("مبلغ فروش") },
                    enabled = !isSubmitting,
                )
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
                OutlinedTextField(
                    value = deposit,
                    onValueChange = onDepositChange,
                    label = { Text("رهن (تومان)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
                OutlinedTextField(
                    value = rent,
                    onValueChange = onRentChange,
                    label = { Text("اجاره (تومان)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
            } else {
                OutlinedTextField(
                    value = salePrice,
                    onValueChange = onSalePriceChange,
                    label = { Text("قیمت فروش (تومان)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
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
