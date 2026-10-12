package ir.divarfiling.mobile.feature.team

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import ir.divarfiling.mobile.core.design.components.showDfMessage
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfFilterChipRow
import ir.divarfiling.mobile.core.design.components.DfFilterOption
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.network.TeamAnnouncementDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AnnouncementFilter { All, Unread, Important }

data class TeamAnnouncementsUiState(
    val items: List<TeamAnnouncementDto> = emptyList(),
    val unread: Int = 0,
    val selected: TeamAnnouncementDto? = null,
    val canCreate: Boolean = false,
    val showCreate: Boolean = false,
    val createTitle: String = "",
    val createBody: String = "",
    val createImportant: Boolean = false,
    val isCreating: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isMarkingAll: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class TeamAnnouncementsViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamAnnouncementsUiState())
    val uiState: StateFlow<TeamAnnouncementsUiState> = _uiState.asStateFlow()
    private var filter: AnnouncementFilter = AnnouncementFilter.All

    init {
        refresh(true)
        viewModelScope.launch {
            when (val home = repository.getAgencyHome()) {
                is ApiResult.Success -> {
                    val perms = home.data.permissions
                    _uiState.update {
                        it.copy(canCreate = perms.canManage)
                    }
                }
                is ApiResult.Error -> Unit
            }
        }
    }

    fun setFilter(value: AnnouncementFilter) {
        filter = value
        refresh(true)
    }

    fun refresh(initial: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = initial && it.items.isEmpty(),
                    isRefreshing = !initial || it.items.isNotEmpty(),
                    error = null,
                )
            }
            when (
                val result = repository.getAnnouncements(
                    unreadOnly = filter == AnnouncementFilter.Unread,
                    importantOnly = filter == AnnouncementFilter.Important,
                )
            ) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        items = result.data.announcements,
                        unread = result.data.unread,
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

    fun open(item: TeamAnnouncementDto) {
        viewModelScope.launch {
            when (val result = repository.getAnnouncement(item.id)) {
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            selected = result.data,
                            items = state.items.map {
                                if (it.id == item.id) it.copy(isRead = true) else it
                            },
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(selected = item, error = result.message)
                }
            }
        }
    }

    fun dismissDetail() = _uiState.update { it.copy(selected = null) }

    fun markAllRead() {
        if (_uiState.value.isMarkingAll || _uiState.value.unread <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isMarkingAll = true, error = null) }
            when (val result = repository.markAllAnnouncementsRead()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isMarkingAll = false,
                            successMessage = if (result.data > 0) {
                                "${DateUtils.toPersianDigits(result.data.toString())} اعلامیه خوانده شد"
                            } else {
                                "همه اعلامیه‌ها قبلاً خوانده شده بودند"
                            },
                        )
                    }
                    refresh(true)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isMarkingAll = false, error = result.message)
                }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }

    fun toggleCreate(show: Boolean) = _uiState.update {
        it.copy(
            showCreate = show,
            createTitle = if (show) it.createTitle else "",
            createBody = if (show) it.createBody else "",
            createImportant = if (show) it.createImportant else false,
        )
    }

    fun onCreateTitleChange(v: String) = _uiState.update { it.copy(createTitle = v) }
    fun onCreateBodyChange(v: String) = _uiState.update { it.copy(createBody = v) }
    fun onCreateImportantChange(checked: Boolean) = _uiState.update { it.copy(createImportant = checked) }

    fun submitCreate() {
        val state = _uiState.value
        if (state.isCreating) return
        if (state.createTitle.isBlank() || state.createBody.isBlank()) {
            _uiState.update { it.copy(error = "عنوان و متن اطلاعیه الزامی است") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, error = null) }
            val importance = if (state.createImportant) "important" else "normal"
            when (val result = repository.createAnnouncement(state.createTitle, state.createBody, importance)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isCreating = false,
                        showCreate = false,
                        createTitle = "",
                        createBody = "",
                        successMessage = "اطلاعیه ثبت شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isCreating = false, error = result.message)
                }
            }
            refresh(true)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamAnnouncementsScreen(
    onBack: () -> Unit,
    viewModel: TeamAnnouncementsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var filter by remember { mutableStateOf(AnnouncementFilter.All) }
    val pad = teamHorizontalPadding()

    LaunchedEffect(state.error, state.successMessage) {
        state.successMessage?.let { snackbar.showDfMessage(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showDfMessage(it); viewModel.clearMessage() }
    }

    state.selected?.let { detail ->
        DfModalBottomSheet(onDismissRequest = viewModel::dismissDetail) {
            DfSheetScaffold(
                title = detail.title,
                subtitle = detail.createdBy.ifBlank { "اعلامیه آژانس" },
                icon = DfIcons.Sparkles,
                iconContainerColor = DfThemeColors.warningContainer(),
                iconTint = DfThemeColors.warning(),
                onClose = viewModel::dismissDetail,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    androidx.compose.foundation.layout.Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        if (detail.importance == "important" || detail.importance == "urgent") {
                            DfBadge(
                                text = "مهم",
                                color = DfThemeColors.errorContainer(),
                                textColor = DfThemeColors.error(),
                            )
                        }
                        if (detail.isPinned) {
                            DfBadge(
                                text = "سنجاق",
                                color = DfThemeColors.primaryContainer(),
                                textColor = DfThemeColors.primary(),
                            )
                        }
                    }
                    Text(
                        detail.body,
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textPrimary(),
                    )
                    Text(
                        DateUtils.formatRelativeFa(detail.publishedAt),
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }

    if (state.showCreate) {
        BackHandler { viewModel.toggleCreate(false) }
        Scaffold(
            containerColor = DfScreenContainerColor,
            snackbarHost = { TeamAgencySnackbarHost(snackbar) },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .navigationBarsPadding(),
            ) {
                AgencyConversationHeader(
                    title = "اطلاعیه جدید",
                    subtitle = "انتشار برای تیم آژانس",
                    onBack = { viewModel.toggleCreate(false) },
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = pad, vertical = AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    DfTextField(
                        value = state.createTitle,
                        onValueChange = viewModel::onCreateTitleChange,
                        label = "عنوان",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DfTextField(
                        value = state.createBody,
                        onValueChange = viewModel::onCreateBodyChange,
                        label = "متن اطلاعیه",
                        singleLine = false,
                        minLines = 8,
                        maxLines = 16,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        Checkbox(
                            checked = state.createImportant,
                            onCheckedChange = viewModel::onCreateImportantChange,
                        )
                        Text(
                            "اولویت مهم",
                            style = AppTypography.bodyDescription,
                            color = DfThemeColors.textPrimary(),
                        )
                    }
                }
                DfPrimaryButton(
                    text = if (state.isCreating) "در حال ثبت…" else "ثبت اطلاعیه",
                    onClick = viewModel::submitCreate,
                    enabled = !state.isCreating &&
                        state.createTitle.isNotBlank() &&
                        state.createBody.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = pad, vertical = AppSpacing.sm),
                )
            }
        }
        return
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { TeamAgencySnackbarHost(snackbar) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                            title = "اعلامیه‌ها",
                            subtitle = if (state.unread > 0) {
                                "${DateUtils.toPersianDigits(state.unread.toString())} خوانده‌نشده"
                            } else {
                                "تابلو اطلاع‌رسانی تیم"
                            },
                            icon = DfIcons.Sparkles,
                            accent = AgencyHeaderAccent.Management,
                            onBack = onBack,
                            actions = {
                                if (state.canCreate) {
                                    androidx.compose.material3.TextButton(onClick = { viewModel.toggleCreate(true) }) {
                                        Text(
                                            "اطلاعیه جدید",
                                            style = AppTypography.labelSmall,
                                            color = DfThemeColors.primary(),
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                                if (state.unread > 0) {
                                    androidx.compose.material3.TextButton(
                                        onClick = viewModel::markAllRead,
                                        enabled = !state.isMarkingAll,
                                    ) {
                                        Text(
                                            if (state.isMarkingAll) "…" else "همه خوانده شد",
                                            style = AppTypography.labelSmall,
                                            color = DfThemeColors.primary(),
                                        )
                                    }
                                }
                            },
                        )
                    }
                    item {
                        DfFilterChipRow(
                            options = listOf(
                                DfFilterOption(AnnouncementFilter.All, "همه"),
                                DfFilterOption(AnnouncementFilter.Unread, "خوانده‌نشده"),
                                DfFilterOption(AnnouncementFilter.Important, "مهم"),
                            ),
                            selected = filter,
                            onSelect = {
                                filter = it
                                viewModel.setFilter(it)
                            },
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                    state.error?.let { err ->
                        if (!state.isLoading && state.items.isEmpty()) {
                            item {
                                DfErrorBanner(message = err, modifier = Modifier.padding(horizontal = pad))
                            }
                        }
                    }
                    when {
                        state.isLoading -> item {
                            DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad))
                        }
                        state.items.isEmpty() && state.error == null -> item {
                            DfEmptyState(
                                title = "اعلامیه‌ای نیست",
                                subtitle = "وقتی مدیر اعلامیه بگذارد اینجا می‌بینید.",
                                variant = DfEmptyVariant.Empty,
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                        else -> items(state.items, key = { it.id }) { item ->
                            TeamAnnouncementListCard(
                                item = item,
                                onClick = { viewModel.open(item) },
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                    }
                }
            }
        }
    }
}
