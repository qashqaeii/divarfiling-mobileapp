package ir.divarfiling.mobile.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfCompactStat
import ir.divarfiling.mobile.core.design.components.DfCompactStatBar
import ir.divarfiling.mobile.core.design.components.DfSectionTitle
import ir.divarfiling.mobile.core.design.components.DfShimmerBox
import ir.divarfiling.mobile.feature.home.DashboardStats

@Composable
fun StatsSection(
    stats: DashboardStats,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        DfSectionTitle(title = "نمای کلی")
        if (isLoading) {
            DfShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            )
            return
        }
        DfCompactStatBar(
            stats = listOf(
                DfCompactStat("باقی‌مانده", DateUtils.toPersianDigits(stats.todayTasksRemaining.toString())),
                DfCompactStat("انجام‌شده", DateUtils.toPersianDigits(stats.todayTasksDone.toString())),
                DfCompactStat("یادآور", DateUtils.toPersianDigits(stats.activeReminders.toString())),
                DfCompactStat("مخاطب", DateUtils.toPersianDigits(stats.contacts.toString())),
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun StatsSectionPreview() {
    DivarFilingTheme {
        StatsSection(
            stats = DashboardStats(
                todayTasksDone = 12,
                todayTasksRemaining = 8,
                todayTasksTotal = 20,
                dailyProgressPercent = 60,
                tasksDoneDelta = 3,
                activeReminders = 4,
                contacts = 48,
            ),
            isLoading = false,
        )
    }
}
