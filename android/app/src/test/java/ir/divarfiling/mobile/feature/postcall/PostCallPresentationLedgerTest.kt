package ir.divarfiling.mobile.feature.postcall

import ir.divarfiling.mobile.feature.postcall.presentation.PostCallPresentationLedger
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostCallPresentationLedgerTest {

    private fun meta(
        phone: String = "09121234567",
        kind: PostCallLogic.CallEndKind = PostCallLogic.CallEndKind.Answered,
        callLogDateMs: Long = 1000L,
    ) = PostCallCallMetadata(
        phone = phone,
        callEndKind = kind,
        callLogDateMs = callLogDateMs,
        durationSec = null,
        callLogType = null,
    )

    @Test
    fun failed_presentation_can_be_retried_after_rollback() {
        val ledger = PostCallPresentationLedger()
        val m = meta()
        assertTrue(ledger.beginPresentation(m, nowMs = 1_000L))
        ledger.rollbackPresentation(m)
        assertTrue(ledger.beginPresentation(m, nowMs = 2_000L))
    }

    @Test
    fun committed_presentation_blocks_duplicate_within_window() {
        val ledger = PostCallPresentationLedger()
        val m = meta(kind = PostCallLogic.CallEndKind.Outgoing)
        assertTrue(ledger.beginPresentation(m, nowMs = 1_000L))
        ledger.commitPresentation(m, nowMs = 1_000L)
        assertFalse(ledger.beginPresentation(m, nowMs = 5_000L))
    }

    @Test
    fun pending_same_key_blocks_second_inflight() {
        val ledger = PostCallPresentationLedger()
        val m = meta(kind = PostCallLogic.CallEndKind.Missed)
        assertTrue(ledger.beginPresentation(m, nowMs = 1_000L))
        assertFalse(ledger.beginPresentation(m, nowMs = 1_100L))
        ledger.rollbackPresentation(m)
    }

    @Test
    fun same_phone_different_call_log_not_blocked_after_commit() {
        val ledger = PostCallPresentationLedger()
        val first = meta(callLogDateMs = 100L)
        val second = meta(callLogDateMs = 200L)
        assertTrue(ledger.beginPresentation(first, nowMs = 1_000L))
        ledger.commitPresentation(first, nowMs = 1_000L)
        assertTrue(ledger.beginPresentation(second, nowMs = 5_000L))
    }
}
