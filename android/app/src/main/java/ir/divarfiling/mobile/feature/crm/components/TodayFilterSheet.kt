package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfSheetOptionRow
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TodayFilterSheet(
    chips: List<TodayFilterChip>,
    selectedTab: TodayFilterTab,
    onSelect: (TodayFilterTab) -> Unit,
    onDismiss: () -> Unit,
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("fa", "IR"))

    DfSheetScaffold(
        title = "فیلتر کارها",
        subtitle = "نمایش کارها بر اساس وضعیت",
        icon = DfIcons.Filter,
        onClose = onDismiss,
        scrollable = true,
    ) {
        chips.forEach { chip ->
            DfSheetOptionRow(
                label = chip.label,
                selected = chip.tab == selectedTab,
                icon = chip.icon,
                trailing = numberFormat.format(chip.count),
                onClick = {
                    onSelect(chip.tab)
                    onDismiss()
                },
            )
        }
        if (selectedTab != TodayFilterTab.All) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onSelect(TodayFilterTab.All)
                        onDismiss()
                    }
                    .padding(vertical = AppSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = DfIcons.X,
                    contentDescription = null,
                    tint = DfColors.TextMuted,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "حذف فیلتر",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}
