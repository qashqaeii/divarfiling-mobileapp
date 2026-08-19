package ir.divarfiling.mobile.feature.extract.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfFilterOption
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.feature.extract.ExtractBatchPreset
import ir.divarfiling.mobile.feature.extract.ExtractCategories
import ir.divarfiling.mobile.feature.extract.ExtractUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExtractBatchCard(
    state: ExtractUiState,
    enabled: Boolean,
    onPresetChange: (ExtractBatchPreset) -> Unit,
    onJobToggle: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val jobs = state.selectedBatchJobs
    val jobCount = if (state.isBatchMode) jobs.size else 1
    val estimate = jobCount * state.maxItems

    ExtractSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            ExtractSectionTitle(
                title = "استخراج گروهی",
                icon = DfIcons.Layers,
            )
            Text(
                text = "مثل نسخه ویندوز: چند زیردسته را با همان موقعیت و فیلتر در یک عملیات پشت‌سرهم استخراج کنید. هر زیردسته فایلینگ جدا می‌سازد.",
                style = AppTypography.bodyDescription,
                color = DfThemeColors.textSecondary(),
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                presetChips().forEach { option ->
                    DfSoftChip(
                        text = option.label,
                        selected = state.batchPreset == option.value,
                        onClick = { if (enabled) onPresetChange(option.value) },
                    )
                }
            }

            val summary = if (state.isBatchMode) {
                "${DateUtils.toPersianDigits(jobCount.toString())} زیردسته × حداکثر ${DateUtils.toPersianDigits(state.maxItems.toString())} آگهی — برآورد ${DateUtils.toPersianDigits(estimate.toString())} آگهی"
            } else {
                "فقط زیردسته انتخاب‌شده در فیلترها استخراج می‌شود"
            }
            Text(
                text = summary,
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = DfThemeColors.primary(),
            )

            if (state.remainingToday != null && state.isBatchMode && jobCount > (state.remainingToday ?: 0)) {
                Text(
                    text = "امروز ${DateUtils.toPersianDigits(state.remainingToday.toString())} استخراج باقی مانده؛ بقیه دسته‌ها فردا ادامه داده می‌شود.",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.warning(),
                )
            }

            AnimatedVisibility(visible = state.batchPreset == ExtractBatchPreset.CUSTOM) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    ExtractCategories.transactionTypes.forEach { tx ->
                        Text(
                            text = tx.label,
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = DfThemeColors.textPrimary(),
                            modifier = Modifier.padding(top = AppSpacing.xs),
                        )
                        tx.subcategories.forEach { sub ->
                            val job = ExtractCategories.ExtractJob(tx.label, sub.label, sub.apiSlug)
                            val checked = job.key in state.customJobKeys
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { selected ->
                                        if (enabled) onJobToggle(job.key, selected)
                                    },
                                    enabled = enabled,
                                    modifier = Modifier.size(22.dp),
                                )
                                Text(
                                    text = sub.label,
                                    style = AppTypography.bodyDescription,
                                    color = DfThemeColors.textPrimary(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun presetChips(): List<DfFilterOption<ExtractBatchPreset>> = listOf(
    DfFilterOption(ExtractBatchPreset.SINGLE, "فقط همین دسته"),
    DfFilterOption(ExtractBatchPreset.ALL, "همه زیردسته‌ها"),
    DfFilterOption(ExtractBatchPreset.RESIDENTIAL, "فقط مسکونی"),
    DfFilterOption(ExtractBatchPreset.COMMERCIAL, "فقط تجاری"),
    DfFilterOption(ExtractBatchPreset.CUSTOM, "انتخاب سفارشی"),
)
