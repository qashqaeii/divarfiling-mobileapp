package ir.divarfiling.mobile.feature.filing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "فیلتر آگهی‌ها",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
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
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        priceMinText = ""
                        priceMaxText = ""
                        areaMinText = ""
                        areaMaxText = ""
                        roomsText = ""
                        onClear()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("پاک کردن")
                }
                Button(
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
                ) {
                    Text("اعمال فیلتر")
                }
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
