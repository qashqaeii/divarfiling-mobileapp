package ir.divarfiling.mobile.feature.home.components

import ir.divarfiling.mobile.core.design.DfColors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.feature.home.HomeNotificationItem
import ir.divarfiling.mobile.feature.home.HomeNotificationType

@Composable
fun NotificationsSection(
    notifications: List<HomeNotificationItem>,
    onViewAll: () -> Unit,
    onNotificationClick: (HomeNotificationItem) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (notifications.isEmpty()) return

    HomeDashboardCard(
        title = "اعلان‌ها",
        iconRes = DfDecorIcons.Bell,
        expanded = true,
        onToggle = {},
        footerLabel = "مشاهده همه اعلان‌ها (${notifications.size})",
        onFooterClick = onViewAll,
        modifier = modifier,
    ) {
        NotificationsSectionContent(
            notifications = notifications,
            onNotificationClick = onNotificationClick,
        )
    }
}

@Composable
fun NotificationsSectionContent(
    notifications: List<HomeNotificationItem>,
    onNotificationClick: (HomeNotificationItem) -> Unit = {},
) {
    notifications.forEachIndexed { index, item ->
        NotificationRow(item, onClick = { onNotificationClick(item) })
        if (index < notifications.lastIndex) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = AppSpacing.xs),
                color = DfColors.OutlineSubtle,
            )
        }
    }
}

@Composable
private fun NotificationRow(item: HomeNotificationItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = AppSpacing.listRowMinHeight),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.iconTextGap),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = item.timeAgo,
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(72.dp),
        )

        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .background(DfColors.Purple, CircleShape),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap),
        ) {
            Text(
                text = item.body?.takeIf { it.isNotBlank() } ?: item.title,
                style = AppTypography.bodyDescription,
                color = DfColors.TextPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun NotificationsSectionPreview() {
    DivarFilingTheme {
        NotificationsSection(
            notifications = listOf(
                HomeNotificationItem(
                    "1",
                    "فایل جدید",
                    "۴۵ دقیقه پیش",
                    HomeNotificationType.ExtractSuccess,
                    body = "فایل جدید جردن - فروش با موفقیت استخراج شد",
                ),
                HomeNotificationItem(
                    "2",
                    "استخراج موفق",
                    "۱ ساعت پیش",
                    HomeNotificationType.ExtractSuccess,
                    body = "استخراج شماره ۲۴۵ با موفقیت انجام شد",
                ),
            ),
            onViewAll = {},
        )
    }
}
