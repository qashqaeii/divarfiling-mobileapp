package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import java.text.NumberFormat
import java.util.Locale

data class TodayFilterChip(
    val tab: TodayFilterTab,
    val label: String,
    val count: Int,
    val icon: ImageVector,
)

@Composable
fun TodayFilterTabsRow(
    chips: List<TodayFilterChip>,
    selectedTab: TodayFilterTab,
    onTabSelected: (TodayFilterTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("fa", "IR"))
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        chips.forEach { chip ->
            val selected = chip.tab == selectedTab
            TodayFilterChipItem(
                label = "${chip.label} (${numberFormat.format(chip.count)})",
                icon = chip.icon,
                selected = selected,
                onClick = { onTabSelected(chip.tab) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayFilterChipItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = AppShapes.ButtonPill,
        color = if (selected) DfColors.Purple else DfColors.Surface,
        shadowElevation = if (selected) 0.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else DfColors.TextMuted,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = label,
                style = AppTypography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else DfColors.TextSecondary,
                maxLines = 1,
            )
        }
    }
}
