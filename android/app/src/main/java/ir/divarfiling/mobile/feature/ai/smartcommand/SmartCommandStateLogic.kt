package ir.divarfiling.mobile.feature.ai.smartcommand

import ir.divarfiling.mobile.core.network.AiCapabilitiesData
import ir.divarfiling.mobile.core.network.AiCapabilitiesMessages
import ir.divarfiling.mobile.core.network.NlCommandParseResult
import ir.divarfiling.mobile.core.network.NlCommandResolveResult
import ir.divarfiling.mobile.core.network.SmartCommandProfileDto

/** Pure state transitions for Smart Command — unit-testable without ViewModel. */
object SmartCommandStateLogic {

    fun evaluateBlock(caps: AiCapabilitiesData?): SmartCommandBlockReason? {
        if (caps == null) return null
        if (!caps.aiEnabled) return SmartCommandBlockReason.AiDisabled
        if (!caps.hasLicense) return SmartCommandBlockReason.NoLicense
        if (!caps.canUseSmartCommand) return SmartCommandBlockReason.FeatureDisabled
        val remaining = caps.nlCommand?.remaining ?: caps.quota?.remaining
        if (remaining != null && remaining <= 0) return SmartCommandBlockReason.QuotaExhausted
        return null
    }

    fun appendVoiceTranscript(currentInput: String, spoken: String): String {
        val merged = (currentInput.trim() + " " + spoken.trim()).trim()
        return merged.take(SmartCommandMapping.MAX_INPUT_CHARS)
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
            )
        }
        val reminder = SmartCommandMapping.reminderFromPreview(data.preview)
        val contact = SmartCommandMapping.contactFromPreview(data.preview)
        val property = SmartCommandMapping.propertyFromPreview(data.preview)
        val baselineReminder = if (resumeCorrection) currentBaselineReminder else reminder
        val baselineContact = if (resumeCorrection) currentBaselineContact else contact
        val baselineProperty = if (resumeCorrection) currentBaselineProperty else property
        val phase = if (SmartCommandMapping.needsContactResolution(data)) {
            SmartCommandPhase.ResolvingContact
        } else {
            SmartCommandPhase.Preview
        }
        return ParseApplyResult(
            phase = phase,
            parseResult = data,
            reminderEdit = reminder,
            contactEdit = contact,
            propertyEdit = property,
            statusMessage = data.message ?: data.hint,
            baselineReminder = baselineReminder,
            baselineContact = baselineContact,
            baselineProperty = baselineProperty,
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
