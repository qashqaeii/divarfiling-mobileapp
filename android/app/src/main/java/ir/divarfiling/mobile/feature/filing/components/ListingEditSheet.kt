package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.network.ListingDetailDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingEditSheet(
    listing: ListingDetailDto,
    title: String,
    price: String,
    deposit: String,
    rent: String,
    area: String,
    rooms: String,
    floor: String,
    buildYear: String,
    neighborhood: String,
    city: String,
    description: String,
    ownerPhone: String,
    isSubmitting: Boolean,
    onTitleChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDepositChange: (String) -> Unit,
    onRentChange: (String) -> Unit,
    onAreaChange: (String) -> Unit,
    onRoomsChange: (String) -> Unit,
    onFloorChange: (String) -> Unit,
    onBuildYearChange: (String) -> Unit,
    onNeighborhoodChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onOwnerPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isRent = listing.rent != null || listing.deposit != null

    DfSheetScaffold(
        title = "ویرایش آگهی",
        subtitle = listing.title.orEmpty().ifBlank { "به‌روزرسانی اطلاعات آگهی" },
        icon = DfIcons.SlidersHorizontal,
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
        DfSheetSection(title = "اطلاعات اصلی") {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("عنوان") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = ownerPhone,
                onValueChange = onOwnerPhoneChange,
                label = { Text("شماره تماس مالک") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            if (isRent) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = deposit,
                        onValueChange = onDepositChange,
                        label = { Text("ودیعه") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = rent,
                        onValueChange = onRentChange,
                        label = { Text("اجاره") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
            } else {
                OutlinedTextField(
                    value = price,
                    onValueChange = onPriceChange,
                    label = { Text("قیمت (تومان)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = area,
                    onValueChange = onAreaChange,
                    label = { Text("متراژ") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = rooms,
                    onValueChange = onRoomsChange,
                    label = { Text("اتاق") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = floor,
                    onValueChange = onFloorChange,
                    label = { Text("طبقه") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = buildYear,
                    onValueChange = onBuildYearChange,
                    label = { Text("سال ساخت") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }
        }
        DfSheetSection(title = "موقعیت") {
            OutlinedTextField(
                value = neighborhood,
                onValueChange = onNeighborhoodChange,
                label = { Text("محله") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = city,
                onValueChange = onCityChange,
                label = { Text("شهر") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        DfSheetSection(title = "توضیحات") {
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("توضیحات آگهی") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
        }
    }
}
