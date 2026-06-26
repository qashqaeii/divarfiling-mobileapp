package ir.divarfiling.mobile.feature.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ir.divarfiling.mobile.core.license.LicenseState

data class HomeUiState(
    val userName: String = "",
    val agencyName: String? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val stats: DashboardStats = DashboardStats(),
    val todayTasks: List<HomeTaskItem> = emptyList(),
    val notifications: List<HomeNotificationItem> = emptyList(),
    val recentFiles: List<RecentFileItem> = emptyList(),
    val license: LicenseState = LicenseState(),
    val notificationBadgeCount: Int = 0,
    val maxExtractItems: Int = 100,
    val canExtract: Boolean = false,
)

data class DashboardStats(
    val newFilesToday: Int = 0,
    val properties: Int = 0,
    val propertiesDelta: Int = 0,
    val deals: Int = 0,
    val dealsDelta: Int = 0,
    val contacts: Int = 0,
    val contactsDelta: Int = 0,
)

enum class HomeTaskType {
    Call,
    Visit,
    FollowUp,
    Reminder,
}

data class HomeTaskItem(
    val id: String,
    val time: String,
    val title: String,
    val subtitle: String,
    val type: HomeTaskType,
)

enum class HomeNotificationType {
    ExtractSuccess,
    NewMatch,
    PriceDrop,
    License,
    FollowUp,
    General,
}

data class HomeNotificationItem(
    val id: String,
    val title: String,
    val timeAgo: String,
    val type: HomeNotificationType,
)

data class RecentFileItem(
    val id: String,
    val city: String?,
    val district: String?,
    val transactionType: String?,
    val itemCount: Int,
    val createdAt: String?,
    val thumbnailUrl: String? = null,
)

data class StatCardData(
    val value: Int,
    val label: String,
    val delta: Int,
    val deltaLabel: String,
    val icon: ImageVector,
    val tint: Color,
    val background: Color,
)
