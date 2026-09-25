package ir.divarfiling.mobile.feature.ai.smartcommand

import ir.divarfiling.mobile.core.network.AiCapabilitiesData
import ir.divarfiling.mobile.core.network.AiCapabilitiesMessages
import ir.divarfiling.mobile.core.network.NlCommandParseResult
import ir.divarfiling.mobile.core.network.NlCommandResolveResult
import ir.divarfiling.mobile.core.network.SmartCommandProfileDto
import ir.divarfiling.mobile.feature.ai.voice.VoiceTranscriptMerge

/** Pure state transitions for Smart Command — unit-testable without ViewModel. */
object SmartCommandStateLogic {

    fun evaluateBlock(caps: AiCapabilitiesData?): SmartCommandBlockReason? {
        if (caps == null) return null
        if (caps.isUnlimited) return null
        if (!caps.aiEnabled) return SmartCommandBlockReason.AiDisabled
        if (!caps.hasLicense) return SmartCommandBlockReason.NoLicense
        if (!caps.canUseSmartCommand) return SmartCommandBlockReason.FeatureDisabled
        val remaining = smartCommandRemaining(caps)
        if (remaining != null && remaining <= 0) return SmartCommandBlockReason.QuotaExhausted
        return null
    }

    internal fun smartCommandRemaining(caps: AiCapabilitiesData): Int? {
        if (caps.isUnlimited) return null
        caps.nlCommand?.resolvedRemaining()?.let { return it }
        return caps.quota?.resolvedRemaining()
    }

    fun appendVoiceTranscript(currentInput: String, spoken: String): String {
        val merged = VoiceTranscriptMerge.appendTranscriptSegmentSafe(currentInput, spoken)
        return merged.take(SmartCommandMapping.MAX_INPUT_CHARS)
    }

    /** @deprecated Prefer [VoiceTranscriptMerge.buildLiveTextareaValue] in voice sessions */
    fun voiceTextFromBaseline(baseline: String, spoken: String): String {
        return VoiceTranscriptMerge.buildLiveTextareaValue(baseline, "", spoken, "")
            .take(SmartCommandMapping.MAX_INPUT_CHARS)
    }

    fun hasPreviewEdits(
        intent: String?,
        baselineReminder: ReminderPreviewEdit?,
        baselineContact: ContactPreviewEdit?,
        baselineProperty: PropertyPreviewEdit?,
        reminderEdit: ReminderPreviewEdit?,
        contactEdit: ContactPreviewEdit?,
        propertyEdit: PropertyPreviewEdit?,
    ): Boolean = when (intent) {
        SmartCommandMapping.INTENT_REMINDER -> baselineReminder != null && baselineReminder != reminderEdit
        SmartCommandMapping.INTENT_CONTACT -> baselineContact != null && baselineContact != contactEdit
        SmartCommandMapping.INTENT_PROPERTY -> baselineProperty != null && baselineProperty != propertyEdit
        else -> false
    }

    data class ParseApplyResult(
        val phase: SmartCommandPhase,
        val parseResult: NlCommandParseResult,
        val reminderEdit: ReminderPreviewEdit?,
        val contactEdit: ContactPreviewEdit?,
        val propertyEdit: PropertyPreviewEdit?,
        val statusMessage: String?,
        val baselineReminder: ReminderPreviewEdit?,
        val baselineContact: ContactPreviewEdit?,
        val baselineProperty: PropertyPreviewEdit?,
        val wrongIntent: WrongIntentState? = null,
        val previewLines: List<SmartCommandPreviewLineUi> = emptyList(),
        val missingMessages: List<String> = emptyList(),
        val ambiguousMessages: List<String> = emptyList(),
        val showStructuredPreview: Boolean = true,
    )

    fun applyParse(
        data: NlCommandParseResult,
        profile: SmartCommandProfileDto?,
        resumeCorrection: Boolean,
        currentBaselineReminder: ReminderPreviewEdit?,
        currentBaselineContact: ContactPreviewEdit?,
        currentBaselineProperty: PropertyPreviewEdit?,
    ): ParseApplyResult {
        if (data.intent == SmartCommandMapping.INTENT_UNKNOWN) {
            return ParseApplyResult(
                phase = SmartCommandPhase.Preview,
                parseResult = data,
                reminderEdit = null,
                contactEdit = null,
                propertyEdit = null,
                statusMessage = SmartCommandMapping.unknownUserMessage(profile?.examples.orEmpty()),
                baselineReminder = currentBaselineReminder,
                baselineContact = currentBaselineContact,
                baselineProperty = currentBaselineProperty,
                showStructuredPreview = false,
            )
        }

        SmartCommandMapping.wrongIntentState(profile, data)?.let { wrong ->
            return ParseApplyResult(
                phase = SmartCommandPhase.Preview,
                parseResult = data,
                reminderEdit = null,
                contactEdit = null,
                propertyEdit = null,
                statusMessage = wrong.message,
                baselineReminder = currentBaselineReminder,
                baselineContact = currentBaselineContact,
                baselineProperty = currentBaselineProperty,
                wrongIntent = wrong,
                showStructuredPreview = false,
            )
        }

        if (SmartCommandMapping.isContactNotFound(data)) {
            return ParseApplyResult(
                phase = SmartCommandPhase.Preview,
                parseResult = data,
                reminderEdit = null,
                contactEdit = null,
                propertyEdit = null,
                statusMessage = data.message ?: data.hint ?: "مخاطب پیدا نشد.",
                baselineReminder = currentBaselineReminder,
                baselineContact = currentBaselineContact,
                baselineProperty = currentBaselineProperty,
                showStructuredPreview = false,
            )
        }

        val phase = if (SmartCommandMapping.needsContactResolution(profile, data)) {
            SmartCommandPhase.ResolvingContact
        } else {
            SmartCommandPhase.Preview
        }

        val reminder = SmartCommandMapping.reminderFromPreview(data.preview)
        val contact = SmartCommandMapping.contactFromPreview(data.preview)
        val property = SmartCommandMapping.propertyFromPreview(data.preview)
        val baselineReminder = if (resumeCorrection) currentBaselineReminder else reminder
        val baselineContact = if (resumeCorrection) currentBaselineContact else contact
        val baselineProperty = if (resumeCorrection) currentBaselineProperty else property

        val previewLines = SmartCommandMapping.previewLinesFromApi(data.preview, data.missingFields)
        val missingMessages = SmartCommandMapping.humanizeMissingFields(data.missingFields, data.intent)
        val ambiguousMessages = SmartCommandMapping.humanizeAmbiguousFields(data.ambiguousFields, data.intent)

        val incompleteReminder = SmartCommandMapping.isIncompleteReminder(data)
        val showStructuredPreview = when (data.intent) {
            SmartCommandMapping.INTENT_REMINDER -> !incompleteReminder && data.canConfirm
            SmartCommandMapping.INTENT_CONTACT,
            SmartCommandMapping.INTENT_PROPERTY,
            -> data.canConfirm || previewLines.isNotEmpty()
            else -> data.canConfirm
        }

        val statusMessage = when {
            incompleteReminder -> SmartCommandMapping.incompleteReminderMessage(data)
            !data.canConfirm && missingMessages.isNotEmpty() -> missingMessages.first()
            !data.canConfirm && ambiguousMessages.isNotEmpty() -> ambiguousMessages.first()
            else -> data.message ?: data.hint
        }

        return ParseApplyResult(
            phase = phase,
            parseResult = data,
            reminderEdit = if (showStructuredPreview) reminder else null,
            contactEdit = if (showStructuredPreview && data.intent == SmartCommandMapping.INTENT_CONTACT) contact else null,
            propertyEdit = if (showStructuredPreview && data.intent == SmartCommandMapping.INTENT_PROPERTY) property else null,
            statusMessage = statusMessage,
            baselineReminder = baselineReminder,
            baselineContact = baselineContact,
            baselineProperty = baselineProperty,
            previewLines = previewLines,
            missingMessages = missingMessages,
            ambiguousMessages = ambiguousMessages,
            showStructuredPreview = showStructuredPreview,
        )
    }

    fun applyResolve(
        current: NlCommandParseResult?,
        data: NlCommandResolveResult,
    ): Pair<NlCommandParseResult?, ReminderPreviewEdit?> {
        val reminder = SmartCommandMapping.reminderFromPreview(data.preview)
        val updated = current?.copy(
            preview = data.preview,
            canConfirm = data.canConfirm,
            missingFields = data.missingFields,
            ambiguousFields = data.ambiguousFields,
        )
        return updated to reminder
    }

    data class ApiErrorApplyResult(
        val blockReason: SmartCommandBlockReason?,
        val errorMessage: String,
        val phase: SmartCommandPhase = SmartCommandPhase.Error,
    )

    fun mapApiError(
        message: String,
        code: String?,
        messages: AiCapabilitiesMessages,
    ): ApiErrorApplyResult {
        val (block, mapped) = SmartCommandMapping.mapApiError(code, message, messages)
        return ApiErrorApplyResult(blockReason = block, errorMessage = mapped)
    }
}
