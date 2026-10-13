package ir.divarfiling.mobile.feature.postcall

import android.provider.CallLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PostCallLogicTest {

    @Test
    fun missed_call_is_not_presented() {
        val kind = PostCallLogic.classifyCallEnd(hadRinging = true, hadOffHook = false)
        assertEquals(PostCallLogic.CallEndKind.Missed, kind)
        assertFalse(PostCallLogic.shouldPresentAfterCall(kind))
    }

    @Test
    fun answered_and_outgoing_calls_are_presented() {
        assertTrue(
            PostCallLogic.shouldPresentAfterCall(
                PostCallLogic.classifyCallEnd(hadRinging = true, hadOffHook = true),
            ),
        )
        assertTrue(
            PostCallLogic.shouldPresentAfterCall(
                PostCallLogic.classifyCallEnd(hadRinging = false, hadOffHook = true),
            ),
        )
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
    fun call_log_accepts_incoming_and_outgoing_only() {
        assertTrue(PostCallLogic.shouldAcceptCallLogType(CallLog.Calls.INCOMING_TYPE))
        assertTrue(PostCallLogic.shouldAcceptCallLogType(CallLog.Calls.OUTGOING_TYPE))
        assertFalse(PostCallLogic.shouldAcceptCallLogType(CallLog.Calls.MISSED_TYPE))
    }

    @Test
    fun idle_without_session_returns_null_kind() {
        assertNull(PostCallLogic.classifyCallEnd(hadRinging = false, hadOffHook = false))
    }
}
