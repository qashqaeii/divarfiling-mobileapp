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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfEmptyState
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
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        DfSectionTitle(
            title = "کارهای امروز",
            badge = if (tasks.isNotEmpty()) tasks.size.toString() else null,
            actionLabel = "مشاهده همه",
            onAction = onViewAll,
        )

        if (isLoading) {
            DfShimmerBox(modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 180.dp))
            return
        }

        DfPremiumCard {
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
                if (tasks.size >= 3) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = AppSpacing.xs),
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
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AppSpacing.listRowMinHeight),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.iconTextGap),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(AppShapes.IconContainer)
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap),
        ) {
            Text(
                text = task.title,
                style = AppTypography.cardTitle,
                color = DfColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = task.subtitle,
                style = AppTypography.bodyDescription,
                color = DfColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = task.time,
            style = AppTypography.timeLabel,
            color = DfColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(44.dp),
        )
    }
}

private fun taskTypeStyle(type: HomeTaskType): Triple<ImageVector, Color, Color> = when (type) {
    HomeTaskType.Call -> Triple(DfIcons.Phone, DfColors.Green, DfColors.GreenLight)
    HomeTaskType.Visit -> Triple(DfIcons.Calendar, DfColors.Blue, DfColors.BlueLight)
    HomeTaskType.FollowUp -> Triple(DfIcons.User, DfColors.Amber, DfColors.AmberLight)
    HomeTaskType.Reminder -> Triple(DfIcons.Bell, DfColors.Purple, DfColors.PurpleContainer)
}

@Preview(showBackground = true, widthDp = 360)
@Preview(showBackground = true, widthDp = 390)
@Preview(showBackground = true, widthDp = 412)
@Composable
private fun TodayTasksSectionPreview() {
    DivarFilingTheme {
        TodayTasksSection(
            tasks = listOf(
                HomeTaskItem(
                    id = "1",
                    time = "09:00",
                    title = "تماس با رضا احمدی",
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
