package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.network.DatasetDto

@Composable
fun DatasetSummaryCard(
    dataset: DatasetDto?,
    filteredCount: Int?,
    modifier: Modifier = Modifier,
) {
    if (dataset == null) return
    val totalCount = dataset.itemCount
    val shownCount = filteredCount ?: totalCount
    val updatedLabel = DateUtils.formatRelativeTimeAgo(dataset.updatedAt)
        .takeIf { dataset.updatedAt?.isNotBlank() == true }
        ?: "—"

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.subtle,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DatasetSummaryStat(
                    label = "آگهی",
                    value = if (shownCount != totalCount) {
                        "${DateUtils.toPersianDigits(shownCount.toString())} از ${DateUtils.toPersianDigits(totalCount.toString())}"
                    } else {
                        DateUtils.toPersianDigits(totalCount.toString())
                    },
                    modifier = Modifier.weight(1f),
                )
                DatasetSummaryStat(
                    label = "آخرین بروزرسانی",
                    value = updatedLabel,
                    modifier = Modifier.weight(1f),
                )
            }
            if (dataset.genuinePersonalCount > 0 || dataset.disguisedConsultantCount > 0) {
                HorizontalDivider(color = DfThemeColors.outlineSubtle())
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    if (dataset.genuinePersonalCount > 0) {
                        DatasetSummaryStat(
                            label = "مالک واقعی",
                            value = DateUtils.toPersianDigits(dataset.genuinePersonalCount.toString()),
                            accent = DfThemeColors.success(),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (dataset.disguisedConsultantCount > 0) {
                        DatasetSummaryStat(
                            label = "مشاور پنهان",
                            value = DateUtils.toPersianDigits(dataset.disguisedConsultantCount.toString()),
                            accent = DfThemeColors.warning(),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DatasetSummaryStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: androidx.compose.ui.graphics.Color = DfThemeColors.textPrimary(),
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = AppTypography.labelSmall,
            color = DfThemeColors.textMuted(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun DatasetQuickActionsRow(
    onInsights: () -> Unit,
    onMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        DatasetQuickActionButton(
            label = "تحلیل فایل",
            icon = DfIcons.BarChart,
            tint = DfThemeColors.primary(),
            background = DfThemeColors.primaryContainer(),
            onClick = onInsights,
            modifier = Modifier.weight(1f),
        )
        DatasetQuickActionButton(
            label = "نقشه",
            icon = DfIcons.Map,
            tint = DfThemeColors.info(),
            background = DfThemeColors.infoContainer(),
            onClick = onMap,
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DatasetQuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    background: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = background.copy(alpha = if (DfThemeColors.isDark()) 0.35f else 0.72f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.18f)),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                modifier = Modifier.padding(start = 6.dp),
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = tint,
                maxLines = 1,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun DatasetSummaryCardPreview() {
    DivarFilingTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DatasetSummaryCard(
                dataset = DatasetDto(
                    id = "1",
                    name = "فایل ونک",
                    itemCount = 248,
                    updatedAt = "2026-03-23T10:00:00Z",
                    genuinePersonalCount = 42,
                    disguisedConsultantCount = 18,
                ),
                filteredCount = 120,
            )
            DatasetQuickActionsRow(onInsights = {}, onMap = {})
        }
    }
}
