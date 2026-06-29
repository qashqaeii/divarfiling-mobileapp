package ir.divarfiling.mobile.feature.home

import ir.divarfiling.mobile.core.design.DfColors

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ir.divarfiling.mobile.feature.home.components.HomeDashboardCard
import ir.divarfiling.mobile.feature.home.components.HomeHeader
import ir.divarfiling.mobile.feature.home.components.NotificationsSectionContent
import ir.divarfiling.mobile.feature.home.components.QuickAction
import ir.divarfiling.mobile.feature.home.components.QuickActionsRow
import ir.divarfiling.mobile.feature.home.components.QuickExtractCard
import ir.divarfiling.mobile.feature.home.components.RecentListingsSection
import ir.divarfiling.mobile.feature.home.components.StatsSection
import ir.divarfiling.mobile.feature.home.components.SyncStatusBanner
import ir.divarfiling.mobile.feature.home.components.TodayTasksSectionContent
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
    var todayExpanded by remember { mutableStateOf(false) }
    var notificationsExpanded by remember { mutableStateOf(false) }

    DfPullRefresh(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier
            .fillMaxSize()
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
                    onNotificationsClick = onNavigateNotifications,
                    onMenuClick = onNavigateSettings,
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

            if (state.isSyncing || state.syncPendingCount > 0) {
                item {
                    SyncStatusBanner(
                        isSyncing = state.isSyncing,
                        pendingCount = state.syncPendingCount,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }

            if (state.isLoading) {
                item {
                    QuickExtractCard(
                        maxItems = state.maxExtractItems,
                        enabled = state.canExtract,
                        onStartClick = onNavigateExtract,
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
                item { StatsSection(stats = state.stats, isLoading = true) }
                item { RecentListingsSection(files = emptyList(), isLoading = true, onFileClick = {}) }
            } else {
                item {
                    QuickExtractCard(
                        maxItems = state.maxExtractItems,
                        enabled = state.canExtract,
                        onStartClick = onNavigateExtract,
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
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { it / 4 },
                    ) {
                        StatsSection(stats = state.stats, isLoading = false)
                    }
                }
                item {
                    HomeDashboardCard(
                        title = "کارهای امروز",
                        icon = DfIcons.ListTodo,
                        expanded = todayExpanded,
                        onToggle = { todayExpanded = !todayExpanded },
                        footerLabel = "مشاهده همه کارها (${state.stats.todayTasksTotal.coerceAtLeast(state.todayTasks.size)})",
                        onFooterClick = onNavigateToday,
                    ) {
                        TodayTasksSectionContent(
                            tasks = state.todayTasks,
                            onViewAll = onNavigateToday,
                        )
                    }
                }
                if (state.notifications.isNotEmpty()) {
                    item {
                        HomeDashboardCard(
                            title = "اعلان‌ها",
                            icon = DfIcons.Bell,
                            expanded = notificationsExpanded,
                            onToggle = { notificationsExpanded = !notificationsExpanded },
                            footerLabel = "مشاهده همه اعلان‌ها (${state.notificationBadgeCount.coerceAtLeast(state.notifications.size)})",
                            onFooterClick = onNavigateNotifications,
                        ) {
                            NotificationsSectionContent(
                                notifications = state.notifications,
                                onNotificationClick = { item ->
                                    item.deepLink?.let { link ->
                                        DeepLinkParser.parse(Uri.parse(link))?.let(onNotificationDeepLink)
                                    } ?: onNavigateNotifications()
                                },
                            )
                        }
                    }
                }
                item {
                    RecentListingsSection(
                        files = state.recentFiles,
                        isLoading = false,
                        onFileClick = onDatasetClick,
                        onViewAll = onNavigateFiling,
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
    QuickAction("یادآور جدید", DfIcons.Bell, DfColors.Pink, DfColors.PinkLight, onNavigateToday),
    QuickAction("مخاطب جدید", DfIcons.UserPlus, DfColors.Amber, DfColors.AmberLight, onNavigateContacts),
    QuickAction("فایل‌ها", DfIcons.Folder, DfColors.Blue, DfColors.BlueLight, onNavigateFiling),
    QuickAction("مخاطبین", DfIcons.Users, DfColors.Purple, DfColors.PurpleContainer, onNavigateCrm),
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
    DfPullRefresh(
        isRefreshing = false,
        onRefresh = {},
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionGap),
    ) {
        item {
            HomeHeader(
                userName = "حسین",
                notificationCount = 9,
                onNotificationsClick = {},
                onMenuClick = {},
            )
        }
        item {
            QuickExtractCard(maxItems = 100, enabled = true, onStartClick = {})
        }
        item {
            QuickActionsRow(
                actions = buildQuickActions({}, {}, {}, {}),
            )
        }
        item {
            StatsSection(
                stats = DashboardStats(
                    todayTasksDone = 12,
                    todayTasksRemaining = 8,
                    dailyProgressPercent = 65,
                    tasksDoneDelta = 3,
                    activeReminders = 4,
                ),
                isLoading = false,
            )
        }
        item {
            HomeDashboardCard(
                title = "کارهای امروز",
                icon = DfIcons.ListTodo,
                expanded = false,
                onToggle = {},
                footerLabel = "مشاهده همه کارها (8)",
                onFooterClick = {},
            ) {
                TodayTasksSectionContent(
                    tasks = listOf(
                        HomeTaskItem(
                            id = "1",
                            time = "09:00",
                            title = "پیگیری مشتریان جدید",
                            subtitle = "خریدار — منطقه ونک",
                            type = HomeTaskType.Call,
                        ),
                    ),
                    onViewAll = {},
                )
            }
        }
        item {
            RecentListingsSection(
                files = listOf(
                    RecentFileItem("1", "تهران", "جردن", "فروش", 528, "1404/03/20"),
                    RecentFileItem("2", "تهران", "ونک", "اجاره", 120, "1405/03/28"),
                ),
                isLoading = false,
                onFileClick = {},
            )
        }
    }
    }
}
