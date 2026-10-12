package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
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
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.network.AgencyInvitePreviewPayload
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgencyInviteUiState(
    val preview: AgencyInvitePreviewPayload? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val accepted: Boolean = false,
)

@HiltViewModel
class AgencyInviteViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgencyInviteUiState())
    val uiState: StateFlow<AgencyInviteUiState> = _uiState.asStateFlow()
    private var token: String = ""

    fun bind(token: String) {
        if (this.token == token && _uiState.value.preview != null) return
        this.token = token
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.preview == null, error = null) }
            when (val result = repository.getAgencyInvitePreview(token)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(preview = result.data, isLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun accept(consent: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.respondAgencyInvite(token, "accept", consent)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isSubmitting = false, accepted = true)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun decline() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = repository.respondAgencyInvite(token, "decline")) {
                is ApiResult.Success -> _uiState.update { it.copy(isSubmitting = false) }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyInviteAcceptScreen(
    token: String,
    onBack: () -> Unit,
    onAccepted: () -> Unit,
    viewModel: AgencyInviteViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var consent by remember { mutableStateOf(false) }
    val pad = teamHorizontalPadding()

    LaunchedEffect(token) { viewModel.bind(token) }
    LaunchedEffect(state.accepted) {
        if (state.accepted) onAccepted()
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it) }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            TeamAmbientBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            TeamAgencyScreenHeader(
                title = "دعوت به آژانس",
                subtitle = "عضویت در تیم",
                icon = ir.divarfiling.mobile.core.design.DfIcons.UserPlus,
                accent = AgencyHeaderAccent.Conversation,
                onBack = onBack,
            )
            when {
                state.isLoading -> DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad))
                state.preview == null -> DfEmptyState(
                    title = "دعوت نامعتبر",
                    subtitle = state.error ?: "لینک منقضی یا نامعتبر است.",
                    variant = DfEmptyVariant.Error,
                    modifier = Modifier.padding(horizontal = pad),
                )
                else -> {
                    val p = state.preview!!
                    if (!p.valid && p.error.isNotBlank()) {
                        DfEmptyState(
                            title = "قابل پذیرش نیست",
                            subtitle = p.error,
                            variant = DfEmptyVariant.Error,
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    } else {
                        DfCard(modifier = Modifier.padding(horizontal = pad)) {
                            Column(
                                modifier = Modifier.padding(AppSpacing.cardPadding),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            ) {
                                Text(p.agency.name, fontWeight = FontWeight.Bold, style = AppTypography.sectionTitle)
                                Text("نقش: ${p.invitation.roleLabel}")
                                if (p.inviterName.isNotBlank()) {
                                    Text("دعوت‌کننده: ${p.inviterName}", style = AppTypography.bodyDescription)
                                }
                                Text("موبایل: ${p.invitation.maskedPhone}", style = AppTypography.bodyDescription)
                                if (!p.userPhoneMatches && p.userAuthenticated) {
                                    Text(
                                        "این دعوت با شماره حساب شما هم‌خوان نیست.",
                                        color = DfThemeColors.warning(),
                                        style = AppTypography.labelSmall,
                                    )
                                }
                            }
                        }
                        if (p.canAccept) {
                            RowConsent(consent = consent, onToggle = { consent = it })
                            DfPrimaryButton(
                                text = if (state.isSubmitting) "در حال ثبت…" else "پذیرش و ورود به آژانس",
                                onClick = { viewModel.accept(consent) },
                                enabled = consent && p.userPhoneMatches && !state.isSubmitting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = pad),
                            )
                            DfSecondaryButton(
                                text = "رد دعوت",
                                onClick = viewModel::decline,
                                enabled = !state.isSubmitting,
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

@Composable
private fun RowConsent(consent: Boolean, onToggle: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = teamHorizontalPadding(), vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = consent, onCheckedChange = onToggle)
        Text(
            "رضایت‌نامه دید داده و قوانین همکاری تیمی را می‌پذیرم.",
            style = AppTypography.labelSmall,
            modifier = Modifier.weight(1f),
        )
    }
}
