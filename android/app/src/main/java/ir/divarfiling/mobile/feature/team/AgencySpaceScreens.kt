package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import ir.divarfiling.mobile.core.design.components.showDfMessage
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfListingImage
import ir.divarfiling.mobile.core.design.components.DfSearchField
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
import androidx.compose.ui.draw.clip
import ir.divarfiling.mobile.core.design.AppShapes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.components.DfImageGallery
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfConfirmBottomSheet
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.network.AgencyHomeDto
import ir.divarfiling.mobile.core.network.AgencySpaceHeaderKpisDto
import ir.divarfiling.mobile.core.network.AgencySpaceItemDto
import ir.divarfiling.mobile.core.network.AgencySpacePublishRequest
import ir.divarfiling.mobile.core.network.AgencySpaceUpdateRequest
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

data class AgencySpaceListUiState(
    val tab: String = AgencySpaceTab.ALL,
    val query: String = "",
    val publicationStatus: String = "active",
    val filters: AgencySpaceFilters = AgencySpaceFilters(),
    val draftFilters: AgencySpaceFilters = AgencySpaceFilters(),
    val showFilterSheet: Boolean = false,
    val canModerateSpace: Boolean = false,
    val items: List<AgencySpaceItemDto> = emptyList(),
    val kpis: AgencySpaceHeaderKpisDto? = null,
    val showAnalytics: Boolean = false,
    val page: Int = 1,
    val numPages: Int = 1,
    val total: Int = 0,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val bulkSubmitting: Boolean = false,
    val pendingBulkAction: String? = null,
    val isLoadingMore: Boolean = false,
    val publisherOptions: List<ir.divarfiling.mobile.core.network.TeamMemberDto> = emptyList(),
)

@HiltViewModel
class AgencySpaceListViewModel @Inject constructor(
    private val repository: TeamRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AgencySpaceListUiState(
            tab = savedStateHandle.get<String>("tab")
                ?.takeIf { key -> AgencySpaceTab.labels.any { it.first == key } }
                ?: AgencySpaceTab.ALL,
        ),
    )
    val uiState: StateFlow<AgencySpaceListUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            when (val home = repository.getAgencyHome()) {
                is ApiResult.Success -> {
                    val caps = home.data.capabilities
                    _uiState.update {
                        it.copy(
                            showAnalytics = caps.canViewSpaceAnalytics,
                            canModerateSpace = caps.canModerateAgencySpace,
                            kpis = home.data.spaceKpis,
                        )
                    }
                }
                is ApiResult.Error -> Unit
            }
            when (val advisors = repository.getAgencyAdvisors()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(publisherOptions = advisors.data.advisors.map { a ->
                        ir.divarfiling.mobile.core.network.TeamMemberDto(
                            id = a.id,
                            userId = a.userId,
                            name = a.name,
                            phone = a.phone,
                            role = a.role,
                            roleLabel = a.roleLabel,
                            title = a.title,
                            isActive = a.isActive,
                        )
                    })
                }
                is ApiResult.Error -> Unit
            }
            refresh(true)
        }
    }

    fun setTab(tab: String) {
        _uiState.update { it.copy(tab = tab, page = 1) }
        refresh(true)
    }

    fun setPublicationStatus(status: String) {
        _uiState.update { it.copy(publicationStatus = status, page = 1) }
        refresh(true)
    }

    fun setQuery(q: String) {
        _uiState.update { it.copy(query = q) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            refresh(true)
        }
    }

    fun openFilters() {
        _uiState.update { it.copy(showFilterSheet = true, draftFilters = it.filters) }
    }

    fun dismissFilters() {
        _uiState.update { it.copy(showFilterSheet = false) }
    }

    fun updateDraftFilters(transform: (AgencySpaceFilters) -> AgencySpaceFilters) {
        _uiState.update { it.copy(draftFilters = transform(it.draftFilters)) }
    }

    fun applyFilters() {
        _uiState.update {
            it.copy(
                filters = it.draftFilters,
                showFilterSheet = false,
                page = 1,
            )
        }
        refresh(true)
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                filters = AgencySpaceFilters(),
                draftFilters = AgencySpaceFilters(),
                page = 1,
            )
        }
        refresh(true)
    }

    private fun queryParamsFor(state: AgencySpaceListUiState, page: Int): Map<String, String> =
        buildMap {
            put("tab", state.tab)
            if (state.query.isNotBlank()) put("q", state.query.trim())
            put("page", page.coerceAtLeast(1).toString())
            if (state.canModerateSpace && state.publicationStatus != "active") {
                put("publication_status", state.publicationStatus)
            }
            state.filters.appendQueryParams(state.tab, this)
        }

    fun refresh(initial: Boolean = false) {
        viewModelScope.launch {
            if (initial) {
                _uiState.update { it.copy(page = 1) }
            }
            val state = _uiState.value
            _uiState.update {
                it.copy(
                    isLoading = initial && it.items.isEmpty(),
                    isRefreshing = !initial || it.items.isNotEmpty(),
                    error = null,
                )
            }
            val params = queryParamsFor(state, state.page)
            when (val result = repository.getAgencySpaceItems(params)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        items = result.data.items,
                        total = result.data.total,
                        page = result.data.page,
                        numPages = result.data.numPages,
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        error = result.message,
                    )
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isRefreshing || state.isLoadingMore || state.page >= state.numPages) {
            return
        }
        viewModelScope.launch {
            val nextPage = state.page + 1
            _uiState.update { it.copy(isLoadingMore = true, error = null) }
            val params = queryParamsFor(state, nextPage)
            when (val result = repository.getAgencySpaceItems(params)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        items = it.items + result.data.items,
                        total = result.data.total,
                        page = result.data.page,
                        numPages = result.data.numPages,
                        isLoadingMore = false,
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoadingMore = false, error = result.message)
                }
            }
        }
    }

    fun copyItem(itemId: Long, onCopied: (kind: String, targetId: Long) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = repository.copyAgencySpaceItem(itemId)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, successMessage = "با موفقیت ثبت شد")
                    }
                    onCopied(result.data.kind, result.data.targetId)
                    refresh()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }

    fun toggleSelectMode() = _uiState.update {
        it.copy(selectMode = !it.selectMode, selectedIds = emptySet(), pendingBulkAction = null)
    }

    fun toggleSelected(itemId: Long) = _uiState.update { state ->
        val next = if (itemId in state.selectedIds) state.selectedIds - itemId else state.selectedIds + itemId
        state.copy(selectedIds = next)
    }

    fun requestBulkAction(action: String) = _uiState.update { it.copy(pendingBulkAction = action) }

    fun dismissBulkConfirm() = _uiState.update { it.copy(pendingBulkAction = null) }

    fun confirmBulkModerate() {
        val action = _uiState.value.pendingBulkAction ?: return
        val ids = _uiState.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(bulkSubmitting = true, pendingBulkAction = null) }
            when (val result = repository.bulkModerateAgencySpace(action, ids)) {
                is ApiResult.Success -> {
                    val msg = "${result.data.succeeded} موفق · ${result.data.failed} ناموفق"
                    _uiState.update {
                        it.copy(
                            bulkSubmitting = false,
                            selectMode = false,
                            selectedIds = emptySet(),
                            successMessage = msg,
                        )
                    }
                    refresh(true)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(bulkSubmitting = false, error = result.message)
                }
            }
        }
    }
}

data class AgencySpaceHubUiState(
    val home: AgencyHomeDto? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AgencySpaceHubViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgencySpaceHubUiState())
    val uiState: StateFlow<AgencySpaceHubUiState> = _uiState.asStateFlow()

    init {
        refresh(initial = true)
    }

    fun refresh(initial: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = initial && it.home == null,
                    isRefreshing = !initial || it.home != null,
                    error = null,
                )
            }
            when (val result = repository.getAgencyHome()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(home = result.data, isLoading = false, isRefreshing = false)
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
fun AgencySpaceHubScreen(
    onBack: () -> Unit,
    onOpenBrowse: (tab: String) -> Unit,
    viewModel: AgencySpaceHubViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pad = teamHorizontalPadding()
    val home = state.home
    val caps = home?.capabilities
    val canAccess = caps?.canAccessAgencySpace == true
    val agencyName = home?.agency?.name?.ifBlank { "فضای آژانس" } ?: "فضای آژانس"
    val roleLabel = home?.membership?.roleLabel?.ifBlank {
        home.membership?.title.orEmpty()
    }.orEmpty()
    val subtitle = when {
        roleLabel.isNotBlank() -> roleLabel
        canAccess -> "اشتراک فایل و مخاطب با تیم"
        caps?.agencySpaceEnabled == true -> "برای دسترسی، عضویت و اشتراک آژانس لازم است"
        else -> "قابلیت فضای آژانس در آژانس شما فعال نیست"
    }
    val kpis = home?.spaceKpis

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    Scaffold(containerColor = DfScreenContainerColor) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TeamAmbientBackground()
            DfPullRefresh(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.refresh(initial = false) },
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    contentPadding = teamListContentPadding(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                ) {
                    item {
                        AgencyDashboardHeader(
                            title = agencyName,
                            roleLabel = roleLabel.ifBlank { subtitle },
                            logoUrl = home?.agency?.logoUrl?.takeIf { it.isNotBlank() },
                            statusChips = buildList {
                                if (canAccess) add("دسترسی فعال")
                                if (caps?.canPublishAgencySpace == true) add("انتشار")
                            },
                            onBack = onBack,
                        )
                    }
                    state.error?.let { err ->
                        if (!state.isLoading && home == null) {
                            item {
                                DfErrorBanner(message = err, modifier = Modifier.padding(horizontal = pad))
                            }
                        }
                    }
                    when {
                        state.isLoading -> item {
                            DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad))
                        }
                        !canAccess -> item {
                            DfEmptyState(
                                title = "دسترسی به فضای آژانس",
                                subtitle = subtitle,
                                variant = DfEmptyVariant.Empty,
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                        else -> {
                            if (kpis != null) {
                                item {
                                    TeamMetricsRow(
                                        metrics = listOf(
                                            TeamMetric(
                                                "فایل‌های فعال",
                                                kpis.activeProperties.toString(),
                                                DfThemeColors.info(),
                                                DfIcons.Building,
                                                onClick = { onOpenBrowse(AgencySpaceTab.PROPERTIES) },
                                            ),
                                            TeamMetric(
                                                "مخاطبین فعال",
                                                kpis.activeContacts.toString(),
                                                DfThemeColors.success(),
                                                DfIcons.Users,
                                                onClick = { onOpenBrowse(AgencySpaceTab.CONTACTS) },
                                            ),
                                        ),
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                            }
                            item {
                                AgencySpaceAccessStrip(
                                    canPublish = caps?.canPublishAgencySpace == true,
                                    canModerate = caps?.canModerateAgencySpace == true,
                                    modifier = Modifier.padding(horizontal = pad),
                                )
                            }
                            item {
                                TeamSectionLabel(
                                    title = "مرور سریع",
                                    subtitle = "فیلترهای پرکاربرد",
                                    modifier = Modifier.padding(horizontal = pad),
                                )
                            }
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = pad),
                                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                                ) {
                                    TeamDestinationCompact(
                                        title = "همه انتشارها",
                                        subtitle = "فایل و مخاطب در یک فهرست",
                                        icon = DfIcons.Layers,
                                        onClick = { onOpenBrowse(AgencySpaceTab.ALL) },
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                                    ) {
                                        TeamDestinationCompact(
                                            title = "فایل‌ها",
                                            subtitle = kpis?.let {
                                                "${DateUtils.toPersianDigits(it.activeProperties.toString())} فعال"
                                            } ?: "فایل‌های اشتراکی",
                                            icon = DfIcons.Building,
                                            onClick = { onOpenBrowse(AgencySpaceTab.PROPERTIES) },
                                            modifier = Modifier.weight(1f),
                                        )
                                        TeamDestinationCompact(
                                            title = "مخاطبین",
                                            subtitle = kpis?.let {
                                                "${DateUtils.toPersianDigits(it.activeContacts.toString())} فعال"
                                            } ?: "مخاطبین اشتراکی",
                                            icon = DfIcons.Users,
                                            onClick = { onOpenBrowse(AgencySpaceTab.CONTACTS) },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                                    ) {
                                        TeamDestinationCompact(
                                            title = "منتشرشده من",
                                            subtitle = "انتشارهای شما",
                                            icon = DfIcons.Upload,
                                            onClick = { onOpenBrowse(AgencySpaceTab.PUBLISHED_BY_ME) },
                                            modifier = Modifier.weight(1f),
                                        )
                                        TeamDestinationCompact(
                                            title = "ذخیره‌های من",
                                            subtitle = "کپی‌های CRM",
                                            icon = DfIcons.Sparkles,
                                            onClick = { onOpenBrowse(AgencySpaceTab.SAVED_BY_ME) },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                            if (caps?.canViewSpaceAnalytics == true && kpis != null) {
                                item {
                                    TeamSectionLabel(
                                        title = "آمار اشتراک",
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                                item {
                                    AgencySpaceKpiRow(kpis = kpis, modifier = Modifier.padding(horizontal = pad))
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
private fun AgencySpaceAccessStrip(
    canPublish: Boolean,
    canModerate: Boolean,
    modifier: Modifier = Modifier,
) {
    val parts = buildList {
        add("دسترسی فعال")
        if (canPublish) add("امکان انتشار")
        if (canModerate) add("مدیریت فضا")
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        parts.forEach { label ->
            DfBadge(
                text = label,
                color = DfThemeColors.primaryContainer(),
                textColor = DfThemeColors.primary(),
            )
        }
    }
}

data class AgencySpaceDetailUiState(
    val item: AgencySpaceItemDto? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val showEditSheet: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class AgencySpaceDetailViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgencySpaceDetailUiState())
    val uiState: StateFlow<AgencySpaceDetailUiState> = _uiState.asStateFlow()
    private var itemId: Long = 0

    fun bind(itemId: Long) {
        if (this.itemId == itemId && _uiState.value.item != null) return
        this.itemId = itemId
        load()
    }

    fun load() {
        if (itemId <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.item == null, error = null) }
            when (val result = repository.getAgencySpaceItem(itemId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(item = result.data.item, isLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun copy(onCopied: (kind: String, targetId: Long) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = repository.copyAgencySpaceItem(itemId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "کپی شد") }
                    onCopied(result.data.kind, result.data.targetId)
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun unpublish() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = repository.unpublishAgencySpaceItem(itemId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isSubmitting = false, successMessage = "انتشار لغو شد")
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
            load()
        }
    }

    fun moderate(action: String, reason: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = repository.moderateAgencySpaceItem(itemId, action, reason)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isSubmitting = false, successMessage = if (action == "hide") "مورد مخفی شد" else "بازگردانی شد")
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
            load()
        }
    }

    fun openEditSheet() = _uiState.update { it.copy(showEditSheet = true) }

    fun dismissEditSheet() = _uiState.update { it.copy(showEditSheet = false) }

    fun saveAccessEdit(
        visibility: String,
        contactShareLevel: String,
        memberIds: List<Long>,
        note: String,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val isCustomer = _uiState.value.item?.customer != null
            val request = AgencySpaceUpdateRequest(
                visibility = visibility,
                contactShareLevel = contactShareLevel.takeIf { isCustomer },
                memberIds = memberIds.takeIf { visibility == "selected_members" },
                note = note,
            )
            when (val result = repository.updateAgencySpaceItem(itemId, request)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        item = result.data.item,
                        isSubmitting = false,
                        showEditSheet = false,
                        successMessage = "تغییرات ذخیره شد",
                    )
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
fun AgencySpaceListScreen(
    onBack: () -> Unit,
    onOpenItem: (Long) -> Unit,
    onOpenCopiedTarget: (kind: String, targetId: Long) -> Unit,
    viewModel: AgencySpaceListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val pad = teamHorizontalPadding()

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showDfMessage(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showDfMessage(it); viewModel.clearMessage() }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { TeamAgencySnackbarHost(snackbar) },
        bottomBar = {
            if (state.selectMode && state.selectedIds.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Text("${ir.divarfiling.mobile.core.design.DateUtils.toPersianDigits(state.selectedIds.size.toString())} مورد انتخاب شده")
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        DfSecondaryButton(
                            text = "مخفی‌سازی",
                            onClick = { viewModel.requestBulkAction("hide") },
                            enabled = !state.bulkSubmitting,
                            modifier = Modifier.weight(1f),
                        )
                        DfSecondaryButton(
                            text = "بازگردانی",
                            onClick = { viewModel.requestBulkAction("restore") },
                            enabled = !state.bulkSubmitting,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            TeamAmbientBackground()
            DfPullRefresh(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.refresh(initial = true) },
                modifier = Modifier.fillMaxSize(),
            ) {
            LazyColumn(
                contentPadding = teamListContentPadding(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    TeamAgencyScreenHeader(
                        title = "فهرست انتشار",
                        subtitle = if (state.total > 0) {
                            "${ir.divarfiling.mobile.core.design.DateUtils.toPersianDigits(state.total.toString())} انتشار"
                        } else {
                            "اشتراک فایل و مخاطب با تیم"
                        },
                        icon = DfIcons.Layers,
                        accent = AgencyHeaderAccent.Management,
                        onBack = onBack,
                    )
                }
                if (state.showAnalytics && state.kpis != null) {
                    item {
                        AgencySpaceKpiRow(kpis = state.kpis!!, modifier = Modifier.padding(horizontal = pad))
                    }
                }
                item {
                    DfSearchField(
                        value = state.query,
                        onValueChange = viewModel::setQuery,
                        placeholder = "جستجو در فضای آژانس",
                        modifier = Modifier.padding(horizontal = pad),
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = pad),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        val filterCount = state.filters.activeCount()
                        DfSecondaryButton(
                            text = if (filterCount > 0) {
                                "فیلتر (${DateUtils.toPersianDigits(filterCount.toString())})"
                            } else {
                                "فیلتر پیشرفته"
                            },
                            onClick = viewModel::openFilters,
                            modifier = Modifier.weight(1f),
                        )
                        if (filterCount > 0) {
                            DfSecondaryButton(
                                text = "حذف فیلتر",
                                onClick = viewModel::clearFilters,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                item {
                    AgencySpaceTabRow(
                        selectedTab = state.tab,
                        onTabSelected = viewModel::setTab,
                        modifier = Modifier.padding(horizontal = pad),
                    )
                }
                if (state.canModerateSpace) {
                    item {
                        LazyRow(
                            modifier = Modifier.padding(horizontal = pad),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            items(
                                listOf(
                                    "active" to "فعال",
                                    "archived" to "مخفی/بایگانی",
                                    "all" to "همه",
                                ),
                            ) { (key, label) ->
                                DfSoftChip(
                                    text = label,
                                    selected = state.publicationStatus == key,
                                    onClick = { viewModel.setPublicationStatus(key) },
                                )
                            }
                        }
                    }
                    item {
                        DfSecondaryButton(
                            text = if (state.selectMode) "لغو حالت انتخاب" else "انتخاب گروهی",
                            onClick = viewModel::toggleSelectMode,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = pad),
                        )
                    }
                }
                state.error?.let { err ->
                    if (!state.isLoading && state.items.isEmpty()) {
                        item {
                            DfErrorBanner(message = err, modifier = Modifier.padding(horizontal = pad))
                        }
                    }
                }
                when {
                    state.isLoading -> item { DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad)) }
                    state.items.isEmpty() && state.error == null -> item {
                        val (title, desc) = agencySpaceEmptyCopy(state.tab)
                        DfEmptyState(
                            title = title,
                            subtitle = desc,
                            variant = DfEmptyVariant.Empty,
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                    else -> {
                        items(state.items, key = { it.id }) { item ->
                            val selectable = state.selectMode && item.canModerate
                            AgencySpaceItemCard(
                                item = item,
                                isSubmitting = state.isSubmitting,
                                selectMode = selectable,
                                selected = item.id in state.selectedIds,
                                onOpen = {
                                    if (selectable) viewModel.toggleSelected(item.id)
                                    else onOpenItem(item.id)
                                },
                                onCopy = {
                                    viewModel.copyItem(item.id, onOpenCopiedTarget)
                                },
                                onViewCopied = onOpenCopiedTarget,
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                        if (state.page < state.numPages) {
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

    if (state.showFilterSheet) {
        AgencySpaceFilterSheet(
            tab = state.tab,
            filters = state.draftFilters,
            publishers = state.publisherOptions,
            onDismiss = viewModel::dismissFilters,
            onFiltersChange = { next -> viewModel.updateDraftFilters { _ -> next } },
            onApply = viewModel::applyFilters,
            onClear = {
                viewModel.updateDraftFilters { AgencySpaceFilters() }
                viewModel.clearFilters()
            },
        )
    }

    state.pendingBulkAction?.let { action ->
        val label = if (action == "hide") "مخفی‌سازی گروهی" else "بازگردانی گروهی"
        DfConfirmBottomSheet(
            title = label,
            message = "این عملیات روی ${state.selectedIds.size} انتشار اعمال می‌شود. موارد غیرمجاز رد می‌شوند.",
            onConfirm = viewModel::confirmBulkModerate,
            onDismiss = viewModel::dismissBulkConfirm,
            isSubmitting = state.bulkSubmitting,
            destructive = action == "hide",
        )
    }
}

@Composable
private fun AgencySpaceKpiRow(
    kpis: AgencySpaceHeaderKpisDto,
    modifier: Modifier = Modifier,
) {
    TeamMetricsRow(
        metrics = listOf(
            TeamMetric("فایل‌ها", kpis.activeProperties.toString(), DfThemeColors.info(), DfIcons.Building),
            TeamMetric("مخاطبین", kpis.activeContacts.toString(), DfThemeColors.success(), DfIcons.Users),
            TeamMetric("ذخیره‌ها", kpis.totalCopies.toString(), DfThemeColors.warning(), DfIcons.Sparkles),
        ),
        modifier = modifier,
    )
}

private fun agencySpaceEmptyCopy(tab: String): Pair<String, String> = when (tab) {
    AgencySpaceTab.PROPERTIES -> "فایلی نیست" to "هنوز ملکی در فضای آژانس منتشر نشده."
    AgencySpaceTab.CONTACTS -> "مخاطبی نیست" to "هنوز مخاطبی در فضای آژانس منتشر نشده."
    AgencySpaceTab.PUBLISHED_BY_ME -> "هنوز منتشر نکرده‌اید" to "انتشارهای شما اینجا نمایش داده می‌شود."
    AgencySpaceTab.SAVED_BY_ME -> "هنوز ذخیره نکرده‌اید" to "مواردی که به CRM شخصی اضافه کنید اینجا هست."
    else -> "فضای آژانس خالی است" to "هنوز چیزی در فضای آژانس منتشر نشده."
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencySpaceDetailScreen(
    itemId: Long,
    onBack: () -> Unit,
    onOpenCopiedTarget: (kind: String, targetId: Long) -> Unit,
    viewModel: AgencySpaceDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val pad = teamHorizontalPadding()

    LaunchedEffect(itemId) { viewModel.bind(itemId) }
    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showDfMessage(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showDfMessage(it); viewModel.clearMessage() }
    }

    LifecycleResumeEffect(itemId) {
        viewModel.load()
        onPauseOrDispose { }
    }

    Scaffold(containerColor = DfScreenContainerColor, snackbarHost = { TeamAgencySnackbarHost(snackbar) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TeamAmbientBackground()
            Column(modifier = Modifier.fillMaxSize()) {
            TeamAgencyScreenHeader(
                title = if (state.item?.property != null) "جزئیات ملک" else "جزئیات مخاطب",
                subtitle = state.item?.visibilityLabel?.takeIf { it.isNotBlank() } ?: "اشتراک آژانس",
                icon = if (state.item?.property != null) DfIcons.Building else DfIcons.Users,
                accent = AgencyHeaderAccent.Detail,
                onBack = onBack,
            )
            when {
                state.isLoading -> DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad))
                state.item == null -> DfEmptyState(
                    title = "یافت نشد",
                    subtitle = state.error ?: "این مورد در دسترس نیست.",
                    variant = DfEmptyVariant.Error,
                    modifier = Modifier.padding(horizontal = pad),
                )
                else -> {
                    val item = state.item!!
                    val isProperty = item.property != null
                    val copyKind = if (isProperty) "property" else "customer"
                    LazyColumn(
                        contentPadding = teamListContentPadding(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        item {
                            AgencySpaceDetailHero(
                                isProperty = isProperty,
                                title = item.property?.title ?: item.customer?.name ?: "—",
                                subtitle = item.property?.priceLabel
                                    ?: item.customer?.budgetLabel
                                    ?: item.property?.region
                                    ?: item.customer?.region.orEmpty(),
                                avatarUrl = item.customer?.avatarUrl,
                                statusLabel = item.statusLabel.ifBlank { item.visibilityLabel },
                                publisherName = item.publisher.name,
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                        item.property?.let { prop ->
                            val gallery = prop.images.ifEmpty {
                                listOfNotNull(prop.thumbnailUrl.takeIf { it.isNotBlank() })
                            }
                            if (gallery.isNotEmpty()) {
                                item {
                                    DfImageGallery(
                                        images = gallery,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = pad),
                                        heroHeight = 220.dp,
                                    )
                                }
                            }
                        }
                        item {
                            AgencySpaceDetailSection(
                                title = if (isProperty) "مشخصات ملک" else "نیاز مشتری",
                                modifier = Modifier.padding(horizontal = pad),
                            ) {
                                item.property?.let { prop ->
                                    AgencySpacePropertySpecsBlock(prop = prop)
                                }
                                item.customer?.let { cust ->
                                    AgencySpaceContactSpecsBlock(customer = cust)
                                }
                                if (item.note.isNotBlank()) detailRow("یادداشت انتشار", item.note)
                            }
                        }
                        item {
                            AgencySpaceDetailSection(
                                title = "ناشر و زمان",
                                modifier = Modifier.padding(horizontal = pad),
                            ) {
                                AgencySpacePublisherRow(
                                    name = item.publisher.name,
                                    roleLabel = item.publisher.roleLabel.ifBlank { "منتشرکننده" },
                                )
                                if (item.publishedAtDisplay.isNotBlank()) {
                                    detailRow("انتشار", item.publishedAtDisplay)
                                }
                            }
                        }
                        item {
                            AgencySpaceDetailSection(
                                title = "دسترسی و وضعیت",
                                modifier = Modifier.padding(horizontal = pad),
                            ) {
                                AgencySpaceVisibilityBadge(
                                    visibility = item.visibility,
                                    label = item.visibilityLabel,
                                )
                                if (item.visibility == "selected_members" && item.audienceCount != null) {
                                    detailRow(
                                        "مخاطبان",
                                        "${ir.divarfiling.mobile.core.design.DateUtils.toPersianDigits(item.audienceCount.toString())} عضو",
                                    )
                                }
                                if (item.contactShareLevelLabel.isNotBlank()) {
                                    detailRow("اشتراک تماس", item.contactShareLevelLabel)
                                }
                                if (item.statusLabel.isNotBlank()) {
                                    Text(
                                        item.statusLabel,
                                        style = AppTypography.bodyDescription,
                                        color = DfThemeColors.textSecondary(),
                                    )
                                }
                            }
                        }
                        if (item.alreadyCopied) {
                            item {
                                DfCard(modifier = Modifier.padding(horizontal = pad)) {
                                    Column(
                                        modifier = Modifier.padding(AppSpacing.cardPadding),
                                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                    ) {
                                        Text(
                                            if (isProperty) {
                                                "این فایل در فایل‌های شخصی شما ذخیره شده است."
                                            } else {
                                                "این مخاطب در CRM شخصی شما ذخیره شده است."
                                            },
                                            style = AppTypography.bodyDescription,
                                        )
                                        if (item.copyTargetId != null) {
                                            DfPrimaryButton(
                                                text = if (isProperty) "مشاهده نسخه من" else "مشاهده مخاطب من",
                                                onClick = {
                                                    onOpenCopiedTarget(copyKind, item.copyTargetId!!)
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            Column(
                                modifier = Modifier.padding(horizontal = pad),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            ) {
                                if (item.canCopy && !item.alreadyCopied) {
                                    DfPrimaryButton(
                                        text = if (isProperty) "افزودن به فایل‌های من" else "افزودن به مخاطبین من",
                                        onClick = { viewModel.copy(onOpenCopiedTarget) },
                                        enabled = !state.isSubmitting,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                                if (item.canEdit) {
                                    DfPrimaryButton(
                                        text = "ویرایش دسترسی",
                                        onClick = viewModel::openEditSheet,
                                        enabled = !state.isSubmitting,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                                if (item.canUnpublish) {
                                    DfSecondaryButton(
                                        text = "لغو انتشار",
                                        onClick = viewModel::unpublish,
                                        enabled = !state.isSubmitting,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                                if (item.canModerate) {
                                    DfSecondaryButton(
                                        text = "مخفی‌سازی (مدیریت)",
                                        onClick = { viewModel.moderate("hide") },
                                        enabled = !state.isSubmitting,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    DfSecondaryButton(
                                        text = "بازگردانی انتشار",
                                        onClick = { viewModel.moderate("restore") },
                                        enabled = !state.isSubmitting,
                                        modifier = Modifier.fillMaxWidth(),
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

    val editItem = state.item
    if (state.showEditSheet && editItem != null && editItem.canEdit) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissEditSheet) {
            AgencySpaceEditAccessSheet(
                item = editItem,
                isSubmitting = state.isSubmitting,
                onDismiss = viewModel::dismissEditSheet,
                onSave = viewModel::saveAccessEdit,
            )
        }
    }
}

@Composable
private fun AgencySpaceDetailHero(
    isProperty: Boolean,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    statusLabel: String = "",
    publisherName: String = "",
) {
    val typeLabel = if (isProperty) "ملک اشتراکی" else "مخاطب اشتراکی"
    val accent = if (isProperty) DfThemeColors.info() else DfThemeColors.primary()
    val resolvedAvatar = remember(avatarUrl) { ir.divarfiling.mobile.core.image.ImageUrlFormatter.normalize(avatarUrl) }
    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!isProperty) {
                    AgencySpaceContactAvatar(
                        name = title,
                        avatarUrl = resolvedAvatar,
                        size = 52.dp,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(AppShapes.IconContainer)
                            .background(accent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(DfIcons.Building, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    DfBadge(text = typeLabel, color = accent.copy(alpha = 0.12f), textColor = accent)
                    Text(
                        title,
                        style = AppTypography.pageTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            subtitle,
                            style = AppTypography.bodyDescription,
                            color = DfThemeColors.primary(),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                if (statusLabel.isNotBlank()) {
                    DfBadge(
                        text = statusLabel,
                        color = DfThemeColors.surfaceVariant(),
                        textColor = DfThemeColors.textSecondary(),
                    )
                }
                if (publisherName.isNotBlank()) {
                    DfBadge(
                        text = "منتشرکننده: $publisherName",
                        color = DfThemeColors.primaryContainer(),
                        textColor = DfThemeColors.primary(),
                    )
                }
            }
        }
    }
}

@Composable
private fun AgencySpacePropertySpecsBlock(
    prop: ir.divarfiling.mobile.core.network.AgencySpacePropertyPreviewDto,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        val primaryPrice = prop.priceRows.firstOrNull()?.value?.takeIf { it.isNotBlank() } ?: prop.priceLabel
        AgencySpaceInfoTile("قیمت / معامله", primaryPrice, DfIcons.Calculator, emphasize = true)
        prop.priceRows.drop(1).forEach { row ->
            AgencySpaceInfoTile(row.label, row.value, DfIcons.Calculator)
        }
        AgencySpaceInfoTile("متراژ", prop.areaLabel, DfIcons.Building)
        AgencySpaceInfoTile("زمین", prop.landAreaLabel, DfIcons.MapPin)
        AgencySpaceInfoTile("خواب", prop.rooms, DfIcons.Users)
        prop.floor?.let { AgencySpaceInfoTile("طبقه", it.toString(), DfIcons.Layers) }
        prop.totalFloors?.let { AgencySpaceInfoTile("تعداد طبقات", it.toString(), DfIcons.Layers) }
        prop.buildYear?.let { AgencySpaceInfoTile("سال ساخت", it.toString(), DfIcons.Clock) }
        AgencySpaceInfoTile("منطقه", prop.region, DfIcons.MapPin)
        AgencySpaceInfoTile("نوع ملک", prop.propertyType, DfIcons.Building)
        AgencySpaceInfoTile("معامله", prop.dealMode, DfIcons.Handshake)
        AgencySpaceInfoTile("وضعیت", prop.transactionStatus, DfIcons.Sparkles)
        AgencySpaceInfoTile("قیمت هر متر", prop.pricePerMeterLabel, DfIcons.Calculator)
        prop.featureDataRows.forEach { row ->
            AgencySpaceInfoTile(row.label, row.value, DfIcons.Layers)
        }
        if (prop.amenityFlags.isNotEmpty()) {
            AgencySpaceInfoTile(
                "امکانات",
                prop.amenityFlags.map { it.label }.filter { it.isNotBlank() }.joinToString(" · "),
                DfIcons.Check,
            )
        }
        AgencySpaceInfoTile("آدرس", prop.address, DfIcons.MapPin)
        AgencySpaceInfoTile("مالک", prop.ownerName, DfIcons.User)
        AgencySpaceInfoTile("تماس", prop.phone, DfIcons.Phone)
        AgencySpaceInfoTile("یادداشت", prop.notes, DfIcons.File)
    }
}

@Composable
private fun AgencySpaceContactSpecsBlock(
    customer: ir.divarfiling.mobile.core.network.AgencySpaceCustomerPreviewDto,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        AgencySpaceInfoTile("نوع مشتری", customer.customerType, DfIcons.Users)
        AgencySpaceInfoTile("وضعیت", customer.status, DfIcons.Sparkles)
        AgencySpaceInfoTile("اولویت", customer.priority, DfIcons.Sparkles)
        AgencySpaceInfoTile("نیاز", customer.needSummary, DfIcons.File, emphasize = true)
        customer.budgetRows.forEach { row ->
            AgencySpaceInfoTile(row.label, row.value, DfIcons.Calculator)
        }
        if (customer.budgetLabel.isNotBlank() && customer.budgetRows.isEmpty()) {
            AgencySpaceInfoTile("بودجه", customer.budgetLabel, DfIcons.Calculator, emphasize = true)
        }
        AgencySpaceInfoTile("رهن و اجاره", customer.rentDepositLabel, DfIcons.Handshake)
        AgencySpaceInfoTile("متراژ", customer.areaLabel, DfIcons.Building)
        AgencySpaceInfoTile("خواب", customer.roomsLabel, DfIcons.Users)
        AgencySpaceInfoTile("نوع ملک", customer.propertyType, DfIcons.Building)
        AgencySpaceInfoTile("محدوده", customer.region, DfIcons.MapPin)
        AgencySpaceInfoTile("مناطق", customer.areasDisplay, DfIcons.MapPin)
        if (customer.amenityTags.isNotEmpty()) {
            AgencySpaceInfoTile("امکانات", customer.amenityTags.joinToString(" · "), DfIcons.Check)
        }
        AgencySpaceInfoTile("تماس", customer.phone, DfIcons.Phone)
        AgencySpaceInfoTile("یادداشت", customer.notes, DfIcons.File)
    }
}

@Composable
private fun detailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
        Spacer(modifier = Modifier.size(AppSpacing.sm))
        Text(
            value,
            style = AppTypography.bodyDescription,
            modifier = Modifier.weight(1f),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
