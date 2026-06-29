package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfFilterApplyButton
import ir.divarfiling.mobile.core.design.components.DfFilterDropdownRow
import ir.divarfiling.mobile.core.design.components.DfSearchFilterPanel

@Composable
fun ListingsSearchFilterPanel(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    activeFilterCount: Int,
    onOpenFilters: () -> Unit,
    activeFilterChips: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    DfSearchFilterPanel(
        modifier = modifier,
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        searchPlaceholder = "جستجو در عنوان، محله یا شهر…",
        filters = {
            DfFilterDropdownRow {
                DfFilterApplyButton(
                    label = if (activeFilterCount > 0) "فیلتر پیشرفته ($activeFilterCount)" else "فیلتر پیشرفته",
                    onClick = onOpenFilters,
                )
            }
            activeFilterChips?.invoke()
        },
    )
}

@Composable
fun ListingsActiveFilterChips(
    priceMin: Long?,
    priceMax: Long?,
    areaMin: Int?,
    areaMax: Int?,
    rooms: Int?,
    formatPrice: (Long) -> String,
    modifier: Modifier = Modifier,
) {
    val hasFilters = listOf(priceMin, priceMax, areaMin, areaMax, rooms).any { it != null }
    if (!hasFilters) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        priceMin?.let { DfBadge(text = "از ${formatPrice(it)}") }
        priceMax?.let { DfBadge(text = "تا ${formatPrice(it)}") }
        areaMin?.let { DfBadge(text = "متراژ از $it") }
        areaMax?.let { DfBadge(text = "متراژ تا $it") }
        rooms?.let { DfBadge(text = "$it اتاق") }
    }
}
