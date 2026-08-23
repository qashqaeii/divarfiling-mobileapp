package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.DivarFilingTheme

@Composable
fun ContactsToolsPanel(
    onDownloadTemplate: () -> Unit,
    onBulkImport: () -> Unit,
    onExportClick: () -> Unit,
    exportPreviewCount: Int,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "ورود و خروج داده",
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DfThemeColors.textPrimary(),
                )
                Text(
                    text = buildString {
                        append(DateUtils.toPersianDigits(exportPreviewCount.toString()))
                        append(" مخاطب")
                        if (hasActiveFilters) append(" · فیلترشده")
                    },
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ContactsToolAction(
                    title = "قالب",
                    subtitle = "دانلود",
                    icon = DfIcons.File,
                    accent = DfThemeColors.info(),
                    container = DfThemeColors.infoContainer(),
                    onClick = onDownloadTemplate,
                    modifier = Modifier.weight(1f),
                )
                ContactsToolAction(
                    title = "ورود",
                    subtitle = "گروهی",
                    icon = DfIcons.Upload,
                    accent = DfThemeColors.success(),
                    container = DfThemeColors.successContainer(),
                    onClick = onBulkImport,
                    modifier = Modifier.weight(1f),
                )
                ContactsToolAction(
                    title = "خروجی",
                    subtitle = if (hasActiveFilters) "فیلترشده" else "همه",
                    icon = DfIcons.Download,
                    accent = DfThemeColors.primary(),
                    container = DfThemeColors.primaryContainer(),
                    onClick = onExportClick,
                    modifier = Modifier.weight(1f),
                    highlighted = true,
                )
            }
        }
    }
}

@Composable
private fun ContactsToolAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    container: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = if (highlighted) container else DfThemeColors.surfaceVariant().copy(alpha = 0.7f),
        border = BorderStroke(1.dp, if (highlighted) accent.copy(alpha = 0.28f) else Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = title,
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.textPrimary(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                style = AppTypography.labelSmall,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ContactsToolsPanelPreview() {
    DivarFilingTheme {
        ContactsToolsPanel(
            onDownloadTemplate = {},
            onBulkImport = {},
            onExportClick = {},
            exportPreviewCount = 248,
            hasActiveFilters = true,
        )
    }
}
