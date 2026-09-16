package ir.divarfiling.mobile.feature.contracts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractFiltersBottomSheet(
    draft: ContractSheetFilters,
    onDraftChange: (ContractSheetFilters) -> Unit,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md)
                .padding(bottom = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text("فیلتر قراردادها")
            Text("نوع قرارداد", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    null to "همه",
                    "lease" to "رهن و اجاره",
                    "sale" to "فروش",
                    "jv" to "مشارکت",
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = draft.contractType == value,
                        onClick = { onDraftChange(draft.copy(contractType = value)) },
                        label = { Text(label) },
                    )
                }
            }
            Text("ارتباط", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    null to "همه",
                    "true" to "متصل به معامله",
                    "false" to "مستقل",
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = draft.dealLinked == value,
                        onClick = { onDraftChange(draft.copy(dealLinked = value)) },
                        label = { Text(label) },
                    )
                }
            }
            Text("بازه تاریخ (میلادی YYYY-MM-DD)", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = draft.dateFrom,
                onValueChange = { onDraftChange(draft.copy(dateFrom = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("از") },
                singleLine = true,
            )
            OutlinedTextField(
                value = draft.dateTo,
                onValueChange = { onDraftChange(draft.copy(dateTo = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("تا") },
                singleLine = true,
            )
            Text("مرتب‌سازی", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
            ContractSortOption.entries.forEach { opt ->
                FilterChip(
                    selected = draft.sort == opt.apiValue,
                    onClick = { onDraftChange(draft.copy(sort = opt.apiValue)) },
                    label = { Text(opt.label) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onClear) { Text("پاک کردن") }
                Button(onClick = onApply) { Text("نمایش نتایج") }
            }
        }
    }
}