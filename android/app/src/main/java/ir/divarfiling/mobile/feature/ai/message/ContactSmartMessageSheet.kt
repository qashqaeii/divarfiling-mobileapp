package ir.divarfiling.mobile.feature.ai.message



import android.content.ClipData

import android.content.ClipboardManager

import android.content.Context

import android.content.Intent

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.ExperimentalLayoutApi

import androidx.compose.foundation.layout.FlowRow

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.padding

import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import ir.divarfiling.mobile.core.design.AppSpacing

import ir.divarfiling.mobile.core.design.AppTypography

import ir.divarfiling.mobile.core.design.DfColors

import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors

import ir.divarfiling.mobile.core.design.components.DfCard

import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet

import ir.divarfiling.mobile.core.design.components.DfSecondaryButton

import ir.divarfiling.mobile.core.design.components.DfSheetActions

import ir.divarfiling.mobile.core.design.components.DfSheetScaffold

import ir.divarfiling.mobile.core.design.components.DfSheetSection

import ir.divarfiling.mobile.core.design.components.DfSoftChip

import ir.divarfiling.mobile.feature.ai.voice.VoiceTextField



@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

@Composable

fun ContactSmartMessageSheet(

    contactId: Long,

    contactName: String,

    visible: Boolean,

    onDismiss: () -> Unit,

    viewModel: ContactSmartMessageViewModel = hiltViewModel(

        key = "crm_msg_$contactId",

    ),

) {

    if (!visible) return

    LaunchedEffect(contactId, contactName) {

        viewModel.bind(contactId, contactName)

    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current



    DfModalBottomSheet(onDismissRequest = onDismiss) {

        DfSheetScaffold(

            title = "ساخت پیام با دستیار هوشمند",

            subtitle = contactName.ifBlank { "پیش‌نویس برای مخاطب" },

            icon = DfIcons.MessageSquare,

            onClose = onDismiss,

            footer = {

                DfSheetActions(

                    primaryText = if (state.isGenerating) "در حال ساخت…" else "ساخت پیام",

                    onPrimary = viewModel::generate,

                    primaryEnabled = state.canUse && !state.isGenerating,

                    isSubmitting = state.isGenerating,

                    secondaryText = "انصراف",

                    onSecondary = onDismiss,

                )

            },

        ) {

            state.quotaHint?.let {

                Text(it, style = AppTypography.labelSmall, color = DfColors.TextMuted, modifier = Modifier.padding(bottom = AppSpacing.xs))

            }

            state.blockMessage?.let {

                DfCard(modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.sm)) {

                    Text(

                        it,

                        style = AppTypography.bodyDescription,

                        color = DfColors.TextSecondary,

                        modifier = Modifier.padding(AppSpacing.cardPadding),

                    )

                }

            }

            state.error?.let {

                Text(

                    it.ifBlank { "ساخت پیام انجام نشد. دوباره امتحان کنید." },

                    style = AppTypography.bodyDescription,

                    color = DfThemeColors.error(),

                    modifier = Modifier.fillMaxWidth(),

                )

                DfSecondaryButton(

                    text = "تلاش دوباره",

                    onClick = viewModel::generate,

                    enabled = state.canUse && !state.isGenerating,

                    modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.xs),

                )

            }

            if (state.tones.isNotEmpty()) {

                DfSheetSection(title = "لحن پیام") {

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {

                        state.tones.forEach { tone ->

                            DfSoftChip(

                                text = tone.label.ifBlank { tone.id },

                                selected = tone.id == state.selectedToneId,

                                onClick = { viewModel.onToneSelected(tone.id) },

                            )

                        }

                    }

                }

            }

            DfSheetSection(title = "نکته (اختیاری)") {

                VoiceTextField(

                    value = state.notes,

                    onValueChange = viewModel::onNotesChange,

                    placeholder = "مثلاً: پیگیری بازدید دیروز",

                    singleLine = false,

                    minLines = 2,

                )

            }

            DfSheetSection(title = "پیش‌نمایش") {

                if (state.isGenerating) {

                    Column(

                        modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.lg),

                        horizontalAlignment = Alignment.CenterHorizontally,

                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),

                    ) {

                        CircularProgressIndicator(strokeWidth = 2.dp)

                        Text("در حال ساخت پیام…", style = AppTypography.bodyDescription, color = DfColors.TextSecondary)

                    }

                } else if (state.draftText.isNotBlank()) {

                    DfCard(modifier = Modifier.fillMaxWidth()) {

                        Text(

                            state.draftText,

                            style = AppTypography.body,

                            modifier = Modifier.padding(AppSpacing.cardPadding),

                        )

                    }

                    DfSecondaryButton(

                        text = "کپی",

                        onClick = { copyToClipboard(context, state.draftText) },

                        modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.sm),

                    )

                    DfSecondaryButton(

                        text = "اشتراک‌گذاری",

                        onClick = { shareText(context, state.draftText) },

                        modifier = Modifier.fillMaxWidth(),

                    )

                } else {

                    Text(

                        "پس از «ساخت پیام»، متن اینجا نمایش داده می‌شود.",

                        style = AppTypography.bodyDescription,

                        color = DfColors.TextMuted,

                    )

                }

            }

        }

    }

}



private fun copyToClipboard(context: Context, text: String) {

    if (text.isBlank()) return

    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    cm.setPrimaryClip(ClipData.newPlainText("crm_message", text))

}



private fun shareText(context: Context, text: String) {

    if (text.isBlank()) return

    val intent = Intent(Intent.ACTION_SEND).apply {

        type = "text/plain"

        putExtra(Intent.EXTRA_TEXT, text)

    }

    context.startActivity(Intent.createChooser(intent, "اشتراک پیام"))

}


