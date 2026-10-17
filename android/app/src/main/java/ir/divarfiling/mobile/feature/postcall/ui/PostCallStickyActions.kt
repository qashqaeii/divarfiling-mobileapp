package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.feature.postcall.PostCallLogic
import ir.divarfiling.mobile.feature.postcall.PostCallSessionPolicy
import ir.divarfiling.mobile.feature.postcall.PostCallUiMode
import ir.divarfiling.mobile.feature.postcall.PostCallUiState
import ir.divarfiling.mobile.feature.postcall.PostCallViewModel

@Composable
fun PostCallStickyActions(
    state: PostCallUiState,
    onDismiss: () -> Unit,
    onOpenContact: (Long) -> Unit,
    viewModel: PostCallViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        when {
            state.isResolving -> {
                DfSecondaryButton(text = "بستن", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
            }
            state.mode == PostCallUiMode.Invalid ||
                state.mode == PostCallUiMode.Error ||
                state.mode == PostCallUiMode.Ambiguous -> {
                DfSecondaryButton(text = "بستن", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
            }
            state.mode == PostCallUiMode.Existing -> {
                PostCallExistingFooter(state, onDismiss, onOpenContact, viewModel)
            }
            state.mode == PostCallUiMode.New -> {
                val needsFollowUp = state.leadSaved && !PostCallSessionPolicy.isSessionComplete(state)
                DfPrimaryButton(
                    text = when {
                        state.isSubmittingLead -> "در حال ثبت…"
                        needsFollowUp -> "تلاش دوباره برای ثبت"
                        else -> "ثبت مخاطب"
                    },
                    onClick = viewModel::submitPrimaryAction,
                    enabled = !state.isSubmittingLead &&
                        (needsFollowUp || (!state.leadSaved && state.leadName.isNotBlank())),
                    loading = state.isSubmittingLead,
                )
                DfSecondaryButton(text = "بعداً", onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun PostCallExistingFooter(
    state: PostCallUiState,
    onDismiss: () -> Unit,
    onOpenContact: (Long) -> Unit,
    viewModel: PostCallViewModel,
) {
    val contact = state.contact
    val isMissed = state.callEndKind == PostCallLogic.CallEndKind.Missed
    val hasOutcome = !isMissed && state.selectedOutcome != null
    val hasNote = state.note.isNotBlank()
    val wantsReminder = PostCallSessionPolicy.wantsReminderOnSubmit(state)
    val canSubmit = when {
        state.isSubmittingSession -> false
        isMissed -> !state.reminderSaved && wantsReminder
        hasOutcome -> !state.outcomeSaved
        hasNote -> !state.noteSaved
        state.followUpReminderEnabled -> !state.reminderSaved
        else -> false
    }
    val primaryLabel = when {
        state.isSubmittingSession -> "در حال ثبت…"
        isMissed && state.reminderSaved -> "یادآور ثبت شد"
        PostCallSessionPolicy.isSessionComplete(state) -> "ثبت شد"
        isMissed -> "ثبت یادآوری تماس"
        hasOutcome || hasNote || state.followUpReminderEnabled -> "ثبت اطلاعات تماس"
        else -> "بستن"
    }

    if (canSubmit || (isMissed && !state.reminderSaved)) {
        DfPrimaryButton(
            text = primaryLabel,
            onClick = {
                if (canSubmit) viewModel.submitCallSession() else onDismiss()
            },
            enabled = canSubmit,
            loading = state.isSubmittingSession,
        )
    }
    if (!isMissed && contact?.id != null) {
        DfSecondaryButton(
            text = "مشاهده پرونده",
            onClick = { contact.id.let(onOpenContact) },
        )
    }
    if (!canSubmit && !(isMissed && !state.reminderSaved)) {
        DfSecondaryButton(text = "بستن", onClick = onDismiss)
    } else {
        DfSecondaryButton(text = "بعداً", onClick = onDismiss)
    }
}
