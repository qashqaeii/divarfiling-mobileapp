package ir.divarfiling.mobile.feature.extract.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.feature.extract.schedule.components.ScheduleCard
import ir.divarfiling.mobile.feature.extract.schedule.components.ScheduleSummaryHero

@Composable
fun ExtractSchedulesScreen(
    onBack: () -> Unit,
    viewModel: ExtractSchedulesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val hasRunningSchedule = state.schedules.any { it.lastStatus == "running" }
    LaunchedEffect(hasRunningSchedule) {
        if (!hasRunningSchedule) return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(5_000)
            viewModel.refresh()
        }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
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
                            title = "زمان‌بندی استخراج",
                            subtitle = "اجرای خودکار فیلترهای ذخیره‌شده",
                            titleIconRes = DfDecorIcons.Timer,
                            onBack = onBack,
                        )
                        DfCardListSkeleton(
                            count = 4,
                            itemHeight = 200.dp,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                state.schedules.isEmpty() -> {
                    Column {
                        DfHubPageHeader(
                            title = "زمان‌بندی استخراج",
                            subtitle = "اجرای خودکار فیلترهای ذخیره‌شده",
                            titleIconRes = DfDecorIcons.Timer,
                            onBack = onBack,
                        )
                        state.error?.let { error ->
                            DfErrorBanner(
                                error,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                        DfEmptyState(
                            title = "زمان‌بندی فعالی ندارید",
                            subtitle = "از صفحه استخراج، فیلترها را ذخیره کنید تا به‌صورت خودکار اجرا شود",
                            variant = DfEmptyVariant.Empty,
                            icon = DfIcons.Timer,
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
                                title = "زمان‌بندی استخراج",
                                subtitle = "اجرای خودکار فیلترهای ذخیره‌شده",
                                titleIconRes = DfDecorIcons.Timer,
                                onBack = onBack,
                            )
                        }
                        state.error?.let { error ->
                            item {
                                DfErrorBanner(
                                    error,
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                )
                            }
                        }
                        item {
                            ScheduleSummaryHero(
                                schedules = state.schedules,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                        items(state.schedules, key = { it.id }) { schedule ->
                            ScheduleCard(
                                schedule = schedule,
                                runs = state.expandedRuns[schedule.id],
                                onToggle = { viewModel.toggleSchedule(schedule.id) },
                                onRunNow = { viewModel.runNow(schedule.id) },
                                onDelete = { viewModel.deleteSchedule(schedule.id) },
                                onToggleRuns = { viewModel.loadRuns(schedule.id) },
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                    }
                }
            }
        }
    }
}
