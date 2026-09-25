package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
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
                if (state.page > 1) put("page", state.page.toString())
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
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
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
}

data class AgencySpaceDetailUiState(
    val item: AgencySpaceItemDto? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
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

    Scaffold(containerColor = DfScreenContainerColor, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(padding).statusBarsPadding(),
        ) {
            LazyColumn(
                contentPadding = teamListContentPadding(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "فضای آژانس",
                        subtitle = "اشتراک فایل و مخاطب با تیم",
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
                    LazyRow(
                        modifier = Modifier.padding(horizontal = pad),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        items(AgencySpaceTab.labels.size) { index ->
                            val (key, label) = AgencySpaceTab.labels[index]
                            DfSoftChip(
                                label = label,
                                selected = state.tab == key,
                                onClick = { viewModel.setTab(key) },
                            )
                        }
                    }
                }
                when {
                    state.isLoading -> item { DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad)) }
                    state.items.isEmpty() -> item {
                        val (title, desc) = agencySpaceEmptyCopy(state.tab)
                        DfEmptyState(
                            title = title,
                            description = desc,
                            variant = DfEmptyVariant.Inbox,
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                    else -> items(state.items, key = { it.id }) { item ->
                        AgencySpaceListCard(
                            item = item,
                            isSubmitting = state.isSubmitting,
                            onOpen = { onOpenItem(item.id) },
                            onCopy = {
                                viewModel.copyItem(item.id, onOpenCopiedTarget)
                            },
                            onViewCopied = onOpenCopiedTarget,
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgencySpaceKpiRow(
    kpis: AgencySpaceHeaderKpisDto,
    modifier: Modifier = Modifier,
) {
    TeamMetricsRow(
        metrics = listOf(
            TeamMetric("فایل‌ها", kpis.activeProperties.toString(), DfColors.Blue, DfIcons.Building),
            TeamMetric("مخاطبین", kpis.activeContacts.toString(), DfColors.Green, DfIcons.Users),
            TeamMetric("ذخیره‌ها", kpis.totalCopies.toString(), DfColors.Amber, DfIcons.Sparkles),
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

@Composable
private fun AgencySpaceListCard(
    item: AgencySpaceItemDto,
    isSubmitting: Boolean,
    onOpen: () -> Unit,
    onCopy: () -> Unit,
    onViewCopied: (kind: String, targetId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isProperty = item.property != null
    val title = item.property?.title ?: item.customer?.name ?: "—"
    val region = item.property?.region ?: item.customer?.region.orEmpty()
    val priceOrBudget = item.property?.priceLabel ?: item.customer?.budgetLabel.orEmpty()
    val metaLine = listOfNotNull(
        item.property?.dealMode?.takeIf { it.isNotBlank() },
        item.customer?.needSummary?.takeIf { it.isNotBlank() },
    ).joinToString(" · ")
    val copyKind = if (isProperty) "property" else "customer"
    DfCard(modifier = modifier, onClick = onOpen) {
        Row(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            if (isProperty) {
                DfListingImage(
                    thumbnailUrl = item.property?.thumbnailUrl,
                    modifier = Modifier.size(width = 88.dp, height = 72.dp),
                    shape = RoundedCornerShape(AppSpacing.sm),
                    contentDescription = title,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
            ) {
                Text(title, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (region.isNotBlank()) {
                    Text(region, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
                }
                if (priceOrBudget.isNotBlank()) {
                    Text(priceOrBudget, style = AppTypography.bodyDescription, fontWeight = FontWeight.Medium)
                } else if (metaLine.isNotBlank()) {
                    Text(metaLine, style = AppTypography.bodyDescription, color = DfThemeColors.textSecondary())
                }
                AgencySpacePublisherRow(
                    name = item.publisher.name,
                    roleLabel = item.publisher.roleLabel.ifBlank { "منتشرکننده" },
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AgencySpaceVisibilityBadge(visibility = item.visibility, label = item.visibilityLabel)
                    if (item.alreadyCopied) {
                        DfBadge(text = if (isProperty) "در فایل‌های من" else "در مخاطبین من")
                    }
                }
                when {
                    item.alreadyCopied && item.copyTargetId != null -> {
                        DfSecondaryButton(
                            text = if (isProperty) "مشاهده فایل" else "مشاهده مخاطب",
                            onClick = { onViewCopied(copyKind, item.copyTargetId!!) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    item.canCopy && !item.alreadyCopied -> {
                        DfPrimaryButton(
                            text = when {
                                isSubmitting -> "…"
                                isProperty -> "افزودن به فایل‌های من"
                                else -> "افزودن به مخاطبین من"
                            },
                            onClick = onCopy,
                            enabled = !isSubmitting,
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

    Scaffold(containerColor = DfScreenContainerColor, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
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
                    description = state.error ?: "این مورد در دسترس نیست.",
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
                            DfCard(modifier = Modifier.padding(horizontal = pad)) {
                                Column(
                                    modifier = Modifier.padding(AppSpacing.cardPadding),
                                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                ) {
                                    Text("اطلاعات کلیدی", style = AppTypography.sectionTitle)
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
                                    if (item.note.isNotBlank()) {
                                        detailRow("یادداشت", item.note)
                                    }
                                }
                            }
                        }
                        item {
                            DfCard(modifier = Modifier.padding(horizontal = pad)) {
                                Column(
                                    modifier = Modifier.padding(AppSpacing.cardPadding),
                                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                ) {
                                    Text("ناشر", style = AppTypography.sectionTitle)
                                    AgencySpacePublisherRow(
                                        name = item.publisher.name,
                                        roleLabel = item.publisher.roleLabel.ifBlank { "منتشرکننده" },
                                    )
                                }
                            }
                        }
                        item {
                            DfCard(modifier = Modifier.padding(horizontal = pad)) {
                                Column(
                                    modifier = Modifier.padding(AppSpacing.cardPadding),
                                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                ) {
                                    Text("وضعیت اشتراک", style = AppTypography.sectionTitle)
                                    AgencySpaceVisibilityBadge(
                                        visibility = item.visibility,
                                        label = item.visibilityLabel,
                                    )
                                    if (item.statusLabel.isNotBlank()) {
                                        Text(
                                            item.statusLabel,
                                            style = AppTypography.bodyDescription,
                                            color = DfThemeColors.textSecondary(),
                                        )
                                    }
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
                                if (item.canUnpublish) {
                                    DfSecondaryButton(
                                        text = "لغو انتشار",
                                        onClick = viewModel::unpublish,
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
