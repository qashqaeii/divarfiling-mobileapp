package ir.divarfiling.mobile.feature.postcall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.PostCallContactBriefDto
import ir.divarfiling.mobile.core.network.PostCallResolveData
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PostCallUiMode {
    Loading,
    Existing,
    New,
    Ambiguous,
    Invalid,
    Error,
}

data class PostCallUiState(
    val phone: String = "",
    val callEndKind: PostCallLogic.CallEndKind = PostCallLogic.CallEndKind.Answered,
    val mode: PostCallUiMode = PostCallUiMode.Loading,
    val contact: PostCallContactBriefDto? = null,
    val ambiguousContacts: List<PostCallContactBriefDto> = emptyList(),
    val leadName: String = "",
    val note: String = "",
    val isResolving: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val submitLocked: Boolean = false,
)

@HiltViewModel
class PostCallViewModel @Inject constructor(
    private val crmRepository: CrmRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PostCallUiState())
    val uiState: StateFlow<PostCallUiState> = _uiState.asStateFlow()

    fun load(phone: String, callEndKind: PostCallLogic.CallEndKind = PostCallLogic.CallEndKind.Answered) {
        val normalizedPhone = phone.trim()
        _uiState.value = PostCallUiState(
            phone = normalizedPhone,
            callEndKind = callEndKind,
            isResolving = true,
        )
        viewModelScope.launch {
            when (val result = crmRepository.resolvePostCall(normalizedPhone)) {
                is ApiResult.Success -> applyResolve(result.data)
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isResolving = false,
                        mode = PostCallUiMode.Error,
                        error = result.message,
                    )
                }
            }
        }
    }

    private fun applyResolve(data: PostCallResolveData) {
        _uiState.update {
            val mode = when (data.status) {
                "existing_contact" -> PostCallUiMode.Existing
                "new_contact" -> PostCallUiMode.New
                "ambiguous" -> PostCallUiMode.Ambiguous
                "invalid_phone" -> PostCallUiMode.Invalid
                else -> PostCallUiMode.Error
            }
            it.copy(
                phone = data.phone.ifBlank { it.phone },
                mode = mode,
                contact = data.contact,
                ambiguousContacts = data.contacts,
                isResolving = false,
                error = if (mode == PostCallUiMode.Error) "خطا در شناسایی مخاطب" else null,
            )
        }
    }

    fun onLeadNameChange(v: String) = _uiState.update { it.copy(leadName = v) }
    fun onNoteChange(v: String) = _uiState.update { it.copy(note = v) }

    fun submitNote() {
        val state = _uiState.value
        val contactId = state.contact?.id ?: return
        val note = state.note.trim()
        if (note.isBlank() || state.isSubmitting || state.submitLocked) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = crmRepository.createNote(contactId, note)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submitLocked = true,
                        successMessage = "یادداشت ثبت شد",
                        note = "",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun submitQuickLead() {
        val state = _uiState.value
        val name = state.leadName.trim()
        val phone = state.phone.trim()
        val note = state.note.trim()
        if (name.isBlank() || phone.isBlank() || state.isSubmitting || state.submitLocked) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = crmRepository.quickLead(name, phone)) {
                is ApiResult.Success -> {
                    val contactId = result.data.id
                    if (note.isNotBlank()) {
                        when (val noteResult = crmRepository.createNote(contactId, note)) {
                            is ApiResult.Success -> _uiState.update {
                                it.copy(
                                    isSubmitting = false,
                                    submitLocked = true,
                                    successMessage = "مخاطب و یادداشت ثبت شد",
                                    mode = PostCallUiMode.Existing,
                                    contact = result.data.toPostCallBrief(contactId),
                                )
                            }
                            is ApiResult.Error -> _uiState.update {
                                it.copy(
                                    isSubmitting = false,
                                    submitLocked = true,
                                    successMessage = "مخاطب ثبت شد",
                                    error = "یادداشت ذخیره نشد: ${noteResult.message}",
                                    mode = PostCallUiMode.Existing,
                                    contact = result.data.toPostCallBrief(contactId),
                                )
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                submitLocked = true,
                                successMessage = "مخاطب ثبت شد",
                                mode = PostCallUiMode.Existing,
                                contact = result.data.toPostCallBrief(contactId),
                            )
                        }
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun clearTransient() {
        _uiState.update { it.copy(successMessage = null, error = null) }
    }
}

private fun ContactDto.toPostCallBrief(contactId: Long): PostCallContactBriefDto =
    PostCallContactBriefDto(
        id = contactId,
        fullName = fullName,
        phone = phone.orEmpty(),
        customerType = customerType.orEmpty(),
        status = status.orEmpty(),
        lastFollowUp = null,
    )
