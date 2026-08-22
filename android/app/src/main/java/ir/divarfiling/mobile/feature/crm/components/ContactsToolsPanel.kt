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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfDecorIconBox
import ir.divarfiling.mobile.core.design.components.DfDecorIcons

@Composable
fun ContactsToolsPanel(
    onDownloadTemplate: () -> Unit,
    onBulkImport: () -> Unit,
    onExportClick: () -> Unit,
    exportPreviewCount: Int,
    hasActiveFilters: Boolean,
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
            Text(
                text = "ورود و خروج داده",
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.Bold,
                color = DfColors.TextPrimary,
            )
            Text(
                text = "Excel · CSV · JSON",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            ContactsToolActionCard(
                title = "قالب ورود",
                subtitle = "دانلود Excel",
                iconRes = DfDecorIcons.FileText,
                accent = DfColors.Blue,
                container = DfColors.BlueLight,
                onClick = onDownloadTemplate,
                modifier = Modifier.weight(1f),
            )
            ContactsToolActionCard(
                title = "ورود گروهی",
                subtitle = "آپلود فایل",
                iconRes = DfDecorIcons.Upload,
                accent = DfColors.Green,
                container = DfColors.GreenLight,
                onClick = onBulkImport,
                modifier = Modifier.weight(1f),
            )
        }

        Surface(
            onClick = onExportClick,
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card,
            color = DfColors.Surface,
            border = BorderStroke(1.dp, DfColors.Purple.copy(alpha = 0.22f)),
            shadowElevation = AppElevations.subtle,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DfDecorIconBox(
                    resId = DfDecorIcons.Download,
                    containerSize = 44.dp,
                    imageSize = 22.dp,
                    background = DfColors.PurpleContainer,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "خروجی مخاطبین",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.TextPrimary,
                    )
                    Text(
                        text = buildString {
                            append("آماده‌سازی ")
                            append(DateUtils.toPersianDigits(exportPreviewCount.toString()))
                            append(" مخاطب")
                            if (hasActiveFilters) append(" · با فیلتر فعلی")
                        },
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextMuted,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ExportFormatChip("XLSX", DfColors.Green)
                    ExportFormatChip("CSV", DfColors.Blue)
                    ExportFormatChip("JSON", DfColors.Purple)
                }
                Icon(
                    imageVector = DfIcons.ChevronLeft,
                    contentDescription = null,
                    tint = DfColors.TextMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun ContactsToolActionCard(
    title: String,
    subtitle: String,
    iconRes: Int,
    accent: androidx.compose.ui.graphics.Color,
    container: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.Card,
        color = DfColors.Surface,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
        shadowElevation = AppElevations.subtle,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DfDecorIconBox(
                resId = iconRes,
                containerSize = 40.dp,
                imageSize = 20.dp,
                background = container,
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                )
                Text(
                    text = subtitle,
                    style = AppTypography.labelSmall,
                    color = accent,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ExportFormatChip(
    label: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Surface(
        shape = AppShapes.Chip,
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            style = AppTypography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
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
