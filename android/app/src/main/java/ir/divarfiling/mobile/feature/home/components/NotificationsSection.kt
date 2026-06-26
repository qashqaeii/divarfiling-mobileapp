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
import ir.divarfiling.mobile.feature.home.HomeNotificationItem
import ir.divarfiling.mobile.feature.home.HomeNotificationType

@Composable
fun NotificationsSection(
    notifications: List<HomeNotificationItem>,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (notifications.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DfSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(DfSpacing.sm),
    ) {
        DfSectionTitle(
            title = "اعلان‌ها",
            actionLabel = "مشاهده همه",
            onAction = onViewAll,
        )
        DfPremiumCard {
            notifications.forEachIndexed { index, item ->
                NotificationRow(item)
                if (index < notifications.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DfSpacing.xs),
                        color = DfColors.OutlineSubtle,
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(item: HomeNotificationItem) {
    val (icon, tint, bg) = notificationStyle(item.type)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DfSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
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
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = DfColors.TextPrimary,
            )
            Text(
                text = item.timeAgo,
                style = MaterialTheme.typography.labelSmall,
                color = DfColors.TextMuted,
                modifier = Modifier.padding(top = 2.dp),
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
    HomeNotificationType.General -> Triple(DfIcons.Bell, DfColors.TextSecondary, DfColors.SurfaceVariant)
}

@Preview(showBackground = true)
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
