package ir.divarfiling.mobile.feature.postcall

/**
 * تعیین تکمیل نشست، کار باقی‌مانده و پاک‌سازی Pending/Draft — منطق خالص برای تست.
 */
object PostCallSessionPolicy {

    fun eventIdentity(metadata: PostCallCallMetadata): String =
        PostCallLogic.ReminderScheduling.pendingCallId(
            metadata.phone,
            metadata.callEndKind,
            metadata.callLogDateMs,
        )

    fun hasUnsavedUserInput(state: PostCallUiState): Boolean {
        val noteDraft = state.note.isNotBlank() && !state.noteSaved
        val outcomeDraft = state.selectedOutcome != null && !state.outcomeSaved
        val leadDraft = state.mode == PostCallUiMode.New &&
            (state.leadName.isNotBlank() || state.note.isNotBlank()) &&
            !state.leadSaved
        val reminderDraft = state.followUpReminderEnabled && !state.reminderSaved
        val missedReminderIntent =
            state.callEndKind == PostCallLogic.CallEndKind.Missed &&
                state.mode == PostCallUiMode.Existing &&
                !state.reminderSaved &&
                (state.note.isNotBlank() || state.followUpReminderEnabled)
        return noteDraft || outcomeDraft || leadDraft || reminderDraft || missedReminderIntent
    }

    fun wantsReminderOnSubmit(state: PostCallUiState): Boolean {
        if (state.reminderSaved) return false
        return when {
            state.callEndKind == PostCallLogic.CallEndKind.Missed &&
                state.mode == PostCallUiMode.Existing -> true
            state.followUpReminderEnabled -> true
            else -> false
        }
    }

    /** نشست از نظر Pending Inbox بسته شده — حداقل یک ثبت معتبر و بدون پیش‌نویس ذخیره‌نشده. */
    fun isSessionComplete(state: PostCallUiState): Boolean {
        if (hasUnsavedUserInput(state)) return false
        val savedSomething =
            state.leadSaved || state.noteSaved || state.outcomeSaved || state.reminderSaved
        if (!savedSomething) return false
        if (state.mode == PostCallUiMode.New) return state.leadSaved
        return true
    }

    fun shouldEnqueuePendingOnDismiss(state: PostCallUiState): Boolean =
        !isSessionComplete(state) &&
            state.mode != PostCallUiMode.Invalid &&
            state.mode != PostCallUiMode.Loading &&
            state.mode != PostCallUiMode.Error
}
