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
import ir.divarfiling.mobile.feature.ai.smartcommand.SmartCommandStateLogic

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
    var voicePhase by remember { mutableStateOf(VoiceInputPhase.Idle) }
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
        voiceSessionBaseline = value.trim()
        voicePhase = VoiceInputPhase.Listening
        val listener = manager.createListener(
            onPartial = { partial ->
                voicePhase = VoiceInputPhase.Listening
                val merged = when (insertPolicy) {
                    VoiceFieldInsertPolicy.Replace -> partial.trim()
                    VoiceFieldInsertPolicy.Append ->
                        SmartCommandStateLogic.voiceTextFromBaseline(voiceSessionBaseline, partial)
                }
                onValueChange(merged.take(SmartCommandMapping.MAX_INPUT_CHARS))
            },
            onFinal = { final ->
                voicePhase = VoiceInputPhase.Ready
                val merged = when (insertPolicy) {
                    VoiceFieldInsertPolicy.Replace -> final.trim()
                    VoiceFieldInsertPolicy.Append ->
                        SmartCommandStateLogic.voiceTextFromBaseline(voiceSessionBaseline, final)
                }
                onValueChange(merged.take(SmartCommandMapping.MAX_INPUT_CHARS))
            },
            onError = { err ->
                voicePhase = VoiceInputPhase.Error
                voiceError = err.userMessage
            },
            onListeningChanged = { listening ->
                voicePhase = if (listening) VoiceInputPhase.Listening else VoiceInputPhase.ProcessingSpeech
            },
        )
        manager.startListening(listener)
    }

    DisposableEffect(Unit) {
        onDispose { manager.destroy() }
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
        enabled = enabled && voicePhase != VoiceInputPhase.Listening,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        trailingIcon = if (sttAvailable && enabled) {
            {
                VoiceMicButton(
                    phase = voicePhase,
                    onClick = {
                        when (voicePhase) {
                            VoiceInputPhase.Listening -> manager.stopListening()
                            else -> startListening()
                        }
                    },
                    onCancel = {
                        manager.cancel()
                        voicePhase = VoiceInputPhase.Idle
                    },
                )
            }
        } else {
            null
        },
        helperText = when {
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
