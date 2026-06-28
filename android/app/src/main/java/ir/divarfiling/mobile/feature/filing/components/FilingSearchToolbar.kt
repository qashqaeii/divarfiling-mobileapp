package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun FilingSearchToolbar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onUploadClick: () -> Unit,
    onTutorialClick: () -> Unit,
    onToolsClick: () -> Unit,
    onCompareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ColumnSection(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilingToolbarAction(
                label = "آپلود فایل / پوشه",
                icon = DfIcons.Upload,
                filled = true,
                onClick = onUploadClick,
            )
            FilingToolbarAction(
                label = "آموزش",
                icon = DfIcons.Play,
                onClick = onTutorialClick,
            )
            FilingToolbarAction(
                label = "ابزارها",
                icon = DfIcons.WandSparkles,
                onClick = onToolsClick,
            )
            FilingToolbarAction(
                label = "مقایسه",
                icon = DfIcons.Scale,
                onClick = onCompareClick,
            )
        }

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
                                text = "جستجوی سریع در فایل‌ها...",
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilingToolbarAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    filled: Boolean = false,
) {
    Surface(
        onClick = onClick,
        shape = AppShapes.CardSmall,
        color = if (filled) DfColors.Purple else DfColors.Surface,
        shadowElevation = if (filled) 0.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (filled) Color.White else DfColors.Purple,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = label,
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (filled) Color.White else DfColors.TextPrimary,
                maxLines = 1,
            )
        }
    }
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
