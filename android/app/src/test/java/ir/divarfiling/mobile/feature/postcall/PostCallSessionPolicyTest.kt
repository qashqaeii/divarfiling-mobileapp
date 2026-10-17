package ir.divarfiling.mobile.feature.postcall

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostCallSessionPolicyTest {

    @Test
    fun reminder_only_does_not_complete_when_note_draft_remains() {
        val state = PostCallUiState(
            mode = PostCallUiMode.Existing,
            callEndKind = PostCallLogic.CallEndKind.Missed,
            reminderSaved = true,
            note = "یادداشت ناتمام",
            noteSaved = false,
        )
        assertFalse(PostCallSessionPolicy.isSessionComplete(state))
    }

    @Test
    fun missed_reminder_without_draft_is_complete() {
        val state = PostCallUiState(
            mode = PostCallUiMode.Existing,
            callEndKind = PostCallLogic.CallEndKind.Missed,
            reminderSaved = true,
        )
        assertTrue(PostCallSessionPolicy.isSessionComplete(state))
    }

    @Test
    fun outcome_saved_with_unselected_outcome_draft_is_incomplete() {
        val state = PostCallUiState(
            mode = PostCallUiMode.Existing,
            callEndKind = PostCallLogic.CallEndKind.Answered,
            selectedOutcome = "نیاز به پیگیری",
            outcomeSaved = false,
        )
        assertFalse(PostCallSessionPolicy.isSessionComplete(state))
    }
}
