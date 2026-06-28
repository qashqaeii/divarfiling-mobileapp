package ir.divarfiling.mobile.feature.filing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfGlassButton
import ir.divarfiling.mobile.core.design.components.DfGlassChip
import ir.divarfiling.mobile.core.design.components.DfGlassTopBar
import ir.divarfiling.mobile.core.design.components.DfLiquidBackground
import ir.divarfiling.mobile.core.design.components.DfPullRefresh

private val levelLabels = listOf("سطح ۱", "سطح ۲", "کارشناسی")

@Composable
fun DatasetInsightsScreen(
    onBack: () -> Unit,
    onOpenMap: (String) -> Unit,
    viewModel: DatasetInsightsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val insights = state.insights

    Box(Modifier.fillMaxSize()) {
        DfLiquidBackground()
        Column(Modifier.fillMaxSize()) {
            DfGlassTopBar(
                title = insights?.dataset?.name?.let { "تحلیل $it" } ?: "تحلیل فایل",
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                action = {
                    if (state.datasetId.isNotBlank() && (insights?.meta?.geoCount ?: 0) > 0) {
                        DfGlassButton(
                            text = "نقشه",
                            onClick = { onOpenMap(state.datasetId) },
                            icon = Icons.Default.Map,
                            selected = true,
                        )
                    }
                },
            )

            DfPullRefresh(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    state.isLoading -> Column(Modifier.padding(16.dp)) {
                        DfCardListSkeleton(count = 4, itemHeight = 120.dp)
                    }
                    state.error != null && insights == null -> {
                        Column(Modifier.padding(16.dp)) { DfErrorBanner(state.error!!) }
                    }
                    insights == null -> DfEmptyState(
                        title = "داده‌ای برای تحلیل نیست",
                        subtitle = "ابتدا آگهی‌ها را در این فایل بارگذاری کنید",
                    )
                    else -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            levelLabels.forEachIndexed { index, label ->
                                DfGlassChip(
                                    text = label,
                                    selected = state.selectedLevel == index,
                                    onClick = { viewModel.selectLevel(index) },
                                )
                            }
                        }

                        Text(
                            "${insights.meta.cleanCount} فایل تحلیل شد · ${insights.meta.rowCount} خام",
                            style = AppTypography.bodyDescription,
                            color = DfColors.TextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )

                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            when (state.selectedLevel) {
                                0 -> item { InsightsLevel1Content(insights) }
                                1 -> item {
                                    InsightsLevel2Content(
                                        insights = insights,
                                        selectedTab = state.selectedL2Tab,
                                        onTabSelected = viewModel::selectL2Tab,
                                    )
                                }
                                else -> item { InsightsLevel3Content(insights) }
                            }
                            if (state.selectedLevel != 2) {
                                item { InsightsOpportunitiesSection(insights) }
                            }
                            insights.charts.take(2).forEach { chart ->
                                if (state.selectedLevel == 0) {
                                    item { ir.divarfiling.mobile.core.design.components.DfChartCard(chart) }
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
