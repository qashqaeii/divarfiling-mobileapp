package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        when {
            state.isResolving -> {
                DfSecondaryButton(
                    text = "بستن",
                    onClick = onDismiss,
                    compact = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            state.mode == PostCallUiMode.Invalid ||
                state.mode == PostCallUiMode.Error ||
                state.mode == PostCallUiMode.Ambiguous -> {
                DfSecondaryButton(
                    text = "بستن",
                    onClick = onDismiss,
                    compact = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            state.mode == PostCallUiMode.Existing -> {
                PostCallExistingFooter(state, onDismiss, onOpenContact, viewModel)
            }
            state.mode == PostCallUiMode.New -> {
                val needsFollowUp = state.leadSaved && !PostCallSessionPolicy.isSessionComplete(state)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    DfPrimaryButton(
                        text = when {
                            state.isSubmittingLead -> "در حال ثبت…"
                            needsFollowUp -> "تلاش دوباره"
                            else -> "ثبت مخاطب"
                        },
                        onClick = viewModel::submitPrimaryAction,
                        enabled = !state.isSubmittingLead &&
                            (needsFollowUp || (!state.leadSaved && state.leadName.isNotBlank())),
                        loading = state.isSubmittingLead,
                        compact = true,
                        modifier = Modifier.weight(1f),
                    )
                    DfSecondaryButton(
                        text = "بعداً",
                        onClick = onDismiss,
                        compact = true,
                        modifier = Modifier.weight(0.42f),
                    )
                }
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
        isMissed -> "ثبت یادآوری"
        hasOutcome || hasNote || state.followUpReminderEnabled -> "ثبت اطلاعات"
        else -> "بستن"
    }

    val showPrimary = canSubmit || (isMissed && !state.reminderSaved)
    if (showPrimary) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            DfPrimaryButton(
                text = primaryLabel,
                onClick = {
                    if (canSubmit) viewModel.submitCallSession() else onDismiss()
                },
                enabled = canSubmit,
                loading = state.isSubmittingSession,
                compact = true,
                modifier = Modifier.weight(1f),
            )
            DfSecondaryButton(
                text = "بعداً",
                onClick = onDismiss,
                compact = true,
                modifier = Modifier.weight(0.42f),
            )
        }
    } else if (!isMissed && contact?.id != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            DfSecondaryButton(
                text = "مشاهده پرونده",
                onClick = { contact.id.let(onOpenContact) },
                compact = true,
                modifier = Modifier.weight(1f),
            )
            DfSecondaryButton(
                text = "بستن",
                onClick = onDismiss,
                compact = true,
                modifier = Modifier.weight(0.42f),
            )
        }
    } else {
        DfSecondaryButton(
            text = "بستن",
            onClick = onDismiss,
            compact = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
