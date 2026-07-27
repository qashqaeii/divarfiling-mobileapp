package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfFilterApplyButton
import ir.divarfiling.mobile.core.design.components.DfFilterDropdownRow
import ir.divarfiling.mobile.core.design.components.DfGlassChip
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfSearchFilterPanel
import ir.divarfiling.mobile.core.network.SavedFilterDto
import ir.divarfiling.mobile.feature.filing.ListingFilterState
import ir.divarfiling.mobile.feature.filing.ListingSortOptions

@Composable
fun ListingsSearchFilterPanel(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    activeFilterCount: Int,
    onOpenFilters: () -> Unit,
    activeFilterChips: @Composable (() -> Unit)? = null,
    savedFiltersSlot: @Composable (() -> Unit)? = null,
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
            savedFiltersSlot?.invoke()
            activeFilterChips?.invoke()
        },
    )
}

@Composable
fun ListingsActiveFilterChips(
    filters: ListingFilterState,
    formatPrice: (Long) -> String,
    onClear: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (filters.activeCount(includeSort = true) == 0) return

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            filters.priceMin?.let { DfBadge(text = "از ${formatPrice(it)}") }
            filters.priceMax?.let { DfBadge(text = "تا ${formatPrice(it)}") }
            filters.areaMin?.let { DfBadge(text = "متراژ از $it") }
            filters.areaMax?.let { DfBadge(text = "متراژ تا $it") }
            if (filters.rooms.isNotBlank()) DfBadge(text = "${filters.rooms} اتاق")
            if (filters.neighborhood.isNotBlank()) DfBadge(text = filters.neighborhood)
            filters.yearMin?.let { DfBadge(text = "سال از $it") }
            filters.yearMax?.let { DfBadge(text = "سال تا $it") }
            if (filters.parking == "1") DfBadge(text = "پارکینگ")
            if (filters.elevator == "1") DfBadge(text = "آسانسور")
            if (filters.storage == "1") DfBadge(text = "انباری")
            when (filters.consultant) {
                "1" -> DfBadge(text = "مشاور")
                "0" -> DfBadge(text = "شخصی")
                "genuine_personal" -> DfBadge(text = "شخصی واقعی")
                "disguised" -> DfBadge(text = "مشاور پنهان")
            }
            when (filters.value) {
                "below" -> DfBadge(text = "زیر ارزش")
                "fair" -> DfBadge(text = "منصفانه")
                "above" -> DfBadge(text = "بالای ارزش")
            }
            if (filters.unique) DfBadge(text = "بدون تکرار")
            if (filters.newOnly) DfBadge(text = "فقط جدید")
            if (filters.sort.isNotBlank()) {
                val label = ListingSortOptions.firstOrNull { it.first == filters.sort }?.second ?: filters.sort
                DfBadge(text = "مرتب: $label")
            }
        }
        if (onClear != null) {
            DfGlassTextButton(text = "پاک‌سازی فیلترها", onClick = onClear)
        }
    }
}

@Composable
fun SavedFiltersChipRow(
    filters: List<SavedFilterDto>,
    activeId: Long?,
    onSelect: (SavedFilterDto) -> Unit,
    onPin: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (filters.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "فیلترهای ذخیره‌شده",
            style = AppTypography.labelSmall,
            color = DfColors.TextSecondary,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            filters.forEach { filter ->
                val selected = filter.id == activeId
                val label = buildString {
                    if (filter.isPinned) append("★ ")
                    append(filter.name)
                    if (filter.newCount > 0) append(" +${filter.newCount}")
                }
                DfGlassChip(
                    text = label,
                    selected = selected,
                    onClick = { onSelect(filter) },
                )
            }
        }
        if (activeId != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DfGlassTextButton(text = "سنجاق", onClick = { onPin(activeId) })
                DfGlassTextButton(text = "حذف", onClick = { onDelete(activeId) })
            }
        }
    }
}
