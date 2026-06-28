package ir.divarfiling.mobile.feature.crm

data class CrmHubUiState(
    val userName: String = "",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val contactsCount: Int = 0,
    val newLeadsCount: Int = 0,
    val todayTasksCount: Int = 0,
    val overdueCount: Int = 0,
    val activeDealsCount: Int = 0,
    val dealsTotalValue: Long = 0,
    val propertiesCount: Int = 0,
    val openCasesCount: Int = 0,
)
