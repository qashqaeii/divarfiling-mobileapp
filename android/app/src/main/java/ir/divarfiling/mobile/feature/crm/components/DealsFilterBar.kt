package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.components.DfFilterDropdown
import ir.divarfiling.mobile.core.design.components.DfFilterDropdownRow
import ir.divarfiling.mobile.core.design.components.DfSearchFilterPanel

@Composable
fun DealsSearchFilterPanel(
    owners: List<String>,
    selectedOwner: String,
    selectedSort: String,
    onOwnerChange: (String) -> Unit,
    onSortChange: (String) -> Unit,
    onResetFilters: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfSearchFilterPanel(
        modifier = modifier,
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        searchPlaceholder = "جستجو در معاملات...",
        filters = {
            DfFilterDropdownRow {
                DfFilterDropdown(
                    label = DealsFilters.ALL_FILTERS,
                    options = listOf(DealsFilters.ALL_FILTERS),
                    onSelect = { if (it == DealsFilters.ALL_FILTERS) onResetFilters() },
                )
                DfFilterDropdown(
                    label = selectedSort,
                    options = listOf(DealsFilters.NEWEST, DealsFilters.OLDEST),
                    onSelect = onSortChange,
                )
                DfFilterDropdown(
                    label = selectedOwner,
                    options = owners,
                    onSelect = onOwnerChange,
                )
            }
        },
    )
}
