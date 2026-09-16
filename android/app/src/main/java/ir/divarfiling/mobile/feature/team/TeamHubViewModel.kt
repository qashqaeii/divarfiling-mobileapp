package ir.divarfiling.mobile.feature.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.WorkspaceBridgePaths
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.network.AgencyHomeDto
import ir.divarfiling.mobile.core.network.TeamPanelNotificationDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamHubUiState(
    val userName: String = "",
    val home: AgencyHomeDto? = null,
    val notifications: List<TeamPanelNotificationDto> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val webBridgeUrl: String? = null,
)

@HiltViewModel
class TeamHubViewModel @Inject constructor(
    private val repository: TeamRepository,
    sessionStore: SessionStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamHubUiState())
    val uiState: StateFlow<TeamHubUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionStore.currentUser.collect { user ->
                _uiState.update {
                    it.copy(userName = user?.fullName?.substringBefore(" ") ?: "کاربر")
                }
            }
        }
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
            when (val home = repository.getAgencyHome()) {
                is ApiResult.Success -> {
                    val notifications = if (home.data.hasAgency) {
                        when (val notes = repository.getPanelNotifications()) {
                            is ApiResult.Success -> notes.data.notifications.take(5)
                            is ApiResult.Error -> emptyList()
                        }
                    } else emptyList()
                    _uiState.update {
                        it.copy(
                            home = home.data,
                            notifications = notifications,
                            isLoading = false,
                            isRefreshing = false,
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = home.message)
                }
            }
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            when (val result = repository.markPanelReadAll()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(successMessage = "اعلان‌ها خوانده شد") }
                    refresh()
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markPanelRead(id)
            refresh()
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }

    fun openTeamWorkspaceBridge() {
        viewModelScope.launch {
            when (val result = repository.createWorkspaceBridge(WorkspaceBridgePaths.CRM_TEAM)) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(webBridgeUrl = result.data.bridgeUrl) }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun consumeWebBridge() = _uiState.update { it.copy(webBridgeUrl = null) }
}
