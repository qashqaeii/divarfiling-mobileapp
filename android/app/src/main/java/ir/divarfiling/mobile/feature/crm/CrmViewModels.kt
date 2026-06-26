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
    val isLoading: Boolean = false,
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

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = crmRepository.getContacts(_uiState.value.query)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(contacts = result.data, isLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun onQueryChange(q: String) {
        _uiState.update { it.copy(query = q) }
    }

    fun search() = load()

    fun toggleQuickLead(show: Boolean) {
        _uiState.update { it.copy(showQuickLead = show) }
    }

    fun onLeadNameChange(v: String) = _uiState.update { it.copy(leadName = v) }
    fun onLeadPhoneChange(v: String) = _uiState.update { it.copy(leadPhone = v) }

    fun submitQuickLead() {
        val state = _uiState.value
        viewModelScope.launch {
            when (val result = crmRepository.quickLead(state.leadName, state.leadPhone)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(showQuickLead = false, leadName = "", leadPhone = "")
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }
}

data class TodayUiState(
    val data: TodayData? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
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
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = crmRepository.getToday()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(data = result.data, isLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }
}
