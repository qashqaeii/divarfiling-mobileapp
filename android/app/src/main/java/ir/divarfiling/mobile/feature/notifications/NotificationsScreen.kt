package ir.divarfiling.mobile.feature.notifications

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import ir.divarfiling.mobile.core.design.AppColors
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfNotificationListSkeleton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfTextButton
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.update.UpdateDistribution
import ir.divarfiling.mobile.feature.home.HomeNotificationType
import ir.divarfiling.mobile.feature.update.AppUpdateInlineBanner
import ir.divarfiling.mobile.feature.update.AppUpdatePhase
import ir.divarfiling.mobile.feature.update.AppUpdateViewModel
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as ComponentActivity
    val updateViewModel: AppUpdateViewModel = hiltViewModel(activity)
    val updateState by updateViewModel.uiState.collectAsStateWithLifecycle()
    val usesInAppApkUpdate = UpdateDistribution.usesInAppApkUpdate

    Scaffold(
        containerColor = DfScreenContainerColor,
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            when {
                state.isLoading -> {
                    Column {
                        DfHubPageHeader(
                            title = "اعلان‌ها",
                            subtitle = "یادآورها، استخراج‌ها و پیگیری‌های شما",
                            sectionLabel = DfHeaderSections.NOTIFICATIONS,
                            titleIconRes = DfDecorIcons.Bell,
                            onBack = onBack,
                        )
                        DfNotificationListSkeleton(
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                state.error != null && state.items.isEmpty() -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                    ) {
                        DfHubPageHeader(
                            title = "اعلان‌ها",
                            subtitle = "یادآورها، استخراج‌ها و پیگیری‌های شما",
                            sectionLabel = DfHeaderSections.NOTIFICATIONS,
                            titleIconRes = DfDecorIcons.Bell,
                            onBack = onBack,
                        )
                        DfStatusBanner(
                            message = state.error!!,
                            tone = DfStatusTone.Error,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                        DfEmptyState(
                            title = "بارگذاری ناموفق",
                            subtitle = "اتصال را بررسی کنید و دوباره تلاش کنید.",
                            variant = DfEmptyVariant.Error,
                            actionLabel = "تلاش مجدد",
                            onAction = viewModel::refresh,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                state.items.isEmpty() -> {
                    Column {
                        DfHubPageHeader(
                            title = "اعلان‌ها",
                            subtitle = "یادآورها، استخراج‌ها و پیگیری‌های شما",
                            sectionLabel = DfHeaderSections.NOTIFICATIONS,
                            titleIconRes = DfDecorIcons.Bell,
                            onBack = onBack,
                        )
                        DfEmptyState(
                            title = "اعلانی ندارید",
                            subtitle = "یادآورها، استخراج‌ها و پیگیری‌ها اینجا نمایش داده می‌شوند",
                            variant = DfEmptyVariant.Empty,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                    ) {
                        item {
                            DfHubPageHeader(
                                title = if (state.unreadCount > 0) {
                                    "اعلان‌ها (${state.unreadCount})"
                                } else {
                                    "اعلان‌ها"
                                },
                            subtitle = "یادآورها، استخراج‌ها و پیگیری‌های شما",
                            sectionLabel = DfHeaderSections.NOTIFICATIONS,
                            titleIconRes = DfDecorIcons.Bell,
                                onBack = onBack,
                                bottomContent = if (state.unreadCount > 0) {
                                    {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = AppSpacing.screenHorizontal),
                                            horizontalArrangement = Arrangement.End,
                                        ) {
                                            DfTextButton(
                                                text = if (state.isMarkingAllRead) {
                                                    "در حال ثبت…"
                                                } else {
                                                    "همه خوانده شد"
                                                },
                                                onClick = {
                                                    if (!state.isMarkingAllRead) {
                                                        viewModel.markAllRead()
                                                    }
                                                },
                                            )
                                        }
                                    }
                                } else {
                                    null
                                },
                            )
                        }
                        if (usesInAppApkUpdate && updateState.visible && updateState.phase != AppUpdatePhase.UpToDate) {
                            item {
                                AppUpdateInlineBanner(
                                    state = updateState,
                                    onPrimaryClick = {
                                        when (updateState.phase) {
                                            AppUpdatePhase.Available, AppUpdatePhase.Error -> updateViewModel.startUpdate()
                                            AppUpdatePhase.ReadyToInstall -> updateViewModel.installNow()
                                            AppUpdatePhase.AwaitingInstallPermission ->
                                                context.startActivity(updateViewModel.openInstallPermissionSettings())
                                            else -> Unit
                                        }
                                    },
                                    onOpenStore = { url ->
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    },
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                )
                            }
                        }
                        val critical = state.items.filter {
                            !it.isRead && (it.type == HomeNotificationType.License || it.type == HomeNotificationType.FollowUp)
                        }
                        val unread = state.items.filter { !it.isRead && it !in critical }
                        val read = state.items.filter { it.isRead }
                        notificationGroup("نیاز به اقدام", critical, viewModel, onDeepLink)
                        notificationGroup("خوانده‌نشده", unread, viewModel, onDeepLink)
                        notificationGroup("قبلی", read, viewModel, onDeepLink)
                        if (state.hasMore) {
                            item {
                                DfSecondaryButton(
                                    text = if (state.isLoadingMore) "در حال بارگذاری…" else "بیشتر",
                                    onClick = viewModel::loadMore,
                                    enabled = !state.isLoadingMore,
                                    loading = state.isLoadingMore,
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun LazyListScope.notificationGroup(
    title: String,
    groupItems: List<NotificationListItem>,
    viewModel: NotificationsViewModel,
    onDeepLink: (DeepLinkTarget) -> Unit,
) {
    if (groupItems.isEmpty()) return
    item {
        Box(Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
            DfSectionHeader(title, groupItems.size)
        }
    }
    items(groupItems, key = { "$title-${it.id}" }) { item ->
        NotificationListRow(
            item = item,
            onClick = {
                val deepLink = viewModel.markReadAndReturnDeepLink(item.id)
                deepLink?.let { link ->
                    DeepLinkParser.parse(Uri.parse(link))?.let(onDeepLink)
                }
            },
            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
        )
    }
}

@Composable
private fun NotificationListRow(
    item: NotificationListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (icon, tint, bg) = notificationStyle(item.type)
    DfCard(
        onClick = onClick,
        modifier = modifier,
        containerColor = if (item.isRead) {
            DfThemeColors.surface()
        } else {
            DfThemeColors.primaryContainer().copy(alpha = 0.35f)
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.iconTextGap),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(AppShapes.IconContainer)
                    .background(bg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        item.title,
                        style = AppTypography.cardTitle,
                        fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.SemiBold,
                        color = DfThemeColors.textPrimary(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(AppShapes.Avatar)
                                .background(DfThemeColors.primary()),
                        )
                    }
                }
                if (item.body.isNotBlank()) {
                    Text(
                        item.body,
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    item.timeAgo,
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
            }
        }
    }
}

private fun notificationStyle(type: HomeNotificationType): Triple<ImageVector, Color, Color> = when (type) {
    HomeNotificationType.ExtractSuccess -> Triple(DfIcons.Download, AppColors.Blue, AppColors.BlueLight)
    HomeNotificationType.NewMatch -> Triple(DfIcons.Star, AppColors.Green, AppColors.GreenLight)
    HomeNotificationType.PriceDrop -> Triple(DfIcons.TrendingDown, AppColors.Amber, AppColors.AmberLight)
    HomeNotificationType.License -> Triple(DfIcons.Sparkles, AppColors.Purple, AppColors.PurpleContainer)
    HomeNotificationType.FollowUp -> Triple(DfIcons.Phone, AppColors.Rose, AppColors.RoseLight)
    HomeNotificationType.General -> Triple(DfIcons.Home, AppColors.Purple, AppColors.PurpleContainer)
}
