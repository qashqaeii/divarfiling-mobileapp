package ir.divarfiling.mobile.feature.postcall

import ir.divarfiling.mobile.feature.postcall.call.PostCallCallSessionStore
import ir.divarfiling.mobile.feature.postcall.call.PostCallLogic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PostCallCallSessionStoreTest {

    @Test
    fun persisted_outgoing_call_survives_process_death_model() {
        val payload = PostCallCallSessionStore.idlePayloadFromPersisted(
            ringing = false,
            offHook = true,
            startedAtMs = 1_000L,
            hint = null,
        )
        assertNotNull(payload)
        assertEquals(PostCallLogic.CallEndKind.Outgoing, payload!!.snapshot.kind)
    }

    @Test
    fun persisted_missed_call_is_presented() {
        val payload = PostCallCallSessionStore.idlePayloadFromPersisted(
            ringing = true,
            offHook = false,
            startedAtMs = 1_000L,
            hint = "09121234567",
        )
        assertNotNull(payload)
        assertEquals(PostCallLogic.CallEndKind.Missed, payload!!.snapshot.kind)
        assertTrue(PostCallLogic.shouldPresentAfterCall(payload.snapshot.kind))
    }

    @Test
    fun empty_session_returns_null() {
        assertNull(
            PostCallCallSessionStore.idlePayloadFromPersisted(
                ringing = false,
                offHook = false,
                startedAtMs = 0L,
                hint = null,
            ),
        )
    }
}
