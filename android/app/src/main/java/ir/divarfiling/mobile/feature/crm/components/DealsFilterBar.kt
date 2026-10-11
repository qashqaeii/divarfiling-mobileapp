package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfFilterChipSection
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
    val hasActiveFilters = selectedOwner != DealsFilters.ALL_OWNERS ||
        selectedSort != DealsFilters.NEWEST ||
        query.isNotBlank()

    DfSearchFilterPanel(
        modifier = modifier,
        title = "جستجو",
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        searchPlaceholder = "عنوان، مخاطب، ملک یا مرحله…",
        filters = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                HorizontalDivider(color = DfThemeColors.outlineSubtle())
                DfFilterChipSection(label = "مرتب‌سازی") {
                    DfFilterDropdownRow {
                        DfFilterDropdown(
                            label = selectedSort,
                            options = listOf(DealsFilters.NEWEST, DealsFilters.OLDEST),
                            onSelect = onSortChange,
                        )
                    }
                }
                DfFilterChipSection(label = "مخاطب") {
                    DfFilterDropdownRow {
                        DfFilterDropdown(
                            label = selectedOwner,
                            options = owners,
                            onSelect = onOwnerChange,
                        )
                    }
                }
                if (hasActiveFilters) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onResetFilters) {
                            Text(
                                text = "پاک‌سازی فیلترها",
                                style = AppTypography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = DfThemeColors.primary(),
                            )
                        }
                    }
                }
            }
        },
    )
}
