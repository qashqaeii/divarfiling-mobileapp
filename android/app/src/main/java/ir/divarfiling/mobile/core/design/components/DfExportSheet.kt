package ir.divarfiling.mobile.core.design.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.export.ExportFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfExportSheet(
    title: String,
    subtitle: String,
    formats: List<ExportFormat>,
    isExporting: Boolean,
    onSelect: (ExportFormat) -> Unit,
    onDismiss: () -> Unit,
) {
    DfSheetScaffold(
        title = title,
        subtitle = subtitle,
        icon = DfIcons.Download,
        onClose = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Text(
                text = "فرمت خروجی را انتخاب کنید",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
            formats.forEach { format ->
                ExportFormatOption(
                    format = format,
                    enabled = !isExporting,
                    onClick = { onSelect(format) },
                )
            }
            if (isExporting) {
                Text(
                    text = "در حال آماده‌سازی فایل…",
                    style = AppTypography.labelSmall,
                    color = DfColors.Purple,
                    modifier = Modifier.padding(top = AppSpacing.xs),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportFormatOption(
    format: ExportFormat,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val icon = when (format) {
        ExportFormat.XLSX -> DfIcons.File
        ExportFormat.JSON -> DfIcons.Database
        ExportFormat.CSV -> DfIcons.ClipboardList
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = AppShapes.IconContainer,
                color = DfColors.PurpleContainer,
                modifier = Modifier.size(40.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = DfColors.Purple,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = format.label,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                )
                Text(
                    text = ".${format.extension}",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
            Icon(
                imageVector = DfIcons.ChevronLeft,
                contentDescription = null,
                tint = DfColors.TextMuted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
