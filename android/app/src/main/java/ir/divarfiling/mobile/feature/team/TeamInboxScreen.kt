package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfSearchField
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfConfirmBottomSheet
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
    val query: String = "",
    val selectMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val assignLeadId: Long? = null,
    val assigneeId: Long? = null,
    val showAssign: Boolean = false,
    val showAssignConfirm: Boolean = false,
    val bulkAssign: Boolean = false,
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

    fun setQuery(value: String) = _uiState.update { it.copy(query = value) }

    fun toggleSelectMode() = _uiState.update {
        it.copy(selectMode = !it.selectMode, selectedIds = emptySet(), bulkAssign = false)
    }

    fun toggleSelected(leadId: Long) = _uiState.update { state ->
        val next = if (leadId in state.selectedIds) state.selectedIds - leadId else state.selectedIds + leadId
        state.copy(selectedIds = next)
    }

    fun openBulkAssign() {
        viewModelScope.launch {
            when (val members = repository.getMembers(excludeSelf = false)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        showAssign = true,
                        bulkAssign = true,
                        assignLeadId = null,
                        members = members.data.members,
                        assigneeId = null,
                    )
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = members.message) }
            }
        }
    }

    fun openAssignLead(leadId: Long) {
        viewModelScope.launch {
            when (val members = repository.getMembers(excludeSelf = false)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        showAssign = true,
                        bulkAssign = false,
                        assignLeadId = leadId,
                        members = members.data.members,
                        assigneeId = null,
                    )
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = members.message) }
            }
        }
    }

    fun dismissAssign() = _uiState.update {
        it.copy(
            showAssign = false,
            showAssignConfirm = false,
            assignLeadId = null,
            bulkAssign = false,
        )
    }
    fun onAssigneeSelect(id: Long) = _uiState.update { it.copy(assigneeId = id) }

    fun requestAssignConfirm() = _uiState.update { it.copy(showAssignConfirm = true) }
    fun dismissAssignConfirm() = _uiState.update { it.copy(showAssignConfirm = false) }

    fun confirmAssign() {
        val state = _uiState.value
        val memberId = state.assigneeId
        if (memberId == null) {
            _uiState.update { it.copy(error = "مشاور را انتخاب کنید") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val memberName = state.members.firstOrNull { it.id == memberId }?.name ?: "مشاور"
            val result = if (state.bulkAssign) {
                val ids = state.selectedIds.toList()
                if (ids.isEmpty()) {
                    _uiState.update { it.copy(isSubmitting = false, error = "حداقل یک سرنخ انتخاب کنید") }
                    return@launch
                }
                repository.assignLeads(memberId, ids)
            } else {
                val leadId = state.assignLeadId
                if (leadId == null) {
                    _uiState.update { it.copy(isSubmitting = false, error = "سرنخ نامعتبر است") }
                    return@launch
                }
                repository.assignLead(leadId, memberId)
            }
            when (result) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showAssign = false,
                            showAssignConfirm = false,
                            assignLeadId = null,
                            bulkAssign = false,
                            selectMode = false,
                            selectedIds = emptySet(),
                            successMessage = if (state.bulkAssign) {
                                "سرنخ‌ها به $memberName واگذار شدند"
                            } else {
                                "سرنخ به $memberName واگذار شد"
                            },
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

    val assignLead = state.leads.firstOrNull { it.id == state.assignLeadId }

    if (state.showAssignConfirm) {
        val memberName = state.members.firstOrNull { it.id == state.assigneeId }?.name ?: "مشاور"
        val message = if (state.bulkAssign) {
            "${DateUtils.toPersianDigits(state.selectedIds.size.toString())} سرنخ به $memberName واگذار شود؟"
        } else {
            "سرنخ به $memberName واگذار شود؟"
        }
        DfConfirmBottomSheet(
            title = "تأیید واگذاری",
            message = message,
            onConfirm = viewModel::confirmAssign,
            onDismiss = viewModel::dismissAssignConfirm,
            isSubmitting = state.isSubmitting,
        )
    }

    var assignMemberQuery by remember { mutableStateOf("") }

    if (state.showAssign) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissAssign) {
            DfSheetScaffold(
                title = if (state.bulkAssign) "تخصیص گروهی" else "تخصیص سرنخ",
                subtitle = when {
                    state.bulkAssign -> "انتخاب مشاور برای ${DateUtils.toPersianDigits(state.selectedIds.size.toString())} سرنخ"
                    assignLead != null -> assignLead.name.ifBlank { assignLead.phone }
                    else -> "انتخاب مشاور مقصد"
                },
                icon = DfIcons.UserPlus,
                iconContainerColor = DfThemeColors.successContainer(),
                iconTint = DfThemeColors.success(),
                onClose = viewModel::dismissAssign,
                footer = {
                    DfSheetActions(
                        primaryText = "ادامه",
                        onPrimary = viewModel::requestAssignConfirm,
                        primaryEnabled = !state.isSubmitting && state.assigneeId != null,
                        isSubmitting = state.isSubmitting,
                        onSecondary = viewModel::dismissAssign,
                    )
                },
            ) {
                TeamMessagesComposeSheetContent(
                    members = state.members,
                    query = assignMemberQuery,
                    onQueryChange = { assignMemberQuery = it },
                    selectedMemberId = state.assigneeId,
                    onMemberSelect = viewModel::onAssigneeSelect,
                    body = "",
                    onBodyChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    showMessageField = false,
                )
            }
        }
    }

    val filteredLeads = remember(state.leads, state.query) {
        val q = state.query.trim()
        if (q.isBlank()) {
            state.leads
        } else {
            state.leads.filter { lead ->
                lead.name.contains(q, ignoreCase = true) ||
                    lead.phone.contains(q) ||
                    lead.needSummary.contains(q, ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (state.selectMode && state.selectedIds.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Text(
                        "${DateUtils.toPersianDigits(state.selectedIds.size.toString())} سرنخ انتخاب شده",
                    )
                    DfPrimaryButton(
                        text = "واگذاری گروهی",
                        onClick = viewModel::openBulkAssign,
                        enabled = !state.isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
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
                        TeamAgencyScreenHeader(
                            title = "صندوق سرنخ",
                            subtitle = "${DateUtils.toPersianDigits(state.leads.size.toString())} سرنخ در صف",
                            onBack = onBack,
                        )
                    }
                    item {
                        DfSearchField(
                            value = state.query,
                            onValueChange = viewModel::setQuery,
                            placeholder = "جستجو در سرنخ‌ها",
                            modifier = Modifier.padding(horizontal = pad),
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
                    item {
                        DfSecondaryButton(
                            text = if (state.selectMode) "لغو انتخاب گروهی" else "انتخاب گروهی",
                            onClick = viewModel::toggleSelectMode,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = pad),
                        )
                    }
                    state.error?.let { err ->
                        if (!state.isLoading) {
                            item {
                                DfErrorBanner(
                                    message = err,
                                    modifier = Modifier.padding(horizontal = pad),
                                )
                            }
                        }
                    }
                    when {
                        state.isLoading -> item {
                            DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad))
                        }
                        filteredLeads.isEmpty() && state.error == null -> item {
                            DfEmptyState(
                                title = if (state.query.isNotBlank()) "نتیجه‌ای نیست" else "سرنخ جدیدی وجود ندارد",
                                subtitle = if (state.query.isNotBlank()) {
                                    "عبارت جستجو را تغییر دهید."
                                } else {
                                    "صف تخصیص تیم خالی است."
                                },
                                variant = DfEmptyVariant.Empty,
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                        else -> items(filteredLeads, key = { it.id }) { lead ->
                            TeamLeadListCard(
                                lead = lead,
                                selected = lead.id in state.selectedIds,
                                selectable = state.selectMode,
                                onToggle = { viewModel.toggleSelected(lead.id) },
                                modifier = Modifier.padding(horizontal = pad),
                                onAssign = if (!state.selectMode && lead.assignedTo == null) {
                                    { viewModel.openAssignLead(lead.id) }
                                } else {
                                    null
                                },
                                onOpenContact = if (!state.selectMode) {
                                    { onOpenContact(lead.id) }
                                } else {
                                    null
                                },
                                assigneeHint = if (lead.assignedTo != null) "دارای مسئول" else null,
                            )
                        }
                    }
                }
            }
        }
    }
}
