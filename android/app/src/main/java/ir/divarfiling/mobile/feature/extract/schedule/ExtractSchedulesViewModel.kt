package ir.divarfiling.mobile.feature.extract.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.core.network.ExtractionRunDto
import ir.divarfiling.mobile.core.network.ExtractionScheduleDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.ExtractionScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExtractSchedulesUiState(
    val schedules: List<ExtractionScheduleDto> = emptyList(),
    val expandedRuns: Map<Long, List<ExtractionRunDto>> = emptyMap(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class ExtractSchedulesViewModel @Inject constructor(
    private val scheduleRepository: ExtractionScheduleRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExtractSchedulesUiState())
    val uiState: StateFlow<ExtractSchedulesUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.schedules.isEmpty(), error = null) }
            when (val result = scheduleRepository.listSchedules()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(schedules = result.data, isLoading = false, isRefreshing = false)
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

    fun toggleSchedule(scheduleId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = scheduleRepository.toggleSchedule(scheduleId)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = if (result.data.isEnabled) "زمان‌بندی فعال شد" else "زمان‌بندی متوقف شد",
                        )
                    }
                    if (result.data.isEnabled) {
                        ScheduleWorkManager.registerPeriodicPolling(context)
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun deleteSchedule(scheduleId: Long) {
        viewModelScope.launch {
            when (val result = scheduleRepository.deleteSchedule(scheduleId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(successMessage = "زمان‌بندی حذف شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun runNow(scheduleId: Long) {
        ScheduleWorkManager.enqueueDueRuns(context, scheduleId)
        _uiState.update { it.copy(successMessage = "اجرای استخراج در پس‌زمینه شروع شد") }
    }

    fun loadRuns(scheduleId: Long) {
        if (_uiState.value.expandedRuns.containsKey(scheduleId)) {
            _uiState.update { state ->
                state.copy(expandedRuns = state.expandedRuns - scheduleId)
            }
            return
        }
        viewModelScope.launch {
            when (val result = scheduleRepository.listRuns(scheduleId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(expandedRuns = it.expandedRuns + (scheduleId to result.data))
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(successMessage = null, error = null) }
}
