package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        query = query,
        onQueryChange = onQueryChange,
        onSearch = {},
        searchPlaceholder = "نام مخاطب، تلفن یا عنوان کار…",
        filters = {
            TodayFilterTabsRow(
                chips = chips,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
            )
        },
    )
}
