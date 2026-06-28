package ir.divarfiling.mobile.feature.crm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.TodayData
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.network.ContactUpdateRequest
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import ir.divarfiling.mobile.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class ContactsUiState(
    val contacts: List<ContactDto> = emptyList(),
    val query: String = "",
    val statusFilter: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val showQuickLead: Boolean = false,
    val leadName: String = "",
    val leadPhone: String = "",
    val leadCustomerType: String = "سرنخ",
    val userName: String = "",
    val notificationBadgeCount: Int = 0,
)

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val crmRepository: CrmRepository,
    private val sessionStore: SessionStore,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionStore.currentUser.collect { user ->
                _uiState.update {
                    it.copy(userName = user?.fullName?.substringBefore(" ") ?: "کاربر")
                }
            }
        }
        viewModelScope.launch {
            when (val result = dashboardRepository.getDashboard()) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(notificationBadgeCount = result.data.notificationsUnread) }
                is ApiResult.Error -> Unit
            }
        }
        load(reset = true)
    }

    fun load(reset: Boolean = false) {
        viewModelScope.launch {
            val page = if (reset) 1 else _uiState.value.page
            _uiState.update {
                it.copy(
                    isLoading = reset && it.contacts.isEmpty(),
                    isLoadingMore = !reset,
                    error = null,
                )
            }
            when (val result = crmRepository.getContacts(_uiState.value.query, page = page)) {
                is ApiResult.Success -> {
                    val merged = if (reset) result.data.items else _uiState.value.contacts + result.data.items
                    _uiState.update {
                        it.copy(
                            contacts = merged,
                            page = page,
                            hasMore = result.data.hasMore,
                            isLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        page = if (!reset && it.page > 1) it.page - 1 else it.page,
                        error = result.message,
                    )
                }
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore || _uiState.value.isLoading) return
        _uiState.update { it.copy(page = it.page + 1) }
        load(reset = false)
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, page = 1) }
        load(reset = true)
    }

    fun onQueryChange(q: String) = _uiState.update { it.copy(query = q) }
    fun onStatusFilterChange(status: String?) = _uiState.update { it.copy(statusFilter = status) }
    fun search() {
        _uiState.update { it.copy(page = 1) }
        load(reset = true)
    }

    fun toggleQuickLead(show: Boolean) = _uiState.update {
        it.copy(
            showQuickLead = show,
            leadName = if (show) it.leadName else "",
            leadPhone = if (show) it.leadPhone else "",
            leadCustomerType = if (show) it.leadCustomerType else "سرنخ",
        )
    }
    fun onLeadNameChange(v: String) = _uiState.update { it.copy(leadName = v) }
    fun onLeadPhoneChange(v: String) = _uiState.update { it.copy(leadPhone = v) }
    fun onLeadCustomerTypeChange(v: String) = _uiState.update { it.copy(leadCustomerType = v) }

    fun submitQuickLead() {
        val state = _uiState.value
        if (state.leadName.isBlank() || state.leadPhone.isBlank()) {
            _uiState.update { it.copy(error = "نام و تلفن الزامی است") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = crmRepository.quickLead(state.leadName, state.leadPhone)) {
                is ApiResult.Success -> {
                    val created = result.data
                    if (state.leadCustomerType.isNotBlank() && state.leadCustomerType != "سرنخ") {
                        crmRepository.updateContact(
                            created.id,
                            ContactUpdateRequest(customerType = state.leadCustomerType),
                        )
                    }
                    _uiState.update {
                        it.copy(
                            showQuickLead = false,
                            leadName = "",
                            leadPhone = "",
                            leadCustomerType = "سرنخ",
                            isSubmitting = false,
                        )
                    }
                    refresh()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }
}

data class TodayUiState(
    val data: TodayData? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isActionRunning: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showFilterSheet: Boolean = false,
    val showNewTaskSheet: Boolean = false,
    val contactPicker: List<ContactDto> = emptyList(),
    val newTaskContactId: Long? = null,
    val newTaskTitle: String = "",
    val newTaskDueMillis: Long = System.currentTimeMillis() + 3_600_000L,
    val isSubmittingTask: Boolean = false,
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val crmRepository: CrmRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = it.data == null && !it.isRefreshing, error = null)
            }
            when (val result = crmRepository.getToday()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(data = result.data, isLoading = false, isRefreshing = false)
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

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }

    fun toggleFilterSheet(show: Boolean) = _uiState.update { it.copy(showFilterSheet = show) }

    fun toggleNewTaskSheet(show: Boolean) {
        _uiState.update {
            it.copy(
                showNewTaskSheet = show,
                newTaskTitle = if (show) it.newTaskTitle else "",
                newTaskContactId = if (show) it.newTaskContactId else null,
            )
        }
        if (show) loadContactsForPicker()
    }

    fun onNewTaskContactSelect(id: Long) = _uiState.update { it.copy(newTaskContactId = id) }
    fun onNewTaskTitleChange(v: String) = _uiState.update { it.copy(newTaskTitle = v) }
    fun onNewTaskDueChange(millis: Long) = _uiState.update { it.copy(newTaskDueMillis = millis) }

    private fun loadContactsForPicker() {
        viewModelScope.launch {
            when (val result = crmRepository.getContacts(page = 1, pageSize = 100)) {
                is ApiResult.Success -> {
                    val items = result.data.items
                    _uiState.update {
                        it.copy(
                            contactPicker = items,
                            newTaskContactId = it.newTaskContactId ?: items.firstOrNull()?.id,
                        )
                    }
                }
                is ApiResult.Error -> Unit
            }
        }
    }

    fun submitNewTask() {
        val state = _uiState.value
        val contactId = state.newTaskContactId
        val title = state.newTaskTitle.trim()
        if (contactId == null || title.isBlank()) {
            _uiState.update { it.copy(error = "مخاطب و عنوان الزامی است") }
            return
        }
        val dueAt = Instant.ofEpochMilli(state.newTaskDueMillis).toString()
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingTask = true, error = null) }
            when (val result = crmRepository.createReminder(contactId, title, dueAt)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingTask = false,
                            showNewTaskSheet = false,
                            newTaskTitle = "",
                            successMessage = "کار جدید ثبت شد",
                        )
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingTask = false, error = result.message)
                }
            }
        }
    }

    fun completeTask(contactId: Long?, reminderId: Long?, note: String = "") {
        if (contactId == null && reminderId == null) {
            _uiState.update { it.copy(error = "این کار قابل تکمیل نیست") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, error = null) }
            when (val result = crmRepository.completeTodayTask(contactId, reminderId, note)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isActionRunning = false, successMessage = "انجام شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isActionRunning = false, error = result.message)
                }
            }
        }
    }

    fun postponeTask(contactId: Long?, reminderId: Long?, days: Int = 1) {
        if (contactId == null && reminderId == null) {
            _uiState.update { it.copy(error = "این کار قابل تعویق نیست") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, error = null) }
            when (val result = crmRepository.postponeTodayTask(contactId, reminderId, days)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isActionRunning = false, successMessage = "به ${days} روز بعد موکول شد")
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isActionRunning = false, error = result.message)
                }
            }
        }
    }

    fun logCallActivity(contactId: Long) {
        viewModelScope.launch {
            crmRepository.createActivity(contactId, "call", "تماس از صفحه امروز")
        }
    }
}
