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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfConfirmBottomSheet
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfSoftChip
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
)

@HiltViewModel
class AgencySpaceListViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgencySpaceListUiState())
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
            val params = buildMap {
                put("tab", state.tab)
                if (state.query.isNotBlank()) put("q", state.query.trim())
                put("page", state.page.coerceAtLeast(1).toString())
                if (state.canModerateSpace && state.publicationStatus != "active") {
                    put("publication_status", state.publicationStatus)
                }
            }
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
            val params = buildMap {
                put("tab", state.tab)
                if (state.query.isNotBlank()) put("q", state.query.trim())
                put("page", nextPage.toString())
                if (state.canModerateSpace && state.publicationStatus != "active") {
                    put("publication_status", state.publicationStatus)
                }
            }
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
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
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
                modifier = Modifier.fillMaxSize().statusBarsPadding(),
            ) {
            LazyColumn(
                contentPadding = teamListContentPadding(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "فضای آژانس",
                        subtitle = if (state.total > 0) {
                            "${ir.divarfiling.mobile.core.design.DateUtils.toPersianDigits(state.total.toString())} انتشار"
                        } else {
                            "اشتراک فایل و مخاطب با تیم"
                        },
                        sectionLabel = DfHeaderSections.TEAM,
                        titleIconRes = DfDecorIcons.Layers,
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
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    LifecycleResumeEffect(itemId) {
        viewModel.load()
        onPauseOrDispose { }
    }

    Scaffold(containerColor = DfScreenContainerColor, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            TeamAmbientBackground()
            Column(modifier = Modifier.fillMaxSize()) {
            DfHubPageHeader(
                title = state.item?.property?.title ?: state.item?.customer?.name ?: "فضای آژانس",
                subtitle = state.item?.publishedAtDisplay?.takeIf { it.isNotBlank() } ?: "جزئیات انتشار",
                sectionLabel = DfHeaderSections.TEAM,
                titleIconRes = DfDecorIcons.Layers,
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
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                        if (isProperty && !item.property?.thumbnailUrl.isNullOrBlank()) {
                            item {
                                DfListingImage(
                                    thumbnailUrl = item.property?.thumbnailUrl,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = pad)
                                        .height(180.dp),
                                    shape = RoundedCornerShape(AppSpacing.md),
                                    contentScale = ContentScale.Crop,
                                    contentDescription = item.property?.title,
                                )
                            }
                        }
                        item {
                            AgencySpaceDetailSection(
                                title = "اطلاعات کلیدی",
                                modifier = Modifier.padding(horizontal = pad),
                            ) {
                                item.property?.let { prop ->
                                    if (prop.region.isNotBlank()) detailRow("منطقه", prop.region)
                                    if (prop.priceLabel.isNotBlank()) detailRow("قیمت", prop.priceLabel)
                                    if (prop.propertyType.isNotBlank()) detailRow("نوع ملک", prop.propertyType)
                                    if (prop.dealMode.isNotBlank()) detailRow("معامله", prop.dealMode)
                                }
                                item.customer?.let { cust ->
                                    if (cust.customerType.isNotBlank()) detailRow("نوع نیاز", cust.customerType)
                                    if (cust.needSummary.isNotBlank()) detailRow("نیاز", cust.needSummary)
                                    if (cust.budgetLabel.isNotBlank()) detailRow("بودجه", cust.budgetLabel)
                                    if (cust.region.isNotBlank()) detailRow("محدوده", cust.region)
                                }
                                if (item.note.isNotBlank()) detailRow("یادداشت", item.note)
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
) {
    val typeLabel = if (isProperty) "ملک اشتراکی" else "مخاطب اشتراکی"
    val accent = if (isProperty) DfThemeColors.info() else DfThemeColors.primary()
    DfCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                DfBadge(text = typeLabel, color = accent.copy(alpha = 0.12f), textColor = accent)
                Text(
                    title,
                    style = AppTypography.pageTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                }
            }
        }
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
