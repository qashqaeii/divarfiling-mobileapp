package ir.divarfiling.mobile.feature.ai

import ir.divarfiling.mobile.core.network.AiCapabilitiesData
import ir.divarfiling.mobile.core.network.AiCapabilitiesMessages
import ir.divarfiling.mobile.core.network.AiFeatureQuota
import ir.divarfiling.mobile.core.network.NlCommandParseResult
import ir.divarfiling.mobile.core.network.NlContactCandidateDto
import ir.divarfiling.mobile.core.network.SmartCommandProfileDto
import ir.divarfiling.mobile.feature.ai.message.ContactSmartMessageLogic
import ir.divarfiling.mobile.feature.ai.smartcommand.ReminderPreviewEdit
import ir.divarfiling.mobile.feature.ai.smartcommand.SmartCommandBlockReason
import ir.divarfiling.mobile.feature.ai.smartcommand.SmartCommandMapping
import ir.divarfiling.mobile.feature.ai.smartcommand.WrongIntentDestination
import ir.divarfiling.mobile.feature.ai.smartcommand.SmartCommandPhase
import ir.divarfiling.mobile.feature.ai.smartcommand.SmartCommandStateLogic
import ir.divarfiling.mobile.feature.ai.voice.SpeechErrorMapper
import ir.divarfiling.mobile.feature.ai.voice.VoiceSpeechError
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartCommandStateLogicTest {

    @Test
    fun parseReminder_goesToPreview() {
        val preview = buildJsonObject {
            put("title", "تماس")
            put("contact_name", "احمدی")
        }
        val result = SmartCommandStateLogic.applyParse(
            data = NlCommandParseResult(intent = SmartCommandMapping.INTENT_REMINDER, preview = preview, canConfirm = true),
            profile = null,
            resumeCorrection = false,
            currentBaselineReminder = null,
            currentBaselineContact = null,
            currentBaselineProperty = null,
        )
        assertEquals(SmartCommandPhase.Preview, result.phase)
        assertEquals("تماس", result.reminderEdit?.title)
    }

    @Test
    fun parseContact_goesToPreview() {
        val preview = buildJsonObject { put("name", "رضا") }
        val result = SmartCommandStateLogic.applyParse(
            data = NlCommandParseResult(intent = SmartCommandMapping.INTENT_CONTACT, preview = preview),
            profile = null,
            resumeCorrection = false,
            currentBaselineReminder = null,
            currentBaselineContact = null,
            currentBaselineProperty = null,
        )
        assertEquals(SmartCommandPhase.Preview, result.phase)
        assertEquals("رضا", result.contactEdit?.name)
    }

    @Test
    fun parseProperty_goesToPreview() {
        val preview = buildJsonObject { put("type_deal_display", "آپارتمان · فروش") }
        val result = SmartCommandStateLogic.applyParse(
            data = NlCommandParseResult(intent = SmartCommandMapping.INTENT_PROPERTY, preview = preview),
            profile = null,
            resumeCorrection = false,
            currentBaselineReminder = null,
            currentBaselineContact = null,
            currentBaselineProperty = null,
        )
        assertEquals(SmartCommandPhase.Preview, result.phase)
        assertTrue(result.propertyEdit?.propertyType?.contains("آپارتمان") == true)
    }

    @Test
    fun unknownIntent_showsMessage() {
        val result = SmartCommandStateLogic.applyParse(
            data = NlCommandParseResult(intent = SmartCommandMapping.INTENT_UNKNOWN),
            profile = null,
            resumeCorrection = false,
            currentBaselineReminder = null,
            currentBaselineContact = null,
            currentBaselineProperty = null,
        )
        assertTrue(result.statusMessage.orEmpty().contains("متوجه نشدم"))
    }

    @Test
    fun ambiguousContact_goesToResolvingWhenEnabled() {
        val profile = SmartCommandProfileDto(
            allowedIntent = SmartCommandMapping.INTENT_REMINDER,
            enableContactResolve = true,
        )
        val result = SmartCommandStateLogic.applyParse(
            data = NlCommandParseResult(
                intent = SmartCommandMapping.INTENT_REMINDER,
                ambiguousFields = listOf("contact_name"),
                contactCandidates = listOf(NlContactCandidateDto(1, "احمدی", "0912…")),
            ),
            profile = profile,
            resumeCorrection = false,
            currentBaselineReminder = null,
            currentBaselineContact = null,
            currentBaselineProperty = null,
        )
        assertEquals(SmartCommandPhase.ResolvingContact, result.phase)
    }

    @Test
    fun ambiguousContact_staysPreviewWhenResolveDisabled() {
        val profile = SmartCommandProfileDto(
            allowedIntent = SmartCommandMapping.INTENT_REMINDER,
            enableContactResolve = false,
        )
        val result = SmartCommandStateLogic.applyParse(
            data = NlCommandParseResult(
                intent = SmartCommandMapping.INTENT_REMINDER,
                ambiguousFields = listOf("contact_name"),
                contactCandidates = listOf(NlContactCandidateDto(1, "احمدی", "0912…")),
            ),
            profile = profile,
            resumeCorrection = false,
            currentBaselineReminder = null,
            currentBaselineContact = null,
            currentBaselineProperty = null,
        )
        assertEquals(SmartCommandPhase.Preview, result.phase)
        assertTrue(result.ambiguousMessages.isNotEmpty())
    }

    @Test
    fun wrongIntent_inTodayContext() {
        val profile = SmartCommandProfileDto(
            allowedIntent = SmartCommandMapping.INTENT_REMINDER,
            wrongIntentMessage = "این درخواست مربوط به فایل‌های شخصی است.",
        )
        val result = SmartCommandStateLogic.applyParse(
            data = NlCommandParseResult(intent = SmartCommandMapping.INTENT_PROPERTY),
            profile = profile,
            resumeCorrection = false,
            currentBaselineReminder = null,
            currentBaselineContact = null,
            currentBaselineProperty = null,
        )
        assertEquals(WrongIntentDestination.Properties, result.wrongIntent?.navigation)
        assertFalse(result.showStructuredPreview)
    }

    @Test
    fun incompleteReminder_hidesStructuredPreview() {
        val result = SmartCommandStateLogic.applyParse(
            data = NlCommandParseResult(
                intent = SmartCommandMapping.INTENT_REMINDER,
                missingFields = listOf("time_text"),
                canConfirm = false,
            ),
            profile = SmartCommandProfileDto(allowedIntent = SmartCommandMapping.INTENT_REMINDER),
            resumeCorrection = false,
            currentBaselineReminder = null,
            currentBaselineContact = null,
            currentBaselineProperty = null,
        )
        assertFalse(result.showStructuredPreview)
        assertTrue(result.missingMessages.isNotEmpty())
    }

    @Test
    fun resolveContact_updatesCanConfirmAndPreviewUi() {
        val previewBefore = buildJsonObject {
            put("title", "تماس")
            put("contact_name", "احمدی")
            put("date_text", "فردا")
            put("time_text", "عصر")
        }
        val current = NlCommandParseResult(
            intent = SmartCommandMapping.INTENT_REMINDER,
            preview = previewBefore,
            canConfirm = false,
            ambiguousFields = listOf("contact_name"),
            contactCandidates = listOf(
                NlContactCandidateDto(1, "احمدی الف", ""),
                NlContactCandidateDto(2, "احمدی ب", ""),
            ),
            commandId = "cmd-1",
        )
        val resolvedPreview = buildJsonObject {
            put("title", "تماس")
            put("contact_name", "احمدی الف")
            put("date_text", "فردا")
            put("time_text", "عصر")
        }
        val result = SmartCommandStateLogic.applyResolve(
            current = current,
            data = ir.divarfiling.mobile.core.network.NlCommandResolveResult(
                preview = resolvedPreview,
                canConfirm = true,
                ambiguousFields = emptyList(),
            ),
            profile = SmartCommandProfileDto(
                allowedIntent = SmartCommandMapping.INTENT_REMINDER,
                enableContactResolve = true,
            ),
        )
        assertEquals(SmartCommandPhase.Preview, result.phase)
        assertTrue(result.showStructuredPreview)
        assertTrue(result.parseResult.canConfirm)
        assertEquals("احمدی الف", result.reminderEdit?.contactName)
        assertTrue(result.parseResult.contactCandidates.isEmpty())
    }

    @Test
    fun block_noLicense() {
        val caps = AiCapabilitiesData(hasLicense = false, aiEnabled = true, canUseSmartCommand = true)
        assertEquals(SmartCommandBlockReason.NoLicense, SmartCommandStateLogic.evaluateBlock(caps))
    }

    @Test
    fun unlimited_superuser_notBlocked() {
        val caps = AiCapabilitiesData(
            aiEnabled = true,
            hasLicense = false,
            isUnlimited = true,
            canUseSmartCommand = true,
            nlCommand = AiFeatureQuota(featureRemaining = 0, featureLimit = 10),
        )
        assertNull(SmartCommandStateLogic.evaluateBlock(caps))
    }

    @Test
    fun block_quotaExhausted() {
        val caps = AiCapabilitiesData(
            aiEnabled = true,
            hasLicense = true,
            canUseSmartCommand = true,
            nlCommand = AiFeatureQuota(featureRemaining = 0, featureLimit = 10),
        )
        assertEquals(SmartCommandBlockReason.QuotaExhausted, SmartCommandStateLogic.evaluateBlock(caps))
    }

    @Test
    fun quota_notBlockedWhenNlQuotaShapeUnparsed() {
        val caps = AiCapabilitiesData(
            aiEnabled = true,
            hasLicense = true,
            canUseSmartCommand = true,
            nlCommand = AiFeatureQuota(
                remaining = 0,
                feature = "nl_command",
                featureLimit = 50,
            ),
            quota = AiFeatureQuota(remaining = 25, limit = 100),
        )
        assertNull(SmartCommandStateLogic.evaluateBlock(caps))
    }

    @Test
    fun quota_blockedWhenRemainingMappedFromApi() {
        val caps = AiCapabilitiesData(
            aiEnabled = true,
            hasLicense = true,
            canUseSmartCommand = true,
            nlCommand = AiFeatureQuota(remaining = 12, limit = 50),
        )
        assertNull(SmartCommandStateLogic.evaluateBlock(caps))
    }

    @Test
    fun block_aiDisabled() {
        val caps = AiCapabilitiesData(aiEnabled = false)
        assertEquals(SmartCommandBlockReason.AiDisabled, SmartCommandStateLogic.evaluateBlock(caps))
    }

    @Test
    fun apiError_quotaMapsToBlock() {
        val mapped = SmartCommandStateLogic.mapApiError(
            "x",
            "QUOTA_EXCEEDED",
            AiCapabilitiesMessages(quotaExhausted = "سهمیه تمام"),
        )
        assertEquals(SmartCommandBlockReason.QuotaExhausted, mapped.blockReason)
    }

    @Test
    fun previewEdits_detected() {
        val base = ReminderPreviewEdit(title = "a")
        val edit = ReminderPreviewEdit(title = "b")
        assertTrue(
            SmartCommandStateLogic.hasPreviewEdits(
                SmartCommandMapping.INTENT_REMINDER,
                base,
                null,
                null,
                edit,
                null,
                null,
            ),
        )
    }

    @Test
    fun voiceFromBaseline_partialThenFinal_noDuplicate() {
        val baseline = ""
        val partial = SmartCommandStateLogic.voiceTextFromBaseline(baseline, "فردا تماس")
        val finalText = SmartCommandStateLogic.voiceTextFromBaseline(baseline, "فردا تماس بگیرم")
        assertEquals("فردا تماس", partial)
        assertEquals("فردا تماس بگیرم", finalText)
    }

    @Test
    fun voiceTranscript_appendsToInput() {
        val merged = SmartCommandStateLogic.appendVoiceTranscript("سلام", "فردا تماس")
        assertEquals("سلام فردا تماس", merged)
    }

    @Test
    fun fieldVoiceInsert_emptyBase() {
        val merged = SmartCommandStateLogic.appendVoiceTranscript("", "یادداشت صوتی")
        assertEquals("یادداشت صوتی", merged)
    }

    @Test
    fun speechPermissionDenied_persianMessage() {
        assertTrue(SpeechErrorMapper.permissionDenied().userMessage.contains("میکروفن"))
    }

    @Test
    fun speechNoMatch_persianMessage() {
        assertEquals(VoiceSpeechError.NoSpeech.userMessage, "صدایی تشخیص داده نشد، دوباره امتحان کنید.")
    }

    @Test
    fun crmMessage_noLicense() {
        val access = ContactSmartMessageLogic.accessFromCapabilities(
            AiCapabilitiesData(
                aiEnabled = true,
                hasLicense = false,
                canUseCrmMessage = true,
                messages = AiCapabilitiesMessages(noLicense = "بدون لایسنس"),
            ),
        )
        assertFalse(access.canUse)
        assertEquals("بدون لایسنس", access.blockMessage)
    }

    @Test
    fun crmMessage_quotaExhausted() {
        val access = ContactSmartMessageLogic.accessFromCapabilities(
            AiCapabilitiesData(
                aiEnabled = true,
                hasLicense = true,
                canUseCrmMessage = true,
                crmMessage = AiFeatureQuota(remaining = 0, limit = 5),
                messages = AiCapabilitiesMessages(quotaExhausted = "تمام"),
            ),
        )
        assertFalse(access.canUse)
        assertEquals("تمام", access.blockMessage)
    }
}
