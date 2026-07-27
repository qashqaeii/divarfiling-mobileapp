package ir.divarfiling.mobile.feature.support

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.SupportTicketDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.WorkspaceExtrasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class SupportTicketDetailUiState(
    val ticket: SupportTicketDto? = null,
    val replyText: String = "",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class SupportTicketDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: WorkspaceExtrasRepository,
) : ViewModel() {
    private val ticketId: Long = checkNotNull(savedStateHandle["ticketId"])

    private val _uiState = MutableStateFlow(SupportTicketDetailUiState())
    val uiState: StateFlow<SupportTicketDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = it.ticket == null && !it.isRefreshing, error = null)
            }
            when (val result = repository.getSupportTicket(ticketId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(ticket = result.data, isLoading = false, isRefreshing = false)
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

    fun onReplyChange(value: String) = _uiState.update { it.copy(replyText = value) }

    fun sendReply(attachment: File? = null, mime: String = "application/octet-stream") {
        val body = _uiState.value.replyText.trim()
        if (body.isBlank()) {
            _uiState.update { it.copy(error = "متن پاسخ الزامی است") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.replySupportTicket(ticketId, body, attachment, mime)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            replyText = "",
                            successMessage = "پاسخ ارسال شد",
                        )
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun closeTicket() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.closeSupportTicket(ticketId)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, successMessage = "تیکت بسته شد")
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun reopenTicket() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.reopenSupportTicket(ticketId)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, successMessage = "تیکت دوباره باز شد")
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }
}
