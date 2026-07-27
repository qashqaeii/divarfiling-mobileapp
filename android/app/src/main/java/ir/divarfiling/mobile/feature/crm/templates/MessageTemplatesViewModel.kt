package ir.divarfiling.mobile.feature.crm.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.MessageTemplateDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.WorkspaceExtrasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessageTemplatesUiState(
    val templates: List<MessageTemplateDto> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val copiedMessage: String? = null,
)

@HiltViewModel
class MessageTemplatesViewModel @Inject constructor(
    private val repository: WorkspaceExtrasRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MessageTemplatesUiState())
    val uiState: StateFlow<MessageTemplatesUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = it.templates.isEmpty() && !it.isRefreshing, error = null)
            }
            when (val result = repository.getMessageTemplates()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(templates = result.data, isLoading = false, isRefreshing = false)
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

    fun onCopied(message: String) = _uiState.update { it.copy(copiedMessage = message) }

    fun clearCopiedMessage() = _uiState.update { it.copy(copiedMessage = null) }
}
