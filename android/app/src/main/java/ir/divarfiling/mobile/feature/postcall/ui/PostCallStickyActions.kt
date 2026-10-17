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
                DfPrimaryButton(
                    text = if (state.isSubmittingLead) "در حال ثبت…" else "ثبت مخاطب",
                    onClick = viewModel::submitQuickLead,
                    enabled = !state.isSubmittingLead && !state.leadSaved && state.leadName.isNotBlank(),
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
    val hasNote = state.note.isNotBlank() || state.noteHeader.isNotBlank()
    val canSubmitPrimary = when {
        isMissed -> !state.reminderSaved && !state.isSubmittingReminder
        hasOutcome -> !state.outcomeSaved && !state.isSubmittingOutcome
        hasNote -> !state.noteSaved && !state.isSubmittingNote && state.selectedOutcome == null
        else -> false
    }
    val primaryLabel = when {
        state.isSubmittingOutcome || state.isSubmittingNote || state.isSubmittingReminder ->
            "در حال ثبت…"
        isMissed && state.reminderSaved -> "یادآور ثبت شد"
        isMissed -> "تنظیم یادآوری"
        hasOutcome -> "ثبت نتیجه تماس"
        hasNote -> "ثبت یادداشت"
        else -> "بستن"
    }
    val primaryLoading = state.isSubmittingOutcome || state.isSubmittingNote || state.isSubmittingReminder

    if (canSubmitPrimary || (isMissed && !state.reminderSaved)) {
        DfPrimaryButton(
            text = primaryLabel,
            onClick = viewModel::submitPrimaryAction,
            enabled = canSubmitPrimary && !primaryLoading,
            loading = primaryLoading,
        )
    }
    if (!isMissed && contact?.id != null) {
        DfSecondaryButton(
            text = "مشاهده پرونده",
            onClick = { contact.id.let(onOpenContact) },
        )
    }
    if (!canSubmitPrimary && !(isMissed && !state.reminderSaved)) {
        DfSecondaryButton(text = "بستن", onClick = onDismiss)
    } else {
        DfSecondaryButton(text = "بعداً", onClick = onDismiss)
    }
}
