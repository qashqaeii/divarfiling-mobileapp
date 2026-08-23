package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.export.ExportFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsExportSheet(
    exportCount: Int,
    hasActiveFilters: Boolean,
    filterSummary: String?,
    isExporting: Boolean,
    onSelect: (ExportFormat) -> Unit,
    onDismiss: () -> Unit,
) {
    val countLabel = DateUtils.toPersianDigits(exportCount.toString())

    DfSheetScaffold(
        title = "خروجی مخاطبین",
        subtitle = "انتخاب فرمت مناسب برای اشتراک یا آرشیو",
        icon = DfIcons.Download,
        onClose = onDismiss,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card,
            color = DfThemeColors.primaryContainer().copy(alpha = 0.55f),
            border = BorderStroke(1.dp, DfThemeColors.primary().copy(alpha = 0.2f)),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "$countLabel مخاطب در این خروجی",
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                )
                Text(
                    text = when {
                        hasActiveFilters && !filterSummary.isNullOrBlank() ->
                            "فیلتر فعال: $filterSummary"
                        hasActiveFilters ->
                            "خروجی بر اساس فیلترهای اعمال‌شده در لیست"
                        else ->
                            "همه مخاطبین بارگذاری‌شده در این خروجی گنجانده می‌شوند"
                    },
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textMuted(),
                )
            }
        }

        if (isExporting) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = DfThemeColors.primary(),
                    trackColor = DfThemeColors.primaryContainer(),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = DfThemeColors.primary(),
                    )
                    Text(
                        text = "در حال آماده‌سازی فایل…",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.primary(),
                    )
                }
            }
        }

        Text(
            text = "فرمت خروجی",
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = DfThemeColors.textPrimary(),
            modifier = Modifier.padding(top = AppSpacing.xs),
        )

        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            ContactsExportFormatCard(
                format = ExportFormat.XLSX,
                title = "اکسل (XLSX)",
                description = "بهترین گزینه برای Excel، Google Sheets و گزارش‌گیری تیمی",
                accent = DfThemeColors.success(),
                container = DfThemeColors.successContainer(),
                icon = DfIcons.File,
                enabled = !isExporting,
                onClick = { onSelect(ExportFormat.XLSX) },
            )
            ContactsExportFormatCard(
                format = ExportFormat.CSV,
                title = "CSV",
                description = "سازگار با ابزارهای تحلیل، CRM دیگر و واردکننده‌های ساده",
                accent = DfThemeColors.info(),
                container = DfThemeColors.infoContainer(),
                icon = DfIcons.ClipboardList,
                enabled = !isExporting,
                onClick = { onSelect(ExportFormat.CSV) },
            )
            ContactsExportFormatCard(
                format = ExportFormat.JSON,
                title = "JSON",
                description = "ساختار کامل داده برای یکپارچه‌سازی، پشتیبان‌گیری و توسعه",
                accent = DfThemeColors.primary(),
                container = DfThemeColors.primaryContainer(),
                icon = DfIcons.Layers,
                enabled = !isExporting,
                onClick = { onSelect(ExportFormat.JSON) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactsExportFormatCard(
    format: ExportFormat,
    title: String,
    description: String,
    accent: Color,
    container: Color,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.2f)),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = AppShapes.CardSmall,
                    color = container,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                    )
                    Surface(
                        shape = AppShapes.Chip,
                        color = accent.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = ".${format.extension}",
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            style = AppTypography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Text(
                    text = description,
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textMuted(),
                )
            }
            Icon(
                imageVector = DfIcons.ChevronLeft,
                contentDescription = null,
                tint = DfThemeColors.textMuted(),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
