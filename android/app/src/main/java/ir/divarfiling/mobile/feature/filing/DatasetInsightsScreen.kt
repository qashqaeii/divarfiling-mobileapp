package ir.divarfiling.mobile.feature.filing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPillChipRow
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor

private val levelLabels = listOf("سطح ۱", "سطح ۲", "کارشناسی")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatasetInsightsScreen(
    onBack: () -> Unit,
    onOpenMap: (String) -> Unit,
    viewModel: DatasetInsightsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val insights = state.insights
    val title = insights?.dataset?.name?.let { "تحلیل $it" } ?: "تحلیل فایل"

    Scaffold(containerColor = DfScreenContainerColor) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            DfPullRefresh(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    state.isLoading && insights == null -> {
                        Column {
                            DfDetailPageHeader(
                                title = title,
                                subtitle = "تحلیل بازار و قیمت",
                                titleIcon = DfIcons.BarChart,
                                onBack = onBack,
                            )
                            DfCardListSkeleton(
                                count = 4,
                                itemHeight = 120.dp,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                    }
                    state.error != null && insights == null -> {
                        Column {
                            DfDetailPageHeader(
                                title = title,
                                subtitle = "تحلیل بازار و قیمت",
                                titleIcon = DfIcons.BarChart,
                                onBack = onBack,
                            )
                            DfErrorBanner(
                                state.error!!,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                    }
                    insights == null -> {
                        Column {
                            DfDetailPageHeader(
                                title = title,
                                subtitle = "تحلیل بازار و قیمت",
                                titleIcon = DfIcons.BarChart,
                                onBack = onBack,
                            )
                            DfEmptyState(
                                title = "داده‌ای برای تحلیل نیست",
                                subtitle = "ابتدا آگهی‌ها را در این فایل بارگذاری کنید",
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
                                DfDetailPageHeader(
                                    title = title,
                                    subtitle = "${insights.meta.cleanCount} فایل تحلیل‌شده",
                                    titleIcon = DfIcons.BarChart,
                                    onBack = onBack,
                                    actions = {
                                        if (state.datasetId.isNotBlank() && insights.meta.geoCount > 0) {
                                            androidx.compose.material3.IconButton(
                                                onClick = { onOpenMap(state.datasetId) },
                                            ) {
                                                androidx.compose.material3.Icon(
                                                    DfIcons.Map,
                                                    contentDescription = "نقشه",
                                                    tint = DfColors.Purple,
                                                )
                                            }
                                        }
                                    },
                                )
                            }
                            item {
                                DfPillChipRow(
                                    labels = levelLabels,
                                    selectedIndex = state.selectedLevel,
                                    onSelected = viewModel::selectLevel,
                                )
                            }
                            item {
                                Text(
                                    "${insights.meta.cleanCount} فایل تحلیل شد · ${insights.meta.rowCount} خام",
                                    style = AppTypography.bodyDescription,
                                    color = DfColors.TextMuted,
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                )
                            }
                            when (state.selectedLevel) {
                                0 -> item {
                                    InsightsLevel1Content(
                                        insights,
                                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                    )
                                }
                                1 -> item {
                                    InsightsLevel2Content(
                                        insights = insights,
                                        selectedTab = state.selectedL2Tab,
                                        onTabSelected = viewModel::selectL2Tab,
                                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                    )
                                }
                                else -> item {
                                    InsightsLevel3Content(
                                        insights,
                                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                    )
                                }
                            }
                            if (state.selectedLevel != 2) {
                                item {
                                    Box(Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                                        InsightsOpportunitiesSection(insights)
                                    }
                                }
                            }
                            if (state.selectedLevel == 0) {
                                insights.charts.take(2).forEach { chart ->
                                    item {
                                        ir.divarfiling.mobile.core.design.components.DfChartCard(
                                            chart,
                                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (state.isLoading && insights != null) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}
