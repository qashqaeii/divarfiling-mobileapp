package ir.divarfiling.mobile.feature.notifications

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfNotificationItem
import ir.divarfiling.mobile.core.design.components.DfNotificationListSkeleton
import ir.divarfiling.mobile.core.design.components.DfNotificationSection
import ir.divarfiling.mobile.core.design.components.DfNotificationSectionKind
import ir.divarfiling.mobile.core.design.components.DfNotificationToolbar
import ir.divarfiling.mobile.core.design.components.DfNotificationUnreadBadge
import ir.divarfiling.mobile.core.design.components.DfNotificationVisual
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
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
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val updateViewModel: AppUpdateViewModel = hiltViewModel(activity)
    val updateState by updateViewModel.uiState.collectAsStateWithLifecycle()
    val usesInAppApkUpdate = UpdateDistribution.usesInAppApkUpdate

    Scaffold(
        containerColor = DfScreenContainerColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                NotificationsPageHeader(
                    userName = state.userName,
                    unreadCount = state.unreadCount,
                    onBack = onBack,
                )

                when {
                    state.isLoading -> {
                        DfNotificationListSkeleton(
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                    state.error != null && state.items.isEmpty() -> {
                        Column(
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                        ) {
                            DfStatusBanner(
                                message = state.error!!,
                                tone = DfStatusTone.Error,
                            )
                            DfEmptyState(
                                title = "بارگذاری ناموفق",
                                subtitle = "اتصال را بررسی کنید و دوباره تلاش کنید.",
                                variant = DfEmptyVariant.Error,
                                actionLabel = "تلاش مجدد",
                                onAction = viewModel::refresh,
                            )
                        }
                    }
                    state.items.isEmpty() -> {
                        DfEmptyState(
                            title = "فعلاً اعلانی ندارید",
                            subtitle = "یادآورها، فایل‌ها و پیگیری‌ها همین‌جا می‌آیند.",
                            variant = DfEmptyVariant.Empty,
                            icon = DfIcons.Bell,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                    else -> {
                        val actionItems = state.items.filter { it.needsAction }
                        val unreadItems = state.items.filter { !it.isRead && !it.needsAction }
                        val olderItems = state.items.filter { it.isRead }

                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = AppSpacing.screenHorizontal,
                                end = AppSpacing.screenHorizontal,
                                top = AppSpacing.xs,
                                bottom = AppSpacing.xxl,
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (state.error != null) {
                                item {
                                    DfStatusBanner(
                                        message = state.error!!,
                                        tone = DfStatusTone.Error,
                                    )
                                }
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
                                    )
                                }
                            }
                            if (state.unreadCount > 0 || state.isMarkingAllRead) {
                                item {
                                    DfNotificationToolbar(
                                        unreadCount = state.unreadCount,
                                        isMarkingAllRead = state.isMarkingAllRead,
                                        onMarkAllRead = viewModel::markAllRead,
                                    )
                                }
                            }
                            notificationGroup(
                                title = "نیاز به اقدام",
                                kind = DfNotificationSectionKind.Action,
                                groupItems = actionItems,
                                viewModel = viewModel,
                                onDeepLink = onDeepLink,
                            )
                            notificationGroup(
                                title = "جدید و خوانده‌نشده",
                                kind = DfNotificationSectionKind.Unread,
                                groupItems = unreadItems,
                                viewModel = viewModel,
                                onDeepLink = onDeepLink,
                            )
                            notificationGroup(
                                title = "قدیمی‌تر",
                                kind = DfNotificationSectionKind.Older,
                                groupItems = olderItems,
                                viewModel = viewModel,
                                onDeepLink = onDeepLink,
                            )
                            if (state.hasMore) {
                                item {
                                    DfSecondaryButton(
                                        text = if (state.isLoadingMore) "در حال بارگذاری…" else "بیشتر",
                                        onClick = viewModel::loadMore,
                                        enabled = !state.isLoadingMore,
                                        loading = state.isLoadingMore,
                                    )
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
private fun NotificationsPageHeader(
    userName: String,
    unreadCount: Int,
    onBack: () -> Unit,
) {
    DfHubPageHeader(
        title = "اعلان‌ها",
        subtitle = "یادآورها، فایل‌ها و پیگیری‌های شما",
        sectionLabel = DfHeaderSections.NOTIFICATIONS,
        titleIconRes = DfDecorIcons.Bell,
        userName = userName.takeIf { it.isNotBlank() },
        onBack = onBack,
        toolbarContent = {
            DfNotificationUnreadBadge(count = unreadCount)
        },
    )
}

private fun LazyListScope.notificationGroup(
    title: String,
    kind: DfNotificationSectionKind,
    groupItems: List<NotificationListItem>,
    viewModel: NotificationsViewModel,
    onDeepLink: (DeepLinkTarget) -> Unit,
) {
    if (groupItems.isEmpty()) return
    item(key = "section-$title") {
        DfNotificationSection(
            title = title,
            count = groupItems.size,
            kind = kind,
            modifier = Modifier.padding(top = if (kind == DfNotificationSectionKind.Action) 4.dp else 8.dp),
        )
    }
    items(groupItems, key = { "$title-${it.id}" }) { item ->
        NotificationListRow(
            item = item,
            kind = kind,
            onClick = {
                val deepLink = viewModel.markReadAndReturnDeepLink(item.id)
                deepLink?.let { link ->
                    DeepLinkParser.parse(Uri.parse(link))?.let(onDeepLink)
                }
            },
        )
    }
}

@Composable
private fun NotificationListRow(
    item: NotificationListItem,
    kind: DfNotificationSectionKind,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfNotificationItem(
        title = item.title,
        body = item.body,
        timeAgo = item.timeAgo,
        visual = notificationVisualFor(item.type, item.rawType, unread = !item.isRead),
        isRead = item.isRead,
        kind = kind,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
@ReadOnlyComposable
private fun notificationVisualFor(
    type: HomeNotificationType,
    rawType: String?,
    unread: Boolean,
): DfNotificationVisual {
    val dark = DfThemeColors.isDark()
    val key = rawType?.lowercase().orEmpty()
    val teal = if (dark) Color(0xFF2DD4BF) else Color(0xFF0D9488)
    val tealContainer = if (dark) Color(0xFF134E4A) else Color(0xFFCCFBF1)
    val indigo = if (dark) Color(0xFF818CF8) else Color(0xFF4F46E5)
    val indigoContainer = if (dark) Color(0xFF312E81) else Color(0xFFEEF2FF)

    val resolvedType = when (key) {
        "support_reply" -> HomeNotificationType.Support
        "welcome", "announcement", "app_update", "product_update" -> HomeNotificationType.Announcement
        else -> type
    }

    val icon = when {
        key == "reminder_visit" -> DfIcons.Calendar
        key == "reminder_call" || resolvedType == HomeNotificationType.FollowUp -> DfIcons.Phone
        resolvedType == HomeNotificationType.ExtractSuccess -> DfIcons.Download
        resolvedType == HomeNotificationType.NewMatch -> DfIcons.Star
        resolvedType == HomeNotificationType.PriceDrop -> DfIcons.TrendingDown
        resolvedType == HomeNotificationType.License -> DfIcons.TriangleAlert
        resolvedType == HomeNotificationType.Support -> DfIcons.MessageCircle
        else -> DfIcons.Bell
    }

    return when (resolvedType) {
        HomeNotificationType.FollowUp -> DfNotificationVisual(
            icon = icon,
            accent = DfThemeColors.warning(),
            container = DfThemeColors.warningContainer(),
        )
        HomeNotificationType.NewMatch -> DfNotificationVisual(
            icon = icon,
            accent = DfThemeColors.success(),
            container = DfThemeColors.successContainer(),
        )
        HomeNotificationType.ExtractSuccess -> DfNotificationVisual(
            icon = icon,
            accent = indigo,
            container = indigoContainer,
        )
        HomeNotificationType.License -> DfNotificationVisual(
            icon = icon,
            accent = if (unread) DfThemeColors.error() else DfThemeColors.warning(),
            container = if (unread) DfThemeColors.errorContainer() else DfThemeColors.warningContainer(),
        )
        HomeNotificationType.PriceDrop -> DfNotificationVisual(
            icon = icon,
            accent = DfThemeColors.warning(),
            container = DfThemeColors.warningContainer(),
        )
        HomeNotificationType.Support -> DfNotificationVisual(
            icon = icon,
            accent = teal,
            container = tealContainer,
        )
        HomeNotificationType.Announcement -> DfNotificationVisual(
            icon = icon,
            accent = DfThemeColors.primary(),
            container = DfThemeColors.primaryContainer(),
        )
        HomeNotificationType.General -> DfNotificationVisual(
            icon = icon,
            accent = DfThemeColors.textSecondary(),
            container = DfThemeColors.surfaceVariant(),
        )
    }
}

@Preview(showBackground = true, widthDp = 320, name = "Notifications 320")
@Preview(showBackground = true, widthDp = 360, name = "Notifications 360")
@Preview(showBackground = true, widthDp = 390, name = "Notifications 390")
@Preview(showBackground = true, widthDp = 430, name = "Notifications 430")
@Composable
private fun NotificationsContentPreview() {
    DivarFilingTheme {
        Column {
            NotificationsPageHeader(userName = "حسین", unreadCount = 15, onBack = {})
            Column(
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DfNotificationToolbar(unreadCount = 8, isMarkingAllRead = false, onMarkAllRead = {})
                DfNotificationSection("نیاز به اقدام", 2, DfNotificationSectionKind.Action)
                NotificationListRow(
                    item = NotificationListItem(
                        id = 1,
                        title = "چند تا پیگیری مونده",
                        body = "۱ مشتری منتظر پیگیریه.",
                        timeAgo = "۲ ساعت پیش",
                        type = HomeNotificationType.FollowUp,
                        rawType = "overdue_followup",
                        isRead = false,
                        deepLink = null,
                    ),
                    kind = DfNotificationSectionKind.Action,
                    onClick = {},
                )
                DfNotificationSection("جدید و خوانده‌نشده", 1, DfNotificationSectionKind.Unread)
                NotificationListRow(
                    item = NotificationListItem(
                        id = 2,
                        title = "استخراج تمام شد",
                        body = "پوشه جردن آماده مشاهده است.",
                        timeAgo = "۴۵ دقیقه پیش",
                        type = HomeNotificationType.ExtractSuccess,
                        rawType = "extract_complete",
                        isRead = false,
                        deepLink = null,
                    ),
                    kind = DfNotificationSectionKind.Unread,
                    onClick = {},
                )
            }
        }
    }
}
