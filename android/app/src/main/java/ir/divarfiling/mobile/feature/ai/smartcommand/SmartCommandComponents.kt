package ir.divarfiling.mobile.feature.ai.smartcommand

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import android.os.Handler
import android.os.Looper
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import ir.divarfiling.mobile.core.design.components.DfSheetExpandableSection
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.network.NlContactCandidateDto
import ir.divarfiling.mobile.feature.ai.voice.SpeechErrorMapper
import ir.divarfiling.mobile.feature.ai.voice.SpeechRecognizerManager
import ir.divarfiling.mobile.feature.ai.voice.VoiceInputPhase
import ir.divarfiling.mobile.feature.ai.voice.VoiceMicButton
import ir.divarfiling.mobile.feature.ai.voice.VoiceSessionController
import ir.divarfiling.mobile.feature.ai.voice.VoiceSessionPhase
import ir.divarfiling.mobile.feature.ai.voice.VoiceSpeechSession
import ir.divarfiling.mobile.feature.ai.voice.VoiceTextField
import ir.divarfiling.mobile.feature.ai.voice.openAppSettingsForMic

private const val SMART_COMMAND_SHEET_HEIGHT_DEFAULT = 0.78f
private const val SMART_COMMAND_SHEET_HEIGHT_PREVIEW = 0.90f
private const val SMART_COMMAND_SHEET_HEIGHT_COMPACT = 0.72f

@Composable
fun SmartCommandHost(
    profile: SmartCommandProfileKey,
    modifier: Modifier = Modifier,
    onNavigateContact: (Long) -> Unit,
    onNavigateProperty: (Long) -> Unit,
    onNavigateToday: () -> Unit = {},
    onNavigateContactsContext: () -> Unit = {},
    onNavigatePropertiesContext: () -> Unit = {},
    onNavigateTodayContext: () -> Unit = {},
    viewModel: SmartCommandViewModel = hiltViewModel(key = "smart_command_${profile.name}"),
) {
    LaunchedEffect(profile) { viewModel.bindProfile(profile) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val blockMessage = blockReasonMessage(state)
    val showDisabled = state.blockReason != null

    SmartCommandEntryCard(
        title = state.profile?.title?.ifBlank { "دستیار هوشمند" } ?: "دستیار هوشمند",
        subtitle = SmartCommandMapping.entrySubtitle(state.profile, profile),
        placeholder = SmartCommandMapping.entryPlaceholder(state.profile, profile),
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
            onNavigateContactsContext = onNavigateContactsContext,
            onNavigatePropertiesContext = onNavigatePropertiesContext,
            onNavigateTodayContext = onNavigateTodayContext,
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
            .padding(horizontal = AppSpacing.screenHorizontal),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    DfIcons.Sparkles,
                    contentDescription = null,
                    tint = DfColors.Purple,
                    modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold, color = DfColors.TextPrimary)
                    Text(
                        subtitle,
                        style = AppTypography.labelSmall,
                        color = DfColors.TextSecondary,
                        maxLines = 2,
                    )
                }
            }
            Text(
                placeholder,
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 40.dp),
                maxLines = 2,
            )
            quotaHint?.let {
                Text(it, style = AppTypography.labelSmall, color = DfColors.TextMuted)
            }
            if (!enabled && !disabledMessage.isNullOrBlank()) {
                Text(disabledMessage, style = AppTypography.labelSmall, color = DfColors.TextSecondary)
            } else if (enabled) {
                DfPrimaryButton(
                    text = "شروع دستیار هوشمند",
                    onClick = onOpen,
                    modifier = Modifier.fillMaxWidth(),
                )
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
    onNavigateContactsContext: () -> Unit,
    onNavigatePropertiesContext: () -> Unit,
    onNavigateTodayContext: () -> Unit,
) {
    val context = LocalContext.current
    val speechManager = remember { SpeechRecognizerManager(context.applicationContext) }
    val voiceSession = remember { VoiceSpeechSession(speechManager) }
    val sttAvailable = remember { speechManager.isAvailable() }
    var pendingMic by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) pendingMic = true
        else viewModel.onVoiceRecoverableError(SpeechErrorMapper.permissionDenied())
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    var backgroundSuspendRunnable by remember { mutableStateOf<Runnable?>(null) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    backgroundSuspendRunnable?.let { mainHandler.removeCallbacks(it) }
                    val r = Runnable { voiceSession.suspendForBackground() }
                    backgroundSuspendRunnable = r
                    // دیالوگ مجوز یا overlay کوتاه نباید فوراً STT را بکشد
                    mainHandler.postDelayed(r, 800L)
                }
                Lifecycle.Event.ON_RESUME -> {
                    backgroundSuspendRunnable?.let { mainHandler.removeCallbacks(it) }
                    backgroundSuspendRunnable = null
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            backgroundSuspendRunnable?.let { mainHandler.removeCallbacks(it) }
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        onDispose { voiceSession.destroyRecognizer() }
    }

    fun startVoiceListening() {
        if (!sttAvailable) return
        viewModel.clearVoiceError()
        voiceSession.start(
            baseInput = state.inputText.trim(),
            onDisplayText = viewModel::onVoiceDisplayText,
            onSessionFinalized = viewModel::onVoiceSessionFinalized,
            onPhase = viewModel::onVoiceSessionPhase,
            onStatusHint = viewModel::onVoiceStatusHint,
            onRecoverableError = viewModel::onVoiceRecoverableError,
        )
    }

    if (pendingMic) {
        pendingMic = false
        startVoiceListening()
    }

    DfModalBottomSheet(onDismissRequest = onDismiss, dismissOnScrimOrSwipe = false) {
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
                onExampleSelect = viewModel::onInputChange,
                onWrongIntentNavigate = { destination ->
                    when (destination) {
                        WrongIntentDestination.Today -> onNavigateTodayContext()
                        WrongIntentDestination.Contacts -> onNavigateContactsContext()
                        WrongIntentDestination.Properties -> onNavigatePropertiesContext()
                    }
                    viewModel.dismissSheet()
                },
                onMicClick = {
                    if (state.voiceSessionPhase == VoiceSessionPhase.WaitingForContinuation) {
                        voiceSession.resumeAfterPause()
                    } else {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO,
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!granted) {
                            viewModel.onVoicePhase(VoiceInputPhase.RequestingPermission)
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            startVoiceListening()
                        }
                    }
                },
                onMicStop = { voiceSession.manualStop() },
                onMicCancel = {
                    voiceSession.stopSession()
                    viewModel.onVoicePhase(VoiceInputPhase.Idle)
                    viewModel.onVoiceSessionPhase(VoiceSessionPhase.Idle)
                    viewModel.onVoiceStatusHint(null)
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
    onExampleSelect: (String) -> Unit,
    onWrongIntentNavigate: (WrongIntentDestination) -> Unit,
    onMicClick: () -> Unit,
    onMicStop: () -> Unit,
    onMicCancel: () -> Unit,
    onOpenSettings: () -> Unit,
    onClearPreview: () -> Unit,
) {
    val showWrongIntent = state.wrongIntent != null
    val showConfirm = state.phase == SmartCommandPhase.Preview &&
        state.parseResult?.intent != SmartCommandMapping.INTENT_UNKNOWN &&
        state.parseResult?.canConfirm == true &&
        !showWrongIntent &&
        state.showStructuredPreview
    val showUnknown = state.phase == SmartCommandPhase.Preview &&
        state.parseResult?.intent == SmartCommandMapping.INTENT_UNKNOWN
    val exampleChips = SmartCommandMapping.profileExamplesForChips(state.profile?.examples.orEmpty())
    val hasStructuredPreview = state.parseResult != null && !showUnknown && !showWrongIntent &&
        (state.showStructuredPreview || state.previewLines.isNotEmpty())
    val sheetHeightFraction = when {
        hasStructuredPreview || showConfirm -> SMART_COMMAND_SHEET_HEIGHT_PREVIEW
        state.phase == SmartCommandPhase.Parsing -> SMART_COMMAND_SHEET_HEIGHT_DEFAULT
        else -> SMART_COMMAND_SHEET_HEIGHT_DEFAULT
    }
    val sheetSubtitle = when {
        showConfirm -> "جزئیات را بررسی کنید؛ در صورت تأیید ثبت می‌شود"
        state.parseResult != null && !showUnknown -> "درخواست تحلیل شد — متن و جزئیات را مرور کنید"
        else -> state.profile?.subtitle?.ifBlank { "درخواست خود را بگویید یا بنویسید" }
            ?: "درخواست خود را بگویید یا بنویسید"
    }
    DfSheetScaffold(
        title = state.profile?.title?.ifBlank { "دستیار هوشمند" } ?: "دستیار هوشمند",
        subtitle = sheetSubtitle,
        sectionLabel = "دستیار هوشمند",
        icon = DfIcons.Sparkles,
        iconContainerColor = DfColors.PurpleContainer.copy(alpha = 0.85f),
        onClose = onDismiss,
        bodyHeightFraction = sheetHeightFraction,
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
                    !showUnknown &&
                    !showWrongIntent,
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
                Text(
                    if (state.parseSlowHint) {
                        "کمی بیشتر زمان می‌بره، هنوز در حال بررسی هستم…"
                    } else {
                        "در حال بررسی درخواست…"
                    },
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextSecondary,
                )
            }
        }
        if (!sttAvailable) {
            Text(
                "ورود صوتی روی این دستگاه در دسترس نیست.",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
                modifier = Modifier.padding(bottom = AppSpacing.xs),
            )
        }
        DfSheetSection(
            title = if (state.parseResult != null && !showUnknown) "ویرایش درخواست" else "درخواست شما",
        ) {
            DfCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = DfColors.PurpleContainer.copy(alpha = 0.22f),
            ) {
                Column(modifier = Modifier.padding(AppSpacing.sm)) {
                    DfTextField(
                        value = state.inputText,
                        onValueChange = onInputChange,
                        placeholder = state.profile?.placeholder?.ifBlank {
                            SmartCommandMapping.defaultPlaceholder(state.profileKey)
                        } ?: SmartCommandMapping.defaultPlaceholder(state.profileKey),
                        singleLine = false,
                        minLines = if (hasStructuredPreview) 3 else 4,
                        maxLines = 8,
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
                            state.voiceStatusHint != null -> state.voiceStatusHint
                            state.voiceSessionPhase == VoiceSessionPhase.WaitingForContinuation ->
                                VoiceSessionController.CONTINUATION_HINT
                            state.voicePhase == VoiceInputPhase.Listening ->
                                "در حال گوش دادن… برای پایان، میکروفن را بزنید."
                            state.voicePhase == VoiceInputPhase.Error && state.voiceError != null -> state.voiceError
                            else -> null
                        },
                    )
                    if (exampleChips.isNotEmpty() && state.parseResult == null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(top = AppSpacing.xs),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            exampleChips.forEach { example ->
                                DfSoftChip(
                                    text = example.chipLabel(),
                                    selected = false,
                                    onClick = { onExampleSelect(example.commandText()) },
                                )
                            }
                        }
                    }
                    if (state.voicePhase == VoiceInputPhase.Listening && state.voiceStatusHint.isNullOrBlank()) {
                        Text("دارم گوش می‌دم…", style = AppTypography.labelSmall, color = DfColors.Purple)
                    }
                    if (state.inputText.length > 40) {
                        Text(
                            "${state.inputText.length} / ${SmartCommandMapping.MAX_INPUT_CHARS}",
                            style = AppTypography.labelSmall,
                            color = DfColors.TextMuted,
                        )
                    }
                }
            }
        }
        if (hasStructuredPreview && state.inputText.isNotBlank()) {
            SmartCommandParsedRequestBanner(
                requestText = state.inputText,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (showWrongIntent) {
            val wrong = state.wrongIntent!!
            DfCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(AppSpacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Text(wrong.message, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
                    Text(
                        "می‌توانید در بخش مربوطه دوباره بنویسید.",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                    wrong.navigation?.let { dest ->
                        DfSecondaryButton(
                            text = SmartCommandMapping.wrongIntentCtaLabel(dest),
                            onClick = { onWrongIntentNavigate(dest) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
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
        if (!showWrongIntent && state.missingMessages.isNotEmpty() && !showConfirm) {
            SmartCommandIncompleteBanner(
                messages = state.missingMessages,
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.sm),
            )
        }
        if (!showWrongIntent && state.ambiguousMessages.isNotEmpty() && state.phase != SmartCommandPhase.ResolvingContact) {
            SmartCommandAmbiguousBanner(
                messages = state.ambiguousMessages,
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.sm),
            )
        }
        if (!showWrongIntent && state.previewLines.isNotEmpty() && state.showStructuredPreview) {
            SmartCommandPreviewSection(
                lines = state.previewLines,
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.sm),
            )
        }
        when (state.parseResult?.intent) {
            SmartCommandMapping.INTENT_REMINDER -> if (state.showStructuredPreview) {
                state.reminderEdit?.let { edit ->
                    ReminderPreviewSection(edit, onReminderChange)
                }
            }
            SmartCommandMapping.INTENT_CONTACT -> if (state.showStructuredPreview) {
                state.contactEdit?.let { edit ->
                    ContactPreviewSection(edit, onContactChange)
                }
            }
            SmartCommandMapping.INTENT_PROPERTY -> if (state.showStructuredPreview) {
                state.propertyEdit?.let { edit ->
                    PropertyPreviewSection(edit, onPropertyChange)
                }
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
    var showEdit by remember { mutableStateOf(false) }
    DfCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text("یادآوری", style = AppTypography.sectionTitle)
            SmartCommandPreviewRow("مخاطب", edit.contactName)
            SmartCommandPreviewRow("زمان", edit.dueAtDisplay.ifBlank { "${edit.dateText} ${edit.timeText}".trim() })
            SmartCommandPreviewRow("موضوع", edit.title)
        }
    }
    DfSheetExpandableSection(
        title = "ویرایش جزئیات",
        expanded = showEdit,
        onToggle = { showEdit = !showEdit },
    ) {
        DfTextField(edit.contactName, { onChange(edit.copy(contactName = it)) }, label = "مخاطب")
        DfTextField(edit.title, { onChange(edit.copy(title = it)) }, label = "موضوع")
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
    val needsCompletion = edit.name.isBlank() || edit.phone.isBlank()
    var showEdit by remember(edit.name, edit.phone, edit.customerType) {
        mutableStateOf(needsCompletion)
    }
    DfCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text("ثبت مخاطب جدید", style = AppTypography.sectionTitle)
            if (needsCompletion) {
                Text(
                    "نام یا شماره را در بخش زیر تکمیل کنید.",
                    style = AppTypography.labelSmall,
                    color = DfColors.Amber,
                )
            }
            SmartCommandPreviewRow("نام", edit.name)
            SmartCommandPreviewRow("شماره", edit.phone)
            SmartCommandPreviewRow("نوع", edit.customerType)
            SmartCommandPreviewRow("شرکت", edit.companyName)
            SmartCommandPreviewRow("محدوده", edit.areas)
            SmartCommandPreviewRow("توضیحات", edit.notes)
        }
    }
    DfSheetExpandableSection(
        title = if (needsCompletion) "تکمیل اطلاعات مخاطب" else "ویرایش جزئیات",
        expanded = showEdit,
        onToggle = { showEdit = !showEdit },
    ) {
        DfTextField(edit.name, { onChange(edit.copy(name = it)) }, label = "نام و نام خانوادگی")
        DfTextField(edit.phone, { onChange(edit.copy(phone = it)) }, label = "شماره تماس")
        DfTextField(
            edit.customerType,
            { onChange(edit.copy(customerType = it)) },
            label = "نوع مخاطب",
            placeholder = "مثلاً خریدار، مالک، مستأجر",
        )
        DfTextField(edit.companyName, { onChange(edit.copy(companyName = it)) }, label = "شرکت (اختیاری)")
        DfTextField(edit.areas, { onChange(edit.copy(areas = it)) }, label = "مناطق / محدوده")
        VoiceTextField(
            value = edit.notes,
            onValueChange = { onChange(edit.copy(notes = it)) },
            label = "توضیحات",
            placeholder = "نیاز، بودجه، یادداشت…",
            singleLine = false,
            minLines = 2,
        )
    }
}

@Composable
private fun PropertyPreviewSection(edit: PropertyPreviewEdit, onChange: (PropertyPreviewEdit) -> Unit) {
    val needsCompletion = edit.propertyType.isBlank() && edit.neighborhood.isBlank()
    var showEdit by remember(edit.propertyType, edit.neighborhood, edit.salePrice) {
        mutableStateOf(needsCompletion)
    }
    DfCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text("ثبت فایل شخصی", style = AppTypography.sectionTitle)
            if (needsCompletion) {
                Text(
                    "نوع ملک یا منطقه را در بخش زیر تکمیل کنید.",
                    style = AppTypography.labelSmall,
                    color = DfColors.Amber,
                )
            }
            SmartCommandPreviewRow("نوع ملک", edit.propertyType)
            SmartCommandPreviewRow("معامله", edit.dealMode)
            SmartCommandPreviewRow("منطقه", edit.neighborhood)
            SmartCommandPreviewRow("شهر", edit.city)
            SmartCommandPreviewRow("متراژ", edit.area.let { if (it.isBlank()) "" else "$it متر" })
            SmartCommandPreviewRow("اتاق", edit.rooms.let { if (it.isBlank()) "" else "$it خواب" })
            SmartCommandPreviewRow("قیمت فروش", edit.salePrice)
            SmartCommandPreviewRow("ودیعه", edit.deposit)
            SmartCommandPreviewRow("اجاره", edit.monthlyRent)
            SmartCommandPreviewRow("توضیحات", edit.notes)
        }
    }
    DfSheetExpandableSection(
        title = if (needsCompletion) "تکمیل اطلاعات فایل" else "ویرایش جزئیات",
        expanded = showEdit,
        onToggle = { showEdit = !showEdit },
    ) {
        DfTextField(edit.propertyType, { onChange(edit.copy(propertyType = it)) }, label = "نوع ملک", placeholder = "آپارتمان، ویلا…")
        DfTextField(edit.dealMode, { onChange(edit.copy(dealMode = it)) }, label = "نوع معامله", placeholder = "فروش، رهن و اجاره…")
        DfTextField(edit.neighborhood, { onChange(edit.copy(neighborhood = it)) }, label = "منطقه / محله")
        DfTextField(edit.city, { onChange(edit.copy(city = it)) }, label = "شهر")
        DfTextField(edit.area, { onChange(edit.copy(area = it)) }, label = "متراژ (متر)")
        DfTextField(edit.rooms, { onChange(edit.copy(rooms = it)) }, label = "تعداد اتاق")
        DfTextField(edit.salePrice, { onChange(edit.copy(salePrice = it)) }, label = "قیمت فروش")
        DfTextField(edit.deposit, { onChange(edit.copy(deposit = it)) }, label = "ودیعه")
        DfTextField(edit.monthlyRent, { onChange(edit.copy(monthlyRent = it)) }, label = "اجاره ماهانه")
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
        sectionLabel = "دستیار هوشمند",
        icon = DfIcons.Users,
        onClose = onCancel,
        bodyHeightFraction = SMART_COMMAND_SHEET_HEIGHT_DEFAULT,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            candidates.forEach { candidate ->
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
        sectionLabel = "دستیار هوشمند",
        icon = DfIcons.CircleCheck,
        iconContainerColor = DfColors.SuccessContainer,
        iconTint = DfColors.Success,
        onClose = onClose,
        bodyHeightFraction = SMART_COMMAND_SHEET_HEIGHT_COMPACT,
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

@Composable
private fun SmartCommandParsedRequestBanner(
    requestText: String,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    DfCard(
        modifier = modifier,
        containerColor = DfColors.PurpleContainer.copy(alpha = 0.45f),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    DfIcons.Sparkles,
                    contentDescription = null,
                    tint = DfColors.Purple,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    "متن درخواست شما",
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DfColors.Purple,
                )
            }
            Text(
                requestText.trim(),
                style = AppTypography.bodyDescription,
                color = DfColors.TextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 88.dp)
                    .heightIn(max = 160.dp)
                    .verticalScroll(scroll),
            )
        }
    }
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
