package ir.divarfiling.mobile.feature.extract.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfGlassCard
import ir.divarfiling.mobile.core.network.ExtractionScheduleDto

@Composable
fun ScheduleSummaryHero(
    schedules: List<ExtractionScheduleDto>,
    modifier: Modifier = Modifier,
) {
    val activeCount = schedules.count { it.isEnabled }
    val pausedCount = schedules.size - activeCount
    val totalRuns = schedules.sumOf { it.runCount }

    DfGlassCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "پایش خودکار",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.TextPrimary,
                    )
                    Text(
                        text = "فیلترهای ذخیره‌شده روی این دستگاه اجرا می‌شوند",
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    DfColors.PurpleGradientStart.copy(alpha = 0.85f),
                                    DfColors.PurpleGradientEnd,
                                ),
                            ),
                            shape = AppShapes.IconContainer,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = DfIcons.Timer,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                SummaryMetricCard(
                    value = DateUtils.toPersianDigits(activeCount.toString()),
                    label = "فعال",
                    accent = DfColors.Green,
                    background = DfColors.GreenLight,
                    modifier = Modifier.weight(1f),
                )
                SummaryMetricCard(
                    value = DateUtils.toPersianDigits(pausedCount.toString()),
                    label = "متوقف",
                    accent = DfColors.TextMuted,
                    background = DfColors.SurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                SummaryMetricCard(
                    value = DateUtils.toPersianDigits(totalRuns.toString()),
                    label = "کل اجرا",
                    accent = DfColors.Purple,
                    background = DfColors.PurpleContainer,
                    modifier = Modifier.weight(1f),
                )
            }

            Surface(
                shape = AppShapes.Chip,
                color = DfColors.BlueLight.copy(alpha = 0.65f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = DfIcons.Smartphone,
                        contentDescription = null,
                        tint = DfColors.Blue,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "اعلان‌ها را فعال نگه دارید تا از اتمام هر استخراج باخبر شوید.",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(
    value: String,
    label: String,
    accent: Color,
    background: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.Card,
        color = background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
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
                color = DfColors.TextSecondary,
            )
        }
    }
}
