package ir.divarfiling.mobile.feature.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.SupportTicketCreateRequest
import ir.divarfiling.mobile.core.network.SupportTicketDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.WorkspaceExtrasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SupportTicketsUiState(
    val tickets: List<SupportTicketDto> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val showCreateDialog: Boolean = false,
    val subject: String = "",
    val body: String = "",
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class SupportTicketsViewModel @Inject constructor(
    private val repository: WorkspaceExtrasRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SupportTicketsUiState())
    val uiState: StateFlow<SupportTicketsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.tickets.isEmpty() && !it.isRefreshing, error = null) }
            when (val result = repository.getSupportTickets()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(tickets = result.data, isLoading = false, isRefreshing = false)
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

    fun toggleCreateDialog(show: Boolean) = _uiState.update {
        it.copy(showCreateDialog = show, subject = if (!show) "" else it.subject, body = if (!show) "" else it.body)
    }

    fun onSubjectChange(value: String) = _uiState.update { it.copy(subject = value) }
    fun onBodyChange(value: String) = _uiState.update { it.copy(body = value) }

    fun createTicket() {
        val subject = _uiState.value.subject.trim()
        val body = _uiState.value.body.trim()
        if (subject.isBlank() || body.isBlank()) {
            _uiState.update { it.copy(error = "موضوع و متن الزامی است") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.createSupportTicket(SupportTicketCreateRequest(subject, body))) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        showCreateDialog = false,
                        subject = "",
                        body = "",
                        successMessage = "تیکت ثبت شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
            load()
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }
}
