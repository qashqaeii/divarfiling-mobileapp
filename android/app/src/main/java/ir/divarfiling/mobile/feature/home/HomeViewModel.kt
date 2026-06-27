package ir.divarfiling.mobile.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.license.LicenseState
import ir.divarfiling.mobile.core.network.NotificationDto
import ir.divarfiling.mobile.core.network.TodayItemDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import ir.divarfiling.mobile.data.repository.DashboardRepository
import ir.divarfiling.mobile.data.repository.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val dashboardRepository: DashboardRepository,
    private val crmRepository: CrmRepository,
    private val syncRepository: SyncRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(sessionStore.currentUser, sessionStore.licenseState) { user, license ->
                user to license
            }.collect { (user, license) ->
                _uiState.update {
                    it.copy(
                        userName = user?.fullName?.substringBefore(" ") ?: "کاربر",
                        agencyName = user?.agencyName,
                        license = license,
                        canExtract = license.canUseLightExtract,
                    )
                }
            }
        }
        loadDashboard(refreshing = false)
        syncInBackground()
    }

    private fun syncInBackground() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val pending = syncRepository.getPendingCount()
            _uiState.update { it.copy(syncPendingCount = pending) }
            when (val result = syncRepository.syncAll()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncPendingCount = 0,
                        lastSyncLabel = formatSyncLabel(result.data.serverTime),
                    )
                }
                is ApiResult.Error -> {
                    val pendingAfter = syncRepository.getPendingCount()
                    _uiState.update {
                        it.copy(isSyncing = false, syncPendingCount = pendingAfter)
                    }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            crmRepository.flushSyncQueue()
            when (val result = syncRepository.syncAll()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncPendingCount = 0,
                        lastSyncLabel = formatSyncLabel(result.data.serverTime),
                    )
                }
                is ApiResult.Error -> {
                    val pendingAfter = syncRepository.getPendingCount()
                    _uiState.update {
                        it.copy(isSyncing = false, syncPendingCount = pendingAfter)
                    }
                }
            }
            loadDashboard(refreshing = true)
        }
    }

    private fun formatSyncLabel(serverTime: String?): String? {
        if (serverTime.isNullOrBlank()) return null
        return try {
            val time = serverTime.substringAfter("T").take(5)
            "آخرین sync: $time"
        } catch (_: Exception) {
            "همگام شد"
        }
    }

    private fun loadDashboard(refreshing: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !refreshing && it.stats.contacts == 0,
                    isRefreshing = refreshing,
                    error = null,
                )
            }
            when (val result = dashboardRepository.getDashboard()) {
                is ApiResult.Success -> {
                    val data = result.data
                    val stats = data.stats
                    val license = data.license
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = null,
                            stats = DashboardStats(
                                newFilesToday = stats.newFilesToday,
                                properties = stats.properties,
                                propertiesDelta = stats.newFilesToday,
                                deals = stats.deals,
                                dealsDelta = stats.todayTasksTotal,
                                contacts = stats.contacts,
                                contactsDelta = stats.contactsNew,
                            ),
                            todayTasks = mapTodayTasks(data.todayPreview).take(5),
                            notifications = data.notifications.map { it.toHomeNotification() }.take(6),
                            recentFiles = data.latestDatasets.take(8).map { ds ->
                                RecentFileItem(
                                    id = ds.id,
                                    city = ds.city,
                                    district = ds.district,
                                    transactionType = ds.transactionType,
                                    itemCount = ds.itemCount,
                                    createdAt = ds.createdAt?.take(10),
                                )
                            },
                            notificationBadgeCount = data.notificationsUnread,
                            maxExtractItems = 100,
                            canExtract = license?.features?.lightExtract ?: state.canExtract,
                            license = license?.let {
                                LicenseState(
                                    valid = it.valid,
                                    plan = it.plan,
                                    lightExtractEnabled = it.features?.lightExtract == true,
                                    expiresAt = it.expiresAt,
                                )
                            } ?: state.license,
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
            }
        }
    }

    private fun mapTodayTasks(items: List<TodayItemDto>): List<HomeTaskItem> =
        items.mapIndexed { index, item ->
            val reminder = item.reminder
            val contact = item.contact
            val type = when (item.type?.lowercase()) {
                "call", "تماس" -> HomeTaskType.Call
                "visit", "بازدید" -> HomeTaskType.Visit
                "follow_up", "پیگیری" -> HomeTaskType.FollowUp
                else -> HomeTaskType.Reminder
            }
            HomeTaskItem(
                id = reminder?.id?.toString() ?: contact?.id?.toString() ?: index.toString(),
                contactId = contact?.id,
                reminderId = reminder?.id,
                time = formatTaskTime(reminder?.dueAt),
                title = reminder?.title ?: contact?.fullName ?: "کار امروز",
                subtitle = contact?.customerType ?: contact?.phone ?: "—",
                type = type,
            )
        }

    private fun formatTaskTime(dueAt: String?): String {
        if (dueAt.isNullOrBlank()) return "—"
        return dueAt.substringAfter("T").take(5).ifBlank { dueAt.take(5) }
    }

    private fun NotificationDto.toHomeNotification(): HomeNotificationItem {
        val notifType = when (type?.lowercase()) {
            "extract_complete", "new_dataset" -> HomeNotificationType.ExtractSuccess
            "price_drop" -> HomeNotificationType.PriceDrop
            "customer_match" -> HomeNotificationType.NewMatch
            "license_expiry" -> HomeNotificationType.License
            "overdue_followup", "today_digest" -> HomeNotificationType.FollowUp
            else -> HomeNotificationType.General
        }
        return HomeNotificationItem(
            id = id.toString(),
            title = title,
            timeAgo = formatTimeAgo(createdAt),
            type = notifType,
            deepLink = deepLink,
            body = body,
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
            "اخیراً"
        }
    }
}
