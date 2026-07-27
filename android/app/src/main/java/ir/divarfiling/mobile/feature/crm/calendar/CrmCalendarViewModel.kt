package ir.divarfiling.mobile.feature.crm.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.network.ReminderCreateRequest
import ir.divarfiling.mobile.core.network.ReminderDto
import ir.divarfiling.mobile.core.network.ReminderPatchRequest
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class CalendarMode { Day, Week, Month }

data class CrmCalendarUiState(
    val mode: CalendarMode = CalendarMode.Month,
    val selectedDate: LocalDate = LocalDate.now(),
    val visibleMonth: LocalDate = LocalDate.now().withDayOfMonth(1),
    val reminders: List<ReminderDto> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val showCreateSheet: Boolean = false,
    val draftTitle: String = "",
    val draftNote: String = "",
    val draftDueMillis: Long = System.currentTimeMillis() + 3_600_000L,
    val draftRecurrence: String = "",
    val error: String? = null,
    val successMessage: String? = null,
) {
    val selectedDayReminders: List<ReminderDto>
        get() = reminders.filter { it.dueAt?.let { due -> dueDate(due) == selectedDate } == true }
            .sortedBy { it.dueAt }

    val monthDayCounts: Map<LocalDate, Int>
        get() = reminders.mapNotNull { r -> r.dueAt?.let { dueDate(it) } }
            .groupingBy { it }
            .eachCount()
}

@HiltViewModel
class CrmCalendarViewModel @Inject constructor(
    private val crmRepository: CrmRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CrmCalendarUiState())
    val uiState: StateFlow<CrmCalendarUiState> = _uiState.asStateFlow()
    private val zone = ZoneId.systemDefault()
    private val iso = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    init { loadForVisibleRange() }

    fun setMode(mode: CalendarMode) {
        if (_uiState.value.mode == mode) return
        _uiState.update { it.copy(mode = mode) }
        loadForVisibleRange()
    }

    fun selectDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                visibleMonth = date.withDayOfMonth(1),
                draftDueMillis = date.atTime(10, 0).atZone(zone).toInstant().toEpochMilli(),
            )
        }
    }

    fun shiftMonth(delta: Long) {
        val next = _uiState.value.visibleMonth.plusMonths(delta)
        _uiState.update { it.copy(visibleMonth = next) }
        loadForVisibleRange()
    }

    fun shiftWeek(delta: Long) {
        val next = _uiState.value.selectedDate.plusWeeks(delta)
        selectDate(next)
        loadForVisibleRange()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadForVisibleRange()
    }

    fun toggleCreateSheet(show: Boolean) = _uiState.update {
        it.copy(
            showCreateSheet = show,
            draftTitle = if (!show) "" else it.draftTitle,
            draftNote = if (!show) "" else it.draftNote,
            draftRecurrence = if (!show) "" else it.draftRecurrence,
            draftDueMillis = if (show) {
                it.selectedDate.atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
            } else {
                it.draftDueMillis
            },
        )
    }

    fun onDraftTitle(v: String) = _uiState.update { it.copy(draftTitle = v) }
    fun onDraftNote(v: String) = _uiState.update { it.copy(draftNote = v) }
    fun onDraftDue(millis: Long) = _uiState.update { it.copy(draftDueMillis = millis) }
    fun onDraftRecurrence(v: String) = _uiState.update { it.copy(draftRecurrence = v) }

    fun createReminder() {
        val state = _uiState.value
        val title = state.draftTitle.trim()
        if (title.isBlank()) {
            _uiState.update { it.copy(error = "عنوان الزامی است") }
            return
        }
        val dueAt = Instant.ofEpochMilli(state.draftDueMillis).atZone(zone).format(iso)
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (
                val result = crmRepository.createStandaloneReminder(
                    ReminderCreateRequest(
                        title = title,
                        dueAt = dueAt,
                        note = state.draftNote,
                        recurrence = state.draftRecurrence,
                    ),
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showCreateSheet = false,
                            draftTitle = "",
                            draftNote = "",
                            draftRecurrence = "",
                            successMessage = "یادآور ثبت شد",
                        )
                    }
                    loadForVisibleRange()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun completeReminder(id: Long) {
        viewModelScope.launch {
            when (val result = crmRepository.patchReminder(id, ReminderPatchRequest(action = "complete"))) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(successMessage = "انجام شد") }
                    loadForVisibleRange()
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun snoozeReminder(id: Long, hours: Int = 1) {
        viewModelScope.launch {
            when (
                val result = crmRepository.patchReminder(
                    id,
                    ReminderPatchRequest(action = "snooze", hours = hours),
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(successMessage = "یادآور به تعویق افتاد") }
                    loadForVisibleRange()
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }

    private fun loadForVisibleRange() {
        viewModelScope.launch {
            val state = _uiState.value
            val (from, to) = when (state.mode) {
                CalendarMode.Month -> {
                    val start = state.visibleMonth.withDayOfMonth(1)
                    start to start.plusMonths(1).minusDays(1)
                }
                CalendarMode.Week -> {
                    val start = state.selectedDate.minusDays(
                        (state.selectedDate.dayOfWeek.value % 7).toLong(),
                    )
                    start to start.plusDays(6)
                }
                CalendarMode.Day -> state.selectedDate to state.selectedDate
            }
            _uiState.update {
                it.copy(isLoading = it.reminders.isEmpty() && !it.isRefreshing, error = null)
            }
            val dueFrom = from.atStartOfDay(zone).format(iso)
            val dueTo = to.atTime(LocalTime.MAX).atZone(zone).format(iso)
            when (val result = crmRepository.getReminders(dueFrom = dueFrom, dueTo = dueTo, done = false)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        reminders = result.data,
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
}

internal fun dueDate(isoDue: String): LocalDate? {
    return runCatching {
        java.time.OffsetDateTime.parse(isoDue).toLocalDate()
    }.recoverCatching {
        Instant.parse(isoDue).atZone(ZoneId.systemDefault()).toLocalDate()
    }.recoverCatching {
        LocalDate.parse(isoDue.take(10))
    }.getOrNull()
}

fun LocalDate.toJalaliLabel(): String {
    val (jy, jm, jd) = DateUtils.gregorianToJalali(year, monthValue, dayOfMonth)
    return DateUtils.toPersianDigits(DateUtils.formatJalali(jy, jm, jd))
}

fun LocalDate.toJalaliMonthTitle(): String {
    val (jy, jm, _) = DateUtils.gregorianToJalali(year, monthValue, 1)
    return "${DateUtils.jalaliMonthName(jm)} ${DateUtils.toPersianDigits(jy.toString())}"
}
