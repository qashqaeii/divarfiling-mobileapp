package ir.divarfiling.mobile.feature.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MoreHubUiState(
    val userName: String = "",
    val notificationBadgeCount: Int = 0,
    val teamUnreadCount: Int = 0,
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class MoreHubViewModel @Inject constructor(
    sessionStore: SessionStore,
    private val dashboardRepository: DashboardRepository,
    private val teamRepository: ir.divarfiling.mobile.data.repository.TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MoreHubUiState())
    val uiState: StateFlow<MoreHubUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionStore.currentUser.collect { user ->
                _uiState.update {
                    it.copy(userName = user?.fullName?.substringBefore(" ") ?: "کاربر")
                }
            }
        }
        viewModelScope.launch {
            refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val dashboard = dashboardRepository.getDashboard()
            val teamUnread = when (val unread = teamRepository.getUnread()) {
                is ApiResult.Success -> unread.data.total
                is ApiResult.Error -> 0
            }
            when (dashboard) {
                is ApiResult.Success ->
                    _uiState.update {
                        it.copy(
                            notificationBadgeCount = dashboard.data.notificationsUnread,
                            teamUnreadCount = teamUnread,
                            isRefreshing = false,
                        )
                    }
                is ApiResult.Error -> _uiState.update {
                    it.copy(teamUnreadCount = teamUnread, isRefreshing = false)
                }
            }
        }
    }
}
