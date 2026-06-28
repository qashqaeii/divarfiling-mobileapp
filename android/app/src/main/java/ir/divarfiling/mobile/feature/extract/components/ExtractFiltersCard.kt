package ir.divarfiling.mobile.feature.extract.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
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
    onToggleAdvanced: () -> Unit,
    onTransactionTypeChange: (String) -> Unit,
    onSubcategoryChange: (String) -> Unit,
    onSortChange: (String) -> Unit,
    onAdvertiserFilterChange: (String) -> Unit,
    onMaxItemsChange: (Int) -> Unit,
    advancedFilters: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val priceLabel = if (state.isRent) "ودیعه / اجاره" else "قیمت (تومان)"
    val priceValue = when {
        state.isRent && (state.depositMin.isNotBlank() || state.depositMax.isNotBlank()) ->
            "${state.depositMin.ifBlank { "از" }} - ${state.depositMax.ifBlank { "تا" }}"
        !state.isRent && (state.priceMin.isNotBlank() || state.priceMax.isNotBlank()) ->
            "${state.priceMin.ifBlank { "از" }} - ${state.priceMax.ifBlank { "تا" }}"
        else -> "از - تا"
    }
    val areaValue = if (state.areaMin.isNotBlank() || state.areaMax.isNotBlank()) {
        "${state.areaMin.ifBlank { "از" }} - ${state.areaMax.ifBlank { "تا" }}"
    } else {
        "از - تا"
    }
    val advertiserLabel = ExtractCategories.advertiserOptions
        .firstOrNull { it.first == state.advertiserFilter }?.second ?: "همه آگهی‌ها"

    ExtractSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = DfIcons.SlidersHorizontal,
                        contentDescription = null,
                        tint = DfColors.Purple,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "فیلترهای پیشرفته (اختیاری)",
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    imageVector = DfIcons.ChevronDown,
                    contentDescription = null,
                    tint = DfColors.TextMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(if (state.showAdvanced) 180f else 0f),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    ExtractFilterTile(
                        label = "نوع معامله",
                        value = state.transactionType,
                        icon = DfIcons.Home,
                        modifier = Modifier.weight(1f),
                    )
                    ExtractFilterTile(
                        label = priceLabel,
                        value = priceValue,
                        icon = DfIcons.Tag,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    ExtractFilterTile(
                        label = "نوع آگهی‌دهنده",
                        value = advertiserLabel,
                        icon = DfIcons.Users,
                        modifier = Modifier.weight(1f),
                    )
                    ExtractFilterTile(
                        label = "متراژ (متر)",
                        value = areaValue,
                        icon = DfIcons.Ruler,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Surface(
                onClick = onToggleAdvanced,
                enabled = enabled,
                shape = AppShapes.ButtonPill,
                color = DfColors.SurfaceVariant,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (state.showAdvanced) "بستن فیلترهای بیشتر" else "نمایش فیلترهای بیشتر",
                        style = AppTypography.labelSmall,
                        color = DfColors.Purple,
                        fontWeight = FontWeight.Medium,
                    )
                    Icon(
                        imageVector = DfIcons.ChevronDown,
                        contentDescription = null,
                        tint = DfColors.Purple,
                        modifier = Modifier
                            .size(14.dp)
                            .rotate(if (state.showAdvanced) 180f else 0f),
                    )
                }
            }

            AnimatedVisibility(
                visible = state.showAdvanced,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
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
                    DfDropdown(
                        label = "مرتب‌سازی",
                        value = ExtractCategories.sortOptions.firstOrNull { it.first == state.sort }?.second ?: "",
                        options = ExtractCategories.sortOptions.map { it.second },
                        enabled = enabled,
                        onSelect = { label ->
                            ExtractCategories.sortOptions.firstOrNull { it.second == label }?.first?.let(onSortChange)
                        },
                    )
                    DfDropdown(
                        label = "نوع آگهی‌دهنده",
                        value = advertiserLabel,
                        options = ExtractCategories.advertiserOptions.map { it.second },
                        enabled = enabled,
                        onSelect = { label ->
                            ExtractCategories.advertiserOptions.firstOrNull { it.second == label }?.first
                                ?.let(onAdvertiserFilterChange)
                        },
                    )
                    DfCountSlider(
                        value = state.maxItems,
                        onValueChange = onMaxItemsChange,
                        enabled = enabled,
                        label = "تعداد آگهی (۰ تا ۱۰۰)",
                    )
                    advancedFilters()
                }
            }
        }
    }
}

@Composable
private fun ExtractFilterTile(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.Field,
        color = DfColors.SurfaceVariant.copy(alpha = 0.65f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DfColors.Purple,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = label,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = value,
                style = AppTypography.bodyDescription,
                fontWeight = FontWeight.SemiBold,
                color = DfColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
