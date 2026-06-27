package ir.divarfiling.mobile.feature.crm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.TodayData
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
)

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val crmRepository: CrmRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init { load(reset = true) }

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
                    it.copy(isLoading = false, isRefreshing = false, isLoadingMore = false, error = result.message)
                }
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return
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

    fun toggleQuickLead(show: Boolean) = _uiState.update { it.copy(showQuickLead = show) }
    fun onLeadNameChange(v: String) = _uiState.update { it.copy(leadName = v) }
    fun onLeadPhoneChange(v: String) = _uiState.update { it.copy(leadPhone = v) }

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
                    _uiState.update {
                        it.copy(showQuickLead = false, leadName = "", leadPhone = "", isSubmitting = false)
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

    fun completeTask(contactId: Long?, reminderId: Long?, note: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true) }
            when (crmRepository.completeTodayTask(contactId, reminderId, note)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isActionRunning = false, successMessage = "انجام شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isActionRunning = false, error = it.error)
                }
            }
        }
    }

    fun postponeTask(contactId: Long?, reminderId: Long?, days: Int = 1) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true) }
            when (crmRepository.postponeTodayTask(contactId, reminderId, days)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isActionRunning = false, successMessage = "تعویق شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isActionRunning = false, error = it.error)
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
