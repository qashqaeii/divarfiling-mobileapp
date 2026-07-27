package ir.divarfiling.mobile.feature.crm.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.TodayData
import ir.divarfiling.mobile.core.network.TodayItemDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalendarDayGroup(
    val dateLabel: String,
    val items: List<TodayItemDto>,
)

data class CrmCalendarUiState(
    val groups: List<CalendarDayGroup> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CrmCalendarViewModel @Inject constructor(
    private val crmRepository: CrmRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CrmCalendarUiState())
    val uiState: StateFlow<CrmCalendarUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = it.groups.isEmpty() && !it.isRefreshing, error = null)
            }
            when (val result = crmRepository.getToday()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        groups = groupByDate(result.data),
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

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        load()
    }

    private fun groupByDate(data: TodayData): List<CalendarDayGroup> {
        val all = (data.overdue + data.today + data.done)
            .filter { it.reminder != null || it.contact != null }
        val grouped = linkedMapOf<String, MutableList<TodayItemDto>>()
        all.forEach { item ->
            val label = item.reminder?.dueAt?.substringBefore("T")?.ifBlank { null }
                ?: data.date
                ?: "بدون تاریخ"
            grouped.getOrPut(label) { mutableListOf() }.add(item)
        }
        return grouped.map { (date, items) -> CalendarDayGroup(date, items) }
    }
}
