package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsFilterBar(
    owners: List<String>,
    selectedOwner: String,
    selectedSort: String,
    viewMode: DealsViewMode,
    onOwnerChange: (String) -> Unit,
    onSortChange: (String) -> Unit,
    onResetFilters: () -> Unit,
    onViewModeChange: (DealsViewMode) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Field,
            color = DfColors.Surface,
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.sm, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Icon(
                    imageVector = DfIcons.Search,
                    contentDescription = null,
                    tint = DfColors.TextMuted,
                    modifier = Modifier.size(18.dp),
                )
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    textStyle = AppTypography.bodyDescription.copy(color = DfColors.TextPrimary),
                    singleLine = true,
                    cursorBrush = SolidColor(DfColors.Purple),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text(
                                text = "جستجو در معاملات...",
                                style = AppTypography.bodyDescription,
                                color = DfColors.TextMuted,
                            )
                        }
                        inner()
                    },
                )
                if (query.isNotBlank()) {
                    Surface(onClick = onSearch, shape = AppShapes.Chip, color = DfColors.Purple) {
                        Text(
                            text = "جستجو",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = AppTypography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DealsFilterDropdown(
                    label = DealsFilters.ALL_FILTERS,
                    options = listOf(DealsFilters.ALL_FILTERS),
                    onSelect = { if (it == DealsFilters.ALL_FILTERS) onResetFilters() },
                )
                DealsFilterDropdown(
                    label = selectedSort,
                    options = listOf(DealsFilters.NEWEST, DealsFilters.OLDEST),
                    onSelect = onSortChange,
                )
                DealsFilterDropdown(
                    label = selectedOwner,
                    options = owners,
                    onSelect = onOwnerChange,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                DealsViewModeButton(
                    icon = DfIcons.LayoutGrid,
                    selected = viewMode == DealsViewMode.Grid,
                    onClick = { onViewModeChange(DealsViewMode.Grid) },
                )
                DealsViewModeButton(
                    icon = DfIcons.LayoutList,
                    selected = viewMode == DealsViewMode.List,
                    onClick = { onViewModeChange(DealsViewMode.List) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DealsViewModeButton(
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
private fun DealsFilterDropdown(
    label: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    BoxAnchor {
        Surface(onClick = { expanded = true }, shape = AppShapes.CardSmall, color = DfColors.Surface, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = label, style = AppTypography.labelSmall, color = DfColors.TextPrimary, maxLines = 1)
                Icon(imageVector = DfIcons.ChevronDown, contentDescription = null, tint = DfColors.TextMuted, modifier = Modifier.size(14.dp))
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
