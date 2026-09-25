package ir.divarfiling.mobile.feature.ai.voice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner

/**
 * Pauses STT when the app leaves the foreground. Uses process lifecycle so permission
 * dialogs and in-app overlays do not stop recognition mid-sentence.
 */
@Composable
fun VoiceSessionBackgroundEffect(voiceSession: VoiceSpeechSession) {
    val lifecycle = ProcessLifecycleOwner.get().lifecycle
    DisposableEffect(lifecycle, voiceSession) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                voiceSession.suspendForBackground()
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}
