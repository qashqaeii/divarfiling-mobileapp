package ir.divarfiling.mobile.feature.postcall

import ir.divarfiling.mobile.feature.postcall.presentation.PostCallPresentationLedger
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostCallPresentationLedgerTest {

    @Test
    fun failed_presentation_can_be_retried_after_rollback() {
        val ledger = PostCallPresentationLedger()
        assertTrue(ledger.beginPresentation("09121234567", PostCallLogic.CallEndKind.Answered, nowMs = 1_000L))
        ledger.rollbackPresentation("09121234567", PostCallLogic.CallEndKind.Answered)
        assertTrue(ledger.beginPresentation("09121234567", PostCallLogic.CallEndKind.Answered, nowMs = 2_000L))
    }

    @Test
    fun committed_presentation_blocks_duplicate_within_window() {
        val ledger = PostCallPresentationLedger()
        assertTrue(ledger.beginPresentation("09121234567", PostCallLogic.CallEndKind.Outgoing, nowMs = 1_000L))
        ledger.commitPresentation("09121234567", PostCallLogic.CallEndKind.Outgoing, nowMs = 1_000L)
        assertFalse(ledger.beginPresentation("09121234567", PostCallLogic.CallEndKind.Outgoing, nowMs = 5_000L))
    }

    @Test
    fun pending_same_key_allows_single_inflight() {
        val ledger = PostCallPresentationLedger()
        assertTrue(ledger.beginPresentation("09121234567", PostCallLogic.CallEndKind.Missed, nowMs = 1_000L))
        assertTrue(ledger.beginPresentation("09121234567", PostCallLogic.CallEndKind.Missed, nowMs = 1_100L))
        ledger.rollbackPresentation("09121234567", PostCallLogic.CallEndKind.Missed)
    }
}
