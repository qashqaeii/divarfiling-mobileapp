package ir.divarfiling.mobile.feature.extract.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.feature.extract.schedule.scheduleNextRunLabel
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.network.ExtractionScheduleDto

@Composable
fun ScheduleSummaryHero(
    schedules: List<ExtractionScheduleDto>,
    modifier: Modifier = Modifier,
) {
    val activeCount = schedules.count { it.isEnabled }
    val pausedCount = schedules.size - activeCount
    val totalRuns = schedules.sumOf { it.runCount }
    val runningCount = schedules.count { it.lastStatus == "running" }
    val nextSchedule = schedules
        .filter { it.isEnabled && !it.nextRunAt.isNullOrBlank() }
        .minByOrNull { it.nextRunAt.orEmpty() }

    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                    Text(
                        text = "پایش خودکار",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                    )
                    Text(
                        text = "فیلترهای ذخیره‌شده روی این دستگاه اجرا می‌شوند",
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = DfThemeColors.primaryContainer(),
                            shape = AppShapes.IconContainer,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    DfDecorImage(
                        resId = DfDecorIcons.Timer,
                        size = 22.dp,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                SummaryMetricChip(
                    value = DateUtils.toPersianDigits(activeCount.toString()),
                    label = "فعال",
                    accent = DfThemeColors.success(),
                    background = DfThemeColors.successContainer(),
                    modifier = Modifier.weight(1f),
                )
                SummaryMetricChip(
                    value = DateUtils.toPersianDigits(pausedCount.toString()),
                    label = "متوقف",
                    accent = DfThemeColors.textMuted(),
                    background = DfThemeColors.surfaceVariant(),
                    modifier = Modifier.weight(1f),
                )
                SummaryMetricChip(
                    value = DateUtils.toPersianDigits(totalRuns.toString()),
                    label = "کل اجرا",
                    accent = DfThemeColors.primary(),
                    background = DfThemeColors.primaryContainer(),
                    modifier = Modifier.weight(1f),
                )
            }

            when {
                runningCount > 0 -> {
                    DfStatusBanner(
                        message = "${DateUtils.toPersianDigits(runningCount.toString())} زمان‌بندی در حال اجراست؛ نتیجه پس از اتمام در فایلینگ ظاهر می‌شود.",
                        tone = DfStatusTone.Info,
                        icon = DfIcons.RefreshCw,
                    )
                }
                nextSchedule != null -> {
                    val nextLabel = scheduleNextRunLabel(nextSchedule.nextRunAt!!) ?: "به‌زودی"
                    DfStatusBanner(
                        message = "اجرای بعدی «${nextSchedule.title}»: $nextLabel",
                        tone = DfStatusTone.Info,
                        icon = DfIcons.AlarmClock,
                    )
                }
                else -> {
                    DfStatusBanner(
                        message = "اعلان‌ها را فعال نگه دارید تا از اتمام هر استخراج باخبر شوید.",
                        tone = DfStatusTone.Info,
                        icon = DfIcons.Smartphone,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricChip(
    value: String,
    label: String,
    accent: Color,
    background: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(background, AppShapes.CardSmall)
            .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = AppTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
            Text(
                text = label,
                style = AppTypography.labelSmall,
                color = DfThemeColors.textSecondary(),
            )
        }
    }
}
