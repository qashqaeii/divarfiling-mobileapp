package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant
import ir.divarfiling.mobile.core.design.components.DfGlassIconButton
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.liquidGlassSurface

@Composable
fun DfSearchFilterPanel(
    modifier: Modifier = Modifier,
    title: String = "جستجو و فیلتر",
    query: String? = null,
    onQueryChange: ((String) -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    searchPlaceholder: String = "جستجو…",
    footer: @Composable (() -> Unit)? = null,
    filters: (@Composable () -> Unit)? = null,
) {
    val showSearch = query != null && onQueryChange != null
    val showFilters = filters != null

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        shape = AppShapes.Hero,
        color = DfColors.Surface,
        shadowElevation = AppElevations.card,
        tonalElevation = AppElevations.none,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DfColors.GlassBorder.copy(alpha = 0.55f), AppShapes.Hero)
                .padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = AppShapes.IconContainer,
                    color = DfColors.PurpleContainer,
                    modifier = Modifier.size(30.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = DfIcons.SlidersHorizontal,
                            contentDescription = null,
                            tint = DfColors.Purple,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
                Text(
                    text = title,
                    style = AppTypography.sectionTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                )
            }

            if (showSearch) {
                DfPanelSearchField(
                    query = query.orEmpty(),
                    onQueryChange = onQueryChange,
                    placeholder = searchPlaceholder,
                    onSearch = onSearch,
                )
            }

            if (showSearch && showFilters) {
                HorizontalDivider(color = DfColors.GlassBorder.copy(alpha = 0.45f))
            }

            filters?.invoke()

            if (footer != null && (showFilters || showSearch)) {
                HorizontalDivider(color = DfColors.GlassBorder.copy(alpha = 0.45f))
                footer()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfPanelSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onSearch: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Field,
        color = Color.Transparent,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassSurface(shape = AppShapes.Field, elevation = 3.dp)
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
                            text = placeholder,
                            style = AppTypography.bodyDescription,
                            color = DfColors.TextMuted,
                        )
                    }
                    inner()
                },
            )
            if (query.isNotBlank() && onSearch != null) {
                DfGlassTextButton(
                    text = "جستجو",
                    onClick = onSearch,
                    compact = true,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfFilterDropdown(
    label: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .liquidGlassSurface(shape = AppShapes.CardSmall, elevation = 4.dp)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 10.dp),
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
fun DfFilterDropdownRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        content = { content() },
    )
}

@Composable
fun DfFilterChipSection(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
    ) {
        Text(
            text = label,
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
            fontWeight = FontWeight.Medium,
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfFilterApplyButton(
    label: String = "اعمال فیلتر",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .liquidGlassSurface(
                shape = AppShapes.CardSmall,
                variant = DfGlassButtonVariant.Primary,
                elevation = 6.dp,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
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
            text = label,
            style = AppTypography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}
