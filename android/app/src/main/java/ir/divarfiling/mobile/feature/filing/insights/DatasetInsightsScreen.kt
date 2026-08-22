package ir.divarfiling.mobile.feature.filing.insights

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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.AppLinks
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfContinueOnWebRow
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import kotlinx.serialization.json.JsonElement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatasetInsightsScreen(
    onBack: () -> Unit,
    viewModel: DatasetInsightsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val insights = state.insights

    Scaffold(containerColor = DfScreenContainerColor) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            when {
                state.isLoading -> DfDetailSkeleton()
                state.error != null && insights == null -> {
                    Column(
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        DfHubPageHeader(
                            title = "تحلیل فایل",
                            subtitle = "بینش‌های هوشمند فایلینگ",
                            titleIconRes = DfDecorIcons.BarChart,
                            sectionLabel = DfHeaderSections.FILING,
                            onBack = onBack,
                        )
                        DfErrorBanner(state.error!!)
                        DfEmptyState(
                            title = "بارگذاری ناموفق",
                            subtitle = "اتصال را بررسی کنید و دوباره تلاش کنید",
                            variant = DfEmptyVariant.Error,
                            actionLabel = "تلاش مجدد",
                            onAction = viewModel::refresh,
                        )
                    }
                }
                insights == null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        DfHubPageHeader(
                            title = "تحلیل فایل",
                            subtitle = "بینش‌های هوشمند فایلینگ",
                            titleIconRes = DfDecorIcons.BarChart,
                            sectionLabel = DfHeaderSections.FILING,
                            onBack = onBack,
                        )
                        DfEmptyState(
                            title = "داده‌ای یافت نشد",
                            subtitle = "تحلیل برای این فایل در دسترس نیست.",
                            variant = DfEmptyVariant.Empty,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                    ) {
                        item {
                            DfHubPageHeader(
                                title = insights.dataset?.name ?: "تحلیل پوشه استخراج",
                                subtitle = "نمای سریع موبایل — تحلیل عمیق در میزکار وب",
                                titleIconRes = DfDecorIcons.BarChart,
                                sectionLabel = DfHeaderSections.FILING,
                                onBack = onBack,
                            )
                        }
                        item {
                            DfContinueOnWebRow(
                                title = "ادامه در میزکار وب",
                                subtitle = "مقایسه و تحلیل عمیق این پوشه در مرورگر",
                                url = AppLinks.workspaceDataset(state.datasetId),
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
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
                            InsightsMetaRow(
                                rowCount = insights.meta.rowCount,
                                cleanCount = insights.meta.cleanCount,
                                geoCount = insights.meta.geoCount,
                                filterLabel = insights.meta.filterValueLabel,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                        if (insights.quickSnapshot.isNotEmpty()) {
                            val (blocks, kpis) = presentSnapshot(insights.quickSnapshot)
                            item {
                                Box(Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                                    DfSectionHeader("خلاصه تحلیل")
                                }
                            }
                            if (kpis.isNotEmpty()) {
                                item {
                                    InsightsKpiGrid(
                                        items = kpis,
                                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                    )
                                }
                            }
                            blocks.forEach { block ->
                                item {
                                    InsightBlockCard(
                                        block = block,
                                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                    )
                                }
                            }
                        }
                        presentedSection("فرصت‌ها", insights.opportunities)
                        presentedSection("بینش‌ها", insights.insights)
                        presentedSection("مذاکره", insights.negotiation)
                        if (
                            insights.opportunities.isEmpty() &&
                            insights.insights.isEmpty() &&
                            insights.negotiation.isEmpty()
                        ) {
                            item {
                                DfEmptyState(
                                    title = "بینشی ثبت نشده",
                                    subtitle = "برای این فایل هنوز تحلیل تفصیلی موجود نیست.",
                                    variant = DfEmptyVariant.Empty,
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

private fun LazyListScope.presentedSection(
    title: String,
    items: List<JsonElement>,
) {
    val blocks = presentElementList(items)
    if (blocks.isEmpty()) return
    item {
        Box(Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
            DfSectionHeader(title, blocks.size)
        }
    }
    itemsIndexed(blocks, key = { index, block -> "$title-$index-${block.title}" }) { _, block ->
        InsightBlockCard(
            block = block,
            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
        )
    }
}

@Composable
private fun InsightsMetaRow(
    rowCount: Int,
    cleanCount: Int,
    geoCount: Int,
    filterLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        MetaChip("$rowCount ردیف", Modifier.weight(1f))
        MetaChip("$cleanCount تمیز", Modifier.weight(1f))
        MetaChip("$geoCount موقعیت", Modifier.weight(1f))
        MetaChip(filterLabel, Modifier.weight(1f))
    }
}

@Composable
private fun MetaChip(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(DfThemeColors.surfaceVariant(), AppShapes.CardSmall)
            .padding(AppSpacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = DfThemeColors.textPrimary(),
            maxLines = 1,
        )
    }
}

@Composable
private fun InsightsKpiGrid(
    items: List<InsightKpi>,
    modifier: Modifier = Modifier,
) {
    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            items.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    row.forEach { kpi ->
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(kpi.label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
                            Text(
                                kpi.value,
                                style = AppTypography.cardTitle,
                                fontWeight = FontWeight.SemiBold,
                                color = DfThemeColors.textPrimary(),
                            )
                        }
                    }
                    if (row.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightBlockCard(
    block: InsightBlock,
    modifier: Modifier = Modifier,
) {
    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                block.title,
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.textPrimary(),
            )
            if (block.body.isNotBlank()) {
                Text(
                    block.body,
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textSecondary(),
                )
            }
            block.facts.forEach { fact ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(fact.label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
                    Text(
                        fact.value,
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DfThemeColors.textPrimary(),
                    )
                }
            }
        }
    }
}
