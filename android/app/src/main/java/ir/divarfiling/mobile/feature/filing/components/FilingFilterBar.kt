package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.components.DfFilterApplyButton
import ir.divarfiling.mobile.core.design.components.DfFilterDropdown
import ir.divarfiling.mobile.core.design.components.DfFilterDropdownRow
import ir.divarfiling.mobile.core.design.components.DfSearchFilterPanel

@Composable
fun FilingSearchFilterPanel(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    formats: List<String>,
    cities: List<String>,
    transactions: List<String>,
    selectedFormat: String,
    selectedCity: String,
    selectedTransaction: String,
    onFormatChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onTransactionChange: (String) -> Unit,
    onApplyFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfSearchFilterPanel(
        modifier = modifier,
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        searchPlaceholder = "جستجوی سریع در فایل‌ها...",
        filters = {
            DfFilterDropdownRow {
                DfFilterDropdown(
                    label = selectedTransaction,
                    options = transactions,
                    onSelect = onTransactionChange,
                )
                DfFilterDropdown(
                    label = selectedCity,
                    options = cities,
                    onSelect = onCityChange,
                )
                DfFilterDropdown(
                    label = selectedFormat,
                    options = formats,
                    onSelect = onFormatChange,
                )
                DfFilterApplyButton(onClick = onApplyFilters)
            }
        },
    )
}
