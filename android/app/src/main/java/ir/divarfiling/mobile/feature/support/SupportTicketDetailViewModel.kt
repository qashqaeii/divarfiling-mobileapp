package ir.divarfiling.mobile.feature.support

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val selectedAttachmentName: String = "",
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
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val ticketId: Long = checkNotNull(savedStateHandle["ticketId"])
    private var selectedAttachmentFile: File? = null
    private var selectedAttachmentMime: String = "application/octet-stream"

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

    fun selectAttachment(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val resolver = context.contentResolver
                val mime = resolver.getType(uri) ?: "application/octet-stream"
                val fileName = resolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
                } ?: "attachment"
                val tempFile = File(context.cacheDir, "support-${ticketId}-${System.currentTimeMillis()}-$fileName")
                resolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                } ?: error("خواندن فایل ناموفق بود")
                selectedAttachmentFile?.delete()
                selectedAttachmentFile = tempFile
                selectedAttachmentMime = mime
                _uiState.update { it.copy(selectedAttachmentName = fileName, error = null) }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message ?: "انتخاب فایل ناموفق بود") }
            }
        }
    }

    fun clearAttachment() {
        selectedAttachmentFile?.delete()
        selectedAttachmentFile = null
        selectedAttachmentMime = "application/octet-stream"
        _uiState.update { it.copy(selectedAttachmentName = "") }
    }

    fun sendReply() {
        val body = _uiState.value.replyText.trim()
        if (body.isBlank()) {
            _uiState.update { it.copy(error = "متن پاسخ الزامی است") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.replySupportTicket(ticketId, body, selectedAttachmentFile, selectedAttachmentMime)) {
                is ApiResult.Success -> {
                    selectedAttachmentFile?.delete()
                    selectedAttachmentFile = null
                    selectedAttachmentMime = "application/octet-stream"
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            replyText = "",
                            selectedAttachmentName = "",
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
