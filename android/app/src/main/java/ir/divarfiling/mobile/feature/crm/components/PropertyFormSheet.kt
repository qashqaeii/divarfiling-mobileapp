package ir.divarfiling.mobile.feature.crm.components

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfFilterChipRow
import ir.divarfiling.mobile.core.design.components.DfFilterOption
import ir.divarfiling.mobile.core.design.components.DfJalaliDateField
import ir.divarfiling.mobile.core.design.components.DfDateTimePresets
import ir.divarfiling.mobile.core.design.components.DfMoneyField
import ir.divarfiling.mobile.core.design.components.DfOccupancyVacancyDateSection
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetOptionRow
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.util.PhoneNormalizer
import ir.divarfiling.mobile.feature.crm.FeatureWidget
import ir.divarfiling.mobile.feature.crm.PropertyAmenityCatalog
import ir.divarfiling.mobile.feature.crm.PropertyConstants
import ir.divarfiling.mobile.feature.crm.PropertyFeatureSchema
import ir.divarfiling.mobile.feature.crm.PropertyFormState
import ir.divarfiling.mobile.feature.filing.ListingFeatureChoices

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PropertyFormSheet(
    mode: PropertyFormMode,
    form: PropertyFormState,
    isSubmitting: Boolean,
    onFormChange: (PropertyFormState) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val profile = remember(form.dealMode, form.propertyType) {
        PropertyFeatureSchema.profileFor(form.dealMode, form.propertyType)
    }

    DfSheetScaffold(
        title = if (mode == PropertyFormMode.Create) "فایل شخصی جدید" else "ویرایش فایل شخصی",
        subtitle = "ثبت کامل اطلاعات ملک — هم‌راستا با فرم CRM وب",
        icon = DfIcons.Building,
        iconContainerColor = if (mode == PropertyFormMode.Create) DfColors.PurpleContainer else DfColors.BlueLight,
        iconTint = if (mode == PropertyFormMode.Create) DfColors.Purple else DfColors.Blue,
        bodyHeightFraction = 0.92f,
        scrollable = true,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = when {
                    isSubmitting && mode == PropertyFormMode.Create -> "در حال ثبت…"
                    isSubmitting -> "در حال ذخیره…"
                    mode == PropertyFormMode.Create -> "ثبت فایل"
                    else -> "ذخیره تغییرات"
                },
                onPrimary = onSubmit,
                primaryEnabled = !isSubmitting && form.title.isNotBlank(),
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "اطلاعات پایه") {
            OutlinedTextField(
                value = form.title,
                onValueChange = { onFormChange(form.copy(title = it)) },
                label = { Text("عنوان ملک") },
                placeholder = { Text("مثلاً آپارتمان ۱۲۰ متری ونک") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            Text("نوع ملک", style = AppTypography.labelSmall, color = DfColors.TextMuted)
            DfFilterChipRow(
                options = PropertyConstants.PROPERTY_TYPES.map { DfFilterOption(it, it) },
                selected = form.propertyType,
                onSelect = { onFormChange(form.copy(propertyType = it)) },
            )
            Text("نوع معامله", style = AppTypography.labelSmall, color = DfColors.TextMuted)
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                PropertyConstants.DEAL_MODES.forEach { modeOption ->
                    DfSheetOptionRow(
                        label = modeOption,
                        selected = modeOption == form.dealMode,
                        onClick = { if (!isSubmitting) onFormChange(form.copy(dealMode = modeOption)) },
                        icon = if (modeOption.contains("فروش")) DfIcons.Home else DfIcons.Tag,
                    )
                }
            }
            DfDropdown(
                label = "وضعیت معامله",
                value = form.transactionStatus,
                options = PropertyConstants.TX_STATUSES,
                enabled = !isSubmitting,
                onSelect = { onFormChange(form.copy(transactionStatus = it)) },
            )
            DfDropdown(
                label = "وضعیت انتشار",
                value = form.publishStatus,
                options = PropertyConstants.PUBLISH_STATUSES,
                enabled = !isSubmitting,
                onSelect = { onFormChange(form.copy(publishStatus = it)) },
            )
            DfDropdown(
                label = "نوع آگهی‌دهنده",
                value = form.businessType.ifBlank { "—" },
                options = listOf("—") + PropertyConstants.BUSINESS_TYPES,
                enabled = !isSubmitting,
                onSelect = { onFormChange(form.copy(businessType = if (it == "—") "" else it)) },
            )
            OutlinedTextField(
                value = form.ownerPhone,
                onValueChange = { onFormChange(form.copy(ownerPhone = PhoneNormalizer.normalize(it))) },
                label = { Text("شماره تماس مالک") },
                placeholder = { Text("اختیاری") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            OutlinedTextField(
                value = form.ownerName,
                onValueChange = { onFormChange(form.copy(ownerName = it)) },
                label = { Text("نام مالک") },
                placeholder = { Text("اختیاری") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = form.link,
                onValueChange = { onFormChange(form.copy(link = it)) },
                label = { Text("لینک آگهی") },
                placeholder = { Text("اختیاری — دیوار یا سایت") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            if (form.token.isNotBlank()) {
                OutlinedTextField(
                    value = form.token,
                    onValueChange = {},
                    label = { Text("شناسه آگهی") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = false,
                )
            }
        }

        DfSheetSection(title = "موقعیت") {
            OutlinedTextField(
                value = form.address,
                onValueChange = { onFormChange(form.copy(address = it)) },
                label = { Text("آدرس") },
                placeholder = { Text("اختیاری") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                enabled = !isSubmitting,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                OutlinedTextField(
                    value = form.city,
                    onValueChange = { onFormChange(form.copy(city = it)) },
                    label = { Text("شهر") },
                    placeholder = { Text("اختیاری") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
                OutlinedTextField(
                    value = form.district,
                    onValueChange = { onFormChange(form.copy(district = it)) },
                    label = { Text("منطقه") },
                    placeholder = { Text("اختیاری") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
            }
            OutlinedTextField(
                value = form.neighborhood,
                onValueChange = { onFormChange(form.copy(neighborhood = it)) },
                label = { Text("محله") },
                placeholder = { Text("اختیاری") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                OutlinedTextField(
                    value = form.latitude,
                    onValueChange = { onFormChange(form.copy(latitude = it)) },
                    label = { Text("عرض جغرافیایی") },
                    placeholder = { Text("اختیاری") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = form.longitude,
                    onValueChange = { onFormChange(form.copy(longitude = it)) },
                    label = { Text("طول جغرافیایی") },
                    placeholder = { Text("اختیاری") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
        }

        DfSheetSection(title = "مشخصات فنی") {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                OutlinedTextField(
                    value = form.area,
                    onValueChange = { onFormChange(form.copy(area = it)) },
                    label = { Text("متراژ (متر)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = form.rooms,
                    onValueChange = { onFormChange(form.copy(rooms = it)) },
                    label = { Text("تعداد اتاق") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                OutlinedTextField(
                    value = form.floor,
                    onValueChange = { onFormChange(form.copy(floor = it)) },
                    label = { Text("طبقه") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = form.totalFloors,
                    onValueChange = { onFormChange(form.copy(totalFloors = it)) },
                    label = { Text("تعداد کل طبقات") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            OutlinedTextField(
                value = form.buildYear,
                onValueChange = { onFormChange(form.copy(buildYear = it)) },
                label = { Text("سال ساخت") },
                placeholder = { Text("اختیاری") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }

        DfSheetSection(title = "امکانات سریع") {
            Text(
                text = "امکانات پرکاربرد — بدون تکرار موارد مدیریت‌شده در بخش ویژگی‌ها",
                style = MaterialTheme.typography.bodySmall,
                color = DfColors.TextMuted,
            )
            PropertyAmenityCatalog.checkboxGroups().forEach { group ->
                Text(group.title, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    group.labels.forEach { label ->
                        val selected = label in form.amenityLabels
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (isSubmitting) return@FilterChip
                                val next = form.amenityLabels.toMutableSet()
                                if (selected) next.remove(label) else next.add(label)
                                onFormChange(form.copy(amenityLabels = next))
                            },
                            label = { Text(label, style = AppTypography.labelSmall) },
                            enabled = !isSubmitting,
                        )
                    }
                }
            }
            OutlinedTextField(
                value = form.amenitiesExtra,
                onValueChange = { onFormChange(form.copy(amenitiesExtra = it)) },
                label = { Text("سایر امکانات / جزئیات") },
                placeholder = { Text("اختیاری") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                enabled = !isSubmitting,
            )
        }

        DfSheetSection(title = profile.title) {
            Text(
                text = profile.hint,
                style = MaterialTheme.typography.bodySmall,
                color = DfColors.TextMuted,
            )
            profile.groups.forEach { group ->
                Text(
                    text = group.title,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
                group.keys.forEach { key ->
                    val field = PropertyFeatureSchema.fieldDef(key)
                    val current = form.features[key].orEmpty()
                    PropertyFeatureFieldEditor(
                        field = field,
                        value = current,
                        enabled = !isSubmitting,
                        onValueChange = { value ->
                            val next = form.features.toMutableMap()
                            if (value.isBlank()) next.remove(key) else next[key] = value
                            onFormChange(form.copy(features = next))
                        },
                    )
                }
            }
        }

        DfSheetSection(title = if (form.isRentDeal) "رهن و اجاره" else "قیمت فروش") {
            if (form.isRentDeal) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    DfMoneyField(
                        value = form.deposit,
                        onValueChange = { onFormChange(form.copy(deposit = it)) },
                        label = "ودیعه / رهن",
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting,
                    )
                    DfMoneyField(
                        value = form.rent,
                        onValueChange = { onFormChange(form.copy(rent = it)) },
                        label = "اجاره ماهانه",
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting,
                    )
                }
            } else {
                DfMoneyField(
                    value = form.salePrice,
                    onValueChange = { onFormChange(form.copy(salePrice = it)) },
                    label = "قیمت فروش",
                    enabled = !isSubmitting,
                )
            }
        }

        DfSheetSection(title = "وضعیت سکونت") {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                PropertyChoiceChip(
                    label = "تخلیه است",
                    selected = form.isVacant,
                    onClick = {
                        onFormChange(form.copy(isVacant = true, tenantName = "", tenantPhone = ""))
                    },
                    modifier = Modifier.weight(1f),
                )
                PropertyChoiceChip(
                    label = "تخلیه نیست",
                    selected = !form.isVacant,
                    onClick = { onFormChange(form.copy(isVacant = false)) },
                    modifier = Modifier.weight(1f),
                )
            }
            if (!form.isVacant) {
                OutlinedTextField(
                    value = form.tenantName,
                    onValueChange = { onFormChange(form.copy(tenantName = it)) },
                    label = { Text("نام مستاجر فعلی") },
                    placeholder = { Text("اختیاری") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
                OutlinedTextField(
                    value = form.tenantPhone,
                    onValueChange = { onFormChange(form.copy(tenantPhone = PhoneNormalizer.normalize(it))) },
                    label = { Text("شماره تماس مستاجر") },
                    placeholder = { Text("اختیاری") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
            }
            DfOccupancyVacancyDateSection(
                vacancyDate = form.vacancyDate,
                enabled = !isSubmitting,
                onVacancyDateChange = { onFormChange(form.copy(vacancyDate = it)) },
            )
            OutlinedTextField(
                value = form.notes,
                onValueChange = { onFormChange(form.copy(notes = it)) },
                label = { Text("یادداشت داخلی") },
                placeholder = { Text("اختیاری") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !isSubmitting,
            )
        }

        DfSheetSection(title = "تصاویر") {
            Text(
                text = "اولین تصویر به‌عنوان کاور نمایش داده می‌شود. لینک تصاویر را اضافه، حذف یا مرتب کنید.",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
            form.images.forEachIndexed { index, url ->
                PropertyImageUrlRow(
                    index = index,
                    url = url,
                    isCover = index == 0,
                    canMoveUp = index > 0,
                    canMoveDown = index < form.images.lastIndex,
                    enabled = !isSubmitting,
                    onUrlChange = { updated ->
                        val next = form.images.toMutableList().also { it[index] = updated }
                        onFormChange(form.copy(images = next))
                    },
                    onMoveUp = {
                        if (index > 0) {
                            val next = form.images.toMutableList()
                            val item = next.removeAt(index)
                            next.add(index - 1, item)
                            onFormChange(form.copy(images = next))
                        }
                    },
                    onMoveDown = {
                        if (index < form.images.lastIndex) {
                            val next = form.images.toMutableList()
                            val item = next.removeAt(index)
                            next.add(index + 1, item)
                            onFormChange(form.copy(images = next))
                        }
                    },
                    onRemove = {
                        onFormChange(form.copy(images = form.images.toMutableList().also { it.removeAt(index) }))
                    },
                )
            }
            DfSheetOptionRow(
                label = "افزودن لینک تصویر",
                selected = false,
                onClick = { if (!isSubmitting) onFormChange(form.copy(images = form.images + "")) },
                icon = DfIcons.Plus,
            )
        }
    }
}

enum class PropertyFormMode { Create, Edit }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PropertyFeatureFieldEditor(
    field: ir.divarfiling.mobile.feature.crm.FeatureFieldDef,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
) {
    when (field.widget) {
        FeatureWidget.Toggle -> PropertyAmenityTriRow(
            label = field.label,
            icon = DfIcons.Sparkles,
            value = value.toTriBool(),
            enabled = enabled,
            onChange = { onValueChange(it.toTriPayload()) },
        )
        FeatureWidget.Chips, FeatureWidget.Select -> {
            val choices = ListingFeatureChoices.choicesFor(field.key, field.choices)
            if (choices.isNotEmpty()) {
                Text(field.label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    choices.forEach { choice ->
                        FilterChip(
                            selected = value == choice,
                            onClick = { if (enabled) onValueChange(choice) },
                            label = { Text(choice, style = AppTypography.labelSmall) },
                            enabled = enabled,
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(field.label) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = enabled,
                )
            }
        }
        FeatureWidget.Date -> DfJalaliDateField(
            value = value,
            onValueChange = onValueChange,
            label = field.label,
            placeholder = "اختیاری — تقویم شمسی",
            enabled = enabled,
            presets = DfDateTimePresets.leaseDeadlineShortcuts(),
        )
        FeatureWidget.Number -> OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(field.label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropertyChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = DfColors.Purple,
    selectedBackground: Color = DfColors.PurpleContainer,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.Chip,
        color = if (selected) selectedBackground else DfThemeColors.surfaceVariant(),
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            style = AppTypography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) selectedColor else DfThemeColors.textSecondary(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PropertyAmenityTriRow(
    label: String,
    icon: ImageVector,
    value: Boolean?,
    enabled: Boolean,
    onChange: (Boolean?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = DfColors.Purple, modifier = Modifier.size(16.dp))
            Text(label, style = AppTypography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            PropertyChoiceChip("دارد", value == true, { onChange(true) }, Modifier.weight(1f), DfColors.Green, DfColors.GreenLight)
            PropertyChoiceChip("ندارد", value == false, { onChange(false) }, Modifier.weight(1f), DfColors.Rose, DfColors.RoseLight)
            PropertyChoiceChip("نامشخص", value == null, { onChange(null) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PropertyImageUrlRow(
    index: Int,
    url: String,
    isCover: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    enabled: Boolean,
    onUrlChange: (String) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    OutlinedTextField(
        value = url,
        onValueChange = onUrlChange,
        label = { Text(if (isCover) "کاور" else "تصویر ${index + 1}") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        trailingIcon = {
            Row {
                IconButton(onClick = onMoveUp, enabled = enabled && canMoveUp, modifier = Modifier.size(32.dp)) {
                    Icon(DfIcons.ChevronUp, contentDescription = "بالا", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onMoveDown, enabled = enabled && canMoveDown, modifier = Modifier.size(32.dp)) {
                    Icon(DfIcons.ChevronDown, contentDescription = "پایین", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onRemove, enabled = enabled, modifier = Modifier.size(32.dp)) {
                    Icon(DfIcons.X, contentDescription = "حذف", tint = DfColors.Rose, modifier = Modifier.size(16.dp))
                }
            }
        },
    )
}

private fun String.toTriBool(): Boolean? = when (trim()) {
    "دارد", "true", "1" -> true
    "ندارد", "false", "0" -> false
    else -> null
}

private fun Boolean?.toTriPayload(): String = when (this) {
    true -> "دارد"
    false -> "ندارد"
    null -> ""
}
