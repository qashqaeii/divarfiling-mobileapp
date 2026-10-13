package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import ir.divarfiling.mobile.feature.postcall.PostCallUiMode
import ir.divarfiling.mobile.feature.postcall.PostCallViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCallSheetContent(
    phone: String,
    onDismiss: () -> Unit,
    onOpenContact: (Long) -> Unit,
    viewModel: PostCallViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(phone) {
        viewModel.load(phone)
    }

    DfSheetScaffold(
        title = "دستیار پس از تماس",
        subtitle = state.phone,
        onClose = onDismiss,
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
                DfSecondaryButton("بستن", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
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
                DfSecondaryButton("بستن", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
            }
            state.mode == PostCallUiMode.Error -> {
                DfSheetSection("خطا") {
                    Text(
                        state.error ?: "خطا در ارتباط با سرور",
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                }
                DfSecondaryButton("بستن", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
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
                                state.phone,
                                style = AppTypography.labelSmall,
                                color = DfThemeColors.textSecondary(),
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
                }
                DfSheetSection("یادداشت کوتاه") {
                    OutlinedTextField(
                        value = state.note,
                        onValueChange = viewModel::onNoteChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("یادداشت این تماس…") },
                        minLines = 2,
                        enabled = !state.isSubmitting && !state.submitLocked,
                    )
                }
                state.error?.let {
                    Text(it, color = DfThemeColors.error(), style = AppTypography.labelSmall)
                }
                state.successMessage?.let {
                    Text(it, color = DfColors.Purple, style = AppTypography.labelSmall)
                }
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    DfPrimaryButton(
                        text = if (state.isSubmitting) "در حال ثبت…" else "ثبت یادداشت",
                        onClick = viewModel::submitNote,
                        enabled = !state.isSubmitting && !state.submitLocked && state.note.isNotBlank(),
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
                DfSheetSection(title = "شماره") {
                    Text(
                        state.phone,
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "در CRM ثبت نشده",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textSecondary(),
                    )
                }
                DfSheetSection("ثبت سریع") {
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
                    OutlinedTextField(
                        value = state.note,
                        onValueChange = viewModel::onNoteChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("یادداشت کوتاه") },
                        minLines = 2,
                        enabled = !state.isSubmitting && !state.submitLocked,
                    )
                }
                state.error?.let {
                    Text(it, color = DfThemeColors.error(), style = AppTypography.labelSmall)
                }
                state.successMessage?.let {
                    Text(it, color = DfColors.Purple, style = AppTypography.labelSmall)
                }
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
