package ir.divarfiling.mobile.feature.extract.components

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
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.network.ExtractionUploadData

@Composable
fun ExtractResultSummaryCard(
    message: String,
    stats: ExtractionUploadData?,
    onViewDataset: (String) -> Unit,
    datasetId: String?,
    modifier: Modifier = Modifier,
) {
    ExtractSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = AppShapes.IconContainer,
                    color = DfThemeColors.successContainer(),
                    modifier = Modifier.size(44.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = DfIcons.CircleCheck,
                            contentDescription = null,
                            tint = DfThemeColors.success(),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "استخراج با موفقیت انجام شد",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                    )
                    Text(
                        text = message,
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                }
            }

            stats?.let { data ->
                stats.datasetName?.takeIf { it.isNotBlank() }?.let { name ->
                    Text(
                        text = name,
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = DfThemeColors.primary(),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    ResultMetricTile(
                        label = "ثبت‌شده",
                        value = DateUtils.toPersianDigits(data.ingestedCount.toString()),
                        modifier = Modifier.weight(1f),
                    )
                    ResultMetricTile(
                        label = "جدید",
                        value = DateUtils.toPersianDigits(data.createdCount.toString()),
                        modifier = Modifier.weight(1f),
                    )
                    ResultMetricTile(
                        label = "در فایل",
                        value = DateUtils.toPersianDigits(data.totalInDataset.toString()),
                        modifier = Modifier.weight(1f),
                    )
                }
                if (data.genuinePersonalCount > 0 || data.disguisedConsultantCount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        if (data.genuinePersonalCount > 0) {
                            ResultMetricTile(
                                label = "مالک واقعی",
                                value = DateUtils.toPersianDigits(data.genuinePersonalCount.toString()),
                                accent = DfThemeColors.success(),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (data.disguisedConsultantCount > 0) {
                            ResultMetricTile(
                                label = "مشاور پنهان",
                                value = DateUtils.toPersianDigits(data.disguisedConsultantCount.toString()),
                                accent = DfThemeColors.warning(),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                if (data.datasetMerged) {
                    Text(
                        text = "با فایلینگ قبلی ادغام شد",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                    )
                }
            }

            datasetId?.let { id ->
                DfPrimaryButton(
                    text = "مشاهده در فایلینگ",
                    onClick = { onViewDataset(id) },
                )
            }
        }
    }
}

@Composable
private fun ResultMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: androidx.compose.ui.graphics.Color = DfThemeColors.primary(),
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = accent.copy(alpha = if (DfThemeColors.isDark()) 0.14f else 0.1f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
            )
            Text(
                text = value,
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
        }
    }
}
