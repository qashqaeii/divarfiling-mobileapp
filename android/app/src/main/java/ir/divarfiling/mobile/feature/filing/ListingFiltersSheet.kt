package ir.divarfiling.mobile.feature.filing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingFiltersSheet(
    visible: Boolean,
    priceMin: Long?,
    priceMax: Long?,
    areaMin: Int?,
    areaMax: Int?,
    rooms: Int?,
    onDismiss: () -> Unit,
    onApply: (Long?, Long?, Int?, Int?, Int?) -> Unit,
    onClear: () -> Unit,
) {
    if (!visible) return

    var priceMinText by remember(visible, priceMin) { mutableStateOf(priceMin?.toString().orEmpty()) }
    var priceMaxText by remember(visible, priceMax) { mutableStateOf(priceMax?.toString().orEmpty()) }
    var areaMinText by remember(visible, areaMin) { mutableStateOf(areaMin?.toString().orEmpty()) }
    var areaMaxText by remember(visible, areaMax) { mutableStateOf(areaMax?.toString().orEmpty()) }
    var roomsText by remember(visible, rooms) { mutableStateOf(rooms?.toString().orEmpty()) }

    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "فیلتر آگهی‌ها",
            subtitle = "قیمت، متراژ و تعداد اتاق را محدود کنید",
            icon = DfIcons.Filter,
            onClose = onDismiss,
            footer = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    DfGlassTextButton(
                        text = "پاک کردن",
                        onClick = {
                            priceMinText = ""
                            priceMaxText = ""
                            areaMinText = ""
                            areaMaxText = ""
                            roomsText = ""
                            onClear()
                        },
                        modifier = Modifier.weight(1f),
                    )
                    DfPrimaryButton(
                        text = "اعمال فیلتر",
                        onClick = {
                            onApply(
                                priceMinText.toLongOrNull(),
                                priceMaxText.toLongOrNull(),
                                areaMinText.toIntOrNull(),
                                areaMaxText.toIntOrNull(),
                                roomsText.toIntOrNull(),
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            },
        ) {
            DfSheetSection(title = "محدوده قیمت") {
                OutlinedTextField(
                    value = priceMinText,
                    onValueChange = { priceMinText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("حداقل قیمت (تومان)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = priceMaxText,
                    onValueChange = { priceMaxText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("حداکثر قیمت (تومان)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            }
            DfSheetSection(title = "مشخصات ملک") {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    OutlinedTextField(
                        value = areaMinText,
                        onValueChange = { areaMinText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("متراژ از") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = areaMaxText,
                        onValueChange = { areaMaxText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("متراژ تا") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
                OutlinedTextField(
                    value = roomsText,
                    onValueChange = { roomsText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("تعداد اتاق") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            }
        }
    }
}

fun activeListingFilterCount(
    priceMin: Long?,
    priceMax: Long?,
    areaMin: Int?,
    areaMax: Int?,
    rooms: Int?,
): Int = listOf(priceMin, priceMax, areaMin, areaMax, rooms).count { it != null }
