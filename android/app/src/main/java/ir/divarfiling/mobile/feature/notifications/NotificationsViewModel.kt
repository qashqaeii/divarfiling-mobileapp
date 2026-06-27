package ir.divarfiling.mobile.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.NotificationDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.NotificationRepository
import ir.divarfiling.mobile.feature.home.HomeNotificationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class NotificationsUiState(
    val items: List<NotificationListItem> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
)

data class NotificationListItem(
    val id: Long,
    val title: String,
    val body: String,
    val timeAgo: String,
    val type: HomeNotificationType,
    val isRead: Boolean,
    val deepLink: String?,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        load(refreshing = false)
    }

    fun refresh() {
        load(refreshing = true)
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            when (val result = repository.getNotifications(page = state.page + 1)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        items = it.items + result.data.items.map { dto -> dto.toListItem() },
                        page = result.data.page,
                        hasMore = result.data.hasMore,
                        isLoadingMore = false,
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoadingMore = false, error = result.message)
                }
            }
        }
    }

    fun markReadAndReturnDeepLink(id: Long): String? {
        val item = _uiState.value.items.find { it.id == id } ?: return null
        if (!item.isRead) {
            viewModelScope.launch {
                repository.markRead(id)
                _uiState.update { state ->
                    state.copy(
                        items = state.items.map {
                            if (it.id == id) it.copy(isRead = true) else it
                        },
                        unreadCount = (state.unreadCount - 1).coerceAtLeast(0),
                    )
                }
            }
        }
        return item.deepLink
    }

    private fun load(refreshing: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !refreshing && it.items.isEmpty(),
                    isRefreshing = refreshing,
                    error = null,
                )
            }
            when (val unread = repository.getUnreadCount()) {
                is ApiResult.Success -> _uiState.update { it.copy(unreadCount = unread.data) }
                is ApiResult.Error -> Unit
            }
            when (val result = repository.getNotifications(page = 1)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        items = result.data.items.map { dto -> dto.toListItem() },
                        page = 1,
                        hasMore = result.data.hasMore,
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

    private fun NotificationDto.toListItem(): NotificationListItem {
        val notifType = when (type?.lowercase()) {
            "extract_complete", "new_dataset" -> HomeNotificationType.ExtractSuccess
            "price_drop" -> HomeNotificationType.PriceDrop
            "customer_match" -> HomeNotificationType.NewMatch
            "license_expiry" -> HomeNotificationType.License
            "overdue_followup", "today_digest", "reminder_call", "reminder_visit" -> HomeNotificationType.FollowUp
            else -> HomeNotificationType.General
        }
        return NotificationListItem(
            id = id,
            title = title,
            body = body.orEmpty(),
            timeAgo = formatTimeAgo(createdAt),
            type = notifType,
            isRead = isRead,
            deepLink = deepLink,
        )
    }

    private fun formatTimeAgo(iso: String?): String {
        if (iso.isNullOrBlank()) return "اخیراً"
        return try {
            val date = LocalDate.parse(iso.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
            val days = LocalDate.now().toEpochDay() - date.toEpochDay()
            when {
                days <= 0 -> "امروز"
                days == 1L -> "دیروز"
                days < 7 -> "$days روز پیش"
                else -> iso.take(10)
            }
        } catch (_: Exception) {
            iso.take(10)
        }
    }
}
