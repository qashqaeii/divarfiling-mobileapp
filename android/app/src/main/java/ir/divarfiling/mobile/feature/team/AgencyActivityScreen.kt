package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.network.AgencyActivityDto
import ir.divarfiling.mobile.core.network.TeamMemberDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgencyActivityUiState(
    val events: List<AgencyActivityDto> = emptyList(),
    val advisors: List<TeamMemberDto> = emptyList(),
    val eventTypes: List<String> = emptyList(),
    val canFilterAdvisor: Boolean = false,
    val advisorMemberId: Long? = null,
    val eventType: String? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AgencyActivityViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgencyActivityUiState())
    val uiState: StateFlow<AgencyActivityUiState> = _uiState.asStateFlow()

    init {
        refresh(initial = true)
    }

    fun setAdvisorFilter(memberId: Long?) {
        _uiState.update { it.copy(advisorMemberId = memberId) }
        refresh(initial = true)
    }

    fun setEventTypeFilter(type: String?) {
        _uiState.update { it.copy(eventType = type?.takeIf { it.isNotBlank() }) }
        refresh(initial = true)
    }

    fun refresh(initial: Boolean = false) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update {
                it.copy(
                    isLoading = initial && it.events.isEmpty(),
                    isRefreshing = !initial || it.events.isNotEmpty(),
                    error = null,
                )
            }
            when (
                val result = repository.getAgencyActivity(
                    advisorMemberId = state.advisorMemberId,
                    eventType = state.eventType,
                )
            ) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        events = result.data.events,
                        advisors = result.data.advisors,
                        eventTypes = result.data.eventTypes,
                        canFilterAdvisor = result.data.canFilterAdvisor,
                        isLoading = false,
                        isRefreshing = false,
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyActivityScreen(
    onBack: () -> Unit,
    viewModel: AgencyActivityViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val pad = teamHorizontalPadding()

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it) }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(
                contentPadding = teamListContentPadding(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "فعالیت آژانس",
                        subtitle = "رویدادهای تیم",
                        sectionLabel = DfHeaderSections.TEAM,
                        titleIconRes = DfDecorIcons.Users,
                        onBack = onBack,
                    )
                }
                if (state.eventTypes.isNotEmpty()) {
                    item {
                        LazyRow(
                            modifier = Modifier.padding(horizontal = pad),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            item {
                                DfSoftChip(
                                    text = "همه رویدادها",
                                    selected = state.eventType.isNullOrBlank(),
                                    onClick = { viewModel.setEventTypeFilter(null) },
                                )
                            }
                            items(state.eventTypes, key = { it }) { type ->
                                DfSoftChip(
                                    text = type.replace(".", " · "),
                                    selected = state.eventType == type,
                                    onClick = { viewModel.setEventTypeFilter(type) },
                                )
                            }
                        }
                    }
                }
                if (state.canFilterAdvisor && state.advisors.isNotEmpty()) {
                    item {
                        LazyRow(
                            modifier = Modifier.padding(horizontal = pad),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            item {
                                DfSoftChip(
                                    text = "همه مشاوران",
                                    selected = state.advisorMemberId == null,
                                    onClick = { viewModel.setAdvisorFilter(null) },
                                )
                            }
                            items(state.advisors, key = { it.id }) { advisor ->
                                DfSoftChip(
                                    text = advisor.name.ifBlank { advisor.phone },
                                    selected = state.advisorMemberId == advisor.id,
                                    onClick = { viewModel.setAdvisorFilter(advisor.id) },
                                )
                            }
                        }
                    }
                }
                when {
                    state.isLoading -> item {
                        DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad))
                    }
                    state.events.isEmpty() -> item {
                        DfEmptyState(
                            title = "رویدادی ثبت نشده",
                            subtitle = state.error ?: "فعالیت تیم در این بازه خالی است.",
                            variant = if (state.error != null) DfEmptyVariant.Error else DfEmptyVariant.Empty,
                            actionLabel = if (state.error != null) "تلاش مجدد" else null,
                            onAction = if (state.error != null) {
                                { viewModel.refresh(initial = true) }
                            } else {
                                null
                            },
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                    else -> items(state.events, key = { it.id }) { event ->
                        DfCard(modifier = Modifier.padding(horizontal = pad)) {
                            Column(
                                modifier = Modifier.padding(AppSpacing.cardPadding),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            ) {
                                Text(event.title, fontWeight = FontWeight.SemiBold)
                                if (event.actorName.isNotBlank()) {
                                    Text(
                                        event.actorName,
                                        style = AppTypography.bodyDescription,
                                        color = DfThemeColors.textSecondary(),
                                    )
                                }
                                Text(
                                    DateUtils.formatRelativeFa(event.createdAt),
                                    style = AppTypography.labelSmall,
                                    color = DfThemeColors.textMuted(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
