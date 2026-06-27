package ir.divarfiling.mobile.feature.notifications

import ir.divarfiling.mobile.core.design.DfColors

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfNotificationListSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfTopBar
import ir.divarfiling.mobile.feature.home.HomeNotificationType
import ir.divarfiling.mobile.navigation.DeepLinkParser
import ir.divarfiling.mobile.navigation.DeepLinkTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onDeepLink: (DeepLinkTarget) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            DfTopBar(
                title = if (state.unreadCount > 0) "اعلان‌ها (${state.unreadCount})" else "اعلان‌ها",
                onBack = onBack,
            )
        },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading -> {
                    DfNotificationListSkeleton()
                }
                state.error != null && state.items.isEmpty() -> {
                    Column(Modifier.padding(16.dp)) {
                        DfErrorBanner(state.error!!)
                    }
                }
                state.items.isEmpty() -> {
                    DfEmptyState(
                        title = "اعلانی ندارید",
                        subtitle = "یادآورها، استخراج‌ها و پیگیری‌ها اینجا نمایش داده می‌شوند",
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.items, key = { it.id }) { item ->
                            NotificationListRow(
                                item = item,
                                onClick = {
                                    val deepLink = viewModel.markReadAndReturnDeepLink(item.id)
                                    deepLink?.let { link ->
                                        DeepLinkParser.parse(Uri.parse(link))?.let(onDeepLink)
                                    }
                                },
                            )
                        }
                        if (state.hasMore) {
                            item {
                                TextButton(
                                    onClick = viewModel::loadMore,
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !state.isLoadingMore,
                                ) {
                                    Text(if (state.isLoadingMore) "در حال بارگذاری…" else "بیشتر")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationListRow(
    item: NotificationListItem,
    onClick: () -> Unit,
) {
    val (icon, tint, bg) = notificationStyle(item.type)
    DfPremiumCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.iconTextGap),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(AppShapes.IconContainer)
                    .background(bg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        item.title,
                        style = AppTypography.cardTitle,
                        fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(AppShapes.IconContainer)
                                .background(DfColors.Purple),
                        )
                    }
                }
                if (item.body.isNotBlank()) {
                    Text(
                        item.body,
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    item.timeAgo,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
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
