package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.showDfMessage
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.network.AgencyAuditEntryDto
import ir.divarfiling.mobile.core.network.AgencySettingsPayload
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgencyProfileUiState(
    val settings: AgencySettingsPayload? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class AgencyProfileViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgencyProfileUiState())
    val uiState: StateFlow<AgencyProfileUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.settings == null, error = null) }
            when (val result = repository.getAgencySettings()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(settings = result.data, isLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun save(name: String, routing: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val result = repository.patchAgencySettings(name.trim(), routing)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(settings = result.data, isSaving = false, successMessage = "ذخیره شد")
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSaving = false, error = result.message)
                }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyProfileScreen(
    onBack: () -> Unit,
    viewModel: AgencyProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val pad = teamHorizontalPadding()
    var name by remember(state.settings?.name) { mutableStateOf(state.settings?.name.orEmpty()) }
    var routing by remember(state.settings?.leadRoutingPolicy) {
        mutableStateOf(state.settings?.leadRoutingPolicy.orEmpty())
    }

    LaunchedEffect(state.settings) {
        state.settings?.let {
            name = it.name
            routing = it.leadRoutingPolicy
        }
    }
    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showDfMessage(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showDfMessage(it); viewModel.clearMessage() }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { TeamAgencySnackbarHost(snackbar) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            TeamAmbientBackground()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = teamListContentPadding(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            item {
                TeamAgencyScreenHeader(
                    title = "پروفایل آژانس",
                    subtitle = "نام و مسیریابی سرنخ",
                    icon = DfIcons.Building,
                    accent = AgencyHeaderAccent.Detail,
                    onBack = onBack,
                )
            }
            when {
                state.isLoading -> item { DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad)) }
                state.settings == null -> item {
                    DfEmptyState(
                        title = "در دسترس نیست",
                        subtitle = state.error.orEmpty(),
                        variant = DfEmptyVariant.Error,
                        modifier = Modifier.padding(horizontal = pad),
                    )
                }
                else -> {
                    val s = state.settings!!
                    item {
                        TeamSectionLabel(
                            title = "اطلاعات پایه",
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                    item {
                        DfTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "نام آژانس",
                            enabled = s.permissions.canEdit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = pad),
                        )
                    }
                    item {
                        TeamSectionLabel(
                            title = "مسیریابی سرنخ",
                            subtitle = "نحوه توزیع سرنخ بین مشاوران",
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
                            s.leadRoutingOptions.forEach { opt ->
                                DfSoftChip(
                                    text = opt.label,
                                    selected = routing == opt.value,
                                    onClick = { if (s.permissions.canEdit) routing = opt.value },
                                )
                            }
                        }
                    }
                    item {
                        if (!s.permissions.canEdit) {
                            Text(
                                "فقط مدیر می‌تواند تنظیمات را ویرایش کند.",
                                style = AppTypography.labelSmall,
                                color = DfThemeColors.textMuted(),
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        } else {
                            DfPrimaryButton(
                                text = if (state.isSaving) "در حال ذخیره…" else "ذخیره تغییرات",
                                onClick = { viewModel.save(name, routing) },
                                enabled = !state.isSaving && name.isNotBlank(),
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

data class AgencyAuditUiState(
    val entries: List<AgencyAuditEntryDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class AgencyAuditViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgencyAuditUiState())
    val uiState: StateFlow<AgencyAuditUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.entries.isEmpty(), error = null) }
            when (val result = repository.getAgencyAudit()) {
                is ApiResult.Success -> {
                    val flat = result.data.timeline.groups.flatMap { it.entries }
                    _uiState.update { it.copy(entries = flat, isLoading = false) }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyAuditScreen(
    onBack: () -> Unit,
    viewModel: AgencyAuditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pad = teamHorizontalPadding()

    Scaffold(containerColor = DfScreenContainerColor) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            TeamAmbientBackground()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = teamListContentPadding(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            item {
                TeamAgencyScreenHeader(
                    title = "گزارش ممیزی",
                    subtitle = "رویدادهای حساس (بدون Secret)",
                    icon = DfIcons.ListTodo,
                    accent = AgencyHeaderAccent.Management,
                    onBack = onBack,
                )
            }
            when {
                state.isLoading -> item { DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad)) }
                state.entries.isEmpty() -> item {
                    DfEmptyState(
                        title = "رویدادی نیست",
                        subtitle = state.error ?: "هنوز لاگ ممیزی ثبت نشده.",
                        variant = DfEmptyVariant.Empty,
                        modifier = Modifier.padding(horizontal = pad),
                    )
                }
                else -> item {
                    DfCard(modifier = Modifier.padding(horizontal = pad)) {
                        Column(modifier = Modifier.padding(AppSpacing.sm)) {
                            state.entries.forEach { entry ->
                                TeamActivityTimelineRow(
                                    title = entry.title.ifBlank { entry.summary },
                                    actorName = entry.actorName,
                                    timeLabel = entry.timeLabel,
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
