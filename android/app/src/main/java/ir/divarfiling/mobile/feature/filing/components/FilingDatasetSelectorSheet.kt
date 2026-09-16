package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.network.DatasetDto
import ir.divarfiling.mobile.feature.filing.map.FilingDatasetSelectionLabels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilingDatasetSelectorSheet(
    visible: Boolean,
    datasets: List<DatasetDto>,
    searchQuery: String,
    draftSelectedIds: Set<String>,
    onSearchChange: (String) -> Unit,
    onToggleAll: () -> Unit,
    onToggleDataset: (String) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    val filtered = datasets.filter { ds ->
        val q = searchQuery.trim()
        q.isBlank() || ds.name.contains(q, ignoreCase = true) ||
            ds.city.contains(q, ignoreCase = true)
    }
    val allSelected = draftSelectedIds.isEmpty()
    val countLabel = FilingDatasetSelectionLabels.sheetSelectedCount(
        draftSelectedIds.size,
        DateUtils::toPersianDigits,
    )

    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "انتخاب فایل‌ها",
            subtitle = countLabel,
            onClose = onDismiss,
            scrollable = false,
            bodyHeightFraction = 0.72f,
            footer = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    DfSecondaryButton(
                        text = "پاک کردن",
                        onClick = onClear,
                        modifier = Modifier.weight(1f),
                    )
                    DfPrimaryButton(
                        text = "اعمال",
                        onClick = onApply,
                        modifier = Modifier.weight(1f),
                    )
                }
            },
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("جستجوی فایل") },
                singleLine = true,
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
            ) {
                item {
                    DatasetRow(
                        title = "همه فایل‌ها",
                        subtitle = "جستجو در تمام فایل‌های فعال",
                        checked = allSelected,
                        onClick = onToggleAll,
                    )
                    HorizontalDivider()
                }
                items(filtered, key = { it.id }) { dataset ->
                    val checked = draftSelectedIds.contains(dataset.id)
                    DatasetRow(
                        title = dataset.name,
                        subtitle = listOfNotNull(
                            dataset.city.takeIf { it.isNotBlank() },
                            dataset.itemCount.takeIf { it > 0 }?.let { count ->
                                "${DateUtils.toPersianDigits(count.toString())} آگهی"
                            },
                        ).joinToString(" · "),
                        checked = checked,
                        onClick = { onToggleDataset(dataset.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DatasetRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Checkbox(checked = checked, onCheckedChange = { onClick() })
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotBlank()) {
                Text(text = subtitle, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
            }
        }
    }
}
