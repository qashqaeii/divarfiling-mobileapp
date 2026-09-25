package ir.divarfiling.mobile.feature.ai

import ir.divarfiling.mobile.feature.ai.voice.VoiceSessionConfig
import ir.divarfiling.mobile.feature.ai.voice.VoiceSessionController
import ir.divarfiling.mobile.feature.ai.voice.VoiceSessionPhase
import ir.divarfiling.mobile.feature.ai.voice.VoiceSpeechError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceSessionControllerTest {

    private fun controller(
        onDisplay: (String) -> Unit = {},
        onFinal: (String) -> Unit = {},
        onPhase: (VoiceSessionPhase) -> Unit = {},
        onHint: (String?) -> Unit = {},
        onError: (VoiceSpeechError) -> Unit = {},
        schedule: (Long, () -> Unit) -> Unit = { _, block -> block() },
        start: () -> Unit = {},
        stop: () -> Unit = {},
    ): VoiceSessionController = VoiceSessionController(
        config = VoiceSessionConfig(maxAutoRestarts = 2, restartDelaysMs = listOf(0L, 0L)),
        clock = { 10_000L },
        onDisplayText = onDisplay,
        onSessionFinalized = onFinal,
        onPhaseChanged = onPhase,
        onStatusHint = onHint,
        onRecoverableError = onError,
        scheduleDelayed = schedule,
        requestStartRecognizer = start,
        requestStopRecognizer = stop,
    )

    @Test
    fun manualStop_finalizesMergedTranscript() {
        var finalized = ""
        val ctrl = controller(onFinal = { finalized = it })
        ctrl.startSession("")
        ctrl.onAndroidFinal("فردا ساعت ده")
        ctrl.onRecognizerCycleEnded()
        ctrl.onAndroidFinal("با احمدی تماس بگیرم")
        ctrl.onRecognizerCycleEnded()
        ctrl.manualStop()
        assertTrue(finalized.contains("فردا"))
        assertTrue(finalized.contains("احمدی"))
    }

    @Test
    fun emptyCycle_restartsWithoutWaitingState() {
        var starts = 0
        val phases = mutableListOf<VoiceSessionPhase>()
        val ctrl = controller(
            onPhase = { phases.add(it) },
            start = { starts += 1 },
        )
        ctrl.startSession("")
        assertEquals(1, starts)
        ctrl.onRecognizerCycleEnded()
        assertTrue(starts >= 2)
        assertFalse(phases.contains(VoiceSessionPhase.WaitingForContinuation))
    }

    @Test
    fun maxRestart_entersWaitingForContinuation() {
        val phases = mutableListOf<VoiceSessionPhase>()
        var starts = 0
        val ctrl = controller(
            onPhase = { phases.add(it) },
            start = { starts += 1 },
            schedule = { _, block -> block() },
        )
        ctrl.startSession("")
        ctrl.onAndroidFinal("بخش اول")
        repeat(3) { ctrl.onRecognizerCycleEnded() }
        assertTrue(phases.contains(VoiceSessionPhase.WaitingForContinuation))
        assertTrue(starts >= 1)
    }

    @Test
    fun recoverableError_keepsTranscript() {
        val displays = mutableListOf<String>()
        val ctrl = controller(onDisplay = { displays.add(it) })
        ctrl.startSession("موجود")
        ctrl.onAndroidFinal("ادامه")
        ctrl.onRecognizerCycleEnded()
        ctrl.onRecognizerError(VoiceSpeechError.NoSpeech)
        assertTrue(displays.last().contains("موجود"))
    }

    @Test
    fun destroy_resetsToIdle() {
        var phase = VoiceSessionPhase.Listening
        val ctrl = controller(onPhase = { phase = it })
        ctrl.startSession("")
        ctrl.destroy()
        assertEquals(VoiceSessionPhase.Idle, phase)
    }
}
