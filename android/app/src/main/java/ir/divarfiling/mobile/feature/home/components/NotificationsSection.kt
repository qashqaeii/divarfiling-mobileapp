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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfSectionTitle
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        DfSectionTitle(
            title = "اعلان‌ها",
            actionLabel = "مشاهده همه",
            onAction = onViewAll,
        )
        DfPremiumCard {
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
    }
}

@Composable
private fun NotificationRow(item: HomeNotificationItem, onClick: () -> Unit) {
    val (icon, tint, bg) = notificationStyle(item.type)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                text = item.title,
                style = AppTypography.cardTitle,
                color = DfColors.TextPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.timeAgo,
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun notificationStyle(type: HomeNotificationType): Triple<ImageVector, Color, Color> = when (type) {
    HomeNotificationType.ExtractSuccess -> Triple(DfIcons.Download, DfColors.Blue, DfColors.BlueLight)
    HomeNotificationType.NewMatch -> Triple(DfIcons.Star, DfColors.Green, DfColors.GreenLight)
    HomeNotificationType.PriceDrop -> Triple(DfIcons.TrendingDown, DfColors.Amber, DfColors.AmberLight)
    HomeNotificationType.License -> Triple(DfIcons.Sparkles, DfColors.Purple, DfColors.PurpleContainer)
    HomeNotificationType.FollowUp -> Triple(DfIcons.Phone, DfColors.Rose, DfColors.RoseLight)
    HomeNotificationType.General -> Triple(DfIcons.Home, DfColors.Purple, DfColors.PurpleContainer)
}

@Preview(showBackground = true, widthDp = 360)
@Preview(showBackground = true, widthDp = 390)
@Preview(showBackground = true, widthDp = 412)
@Composable
private fun NotificationsSectionPreview() {
    DivarFilingTheme {
        NotificationsSection(
            notifications = listOf(
                HomeNotificationItem("1", "۳ فایل جدید مناسب برای مشتری شما پیدا شد", "۲ دقیقه پیش", HomeNotificationType.NewMatch),
                HomeNotificationItem("2", "استخراج شماره ۲۴۵ با موفقیت انجام شد", "۱ ساعت پیش", HomeNotificationType.ExtractSuccess),
            ),
            onViewAll = {},
        )
    }
}
