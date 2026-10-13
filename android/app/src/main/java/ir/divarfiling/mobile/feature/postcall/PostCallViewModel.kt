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
import java.time.Instant
import javax.inject.Inject

enum class PostCallUiMode {
    Loading,
    Existing,
    New,
    Ambiguous,
    Invalid,
    Error,
}

val POST_CALL_LEAD_TYPES = listOf("خریدار", "مالک", "فروشنده", "مستاجر", "سرنخ")

val POST_CALL_OUTCOMES = listOf(
    "نیاز به پیگیری",
    "ارسال فایل",
    "هماهنگی بازدید",
    "پاسخ نداد",
    "سایر",
)

val POST_CALL_OUTCOMES_OUTGOING = listOf(
    "مکالمه انجام شد",
    "پاسخ نداد",
    "ارسال فایل",
    "هماهنگی بازدید",
    "پیگیری مجدد",
    "سایر",
)

data class PostCallUiState(
    val phone: String = "",
    val callEndKind: PostCallLogic.CallEndKind = PostCallLogic.CallEndKind.Answered,
    /** سربرگ ثابت یادداشت تماس؛ یک‌بار از Call Log ساخته می‌شود. */
    val noteHeader: String = "",
    val callMetadata: PostCallCallMetadata? = null,
    val mode: PostCallUiMode = PostCallUiMode.Loading,
    val expanded: Boolean = false,
    val contact: PostCallContactBriefDto? = null,
    val ambiguousContacts: List<PostCallContactBriefDto> = emptyList(),
    val leadName: String = "",
    val leadCustomerType: String = "سرنخ",
    val note: String = "",
    val selectedOutcome: String? = null,
    val isResolving: Boolean = true,
    val isSubmittingNote: Boolean = false,
    val isSubmittingOutcome: Boolean = false,
    val isSubmittingReminder: Boolean = false,
    val isSubmittingLead: Boolean = false,
    val noteSaved: Boolean = false,
    val outcomeSaved: Boolean = false,
    val reminderSaved: Boolean = false,
    val leadSaved: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class PostCallViewModel @Inject constructor(
    private val crmRepository: CrmRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PostCallUiState())
    val uiState: StateFlow<PostCallUiState> = _uiState.asStateFlow()

    fun load(metadata: PostCallCallMetadata) {
        val normalizedPhone = metadata.phone.trim()
        val headerSource = metadata.withValidCallLogFieldsOnly()
        val noteHeader = PostCallNoteHeaderBuilder.build(headerSource)
        _uiState.value = PostCallUiState(
            phone = normalizedPhone,
            callEndKind = metadata.callEndKind,
            noteHeader = noteHeader,
            callMetadata = metadata,
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

    fun retryResolve() {
        val meta = _uiState.value.callMetadata ?: return
        load(meta)
    }

    private fun composeCrmNote(userBody: String): String =
        PostCallNoteHeaderBuilder.composeNoteForCrm(_uiState.value.noteHeader, userBody)

    fun setExpanded(expanded: Boolean) = _uiState.update { it.copy(expanded = expanded) }

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
    fun onLeadTypeChange(type: String) = _uiState.update { it.copy(leadCustomerType = type) }
    fun onOutcomeSelected(outcome: String) = _uiState.update { it.copy(selectedOutcome = outcome) }

    fun selectAmbiguousContact(contact: PostCallContactBriefDto) {
        _uiState.update {
            it.copy(
                mode = PostCallUiMode.Existing,
                contact = contact,
                ambiguousContacts = emptyList(),
                error = null,
            )
        }
    }

    fun submitNote() {
        val state = _uiState.value
        val contactId = state.contact?.id ?: return
        val note = state.note.trim()
        if ((note.isBlank() && state.noteHeader.isBlank()) || state.isSubmittingNote || state.noteSaved) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingNote = true, error = null) }
            when (val result = crmRepository.createNote(contactId, composeCrmNote(note))) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSubmittingNote = false,
                        noteSaved = true,
                        successMessage = "یادداشت ثبت شد",
                        note = "",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingNote = false, error = result.message)
                }
            }
        }
    }

    fun submitCallOutcome() {
        val state = _uiState.value
        val contactId = state.contact?.id ?: return
        val outcome = state.selectedOutcome ?: return
        val extra = state.note.trim()
        val body = buildString {
            append("نتیجه تماس: $outcome")
            if (extra.isNotBlank()) append("\n").append(extra)
        }
        val content = composeCrmNote(body)
        if (state.isSubmittingOutcome || state.outcomeSaved) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingOutcome = true, error = null) }
            when (val result = crmRepository.createNote(contactId, content)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSubmittingOutcome = false,
                        outcomeSaved = true,
                        successMessage = "نتیجه تماس ثبت شد",
                        note = "",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingOutcome = false, error = result.message)
                }
            }
        }
    }

    fun submitQuickReminder(hoursFromNow: Long) {
        val state = _uiState.value
        val contactId = state.contact?.id ?: return
        if (state.isSubmittingReminder || state.reminderSaved) return
        val dueAt = Instant.now().plusSeconds(hoursFromNow * 3600).toString()
        val title = "پیگیری تماس"
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReminder = true, error = null) }
            val reminderNote = composeCrmNote(state.note.trim())
            when (val result = crmRepository.createReminder(contactId, title, dueAt, note = reminderNote)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSubmittingReminder = false,
                        reminderSaved = true,
                        successMessage = "یادآور ثبت شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingReminder = false, error = result.message)
                }
            }
        }
    }

    fun submitQuickLead() {
        val state = _uiState.value
        val name = state.leadName.trim()
        val phone = state.phone.trim()
        val note = state.note.trim()
        if (name.isBlank() || phone.isBlank() || state.isSubmittingLead || state.leadSaved) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingLead = true, error = null) }
            when (val result = crmRepository.quickLead(name, phone, state.leadCustomerType)) {
                is ApiResult.Success -> {
                    val contactId = result.data.id
                    if (note.isNotBlank() && !state.noteSaved) {
                        when (val noteResult = crmRepository.createNote(contactId, composeCrmNote(note))) {
                            is ApiResult.Success -> _uiState.update {
                                it.copy(
                                    isSubmittingLead = false,
                                    leadSaved = true,
                                    noteSaved = true,
                                    successMessage = "مخاطب و یادداشت ثبت شد",
                                    mode = PostCallUiMode.Existing,
                                    contact = result.data.toPostCallBrief(contactId),
                                )
                            }
                            is ApiResult.Error -> _uiState.update {
                                it.copy(
                                    isSubmittingLead = false,
                                    leadSaved = true,
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
                                isSubmittingLead = false,
                                leadSaved = true,
                                successMessage = "مخاطب ثبت شد",
                                mode = PostCallUiMode.Existing,
                                contact = result.data.toPostCallBrief(contactId),
                            )
                        }
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingLead = false, error = result.message)
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
