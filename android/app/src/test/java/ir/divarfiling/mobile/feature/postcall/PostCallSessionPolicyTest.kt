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

    @Test
    fun enqueue_on_network_error_with_valid_phone() {
        val state = PostCallUiState(
            phone = "09121234567",
            mode = PostCallUiMode.Error,
            isResolving = false,
        )
        assertTrue(PostCallSessionPolicy.shouldEnqueuePendingOnDismiss(state))
    }

    @Test
    fun enqueue_while_loading_if_phone_known() {
        val state = PostCallUiState(
            phone = "09121234567",
            mode = PostCallUiMode.Loading,
            isResolving = true,
        )
        assertTrue(PostCallSessionPolicy.shouldEnqueuePendingOnDismiss(state))
    }

    @Test
    fun do_not_enqueue_invalid_phone() {
        val state = PostCallUiState(
            phone = "09121234567",
            mode = PostCallUiMode.Invalid,
        )
        assertFalse(PostCallSessionPolicy.shouldEnqueuePendingOnDismiss(state))
    }

    @Test
    fun partial_lead_with_failed_note_is_not_session_complete() {
        val state = PostCallUiState(
            phone = "09121234567",
            mode = PostCallUiMode.Existing,
            leadSaved = true,
            note = "یادداشت",
            noteSaved = false,
            leadExtrasSaved = true,
        )
        assertFalse(PostCallSessionPolicy.isSessionComplete(state))
        assertTrue(PostCallSessionPolicy.hasPendingLeadFollowUp(state))
    }

    @Test
    fun partial_lead_with_failed_extras_is_not_session_complete() {
        val state = PostCallUiState(
            phone = "09121234567",
            mode = PostCallUiMode.Existing,
            leadSaved = true,
            leadExtrasSaved = false,
            leadBudgetMin = "۱٬۰۰۰٬۰۰۰",
        )
        assertFalse(PostCallSessionPolicy.isSessionComplete(state))
    }
}
