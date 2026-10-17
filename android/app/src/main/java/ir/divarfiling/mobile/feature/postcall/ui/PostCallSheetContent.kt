package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.feature.ai.voice.VoiceTextField
import ir.divarfiling.mobile.feature.postcall.POST_CALL_LEAD_TYPES
import ir.divarfiling.mobile.feature.postcall.POST_CALL_OUTCOMES
import ir.divarfiling.mobile.feature.postcall.POST_CALL_OUTCOMES_OUTGOING
import ir.divarfiling.mobile.feature.postcall.PostCallUiState
import ir.divarfiling.mobile.feature.postcall.PostCallCallMetadata
import ir.divarfiling.mobile.feature.postcall.PostCallLogic
import ir.divarfiling.mobile.feature.postcall.PostCallUiMode
import ir.divarfiling.mobile.feature.postcall.PostCallUiCopy
import ir.divarfiling.mobile.feature.postcall.PostCallViewModel

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
    val isOutgoing = metadata.callEndKind == PostCallLogic.CallEndKind.Outgoing
    val isAnswered = metadata.callEndKind == PostCallLogic.CallEndKind.Answered
    DfSheetScaffold(
        title = PostCallUiCopy.sheetTitle(state.mode, metadata.callEndKind),
        subtitle = headerSubtitle,
        onClose = onDismiss,
        bodyHeightFraction = PostCallUiCopy.computeAdaptiveSheetHeightFraction(state),
        footer = {
            PostCallStickyActions(
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
                        "یک پرونده را انتخاب کنید. تا انتخاب نکنید، هیچ اطلاعاتی ثبت نمی‌شود.",
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                    state.ambiguousContacts.forEach { c ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = AppSpacing.sm)
                                .clickable { viewModel.selectAmbiguousContact(c) },
                            shape = RoundedCornerShape(14.dp),
                            color = DfThemeColors.surfaceVariant().copy(alpha = 0.45f),
                        ) {
                            Column(modifier = Modifier.padding(AppSpacing.md)) {
                                Text(
                                    c.fullName,
                                    style = AppTypography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    listOfNotNull(
                                        c.customerType.takeIf { it.isNotBlank() },
                                        c.status.takeIf { it.isNotBlank() },
                                    ).joinToString(" · "),
                                    style = AppTypography.labelSmall,
                                    color = DfThemeColors.textSecondary(),
                                    modifier = Modifier.padding(top = AppSpacing.xxs),
                                )
                                Text(
                                    "انتخاب این مخاطب",
                                    style = AppTypography.labelSmall,
                                    color = DfColors.Purple,
                                    modifier = Modifier.padding(top = AppSpacing.xs),
                                )
                            }
                        }
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
                val showOutcomeSection = !isMissed && (isOutgoing || state.expanded || isAnswered)
                if (showOutcomeSection) {
                    val outcomes = if (isOutgoing) POST_CALL_OUTCOMES_OUTGOING else POST_CALL_OUTCOMES
                    DfSheetSection(if (isOutgoing) "نتیجه تماس خروجی" else "نتیجه تماس") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            outcomes.forEach { outcome ->
                                FilterChip(
                                    selected = state.selectedOutcome == outcome,
                                    onClick = { viewModel.onOutcomeSelected(outcome) },
                                    label = { Text(outcome, style = AppTypography.labelSmall) },
                                    enabled = !state.outcomeSaved,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DfColors.PurpleContainer,
                                        selectedLabelColor = DfColors.Purple,
                                    ),
                                )
                            }
                        }
                    }
                } else if (!isMissed && isAnswered) {
                    DfSecondaryButton("نمایش جزئیات", onClick = { viewModel.setExpanded(true) })
                }
                if (isMissed || (!isMissed && (showOutcomeSection || state.expanded))) {
                    val reminderEnabled = !state.reminderSaved && !state.isSubmittingReminder
                    DfSheetSection(if (isMissed) "یادآوری تماس مجدد" else "پیگیری بعدی (اختیاری)") {
                        if (isMissed) {
                            Text(
                                "مکالمه انجام نشده است. زمان پیگیری را انتخاب کنید یا یادداشت کوتاه بنویسید.",
                                style = AppTypography.bodyDescription,
                                color = DfThemeColors.textSecondary(),
                                modifier = Modifier.padding(bottom = AppSpacing.xs),
                            )
                        }
                        PostCallReminderPicker(
                            selectedMillis = state.reminderDueAtMillis
                                ?: PostCallLogic.ReminderScheduling.tomorrowMorning().toEpochMilli(),
                            enabled = reminderEnabled,
                            savedAtMillis = state.savedReminderAtMillis,
                            onQuickOneHour = viewModel::selectQuickReminderOneHour,
                            onQuickTomorrow = viewModel::selectQuickReminderTomorrow,
                            onReminderTimeChange = viewModel::onReminderTimeChange,
                        )
                    }
                }
                PostCallNoteSections(
                    noteHeader = state.noteHeader,
                    note = state.note,
                    onNoteChange = viewModel::onNoteChange,
                    enabled = state.canEditNoteField(),
                    placeholder = if (isMissed) "یادداشت اختیاری…" else "توضیحات این تماس…",
                )
                state.error?.let {
                    Text(it, color = DfThemeColors.error(), style = AppTypography.labelSmall)
                }
                state.partialSuccessMessage?.let {
                    Text(it, color = DfThemeColors.error(), style = AppTypography.labelSmall)
                }
                state.successMessage?.let {
                    Text(it, color = DfColors.Purple, style = AppTypography.labelSmall)
                }
            }
            state.mode == PostCallUiMode.New -> {
                DfSheetSection(title = if (isMissed) "تماس بی‌پاسخ · مخاطب جدید" else "مخاطب جدید") {
                    Text(
                        if (isMissed) {
                            "این شماره در CRM نیست. می‌توانید مخاطب جدید ثبت کنید."
                        } else {
                            "در CRM ثبت نشده — ثبت سریع مخاطب"
                        },
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                    Text(
                        metadata.phone,
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = AppSpacing.xs),
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
                        enabled = !state.isSubmittingLead && !state.leadSaved,
                    )
                    PostCallNoteSections(
                        noteHeader = state.noteHeader,
                        note = state.note,
                        onNoteChange = viewModel::onNoteChange,
                        enabled = state.canEditNoteField(),
                        placeholder = "یادداشت کوتاه",
                    )
                    PostCallQuickLeadExtras(
                        expanded = state.leadExtrasExpanded,
                        budgetMin = state.leadBudgetMin,
                        budgetMax = state.leadBudgetMax,
                        propertyType = state.leadPropertyType,
                        areas = state.leadAreas,
                        enabled = !state.isSubmittingLead && !state.leadSaved,
                        onExpandedChange = viewModel::onLeadExtrasExpandedChange,
                        onBudgetMinChange = viewModel::onLeadBudgetMinChange,
                        onBudgetMaxChange = viewModel::onLeadBudgetMaxChange,
                        onPropertyTypeChange = viewModel::onLeadPropertyTypeChange,
                        onAreasChange = viewModel::onLeadAreasChange,
                    )
                }
                state.error?.let {
                    Text(it, color = DfThemeColors.error(), style = AppTypography.labelSmall)
                }
                state.partialSuccessMessage?.let {
                    Text(it, color = DfThemeColors.error(), style = AppTypography.labelSmall)
                }
                state.successMessage?.let {
                    Text(it, color = DfColors.Purple, style = AppTypography.labelSmall)
                }
            }
        }
    }
}

private fun PostCallUiState.canEditNoteField(): Boolean =
    !isSubmittingSession && !isSubmittingNote && !isSubmittingOutcome && !isSubmittingLead &&
        !noteSaved && !outcomeSaved

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
