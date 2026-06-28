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

enum class ContactsViewMode { Grid, List }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsFilterBar(
    priorities: List<String>,
    statuses: List<String>,
    types: List<String>,
    selectedPriority: String,
    selectedStatus: String,
    selectedType: String,
    viewMode: ContactsViewMode,
    onPriorityChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onViewModeChange: (ContactsViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
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
                ContactsFilterDropdown(
                    label = selectedType,
                    options = types,
                    onSelect = onTypeChange,
                )
                ContactsFilterDropdown(
                    label = selectedStatus,
                    options = statuses,
                    onSelect = onStatusChange,
                )
                ContactsFilterDropdown(
                    label = selectedPriority,
                    options = priorities,
                    onSelect = onPriorityChange,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                ContactsViewModeButton(
                    icon = DfIcons.LayoutGrid,
                    selected = viewMode == ContactsViewMode.Grid,
                    onClick = { onViewModeChange(ContactsViewMode.Grid) },
                )
                ContactsViewModeButton(
                    icon = DfIcons.LayoutList,
                    selected = viewMode == ContactsViewMode.List,
                    onClick = { onViewModeChange(ContactsViewMode.List) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
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
                            text = "جستجو نام، تلفن، شرکت...",
                            style = AppTypography.bodyDescription,
                            color = DfColors.TextMuted,
                        )
                    }
                    inner()
                },
            )
            if (query.isNotBlank()) {
                Surface(
                    onClick = onSearch,
                    shape = AppShapes.Chip,
                    color = DfColors.Purple,
                ) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactsViewModeButton(
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
private fun ContactsFilterDropdown(
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
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
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
