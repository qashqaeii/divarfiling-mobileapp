package ir.divarfiling.mobile.feature.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.TeamMemberDto
import ir.divarfiling.mobile.core.network.TeamSendMessageRequest
import ir.divarfiling.mobile.core.network.TeamThreadDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TeamMessageFolder(val apiValue: String, val label: String) {
    Inbox("inbox", "صندوق"),
    Starred("starred", "ستاره‌دار"),
    Archived("archived", "بایگانی"),
}

data class TeamMessagesUiState(
    val folder: TeamMessageFolder = TeamMessageFolder.Inbox,
    val threads: List<TeamThreadDto> = emptyList(),
    val members: List<TeamMemberDto> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val showCompose: Boolean = false,
    val composeBody: String = "",
    val selectedMemberId: Long? = null,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class TeamMessagesViewModel @Inject constructor(
    private val repository: TeamRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamMessagesUiState())
    val uiState: StateFlow<TeamMessagesUiState> = _uiState.asStateFlow()

    init { refresh(initial = true) }

    fun refresh(initial: Boolean = false) {
        val folder = _uiState.value.folder
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = initial && it.threads.isEmpty(),
                    isRefreshing = !initial || it.threads.isNotEmpty(),
                    error = null,
                )
            }
            when (val result = repository.getMessages(folder.apiValue)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        threads = result.data.threads,
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

    fun setFolder(folder: TeamMessageFolder) {
        if (folder == _uiState.value.folder) return
        _uiState.update { it.copy(folder = folder) }
        refresh(initial = true)
    }

    fun toggleCompose(show: Boolean) {
        if (show) {
            viewModelScope.launch {
                when (val members = repository.getMembers(excludeSelf = true)) {
                    is ApiResult.Success -> _uiState.update {
                        it.copy(
                            showCompose = true,
                            members = members.data.members,
                            selectedMemberId = members.data.members.firstOrNull()?.id,
                            composeBody = "",
                        )
                    }
                    is ApiResult.Error -> _uiState.update { it.copy(error = members.message) }
                }
            }
        } else {
            _uiState.update { it.copy(showCompose = false) }
        }
    }

    fun onComposeBodyChange(value: String) = _uiState.update { it.copy(composeBody = value) }
    fun onMemberSelect(id: Long) = _uiState.update { it.copy(selectedMemberId = id) }

    fun sendDirect() {
        val state = _uiState.value
        val memberId = state.selectedMemberId
        if (memberId == null || state.composeBody.isBlank()) {
            _uiState.update { it.copy(error = "گیرنده و متن پیام الزامی است") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (
                val result = repository.sendMessage(
                    TeamSendMessageRequest(
                        kind = "direct",
                        body = state.composeBody.trim(),
                        recipientMemberId = memberId,
                    ),
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showCompose = false,
                            successMessage = "پیام ارسال شد",
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

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }
}
