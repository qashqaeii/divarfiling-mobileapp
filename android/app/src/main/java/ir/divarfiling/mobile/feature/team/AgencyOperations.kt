package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import ir.divarfiling.mobile.core.design.components.DfConfirmBottomSheet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfCard
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
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.network.AgencyAdvisorDetailPayload
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
)

@HiltViewModel
class AgencyAdvisorsViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgencyAdvisorsUiState())
    val uiState: StateFlow<AgencyAdvisorsUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    init { load(initial = true) }

    fun load(initial: Boolean = false) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update {
                it.copy(
                    isLoading = initial && it.advisors.isEmpty(),
                    isRefreshing = !initial || it.advisors.isNotEmpty(),
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
        val phone = _uiState.value.invitePhone.trim()
        if (phone.isBlank()) {
            _uiState.update { it.copy(error = "شماره موبایل الزامی است") }
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
        }
    }

    fun cancelInvitation(id: Long) {
        viewModelScope.launch {
            when (val result = repository.invitationAction(id, "cancel")) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(successMessage = "دعوت لغو شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
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
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class AgencyAdvisorDetailViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgencyAdvisorDetailUiState())
    val uiState: StateFlow<AgencyAdvisorDetailUiState> = _uiState.asStateFlow()

    fun load(memberId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.detail == null, error = null) }
            when (val result = repository.getAgencyAdvisorDetail(memberId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(detail = result.data, isLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

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
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }
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
    val assignTarget: AgencyAdvisorDto? = null,
    val showAssign: Boolean = false,
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

    fun openAssign(member: AgencyAdvisorDto) = _uiState.update {
        it.copy(showAssign = true, assignTarget = member)
    }

    fun dismissAssign() = _uiState.update { it.copy(showAssign = false, assignTarget = null) }

    fun assignSeat(licenseId: Long) {
        val target = _uiState.value.assignTarget ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = repository.mutateAgencySeat(licenseId, "assign", target.userId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isSubmitting = false, showAssign = false, successMessage = "صندلی تخصیص یافت")
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
            refresh()
        }
    }

    fun releaseSeat(licenseId: Long) {
        viewModelScope.launch {
            when (val result = repository.mutateAgencySeat(licenseId, "unassign")) {
                is ApiResult.Success -> _uiState.update { it.copy(successMessage = "صندلی آزاد شد") }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
            refresh()
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
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }
    val pad = teamHorizontalPadding()
    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = teamListContentPadding(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            item {
                DfHubPageHeader(
                    title = state.detail?.member?.name ?: "مشاور",
                    subtitle = state.detail?.member?.roleLabel.orEmpty(),
                    sectionLabel = DfHeaderSections.TEAM,
                    titleIconRes = DfDecorIcons.Users,
                    onBack = onBack,
                )
            }
            when {
                state.isLoading -> item { DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad)) }
                state.error != null -> item {
                    DfEmptyState(
                        title = "خطا",
                        subtitle = state.error ?: "",
                        variant = DfEmptyVariant.Error,
                        actionLabel = "تلاش مجدد",
                        onAction = { viewModel.load(memberId) },
                        modifier = Modifier.padding(horizontal = pad),
                    )
                }
                state.detail != null -> {
                    val d = state.detail!!
                    item {
                        TeamAdvisorProfileHeader(
                            name = d.member.name,
                            phone = d.member.phone,
                            roleLabel = d.member.roleLabel.ifBlank { d.member.role },
                            isActive = d.member.isActive,
                            seatLabel = d.seat.seatSummaryLabel(),
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                    item {
                        DfCard(modifier = Modifier.padding(horizontal = pad)) {
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                Text("عضویت", fontWeight = FontWeight.Bold)
                                if (d.email.isNotBlank()) Text("ایمیل: ${d.email}")
                                if (d.seat.expiresAt.isNotBlank()) {
                                    Text("انقضای صندلی: ${d.seat.expiresAt}")
                                }
                                if (d.seat.status.isNotBlank()) {
                                    Text("وضعیت لایسنس: ${d.seat.status}")
                                }
                                if (d.permissions.canSetRole) {
                                    DfSecondaryButton(
                                        text = "تغییر نقش",
                                        onClick = viewModel::openRoleSheet,
                                        enabled = !state.isSubmitting,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }
                    item {
                        DfCard(modifier = Modifier.padding(horizontal = pad)) {
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                Text("دامنه کاری", fontWeight = FontWeight.Bold)
                                Text("سرنخ فعال: ${DateUtils.toPersianDigits(d.stats.activeLeads.toString())}")
                                Text("معامله فعال: ${DateUtils.toPersianDigits(d.stats.activeDeals.toString())}")
                            }
                        }
                    }
                    item {
                        DfCard(modifier = Modifier.padding(horizontal = pad)) {
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                Text("عملکرد (۳۰ روز)", fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("تماس: ${DateUtils.toPersianDigits(d.performance.calls.toString())}")
                                    Text("بازدید: ${DateUtils.toPersianDigits(d.performance.visits.toString())}")
                                }
                                Text("مخاطب/سرنخ: ${DateUtils.toPersianDigits(d.performance.contacts.toString())}")
                                Text("معاملات فعال: ${DateUtils.toPersianDigits(d.performance.activeDeals.toString())}")
                                Text("بسته‌شده: ${DateUtils.toPersianDigits(d.performance.dealsClosed.toString())}")
                                d.performance.rank?.let { rank ->
                                    Text("رتبه تیم: ${DateUtils.toPersianDigits(rank.toString())}")
                                }
                                LinearProgressIndicator(
                                    progress = { d.performance.scorePct / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Text(
                                    "امتیاز: ${DateUtils.toPersianDigits(d.performance.scorePct.toString())}٪",
                                    style = ir.divarfiling.mobile.core.design.AppTypography.labelSmall,
                                )
                                DfSecondaryButton(text = "عملکرد تیم", onClick = onOpenPerformance)
                                if (d.permissions.canManageSeat) {
                                    DfSecondaryButton(text = "مدیریت صندلی", onClick = onOpenSeats)
                                }
                            }
                        }
                    }
                    if (d.goals.isNotEmpty()) {
                        item {
                            DfCard(modifier = Modifier.padding(horizontal = pad)) {
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                    Text("اهداف", fontWeight = FontWeight.Bold)
                                    d.goals.forEach { goal ->
                                        Text("${goal.metric} · ${goal.periodLabel}")
                                        LinearProgressIndicator(
                                            progress = { goal.pct / 100f },
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                        Text(
                                            "${DateUtils.toPersianDigits(goal.actual.toString())} / ${DateUtils.toPersianDigits(goal.target.toString())}",
                                            style = ir.divarfiling.mobile.core.design.AppTypography.labelSmall,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (d.permissions.canManageMember && d.member.isActive) {
                        item {
                            DfSecondaryButton(
                                text = "غیرفعال کردن عضو",
                                onClick = viewModel::openDeactivateConfirm,
                                enabled = !state.isSubmitting,
                                modifier = Modifier
                                    .padding(horizontal = pad)
                                    .fillMaxWidth(),
                            )
                        }
                    }
                    if (d.recentActivity.isNotEmpty()) {
                        item {
                            DfCard(modifier = Modifier.padding(horizontal = pad)) {
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                    Text("فعالیت اخیر", fontWeight = FontWeight.Bold)
                                    d.recentActivity.take(5).forEach { act ->
                                        Text(
                                            "${act.title} · ${DateUtils.formatRelativeFa(act.createdAt)}",
                                            style = ir.divarfiling.mobile.core.design.AppTypography.labelSmall,
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
                        title = "عملکرد تیم",
                        subtitle = "خلاصه تصمیم‌یار",
                        sectionLabel = DfHeaderSections.TEAM,
                        titleIconRes = DfDecorIcons.Users,
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
                if (state.totals.isNotEmpty()) {
                    item {
                        DfCard(modifier = Modifier.padding(horizontal = pad)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(AppSpacing.sm),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                state.totals.entries.take(4).forEach { (key, value) ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            DateUtils.toPersianDigits(value.toString()),
                                            fontWeight = FontWeight.Bold,
                                            color = DfThemeColors.primary(),
                                        )
                                        Text(key, style = ir.divarfiling.mobile.core.design.AppTypography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
                if (state.goals.isNotEmpty()) {
                    item {
                        DfCard(modifier = Modifier.padding(horizontal = pad)) {
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                Text("اهداف تیم", fontWeight = FontWeight.Bold)
                                state.goals.forEach { goal ->
                                    Text("${goal.memberName} · ${goal.metric}")
                                    LinearProgressIndicator(
                                        progress = { goal.pct / 100f },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    Text(
                                        "${DateUtils.toPersianDigits(goal.actual.toString())} / ${DateUtils.toPersianDigits(goal.target.toString())}",
                                        style = ir.divarfiling.mobile.core.design.AppTypography.labelSmall,
                                    )
                                }
                            }
                        }
                    }
                }
                when {
                    state.isLoading -> item { DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad)) }
                    state.members.isEmpty() -> item {
                        DfEmptyState(
                            title = "در این بازه داده‌ای ثبت نشده",
                            subtitle = "فعالیت تیم را در روزهای آینده بررسی کنید.",
                            variant = DfEmptyVariant.Empty,
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                    else -> items(state.members, key = { it.memberId ?: it.name }) { row ->
                        TeamPerformanceAdvisorRow(
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
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    if (state.showAssign && state.assignTarget != null) {
        val licenses = state.payload?.availableLicenses.orEmpty()
        DfModalBottomSheet(onDismissRequest = viewModel::dismissAssign) {
            DfSheetScaffold(
                title = "تخصیص صندلی",
                subtitle = state.assignTarget!!.name,
                onClose = viewModel::dismissAssign,
                footer = {
                    DfSheetActions(
                        primaryText = "بستن",
                        onPrimary = viewModel::dismissAssign,
                        onSecondary = viewModel::dismissAssign,
                        secondaryText = "انصراف",
                    )
                },
            ) {
                licenses.forEach { lic ->
                    DfPrimaryButton(
                        text = "صندلی #${lic.sequence} · ${lic.plan}",
                        onClick = { viewModel.assignSeat(lic.id) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSubmitting,
                    )
                }
                if (licenses.isEmpty()) {
                    Text("صندلی آزاد وجود ندارد")
                }
            }
        }
    }
    Scaffold(containerColor = DfScreenContainerColor, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
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
                        title = "لایسنس‌ها و صندلی‌ها",
                        subtitle = state.payload?.seats?.let {
                            "${DateUtils.toPersianDigits(it.assigned.toString())} از ${DateUtils.toPersianDigits(it.total.toString())} تخصیص"
                        } ?: "",
                        sectionLabel = DfHeaderSections.TEAM,
                        titleIconRes = DfDecorIcons.Users,
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
                    else -> items(state.payload?.members.orEmpty(), key = { it.id }) { member ->
                        val hasSeat = member.resolvedHasSeat()
                        DfCard(modifier = Modifier.padding(horizontal = pad)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(AppSpacing.sm),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(member.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        member.seatSummaryLabel(),
                                        style = ir.divarfiling.mobile.core.design.AppTypography.labelSmall,
                                        color = DfThemeColors.textMuted(),
                                    )
                                }
                                if (!hasSeat) {
                                    DfPrimaryButton(
                                        text = "تخصیص",
                                        onClick = { viewModel.openAssign(member) },
                                    )
                                } else if (member.licenseId != null) {
                                    DfSecondaryButton(
                                        text = "آزادسازی",
                                        onClick = { viewModel.releaseSeat(member.licenseId!!) },
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
        Triple("وب‌هوک‌ها", "مدیریت Secret و URL — فقط میزکار", AppLinks.WORKSPACE_AGENCY_WEBHOOKS),
        Triple("همگام‌سازی / Transfers", "ورود/خروج CRM و انتقال داده — عملیات پرحجم", AppLinks.WORKSPACE_AGENCY_SYNC),
        Triple("TV Wallboard", "نمایش زنده آژانس", "/workspace/crm/team/tv/"),
    )
    Scaffold(containerColor = DfScreenContainerColor) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = teamListContentPadding(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            item {
                DfHubPageHeader(
                    title = "تنظیمات پیشرفته",
                    subtitle = "ادامه در میزکار",
                    sectionLabel = DfHeaderSections.TEAM,
                    titleIconRes = DfDecorIcons.Users,
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
