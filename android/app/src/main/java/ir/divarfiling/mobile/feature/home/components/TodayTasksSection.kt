package ir.divarfiling.mobile.feature.home.components

import ir.divarfiling.mobile.core.design.DfColors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
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
    if (isLoading) {
        DfShimmerBox(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenHorizontal)
                .defaultMinSize(minHeight = 180.dp),
        )
        return
    }

    HomeDashboardCard(
        title = "کارهای امروز",
        iconRes = DfDecorIcons.ListTodo,
        expanded = true,
        onToggle = {},
        footerLabel = "مشاهده همه کارها (${tasks.size.coerceAtLeast(0)})",
        onFooterClick = onViewAll,
        modifier = modifier,
    ) {
        TodayTasksSectionContent(tasks = tasks, onViewAll = onViewAll)
    }
}

@Composable
fun TodayTasksSectionContent(
    tasks: List<HomeTaskItem>,
    onViewAll: () -> Unit,
) {
    if (tasks.isEmpty()) {
        DfEmptyState(
            title = "کار امروز ندارید",
            subtitle = "یادآورها و پیگیری‌های سررسید اینجا نمایش داده می‌شوند",
            actionLabel = "مشاهده همه",
            onAction = onViewAll,
        )
    } else {
        tasks.forEachIndexed { index, task ->
            TodayTaskRow(task)
            if (index < tasks.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = AppSpacing.xs),
                    color = DfColors.OutlineSubtle,
                )
            }
        }
    }
}

@Composable
private fun TodayTaskRow(task: HomeTaskItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.iconTextGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(AppShapes.Chip)
                .background(DfColors.Green),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = DfIcons.Check,
                contentDescription = null,
                tint = DfColors.Surface,
                modifier = Modifier.size(14.dp),
            )
        }

        Text(
            text = task.title,
            style = AppTypography.bodyDescription,
            color = DfColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = task.time,
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(max = 96.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun TodayTasksSectionPreview() {
    DivarFilingTheme {
        TodayTasksSection(
            tasks = listOf(
                HomeTaskItem(
                    id = "1",
                    time = "09:00",
                    title = "پیگیری مشتریان جدید",
                    subtitle = "خریدار — منطقه ونک",
                    type = HomeTaskType.Call,
                ),
                HomeTaskItem(
                    id = "2",
                    time = "11:30",
                    title = "بازدید ملک در سعادت‌آباد",
                    subtitle = "فروش آپارتمان ۱۲۰ متر",
                    type = HomeTaskType.Visit,
                ),
            ),
            isLoading = false,
            onViewAll = {},
        )
    }
}
