package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.network.TeamLeadDto
import ir.divarfiling.mobile.core.network.TeamMemberDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamInboxUiState(
    val leads: List<TeamLeadDto> = emptyList(),
    val members: List<TeamMemberDto> = emptyList(),
    val filter: String = AgencyInboxFilter.ALL,
    val assignLeadId: Long? = null,
    val assigneeId: Long? = null,
    val showAssign: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class TeamInboxViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamInboxUiState())
    val uiState: StateFlow<TeamInboxUiState> = _uiState.asStateFlow()

    init { refresh(true) }

    fun refresh(initial: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = initial && it.leads.isEmpty(),
                    isRefreshing = !initial || it.leads.isNotEmpty(),
                    error = null,
                )
            }
            when (val result = repository.getLeadInbox(_uiState.value.filter)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        leads = result.data.leads,
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

    fun setFilter(filter: String) {
        _uiState.update { it.copy(filter = filter) }
        refresh(true)
    }

    fun openAssignLead(leadId: Long) {
        viewModelScope.launch {
            when (val members = repository.getMembers(excludeSelf = false)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        showAssign = true,
                        assignLeadId = leadId,
                        members = members.data.members,
                        assigneeId = members.data.members.firstOrNull()?.id,
                    )
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = members.message) }
            }
        }
    }

    fun dismissAssign() = _uiState.update { it.copy(showAssign = false, assignLeadId = null) }
    fun onAssigneeSelect(id: Long) = _uiState.update { it.copy(assigneeId = id) }

    fun confirmAssign() {
        val state = _uiState.value
        val leadId = state.assignLeadId
        val memberId = state.assigneeId
        if (leadId == null || memberId == null) {
            _uiState.update { it.copy(error = "مشاور را انتخاب کنید") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = repository.assignLead(leadId, memberId)) {
                is ApiResult.Success -> {
                    val memberName = state.members.firstOrNull { it.id == memberId }?.name ?: "مشاور"
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showAssign = false,
                            assignLeadId = null,
                            successMessage = "سرنخ به $memberName واگذار شد",
                        )
                    }
                    refresh()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamInboxScreen(
    onBack: () -> Unit,
    onOpenContact: (Long) -> Unit = {},
    viewModel: TeamInboxViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val pad = teamHorizontalPadding()

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    if (state.showAssign) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissAssign) {
            DfSheetScaffold(
                title = "تخصیص سرنخ",
                subtitle = "${state.leads.size} سرنخ",
                onClose = viewModel::dismissAssign,
                footer = {
                    DfSheetActions(
                        primaryText = if (state.isSubmitting) "در حال تخصیص…" else "واگذاری",
                        onPrimary = viewModel::confirmAssign,
                        primaryEnabled = !state.isSubmitting && state.assigneeId != null,
                        isSubmitting = state.isSubmitting,
                        onSecondary = viewModel::dismissAssign,
                    )
                },
            ) {
                DfSheetSection(title = "مشاور مقصد") {
                    TeamMemberSelectList(
                        members = state.members,
                        selectedId = state.assigneeId,
                        onSelect = viewModel::onAssigneeSelect,
                        emptyLabel = "عضوی برای تخصیص نیست",
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            TeamAmbientBackground()
            DfPullRefresh(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize().statusBarsPadding(),
            ) {
                LazyColumn(
                    contentPadding = teamListContentPadding(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                ) {
                    item {
                        DfHubPageHeader(
                            title = "صندوق سرنخ",
                            subtitle = "${state.leads.size} سرنخ در صف تخصیص",
                            sectionLabel = DfHeaderSections.TEAM,
                            titleIconRes = DfDecorIcons.Users,
                            onBack = onBack,
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = pad),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            listOf(
                                AgencyInboxFilter.ALL to "همه",
                                AgencyInboxFilter.FRESH to "تازه",
                                AgencyInboxFilter.STALE to "نیازمند پیگیری",
                            ).forEach { (key, label) ->
                                ir.divarfiling.mobile.core.design.components.DfSoftChip(
                                    text = label,
                                    selected = state.filter == key,
                                    onClick = { viewModel.setFilter(key) },
                                )
                            }
                        }
                    }
                    when {
                        state.isLoading -> item {
                            DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad))
                        }
                        state.leads.isEmpty() -> item {
                            DfEmptyState(
                                title = "سرنخ جدیدی وجود ندارد",
                                subtitle = "صف تخصیص تیم خالی است.",
                                variant = DfEmptyVariant.Empty,
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                        else -> items(state.leads, key = { it.id }) { lead ->
                            TeamLeadListCard(
                                lead = lead,
                                selected = false,
                                onToggle = { },
                                modifier = Modifier.padding(horizontal = pad),
                                onAssign = { viewModel.openAssignLead(lead.id) },
                                onOpenContact = { onOpenContact(lead.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
