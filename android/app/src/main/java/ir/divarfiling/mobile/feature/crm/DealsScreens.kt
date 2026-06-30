package ir.divarfiling.mobile.feature.crm

import ir.divarfiling.mobile.core.design.DfColors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfExtendedFab
import ir.divarfiling.mobile.feature.crm.components.DealCreateSheet
import ir.divarfiling.mobile.feature.crm.components.DealEditSheet
import ir.divarfiling.mobile.feature.crm.components.DealListCard
import ir.divarfiling.mobile.feature.crm.components.DealsSearchFilterPanel
import ir.divarfiling.mobile.feature.crm.components.DealsFilters
import ir.divarfiling.mobile.feature.crm.components.DealsHeader
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.feature.crm.components.DealsPipelineBar
import ir.divarfiling.mobile.feature.crm.components.DealsSortOrder
import ir.divarfiling.mobile.feature.crm.components.DealsStatsRow
import ir.divarfiling.mobile.feature.crm.components.DealDetailHeroCard
import ir.divarfiling.mobile.feature.crm.components.DealDetailQuickActions
import ir.divarfiling.mobile.feature.crm.components.DealStageSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsScreen(
    onBack: () -> Unit = {},
    onDealClick: (Long) -> Unit = {},
    onNavigateContacts: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    viewModel: DealsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var ownerFilter by remember { mutableStateOf(DealsFilters.ALL_OWNERS) }
    var sortLabel by remember { mutableStateOf(DealsFilters.NEWEST) }

    val sortOrder = if (sortLabel == DealsFilters.OLDEST) DealsSortOrder.Oldest else DealsSortOrder.Newest
    val displayedDeals = remember(state.deals, ownerFilter, sortOrder, state.query) {
        DealsFilters.filterAndSortDeals(
            deals = state.deals,
            ownerFilter = ownerFilter,
            sortOrder = sortOrder,
            localQuery = state.query,
        )
    }

    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            DfExtendedFab(
                text = "معامله جدید",
                icon = DfIcons.Plus,
                onClick = { viewModel.toggleCreate(true) },
            )
        },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = AppSpacing.fabClearance + AppSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DealsHeader(
                        userName = state.userName,
                        notificationCount = state.notificationBadgeCount,
                        onNotificationsClick = onNavigateNotifications,
                        onMenuClick = onNavigateSettings,
                        onBack = onBack,
                    )
                }
                if (state.deals.isNotEmpty() || state.pipelineColumns.isNotEmpty()) {
                    item {
                        DealsStatsRow(
                            activeCount = DealsFilters.activeCount(state.deals),
                            pipelineValueLabel = DealsFilters.formatCompactToman(
                                DealsFilters.pipelineValue(state.deals, state.pipelineColumns),
                            ),
                            weightedForecastLabel = DealsFilters.formatCompactToman(
                                DealsFilters.weightedForecast(state.deals),
                            ),
                            closedCommissionLabel = DealsFilters.formatCompactToman(
                                DealsFilters.closedCommission(state.deals),
                            ),
                            closingRate = DealsFilters.closingRate(state.deals),
                        )
                    }
                }
                if (state.pipelineColumns.isNotEmpty()) {
                    item {
                        DealsPipelineBar(
                            columns = state.pipelineColumns,
                            onStageClick = viewModel::selectStage,
                        )
                    }
                }
                item {
                    DealsSearchFilterPanel(
                        owners = DealsFilters.uniqueOwners(state.deals),
                        selectedOwner = ownerFilter,
                        selectedSort = sortLabel,
                        onOwnerChange = { ownerFilter = it },
                        onSortChange = { sortLabel = it },
                        onResetFilters = {
                            ownerFilter = DealsFilters.ALL_OWNERS
                            sortLabel = DealsFilters.NEWEST
                            viewModel.onQueryChange("")
                            viewModel.clearStageFilter()
                        },
                        query = state.query,
                        onQueryChange = viewModel::onQueryChange,
                        onSearch = viewModel::search,
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
                if (state.isLoading && state.deals.isEmpty()) {
                    item {
                        DfCardListSkeleton(
                            count = 5,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else if (displayedDeals.isEmpty() && state.error == null) {
                    item {
                        DfEmptyState(
                            title = if (state.deals.isEmpty()) "معامله‌ای نیست" else "نتیجه‌ای با این فیلتر نیست",
                            subtitle = if (state.deals.isEmpty()) {
                                "با «معامله جدید» یک فرصت فروش بسازید"
                            } else {
                                "فیلترها یا جستجو را تغییر دهید"
                            },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else {
                    item {
                        Box(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                            DfSectionHeader(
                                title = "معاملات",
                                count = displayedDeals.size,
                            )
                        }
                    }
                    items(displayedDeals, key = { it.id }) { deal ->
                        DealListCard(
                            deal = deal,
                            onClick = { onDealClick(deal.id) },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
            }
        }
    }

    if (state.showCreateDialog) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleCreate(false) }) {
            DealCreateSheet(
                contacts = state.contactPicker,
                stages = state.stages,
                selectedContactId = state.createCustomerId,
                selectedStage = state.createStage,
                title = state.createTitle,
                amount = state.createAmount,
                notes = state.createNotes,
                isSubmitting = state.isSubmittingCreate,
                onContactSelect = viewModel::onCreateCustomerSelect,
                onStageChange = viewModel::onCreateStageChange,
                onTitleChange = viewModel::onCreateTitleChange,
                onAmountChange = viewModel::onCreateAmountChange,
                onNotesChange = viewModel::onCreateNotesChange,
                onSubmit = viewModel::submitCreate,
                onDismiss = { viewModel.toggleCreate(false) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealDetailScreen(
    onBack: () -> Unit,
    onContactClick: (Long) -> Unit = {},
    viewModel: DealDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val deal = state.deal
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
                state.isLoading -> DfDetailSkeleton()
                deal != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                    ) {
                        item {
                            DfDetailPageHeader(
                                title = deal.title,
                                subtitle = deal.stage,
                                titleIconRes = DfDecorIcons.Handshake,
                                onBack = onBack,
                            )
                        }
                        item {
                            DealDetailHeroCard(
                                deal = deal,
                                onContactClick = { deal.customerId?.let(onContactClick) },
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                        item {
                            DealStageSection(
                                stages = state.stages,
                                currentStage = deal.stage,
                                isSubmitting = state.isSubmitting,
                                onStageSelect = viewModel::changeStage,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                        item {
                            DealDetailQuickActions(
                                onEdit = { viewModel.toggleEditSheet(true) },
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showEditSheet) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleEditSheet(false) }) {
            DealEditSheet(
                title = state.editTitle,
                amount = state.editAmount,
                notes = state.editNotes,
                stages = state.stages,
                selectedStage = state.editStage.ifBlank { deal?.stage.orEmpty() },
                isSubmitting = state.isSubmitting,
                onTitleChange = viewModel::onEditTitleChange,
                onAmountChange = viewModel::onEditAmountChange,
                onNotesChange = viewModel::onEditNotesChange,
                onStageChange = viewModel::onEditStageChange,
                onSave = viewModel::saveEdit,
                onDismiss = { viewModel.toggleEditSheet(false) },
            )
        }
    }
}
