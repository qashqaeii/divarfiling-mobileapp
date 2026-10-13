package ir.divarfiling.mobile.feature.postcall

import android.provider.CallLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PostCallLogicTest {

    @Test
    fun ringing_to_idle_is_missed_and_presented() {
        val kind = PostCallLogic.classifyCallEnd(hadRinging = true, hadOffHook = false)
        assertEquals(PostCallLogic.CallEndKind.Missed, kind)
        assertTrue(PostCallLogic.shouldPresentAfterCall(kind))
    }

    @Test
    fun ringing_offhook_idle_is_answered() {
        val kind = PostCallLogic.classifyCallEnd(hadRinging = true, hadOffHook = true)
        assertEquals(PostCallLogic.CallEndKind.Answered, kind)
        assertTrue(PostCallLogic.shouldPresentAfterCall(kind))
    }

    @Test
    fun offhook_idle_without_ringing_is_outgoing() {
        val kind = PostCallLogic.classifyCallEnd(hadRinging = false, hadOffHook = true)
        assertEquals(PostCallLogic.CallEndKind.Outgoing, kind)
        assertTrue(PostCallLogic.shouldPresentAfterCall(kind))
    }

    @Test
    fun private_numbers_are_rejected() {
        assertTrue(PostCallLogic.isPrivateOrEmptyNumber(null))
        assertTrue(PostCallLogic.isPrivateOrEmptyNumber("مخفی"))
        assertFalse(PostCallLogic.isPrivateOrEmptyNumber("09121234567"))
    }

    @Test
    fun duplicate_events_within_window_are_suppressed() {
        val now = 10_000L
        assertTrue(
            PostCallLogic.shouldSuppressDuplicate(
                lastPhone = "09121234567",
                lastAtMs = 9_000L,
                phone = "09121234567",
                nowMs = now,
                windowMs = 45_000L,
            ),
        )
        assertFalse(
            PostCallLogic.shouldSuppressDuplicate(
                lastPhone = "09120000000",
                lastAtMs = 9_000L,
                phone = "09121234567",
                nowMs = now,
                windowMs = 45_000L,
            ),
        )
    }

    @Test
    fun call_log_accepts_missed_type_for_missed_kind() {
        assertTrue(
            PostCallLogic.shouldAcceptCallLogType(
                CallLog.Calls.MISSED_TYPE,
                PostCallLogic.CallEndKind.Missed,
            ),
        )
        assertTrue(
            PostCallLogic.matchesCallLogEntryStrict(
                PostCallLogic.CallLogEntry("09121234567", CallLog.Calls.MISSED_TYPE, 5_000L, 0L),
                PostCallLogic.CallEndKind.Missed,
            ),
        )
    }

    @Test
    fun pick_call_log_prefers_matching_hint_and_recent_window() {
        val now = 20_000L
        val entries = listOf(
            PostCallLogic.CallLogEntry("09121111111", CallLog.Calls.OUTGOING_TYPE, 19_500L, 30L),
            PostCallLogic.CallLogEntry("09122222222", CallLog.Calls.MISSED_TYPE, 19_800L, 0L),
            PostCallLogic.CallLogEntry("09123333333", CallLog.Calls.MISSED_TYPE, 5_000L, 0L),
        )
        val picked = PostCallLogic.pickCallLogNumber(
            entries = entries,
            kind = PostCallLogic.CallEndKind.Missed,
            notBeforeMs = now,
            hintedNormalized = "09122222222",
            normalize = { it },
            isValidMobile = { it.startsWith("09") },
        )
        assertEquals("09122222222", picked)
    }

    @Test
    fun pick_call_log_does_not_select_unrelated_old_record() {
        val now = 20_000L
        val entries = listOf(
            PostCallLogic.CallLogEntry("09129999999", CallLog.Calls.MISSED_TYPE, 1_000L, 0L),
        )
        val picked = PostCallLogic.pickCallLogNumber(
            entries = entries,
            kind = PostCallLogic.CallEndKind.Missed,
            notBeforeMs = now,
            hintedNormalized = null,
            normalize = { it },
            isValidMobile = { it.startsWith("09") },
        )
        assertNull(picked)
    }

    @Test
    fun idle_without_session_returns_null_kind() {
        assertNull(PostCallLogic.classifyCallEnd(hadRinging = false, hadOffHook = false))
    }

    @Test
    fun ignored_kind_is_never_presented() {
        assertFalse(PostCallLogic.shouldPresentAfterCall(PostCallLogic.CallEndKind.Ignored))
    }
}
