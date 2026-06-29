package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfSearchFilterPanel

@Composable
fun TodaySearchFilterPanel(
    query: String,
    onQueryChange: (String) -> Unit,
    chips: List<TodayFilterChip>,
    selectedTab: TodayFilterTab,
    onTabSelected: (TodayFilterTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    DfSearchFilterPanel(
        modifier = modifier,
        title = "جستجو",
        query = query,
        onQueryChange = onQueryChange,
        onSearch = {},
        searchPlaceholder = "نام مخاطب، تلفن یا عنوان کار…",
        filters = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                HorizontalDivider(color = DfColors.Outline.copy(alpha = 0.2f))
                Text(
                    text = "فیلتر وضعیت",
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DfColors.TextMuted,
                    modifier = Modifier.padding(top = 2.dp),
                )
                TodayFilterTabsRow(
                    chips = chips,
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                    modifier = Modifier.padding(horizontal = 0.dp),
                )
            }
        },
    )
}
