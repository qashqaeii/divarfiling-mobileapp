package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ir.divarfiling.mobile.core.design.components.showDfMessage
import ir.divarfiling.mobile.core.design.components.DfConfirmBottomSheet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import ir.divarfiling.mobile.core.design.DfThemeColors
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.AppLinks
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.network.AgencyAdvisorDetailPayload
import ir.divarfiling.mobile.core.network.TeamMemberDto
import ir.divarfiling.mobile.core.network.AgencyAdvisorDto
import ir.divarfiling.mobile.core.network.AgencyInvitationDto
import ir.divarfiling.mobile.core.network.AgencyPerformanceMemberDto
import ir.divarfiling.mobile.core.network.AgencySeatsPayload
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.TeamRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

object AgencyAdvisorFilter {
    const val ALL = ""
    const val ACTIVE = "active"
    const val INACTIVE = "inactive"
    const val INVITED = "invited"
    const val NO_SEAT = "no_seat"
}

object AgencyPerformanceRange {
    fun daysFor(key: String): Int = when (key) {
        "today" -> 1
        "month" -> 30
        else -> 7
    }
}

object AgencyInboxFilter {
    const val ALL = "all"
    const val FRESH = "fresh"
    const val STALE = "stale"
}

data class AgencyAdvisorsUiState(
    val advisors: List<AgencyAdvisorDto> = emptyList(),
    val invitations: List<AgencyInvitationDto> = emptyList(),
    val query: String = "",
    val statusFilter: String = AgencyAdvisorFilter.ALL,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val showInvite: Boolean = false,
    val invitePhone: String = "",
    val inviteSubmitting: Boolean = false,
    val successMessage: String? = null,
    val pendingCancelInvitationId: Long? = null,
    val cancelInvitationSubmitting: Boolean = false,
)

@HiltViewModel
class AgencyAdvisorsViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgencyAdvisorsUiState())
    val uiState: StateFlow<AgencyAdvisorsUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null
    private var lastRosterVersion = 0

    init { load(initial = true) }

    fun load(initial: Boolean = false) {
        viewModelScope.launch {
            val rosterChanged = lastRosterVersion != repository.agencyRosterInvalidation
            lastRosterVersion = repository.agencyRosterInvalidation
            val state = _uiState.value
            val forceRefresh = rosterChanged && !initial
            _uiState.update {
                it.copy(
                    isLoading = (initial || forceRefresh) && it.advisors.isEmpty(),
                    isRefreshing = forceRefresh || !initial || it.advisors.isNotEmpty(),
                    error = null,
                )
            }
            when (val result = repository.getAgencyAdvisors(state.query, state.statusFilter)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        advisors = result.data.advisors,
                        invitations = result.data.invitations,
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

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            load()
        }
    }

    fun setStatusFilter(filter: String) {
        _uiState.update { it.copy(statusFilter = filter) }
        load()
    }

    fun openInvite() = _uiState.update { it.copy(showInvite = true, invitePhone = "") }
    fun dismissInvite() = _uiState.update { it.copy(showInvite = false) }
    fun onInvitePhoneChange(v: String) = _uiState.update { it.copy(invitePhone = v) }

    fun submitInvite() {
        if (_uiState.value.inviteSubmitting) return
        val phone = ir.divarfiling.mobile.core.util.PhoneNormalizer.normalize(_uiState.value.invitePhone)
        if (!ir.divarfiling.mobile.core.util.PhoneNormalizer.isValidIranMobile(phone)) {
            _uiState.update { it.copy(error = "شماره موبایل ایران معتبر وارد کنید (مثلاً ۰۹۱۲۱۲۳۴۵۶۷)") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(inviteSubmitting = true) }
            when (val result = repository.createInvitation(phone)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        inviteSubmitting = false,
                        showInvite = false,
                        successMessage = "دعوت ارسال شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(inviteSubmitting = false, error = result.message)
                }
            }
            load()
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }

    fun resendInvitation(id: Long) {
        viewModelScope.launch {
            when (val result = repository.invitationAction(id, "resend")) {
                is ApiResult.Success -> _uiState.update { it.copy(successMessage = "پیامک دوباره ارسال شد") }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
            load()
        }
    }

    fun requestCancelInvitation(id: Long) {
        _uiState.update { it.copy(pendingCancelInvitationId = id) }
    }

    fun dismissCancelInvitation() {
        _uiState.update { it.copy(pendingCancelInvitationId = null) }
    }

    fun confirmCancelInvitation() {
        val id = _uiState.value.pendingCancelInvitationId ?: return
        if (_uiState.value.cancelInvitationSubmitting) return
        viewModelScope.launch {
            _uiState.update { it.copy(cancelInvitationSubmitting = true) }
            when (val result = repository.invitationAction(id, "cancel")) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        cancelInvitationSubmitting = false,
                        pendingCancelInvitationId = null,
                        invitations = it.invitations.filter { inv -> inv.id != id },
                        successMessage = "دعوت حذف شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        cancelInvitationSubmitting = false,
                        error = result.message,
                    )
                }
            }
            load()
        }
    }
}

data class AgencyAdvisorDetailUiState(
    val detail: AgencyAdvisorDetailPayload? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val showRoleSheet: Boolean = false,
    val showDeactivateConfirm: Boolean = false,
    val pendingRole: String? = null,
    /** خطای بارگذاری پروفایل مشاور */
    val detailError: String? = null,
    /** خطای عملیات مدیریتی (نقش، غیرفعال‌سازی) */
    val error: String? = null,
    val successMessage: String? = null,
    val workspaceTab: Advisor360Tab = Advisor360Tab.OVERVIEW,
    val workspace: ir.divarfiling.mobile.core.network.AgencyAdvisorWorkspacePayload? = null,
    val workspaceLoading: Boolean = false,
    val workspaceError: String? = null,
    val workspaceForbidden: Boolean = false,
    val workspaceQuery: String = "",
    val workspacePage: Int = 1,
    val periodDays: Int = 30,
    val filingDatasetId: String? = null,
    val memberId: Long = 0L,
    val showManageActions: Boolean = false,
)

@HiltViewModel
class AgencyAdvisorDetailViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgencyAdvisorDetailUiState())
    val uiState: StateFlow<AgencyAdvisorDetailUiState> = _uiState.asStateFlow()
    private var workspaceSearchJob: kotlinx.coroutines.Job? = null

    fun load(memberId: Long) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = it.detail == null,
                    detailError = null,
                    workspaceError = null,
                    workspaceForbidden = false,
                    memberId = memberId,
                    workspacePage = 1,
                    workspaceTab = Advisor360Tab.OVERVIEW,
                    filingDatasetId = null,
                    workspace = null,
                )
            }
            when (val result = repository.getAgencyAdvisorDetail(memberId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(detail = result.data, isLoading = false, detailError = null)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, detailError = result.message)
                }
            }
            loadWorkspace(memberId, reset = true)
        }
    }

    fun selectWorkspaceTab(tab: Advisor360Tab) {
        _uiState.update {
            it.copy(
                workspaceTab = tab,
                workspacePage = 1,
                filingDatasetId = if (tab == Advisor360Tab.FILING) it.filingDatasetId else null,
            )
        }
        loadWorkspace(_uiState.value.memberId, reset = true)
    }

    fun onWorkspaceQueryChange(query: String) {
        _uiState.update { it.copy(workspaceQuery = query, workspacePage = 1) }
        workspaceSearchJob?.cancel()
        workspaceSearchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(350)
            loadWorkspace(_uiState.value.memberId, reset = true)
        }
    }

    fun setPeriodDays(days: Int) {
        _uiState.update { it.copy(periodDays = days, workspacePage = 1) }
        loadWorkspace(_uiState.value.memberId, reset = true)
    }

    fun openFilingDataset(id: String) {
        if (id.isBlank()) return
        _uiState.update {
            it.copy(filingDatasetId = id, workspacePage = 1, workspaceError = null, workspaceForbidden = false)
        }
        loadWorkspace(_uiState.value.memberId, reset = true)
    }

    fun closeFilingDataset() {
        _uiState.update { it.copy(filingDatasetId = null, workspacePage = 1) }
        loadWorkspace(_uiState.value.memberId, reset = true)
    }

    fun loadMoreWorkspace() {
        val state = _uiState.value
        if (state.workspacePage >= (state.workspace?.numPages ?: 1)) return
        _uiState.update { it.copy(workspacePage = it.workspacePage + 1) }
        loadWorkspace(state.memberId, reset = false)
    }

    private fun loadWorkspace(memberId: Long, reset: Boolean) {
        val state = _uiState.value
        if (state.detail?.canViewWorkspace != true) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    workspaceLoading = true,
                    workspace = if (reset) null else it.workspace,
                    workspaceError = null,
                    workspaceForbidden = false,
                )
            }
            val params = buildMap {
                put("tab", state.workspaceTab.apiValue)
                put("page", state.workspacePage.coerceAtLeast(1).toString())
                put("period", state.periodDays.toString())
                if (state.workspaceQuery.isNotBlank()) put("q", state.workspaceQuery.trim())
                state.filingDatasetId?.let { put("dataset_id", it) }
            }
            when (val result = repository.getAgencyAdvisorWorkspace(memberId, params)) {
                is ApiResult.Success -> {
                    val merged = if (reset || state.workspace == null) {
                        result.data
                    } else {
                        result.data.copy(
                            items = state.workspace.items + result.data.items,
                            datasets = state.workspace.datasets + result.data.datasets,
                            listings = state.workspace.listings + result.data.listings,
                        )
                    }
                    _uiState.update {
                        it.copy(
                            workspace = merged,
                            workspaceLoading = false,
                            workspaceError = null,
                            workspaceForbidden = false,
                        )
                    }
                }
                is ApiResult.Error -> {
                    val forbidden = result.code == "FORBIDDEN" ||
                        (
                            result.code != "PARSE_ERROR" &&
                                result.code != "NETWORK_ERROR" &&
                                result.message.contains("مجاز نیست")
                            )
                    val message = if (forbidden) {
                        result.message.ifBlank {
                            "دسترسی به داده این مشاور مجاز نیست. رضایت‌نامه دیداری داده یا اشتراک آژانس را بررسی کنید."
                        }
                    } else {
                        result.message
                    }
                    _uiState.update {
                        it.copy(
                            workspaceLoading = false,
                            workspaceError = message,
                            workspaceForbidden = forbidden,
                        )
                    }
                }
            }
        }
    }

    fun retryWorkspace() = loadWorkspace(_uiState.value.memberId, reset = true)

    fun openRoleSheet() = _uiState.update { it.copy(showRoleSheet = true, pendingRole = null) }

    fun dismissRoleSheet() = _uiState.update { it.copy(showRoleSheet = false, pendingRole = null) }

    fun selectPendingRole(role: String) = _uiState.update { it.copy(pendingRole = role) }

    fun confirmSetRole(memberId: Long) {
        val role = _uiState.value.pendingRole ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.advisorAction(memberId, "set_role", role)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showRoleSheet = false,
                            pendingRole = null,
                            successMessage = "نقش به‌روزرسانی شد",
                        )
                    }
                    load(memberId)
                    repository.invalidateAgencyRoster()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun openDeactivateConfirm() = _uiState.update { it.copy(showDeactivateConfirm = true) }

    fun dismissDeactivateConfirm() = _uiState.update { it.copy(showDeactivateConfirm = false) }

    fun deactivateMember(memberId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, showDeactivateConfirm = false) }
            when (val result = repository.advisorAction(memberId, "deactivate")) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "عضو غیرفعال شد") }
                    load(memberId)
                    repository.invalidateAgencyRoster()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }

    fun openManageActions() = _uiState.update { it.copy(showManageActions = true) }

    fun dismissManageActions() = _uiState.update { it.copy(showManageActions = false) }
}

data class AgencyPerformanceUiState(
    val rangeKey: String = "week",
    val members: List<AgencyPerformanceMemberDto> = emptyList(),
    val totals: Map<String, Int> = emptyMap(),
    val goals: List<ir.divarfiling.mobile.core.network.AgencyTeamGoalDto> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AgencyPerformanceViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgencyPerformanceUiState())
    val uiState: StateFlow<AgencyPerformanceUiState> = _uiState.asStateFlow()

    init { refresh(initial = true) }

    fun setRange(key: String) {
        _uiState.update { it.copy(rangeKey = key) }
        refresh()
    }

    fun refresh(initial: Boolean = false) {
        viewModelScope.launch {
            val days = AgencyPerformanceRange.daysFor(_uiState.value.rangeKey)
            _uiState.update {
                it.copy(
                    isLoading = initial && it.members.isEmpty(),
                    isRefreshing = !initial || it.members.isNotEmpty(),
                    error = null,
                )
            }
            when (val result = repository.getAgencyPerformance(days)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        members = result.data.members,
                        totals = result.data.totals,
                        goals = result.data.goals,
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

data class AgencySeatsUiState(
    val payload: AgencySeatsPayload? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    /** تخصیص: عضو انتخاب‌شده → انتخاب صندلی */
    val assignMember: AgencyAdvisorDto? = null,
    /** تخصیص: صندلی آزاد انتخاب‌شده → انتخاب عضو */
    val assignLicense: ir.divarfiling.mobile.core.network.AgencyAvailableLicenseDto? = null,
    val showAssign: Boolean = false,
    val releaseTarget: AgencyAdvisorDto? = null,
    val showReleaseConfirm: Boolean = false,
    val isSubmitting: Boolean = false,
)

@HiltViewModel
class AgencySeatsViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgencySeatsUiState())
    val uiState: StateFlow<AgencySeatsUiState> = _uiState.asStateFlow()

    init { refresh(true) }

    fun refresh(initial: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = initial && it.payload == null,
                    isRefreshing = !initial || it.payload != null,
                    error = null,
                )
            }
            when (val result = repository.getAgencySeats()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(payload = result.data, isLoading = false, isRefreshing = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
            }
        }
    }

    fun openAssignForMember(member: AgencyAdvisorDto) = _uiState.update {
        it.copy(showAssign = true, assignMember = member, assignLicense = null)
    }

    fun openAssignForLicense(license: ir.divarfiling.mobile.core.network.AgencyAvailableLicenseDto) =
        _uiState.update {
            it.copy(showAssign = true, assignLicense = license, assignMember = null)
        }

    fun dismissAssign() = _uiState.update {
        it.copy(showAssign = false, assignMember = null, assignLicense = null)
    }

    fun assignSeat(licenseId: Long, memberUserId: Long) {
        if (licenseId <= 0L || memberUserId <= 0L) {
            _uiState.update { it.copy(error = "صندلی و مشاور باید مشخص باشند") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = repository.mutateAgencySeat(licenseId, "assign", memberUserId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        showAssign = false,
                        assignMember = null,
                        assignLicense = null,
                        successMessage = "صندلی تخصیص یافت",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
            refresh()
            repository.invalidateAgencyRoster()
        }
    }

    fun openReleaseConfirm(member: AgencyAdvisorDto) {
        val licenseId = member.licenseId
        if (licenseId == null || licenseId <= 0L) {
            _uiState.update { it.copy(error = "شناسه صندلی این عضو در دسترس نیست") }
            return
        }
        _uiState.update { it.copy(showReleaseConfirm = true, releaseTarget = member) }
    }

    fun dismissReleaseConfirm() {
        _uiState.update { it.copy(showReleaseConfirm = false, releaseTarget = null) }
    }

    fun confirmReleaseSeat() {
        val licenseId = _uiState.value.releaseTarget?.licenseId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, showReleaseConfirm = false) }
            when (val result = repository.mutateAgencySeat(licenseId, "unassign")) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isSubmitting = false, successMessage = "صندلی آزاد شد", releaseTarget = null)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message, releaseTarget = null)
                }
            }
            refresh()
            repository.invalidateAgencyRoster()
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }
}

@HiltViewModel
class AgencyAdvancedSettingsViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    fun openWorkspace(path: String, onBridge: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val result = repository.createWorkspaceBridge(path)) {
                is ApiResult.Success -> onBridge(result.data.bridgeUrl)
                is ApiResult.Error -> onError(result.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyAdvisorDetailScreen(
    memberId: Long,
    onBack: () -> Unit,
    onOpenPerformance: () -> Unit = {},
    onOpenSeats: () -> Unit = {},
    onOpenContact: (Long) -> Unit = {},
    onOpenProperty: (Long) -> Unit = {},
    onOpenDeal: (Long) -> Unit = {},
    onOpenContract: (Long) -> Unit = {},
    onOpenListing: (String) -> Unit = {},
    viewModel: AgencyAdvisorDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(memberId) { viewModel.load(memberId) }
    LifecycleResumeEffect(memberId) {
        viewModel.load(memberId)
        onPauseOrDispose { }
    }
    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showDfMessage(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showDfMessage(it); viewModel.clearMessage() }
    }
    val pad = teamHorizontalPadding()
    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { TeamAgencySnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            TeamAmbientBackground()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = teamListContentPadding(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
            item {
                TeamAgencyScreenHeader(
                    title = AGENCY_ADVISOR_WORKSPACE_TITLE,
                    subtitle = if (state.isLoading) "در حال بارگذاری…" else "فعالیت، پرونده و صندلی",
                    icon = DfIcons.User,
                    accent = AgencyHeaderAccent.Detail,
                    onBack = onBack,
                )
            }
            when {
                state.isLoading -> item { DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad)) }
                state.detailError != null && state.detail == null -> item {
                    DfEmptyState(
                        title = "خطا",
                        subtitle = state.detailError ?: "",
                        variant = DfEmptyVariant.Error,
                        actionLabel = "تلاش مجدد",
                        onAction = { viewModel.load(memberId) },
                        modifier = Modifier.padding(horizontal = pad),
                    )
                }
                state.detail != null -> {
                    val d = state.detail!!
                    val tabCounts = mapOf(
                        Advisor360Tab.CONTACTS to d.sectionCounts.contacts,
                        Advisor360Tab.PROPERTIES to d.sectionCounts.properties,
                        Advisor360Tab.FILING to d.sectionCounts.datasets,
                        Advisor360Tab.DEALS to d.sectionCounts.deals,
                        Advisor360Tab.CONTRACTS to d.sectionCounts.contracts,
                    )
                    item {
                        AgencyAdvisorProfileHero(
                            name = d.member.name,
                            phone = d.member.phone,
                            roleLabel = d.member.roleLabel.ifBlank { d.member.role },
                            isActive = d.member.isActive,
                            seatSummary = d.seat.seatSummaryLabel(),
                            seatExpiry = d.seat.seatExpiryLabel(),
                            scorePct = d.performance.scorePct,
                            rank = d.performance.rank,
                            activeLeads = d.stats.activeLeads,
                            activeDeals = d.stats.activeDeals,
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                    item {
                        AgencyAdvisorWorkspaceSnapshot(
                            sectionCounts = d.sectionCounts,
                            calls = d.performance.calls,
                            visits = d.performance.visits,
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                    item {
                        AgencyAdvisor360Tabs(
                            selected = state.workspaceTab,
                            counts = tabCounts,
                            onSelect = viewModel::selectWorkspaceTab,
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                    item {
                        AgencyAdvisor360WorkspaceBody(
                            tab = state.workspaceTab,
                            detail = d,
                            workspace = state.workspace,
                            workspaceLoading = state.workspaceLoading,
                            workspaceError = state.workspaceError,
                            workspaceForbidden = state.workspaceForbidden,
                            workspaceQuery = state.workspaceQuery,
                            periodDays = state.periodDays,
                            filingDatasetId = state.filingDatasetId,
                            onQueryChange = viewModel::onWorkspaceQueryChange,
                            onPeriodChange = viewModel::setPeriodDays,
                            onOpenDataset = viewModel::openFilingDataset,
                            onBackFromDataset = viewModel::closeFilingDataset,
                            onLoadMore = viewModel::loadMoreWorkspace,
                            onRetryWorkspace = viewModel::retryWorkspace,
                            onOpenContact = onOpenContact,
                            onOpenProperty = onOpenProperty,
                            onOpenDeal = onOpenDeal,
                            onOpenContract = onOpenContract,
                            onOpenListing = onOpenListing,
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                    item {
                        DfPrimaryButton(
                            text = "مدیریت مشاور",
                            onClick = viewModel::openManageActions,
                            enabled = !state.isSubmitting,
                            modifier = Modifier
                                .padding(horizontal = pad)
                                .fillMaxWidth(),
                        )
                    }
                }
            }
            }
        }
    }

    if (state.showRoleSheet && state.detail != null) {
        val d = state.detail!!
        DfModalBottomSheet(onDismissRequest = viewModel::dismissRoleSheet) {
            DfSheetScaffold(
                title = "تغییر نقش",
                subtitle = "نقش فعلی: ${d.member.roleLabel.ifBlank { d.member.role }}",
                onClose = viewModel::dismissRoleSheet,
                footer = {
                    DfSheetActions(
                        primaryText = if (state.isSubmitting) "در حال ذخیره…" else "ذخیره نقش",
                        onPrimary = { viewModel.confirmSetRole(memberId) },
                        primaryEnabled = !state.isSubmitting && state.pendingRole != null &&
                            state.pendingRole != d.member.role,
                        isSubmitting = state.isSubmitting,
                        secondaryText = "انصراف",
                        onSecondary = viewModel::dismissRoleSheet,
                    )
                },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    d.assignableRoles.forEach { role ->
                        DfSoftChip(
                            text = role.label,
                            selected = state.pendingRole == role.value || (
                                state.pendingRole == null && d.member.role == role.value
                                ),
                            onClick = { viewModel.selectPendingRole(role.value) },
                        )
                    }
                }
            }
        }
    }

    if (state.showDeactivateConfirm) {
        DfConfirmBottomSheet(
            title = "غیرفعال کردن عضو",
            message = "انتشارهای فعال این عضو بایگانی می‌شود. ادامه می‌دهید؟",
            onConfirm = { viewModel.deactivateMember(memberId) },
            onDismiss = viewModel::dismissDeactivateConfirm,
            destructive = true,
            isSubmitting = state.isSubmitting,
        )
    }

    if (state.showManageActions && state.detail != null) {
        val d = state.detail!!
        DfModalBottomSheet(onDismissRequest = viewModel::dismissManageActions) {
            DfSheetScaffold(
                title = "مدیریت مشاور",
                subtitle = d.member.name,
                onClose = viewModel::dismissManageActions,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    if (d.permissions.canSetRole) {
                        DfSecondaryButton(
                            text = "تغییر نقش",
                            onClick = {
                                viewModel.dismissManageActions()
                                viewModel.openRoleSheet()
                            },
                            enabled = !state.isSubmitting,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (d.permissions.canManageSeat) {
                        DfSecondaryButton(
                            text = "مدیریت صندلی",
                            onClick = {
                                viewModel.dismissManageActions()
                                onOpenSeats()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    DfSecondaryButton(
                        text = "عملکرد در تیم",
                        onClick = {
                            viewModel.dismissManageActions()
                            onOpenPerformance()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (d.permissions.canManageMember && d.member.isActive) {
                        DfSecondaryButton(
                            text = "غیرفعال کردن عضو",
                            onClick = {
                                viewModel.dismissManageActions()
                                viewModel.openDeactivateConfirm()
                            },
                            enabled = !state.isSubmitting,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyPerformanceScreen(
    onBack: () -> Unit,
    onAdvisorClick: (Long) -> Unit = {},
    viewModel: AgencyPerformanceViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pad = teamHorizontalPadding()
    Scaffold(containerColor = DfScreenContainerColor) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            TeamAmbientBackground()
            DfPullRefresh(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                contentPadding = teamListContentPadding(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    TeamAgencyScreenHeader(
                        title = "عملکرد تیم",
                        subtitle = "خلاصه تصمیم‌یار",
                        icon = DfIcons.BarChart,
                        accent = AgencyHeaderAccent.Management,
                        onBack = onBack,
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = pad),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        listOf("today" to "امروز", "week" to "این هفته", "month" to "این ماه").forEach { (key, label) ->
                            DfSoftChip(
                                text = label,
                                selected = state.rangeKey == key,
                                onClick = { viewModel.setRange(key) },
                            )
                        }
                    }
                }
                item {
                    val rangeLabel = when (state.rangeKey) {
                        "today" -> "امروز"
                        "month" -> "این ماه"
                        else -> "این هفته"
                    }
                    AgencyPerformanceHero(
                        rangeLabel = rangeLabel,
                        totals = state.totals,
                        memberCount = state.members.size,
                        modifier = Modifier.padding(horizontal = pad),
                    )
                }
                if (state.members.size >= 2) {
                    item {
                        AgencyPerformanceScoreChart(
                            members = state.members,
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                }
                if (state.totals.isNotEmpty()) {
                    item {
                        AgencyPerformanceKpiStrip(
                            totals = state.totals,
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                }
                if (state.goals.isNotEmpty()) {
                    item {
                        AgencyPerformanceGoalsPanel(
                            goals = state.goals,
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                }
                when {
                    state.isLoading -> item { DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad)) }
                    state.error != null && state.members.isEmpty() -> item {
                        DfEmptyState(
                            title = "بارگذاری ناموفق",
                            subtitle = state.error ?: "",
                            variant = DfEmptyVariant.Error,
                            actionLabel = "تلاش مجدد",
                            onAction = { viewModel.refresh() },
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                    state.members.isEmpty() -> item {
                        DfEmptyState(
                            title = "در این بازه داده‌ای ثبت نشده",
                            subtitle = "فعالیت تیم را در روزهای آینده بررسی کنید.",
                            variant = DfEmptyVariant.Empty,
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                    else -> {
                        item {
                            Text(
                                "عملکرد هر مشاور",
                                modifier = Modifier.padding(horizontal = pad),
                                style = AppTypography.cardTitle,
                                fontWeight = FontWeight.Bold,
                                color = DfThemeColors.textPrimary(),
                            )
                        }
                        items(state.members, key = { it.memberId ?: it.name }) { row ->
                            AgencyPerformanceMemberCard(
                                rank = row.rank,
                                name = row.name,
                                scorePct = row.scorePct,
                                calls = row.calls,
                                visits = row.visits,
                                activeDeals = row.activeDeals,
                                modifier = Modifier.padding(horizontal = pad),
                                onClick = { row.memberId?.let(onAdvisorClick) },
                            )
                        }
                    }
                }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencySeatsScreen(
    onBack: () -> Unit,
    viewModel: AgencySeatsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val pad = teamHorizontalPadding()
    LaunchedEffect(state.error, state.successMessage) {
        state.successMessage?.let { snackbar.showDfMessage(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showDfMessage(it); viewModel.clearMessage() }
    }
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    if (state.showAssign) {
        val licenses = state.payload?.availableLicenses.orEmpty()
        val unassigned = state.payload?.members.orEmpty().filter { it.eligibleForSeatAssignment() }
        DfModalBottomSheet(onDismissRequest = viewModel::dismissAssign) {
            when {
                state.assignMember != null -> {
                    val target = state.assignMember!!
                    DfSheetScaffold(
                        title = "انتخاب صندلی",
                        subtitle = "تخصیص به ${target.name}",
                        onClose = viewModel::dismissAssign,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            Text(
                                "یک صندلی آزاد را برای این مشاور انتخاب کنید.",
                                style = ir.divarfiling.mobile.core.design.AppTypography.bodyDescription,
                                color = DfThemeColors.textSecondary(),
                            )
                            if (licenses.isEmpty()) {
                                DfEmptyState(
                                    title = "صندلی آزاد نیست",
                                    subtitle = "ابتدا صندلی آزاد کنید یا لایسنس جدید بخرید.",
                                    variant = DfEmptyVariant.Empty,
                                )
                            } else {
                                licenses.forEach { lic ->
                                    AgencySeatLicensePickCard(
                                        license = lic,
                                        enabled = !state.isSubmitting,
                                        onClick = { viewModel.assignSeat(lic.id, target.userId) },
                                    )
                                }
                            }
                        }
                    }
                }
                state.assignLicense != null -> {
                    val license = state.assignLicense!!
                    DfSheetScaffold(
                        title = "انتخاب مشاور",
                        subtitle = "صندلی ${DateUtils.toPersianDigits(license.sequence.toString())}",
                        onClose = viewModel::dismissAssign,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            Text(
                                "مشاور بدون صندلی را برای این لایسنس انتخاب کنید.",
                                style = ir.divarfiling.mobile.core.design.AppTypography.bodyDescription,
                                color = DfThemeColors.textSecondary(),
                            )
                            if (unassigned.isEmpty()) {
                                DfEmptyState(
                                    title = "عضو بدون صندلی نیست",
                                    subtitle = "ابتدا مشاور جدید دعوت کنید یا صندلی آزاد کنید.",
                                    variant = DfEmptyVariant.Empty,
                                )
                            } else {
                                TeamMemberSelectList(
                                    members = unassigned.map { it.toTeamMemberDto() },
                                    selectedId = null,
                                    onSelect = { memberId ->
                                        val member = unassigned.firstOrNull { it.id == memberId }
                                        if (member != null && member.userId > 0L) {
                                            viewModel.assignSeat(license.id, member.userId)
                                        }
                                    },
                                    emptyLabel = "عضوی برای تخصیص نیست",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (state.showReleaseConfirm && state.releaseTarget != null) {
        val target = state.releaseTarget!!
        DfConfirmBottomSheet(
            title = "آزادسازی صندلی",
            message = "صندلی ${target.seatSummaryLabel()} از ${target.name} آزاد می‌شود. ادامه می‌دهید؟",
            onConfirm = viewModel::confirmReleaseSeat,
            onDismiss = viewModel::dismissReleaseConfirm,
            destructive = true,
            isSubmitting = state.isSubmitting,
        )
    }
    Scaffold(containerColor = DfScreenContainerColor, snackbarHost = { TeamAgencySnackbarHost(snackbar) }) { padding ->
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
                    TeamAgencyScreenHeader(
                        title = "لایسنس‌ها و صندلی‌ها",
                        subtitle = state.payload?.seats?.let {
                            "${DateUtils.toPersianDigits(it.assigned.toString())} از ${DateUtils.toPersianDigits(it.total.toString())} تخصیص"
                        } ?: "",
                        icon = DfIcons.Lock,
                        accent = AgencyHeaderAccent.Management,
                        onBack = onBack,
                    )
                }
                item {
                    TeamSeatsOverviewBar(
                        seats = state.payload?.seats,
                        modifier = Modifier.padding(horizontal = pad),
                    )
                }
                when {
                    state.isLoading -> item { DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad)) }
                    state.payload?.members.isNullOrEmpty() -> item {
                        DfEmptyState(
                            title = "عضوی برای مدیریت صندلی نیست",
                            subtitle = "ابتدا مشاور را دعوت و فعال کنید.",
                            variant = DfEmptyVariant.Empty,
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                    else -> {
                        val assigned = state.payload?.members.orEmpty()
                            .filter { it.hasSeatAssignment() }
                            .sortedBy { it.sequence ?: Int.MAX_VALUE }
                        val unassigned = state.payload?.members.orEmpty()
                            .filter { it.eligibleForSeatAssignment() }
                        val eligibleCount = unassigned.size
                        if (assigned.isNotEmpty()) {
                            item {
                                TeamSectionLabel(title = "صندلی‌های تخصیص‌یافته", modifier = Modifier.padding(horizontal = pad))
                            }
                            items(assigned, key = { "seat-${it.id}" }) { member ->
                                AgencySeatOccupiedRow(
                                    member = member,
                                    onRelease = { viewModel.openReleaseConfirm(member) },
                                    modifier = Modifier.padding(horizontal = pad),
                                )
                            }
                        }
                        val freeLicenses = state.payload?.availableLicenses.orEmpty()
                        if (freeLicenses.isNotEmpty()) {
                            item {
                                TeamSectionLabel(
                                    title = "صندلی‌های آزاد",
                                    subtitle = "${DateUtils.toPersianDigits(freeLicenses.size.toString())} صندلی",
                                    modifier = Modifier.padding(horizontal = pad),
                                )
                            }
                            items(freeLicenses, key = { "free-${it.id}" }) { lic ->
                                AgencySeatFreeRow(
                                    license = lic,
                                    onAssign = { viewModel.openAssignForLicense(lic) },
                                    unassignedCount = eligibleCount,
                                    modifier = Modifier.padding(horizontal = pad),
                                )
                            }
                        }
                        if (unassigned.isNotEmpty()) {
                            item {
                                TeamSectionLabel(title = "اعضا بدون صندلی", modifier = Modifier.padding(horizontal = pad))
                            }
                            items(unassigned, key = { "no-seat-${it.id}" }) { member ->
                                AgencyMemberWithoutSeatRow(
                                    member = member,
                                    onAssign = { viewModel.openAssignForMember(member) },
                                    modifier = Modifier.padding(horizontal = pad),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgencyAdvancedSettingsScreen(
    onBack: () -> Unit,
    onOpenActivity: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenAudit: () -> Unit = {},
    onOpenBridge: (String) -> Unit,
    viewModel: AgencyAdvancedSettingsViewModel = hiltViewModel(),
) {
    val pad = teamHorizontalPadding()
    val webRows = listOf(
        Triple("اتصال وب‌هوک", "تنظیم آدرس و کلید امنیتی", AppLinks.WORKSPACE_AGENCY_WEBHOOKS),
        Triple("همگام‌سازی داده", "انتقال و یکپارچه‌سازی CRM", AppLinks.WORKSPACE_AGENCY_SYNC),
        Triple("تابلوی نمایش", "نمای زنده عملکرد آژانس", "/workspace/crm/team/tv/"),
    )
    Scaffold(containerColor = DfScreenContainerColor) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            TeamAmbientBackground()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = teamListContentPadding(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            item {
                TeamAgencyScreenHeader(
                    title = "تنظیمات آژانس",
                    subtitle = "مدیریت در اپ و میزکار",
                    icon = DfIcons.Settings,
                    accent = AgencyHeaderAccent.Management,
                    onBack = onBack,
                )
            }
            item {
                TeamSectionLabel(title = "در اپلیکیشن", modifier = Modifier.padding(horizontal = pad))
            }
            item {
                TeamSettingsNavRow(
                    title = "فعالیت آژانس",
                    subtitle = "Timeline رویدادها و فیلتر مشاور",
                    icon = DfIcons.Clock,
                    onClick = onOpenActivity,
                    modifier = Modifier.padding(horizontal = pad),
                )
            }
            item {
                TeamSettingsNavRow(
                    title = "پروفایل آژانس",
                    subtitle = "نام آژانس و مسیر سرنخ",
                    icon = DfIcons.Building,
                    onClick = onOpenProfile,
                    modifier = Modifier.padding(horizontal = pad),
                )
            }
            item {
                TeamSettingsNavRow(
                    title = "گزارش ممیزی",
                    subtitle = "خلاصه فعالیت‌های حساس",
                    icon = DfIcons.ListTodo,
                    onClick = onOpenAudit,
                    modifier = Modifier.padding(horizontal = pad),
                )
            }
            item {
                TeamSectionLabel(title = "ادامه در میزکار", modifier = Modifier.padding(horizontal = pad))
            }
            items(webRows.size) { index ->
                val (title, subtitle, path) = webRows[index]
                TeamSettingsNavRow(
                    title = title,
                    subtitle = subtitle,
                    icon = DfIcons.ExternalLink,
                    onClick = { viewModel.openWorkspace(path, onOpenBridge) { } },
                    modifier = Modifier.padding(horizontal = pad),
                )
            }
        }
        }
    }
}
