package ir.divarfiling.mobile.feature.postcall

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PostCallEventHubTest {

    @Test
    fun only_one_prompt_at_a_time() {
        val hub = PostCallEventHub()
        assertTrue(
            hub.tryPresent("09121234567", PostCallLogic.CallEndKind.Answered, nowMs = 1_000L),
        )
        assertFalse(
            hub.tryPresent("09129998877", PostCallLogic.CallEndKind.Answered, nowMs = 2_000L),
        )
        assertNotNull(hub.active.value)
        hub.dismiss()
        assertNull(hub.active.value)
    }

    @Test
    fun duplicate_phone_in_window_is_suppressed() {
        val hub = PostCallEventHub()
        assertTrue(
            hub.tryPresent("09121234567", PostCallLogic.CallEndKind.Outgoing, nowMs = 1_000L),
        )
        hub.dismiss()
        assertFalse(
            hub.tryPresent("09121234567", PostCallLogic.CallEndKind.Outgoing, nowMs = 5_000L),
        )
    }

    @Test
    fun same_phone_different_kind_within_window_is_allowed() {
        val hub = PostCallEventHub()
        assertTrue(
            hub.tryPresent("09121234567", PostCallLogic.CallEndKind.Missed, nowMs = 1_000L),
        )
        hub.dismiss()
        assertTrue(
            hub.tryPresent("09121234567", PostCallLogic.CallEndKind.Answered, nowMs = 5_000L),
        )
    }
}
