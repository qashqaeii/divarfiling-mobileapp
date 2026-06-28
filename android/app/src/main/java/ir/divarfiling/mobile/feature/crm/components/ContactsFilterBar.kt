package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.components.DfFilterDropdown
import ir.divarfiling.mobile.core.design.components.DfFilterDropdownRow
import ir.divarfiling.mobile.core.design.components.DfSearchFilterPanel

@Composable
fun ContactsSearchFilterPanel(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    priorities: List<String>,
    statuses: List<String>,
    types: List<String>,
    selectedPriority: String,
    selectedStatus: String,
    selectedType: String,
    onPriorityChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    DfSearchFilterPanel(
        modifier = modifier,
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        searchPlaceholder = "جستجو نام، تلفن، شرکت...",
        filters = {
            DfFilterDropdownRow {
                DfFilterDropdown(
                    label = selectedType,
                    options = types,
                    onSelect = onTypeChange,
                )
                DfFilterDropdown(
                    label = selectedStatus,
                    options = statuses,
                    onSelect = onStatusChange,
                )
                DfFilterDropdown(
                    label = selectedPriority,
                    options = priorities,
                    onSelect = onPriorityChange,
                )
            }
        },
    )
}
