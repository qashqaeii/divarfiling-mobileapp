package ir.divarfiling.mobile.feature.crm

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfActionButton
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.network.DealFinanceDashboardData
import ir.divarfiling.mobile.core.network.DealFinanceDealRowDto
import ir.divarfiling.mobile.core.network.DealFinanceDefaultsDto
import ir.divarfiling.mobile.core.network.DealFinanceKpisDto
import ir.divarfiling.mobile.core.network.DealFinanceMonthDto
import ir.divarfiling.mobile.core.network.DealFinancePayoutDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealFinanceDashboardScreen(
    onBack: () -> Unit,
    onDealClick: (Long) -> Unit,
    viewModel: DealFinanceViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
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
                state.isLoading && state.dashboard == null -> DfCardListSkeleton()
                state.dashboard != null -> {
                    val dash = state.dashboard!!
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                    ) {
                        item {
                            DfDetailPageHeader(
                                title = "مالی معاملات",
                                subtitle = dash.periodLabel,
                                sectionLabel = DfHeaderSections.CRM,
                                titleIconRes = DfDecorIcons.Coins,
                                onBack = onBack,
                                showBottomDivider = true,
                            )
                        }
                        state.error?.let { err ->
                            item {
                                DfErrorBanner(
                                    message = err,
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                )
                            }
                        }
                        item {
                            FinancePeriodRow(
                                periods = dash.periods.map { it.id to it.label },
                                selected = dash.period,
                                onSelect = viewModel::selectPeriod,
                                onSettings = { viewModel.toggleSettings(true) },
                            )
                        }
                        item { FinanceHero(dash.kpis) }
                        item { FinanceKpiGrid(dash.kpis) }
                        item { FinanceSplitCard(dash) }
                        item { FinancePayoutCard(dash.payout) }
                        item { FinanceMonthlyCard(dash.monthly) }
                        item { FinanceForecastCard(dash) }
                        if (dash.deals.isEmpty()) {
                            item {
                                DfEmptyState(
                                    title = "معامله بسته‌شده‌ای در این بازه نیست",
                                    subtitle = "با بستن معاملات، درآمد مشاور و سهم املاک اینجا دیده می‌شود.",
                                    variant = DfEmptyVariant.NoResults,
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                )
                            }
                        } else {
                            items(dash.deals, key = { it.id }) { row ->
                                FinanceDealRow(
                                    row = row,
                                    onClick = { onDealClick(row.id) },
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                )
                            }
                        }
                    }
                }
                else -> {
                    DfErrorBanner(
                        message = state.error ?: "بارگذاری داشبورد مالی ممکن نشد",
                        modifier = Modifier.padding(AppSpacing.screenHorizontal),
                    )
                }
            }
        }
    }

    if (state.showSettings) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleSettings(false) }) {
            FinanceDefaultsSheet(
                defaults = state.editDefaults,
                isSubmitting = state.isSubmitting,
                onSaleBuyerChange = viewModel::onSaleBuyerChange,
                onSaleSellerChange = viewModel::onSaleSellerChange,
                onRentBuyerChange = viewModel::onRentBuyerChange,
                onRentSellerChange = viewModel::onRentSellerChange,
                onAdvisorChange = viewModel::onDefaultAdvisorChange,
                onAgencyChange = viewModel::onDefaultAgencyChange,
                onSave = viewModel::saveSettings,
                onDismiss = { viewModel.toggleSettings(false) },
            )
        }
    }
}

@Composable
private fun FinancePeriodRow(
    periods: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    onSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        periods.forEach { (id, label) ->
            DfActionButton(
                text = label,
                filled = id == selected,
                onClick = { onSelect(id) },
            )
        }
        DfActionButton(
            text = "پیش‌فرض",
            icon = DfIcons.Settings,
            onClick = onSettings,
        )
    }
}

@Composable
private fun FinanceHero(kpis: DealFinanceKpisDto) {
    Column(
        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        FinanceHeroCard("درآمد مشاور", kpis.advisorIncome, DfColors.Green, DfColors.GreenLight)
        FinanceHeroCard("سهم املاک", kpis.agencyIncome, DfColors.Purple, DfColors.PurpleContainer)
        FinanceHeroCard("حجم معاملات", kpis.volume, DfColors.TextPrimary, DfColors.BlueLight)
    }
}

@Composable
private fun FinanceHeroCard(label: String, amount: Long, tint: androidx.compose.ui.graphics.Color, background: androidx.compose.ui.graphics.Color) {
    Surface(shape = AppShapes.Card, color = background, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = label, style = AppTypography.labelSmall, fontWeight = FontWeight.SemiBold, color = tint)
            Text(
                text = FormatUtils.formatPriceToman(amount),
                style = AppTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = tint,
            )
        }
    }
}

@Composable
private fun FinanceKpiGrid(kpis: DealFinanceKpisDto) {
    Column(
        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), modifier = Modifier.fillMaxWidth()) {
            FinanceKpiChip("کمیسیون کل", kpis.commissionTotal, Modifier.weight(1f))
            FinanceKpiChip("خالص شما", kpis.netIncome, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), modifier = Modifier.fillMaxWidth()) {
            FinanceKpiChip("وصول‌شده", kpis.received, Modifier.weight(1f))
            FinanceKpiChip("معوق", kpis.outstanding, Modifier.weight(1f))
        }
    }
}

@Composable
private fun FinanceKpiChip(label: String, amount: Long, modifier: Modifier = Modifier) {
    Surface(shape = AppShapes.Card, color = DfThemeColors.surface(), modifier = modifier) {
        Column(modifier = Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
            Text(
                text = FormatUtils.formatPriceShort(amount),
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = DfColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun FinanceSplitCard(dash: DealFinanceDashboardData) {
    val advisor = dash.split.advisorPercent.coerceIn(0, 100)
    val agency = (100 - advisor).coerceAtLeast(0)
    Surface(
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Text("تقسیم کمیسیون", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth().clip(AppShapes.Chip).height(12.dp)) {
                if (advisor > 0) Box(Modifier.weight(advisor.toFloat()).height(12.dp).background(DfColors.Green))
                if (agency > 0) Box(Modifier.weight(agency.toFloat()).height(12.dp).background(DfColors.Purple))
            }
            Text(
                text = "مشاور $advisor٪ · ${FormatUtils.formatPriceShort(dash.split.advisor)}   |   املاک $agency٪ · ${FormatUtils.formatPriceShort(dash.split.agency)}",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
        }
    }
}

@Composable
private fun FinancePayoutCard(rows: List<DealFinancePayoutDto>) {
    Surface(
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text("وصول کمیسیون", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            rows.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${row.label} · ${row.count}", style = AppTypography.labelSmall, color = DfColors.TextSecondary)
                    Text(FormatUtils.formatPriceShort(row.amount), style = AppTypography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FinanceMonthlyCard(months: List<DealFinanceMonthDto>) {
    Surface(
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Text("روند شش ماه", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                months.forEach { month ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height((40 + month.advisorPct).coerceAtMost(110).dp)
                                .clip(AppShapes.Chip)
                                .background(DfColors.Green.copy(alpha = 0.85f)),
                        )
                        Text(month.label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceForecastCard(dash: DealFinanceDashboardData) {
    Surface(
        shape = AppShapes.Card,
        color = DfColors.PurpleContainer.copy(alpha = 0.55f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("پیش‌بینی قیف فعال", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            Text(
                text = "${dash.forecast.activeCount} معامله فعال · سهم مورد انتظار ${FormatUtils.formatPriceToman(dash.forecast.weightedAdvisor)}",
                style = AppTypography.labelSmall,
                color = DfColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun FinanceDealRow(
    row: DealFinanceDealRowDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(onClick = onClick, shape = AppShapes.Card, color = DfThemeColors.surface(), modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(row.title, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(
                text = listOf(row.customerName, row.dealKindLabel, row.payoutStatusLabel).filter { it.isNotBlank() }.joinToString(" · "),
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("شما ${FormatUtils.formatPriceShort(row.advisorCommission)}", color = DfColors.Green, style = AppTypography.labelSmall, fontWeight = FontWeight.Bold)
                Text("املاک ${FormatUtils.formatPriceShort(row.agencyCommission)}", color = DfColors.Purple, style = AppTypography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FinanceDefaultsSheet(
    defaults: DealFinanceDefaultsDto,
    isSubmitting: Boolean,
    onSaleBuyerChange: (String) -> Unit,
    onSaleSellerChange: (String) -> Unit,
    onRentBuyerChange: (String) -> Unit,
    onRentSellerChange: (String) -> Unit,
    onAdvisorChange: (String) -> Unit,
    onAgencyChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    DfSheetScaffold(
        title = "پیش‌فرض کمیسیون",
        subtitle = "برای معاملات جدید اعمال می‌شود",
        icon = DfIcons.Settings,
        iconContainerColor = DfColors.GreenLight,
        iconTint = DfColors.Green,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ذخیره…" else "ذخیره پیش‌فرض",
                onPrimary = onSave,
                primaryEnabled = !isSubmitting,
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "فروش") {
            OutlinedTextField(
                value = defaults.saleBuyerRate.toString(),
                onValueChange = onSaleBuyerChange,
                label = { Text("نرخ خریدار (٪)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = defaults.saleSellerRate.toString(),
                onValueChange = onSaleSellerChange,
                label = { Text("نرخ فروشنده (٪)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
        }
        DfSheetSection(title = "اجاره") {
            OutlinedTextField(
                value = defaults.rentBuyerRate.toString(),
                onValueChange = onRentBuyerChange,
                label = { Text("نرخ مستأجر (٪)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = defaults.rentSellerRate.toString(),
                onValueChange = onRentSellerChange,
                label = { Text("نرخ مالک (٪)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
        }
        DfSheetSection(title = "تقسیم دفتر") {
            OutlinedTextField(
                value = defaults.advisorSharePercent.toString(),
                onValueChange = onAdvisorChange,
                label = { Text("سهم مشاور (٪)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = defaults.agencySharePercent.toString(),
                onValueChange = onAgencyChange,
                label = { Text("سهم املاک (٪)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
        }
    }
}
