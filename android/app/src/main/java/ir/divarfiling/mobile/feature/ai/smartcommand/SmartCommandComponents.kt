package ir.divarfiling.mobile.feature.ai.smartcommand

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.network.NlContactCandidateDto
import ir.divarfiling.mobile.feature.ai.voice.SpeechErrorMapper
import ir.divarfiling.mobile.feature.ai.voice.SpeechRecognizerManager
import ir.divarfiling.mobile.feature.ai.voice.VoiceInputPhase
import ir.divarfiling.mobile.feature.ai.voice.VoiceMicButton
import ir.divarfiling.mobile.feature.ai.voice.VoiceTextField
import ir.divarfiling.mobile.feature.ai.voice.openAppSettingsForMic

@Composable
fun SmartCommandHost(
    profile: SmartCommandProfileKey,
    modifier: Modifier = Modifier,
    onNavigateContact: (Long) -> Unit,
    onNavigateProperty: (Long) -> Unit,
    onNavigateToday: () -> Unit = {},
    viewModel: SmartCommandViewModel = hiltViewModel(key = "smart_command_${profile.name}"),
) {
    LaunchedEffect(profile) { viewModel.bindProfile(profile) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val blockMessage = blockReasonMessage(state)
    val showDisabled = state.blockReason != null

    SmartCommandEntryCard(
        title = state.profile?.title?.ifBlank { "دستیار هوشمند" } ?: "دستیار هوشمند",
        subtitle = "کاری که می‌خوای انجام بدی رو بنویس یا بگو",
        placeholder = state.profile?.placeholder.orEmpty(),
        quotaHint = state.quotaHint,
        enabled = !showDisabled,
        disabledMessage = blockMessage,
        modifier = modifier,
        onOpen = viewModel::openSheet,
    )

    if (state.isSheetOpen) {
        SmartCommandSheet(
            state = state,
            viewModel = viewModel,
            onDismiss = viewModel::dismissSheet,
            onNavigateContact = onNavigateContact,
            onNavigateProperty = onNavigateProperty,
            onNavigateToday = onNavigateToday,
        )
    }
}

@Composable
private fun SmartCommandEntryCard(
    title: String,
    subtitle: String,
    placeholder: String,
    quotaHint: String?,
    enabled: Boolean,
    disabledMessage: String?,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit,
) {
    DfCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal)
            .then(if (enabled) Modifier.clickable(onClick = onOpen) else Modifier),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(DfIcons.Sparkles, contentDescription = null, tint = DfColors.Purple)
                Text(title, style = AppTypography.sectionTitle, color = DfColors.TextPrimary)
            }
            Text(subtitle, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
            Text(
                placeholder.ifBlank { "مثلاً: فردا ساعت ۱۰ یادم بنداز با احمدی تماس بگیرم" },
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
            quotaHint?.let {
                Text(it, style = AppTypography.labelSmall, color = DfColors.TextMuted)
            }
            if (!enabled && !disabledMessage.isNullOrBlank()) {
                Text(disabledMessage, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
            } else if (enabled) {
                Text("برای شروع ضربه بزنید", style = AppTypography.labelSmall, color = DfColors.Purple)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmartCommandSheet(
    state: SmartCommandUiState,
    viewModel: SmartCommandViewModel,
    onDismiss: () -> Unit,
    onNavigateContact: (Long) -> Unit,
    onNavigateProperty: (Long) -> Unit,
    onNavigateToday: () -> Unit,
) {
    val context = LocalContext.current
    val speechManager = remember { SpeechRecognizerManager(context.applicationContext) }
    val sttAvailable = remember { speechManager.isAvailable() }
    var pendingMic by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) pendingMic = true
        else viewModel.onVoiceError(SpeechErrorMapper.permissionDenied())
    }

    DisposableEffect(Unit) {
        onDispose { speechManager.destroy() }
    }

    if (pendingMic) {
        pendingMic = false
        startSmartCommandVoice(speechManager, viewModel)
    }

    DfModalBottomSheet(onDismissRequest = onDismiss) {
        when (state.phase) {
            SmartCommandPhase.Success -> SuccessStep(
                state = state,
                onNavigateContact = onNavigateContact,
                onNavigateProperty = onNavigateProperty,
                onNavigateToday = onNavigateToday,
                onClose = {
                    viewModel.resetFlow()
                    onDismiss()
                },
            )
            SmartCommandPhase.ResolvingContact -> ContactResolveStep(
                candidates = state.parseResult?.contactCandidates.orEmpty(),
                onSelect = viewModel::selectContactCandidate,
                onCancel = { viewModel.resetFlow() },
            )
            else -> InputAndPreviewStep(
                state = state,
                sttAvailable = sttAvailable,
                onDismiss = onDismiss,
                onInputChange = viewModel::onInputChange,
                onParse = { viewModel.parse() },
                onConfirm = viewModel::confirm,
                onReminderChange = viewModel::updateReminderEdit,
                onContactChange = viewModel::updateContactEdit,
                onPropertyChange = viewModel::updatePropertyEdit,
                onMicClick = {
                    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED
                    if (!granted) {
                        viewModel.onVoicePhase(VoiceInputPhase.RequestingPermission)
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        startSmartCommandVoice(speechManager, viewModel)
                    }
                },
                onMicStop = { speechManager.stopListening() },
                onMicCancel = {
                    speechManager.cancel()
                    viewModel.onVoicePhase(VoiceInputPhase.Idle)
                },
                onOpenSettings = { openAppSettingsForMic(context) },
                onClearPreview = viewModel::clearPreviewKeepInput,
            )
        }
    }
}

@Composable
private fun InputAndPreviewStep(
    state: SmartCommandUiState,
    sttAvailable: Boolean,
    onDismiss: () -> Unit,
    onInputChange: (String) -> Unit,
    onParse: () -> Unit,
    onConfirm: () -> Unit,
    onReminderChange: (ReminderPreviewEdit) -> Unit,
    onContactChange: (ContactPreviewEdit) -> Unit,
    onPropertyChange: (PropertyPreviewEdit) -> Unit,
    onMicClick: () -> Unit,
    onMicStop: () -> Unit,
    onMicCancel: () -> Unit,
    onOpenSettings: () -> Unit,
    onClearPreview: () -> Unit,
) {
    val showConfirm = state.phase == SmartCommandPhase.Preview &&
        state.parseResult?.intent != SmartCommandMapping.INTENT_UNKNOWN &&
        state.parseResult?.canConfirm == true
    val showUnknown = state.phase == SmartCommandPhase.Preview &&
        state.parseResult?.intent == SmartCommandMapping.INTENT_UNKNOWN
    DfSheetScaffold(
        title = state.profile?.title?.ifBlank { "دستیار هوشمند" } ?: "دستیار هوشمند",
        subtitle = "درخواست خود را بگویید یا بنویسید",
        icon = DfIcons.Sparkles,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = when {
                    state.confirmInFlight -> "در حال ثبت…"
                    showConfirm -> "تأیید و ثبت"
                    state.phase == SmartCommandPhase.Parsing -> "در حال بررسی…"
                    else -> "بررسی درخواست"
                },
                onPrimary = if (showConfirm) onConfirm else onParse,
                primaryEnabled = !state.confirmInFlight &&
                    state.phase != SmartCommandPhase.Parsing &&
                    state.blockReason == null &&
                    state.inputText.isNotBlank() &&
                    !showUnknown,
                isSubmitting = state.phase == SmartCommandPhase.Parsing || state.confirmInFlight,
                secondaryText = if (showConfirm || showUnknown) "ویرایش درخواست" else "انصراف",
                onSecondary = if (showConfirm || showUnknown) onClearPreview else onDismiss,
            )
        },
    ) {
        state.quotaHint?.let {
            Text(it, style = AppTypography.labelSmall, color = DfColors.TextMuted, modifier = Modifier.padding(bottom = AppSpacing.xs))
        }
        state.blockReason?.let {
            DfCard(modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.sm)) {
                Text(
                    blockReasonMessage(state).orEmpty(),
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextSecondary,
                    modifier = Modifier.padding(AppSpacing.cardPadding),
                )
            }
        }
        state.errorMessage?.let { DfErrorBanner(it, modifier = Modifier.fillMaxWidth()) }
        if (state.voicePhase == VoiceInputPhase.RequestingPermission) {
            Text(
                "برای ثبت فرمان صوتی، دسترسی میکروفن لازم است.",
                style = AppTypography.bodyDescription,
                color = DfColors.TextSecondary,
                modifier = Modifier.padding(bottom = AppSpacing.sm),
            )
        }
        if (state.phase == SmartCommandPhase.Parsing) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Text("در حال بررسی درخواست…", style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
            }
        }
        DfSheetSection(title = if (state.parseResult != null && !showUnknown) "بررسی قبل از ثبت" else "درخواست شما") {
            DfTextField(
                value = state.inputText,
                onValueChange = onInputChange,
                placeholder = state.profile?.placeholder ?: "فرمان خود را بنویسید…",
                singleLine = false,
                minLines = 2,
                maxLines = 5,
                trailingIcon = if (sttAvailable) {
                    {
                        VoiceMicButton(
                            phase = state.voicePhase,
                            onClick = {
                                if (state.voicePhase == VoiceInputPhase.Listening) onMicStop() else onMicClick()
                            },
                            onCancel = onMicCancel,
                        )
                    }
                } else {
                    null
                },
                helperText = when {
                    state.voicePhase == VoiceInputPhase.Listening -> "در حال گوش دادن…"
                    state.voicePhase == VoiceInputPhase.Error && state.voiceError != null -> state.voiceError
                    else -> null
                },
            )
            if (state.voicePhase == VoiceInputPhase.Listening) {
                Text("دارم گوش می‌دم…", style = AppTypography.labelSmall, color = DfColors.Purple)
            }
            Text(
                "${state.inputText.length} / ${SmartCommandMapping.MAX_INPUT_CHARS}",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
        }
        if (showUnknown) {
            DfCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(AppSpacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Text("درخواستت رو کامل متوجه نشدم", style = AppTypography.sectionTitle)
                    Text(
                        state.statusMessage ?: "مثلاً: فردا ساعت ۱۰ یادم بنداز با احمدی تماس بگیرم.",
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                    )
                    DfSecondaryButton(
                        text = "دوباره امتحان می‌کنم",
                        onClick = onClearPreview,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        when (state.parseResult?.intent) {
            SmartCommandMapping.INTENT_REMINDER -> state.reminderEdit?.let { edit ->
                ReminderPreviewSection(edit, onReminderChange)
            }
            SmartCommandMapping.INTENT_CONTACT -> state.contactEdit?.let { edit ->
                ContactPreviewSection(edit, onContactChange)
            }
            SmartCommandMapping.INTENT_PROPERTY -> state.propertyEdit?.let { edit ->
                PropertyPreviewSection(edit, onPropertyChange)
            }
        }
        if (state.voicePhase == VoiceInputPhase.Error && state.voiceError?.contains("دسترسی") == true) {
            DfSecondaryButton(text = "رفتن به تنظیمات", onClick = onOpenSettings, modifier = Modifier.fillMaxWidth())
        } else if (state.voicePhase == VoiceInputPhase.Error && !state.voiceError.isNullOrBlank()) {
            DfSecondaryButton(
                text = "تلاش دوباره",
                onClick = onMicClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ReminderPreviewSection(edit: ReminderPreviewEdit, onChange: (ReminderPreviewEdit) -> Unit) {
    DfSheetSection(title = "یادآوری") {
        SmartCommandPreviewRow("مخاطب", edit.contactName)
        SmartCommandPreviewRow("زمان", edit.dueAtDisplay.ifBlank { "${edit.dateText} ${edit.timeText}".trim() })
        SmartCommandPreviewRow("موضوع", edit.title)
        DfTextField(edit.contactName, { onChange(edit.copy(contactName = it)) }, label = "ویرایش مخاطب")
        DfTextField(edit.title, { onChange(edit.copy(title = it)) }, label = "ویرایش موضوع")
        DfTextField(edit.dateText, { onChange(edit.copy(dateText = it)) }, label = "تاریخ")
        DfTextField(edit.timeText, { onChange(edit.copy(timeText = it)) }, label = "ساعت")
        VoiceTextField(
            value = edit.note,
            onValueChange = { onChange(edit.copy(note = it)) },
            label = "توضیح",
            singleLine = false,
            minLines = 2,
        )
    }
}

@Composable
private fun ContactPreviewSection(edit: ContactPreviewEdit, onChange: (ContactPreviewEdit) -> Unit) {
    DfSheetSection(title = "مخاطب") {
        SmartCommandPreviewRow("نام", edit.name)
        SmartCommandPreviewRow("شماره", edit.phone)
        DfTextField(edit.name, { onChange(edit.copy(name = it)) }, label = "نام")
        DfTextField(edit.phone, { onChange(edit.copy(phone = it)) }, label = "شماره")
        DfTextField(edit.customerType, { onChange(edit.copy(customerType = it)) }, label = "نوع مخاطب")
        VoiceTextField(
            value = edit.notes,
            onValueChange = { onChange(edit.copy(notes = it)) },
            label = "توضیحات",
            singleLine = false,
            minLines = 2,
        )
    }
}

@Composable
private fun PropertyPreviewSection(edit: PropertyPreviewEdit, onChange: (PropertyPreviewEdit) -> Unit) {
    DfSheetSection(title = "فایل شخصی") {
        SmartCommandPreviewRow("نوع ملک", edit.propertyType)
        SmartCommandPreviewRow("معامله", edit.dealMode)
        SmartCommandPreviewRow("منطقه", edit.neighborhood)
        SmartCommandPreviewRow("متراژ", edit.area)
        SmartCommandPreviewRow("قیمت", edit.salePrice.ifBlank { edit.monthlyRent.ifBlank { edit.deposit } })
        DfTextField(edit.propertyType, { onChange(edit.copy(propertyType = it)) }, label = "نوع ملک")
        DfTextField(edit.dealMode, { onChange(edit.copy(dealMode = it)) }, label = "نوع معامله")
        DfTextField(edit.neighborhood, { onChange(edit.copy(neighborhood = it)) }, label = "منطقه")
        DfTextField(edit.area, { onChange(edit.copy(area = it)) }, label = "متراژ")
        DfTextField(edit.salePrice, { onChange(edit.copy(salePrice = it)) }, label = "قیمت")
        VoiceTextField(
            value = edit.notes,
            onValueChange = { onChange(edit.copy(notes = it)) },
            label = "توضیحات",
            singleLine = false,
            minLines = 2,
        )
    }
}

@Composable
private fun SmartCommandPreviewRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
        Text(value, style = AppTypography.bodyDescription, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ContactResolveStep(
    candidates: List<NlContactCandidateDto>,
    onSelect: (Long) -> Unit,
    onCancel: () -> Unit,
) {
    DfSheetScaffold(
        title = "کدام مخاطب منظورتان است؟",
        subtitle = "یکی را انتخاب کنید",
        icon = DfIcons.Users,
        onClose = onCancel,
    ) {
        LazyColumn(contentPadding = PaddingValues(bottom = AppSpacing.xl), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            items(candidates, key = { it.id }) { candidate ->
                DfCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(candidate.id) },
                ) {
                    Row(
                        modifier = Modifier.padding(AppSpacing.cardPadding),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DfBadge(text = candidate.name.take(1).ifBlank { "?" })
                        Column(Modifier.weight(1f)) {
                            Text(candidate.name, style = AppTypography.sectionTitle)
                            if (candidate.phone.isNotBlank()) {
                                Text(
                                    maskPhone(candidate.phone),
                                    style = AppTypography.bodyDescription,
                                    color = DfColors.TextSecondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessStep(
    state: SmartCommandUiState,
    onNavigateContact: (Long) -> Unit,
    onNavigateProperty: (Long) -> Unit,
    onNavigateToday: () -> Unit,
    onClose: () -> Unit,
) {
    val successTitle = when (state.successEntityType) {
        "contact" -> "مخاطب با موفقیت ثبت شد"
        "property" -> "فایل شخصی ثبت شد"
        "reminder" -> "یادآوری ثبت شد"
        else -> "با موفقیت ثبت شد"
    }
    DfSheetScaffold(
        title = successTitle,
        subtitle = state.successMessage ?: "",
        icon = DfIcons.CircleCheck,
        onClose = onClose,
        footer = {
            val id = state.successEntityId
            DfSheetActions(
                primaryText = when (state.successEntityType) {
                    "contact" -> "مشاهده مخاطب"
                    "property" -> "مشاهده فایل"
                    "reminder" -> "مشاهده"
                    else -> "تمام"
                },
                onPrimary = {
                    when (state.successEntityType) {
                        "contact" -> id?.let(onNavigateContact)
                        "property" -> id?.let(onNavigateProperty)
                        "reminder" -> onNavigateToday()
                    }
                    onClose()
                },
                secondaryText = "تمام",
                onSecondary = onClose,
            )
        },
    ) {
        Text("می‌توانید همین حالا نتیجه را ببینید یا بعداً از CRM پیگیری کنید.", style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
    }
}

private fun maskPhone(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    if (digits.length < 4) return phone
    val tail = digits.takeLast(4)
    return "••• ${digits.take(3)} ••• $tail"
}

private fun startSmartCommandVoice(
    manager: SpeechRecognizerManager,
    viewModel: SmartCommandViewModel,
) {
    if (!manager.isAvailable()) return
    viewModel.onVoicePhase(VoiceInputPhase.Listening)
    val listener = manager.createListener(
        onPartial = { partial -> viewModel.onInputChange(partial) },
        onFinal = { final -> viewModel.appendVoiceTranscript(final) },
        onError = { err -> viewModel.onVoiceError(err) },
        onListeningChanged = { listening ->
            viewModel.onVoicePhase(if (listening) VoiceInputPhase.Listening else VoiceInputPhase.ProcessingSpeech)
        },
    )
    manager.startListening(listener)
}

private fun blockReasonMessage(state: SmartCommandUiState): String? {
    val caps = state.capabilities ?: return null
    return when (state.blockReason) {
        SmartCommandBlockReason.AiDisabled ->
            caps.messages.aiDisabled.ifBlank { "دستیار هوشمند در دسترس نیست." }
        SmartCommandBlockReason.NoLicense ->
            caps.messages.noLicense.ifBlank { "برای استفاده از دستیار هوشمند، لایسنس فعال لازم است." }
        SmartCommandBlockReason.QuotaExhausted ->
            caps.messages.quotaExhausted.ifBlank { "سهمیه فرمان‌های امروز تمام شده." }
        SmartCommandBlockReason.FeatureDisabled -> "دستیار هوشمند فعال نیست."
        null -> null
    }
}
