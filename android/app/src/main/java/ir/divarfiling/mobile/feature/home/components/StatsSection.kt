package ir.divarfiling.mobile.feature.home.components

import ir.divarfiling.mobile.core.design.DfColors

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfAnimation
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfAnimatedCounter
import ir.divarfiling.mobile.core.design.components.DfShimmerBox
import ir.divarfiling.mobile.feature.home.DashboardStats
import java.util.Locale

@Composable
fun StatsSection(
    stats: DashboardStats,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val hoursSpent = formatHoursSpent(stats.todayTasksDone, stats.activeReminders)
    val cards = listOf(
        OverviewStatData(
            displayValue = hoursSpent,
            label = "ساعت صرف شده",
            footer = "امروز",
            icon = DfIcons.Clock,
            tint = DfColors.Amber,
            background = DfColors.AmberLight,
            showProgress = false,
            progress = 0f,
        ),
        OverviewStatData(
            displayValue = "${stats.dailyProgressPercent}٪",
            label = "پیشرفت روزانه",
            footer = null,
            icon = DfIcons.RefreshCw,
            tint = DfColors.Blue,
            background = DfColors.BlueLight,
            showProgress = true,
            progress = stats.dailyProgressPercent / 100f,
        ),
        OverviewStatData(
            displayValue = stats.todayTasksRemaining.toString(),
            label = "کارهای باقی‌مانده",
            footer = "امروز",
            icon = DfIcons.ListTodo,
            tint = DfColors.Purple,
            background = DfColors.PurpleContainer,
            showProgress = false,
            progress = 0f,
        ),
        OverviewStatData(
            displayValue = stats.todayTasksDone.toString(),
            label = "کارهای انجام شده",
            footer = if (stats.tasksDoneDelta > 0) "+${stats.tasksDoneDelta} نسبت به دیروز" else "امروز",
            icon = DfIcons.CircleCheck,
            tint = DfColors.Green,
            background = DfColors.GreenLight,
            showProgress = false,
            progress = 0f,
            footerTint = DfColors.Green,
        ),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        HomeSectionTitle(title = "نمای کلی امروز")

        if (isLoading) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                repeat(4) {
                    DfShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp),
                    )
                }
            }
            return
        }

        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            cards.forEach { card ->
                OverviewStatCard(
                    data = card,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private data class OverviewStatData(
    val displayValue: String,
    val label: String,
    val footer: String?,
    val icon: ImageVector,
    val tint: Color,
    val background: Color,
    val showProgress: Boolean,
    val progress: Float,
    val footerTint: Color = DfColors.TextMuted,
)

@Composable
private fun OverviewStatCard(
    data: OverviewStatData,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = DfAnimation.springGentle(),
        label = "overviewStatScale",
    )
    Surface(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth(),
        shape = AppShapes.StatCard,
        color = DfColors.Surface,
        shadowElevation = AppElevations.card,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(data.background),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    data.icon,
                    contentDescription = null,
                    tint = data.tint,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (data.displayValue.any { it.isDigit() } && data.displayValue.endsWith("٪").not()) {
                    val numeric = data.displayValue.toIntOrNull()
                    if (numeric != null) {
                        DfAnimatedCounter(
                            target = numeric,
                            style = AppTypography.statNumber.copy(fontSize = AppTypography.statNumber.fontSize * 0.9f),
                            color = DfColors.TextPrimary,
                        )
                    } else {
                        Text(
                            data.displayValue,
                            style = AppTypography.statNumber.copy(fontSize = AppTypography.statNumber.fontSize * 0.9f),
                            fontWeight = FontWeight.Bold,
                            color = DfColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                } else {
                    Text(
                        data.displayValue,
                        style = AppTypography.statNumber.copy(fontSize = AppTypography.statNumber.fontSize * 0.9f),
                        fontWeight = FontWeight.Bold,
                        color = DfColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    data.label,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (data.showProgress) {
                    LinearProgressIndicator(
                        progress = { data.progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .height(4.dp)
                            .clip(AppShapes.Chip),
                        color = DfColors.Blue,
                        trackColor = DfColors.BlueLight,
                    )
                } else if (data.footer != null) {
                    Text(
                        data.footer,
                        style = AppTypography.labelSmall,
                        color = data.footerTint,
                        fontWeight = if (data.footerTint == DfColors.Green) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun formatHoursSpent(tasksDone: Int, activeReminders: Int): String {
    val hours = (tasksDone * 0.35f + activeReminders * 0.15f).coerceAtLeast(0.1f)
    return String.format(Locale.US, "%.1f", hours)
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun StatsSectionPreview() {
    DivarFilingTheme {
        StatsSection(
            stats = DashboardStats(
                todayTasksDone = 12,
                todayTasksRemaining = 8,
                dailyProgressPercent = 65,
                tasksDoneDelta = 3,
                activeReminders = 4,
            ),
            isLoading = false,
        )
    }
}
