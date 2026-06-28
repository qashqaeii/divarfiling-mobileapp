package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons

enum class FilingViewMode { Grid, List }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilingFilterBar(
    formats: List<String>,
    cities: List<String>,
    transactions: List<String>,
    selectedFormat: String,
    selectedCity: String,
    selectedTransaction: String,
    viewMode: FilingViewMode,
    onFormatChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onTransactionChange: (String) -> Unit,
    onViewModeChange: (FilingViewMode) -> Unit,
    onApplyFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ColumnSection(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                ViewModeButton(
                    icon = DfIcons.LayoutGrid,
                    selected = viewMode == FilingViewMode.Grid,
                    onClick = { onViewModeChange(FilingViewMode.Grid) },
                )
                ViewModeButton(
                    icon = DfIcons.LayoutList,
                    selected = viewMode == FilingViewMode.List,
                    onClick = { onViewModeChange(FilingViewMode.List) },
                )
            }
            Surface(
                onClick = onApplyFilters,
                shape = AppShapes.CardSmall,
                color = DfColors.Purple,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = DfIcons.SlidersHorizontal,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "اعمال فیلتر",
                        style = AppTypography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilingFilterDropdown(
                label = selectedTransaction,
                options = transactions,
                onSelect = onTransactionChange,
            )
            FilingFilterDropdown(
                label = selectedCity,
                options = cities,
                onSelect = onCityChange,
            )
            FilingFilterDropdown(
                label = selectedFormat,
                options = formats,
                onSelect = onFormatChange,
            )
            Surface(
                shape = AppShapes.CardSmall,
                color = DfColors.SurfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = DfIcons.Bookmark,
                        contentDescription = null,
                        tint = DfColors.Purple,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "فیلترهای ذخیره‌شده",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextSecondary,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewModeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = AppShapes.IconContainer,
        color = if (selected) DfColors.Purple else DfColors.SurfaceVariant,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.White else DfColors.TextMuted,
            modifier = Modifier
                .padding(10.dp)
                .size(16.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilingFilterDropdown(
    label: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    BoxAnchor {
        Surface(
            onClick = { expanded = true },
            shape = AppShapes.CardSmall,
            color = DfColors.Surface,
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextPrimary,
                    maxLines = 1,
                )
                Icon(
                    imageVector = DfIcons.ChevronDown,
                    contentDescription = null,
                    tint = DfColors.TextMuted,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun BoxAnchor(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box { content() }
}

@Composable
private fun ColumnSection(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        content = { content() },
    )
}
