package ir.divarfiling.mobile.feature.extract.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCountSlider
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.feature.extract.ExtractCategories
import ir.divarfiling.mobile.feature.extract.ExtractSubcategory
import ir.divarfiling.mobile.feature.extract.ExtractUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtractFiltersCard(
    state: ExtractUiState,
    subcategories: List<ExtractSubcategory>,
    enabled: Boolean,
    onTransactionTypeChange: (String) -> Unit,
    onSubcategoryChange: (String) -> Unit,
    onSortChange: (String) -> Unit,
    onAdvertiserFilterChange: (String) -> Unit,
    onMaxItemsChange: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    advancedFilters: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val advertiserLabel = ExtractCategories.advertiserOptions
        .firstOrNull { it.first == state.advertiserFilter }?.second ?: "همه آگهی‌ها"

    ExtractSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            ExtractSectionTitle(
                title = "فیلترهای استخراج",
                icon = DfIcons.SlidersHorizontal,
            )

            FilterBlock(title = "جستجو", subtitle = "خالی = همه آگهی‌های دسته") {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("عبارت جستجو (اختیاری)") },
                    placeholder = { Text("معاوضه، پارکینگ، نوساز…") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    singleLine = true,
                    shape = AppShapes.Field,
                    colors = filterFieldColors(),
                    leadingIcon = {
                        Icon(
                            imageVector = DfIcons.Search,
                            contentDescription = null,
                            tint = DfThemeColors.primary(),
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }

            FilterBlock(title = "دسته و ترتیب") {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    DfDropdown(
                        label = "نوع معامله",
                        value = state.transactionType,
                        options = ExtractCategories.transactionTypes.map { it.label },
                        enabled = enabled,
                        onSelect = onTransactionTypeChange,
                    )
                    DfDropdown(
                        label = "زیردسته",
                        value = state.subcategoryLabel,
                        options = subcategories.map { it.label },
                        enabled = enabled,
                        onSelect = onSubcategoryChange,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            DfDropdown(
                                label = "مرتب‌سازی",
                                value = ExtractCategories.sortOptions
                                    .firstOrNull { it.first == state.sort }?.second ?: "",
                                options = ExtractCategories.sortOptions.map { it.second },
                                enabled = enabled,
                                onSelect = { label ->
                                    ExtractCategories.sortOptions
                                        .firstOrNull { it.second == label }
                                        ?.first
                                        ?.let(onSortChange)
                                },
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DfDropdown(
                                label = "آگهی‌دهنده",
                                value = advertiserLabel,
                                options = ExtractCategories.advertiserOptions.map { it.second },
                                enabled = enabled,
                                onSelect = { label ->
                                    ExtractCategories.advertiserOptions
                                        .firstOrNull { it.second == label }
                                        ?.first
                                        ?.let(onAdvertiserFilterChange)
                                },
                            )
                        }
                    }
                }
            }

            FilterBlock(title = "حجم استخراج") {
                DfCountSlider(
                    value = state.maxItems,
                    onValueChange = onMaxItemsChange,
                    enabled = enabled,
                    label = "تعداد آگهی",
                )
            }

            FilterBlock(title = "قیمت و مشخصات", subtitle = "اختیاری") {
                advancedFilters()
            }
        }
    }
}

@Composable
private fun FilterBlock(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = 14.dp)
                    .background(
                        color = DfThemeColors.primary(),
                        shape = RoundedCornerShape(2.dp),
                    ),
            )
            Text(
                text = title,
                style = AppTypography.bodyDescription,
                fontWeight = FontWeight.SemiBold,
                color = DfThemeColors.textPrimary(),
            )
        }
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
                modifier = Modifier.padding(start = 11.dp),
            )
        }
        content()
    }
}

@Composable
private fun filterFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = DfThemeColors.primary(),
    unfocusedBorderColor = DfThemeColors.outline(),
    focusedContainerColor = DfThemeColors.surface(),
    unfocusedContainerColor = DfThemeColors.surface(),
    cursorColor = DfThemeColors.primary(),
    focusedLabelColor = DfThemeColors.primary(),
)
