package ir.divarfiling.mobile.feature.postcall

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.ContactUpdateRequest
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
    val leadExtrasExpanded: Boolean = false,
    val leadBudgetMin: String = "",
    val leadBudgetMax: String = "",
    val leadPropertyType: String = "",
    val leadAreas: String = "",
    val note: String = "",
    val selectedOutcome: String? = null,
    val reminderDueAtMillis: Long? = null,
    val followUpReminderEnabled: Boolean = false,
    val savedReminderAtMillis: Long? = null,
    val isResolving: Boolean = true,
    val isSubmittingSession: Boolean = false,
    val isSubmittingNote: Boolean = false,
    val isSubmittingOutcome: Boolean = false,
    val isSubmittingReminder: Boolean = false,
    val isSubmittingLead: Boolean = false,
    val noteSaved: Boolean = false,
    val outcomeSaved: Boolean = false,
    val reminderSaved: Boolean = false,
    val leadSaved: Boolean = false,
    val leadExtrasSaved: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null,
    val partialSuccessMessage: String? = null,
)

@HiltViewModel
class PostCallViewModel @Inject constructor(
    private val crmRepository: CrmRepository,
    private val savedStateHandle: SavedStateHandle,
    private val pendingCallStore: PostCallPendingCallStore,
    private val draftStore: PostCallDraftStore,
    private val sessionStore: SessionStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PostCallUiState())
    val uiState: StateFlow<PostCallUiState> = _uiState.asStateFlow()

    private var activeSubmitToken: String? = null

    fun load(metadata: PostCallCallMetadata) {
        val normalizedPhone = metadata.phone.trim()
        val headerSource = metadata.withValidCallLogFieldsOnly()
        val noteHeader = PostCallNoteHeaderBuilder.build(headerSource)
        val defaultReminder = PostCallLogic.ReminderScheduling.tomorrowMorning(zone = ZoneId.systemDefault()).toEpochMilli()
        viewModelScope.launch {
            val userId = sessionStore.getUser()?.id
            val persisted = userId?.let { draftStore.read(it, metadata) }
            val draft = persisted?.let { draftToSnapshot(it) } ?: readDraftFromSavedState(metadata)
            _uiState.value = PostCallUiState(
                phone = normalizedPhone,
                callEndKind = metadata.callEndKind,
                noteHeader = noteHeader,
                callMetadata = metadata,
                note = draft?.note.orEmpty(),
                leadName = draft?.leadName.orEmpty(),
                leadCustomerType = draft?.leadCustomerType ?: "سرنخ",
                leadBudgetMin = draft?.leadBudgetMin.orEmpty(),
                leadBudgetMax = draft?.leadBudgetMax.orEmpty(),
                leadPropertyType = draft?.leadPropertyType.orEmpty(),
                leadAreas = draft?.leadAreas.orEmpty(),
                selectedOutcome = draft?.outcome,
                reminderDueAtMillis = draft?.reminderMillis ?: defaultReminder,
                followUpReminderEnabled = draft?.followUpReminderEnabled == true,
                isResolving = true,
            )
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
        if (PostCallSessionPolicy.shouldEnqueuePendingOnDismiss(state)) {
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

    fun onLeadTypeChange(type: String) {
        _uiState.update { it.copy(leadCustomerType = type) }
        persistDraft(_uiState.value)
    }

    fun onLeadExtrasExpandedChange(expanded: Boolean) = _uiState.update { it.copy(leadExtrasExpanded = expanded) }

    fun onLeadBudgetMinChange(v: String) {
        _uiState.update { it.copy(leadBudgetMin = v) }
        persistDraft(_uiState.value)
    }

    fun onLeadBudgetMaxChange(v: String) {
        _uiState.update { it.copy(leadBudgetMax = v) }
        persistDraft(_uiState.value)
    }

    fun onLeadPropertyTypeChange(v: String) {
        _uiState.update { it.copy(leadPropertyType = v) }
        persistDraft(_uiState.value)
    }

    fun onLeadAreasChange(v: String) {
        _uiState.update { it.copy(leadAreas = v) }
        persistDraft(_uiState.value)
    }

    fun onOutcomeSelected(outcome: String) {
        _uiState.update { it.copy(selectedOutcome = outcome) }
        persistDraft(_uiState.value)
    }

    fun onReminderTimeChange(millis: Long) {
        _uiState.update {
            it.copy(
                reminderDueAtMillis = millis,
                followUpReminderEnabled = true,
            )
        }
        persistDraft(_uiState.value)
    }

    fun selectQuickReminderOneHour() {
        val at = PostCallLogic.ReminderScheduling.oneHourFromNow().toEpochMilli()
        _uiState.update { it.copy(reminderDueAtMillis = at, followUpReminderEnabled = true) }
        persistDraft(_uiState.value)
    }

    fun selectQuickReminderTomorrow() {
        val at = PostCallLogic.ReminderScheduling.tomorrowMorning().toEpochMilli()
        _uiState.update { it.copy(reminderDueAtMillis = at, followUpReminderEnabled = true) }
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
            state.mode == PostCallUiMode.New ||
                (state.leadSaved && !PostCallSessionPolicy.isSessionComplete(state)) -> submitQuickLead()
            else -> submitCallSession()
        }
    }

    fun submitCallSession() {
        val state = _uiState.value
        if (state.isSubmittingSession || activeSubmitToken != null) return
        val contactId = state.contact?.id ?: return
        val token = PostCallSessionPolicy.eventIdentity(state.callMetadata ?: return)
        activeSubmitToken = token
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingSession = true, error = null, partialSuccessMessage = null) }
            val errors = mutableListOf<String>()
            var outcomeOk = state.outcomeSaved
            var noteOk = state.noteSaved
            var reminderOk = state.reminderSaved

            if (!state.outcomeSaved && state.selectedOutcome != null && state.callEndKind != PostCallLogic.CallEndKind.Missed) {
                when (val r = saveOutcome(contactId, state)) {
                    is SaveStep.Ok -> {
                        outcomeOk = true
                        _uiState.update { it.copy(outcomeSaved = true, note = if (state.note.isNotBlank()) "" else it.note) }
                    }
                    is SaveStep.Fail -> errors += r.message
                }
            } else if (!state.noteSaved && state.note.isNotBlank() && state.selectedOutcome == null) {
                when (val r = saveNote(contactId, state.note)) {
                    is SaveStep.Ok -> {
                        noteOk = true
                        _uiState.update { it.copy(noteSaved = true, note = "") }
                    }
                    is SaveStep.Fail -> errors += r.message
                }
            } else if (!state.noteSaved && state.note.isNotBlank() && state.outcomeSaved) {
                when (val r = saveNote(contactId, state.note)) {
                    is SaveStep.Ok -> {
                        noteOk = true
                        _uiState.update { it.copy(noteSaved = true, note = "") }
                    }
                    is SaveStep.Fail -> errors += r.message
                }
            }

            if (!state.reminderSaved && PostCallSessionPolicy.wantsReminderOnSubmit(state)) {
                val due = Instant.ofEpochMilli(
                    state.reminderDueAtMillis
                        ?: PostCallLogic.ReminderScheduling.tomorrowMorning().toEpochMilli(),
                )
                when (val r = saveReminder(contactId, due, state.note.takeIf { !noteOk && !outcomeOk })) {
                    is SaveStep.Ok -> {
                        reminderOk = true
                        _uiState.update {
                            it.copy(reminderSaved = true, savedReminderAtMillis = due.toEpochMilli())
                        }
                    }
                    is SaveStep.Fail -> errors += r.message
                }
            }

            activeSubmitToken = null
            _uiState.update { current ->
                val successParts = buildList {
                    if (outcomeOk && !state.outcomeSaved) add("نتیجه تماس")
                    if (noteOk && !state.noteSaved) add("یادداشت")
                    if (reminderOk && !state.reminderSaved) add("یادآور")
                }
                val successMessage = when {
                    successParts.isEmpty() && errors.isEmpty() -> null
                    successParts.isNotEmpty() && errors.isEmpty() -> "${successParts.joinToString(" و ")} ثبت شد"
                    successParts.isNotEmpty() -> "${successParts.joinToString(" و ")} ثبت شد"
                    else -> null
                }
                current.copy(
                    isSubmittingSession = false,
                    isSubmittingOutcome = false,
                    isSubmittingNote = false,
                    isSubmittingReminder = false,
                    successMessage = successMessage,
                    partialSuccessMessage = if (errors.isNotEmpty()) errors.joinToString("\n") else null,
                    error = if (errors.isNotEmpty() && successParts.isEmpty()) errors.first() else current.error,
                )
            }
            maybeFinalizeSession()
        }
    }

    private sealed class SaveStep {
        data object Ok : SaveStep()
        data class Fail(val message: String) : SaveStep()
    }

    private suspend fun saveOutcome(contactId: Long, state: PostCallUiState): SaveStep {
        val outcome = state.selectedOutcome ?: return SaveStep.Fail("نتیجه تماس انتخاب نشده")
        val extra = state.note.trim()
        val body = buildString {
            append("نتیجه تماس: $outcome")
            if (extra.isNotBlank()) append("\n").append(extra)
        }
        return when (val result = crmRepository.createNote(contactId, composeCrmNote(body))) {
            is ApiResult.Success -> SaveStep.Ok
            is ApiResult.Error -> SaveStep.Fail(result.message)
        }
    }

    private suspend fun saveNote(contactId: Long, note: String): SaveStep {
        val trimmed = note.trim()
        if (trimmed.isBlank() && _uiState.value.noteHeader.isBlank()) return SaveStep.Fail("یادداشت خالی است")
        return when (val result = crmRepository.createNote(contactId, composeCrmNote(trimmed))) {
            is ApiResult.Success -> SaveStep.Ok
            is ApiResult.Error -> SaveStep.Fail(result.message)
        }
    }

    private suspend fun saveReminder(contactId: Long, due: Instant, note: String?): SaveStep {
        val reminderNote = note?.trim()?.takeIf { it.isNotBlank() }?.let { composeCrmNote(it) }
        return when (
            val result = crmRepository.createReminder(
                contactId,
                "پیگیری تماس",
                due.toString(),
                note = reminderNote ?: "",
            )
        ) {
            is ApiResult.Success -> SaveStep.Ok
            is ApiResult.Error -> SaveStep.Fail(result.message)
        }
    }

    fun submitNote() = submitCallSession()

    fun submitCallOutcome() = submitCallSession()

    fun submitQuickReminderOneHour() = selectQuickReminderOneHour()

    fun submitQuickReminderTomorrow() = selectQuickReminderTomorrow()

    fun submitQuickLead() {
        val state = _uiState.value
        if (state.isSubmittingLead || state.isSubmittingSession) return
        val contactId = state.contact?.id
        if (state.leadSaved && contactId != null) {
            retryLeadFollowUps(contactId)
            return
        }
        val name = state.leadName.trim()
        val phone = state.phone.trim()
        if (name.isBlank() || phone.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingLead = true, isSubmittingSession = true, error = null) }
            when (val result = crmRepository.quickLead(name, phone, state.leadCustomerType)) {
                is ApiResult.Success -> {
                    val newContactId = result.data.id
                    val brief = result.data.toPostCallBrief(newContactId)
                    _uiState.update {
                        it.copy(
                            leadSaved = true,
                            mode = PostCallUiMode.Existing,
                            contact = brief,
                            leadExtrasSaved = !PostCallSessionPolicy.hasLeadExtrasInput(it),
                        )
                    }
                    completeLeadFollowUps(newContactId)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingLead = false, isSubmittingSession = false, error = result.message)
                }
            }
        }
    }

    private fun retryLeadFollowUps(contactId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingLead = true, isSubmittingSession = true, error = null) }
            completeLeadFollowUps(contactId)
        }
    }

    private suspend fun completeLeadFollowUps(contactId: Long) {
        var state = _uiState.value
        val errors = mutableListOf<String>()
        var extrasSaved = state.leadExtrasSaved
        var noteSaved = state.noteSaved

        if (!extrasSaved && PostCallSessionPolicy.hasLeadExtrasInput(state)) {
            when (val r = applyLeadExtras(contactId, state)) {
                is SaveStep.Ok -> extrasSaved = true
                is SaveStep.Fail -> errors += r.message
            }
        }

        state = _uiState.value
        val note = state.note.trim()
        if (!noteSaved && note.isNotBlank()) {
            when (val r = saveNote(contactId, note)) {
                is SaveStep.Ok -> noteSaved = true
                is SaveStep.Fail -> errors += r.message
            }
        }

        _uiState.update {
            it.copy(
                isSubmittingLead = false,
                isSubmittingSession = false,
                leadExtrasSaved = extrasSaved,
                noteSaved = noteSaved,
                note = if (noteSaved) "" else it.note,
                successMessage = when {
                    errors.isEmpty() -> "ثبت اطلاعات تماس انجام شد"
                    noteSaved || extrasSaved -> "بخشی از اطلاعات ثبت شد"
                    else -> "مخاطب ثبت شد"
                },
                partialSuccessMessage = errors.joinToString("\n").ifBlank { null },
            )
        }
        persistDraft(_uiState.value)
        maybeFinalizeSession()
    }

    private suspend fun applyLeadExtras(contactId: Long, state: PostCallUiState): SaveStep {
        if (!PostCallSessionPolicy.hasLeadExtrasInput(state)) return SaveStep.Ok
        val request = ContactUpdateRequest(
            budgetMin = FormatUtils.parseLocalizedLong(state.leadBudgetMin),
            budgetMax = FormatUtils.parseLocalizedLong(state.leadBudgetMax),
            propertyType = state.leadPropertyType.trim().ifBlank { null },
            areas = state.leadAreas.trim().ifBlank { null },
        )
        return when (val r = crmRepository.updateContact(contactId, request)) {
            is ApiResult.Success -> SaveStep.Ok
            is ApiResult.Error -> SaveStep.Fail("اطلاعات تکمیلی ذخیره نشد: ${r.message}")
        }
    }

    fun clearTransient() {
        _uiState.update { it.copy(successMessage = null, error = null, partialSuccessMessage = null) }
    }

    private fun maybeFinalizeSession() {
        val state = _uiState.value
        if (!PostCallSessionPolicy.isSessionComplete(state)) return
        state.callMetadata?.let { pendingCallStore.removeForMetadata(it) }
        viewModelScope.launch {
            val userId = sessionStore.getUser()?.id
            val meta = state.callMetadata
            if (userId != null && meta != null) {
                draftStore.clear(userId, meta)
            }
            clearDraftFromSavedState()
        }
    }

    private fun draftKey(metadata: PostCallCallMetadata): String =
        "pc_draft_${metadata.phone}_${metadata.callEndKind.name}_${metadata.callLogDateMs ?: 0}"

    private data class DraftSnapshot(
        val note: String,
        val leadName: String,
        val outcome: String?,
        val reminderMillis: Long?,
        val followUpReminderEnabled: Boolean,
        val leadCustomerType: String,
        val leadBudgetMin: String,
        val leadBudgetMax: String,
        val leadPropertyType: String,
        val leadAreas: String,
    )

    private fun draftToSnapshot(d: PostCallDraft): DraftSnapshot =
        DraftSnapshot(
            note = d.note,
            leadName = d.leadName,
            outcome = d.outcome,
            reminderMillis = d.reminderMillis,
            followUpReminderEnabled = d.followUpReminderEnabled,
            leadCustomerType = d.leadCustomerType,
            leadBudgetMin = d.leadBudgetMin,
            leadBudgetMax = d.leadBudgetMax,
            leadPropertyType = d.leadPropertyType,
            leadAreas = d.leadAreas,
        )

    private fun readDraftFromSavedState(metadata: PostCallCallMetadata): DraftSnapshot? {
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
            followUpReminderEnabled = false,
            leadCustomerType = "سرنخ",
            leadBudgetMin = "",
            leadBudgetMax = "",
            leadPropertyType = "",
            leadAreas = "",
        )
    }

    private fun persistDraft(state: PostCallUiState) {
        val meta = state.callMetadata ?: return
        val key = draftKey(meta)
        savedStateHandle["${key}_note"] = state.note
        savedStateHandle["${key}_lead"] = state.leadName
        savedStateHandle["${key}_outcome"] = state.selectedOutcome
        savedStateHandle["${key}_reminder"] = state.reminderDueAtMillis ?: 0L
        viewModelScope.launch {
            val userId = sessionStore.getUser()?.id ?: return@launch
            draftStore.save(
                userId,
                meta,
                PostCallDraft(
                    callKey = PostCallSessionPolicy.eventIdentity(meta),
                    userId = userId,
                    note = state.note,
                    leadName = state.leadName,
                    outcome = state.selectedOutcome,
                    reminderMillis = state.reminderDueAtMillis,
                    followUpReminderEnabled = state.followUpReminderEnabled,
                    leadCustomerType = state.leadCustomerType,
                    leadBudgetMin = state.leadBudgetMin,
                    leadBudgetMax = state.leadBudgetMax,
                    leadPropertyType = state.leadPropertyType,
                    leadAreas = state.leadAreas,
                ),
            )
        }
    }

    private fun clearDraftFromSavedState() {
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
