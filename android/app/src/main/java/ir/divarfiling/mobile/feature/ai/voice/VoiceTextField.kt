package ir.divarfiling.mobile.feature.ai.voice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.feature.ai.smartcommand.SmartCommandMapping

enum class VoiceFieldInsertPolicy {
    Append,
    Replace,
}

@Composable
fun VoiceTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    insertPolicy: VoiceFieldInsertPolicy = VoiceFieldInsertPolicy.Append,
    speechManager: SpeechRecognizerManager? = null,
) {
    val context = LocalContext.current
    val manager = speechManager ?: remember { SpeechRecognizerManager(context.applicationContext) }
    val voiceSession = remember { VoiceSpeechSession(manager) }
    var voicePhase by remember { mutableStateOf(VoiceInputPhase.Idle) }
    var voiceSessionPhase by remember { mutableStateOf(VoiceSessionPhase.Idle) }
    var voiceStatusHint by remember { mutableStateOf<String?>(null) }
    var voiceError by remember { mutableStateOf<String?>(null) }
    var pendingStart by remember { mutableStateOf(false) }
    val sttAvailable = remember { manager.isAvailable() }

    var showSettingsCta by remember { mutableStateOf(false) }
    var voiceSessionBaseline by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            voicePhase = VoiceInputPhase.Idle
            showSettingsCta = false
            pendingStart = true
        } else {
            voicePhase = VoiceInputPhase.Error
            voiceError = SpeechErrorMapper.permissionDenied().userMessage
            val activity = context as? android.app.Activity
            showSettingsCta = activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)
        }
    }

    fun mapSessionPhase(phase: VoiceSessionPhase): VoiceInputPhase = when (phase) {
        VoiceSessionPhase.Idle -> VoiceInputPhase.Idle
        VoiceSessionPhase.Listening, VoiceSessionPhase.Restarting ->
            VoiceInputPhase.Listening
        VoiceSessionPhase.WaitingForContinuation -> VoiceInputPhase.Ready
        VoiceSessionPhase.Finalizing -> VoiceInputPhase.ProcessingSpeech
        VoiceSessionPhase.Error -> VoiceInputPhase.Error
    }

    fun startListening() {
        if (!sttAvailable) return
        voiceError = null
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) {
            voicePhase = VoiceInputPhase.RequestingPermission
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        if (voiceSessionPhase == VoiceSessionPhase.WaitingForContinuation) {
            voiceSession.resumeAfterPause()
            return
        }
        val baseline = when (insertPolicy) {
            VoiceFieldInsertPolicy.Replace -> ""
            VoiceFieldInsertPolicy.Append -> value.trim()
        }
        voiceSessionBaseline = baseline
        voiceSession.start(
            baseInput = baseline,
            onDisplayText = { display ->
                voicePhase = VoiceInputPhase.Listening
                onValueChange(display.take(SmartCommandMapping.MAX_INPUT_CHARS))
            },
            onSessionFinalized = { final ->
                voicePhase = VoiceInputPhase.Ready
                voiceSessionPhase = VoiceSessionPhase.Idle
                voiceStatusHint = null
                onValueChange(final.take(SmartCommandMapping.MAX_INPUT_CHARS))
            },
            onPhase = { phase ->
                voiceSessionPhase = phase
                voicePhase = mapSessionPhase(phase)
            },
            onStatusHint = { hint -> voiceStatusHint = hint },
            onRecoverableError = { err ->
                voicePhase = VoiceInputPhase.Error
                voiceError = err.userMessage
            },
        )
    }

    fun stopListeningManual() {
        voiceSession.manualStop()
    }

    DisposableEffect(Unit) {
        onDispose { voiceSession.destroyRecognizer() }
    }

    if (pendingStart) {
        pendingStart = false
        startListening()
    }

    Column(modifier = modifier) {
        if (voicePhase == VoiceInputPhase.RequestingPermission) {
            Text(
                "برای ثبت فرمان صوتی، دسترسی میکروفن لازم است.",
                style = ir.divarfiling.mobile.core.design.AppTypography.bodyDescription,
                color = DfThemeColors.textSecondary(),
                modifier = Modifier.padding(bottom = AppSpacing.xs),
            )
        }
    DfTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = voicePhase == VoiceInputPhase.Listening ||
            voicePhase == VoiceInputPhase.ProcessingSpeech,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        trailingIcon = if (sttAvailable && enabled) {
            {
                VoiceMicButton(
                    phase = voicePhase,
                    onClick = {
                        when (voicePhase) {
                            VoiceInputPhase.Listening -> stopListeningManual()
                            else -> startListening()
                        }
                    },
                    onCancel = {
                        voiceSession.stopSession()
                        voicePhase = VoiceInputPhase.Idle
                        voiceSessionPhase = VoiceSessionPhase.Idle
                        voiceStatusHint = null
                    },
                )
            }
        } else {
            null
        },
        helperText = when {
            voiceStatusHint != null -> voiceStatusHint
            voicePhase == VoiceInputPhase.Listening ->
                "در حال گوش دادن… برای پایان، دوباره میکروفن را بزنید."
            voiceError != null -> voiceError
            else -> null
        },
    )
    if (voicePhase == VoiceInputPhase.Error && voiceError != null && !showSettingsCta) {
        DfSecondaryButton(
            text = "تلاش دوباره",
            onClick = { voiceError = null; startListening() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.xs),
        )
    }
    if (showSettingsCta) {
        DfSecondaryButton(
            text = "رفتن به تنظیمات",
            onClick = { openAppSettingsForMic(context) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.xs),
        )
    }
    }
}

@Composable
fun VoiceMicButton(
    phase: VoiceInputPhase,
    onClick: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        if (phase == VoiceInputPhase.Listening) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier.semantics { contentDescription = "لغو تشخیص گفتار" },
            ) {
                Icon(DfIcons.X, contentDescription = null, tint = DfThemeColors.error())
            }
        }
        IconButton(
            onClick = onClick,
            modifier = Modifier.semantics {
                contentDescription = if (phase == VoiceInputPhase.Listening) "توقف گوش دادن" else "شروع تشخیص گفتار"
            },
        ) {
            when (phase) {
                VoiceInputPhase.ProcessingSpeech ->
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                VoiceInputPhase.Listening ->
                    Icon(DfIcons.Mic, contentDescription = null, tint = DfThemeColors.primary())
                else -> Icon(DfIcons.Mic, contentDescription = null, tint = DfThemeColors.primary())
            }
        }
    }
}

fun openAppSettingsForMic(context: android.content.Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null),
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private fun applyVoiceText(
    current: String,
    spoken: String,
    policy: VoiceFieldInsertPolicy,
    onValueChange: (String) -> Unit,
) {
    val text = spoken.trim()
    if (text.isBlank()) return
    val merged = when (policy) {
        VoiceFieldInsertPolicy.Replace -> text
        VoiceFieldInsertPolicy.Append -> {
            val base = current.trim()
            if (base.isBlank()) text else "$base $text"
        }
    }
    onValueChange(merged.take(SmartCommandMapping.MAX_INPUT_CHARS))
}
