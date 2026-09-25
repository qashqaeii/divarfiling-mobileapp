package ir.divarfiling.mobile.feature.ai.voice

import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener

/**
 * Binds [SpeechRecognizerManager] to [VoiceSessionController] for multi-segment STT.
 */
class VoiceSpeechSession(
    private val manager: SpeechRecognizerManager,
    private val config: VoiceSessionConfig = VoiceSessionConfig.Default,
    private val mainHandler: Handler = Handler(Looper.getMainLooper()),
) {
    private var controller: VoiceSessionController? = null
    private var pendingCycleEnd: Runnable? = null

    fun start(
        baseInput: String,
        onDisplayText: (String) -> Unit,
        onSessionFinalized: (String) -> Unit,
        onPhase: (VoiceSessionPhase) -> Unit,
        onStatusHint: (String?) -> Unit,
        onRecoverableError: (VoiceSpeechError) -> Unit,
    ) {
        stopSession()
        controller = VoiceSessionController(
            config = config,
            onDisplayText = onDisplayText,
            onSessionFinalized = onSessionFinalized,
            onPhaseChanged = onPhase,
            onStatusHint = onStatusHint,
            onRecoverableError = onRecoverableError,
            scheduleDelayed = { delayMs, block ->
                mainHandler.postDelayed(block, delayMs)
            },
            requestStartRecognizer = { startRecognizerInternal() },
            requestStopRecognizer = { manager.stopListening() },
        ).also { it.startSession(baseInput) }
    }

    fun manualStop() {
        controller?.manualStop()
    }

    fun resumeAfterPause() {
        controller?.resumeAfterPause()
    }

    fun suspendForBackground() {
        controller?.suspendForBackground()
    }

    fun stopSession() {
        pendingCycleEnd?.let { mainHandler.removeCallbacks(it) }
        pendingCycleEnd = null
        controller?.destroy()
        controller = null
        manager.cancel()
    }

    fun destroyRecognizer() {
        stopSession()
        manager.destroy()
    }

    fun retryAfterError(baseInput: String) {
        controller?.retryAfterError(baseInput)
    }

    private fun startRecognizerInternal() {
        val ctrl = controller ?: return
        val listener = manager.createListener(
            onPartial = { partial -> ctrl.onAndroidPartial(partial) },
            onFinal = { final ->
                if (final.isNotBlank()) {
                    ctrl.onAndroidFinal(final)
                }
                scheduleCycleEnd()
            },
            onError = { err ->
                ctrl.onRecognizerError(err)
                scheduleCycleEnd()
            },
            onListeningChanged = { listening -> ctrl.onRecognizerListening(listening) },
        )
        manager.startListening(listener)
    }

    private fun scheduleCycleEnd() {
        pendingCycleEnd?.let { mainHandler.removeCallbacks(it) }
        val runnable = Runnable {
            pendingCycleEnd = null
            controller?.onRecognizerCycleEnded()
        }
        pendingCycleEnd = runnable
        mainHandler.postDelayed(runnable, 120L)
    }
}
