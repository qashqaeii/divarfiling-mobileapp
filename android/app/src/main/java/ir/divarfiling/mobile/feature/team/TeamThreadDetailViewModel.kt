package ir.divarfiling.mobile.feature.team

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.TeamChatMessageDto
import ir.divarfiling.mobile.core.network.TeamThreadDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamThreadUiState(
    val thread: TeamThreadDto? = null,
    val messages: List<TeamChatMessageDto> = emptyList(),
    val replyBody: String = "",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class TeamThreadDetailViewModel @Inject constructor(
    private val repository: TeamRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val threadId: Long = savedStateHandle.get<Long>("threadId") ?: 0L
    private val _uiState = MutableStateFlow(TeamThreadUiState())
    val uiState: StateFlow<TeamThreadUiState> = _uiState.asStateFlow()

    init { refresh(initial = true) }

    fun refresh(initial: Boolean = false) {
        if (threadId <= 0L) {
            _uiState.update { it.copy(isLoading = false, error = "شناسه مکالمه نامعتبر است") }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = initial && it.thread == null,
                    isRefreshing = !initial || it.thread != null,
                    error = null,
                )
            }
            when (val result = repository.getThread(threadId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        thread = result.data.thread,
                        messages = result.data.messages,
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

    fun onReplyChange(value: String) = _uiState.update { it.copy(replyBody = value) }

    fun sendReply() {
        val body = _uiState.value.replyBody.trim()
        if (body.isBlank()) {
            _uiState.update { it.copy(error = "متن پاسخ خالی است") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.reply(threadId, body)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        replyBody = "",
                        thread = result.data.thread ?: it.thread,
                        messages = result.data.messages.ifEmpty { it.messages },
                        successMessage = "پاسخ ارسال شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun toggleStar() {
        val starred = !(_uiState.value.thread?.isStarred ?: false)
        viewModelScope.launch {
            when (repository.patchThread(threadId, isStarred = starred)) {
                is ApiResult.Success -> refresh()
                is ApiResult.Error -> Unit
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }
}
