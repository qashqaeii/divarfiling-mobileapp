package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfFilterChipRow
import ir.divarfiling.mobile.core.design.components.DfFilterOption
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
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
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class TeamAnnouncementsViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamAnnouncementsUiState())
    val uiState: StateFlow<TeamAnnouncementsUiState> = _uiState.asStateFlow()
    private var filter: AnnouncementFilter = AnnouncementFilter.All

    init { refresh(true) }

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

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it) }
    }

    state.selected?.let { detail ->
        DfModalBottomSheet(onDismissRequest = viewModel::dismissDetail) {
            DfSheetScaffold(
                title = detail.title,
                subtitle = detail.createdBy.ifBlank { "اعلامیه آژانس" },
                onClose = viewModel::dismissDetail,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    androidx.compose.foundation.layout.Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        if (detail.importance == "important" || detail.importance == "urgent") {
                            DfBadge(text = "مهم", color = DfColors.RoseLight, textColor = DfColors.Rose)
                        }
                        if (detail.isPinned) {
                            DfBadge(text = "سنجاق", color = DfColors.AmberLight, textColor = DfColors.Amber)
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
                            title = "اعلامیه‌ها",
                            subtitle = if (state.unread > 0) {
                                "${state.unread} خوانده‌نشده"
                            } else {
                                "تابلو اطلاع‌رسانی تیم"
                            },
                            titleIconRes = DfDecorIcons.Sparkles,
                            onBack = onBack,
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
                    when {
                        state.isLoading -> item {
                            DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad))
                        }
                        state.items.isEmpty() -> item {
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
