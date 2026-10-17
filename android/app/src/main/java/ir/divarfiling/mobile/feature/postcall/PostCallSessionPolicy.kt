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

    fun hasPendingLeadFollowUp(state: PostCallUiState): Boolean {
        if (!state.leadSaved) return false
        if (state.note.isNotBlank() && !state.noteSaved) return true
        if (!state.leadExtrasSaved && hasLeadExtrasInput(state)) return true
        return false
    }

    fun hasLeadExtrasInput(state: PostCallUiState): Boolean =
        state.leadBudgetMin.isNotBlank() ||
            state.leadBudgetMax.isNotBlank() ||
            state.leadPropertyType.isNotBlank() ||
            state.leadAreas.isNotBlank()

    /** نشست از نظر Pending Inbox بسته شده — حداقل یک ثبت معتبر و بدون پیش‌نویس ذخیره‌نشده. */
    fun isSessionComplete(state: PostCallUiState): Boolean {
        if (hasUnsavedUserInput(state)) return false
        if (hasPendingLeadFollowUp(state)) return false
        val savedSomething =
            state.leadSaved || state.noteSaved || state.outcomeSaved || state.reminderSaved
        if (!savedSomething) return false
        if (state.mode == PostCallUiMode.New) {
            return state.leadSaved && state.leadExtrasSaved &&
                (state.note.isBlank() || state.noteSaved)
        }
        if (state.leadSaved && state.mode == PostCallUiMode.Existing) {
            return state.leadExtrasSaved && (state.note.isBlank() || state.noteSaved)
        }
        return true
    }

    fun shouldEnqueuePendingOnDismiss(state: PostCallUiState): Boolean {
        if (isSessionComplete(state)) return false
        if (state.mode == PostCallUiMode.Invalid) return false
        if (state.phone.isBlank()) return false
        return true
    }
}
