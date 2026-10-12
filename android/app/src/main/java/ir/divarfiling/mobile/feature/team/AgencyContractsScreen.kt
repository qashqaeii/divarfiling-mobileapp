package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.design.components.showDfMessage
import ir.divarfiling.mobile.core.network.AgencyContractListItemDto
import ir.divarfiling.mobile.core.network.ContractListStatsDto
import ir.divarfiling.mobile.core.network.TeamMemberDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.TeamRepository
import ir.divarfiling.mobile.feature.contracts.ContractSummaryTab
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgencyContractsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val stats: ContractListStatsDto = ContractListStatsDto(),
    val items: List<AgencyContractListItemDto> = emptyList(),
    val advisors: List<TeamMemberDto> = emptyList(),
    val canFilterAdvisor: Boolean = false,
    val query: String = "",
    val selectedTab: ContractSummaryTab = ContractSummaryTab.ALL,
    val advisorMemberId: Long? = null,
    val error: String? = null,
    val forbidden: Boolean = false,
    val page: Int = 1,
    val hasMore: Boolean = false,
    val isFilteredEmpty: Boolean = false,
)

@HiltViewModel
class AgencyContractsViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgencyContractsUiState())
    val uiState: StateFlow<AgencyContractsUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    init {
        load(refresh = true)
    }

    fun load(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = it.items.isEmpty() || refresh,
                    isRefreshing = refresh && it.items.isNotEmpty(),
                    error = null,
                    forbidden = false,
                    items = if (refresh) emptyList() else it.items,
                )
            }
            fetchPage(page = 1, replace = true)
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            fetchPage(page = state.page + 1, replace = false)
        }
    }

    private suspend fun fetchPage(page: Int, replace: Boolean) {
        val state = _uiState.value
        val needsAction = state.selectedTab == ContractSummaryTab.NEEDS_ACTION
        val status = when (state.selectedTab) {
            ContractSummaryTab.DRAFT -> "draft"
            ContractSummaryTab.FINALIZED -> "finalized"
            ContractSummaryTab.AWAITING -> "sent"
            else -> null
        }
        when (
            val result = repository.listAgencyContracts(
                query = state.query,
                status = if (needsAction) null else status,
                advisorMemberId = state.advisorMemberId,
                sort = if (needsAction) "needs_action" else "updated",
                needsAction = needsAction,
                page = page,
            )
        ) {
            is ApiResult.Success -> {
                val (data, pageInfo) = result.data
                val merged = if (replace) data.items else state.items + data.items
                val filteredEmpty = merged.isEmpty() &&
                    (state.query.isNotBlank() || state.advisorMemberId != null || state.selectedTab != ContractSummaryTab.ALL)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        stats = data.stats,
                        items = merged,
                        advisors = data.advisors,
                        canFilterAdvisor = data.canFilterAdvisor,
                        page = pageInfo.page,
                        hasMore = pageInfo.hasMore,
                        isFilteredEmpty = filteredEmpty,
                    )
                }
            }
            is ApiResult.Error -> {
                val forbidden = result.code == "FORBIDDEN"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        error = result.message,
                        forbidden = forbidden,
                    )
                }
            }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            load(refresh = true)
        }
    }

    fun onTabSelected(tab: ContractSummaryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        load(refresh = true)
    }

    fun setAdvisorFilter(memberId: Long?) {
        _uiState.update { it.copy(advisorMemberId = memberId) }
        load(refresh = true)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyContractsScreen(
    onBack: () -> Unit,
    onContractClick: (Long) -> Unit,
    onContinueWizard: (Long) -> Unit = onContractClick,
    onOpenAdvisor: (Long) -> Unit = {},
    viewModel: AgencyContractsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val pad = teamHorizontalPadding()

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showDfMessage(it) }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { TeamAgencySnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            TeamAmbientBackground()
            when {
                state.forbidden -> {
                    Column(Modifier.fillMaxSize()) {
                        TeamAgencyScreenHeader(
                            title = "قراردادهای تیم",
                            subtitle = "نمای مدیریتی آژانس",
                            icon = DfIcons.File,
                            accent = AgencyHeaderAccent.Management,
                            onBack = onBack,
                        )
                        DfEmptyState(
                            title = "دسترسی محدود",
                            subtitle = state.error ?: "فقط مدیران تیم می‌توانند قراردادهای آژانس را ببینند.",
                            variant = DfEmptyVariant.Empty,
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                }
                else -> {
                    DfPullRefresh(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { viewModel.load(refresh = true) },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        LazyColumn(
                            contentPadding = teamListContentPadding(),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                        ) {
                            item {
                                TeamAgencyScreenHeader(
                                    title = "قراردادهای تیم",
                                    subtitle = if (state.isLoading) "در حال بارگذاری…"
                                    else "${DateUtils.toPersianDigits(state.stats.all.toString())} قرارداد در آژانس",
                                    icon = DfIcons.File,
                                    accent = AgencyHeaderAccent.Management,
                                    onBack = onBack,
                                )
                            }
                            if (!state.isLoading) {
                                item {
                                    AgencyContractsStatsRow(
                                        stats = state.stats,
                                        selectedTab = state.selectedTab,
                                        onTabSelected = viewModel::onTabSelected,
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                            }
                            item {
                                DfTextField(
                                    value = state.query,
                                    onValueChange = viewModel::onQueryChange,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = pad),
                                    label = "جستجو",
                                    placeholder = "شماره، عنوان، مشتری یا مشاور…",
                                    singleLine = true,
                                )
                            }
                            if (state.canFilterAdvisor && state.advisors.isNotEmpty()) {
                                item {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(horizontal = pad),
                                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                                    ) {
                                        DfSoftChip(
                                            text = "همه مشاوران",
                                            selected = state.advisorMemberId == null,
                                            onClick = { viewModel.setAdvisorFilter(null) },
                                        )
                                        state.advisors.forEach { advisor ->
                                            DfSoftChip(
                                                text = advisor.name.ifBlank { "مشاور" },
                                                selected = state.advisorMemberId == advisor.id,
                                                onClick = { viewModel.setAdvisorFilter(advisor.id) },
                                            )
                                        }
                                    }
                                }
                            }
                            state.error?.let { err ->
                                if (!state.forbidden) {
                                    item { DfErrorBanner(message = err, modifier = Modifier.padding(horizontal = pad)) }
                                }
                            }
                            if (state.isLoading) {
                                item { DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad)) }
                            } else if (state.items.isEmpty()) {
                                item {
                                    DfEmptyState(
                                        title = if (state.isFilteredEmpty) "قراردادی مطابق فیلتر نیست" else "قراردادی ثبت نشده",
                                        subtitle = if (state.isFilteredEmpty) "جستجو یا فیلتر مشاور را تغییر دهید"
                                        else "قراردادهای مشاوران پس از ثبت اینجا نمایش داده می‌شوند.",
                                        variant = if (state.isFilteredEmpty) DfEmptyVariant.NoResults else DfEmptyVariant.Empty,
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                            } else {
                                items(state.items, key = { it.id }) { item ->
                                    AgencyContractListCard(
                                        item = item,
                                        onClick = { onContractClick(item.id) },
                                        onPrimaryAction = {
                                            when (item.nextAction.nativeTarget) {
                                                "wizard" -> onContinueWizard(item.id)
                                                else -> onContractClick(item.id)
                                            }
                                        },
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                                if (state.hasMore) {
                                    item {
                                        DfSecondaryButton(
                                            text = if (state.isLoadingMore) "در حال بارگذاری…" else "نمایش بیشتر",
                                            onClick = viewModel::loadMore,
                                            enabled = !state.isLoadingMore,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = pad),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
