package ir.divarfiling.mobile.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfSpacing
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
import ir.divarfiling.mobile.feature.home.components.TodayTasksSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToday: () -> Unit,
    onNavigateContacts: () -> Unit,
    onNavigateFiling: () -> Unit = {},
    onNavigateExtract: () -> Unit = {},
    onNavigateCrm: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    onDatasetClick: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DfPullRefresh(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier
            .fillMaxSize()
            .background(DfColors.Background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DfSpacing.sectionGap),
        ) {
            HomeHeader(
                userName = state.userName,
                notificationCount = state.notificationBadgeCount,
                onSearchClick = onNavigateFiling,
                onNotificationsClick = onNavigateToday,
            )

            state.error?.let { DfErrorBanner(message = it) }

            AnimatedVisibility(
                visible = !state.isLoading,
                enter = fadeIn() + slideInVertically { it / 4 },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(DfSpacing.sectionGap)) {
                    StatsSection(stats = state.stats, isLoading = false)

                    QuickExtractCard(
                        maxItems = state.maxExtractItems,
                        enabled = state.canExtract,
                        onStartClick = onNavigateExtract,
                    )

                    TodayTasksSection(
                        tasks = state.todayTasks,
                        isLoading = false,
                        onViewAll = onNavigateToday,
                    )

                    NotificationsSection(
                        notifications = state.notifications,
                        onViewAll = onNavigateToday,
                    )

                    RecentListingsSection(
                        files = state.recentFiles,
                        isLoading = false,
                        onFileClick = onDatasetClick,
                    )

                    QuickActionsRow(
                        actions = buildQuickActions(
                            onNavigateContacts = onNavigateContacts,
                            onNavigateFiling = onNavigateFiling,
                            onNavigateCrm = onNavigateCrm,
                            onNavigateToday = onNavigateToday,
                        ),
                        modifier = Modifier.padding(bottom = DfSpacing.xxl),
                    )
                }
            }

            if (state.isLoading) {
                Column(verticalArrangement = Arrangement.spacedBy(DfSpacing.sectionGap)) {
                    StatsSection(stats = state.stats, isLoading = true)
                    QuickExtractCard(
                        maxItems = state.maxExtractItems,
                        enabled = state.canExtract,
                        onStartClick = onNavigateExtract,
                    )
                    TodayTasksSection(tasks = emptyList(), isLoading = true, onViewAll = {})
                    RecentListingsSection(files = emptyList(), isLoading = true, onFileClick = {})
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

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeScreenPreview() {
    DivarFilingTheme {
        // Preview without ViewModel — use static sections in component previews
    }
}
