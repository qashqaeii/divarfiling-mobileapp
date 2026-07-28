package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfFilterChipRow
import ir.divarfiling.mobile.core.design.components.DfFilterOption
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
    val selectedIds: Set<Long> = emptySet(),
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
            when (val result = repository.getLeadInbox()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        leads = result.data.leads,
                        selectedIds = emptySet(),
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

    fun toggleLead(id: Long) {
        _uiState.update {
            val next = it.selectedIds.toMutableSet()
            if (!next.add(id)) next.remove(id)
            it.copy(selectedIds = next)
        }
    }

    fun openAssign() {
        viewModelScope.launch {
            when (val members = repository.getMembers(excludeSelf = false)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        showAssign = true,
                        members = members.data.members.filter { m -> m.role != "owner" || true },
                        assigneeId = members.data.members.firstOrNull()?.id,
                    )
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = members.message) }
            }
        }
    }

    fun dismissAssign() = _uiState.update { it.copy(showAssign = false) }
    fun onAssigneeSelect(id: Long) = _uiState.update { it.copy(assigneeId = id) }

    fun assignSelected() {
        val state = _uiState.value
        val memberId = state.assigneeId
        if (memberId == null || state.selectedIds.isEmpty()) {
            _uiState.update { it.copy(error = "سرنخ و مشاور را انتخاب کنید") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = repository.assignLeads(memberId, state.selectedIds.toList())) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showAssign = false,
                            successMessage = "سرنخ‌ها تخصیص داده شدند",
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
    viewModel: TeamInboxViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    if (state.showAssign) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissAssign) {
            DfSheetScaffold(
                title = "تخصیص سرنخ",
                subtitle = "${state.selectedIds.size} مورد انتخاب شده",
                onClose = viewModel::dismissAssign,
                footer = {
                    DfSheetActions(
                        primaryText = if (state.isSubmitting) "در حال تخصیص…" else "تخصیص",
                        onPrimary = viewModel::assignSelected,
                        primaryEnabled = !state.isSubmitting && state.assigneeId != null,
                        isSubmitting = state.isSubmitting,
                        onSecondary = viewModel::dismissAssign,
                    )
                },
            ) {
                DfSheetSection(title = "مشاور مقصد") {
                    DfFilterChipRow(
                        options = state.members.map { DfFilterOption(it.id, it.name) },
                        selected = state.assigneeId ?: -1L,
                        onSelect = viewModel::onAssigneeSelect,
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(padding).statusBarsPadding(),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "صندوق سرنخ",
                        subtitle = "${state.leads.size} سرنخ در صف تخصیص",
                        titleIconRes = DfDecorIcons.Users,
                        onBack = onBack,
                    )
                }
                if (state.selectedIds.isNotEmpty()) {
                    item {
                        DfPrimaryButton(
                            text = "تخصیص ${state.selectedIds.size} سرنخ",
                            onClick = viewModel::openAssign,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                when {
                    state.isLoading -> item {
                        DfCardListSkeleton(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal))
                    }
                    state.leads.isEmpty() -> item {
                        DfEmptyState(
                            title = "صف خالی است",
                            subtitle = "سرنخ تخصیص‌نیافته‌ای در صندوق تیم نیست.",
                            variant = DfEmptyVariant.Empty,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                    else -> items(state.leads, key = { it.id }) { lead ->
                        LeadRow(
                            lead = lead,
                            selected = lead.id in state.selectedIds,
                            onToggle = { viewModel.toggleLead(lead.id) },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeadRow(
    lead: TeamLeadDto,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfCard(onClick = onToggle, modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Checkbox(checked = selected, onCheckedChange = { onToggle() })
            Column(modifier = Modifier.weight(1f)) {
                Text(lead.name.ifBlank { "بدون نام" }, fontWeight = FontWeight.Bold)
                if (lead.phone.isNotBlank()) {
                    Text(lead.phone, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
                }
                Text(
                    listOfNotNull(
                        lead.source.takeIf { it.isNotBlank() },
                        DateUtils.formatRelativeFa(lead.createdAt).takeIf { it.isNotBlank() },
                    ).joinToString(" · "),
                    style = AppTypography.labelSmall,
                    color = DfColors.TextSecondary,
                )
            }
        }
    }
}
