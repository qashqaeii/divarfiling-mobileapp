package ir.divarfiling.mobile.feature.ai

import ir.divarfiling.mobile.feature.ai.voice.VoiceTranscriptMerge
import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceTranscriptMergeTest {

    @Test
    fun appendSafe_firstSegment() {
        assertEquals("فردا ساعت ده", VoiceTranscriptMerge.appendTranscriptSegmentSafe("", "فردا ساعت ده"))
    }

    @Test
    fun appendSafe_overlapPrefixExtension() {
        val base = "فردا ساعت ده"
        val segment = "فردا ساعت ده با احمدی تماس بگیرم"
        assertEquals(segment, VoiceTranscriptMerge.appendTranscriptSegmentSafe(base, segment))
    }

    @Test
    fun appendSafe_noDuplicateConcat() {
        val base = "فردا ساعت ده"
        val segment = "فردا ساعت ده"
        assertEquals(base, VoiceTranscriptMerge.appendTranscriptSegmentSafe(base, segment))
    }

    @Test
    fun appendSafe_wordOverlap() {
        val merged = VoiceTranscriptMerge.appendTranscriptSegmentSafe(
            "فردا ساعت ده",
            "ساعت ده با احمدی",
        )
        assertEquals("فردا ساعت ده با احمدی", merged)
    }

    @Test
    fun buildLive_mergesSessionAndInterim() {
        val live = VoiceTranscriptMerge.buildLiveTextareaValue(
            baseInputText = "سلام",
            sessionTranscript = "فردا ساعت ده",
            cycleFinalTranscript = "",
            cycleInterimTranscript = "با احمدی",
        )
        assertEquals("سلام فردا ساعت ده با احمدی", live)
    }
}
