package ir.divarfiling.mobile.feature.home

import ir.divarfiling.mobile.core.design.DfColors

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.feature.home.components.HomeHeader
import ir.divarfiling.mobile.feature.home.components.NotificationsSection
import ir.divarfiling.mobile.feature.home.components.QuickAction
import ir.divarfiling.mobile.feature.home.components.QuickActionsRow
import ir.divarfiling.mobile.feature.home.components.QuickExtractCard
import ir.divarfiling.mobile.feature.home.components.RecentListingsSection
import ir.divarfiling.mobile.feature.home.components.StatsSection
import ir.divarfiling.mobile.feature.home.components.SyncStatusBanner
import ir.divarfiling.mobile.feature.home.components.TodayTasksSection
import android.net.Uri
import ir.divarfiling.mobile.navigation.DeepLinkParser
import ir.divarfiling.mobile.navigation.DeepLinkTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToday: () -> Unit,
    onNavigateNotifications: () -> Unit = onNavigateToday,
    onNavigateContacts: () -> Unit,
    onNavigateFiling: () -> Unit = {},
    onNavigateExtract: () -> Unit = {},
    onNavigateCrm: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    onDatasetClick: (String) -> Unit = {},
    onNotificationDeepLink: (DeepLinkTarget) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DfPullRefresh(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier
            .fillMaxSize()
            .background(DfColors.Background)
            .statusBarsPadding(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionGap),
        ) {
            item {
                HomeHeader(
                    userName = state.userName,
                    notificationCount = state.notificationBadgeCount,
                    onSearchClick = onNavigateFiling,
                    onNotificationsClick = onNavigateNotifications,
                )
            }

            state.error?.let { error ->
                item {
                    DfErrorBanner(
                        message = error,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }

            if (state.isSyncing || state.syncPendingCount > 0 || state.lastSyncLabel != null) {
                item {
                    SyncStatusBanner(
                        isSyncing = state.isSyncing,
                        pendingCount = state.syncPendingCount,
                        lastSyncLabel = state.lastSyncLabel,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }

            if (state.isLoading) {
                item { StatsSection(stats = state.stats, isLoading = true) }
                item {
                    QuickExtractCard(
                        maxItems = state.maxExtractItems,
                        enabled = state.canExtract,
                        onStartClick = onNavigateExtract,
                    )
                }
                item { TodayTasksSection(tasks = emptyList(), isLoading = true, onViewAll = {}) }
                item { RecentListingsSection(files = emptyList(), isLoading = true, onFileClick = {}) }
            } else {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { it / 4 },
                    ) {
                        StatsSection(stats = state.stats, isLoading = false)
                    }
                }
                item {
                    QuickExtractCard(
                        maxItems = state.maxExtractItems,
                        enabled = state.canExtract,
                        onStartClick = onNavigateExtract,
                    )
                }
                item {
                    TodayTasksSection(
                        tasks = state.todayTasks,
                        isLoading = false,
                        onViewAll = onNavigateToday,
                    )
                }
                item {
                    NotificationsSection(
                        notifications = state.notifications,
                        onViewAll = onNavigateNotifications,
                        onNotificationClick = { item ->
                            item.deepLink?.let { link ->
                                DeepLinkParser.parse(Uri.parse(link))?.let(onNotificationDeepLink)
                            } ?: onNavigateNotifications()
                        },
                    )
                }
                item {
                    RecentListingsSection(
                        files = state.recentFiles,
                        isLoading = false,
                        onFileClick = onDatasetClick,
                    )
                }
                item {
                    QuickActionsRow(
                        actions = buildQuickActions(
                            onNavigateContacts = onNavigateContacts,
                            onNavigateFiling = onNavigateFiling,
                            onNavigateCrm = onNavigateCrm,
                            onNavigateToday = onNavigateToday,
                        ),
                    )
                }
            }
        }
    }
}

private fun buildQuickActions(
    onNavigateContacts: () -> Unit,
    onNavigateFiling: () -> Unit,
    onNavigateCrm: () -> Unit,
    onNavigateToday: () -> Unit,
): List<QuickAction> = listOf(
    QuickAction("نقشه", DfIcons.MapPin, DfColors.Green, DfColors.GreenLight) { onNavigateFiling() },
    QuickAction("مخاطبین", DfIcons.Users, DfColors.Purple, DfColors.PurpleContainer, onNavigateContacts),
    QuickAction("فایل‌ها", DfIcons.Folder, DfColors.Blue, DfColors.BlueLight, onNavigateFiling),
    QuickAction("مخاطب جدید", DfIcons.Plus, DfColors.Amber, DfColors.AmberLight, onNavigateContacts),
    QuickAction("یادآور جدید", DfIcons.Bell, DfColors.Pink, DfColors.PinkLight, onNavigateToday),
)

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "Home 360×800")
@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Home 390×844")
@Preview(showBackground = true, widthDp = 412, heightDp = 915, name = "Home 412×915")
@Composable
private fun HomeScreenPreview() {
    DivarFilingTheme {
        HomeScreenContentPreview()
    }
}

@Composable
internal fun HomeScreenContentPreview() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DfColors.Background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = AppSpacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionGap),
    ) {
        item {
            HomeHeader(
                userName = "حسین",
                notificationCount = 3,
                onSearchClick = {},
                onNotificationsClick = {},
            )
        }
        item {
            StatsSection(
                stats = DashboardStats(
                    newFilesToday = 5,
                    properties = 17,
                    propertiesDelta = 1,
                    deals = 20,
                    dealsDelta = 2,
                    contacts = 29,
                    contactsDelta = 3,
                ),
                isLoading = false,
            )
        }
        item {
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
        item {
            NotificationsSection(
                notifications = listOf(
                    HomeNotificationItem("1", "۳ فایل جدید مناسب برای مشتری شما پیدا شد", "۲ دقیقه پیش", HomeNotificationType.NewMatch),
                    HomeNotificationItem("2", "استخراج شماره ۲۴۵ با موفقیت انجام شد", "۱ ساعت پیش", HomeNotificationType.ExtractSuccess),
                ),
                onViewAll = {},
            )
        }
        item {
            RecentListingsSection(
                files = listOf(
                    RecentFileItem("1", "تهران", "زعفرانیه", "فروش", 369, "1405/04/01"),
                    RecentFileItem("2", "تهران", "ونک", "اجاره", 120, "1405/03/28"),
                ),
                isLoading = false,
                onFileClick = {},
            )
        }
    }
}
