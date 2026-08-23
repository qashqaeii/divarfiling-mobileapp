package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import ir.divarfiling.mobile.core.design.components.DfMoneyField
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetOptionRow
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.core.network.ListingFeatureFieldDto
import ir.divarfiling.mobile.core.util.PhoneNormalizer
import ir.divarfiling.mobile.feature.filing.ListingEditForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingEditSheet(
    listing: ListingDetailDto,
    form: ListingEditForm,
    isSubmitting: Boolean,
    onFormChange: (ListingEditForm) -> Unit,
    onCallOwner: (() -> Unit)?,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isRent = listing.rent != null || listing.deposit != null || form.deposit.isNotBlank() || form.rent.isNotBlank()
    val groupedFeatures = remember(listing.featureFields) {
        listing.featureFields.groupBy { it.groupTitle.ifBlank { "سایر جزئیات" } }
    }

    DfSheetScaffold(
        title = "ویرایش آگهی",
        subtitle = listing.title.orEmpty().ifBlank { "به‌روزرسانی اطلاعات آگهی" },
        icon = DfIcons.Pencil,
        onClose = onDismiss,
        bodyHeightFraction = 0.78f,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ذخیره…" else "ذخیره تغییرات",
                onPrimary = onSave,
                primaryEnabled = !isSubmitting && form.title.isNotBlank(),
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        if (form.ownerPhone.isNotBlank() && onCallOwner != null) {
            ListingOwnerCallBanner(
                phone = form.ownerPhone,
                onCall = onCallOwner,
                modifier = Modifier.padding(bottom = AppSpacing.sm),
            )
        }

        DfSheetSection(title = "اطلاعات اصلی") {
            OutlinedTextField(
                value = form.title,
                onValueChange = { onFormChange(form.copy(title = it)) },
                label = { Text("عنوان آگهی") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            Text("آگهی‌دهنده", style = AppTypography.labelSmall, color = DfColors.TextMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                listOf("شخصی", "مشاور").forEach { type ->
                    ChoiceChip(
                        label = type,
                        selected = form.businessType == type,
                        onClick = { onFormChange(form.copy(businessType = type)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Text("وضعیت آگهی", style = AppTypography.labelSmall, color = DfColors.TextMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                ChoiceChip(
                    label = "فعال",
                    selected = !form.isExpired,
                    onClick = { onFormChange(form.copy(isExpired = false)) },
                    modifier = Modifier.weight(1f),
                )
                ChoiceChip(
                    label = "منقضی",
                    selected = form.isExpired,
                    onClick = { onFormChange(form.copy(isExpired = true)) },
                    modifier = Modifier.weight(1f),
                    selectedColor = DfColors.Rose,
                    selectedBackground = DfColors.RoseLight,
                )
            }
            OutlinedTextField(
                value = form.ownerName,
                onValueChange = { onFormChange(form.copy(ownerName = it)) },
                label = { Text("نام مالک") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = form.ownerPhone,
                onValueChange = { onFormChange(form.copy(ownerPhone = PhoneNormalizer.normalize(it))) },
                label = { Text("شماره تماس مالک") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            OutlinedTextField(
                value = form.link,
                onValueChange = { onFormChange(form.copy(link = it)) },
                label = { Text("لینک دیوار") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
        }

        DfSheetSection(title = "اطلاعات مالی") {
            if (isRent) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    value = form.price,
                    onValueChange = { onFormChange(form.copy(price = it)) },
                    label = "قیمت فروش",
                    enabled = !isSubmitting,
                )
            }
            Text("وضعیت سکونت", style = AppTypography.labelSmall, color = DfColors.TextMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                ChoiceChip(
                    label = "تخلیه است",
                    selected = form.isVacant,
                    onClick = { onFormChange(form.copy(isVacant = true, tenantName = "", tenantPhone = "")) },
                    modifier = Modifier.weight(1f),
                )
                ChoiceChip(
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
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
                OutlinedTextField(
                    value = form.tenantPhone,
                    onValueChange = { onFormChange(form.copy(tenantPhone = PhoneNormalizer.normalize(it))) },
                    label = { Text("شماره تماس مستاجر") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
            }
        }

        DfSheetSection(title = "مشخصات ملک") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = form.floor,
                    onValueChange = { onFormChange(form.copy(floor = it)) },
                    label = { Text("طبقه") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
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
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }

        DfSheetSection(title = "امکانات اصلی") {
            AmenityTriRow("آسانسور", DfIcons.Layers, form.elevator) {
                onFormChange(form.copy(elevator = it))
            }
            AmenityTriRow("پارکینگ", DfIcons.Car, form.parking) {
                onFormChange(form.copy(parking = it))
            }
            AmenityTriRow("انباری", DfIcons.Inbox, form.storage) {
                onFormChange(form.copy(storage = it))
            }
            AmenityTriRow("بالکن", DfIcons.Home, form.balcony) {
                onFormChange(form.copy(balcony = it))
            }
            AmenityTriRow("لابی", DfIcons.Building, form.lobby) {
                onFormChange(form.copy(lobby = it))
            }
            AmenityTriRow("استخر", DfIcons.Bath, form.pool) {
                onFormChange(form.copy(pool = it))
            }
            AmenityTriRow("روف گاردن", DfIcons.Sparkles, form.roofGarden) {
                onFormChange(form.copy(roofGarden = it))
            }
            AmenityTriRow("جکوزی", DfIcons.Bath, form.jacuzzi) {
                onFormChange(form.copy(jacuzzi = it))
            }
            AmenityTriRow("سالن ورزش", DfIcons.Trophy, form.gym) {
                onFormChange(form.copy(gym = it))
            }
        }

        if (groupedFeatures.isNotEmpty()) {
            groupedFeatures.forEach { (groupTitle, fields) ->
                DfSheetSection(title = groupTitle) {
                    fields.forEach { field ->
                        val current = form.features[field.key] ?: field.value
                        if (field.input == "tri") {
                            AmenityTriRow(field.label.ifBlank { field.key }, featureFieldIcon(field), current.toTriBool()) {
                                onFormChange(form.copy(features = form.features + (field.key to it.toTriPayload())))
                            }
                        } else {
                            OutlinedTextField(
                                value = current,
                                onValueChange = { onFormChange(form.copy(features = form.features + (field.key to it))) },
                                label = { Text(field.label.ifBlank { field.key }) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !isSubmitting,
                            )
                        }
                    }
                }
            }
        }

        DfSheetSection(title = "موقعیت") {
            OutlinedTextField(
                value = form.address,
                onValueChange = { onFormChange(form.copy(address = it)) },
                label = { Text("آدرس کامل") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                enabled = !isSubmitting,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = form.city,
                    onValueChange = { onFormChange(form.copy(city = it)) },
                    label = { Text("شهر") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
                OutlinedTextField(
                    value = form.region,
                    onValueChange = { onFormChange(form.copy(region = it)) },
                    label = { Text("منطقه") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
            }
            OutlinedTextField(
                value = form.neighborhood,
                onValueChange = { onFormChange(form.copy(neighborhood = it)) },
                label = { Text("محله") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = form.latitude,
                    onValueChange = { onFormChange(form.copy(latitude = it)) },
                    label = { Text("عرض جغرافیایی") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = form.longitude,
                    onValueChange = { onFormChange(form.copy(longitude = it)) },
                    label = { Text("طول جغرافیایی") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
        }

        DfSheetSection(title = "تصاویر") {
            Text(
                text = "اولین تصویر کاور است. لینک تصاویر دیوار را اضافه، حذف یا جابه‌جا کنید.",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
            form.images.forEachIndexed { index, url ->
                ImageUrlRow(
                    index = index,
                    url = url,
                    isCover = index == 0,
                    canMoveUp = index > 0,
                    canMoveDown = index < form.images.lastIndex,
                    onUrlChange = { updated ->
                        onFormChange(form.copy(images = form.images.toMutableList().also { it[index] = updated }))
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
                onClick = { onFormChange(form.copy(images = form.images + "")) },
                icon = DfIcons.Plus,
            )
        }

        DfSheetSection(title = "توضیحات") {
            OutlinedTextField(
                value = form.description,
                onValueChange = { onFormChange(form.copy(description = it)) },
                label = { Text("توضیحات آگهی") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                enabled = !isSubmitting,
            )
        }
    }
}

@Composable
private fun AmenityTriRow(
    label: String,
    icon: ImageVector,
    value: Boolean?,
    onChange: (Boolean?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = DfColors.Purple, modifier = Modifier.size(16.dp))
            Text(text = label, style = AppTypography.labelSmall, fontWeight = FontWeight.SemiBold, color = DfColors.TextPrimary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            ChoiceChip(
                label = "دارد",
                selected = value == true,
                onClick = { onChange(true) },
                modifier = Modifier.weight(1f),
                selectedColor = DfColors.Green,
                selectedBackground = DfColors.GreenLight,
            )
            ChoiceChip(
                label = "ندارد",
                selected = value == false,
                onClick = { onChange(false) },
                modifier = Modifier.weight(1f),
                selectedColor = DfColors.Rose,
                selectedBackground = DfColors.RoseLight,
            )
            ChoiceChip(
                label = "نامشخص",
                selected = value == null,
                onClick = { onChange(null) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceChip(
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
        color = if (selected) selectedBackground else DfColors.SurfaceVariant,
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            style = AppTypography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) selectedColor else DfColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ImageUrlRow(
    index: Int,
    url: String,
    isCover: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onUrlChange: (String) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            label = { Text(if (isCover) "کاور" else "تصویر ${index + 1}") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                Row {
                    IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(32.dp)) {
                        Icon(DfIcons.ChevronUp, contentDescription = "بالا", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(32.dp)) {
                        Icon(DfIcons.ChevronDown, contentDescription = "پایین", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(DfIcons.X, contentDescription = "حذف", tint = DfColors.Rose, modifier = Modifier.size(16.dp))
                    }
                }
            },
        )
    }
}

private fun featureFieldIcon(field: ListingFeatureFieldDto): ImageVector {
    val key = field.key + field.groupTitle
    return when {
        "ساختمان" in key || "جهت" in key -> DfIcons.Building
        "کف" in key || "کابینت" in key || "آشپزخانه" in key -> DfIcons.LayoutGrid
        "سرمایش" in key -> DfIcons.Cloud
        "گرمایش" in key || "آب گرم" in key -> DfIcons.Zap
        "سند" in key || "مجوز" in key -> DfIcons.File
        "تعداد" in key -> DfIcons.List
        "سونا" in key || "جکوزی" in key -> DfIcons.Bath
        "نما" in key || "عرض" in key -> DfIcons.Ruler
        else -> DfIcons.CircleAlert
    }
}

private fun String.toTriBool(): Boolean? = when (trim()) {
    "دارد", "1", "true", "yes" -> true
    "ندارد", "0", "false", "no" -> false
    else -> null
}

private fun Boolean?.toTriPayload(): String = when (this) {
    true -> "دارد"
    false -> "ندارد"
    null -> ""
}
