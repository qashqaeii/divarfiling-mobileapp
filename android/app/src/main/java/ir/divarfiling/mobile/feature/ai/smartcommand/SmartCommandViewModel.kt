package ir.divarfiling.mobile.feature.ai.smartcommand

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.AiCapabilitiesData
import ir.divarfiling.mobile.core.network.NlCommandParseRequest
import ir.divarfiling.mobile.core.network.NlCommandParseResult
import ir.divarfiling.mobile.core.network.NlCommandResolveResult
import ir.divarfiling.mobile.core.network.SmartCommandProfileDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.SmartCommandRepository
import ir.divarfiling.mobile.feature.ai.voice.VoiceInputPhase
import ir.divarfiling.mobile.feature.ai.voice.VoiceSpeechError
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SmartCommandUiState(
    val profileKey: SmartCommandProfileKey = SmartCommandProfileKey.TODAY,
    val profile: SmartCommandProfileDto? = null,
    val capabilities: AiCapabilitiesData? = null,
    val inputText: String = "",
    val voicePhase: VoiceInputPhase = VoiceInputPhase.Idle,
    val voiceError: String? = null,
    val phase: SmartCommandPhase = SmartCommandPhase.Idle,
    val parseResult: NlCommandParseResult? = null,
    val reminderEdit: ReminderPreviewEdit? = null,
    val contactEdit: ContactPreviewEdit? = null,
    val propertyEdit: PropertyPreviewEdit? = null,
    val selectedContactId: Long? = null,
    val successEntityType: String? = null,
    val successEntityId: Long? = null,
    val successMessage: String? = null,
    val blockReason: SmartCommandBlockReason? = null,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
    val quotaHint: String? = null,
    val isSheetOpen: Boolean = false,
    val isLoadingCapabilities: Boolean = false,
    val confirmInFlight: Boolean = false,
)

@HiltViewModel
class SmartCommandViewModel @Inject constructor(
    private val repository: SmartCommandRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SmartCommandUiState())
    val uiState: StateFlow<SmartCommandUiState> = _uiState.asStateFlow()

    private var boundProfile: SmartCommandProfileKey? = null
    private var baselineReminder: ReminderPreviewEdit? = null
    private var baselineContact: ContactPreviewEdit? = null
    private var baselineProperty: PropertyPreviewEdit? = null
    private var voiceInputBaseline: String = ""

    fun beginVoiceInputSession() {
        voiceInputBaseline = _uiState.value.inputText.trim()
    }

    fun updateVoicePartial(partial: String) {
        onInputChange(SmartCommandStateLogic.voiceTextFromBaseline(voiceInputBaseline, partial))
    }

    fun finalizeVoiceInput(final: String) {
        onInputChange(SmartCommandStateLogic.voiceTextFromBaseline(voiceInputBaseline, final))
        _uiState.update { it.copy(voicePhase = VoiceInputPhase.Ready, voiceError = null) }
    }

    fun bindProfile(profile: SmartCommandProfileKey) {
        if (boundProfile == profile && _uiState.value.capabilities != null) {
            _uiState.update { it.copy(profileKey = profile, profile = profile.profile(it.capabilities!!.smartCommandProfiles)) }
            return
        }
        boundProfile = profile
        _uiState.update { it.copy(profileKey = profile, isLoadingCapabilities = true) }
        loadCapabilities(profile)
    }

    fun openSheet() {
        val caps = _uiState.value.capabilities
        val block = SmartCommandStateLogic.evaluateBlock(caps)
        _uiState.update {
            it.copy(
                isSheetOpen = true,
                blockReason = block,
                errorMessage = null,
                statusMessage = null,
            )
        }
    }

    fun dismissSheet() {
        _uiState.update {
            it.copy(
                isSheetOpen = false,
                phase = SmartCommandPhase.Idle,
                parseResult = null,
                voicePhase = VoiceInputPhase.Idle,
                voiceError = null,
                confirmInFlight = false,
            )
        }
    }

    fun onInputChange(text: String) {
        val trimmed = text.take(SmartCommandMapping.MAX_INPUT_CHARS)
        _uiState.update { it.copy(inputText = trimmed, errorMessage = null) }
    }

    fun appendVoiceTranscript(text: String) {
        finalizeVoiceInput(text)
    }

    fun onVoicePhase(phase: VoiceInputPhase) {
        _uiState.update { it.copy(voicePhase = phase) }
    }

    fun onVoiceError(error: VoiceSpeechError?) {
        _uiState.update {
            it.copy(
                voicePhase = VoiceInputPhase.Error,
                voiceError = error?.userMessage,
            )
        }
    }

    fun clearVoiceError() = _uiState.update { it.copy(voiceError = null, voicePhase = VoiceInputPhase.Idle) }

    fun parse(resumeCorrection: Boolean = false) {
        val state = _uiState.value
        if (state.blockReason != null) return
        val text = state.inputText.trim()
        if (text.isBlank()) {
            _uiState.update { it.copy(errorMessage = "یک فرمان کوتاه بنویسید.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(phase = SmartCommandPhase.Parsing, errorMessage = null, statusMessage = null) }
            when (
                val result = repository.parse(
                    NlCommandParseRequest(
                        text = text,
                        commandId = if (resumeCorrection) state.parseResult?.commandId else null,
                        resumeCorrection = resumeCorrection,
                    ),
                )
            ) {
                is ApiResult.Success -> applyParse(result.data, resumeCorrection)
                is ApiResult.Error -> handleApiError(result.message, result.code)
            }
        }
    }

    fun applyPreviewEditsAndReparse() {
        val state = _uiState.value
        val intent = state.parseResult?.intent.orEmpty()
        val corrections = mutableListOf<String>()
        when (intent) {
            SmartCommandMapping.INTENT_REMINDER -> {
                val base = baselineReminder ?: return
                val edit = state.reminderEdit ?: return
                SmartCommandMapping.buildResumeCorrection(intent, base.title, edit.title, "title")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.note, edit.note, "note")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.dateText, edit.dateText, "date")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.timeText, edit.timeText, "time")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.contactName, edit.contactName, "contact")?.let { corrections.add(it) }
            }
            SmartCommandMapping.INTENT_CONTACT -> {
                val base = baselineContact ?: return
                val edit = state.contactEdit ?: return
                SmartCommandMapping.buildResumeCorrection(intent, base.name, edit.name, "name")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.phone, edit.phone, "phone")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.customerType, edit.customerType, "type")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.companyName, edit.companyName, "company")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.areas, edit.areas, "areas")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.notes, edit.notes, "notes")?.let { corrections.add(it) }
            }
            SmartCommandMapping.INTENT_PROPERTY -> {
                val base = baselineProperty ?: return
                val edit = state.propertyEdit ?: return
                SmartCommandMapping.buildResumeCorrection(intent, base.propertyType, edit.propertyType, "type")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.dealMode, edit.dealMode, "deal")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.neighborhood, edit.neighborhood, "neighborhood")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.city, edit.city, "city")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.area, edit.area, "area")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.rooms, edit.rooms, "rooms")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.salePrice, edit.salePrice, "price")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.deposit, edit.deposit, "deposit")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.monthlyRent, edit.monthlyRent, "rent")?.let { corrections.add(it) }
                SmartCommandMapping.buildResumeCorrection(intent, base.notes, edit.notes, "notes")?.let { corrections.add(it) }
            }
        }
        val merged = SmartCommandMapping.mergeCorrections(corrections)
        if (merged == null) {
            confirm()
            return
        }
        _uiState.update { it.copy(inputText = merged) }
        parse(resumeCorrection = true)
    }

    fun confirm() {
        val state = _uiState.value
        if (hasPreviewEdits()) {
            applyPreviewEditsAndReparse()
            return
        }
        val commandId = state.parseResult?.commandId
        if (commandId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "فرمان معتبر نیست. دوباره تلاش کنید.") }
            return
        }
        if (state.parseResult?.canConfirm != true) {
            _uiState.update {
                it.copy(
                    errorMessage = state.parseResult?.message ?: state.parseResult?.hint ?: "اطلاعات ناقص است.",
                )
            }
            return
        }
        if (state.confirmInFlight) return
        viewModelScope.launch {
            _uiState.update { it.copy(phase = SmartCommandPhase.Confirming, confirmInFlight = true, errorMessage = null) }
            when (val result = repository.confirm(commandId)) {
                is ApiResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            phase = SmartCommandPhase.Success,
                            confirmInFlight = false,
                            successEntityType = data.entityType,
                            successEntityId = data.entityId,
                            successMessage = data.message ?: "با موفقیت ثبت شد.",
                        )
                    }
                    refreshQuota()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(phase = SmartCommandPhase.Preview, confirmInFlight = false) }
                    handleApiError(result.message, result.code)
                }
            }
        }
    }

    fun selectContactCandidate(contactId: Long) {
        val commandId = _uiState.value.parseResult?.commandId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(phase = SmartCommandPhase.ResolvingContact, selectedContactId = contactId) }
            when (val result = repository.resolveContact(commandId, contactId)) {
                is ApiResult.Success -> applyResolve(result.data)
                is ApiResult.Error -> {
                    _uiState.update { it.copy(phase = SmartCommandPhase.Preview) }
                    handleApiError(result.message, result.code)
                }
            }
        }
    }

    fun clearPreviewKeepInput() {
        baselineReminder = null
        baselineContact = null
        baselineProperty = null
        _uiState.update {
            it.copy(
                phase = SmartCommandPhase.Idle,
                parseResult = null,
                reminderEdit = null,
                contactEdit = null,
                propertyEdit = null,
                statusMessage = null,
                errorMessage = null,
                selectedContactId = null,
            )
        }
    }

    fun resetFlow() {
        baselineReminder = null
        baselineContact = null
        baselineProperty = null
        _uiState.update {
            it.copy(
                phase = SmartCommandPhase.Idle,
                parseResult = null,
                inputText = "",
                reminderEdit = null,
                contactEdit = null,
                propertyEdit = null,
                successEntityType = null,
                successEntityId = null,
                successMessage = null,
                errorMessage = null,
                statusMessage = null,
                selectedContactId = null,
                confirmInFlight = false,
            )
        }
    }

    fun updateReminderEdit(edit: ReminderPreviewEdit) = _uiState.update { it.copy(reminderEdit = edit) }
    fun updateContactEdit(edit: ContactPreviewEdit) = _uiState.update { it.copy(contactEdit = edit) }
    fun updatePropertyEdit(edit: PropertyPreviewEdit) = _uiState.update { it.copy(propertyEdit = edit) }

    private fun hasPreviewEdits(): Boolean {
        val state = _uiState.value
        return SmartCommandStateLogic.hasPreviewEdits(
            intent = state.parseResult?.intent,
            baselineReminder = baselineReminder,
            baselineContact = baselineContact,
            baselineProperty = baselineProperty,
            reminderEdit = state.reminderEdit,
            contactEdit = state.contactEdit,
            propertyEdit = state.propertyEdit,
        )
    }

    private fun loadCapabilities(profile: SmartCommandProfileKey) {
        viewModelScope.launch {
            when (val result = repository.getCapabilities()) {
                is ApiResult.Success -> {
                    val caps = result.data
                    val prof = profile.profile(caps.smartCommandProfiles)
                    val block = SmartCommandStateLogic.evaluateBlock(caps)
                    _uiState.update {
                        it.copy(
                            capabilities = caps,
                            profile = prof,
                            isLoadingCapabilities = false,
                            blockReason = block,
                            quotaHint = SmartCommandMapping.quotaHint(
                                caps.nlCommand?.remaining ?: caps.quota?.remaining,
                                caps.nlCommand?.limit ?: caps.quota?.limit,
                            ),
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isLoadingCapabilities = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    private fun refreshQuota() {
        val profile = boundProfile ?: return
        loadCapabilities(profile)
    }

    private fun handleApiError(message: String, code: String?) {
        val caps = _uiState.value.capabilities
        val mapped = SmartCommandStateLogic.mapApiError(
            message,
            code,
            caps?.messages ?: ir.divarfiling.mobile.core.network.AiCapabilitiesMessages(),
        )
        _uiState.update {
            it.copy(
                phase = mapped.phase,
                blockReason = mapped.blockReason ?: it.blockReason,
                errorMessage = mapped.errorMessage,
                confirmInFlight = false,
            )
        }
    }

    private fun applyParse(data: NlCommandParseResult, resumeCorrection: Boolean) {
        val applied = SmartCommandStateLogic.applyParse(
            data = data,
            profile = _uiState.value.profile,
            resumeCorrection = resumeCorrection,
            currentBaselineReminder = baselineReminder,
            currentBaselineContact = baselineContact,
            currentBaselineProperty = baselineProperty,
        )
        baselineReminder = applied.baselineReminder
        baselineContact = applied.baselineContact
        baselineProperty = applied.baselineProperty
        _uiState.update {
            it.copy(
                phase = applied.phase,
                parseResult = applied.parseResult,
                reminderEdit = applied.reminderEdit,
                contactEdit = applied.contactEdit,
                propertyEdit = applied.propertyEdit,
                statusMessage = applied.statusMessage,
                errorMessage = null,
            )
        }
    }

    private fun applyResolve(data: NlCommandResolveResult) {
        val (updated, reminder) = SmartCommandStateLogic.applyResolve(_uiState.value.parseResult, data)
        baselineReminder = reminder
        _uiState.update {
            it.copy(
                phase = SmartCommandPhase.Preview,
                parseResult = updated,
                reminderEdit = reminder,
                statusMessage = null,
            )
        }
    }
}
