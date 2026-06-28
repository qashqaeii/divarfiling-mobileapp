package ir.divarfiling.mobile.feature.crm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.DashboardRepository
import ir.divarfiling.mobile.data.repository.DealsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CrmHubViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val dashboardRepository: DashboardRepository,
    private val dealsRepository: DealsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrmHubUiState())
    val uiState: StateFlow<CrmHubUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionStore.currentUser.collect { user ->
                _uiState.update {
                    it.copy(userName = user?.fullName?.substringBefore(" ") ?: "کاربر")
                }
            }
        }
        viewModelScope.launch { loadSummary() }
    }

    fun refresh() {
        viewModelScope.launch { loadSummary(refreshing = true) }
    }

    private suspend fun loadSummary(refreshing: Boolean = false) {
        _uiState.update {
            it.copy(
                isLoading = !refreshing && it.contactsCount == 0,
                isRefreshing = refreshing,
            )
        }

        var dealsTotal = 0L
        when (val dealsResult = dealsRepository.getDeals(page = 1)) {
            is ApiResult.Success -> {
                dealsTotal = dealsResult.data.items.sumOf { deal -> deal.amount ?: 0L }
            }
            is ApiResult.Error -> Unit
        }

        when (val dashboardResult = dashboardRepository.getDashboard()) {
            is ApiResult.Success -> {
                val stats = dashboardResult.data.stats
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        contactsCount = stats.contacts,
                        newLeadsCount = stats.contactsNew,
                        todayTasksCount = stats.todayTasksTotal,
                        overdueCount = stats.overdueCount,
                        activeDealsCount = stats.deals,
                        dealsTotalValue = dealsTotal,
                        propertiesCount = stats.properties,
                        openCasesCount = stats.contactsInProgress.coerceAtLeast(stats.overdueFollowups),
                    )
                }
            }
            is ApiResult.Error -> _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    dealsTotalValue = dealsTotal.takeIf { value -> value > 0 } ?: it.dealsTotalValue,
                )
            }
        }
    }
}
