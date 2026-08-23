package ir.divarfiling.mobile.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.network.NotificationDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.NotificationRepository
import ir.divarfiling.mobile.feature.home.HomeNotificationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val userName: String = "",
    val items: List<NotificationListItem> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isMarkingAllRead: Boolean = false,
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
    val rawType: String?,
    val isRead: Boolean,
    val deepLink: String?,
) {
    val needsAction: Boolean
        get() {
            if (isRead) return false
            return when (rawType?.lowercase()) {
                "today_digest" -> false
                "overdue_followup",
                "reminder_call",
                "reminder_visit",
                "license_expiry",
                "extract_schedule_due",
                -> true
                else -> type == HomeNotificationType.License || type == HomeNotificationType.FollowUp
            }
        }
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository,
    sessionStore: SessionStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionStore.currentUser.collect { user ->
                _uiState.update {
                    it.copy(userName = user?.fullName?.substringBefore(" ") ?: "کاربر")
                }
            }
        }
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
                        error = null,
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        error = result.message.toUserFacingNotificationError(),
                    )
                }
            }
        }
    }

    fun markAllRead() {
        if (_uiState.value.unreadCount == 0 || _uiState.value.isMarkingAllRead) return
        viewModelScope.launch {
            _uiState.update { it.copy(isMarkingAllRead = true, error = null) }
            when (val result = repository.markAllRead()) {
                is ApiResult.Success -> _uiState.update { state ->
                    state.copy(
                        items = state.items.map { it.copy(isRead = true) },
                        unreadCount = 0,
                        isMarkingAllRead = false,
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isMarkingAllRead = false,
                        error = result.message.toUserFacingNotificationError(
                            fallback = "خوانده شدن همه اعلان‌ها انجام نشد.",
                        ),
                    )
                }
            }
        }
    }

    fun markReadAndReturnDeepLink(id: Long): String? {
        val item = _uiState.value.items.find { it.id == id } ?: return null
        if (!item.isRead) {
            _uiState.update { state ->
                state.copy(
                    items = state.items.map {
                        if (it.id == id) it.copy(isRead = true) else it
                    },
                    unreadCount = (state.unreadCount - 1).coerceAtLeast(0),
                )
            }
            viewModelScope.launch {
                repository.markRead(id)
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
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = result.message.toUserFacingNotificationError(),
                    )
                }
            }
        }
    }

    private fun NotificationDto.toListItem(): NotificationListItem {
        return NotificationListItem(
            id = id,
            title = title,
            body = body.orEmpty(),
            timeAgo = DateUtils.formatRelativeTimeAgo(createdAt),
            type = mapNotificationType(type),
            rawType = type,
            isRead = isRead,
            deepLink = deepLink,
        )
    }
}

internal fun mapNotificationType(type: String?): HomeNotificationType = when (type?.lowercase()) {
    "extract_complete", "new_dataset" -> HomeNotificationType.ExtractSuccess
    "extract_schedule_created", "extract_schedule_due" -> HomeNotificationType.ExtractSuccess
    "price_drop" -> HomeNotificationType.PriceDrop
    "customer_match" -> HomeNotificationType.NewMatch
    "license_expiry" -> HomeNotificationType.License
    "overdue_followup", "today_digest", "reminder_call", "reminder_visit" -> HomeNotificationType.FollowUp
    "support_reply" -> HomeNotificationType.Support
    "welcome", "announcement", "app_update", "product_update" -> HomeNotificationType.Announcement
    else -> HomeNotificationType.General
}

internal fun String.toUserFacingNotificationError(
    fallback: String = "بارگذاری اعلان‌ها ناموفق بود.",
): String {
    val trimmed = trim()
    if (trimmed.isBlank()) return fallback
    val lower = trimmed.lowercase()
    val looksTechnical = listOf(
        "exception",
        "http",
        "timeout",
        "socket",
        "unable to resolve",
        "failed to connect",
        "unexpected",
        "status code",
    ).any { it in lower }
    return if (looksTechnical) "اتصال برقرار نشد. دوباره تلاش کنید." else trimmed
}
