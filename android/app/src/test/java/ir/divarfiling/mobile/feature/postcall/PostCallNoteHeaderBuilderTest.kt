package ir.divarfiling.mobile.feature.postcall

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostCallNoteHeaderBuilderTest {

    @Test
    fun missed_call_has_no_duration_line() {
        val header = PostCallNoteHeaderBuilder.build(
            PostCallCallMetadata(
                phone = "09121234567",
                callEndKind = PostCallLogic.CallEndKind.Missed,
                callLogDateMs = 1_700_000_000_000L,
                durationSec = 120L,
                callLogType = null,
            ),
        )
        assertTrue(header.contains("بی‌پاسخ"))
        assertFalse(header.contains("مدت مکالمه"))
    }

    @Test
    fun compose_note_merges_header_and_body_once() {
        val header = "یادداشت تماس\nنوع تماس: خروجی"
        val merged = PostCallNoteHeaderBuilder.composeNoteForCrm(header, "مشتری پیگیری خواست")
        assertTrue(merged.contains("توضیحات:"))
        assertTrue(merged.contains("مشتری پیگیری خواست"))
        assertFalse(merged.contains("یادداشت تماس\n\nیادداشت تماس"))
    }

    @Test
    fun zero_duration_omits_duration_line() {
        val header = PostCallNoteHeaderBuilder.build(
            PostCallCallMetadata(
                phone = "09121234567",
                callEndKind = PostCallLogic.CallEndKind.Outgoing,
                callLogDateMs = null,
                durationSec = 0L,
                callLogType = null,
            ),
        )
        assertFalse(header.contains("مدت مکالمه"))
    }
}
