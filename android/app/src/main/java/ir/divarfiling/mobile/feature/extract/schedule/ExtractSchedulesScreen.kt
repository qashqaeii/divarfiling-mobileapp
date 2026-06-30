package ir.divarfiling.mobile.feature.extract.schedule

import ir.divarfiling.mobile.core.design.DfColors

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
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
                    DfCardListSkeleton(
                        count = 4,
                        itemHeight = 200.dp,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                state.schedules.isEmpty() -> {
                    Column {
                        DfHubPageHeader(
                            title = "زمان‌بندی استخراج",
                            subtitle = "اجرای خودکار فیلترهای ذخیره‌شده",
                            titleIcon = DfIcons.Timer,
                            onBack = onBack,
                        )
                        ScheduleEmptyIllustration(
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                        DfEmptyState(
                            title = "زمان‌بندی فعالی ندارید",
                            subtitle = "از صفحه استخراج فایل، فیلترها را ذخیره کنید تا به‌صورت خودکار اجرا شود",
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
                                titleIcon = DfIcons.Timer,
                                onBack = onBack,
                            )
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

@Composable
private fun ScheduleEmptyIllustration(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Hero,
        color = DfColors.Surface,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = AppSpacing.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(DfIcons.Zap, DfIcons.Timer, DfIcons.Cloud).forEachIndexed { index, icon ->
                    val colors = listOf(
                        DfColors.Amber to DfColors.AmberLight,
                        DfColors.Purple to DfColors.PurpleContainer,
                        DfColors.Blue to DfColors.BlueLight,
                    )[index]
                    Box(
                        modifier = Modifier
                            .size(if (index == 1) 56.dp else 44.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        colors.first.copy(alpha = 0.18f),
                                        colors.second,
                                    ),
                                ),
                                shape = AppShapes.IconContainer,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colors.first,
                            modifier = Modifier.size(if (index == 1) 24.dp else 20.dp),
                        )
                    }
                }
            }
            Text(
                text = "استخراج خودکار در انتظار شماست",
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.Bold,
                color = DfColors.TextPrimary,
            )
            Text(
                text = "یک فیلتر بسازید، بازه زمانی را انتخاب کنید و ذخیره کنید.",
                style = AppTypography.bodyDescription,
                color = DfColors.TextSecondary,
            )
        }
    }
}
