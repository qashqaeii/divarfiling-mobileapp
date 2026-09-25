package ir.divarfiling.mobile.feature.ai.smartcommand

import ir.divarfiling.mobile.core.network.NlCommandParseResult
import ir.divarfiling.mobile.core.network.NlContactCandidateDto
import ir.divarfiling.mobile.core.network.SmartCommandProfileDto
import ir.divarfiling.mobile.feature.ai.voice.SpeechErrorMapper
import ir.divarfiling.mobile.feature.ai.voice.VoiceSpeechError
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartCommandMappingTest {
    @Test
    fun reminderPreview_mapsFields() {
        val preview = buildJsonObject {
            put("contact_name", "احمدی")
            put("title", "تماس")
            put("note", "پیگیری")
            put("date_text", "فردا")
            put("time_text", "۱۰")
            put("due_at_display", "فردا ۱۰")
        }
        val edit = SmartCommandMapping.reminderFromPreview(preview)
        assertEquals("احمدی", edit.contactName)
        assertEquals("تماس", edit.title)
        assertEquals("پیگیری", edit.note)
    }

    @Test
    fun unknownMessage_includesExamples() {
        val msg = SmartCommandMapping.unknownUserMessage(listOf("نمونه ۱", "نمونه ۲"))
        assertTrue(msg.contains("متوجه نشدم"))
        assertTrue(msg.contains("نمونه ۱"))
    }

    @Test
    fun contactResolution_whenAmbiguous() {
        val result = NlCommandParseResult(
            intent = SmartCommandMapping.INTENT_REMINDER,
            ambiguousFields = listOf("contact_name"),
            contactCandidates = listOf(NlContactCandidateDto(1, "علی", "0912…")),
        )
        val profile = SmartCommandProfileDto(enableContactResolve = true)
        assertTrue(SmartCommandMapping.needsContactResolution(profile, result))
        assertFalse(SmartCommandMapping.needsContactResolution(SmartCommandProfileDto(enableContactResolve = false), result))
    }

    @Test
    fun wrongIntent_detectsPropertyInTodayProfile() {
        val profile = SmartCommandProfileDto(allowedIntent = SmartCommandMapping.INTENT_REMINDER)
        val data = NlCommandParseResult(intent = SmartCommandMapping.INTENT_PROPERTY)
        assertTrue(SmartCommandMapping.isWrongIntent(profile, data))
        assertEquals(WrongIntentDestination.Properties, SmartCommandMapping.wrongIntentDestination(data.intent))
    }

    @Test
    fun profileExamples_limitedToThree() {
        val chips = SmartCommandMapping.profileExamplesForChips(
            listOf("a", "b", "c", "d"),
            max = 3,
        )
        assertEquals(3, chips.size)
    }

    @Test
    fun quotaHint_formatsRemaining() {
        assertEquals("7 از 10 فرمان امروز باقی مانده", SmartCommandMapping.quotaHint(7, 10))
    }

    @Test
    fun resumeCorrection_buildsPersianPhrase() {
        val phrase = SmartCommandMapping.buildResumeCorrection(
            SmartCommandMapping.INTENT_REMINDER,
            "تماس",
            "تماس فوری",
            "title",
        )
        assertEquals("عنوانش تماس فوری باشه", phrase)
    }

    @Test
    fun mapApiError_quotaExceeded() {
        val (reason, msg) = SmartCommandMapping.mapApiError(
            "QUOTA_EXCEEDED",
            "raw",
            ir.divarfiling.mobile.core.network.AiCapabilitiesMessages(quotaExhausted = "سهمیه تمام"),
        )
        assertEquals(SmartCommandBlockReason.QuotaExhausted, reason)
        assertEquals("سهمیه تمام", msg)
    }
}

class SpeechErrorMappingTest {
    @Test
    fun permissionDenied_message() {
        assertEquals(
            "برای استفاده از میکروفن، دسترسی ضبط صدا لازم است.",
            SpeechErrorMapper.permissionDenied().userMessage,
        )
    }

    @Test
    fun noSpeech_message() {
        assertEquals(
            "صدایی تشخیص داده نشد، دوباره امتحان کنید.",
            VoiceSpeechError.NoSpeech.userMessage,
        )
    }
}
