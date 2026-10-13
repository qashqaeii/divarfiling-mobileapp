package ir.divarfiling.mobile.feature.postcall

import ir.divarfiling.mobile.feature.postcall.presentation.PostCallPresentationLedger
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostCallPresentationLedgerTest {

    @Test
    fun duplicate_presentation_is_blocked() {
        val ledger = PostCallPresentationLedger()
        assertTrue(ledger.tryClaim("09121234567", PostCallLogic.CallEndKind.Answered, nowMs = 1_000L))
        assertFalse(ledger.tryClaim("09121234567", PostCallLogic.CallEndKind.Answered, nowMs = 2_000L))
    }
}
