package ir.divarfiling.mobile.feature.postcall

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PostCallEventHubTest {

    private fun meta(
        phone: String,
        kind: PostCallLogic.CallEndKind = PostCallLogic.CallEndKind.Answered,
        callLogDateMs: Long? = 1000L,
    ) = PostCallCallMetadata(
        phone = phone,
        callEndKind = kind,
        callLogDateMs = callLogDateMs,
        durationSec = null,
        callLogType = null,
    )

    @Test
    fun only_one_prompt_at_a_time() {
        val hub = PostCallEventHub()
        assertTrue(hub.tryPresent(meta("09121234567"), nowMs = 1_000L))
        assertFalse(hub.tryPresent(meta("09129998877"), nowMs = 2_000L))
        assertNotNull(hub.active.value)
        hub.dismiss()
        assertNull(hub.active.value)
    }

    @Test
    fun duplicate_same_event_in_window_is_suppressed() {
        val hub = PostCallEventHub()
        assertTrue(hub.tryPresent(meta("09121234567", PostCallLogic.CallEndKind.Outgoing, 500L), nowMs = 1_000L))
        hub.dismiss()
        assertFalse(
            hub.tryPresent(meta("09121234567", PostCallLogic.CallEndKind.Outgoing, 500L), nowMs = 5_000L),
        )
    }

    @Test
    fun same_phone_different_call_log_is_allowed() {
        val hub = PostCallEventHub()
        assertTrue(hub.tryPresent(meta("09121234567", PostCallLogic.CallEndKind.Outgoing, 500L), nowMs = 1_000L))
        hub.dismiss()
        assertTrue(hub.tryPresent(meta("09121234567", PostCallLogic.CallEndKind.Outgoing, 900L), nowMs = 5_000L))
    }

    @Test
    fun same_phone_different_kind_within_window_is_allowed() {
        val hub = PostCallEventHub()
        assertTrue(hub.tryPresent(meta("09121234567", PostCallLogic.CallEndKind.Missed, 100L), nowMs = 1_000L))
        hub.dismiss()
        assertTrue(hub.tryPresent(meta("09121234567", PostCallLogic.CallEndKind.Answered, 200L), nowMs = 5_000L))
    }
}
