package ir.divarfiling.mobile.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.license.ExtractLightLimits
import ir.divarfiling.mobile.core.license.LicenseState
import ir.divarfiling.mobile.core.network.DatasetDto
import ir.divarfiling.mobile.core.network.TodayItemDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import ir.divarfiling.mobile.data.repository.FilingRepository
import kotlinx.coroutines.async
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
    private val crmRepository: CrmRepository,
    private val filingRepository: FilingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                sessionStore.currentUser,
                sessionStore.licenseState,
            ) { user, license -> user to license }
                .collect { (user, license) ->
                    _uiState.update {
                        it.copy(
                            userName = user?.fullName?.substringBefore(" ") ?: "کاربر",
                            agencyName = user?.agencyName,
                            license = license,
                            canExtract = license.canUseLightExtract,
                            maxExtractItems = ExtractLightLimits.MAX_ITEMS,
                        )
                    }
                }
        }
        loadDashboard(refreshing = false)
    }

    fun refresh() {
        loadDashboard(refreshing = true)
    }

    private fun loadDashboard(refreshing: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !refreshing && it.stats.contacts == 0 && it.todayTasks.isEmpty(),
                    isRefreshing = refreshing,
                    error = null,
                )
            }

            val contactsDeferred = async { crmRepository.getContacts() }
            val todayDeferred = async { crmRepository.getToday() }
            val datasetsDeferred = async { filingRepository.getDatasets() }

            val contactsResult = contactsDeferred.await()
            val todayResult = todayDeferred.await()
            val datasetsResult = datasetsDeferred.await()

            val contacts = (contactsResult as? ApiResult.Success)?.data.orEmpty()
            val todayData = (todayResult as? ApiResult.Success)?.data
            val datasets = (datasetsResult as? ApiResult.Success)?.data.orEmpty()

            val todayIso = LocalDate.now().toString()
            val newFilesToday = datasets.count { it.createdAt?.startsWith(todayIso) == true }
            val totalListings = datasets.sumOf { it.itemCount }
            val todayTasks = mapTodayTasks(
                (todayData?.today.orEmpty() + todayData?.overdue.orEmpty())
                    .distinctBy { item ->
                        item.reminder?.id ?: item.contact?.id ?: item.type
                    },
            )
            val notifications = buildNotifications(
                license = _uiState.value.license,
                datasets = datasets,
                overdueCount = todayData?.overdue?.size ?: 0,
            )
            val recentFiles = datasets.take(8).map { it.toRecentFile() }

            val errors = listOfNotNull(
                (contactsResult as? ApiResult.Error)?.message,
                (todayResult as? ApiResult.Error)?.message,
                (datasetsResult as? ApiResult.Error)?.message,
            ).distinct()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = errors.firstOrNull(),
                    stats = DashboardStats(
                        newFilesToday = newFilesToday,
                        properties = totalListings,
                        propertiesDelta = newFilesToday,
                        deals = todayData?.stats?.total ?: todayTasks.size,
                        dealsDelta = todayData?.today?.size ?: 0,
                        contacts = contacts.size,
                        contactsDelta = contacts.count { it.updatedAt?.startsWith(todayIso) == true },
                    ),
                    todayTasks = todayTasks.take(5),
                    notifications = notifications.take(4),
                    recentFiles = recentFiles,
                    notificationBadgeCount = notifications.size,
                )
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

    private fun buildNotifications(
        license: LicenseState,
        datasets: List<DatasetDto>,
        overdueCount: Int,
    ): List<HomeNotificationItem> {
        val list = mutableListOf<HomeNotificationItem>()
        if (!license.valid) {
            list += HomeNotificationItem(
                id = "license",
                title = "لایسنس فعال نیست — برای استفاده کامل تمدید کنید",
                timeAgo = "الان",
                type = HomeNotificationType.License,
            )
        } else if (!license.lightExtractEnabled) {
            list += HomeNotificationItem(
                id = "extract-disabled",
                title = "استخراج سبک در پلن شما فعال نیست",
                timeAgo = "الان",
                type = HomeNotificationType.License,
            )
        }
        datasets.firstOrNull()?.let { latest ->
            list += HomeNotificationItem(
                id = "extract-${latest.id}",
                title = "فایل «${latest.name}» با ${latest.itemCount} آگهی آماده است",
                timeAgo = formatTimeAgo(latest.createdAt),
                type = HomeNotificationType.ExtractSuccess,
            )
        }
        if (overdueCount > 0) {
            list += HomeNotificationItem(
                id = "overdue",
                title = "$overdueCount پیگیری عقب‌افتاده دارید",
                timeAgo = "امروز",
                type = HomeNotificationType.FollowUp,
            )
        }
        if (datasets.size >= 2) {
            val recent = datasets[0]
            val previous = datasets[1]
            if (recent.itemCount < previous.itemCount) {
                list += HomeNotificationItem(
                    id = "price-hint",
                    title = "تغییر در حجم آگهی‌های ${recent.district ?: recent.city ?: "منطقه"}",
                    timeAgo = formatTimeAgo(recent.createdAt),
                    type = HomeNotificationType.PriceDrop,
                )
            }
        }
        return list
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

    private fun DatasetDto.toRecentFile() = RecentFileItem(
        id = id,
        city = city,
        district = district,
        transactionType = transactionType,
        itemCount = itemCount,
        createdAt = createdAt?.take(10),
    )
}
