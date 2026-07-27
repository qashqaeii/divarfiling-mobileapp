package ir.divarfiling.mobile.feature.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.AiDraftMessageRequest
import ir.divarfiling.mobile.core.network.AiQuotaData
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.WorkspaceExtrasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiAssistantUiState(
    val quota: AiQuotaData? = null,
    val contactId: String = "",
    val listingToken: String = "",
    val intent: String = "followup",
    val notes: String = "",
    val draftText: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val repository: WorkspaceExtrasRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    init { loadQuota() }

    fun loadQuota() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.quota == null && !it.isRefreshing, error = null) }
            when (val result = repository.getAiQuota()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(quota = result.data, isLoading = false, isRefreshing = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadQuota()
    }

    fun onContactIdChange(value: String) = _uiState.update { it.copy(contactId = value) }
    fun onListingTokenChange(value: String) = _uiState.update { it.copy(listingToken = value) }
    fun onIntentChange(value: String) = _uiState.update { it.copy(intent = value) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }

    fun generateDraft() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null, successMessage = null) }
            val state = _uiState.value
            when (
                val result = repository.aiDraftMessage(
                    AiDraftMessageRequest(
                        contactId = state.contactId.trim().toLongOrNull(),
                        listingToken = state.listingToken.trim().ifBlank { null },
                        intent = state.intent.ifBlank { "followup" },
                        notes = state.notes.trim().ifBlank { null },
                    ),
                )
            ) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        draftText = result.data.text,
                        successMessage = "پیش‌نویس آماده شد",
                        quota = result.data.quotaRemaining?.let { remaining ->
                            it.quota?.copy(remaining = remaining) ?: AiQuotaData(remaining = remaining)
                        } ?: it.quota,
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }
}
