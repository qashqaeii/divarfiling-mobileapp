package ir.divarfiling.mobile.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfShapes
import ir.divarfiling.mobile.core.design.DfSpacing
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfSectionTitle
import ir.divarfiling.mobile.core.design.components.DfShimmerBox
import ir.divarfiling.mobile.feature.home.HomeTaskItem
import ir.divarfiling.mobile.feature.home.HomeTaskType

@Composable
fun TodayTasksSection(
    tasks: List<HomeTaskItem>,
    isLoading: Boolean,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DfSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(DfSpacing.sm),
    ) {
        DfSectionTitle(
            title = "کارهای امروز",
            badge = if (tasks.isNotEmpty()) tasks.size.toString() else null,
            actionLabel = "مشاهده همه",
            onAction = onViewAll,
        )

        if (isLoading) {
            DfShimmerBox(modifier = Modifier.fillMaxWidth().size(180.dp))
            return
        }

        DfPremiumCard {
            if (tasks.isEmpty()) {
                Text(
                    text = "کار برنامه‌ریزی‌شده‌ای برای امروز ندارید",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DfColors.TextSecondary,
                )
            } else {
                tasks.forEachIndexed { index, task ->
                    TodayTaskRow(task)
                    if (index < tasks.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = DfSpacing.xs),
                            color = DfColors.OutlineSubtle,
                        )
                    }
                }
                if (tasks.size >= 3) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = DfSpacing.xs),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = DfIcons.ChevronDown,
                            contentDescription = null,
                            tint = DfColors.TextMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayTaskRow(task: HomeTaskItem) {
    val (icon, tint, bg) = taskTypeStyle(task.type)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DfSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = task.time,
            style = MaterialTheme.typography.labelMedium,
            color = DfColors.TextMuted,
            modifier = Modifier.size(width = 40.dp, height = 20.dp),
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(DfShapes.Chip)
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = DfColors.TextPrimary,
            )
            Text(
                text = task.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = DfColors.TextSecondary,
            )
        }
    }
}

private fun taskTypeStyle(type: HomeTaskType): Triple<ImageVector, Color, Color> = when (type) {
    HomeTaskType.Call -> Triple(DfIcons.Phone, DfColors.Green, DfColors.GreenLight)
    HomeTaskType.Visit -> Triple(DfIcons.Calendar, DfColors.Blue, DfColors.BlueLight)
    HomeTaskType.FollowUp -> Triple(DfIcons.User, DfColors.Amber, DfColors.AmberLight)
    HomeTaskType.Reminder -> Triple(DfIcons.Bell, DfColors.Purple, DfColors.PurpleContainer)
}

@Preview(showBackground = true)
@Composable
private fun TodayTasksSectionPreview() {
    DivarFilingTheme {
        TodayTasksSection(
            tasks = listOf(
                HomeTaskItem("1", "09:00", "تماس با رضا احمدی", "خریدار — منطقه ونک", HomeTaskType.Call),
                HomeTaskItem("2", "11:30", "بازدید ملک در سعادت‌آباد", "فروش آپارتمان ۱۲۰ متر", HomeTaskType.Visit),
            ),
            isLoading = false,
            onViewAll = {},
        )
    }
}
