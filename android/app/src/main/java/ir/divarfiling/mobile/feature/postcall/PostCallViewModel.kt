package ir.divarfiling.mobile.feature.postcall

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.PostCallContactBriefDto
import ir.divarfiling.mobile.core.network.PostCallResolveData
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    "درخواست فایل",
    "هماهنگی بازدید",
    "اطلاعات تکمیل شد",
    "سایر",
)

val POST_CALL_OUTCOMES_OUTGOING = listOf(
    "مکالمه انجام شد",
    "پاسخ نداد",
    "نیاز به پیگیری",
    "ارسال فایل",
    "هماهنگی بازدید",
    "سایر",
)

data class PostCallUiState(
    val phone: String = "",
    val callEndKind: PostCallLogic.CallEndKind = PostCallLogic.CallEndKind.Answered,
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
    val reminderDueAtMillis: Long? = null,
    val savedReminderAtMillis: Long? = null,
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
) {
    fun isSessionComplete(): Boolean =
        leadSaved || outcomeSaved || noteSaved || reminderSaved
}

@HiltViewModel
class PostCallViewModel @Inject constructor(
    private val crmRepository: CrmRepository,
    private val savedStateHandle: SavedStateHandle,
    private val pendingCallStore: PostCallPendingCallStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PostCallUiState())
    val uiState: StateFlow<PostCallUiState> = _uiState.asStateFlow()

    fun load(metadata: PostCallCallMetadata) {
        val normalizedPhone = metadata.phone.trim()
        val headerSource = metadata.withValidCallLogFieldsOnly()
        val noteHeader = PostCallNoteHeaderBuilder.build(headerSource)
        val draft = readDraft(metadata)
        val defaultReminder = PostCallLogic.ReminderScheduling.tomorrowMorning(zone = ZoneId.systemDefault()).toEpochMilli()
        _uiState.value = PostCallUiState(
            phone = normalizedPhone,
            callEndKind = metadata.callEndKind,
            noteHeader = noteHeader,
            callMetadata = metadata,
            note = draft?.note.orEmpty(),
            leadName = draft?.leadName.orEmpty(),
            selectedOutcome = draft?.outcome,
            reminderDueAtMillis = draft?.reminderMillis ?: defaultReminder,
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

    fun onDismissWithoutSave() {
        val state = _uiState.value
        val meta = state.callMetadata ?: return
        persistDraft(state)
        if (!state.isSessionComplete()) {
            pendingCallStore.enqueue(meta, state.contact?.fullName)
        }
    }

    private fun composeCrmNote(userBody: String): String =
        PostCallNoteHeaderBuilder.composeNoteForCrm(_uiState.value.noteHeader, userBody)

    fun setExpanded(expanded: Boolean) = _uiState.update { it.copy(expanded = expanded) }

    private fun applyResolve(data: PostCallResolveData) {
        _uiState.update {
            val mode = PostCallLogic.resolveStatusToMode(data.status)
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

    fun onLeadNameChange(v: String) {
        _uiState.update { it.copy(leadName = v) }
        persistDraft(_uiState.value)
    }

    fun onNoteChange(v: String) {
        _uiState.update { it.copy(note = v) }
        persistDraft(_uiState.value)
    }

    fun onLeadTypeChange(type: String) = _uiState.update { it.copy(leadCustomerType = type) }

    fun onOutcomeSelected(outcome: String) {
        _uiState.update { it.copy(selectedOutcome = outcome) }
        persistDraft(_uiState.value)
    }

    fun onReminderTimeChange(millis: Long) {
        _uiState.update { it.copy(reminderDueAtMillis = millis) }
        persistDraft(_uiState.value)
    }

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

    fun submitPrimaryAction() {
        val state = _uiState.value
        when {
            state.callEndKind == PostCallLogic.CallEndKind.Missed &&
                state.mode == PostCallUiMode.Existing -> {
                val at = state.reminderDueAtMillis
                    ?: PostCallLogic.ReminderScheduling.tomorrowMorning().toEpochMilli()
                submitReminderAt(Instant.ofEpochMilli(at))
            }
            state.selectedOutcome != null -> submitCallOutcome()
            (state.note.isNotBlank() || state.noteHeader.isNotBlank()) -> submitNote()
        }
    }

    fun submitQuickReminderOneHour() {
        submitReminderAt(PostCallLogic.ReminderScheduling.oneHourFromNow())
    }

    fun submitQuickReminderTomorrow() {
        submitReminderAt(PostCallLogic.ReminderScheduling.tomorrowMorning())
    }

    fun submitNote() {
        val state = _uiState.value
        val contactId = state.contact?.id ?: return
        val note = state.note.trim()
        if ((note.isBlank() && state.noteHeader.isBlank()) || state.isSubmittingNote || state.noteSaved) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingNote = true, error = null) }
            when (val result = crmRepository.createNote(contactId, composeCrmNote(note))) {
                is ApiResult.Success -> {
                    onSessionCompleted()
                    _uiState.update {
                        it.copy(
                            isSubmittingNote = false,
                            noteSaved = true,
                            successMessage = "یادداشت ثبت شد",
                            note = "",
                        )
                    }
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
                is ApiResult.Success -> {
                    onSessionCompleted()
                    _uiState.update {
                        it.copy(
                            isSubmittingOutcome = false,
                            outcomeSaved = true,
                            noteSaved = extra.isNotBlank() || it.noteSaved,
                            successMessage = "نتیجه تماس ثبت شد",
                            note = "",
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingOutcome = false, error = result.message)
                }
            }
        }
    }

    private fun submitReminderAt(due: Instant) {
        val state = _uiState.value
        val contactId = state.contact?.id ?: return
        if (state.isSubmittingReminder || state.reminderSaved) return
        val dueAt = due.toString()
        val title = "پیگیری تماس"
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReminder = true, error = null) }
            val reminderNote = composeCrmNote(state.note.trim())
            when (val result = crmRepository.createReminder(contactId, title, dueAt, note = reminderNote)) {
                is ApiResult.Success -> {
                    onSessionCompleted()
                    _uiState.update {
                        it.copy(
                            isSubmittingReminder = false,
                            reminderSaved = true,
                            savedReminderAtMillis = due.toEpochMilli(),
                            successMessage = "یادآور ثبت شد",
                        )
                    }
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
                            is ApiResult.Success -> {
                                onSessionCompleted()
                                _uiState.update {
                                    it.copy(
                                        isSubmittingLead = false,
                                        leadSaved = true,
                                        noteSaved = true,
                                        successMessage = "مخاطب و یادداشت ثبت شد",
                                        mode = PostCallUiMode.Existing,
                                        contact = result.data.toPostCallBrief(contactId),
                                    )
                                }
                            }
                            is ApiResult.Error -> {
                                onSessionCompleted()
                                _uiState.update {
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
                        }
                    } else {
                        onSessionCompleted()
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

    private fun onSessionCompleted() {
        _uiState.value.callMetadata?.let { pendingCallStore.removeForMetadata(it) }
        clearDraft()
    }

    private fun draftKey(metadata: PostCallCallMetadata): String =
        "pc_draft_${metadata.phone}_${metadata.callEndKind.name}_${metadata.callLogDateMs ?: 0}"

    private data class DraftSnapshot(
        val note: String,
        val leadName: String,
        val outcome: String?,
        val reminderMillis: Long?,
    )

    private fun readDraft(metadata: PostCallCallMetadata): DraftSnapshot? {
        val key = draftKey(metadata)
        val note = savedStateHandle.get<String>("${key}_note")
        val lead = savedStateHandle.get<String>("${key}_lead")
        val outcome = savedStateHandle.get<String>("${key}_outcome")
        val reminder = savedStateHandle.get<Long>("${key}_reminder")
        if (note == null && lead == null && outcome == null && reminder == null) return null
        return DraftSnapshot(
            note = note.orEmpty(),
            leadName = lead.orEmpty(),
            outcome = outcome,
            reminderMillis = reminder?.takeIf { it > 0L },
        )
    }

    private fun persistDraft(state: PostCallUiState) {
        val meta = state.callMetadata ?: return
        val key = draftKey(meta)
        savedStateHandle["${key}_note"] = state.note
        savedStateHandle["${key}_lead"] = state.leadName
        savedStateHandle["${key}_outcome"] = state.selectedOutcome
        savedStateHandle["${key}_reminder"] = state.reminderDueAtMillis ?: 0L
    }

    private fun clearDraft() {
        val meta = _uiState.value.callMetadata ?: return
        val key = draftKey(meta)
        savedStateHandle.remove<String>("${key}_note")
        savedStateHandle.remove<String>("${key}_lead")
        savedStateHandle.remove<String>("${key}_outcome")
        savedStateHandle.remove<Long>("${key}_reminder")
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
