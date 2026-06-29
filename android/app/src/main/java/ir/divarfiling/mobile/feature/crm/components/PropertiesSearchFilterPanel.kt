package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfFilterChipSection
import ir.divarfiling.mobile.core.design.components.DfFilterDropdown
import ir.divarfiling.mobile.core.design.components.DfFilterDropdownRow
import ir.divarfiling.mobile.core.design.components.DfSearchFilterPanel
import ir.divarfiling.mobile.feature.crm.PropertyConstants

private const val ALL_TX = "همه وضعیت‌ها"
private const val ALL_DEAL = "همه معاملات"
private const val ALL_TYPE = "همه انواع"

@Composable
fun PropertiesSearchFilterPanel(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    transactionStatus: String?,
    dealMode: String?,
    propertyType: String?,
    onTransactionStatusChange: (String?) -> Unit,
    onDealModeChange: (String?) -> Unit,
    onPropertyTypeChange: (String?) -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val txLabel = transactionStatus ?: ALL_TX
    val dealLabel = dealMode ?: ALL_DEAL
    val typeLabel = propertyType ?: ALL_TYPE
    val hasActiveFilters = transactionStatus != null ||
        dealMode != null ||
        propertyType != null ||
        query.isNotBlank()

    DfSearchFilterPanel(
        modifier = modifier,
        title = "جستجو",
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        searchPlaceholder = "عنوان، شهر، محله یا نوع ملک…",
        filters = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                HorizontalDivider(color = DfColors.Outline.copy(alpha = 0.2f))
                DfFilterChipSection(label = "وضعیت معامله") {
                    DfFilterDropdownRow {
                        DfFilterDropdown(
                            label = txLabel,
                            options = listOf(ALL_TX) + PropertyConstants.TX_STATUSES,
                            onSelect = { selected ->
                                onTransactionStatusChange(
                                    if (selected == ALL_TX) null else selected,
                                )
                                onSearch()
                            },
                        )
                    }
                }
                DfFilterChipSection(label = "نوع معامله") {
                    DfFilterDropdownRow {
                        DfFilterDropdown(
                            label = dealLabel,
                            options = listOf(ALL_DEAL) + PropertyConstants.DEAL_MODES,
                            onSelect = { selected ->
                                onDealModeChange(if (selected == ALL_DEAL) null else selected)
                                onSearch()
                            },
                        )
                    }
                }
                DfFilterChipSection(label = "نوع ملک") {
                    DfFilterDropdownRow {
                        DfFilterDropdown(
                            label = typeLabel,
                            options = listOf(ALL_TYPE) + PropertyConstants.PROPERTY_TYPES,
                            onSelect = { selected ->
                                onPropertyTypeChange(if (selected == ALL_TYPE) null else selected)
                                onSearch()
                            },
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
                                color = DfColors.Purple,
                            )
                        }
                    }
                }
            }
        },
    )
}
