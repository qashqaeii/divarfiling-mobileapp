package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.feature.ai.voice.VoiceTextField
import ir.divarfiling.mobile.feature.postcall.POST_CALL_LEAD_TYPES
import ir.divarfiling.mobile.feature.postcall.POST_CALL_OUTCOMES
import ir.divarfiling.mobile.feature.postcall.PostCallCallMetadata
import ir.divarfiling.mobile.feature.postcall.PostCallLogic
import ir.divarfiling.mobile.feature.postcall.PostCallUiMode
import ir.divarfiling.mobile.feature.postcall.PostCallViewModel

private const val POST_CALL_SHEET_HEIGHT_FRACTION = 0.88f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCallSheetContent(
    metadata: PostCallCallMetadata,
    onDismiss: () -> Unit,
    onOpenContact: (Long) -> Unit,
    viewModel: PostCallViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(metadata) {
        viewModel.load(metadata)
    }

    val headerSubtitle = buildSheetSubtitle(metadata.phone, metadata.callEndKind)
    val isMissed = metadata.callEndKind == PostCallLogic.CallEndKind.Missed

    DfSheetScaffold(
        title = "دستیار پس از تماس",
        subtitle = headerSubtitle,
        onClose = onDismiss,
        bodyHeightFraction = POST_CALL_SHEET_HEIGHT_FRACTION,
        footer = {
            PostCallSheetFooter(
                state = state,
                onDismiss = onDismiss,
                onOpenContact = onOpenContact,
                viewModel = viewModel,
            )
        },
    ) {
        when {
            state.isResolving -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.lg),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(color = DfColors.Purple)
                }
            }
            state.mode == PostCallUiMode.Invalid -> {
                DfSheetSection("شماره نامعتبر") {
                    Text(
                        "شماره تماس قابل شناسایی نیست.",
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                }
            }
            state.mode == PostCallUiMode.Ambiguous -> {
                DfSheetSection("چند مخاطب با این شماره") {
                    Text(
                        "برای جلوگیری از انتخاب اشتباه، از CRM پرونده مناسب را باز کنید.",
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                    state.ambiguousContacts.forEach { c ->
                        Text(
                            "• ${c.fullName} — ${c.customerType}",
                            style = AppTypography.labelSmall,
                            modifier = Modifier.padding(top = AppSpacing.xs),
                        )
                    }
                }
            }
            state.mode == PostCallUiMode.Error -> {
                DfSheetSection("خطا") {
                    Text(
                        state.error ?: "خطا در ارتباط با سرور",
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                    DfSecondaryButton("تلاش دوباره", onClick = viewModel::retryResolve)
                }
            }
            state.mode == PostCallUiMode.Existing -> {
                val contact = state.contact
                DfSheetSection(title = "مخاطب") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            color = DfThemeColors.primaryContainer(),
                        ) {
                            Icon(
                                DfIcons.User,
                                contentDescription = null,
                                tint = DfThemeColors.primary(),
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                contact?.fullName ?: "—",
                                style = AppTypography.cardTitle,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "موجود در CRM",
                                style = AppTypography.labelSmall,
                                color = DfColors.Purple,
                                modifier = Modifier.padding(top = AppSpacing.xxs),
                            )
                        }
                    }
                    contact?.customerType?.takeIf { it.isNotBlank() }?.let {
                        MetaLine("نوع", it)
                    }
                    contact?.status?.takeIf { it.isNotBlank() }?.let {
                        MetaLine("وضعیت", it)
                    }
                    contact?.lastFollowUp?.summary?.takeIf { it.isNotBlank() }?.let {
                        MetaLine("آخرین پیگیری", it)
                    }
                    contact?.areasSummary?.takeIf { it.isNotBlank() }?.let {
                        MetaLine("محله", it)
                    }
                    contact?.budgetSummary?.takeIf { it.isNotBlank() }?.let {
                        MetaLine("بودجه", it)
                    }
                }
                if (!isMissed && state.expanded) {
                    DfSheetSection("نتیجه تماس") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            POST_CALL_OUTCOMES.forEach { outcome ->
                                FilterChip(
                                    selected = state.selectedOutcome == outcome,
                                    onClick = { viewModel.onOutcomeSelected(outcome) },
                                    label = { Text(outcome, style = AppTypography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DfColors.PurpleContainer,
                                        selectedLabelColor = DfColors.Purple,
                                    ),
                                )
                            }
                        }
                    }
                    DfSheetSection("یادآور سریع") {
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            DfSecondaryButton("۱ ساعت", onClick = { viewModel.submitQuickReminder(1) })
                            DfSecondaryButton("فردا", onClick = { viewModel.submitQuickReminder(24) })
                        }
                    }
                } else if (!isMissed) {
                    DfSecondaryButton("نمایش جزئیات", onClick = { viewModel.setExpanded(true) })
                }
                if (isMissed) {
                    DfSheetSection("یادآوری تماس مجدد") {
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            DfSecondaryButton("۱ ساعت", onClick = { viewModel.submitQuickReminder(1) })
                            DfSecondaryButton("فردا", onClick = { viewModel.submitQuickReminder(24) })
                        }
                    }
                }
                PostCallNoteSections(
                    noteHeader = state.noteHeader,
                    note = state.note,
                    onNoteChange = viewModel::onNoteChange,
                    enabled = !state.isSubmitting && !state.submitLocked,
                    placeholder = if (isMissed) "یادداشت اختیاری…" else "توضیحات این تماس…",
                )
                state.error?.let {
                    Text(it, color = DfThemeColors.error(), style = AppTypography.labelSmall)
                }
                state.successMessage?.let {
                    Text(it, color = DfColors.Purple, style = AppTypography.labelSmall)
                }
            }
            state.mode == PostCallUiMode.New -> {
                DfSheetSection(title = "وضعیت CRM") {
                    Text(
                        "در CRM ثبت نشده",
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                }
                DfSheetSection("ثبت سریع") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        POST_CALL_LEAD_TYPES.forEach { type ->
                            FilterChip(
                                selected = state.leadCustomerType == type,
                                onClick = { viewModel.onLeadTypeChange(type) },
                                label = { Text(type, style = AppTypography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DfColors.PurpleContainer,
                                    selectedLabelColor = DfColors.Purple,
                                ),
                            )
                        }
                    }
                    OutlinedTextField(
                        value = state.leadName,
                        onValueChange = viewModel::onLeadNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("نام مخاطب") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                        ),
                        enabled = !state.isSubmitting && !state.submitLocked,
                    )
                    PostCallNoteSections(
                        noteHeader = state.noteHeader,
                        note = state.note,
                        onNoteChange = viewModel::onNoteChange,
                        enabled = !state.isSubmitting && !state.submitLocked,
                        placeholder = "یادداشت کوتاه",
                    )
                }
                state.error?.let {
                    Text(it, color = DfThemeColors.error(), style = AppTypography.labelSmall)
                }
                state.successMessage?.let {
                    Text(it, color = DfColors.Purple, style = AppTypography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun PostCallSheetFooter(
    state: ir.divarfiling.mobile.feature.postcall.PostCallUiState,
    onDismiss: () -> Unit,
    onOpenContact: (Long) -> Unit,
    viewModel: PostCallViewModel,
) {
    when {
        state.isResolving -> {
            DfSecondaryButton(text = "بستن", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
        }
        state.mode == PostCallUiMode.Invalid ||
            state.mode == PostCallUiMode.Ambiguous ||
            state.mode == PostCallUiMode.Error -> {
            DfSecondaryButton(text = "بستن", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
        }
        state.mode == PostCallUiMode.Existing -> {
            val contact = state.contact
            val isMissed = state.callEndKind == PostCallLogic.CallEndKind.Missed
            val primaryOutcome = !isMissed && state.selectedOutcome != null
            val canSaveNote = state.note.isNotBlank() || state.noteHeader.isNotBlank()
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                DfPrimaryButton(
                    text = if (state.isSubmitting) {
                        "در حال ثبت…"
                    } else if (primaryOutcome) {
                        "ثبت نتیجه تماس"
                    } else {
                        "ثبت یادداشت"
                    },
                    onClick = if (primaryOutcome) viewModel::submitCallOutcome else viewModel::submitNote,
                    enabled = !state.isSubmitting && !state.submitLocked &&
                        (primaryOutcome || canSaveNote),
                    loading = state.isSubmitting,
                )
                DfSecondaryButton(
                    text = "مشاهده پرونده",
                    onClick = { contact?.id?.let(onOpenContact) },
                    enabled = contact?.id != null,
                )
                DfSecondaryButton(text = "بستن", onClick = onDismiss)
            }
        }
        state.mode == PostCallUiMode.New -> {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                DfPrimaryButton(
                    text = if (state.isSubmitting) "در حال ثبت…" else "ثبت مخاطب",
                    onClick = viewModel::submitQuickLead,
                    enabled = !state.isSubmitting && !state.submitLocked && state.leadName.isNotBlank(),
                    loading = state.isSubmitting,
                )
                DfSecondaryButton(text = "فعلاً نه", onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun MetaLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
        Text(value, style = AppTypography.labelSmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PostCallNoteSections(
    noteHeader: String,
    note: String,
    onNoteChange: (String) -> Unit,
    enabled: Boolean,
    placeholder: String,
) {
    if (noteHeader.isNotBlank()) {
        DfSheetSection("اطلاعات تماس") {
            Text(
                noteHeader,
                style = AppTypography.labelSmall,
                color = DfThemeColors.textSecondary(),
            )
        }
    }
    DfSheetSection("توضیحات") {
        VoiceTextField(
            value = note,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder,
            enabled = enabled,
            minLines = 2,
        )
    }
}

private fun buildSheetSubtitle(phone: String, kind: PostCallLogic.CallEndKind): String {
    val kindLabel = when (kind) {
        PostCallLogic.CallEndKind.Missed -> "تماس بی‌پاسخ"
        PostCallLogic.CallEndKind.Outgoing -> "تماس خروجی"
        PostCallLogic.CallEndKind.Answered -> "تماس ورودی"
        PostCallLogic.CallEndKind.Ignored -> "تماس"
    }
    return "$kindLabel · $phone"
}
