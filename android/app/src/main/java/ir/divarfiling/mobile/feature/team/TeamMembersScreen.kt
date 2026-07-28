package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.remember
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
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.network.TeamMemberDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamMembersUiState(
    val members: List<TeamMemberDto> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class TeamMembersViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamMembersUiState())
    val uiState: StateFlow<TeamMembersUiState> = _uiState.asStateFlow()

    init { refresh(true) }

    fun refresh(initial: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = initial && it.members.isEmpty(),
                    isRefreshing = !initial || it.members.isNotEmpty(),
                    error = null,
                )
            }
            when (val result = repository.getMembers()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(members = result.data.members, isLoading = false, isRefreshing = false)
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
fun TeamMembersScreen(
    onBack: () -> Unit,
    viewModel: TeamMembersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it) }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(padding).statusBarsPadding(),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "اعضای تیم",
                        subtitle = "${state.members.size} نفر فعال در آژانس",
                        titleIconRes = DfDecorIcons.Users,
                        onBack = onBack,
                    )
                }
                when {
                    state.isLoading -> item {
                        DfCardListSkeleton(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal))
                    }
                    state.members.isEmpty() -> item {
                        DfEmptyState(
                            title = "عضوی نیست",
                            subtitle = "هنوز عضوی در آژانس ثبت نشده است.",
                            variant = DfEmptyVariant.Empty,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                    else -> items(state.members, key = { it.id }) { member ->
                        DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(member.name, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
                                    if (member.title.isNotBlank()) {
                                        Text(member.title, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
                                    }
                                    if (member.phone.isNotBlank()) {
                                        Text(member.phone, style = AppTypography.labelSmall, color = DfColors.TextSecondary)
                                    }
                                }
                                DfBadge(
                                    text = member.roleLabel.ifBlank { member.role },
                                    color = roleColor(member.role).copy(alpha = 0.15f),
                                    textColor = roleColor(member.role),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun roleColor(role: String) = when (role) {
    "owner" -> DfColors.Amber
    "manager" -> DfColors.Purple
    "secretary" -> DfColors.Blue
    else -> DfColors.Green
}
