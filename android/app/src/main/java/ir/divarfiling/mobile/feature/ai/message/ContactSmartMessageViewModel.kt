package ir.divarfiling.mobile.feature.ai.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.AiDraftMessageRequest
import ir.divarfiling.mobile.core.network.AiToneOptionDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.SmartCommandRepository
import ir.divarfiling.mobile.data.repository.WorkspaceExtrasRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContactSmartMessageUiState(
    val contactId: Long = 0,
    val contactName: String = "",
    val tones: List<AiToneOptionDto> = emptyList(),
    val selectedToneId: String = "",
    val notes: String = "",
    val draftText: String = "",
    val canUse: Boolean = true,
    val blockMessage: String? = null,
    val quotaHint: String? = null,
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class ContactSmartMessageViewModel @Inject constructor(
    private val smartCommandRepository: SmartCommandRepository,
    private val extrasRepository: WorkspaceExtrasRepository,
) : ViewModel() {
    private var boundContactId: Long? = null

    private val _uiState = MutableStateFlow(ContactSmartMessageUiState())
    val uiState: StateFlow<ContactSmartMessageUiState> = _uiState.asStateFlow()

    fun bind(contactId: Long, contactName: String) {
        if (boundContactId == contactId && _uiState.value.tones.isNotEmpty()) return
        boundContactId = contactId
        _uiState.update { it.copy(contactId = contactId, contactName = contactName) }
        loadCapabilities()
    }

    fun loadCapabilities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = smartCommandRepository.getCapabilities()) {
                is ApiResult.Success -> {
                    val caps = result.data
                    val access = ContactSmartMessageLogic.accessFromCapabilities(caps)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tones = access.tones,
                            selectedToneId = access.defaultToneId,
                            canUse = access.canUse,
                            blockMessage = access.blockMessage,
                            quotaHint = access.quotaHint,
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = ContactSmartMessageLogic.humanizeError(result.message))
                }
            }
        }
    }

    fun onToneSelected(toneId: String) = _uiState.update { it.copy(selectedToneId = toneId) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }
    fun onDraftChange(value: String) = _uiState.update { it.copy(draftText = value) }
    fun clearMessages() = _uiState.update { it.copy(error = null, successMessage = null) }

    fun generate() {
        val state = _uiState.value
        if (!state.canUse) {
            _uiState.update { it.copy(error = state.blockMessage ?: "این قابلیت در دسترس نیست.") }
            return
        }
        if (state.contactId <= 0L) {
            _uiState.update { it.copy(error = "مخاطب نامعتبر است.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null, successMessage = null) }
            val toneLabel = state.tones.firstOrNull { it.id == state.selectedToneId }?.id
                ?: state.selectedToneId.ifBlank { "رسمی" }
            when (
                val result = extrasRepository.aiDraftMessage(
                    AiDraftMessageRequest(
                        contactId = state.contactId,
                        tone = toneLabel,
                        notes = state.notes.trim().ifBlank { null },
                    ),
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            draftText = result.data.text,
                            successMessage = "پیش‌نویس آماده شد",
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isGenerating = false, error = ContactSmartMessageLogic.humanizeError(result.message))
                }
            }
        }
    }
}
