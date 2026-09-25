package ir.divarfiling.mobile.feature.ai.voice

/**
 * Multi-segment voice session (Web semantics). Android feeds one partial/final per recognizer cycle;
 * controller merges segments and schedules bounded auto-restarts.
 */
class VoiceSessionController(
    private val config: VoiceSessionConfig = VoiceSessionConfig.Default,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val onDisplayText: (String) -> Unit,
    private val onSessionFinalized: (String) -> Unit,
    private val onPhaseChanged: (VoiceSessionPhase) -> Unit,
    private val onStatusHint: (String?) -> Unit,
    private val onRecoverableError: (VoiceSpeechError) -> Unit,
    private val scheduleDelayed: (delayMs: Long, block: () -> Unit) -> Unit,
    private val requestStartRecognizer: () -> Unit,
    private val requestStopRecognizer: () -> Unit,
) {
    var baseInputText: String = ""
        private set

    private var sessionTranscript: String = ""
    private var cycleFinalTranscript: String = ""
    private var cycleInterimTranscript: String = ""

    private var userListening: Boolean = false
    private var manualStopRequested: Boolean = false
    private var sessionFinalized: Boolean = false
    private var autoRestartCount: Int = 0
    private var lastRestartAt: Long = 0L
    private var blockAutoRestart: Boolean = false

    val phase: VoiceSessionPhase
        get() = _phase
    private var _phase: VoiceSessionPhase = VoiceSessionPhase.Idle

    fun startSession(baseInput: String) {
        resetAll()
        baseInputText = VoiceTranscriptMerge.normalizeWhitespace(baseInput)
        userListening = true
        manualStopRequested = false
        sessionFinalized = false
        blockAutoRestart = false
        autoRestartCount = 0
        lastRestartAt = 0L
        setPhase(VoiceSessionPhase.Listening)
        onStatusHint(LISTENING_HINT)
        syncDisplay()
        requestStartRecognizer()
    }

    fun manualStop() {
        if (!userListening || sessionFinalized) return
        manualStopRequested = true
        blockAutoRestart = true
        requestStopRecognizer()
        finalizeSession(flushInterim = true, statusHint = null)
    }

    /** After max auto-restarts — user taps mic again to keep speaking without losing transcript. */
    fun resumeAfterPause() {
        if (sessionFinalized || _phase != VoiceSessionPhase.WaitingForContinuation) return
        blockAutoRestart = false
        autoRestartCount = 0
        lastRestartAt = 0L
        userListening = true
        manualStopRequested = false
        setPhase(VoiceSessionPhase.Listening)
        onStatusHint(LISTENING_HINT)
        syncDisplay()
        requestStartRecognizer()
    }

    fun suspendForBackground() {
        if (!userListening || sessionFinalized) return
        blockAutoRestart = true
        requestStopRecognizer()
        commitCycleToSession()
        syncDisplay()
        setPhase(VoiceSessionPhase.WaitingForContinuation)
        onStatusHint(CONTINUATION_HINT)
    }

    fun destroy() {
        blockAutoRestart = true
        userListening = false
        requestStopRecognizer()
        resetBuffers()
        setPhase(VoiceSessionPhase.Idle)
        onStatusHint(null)
    }

    fun onRecognizerListening(active: Boolean) {
        if (sessionFinalized || !userListening) return
        if (active) {
            setPhase(VoiceSessionPhase.Listening)
        }
    }

    fun onAndroidPartial(partial: String) {
        if (!userListening || sessionFinalized || manualStopRequested) return
        cycleInterimTranscript = VoiceTranscriptMerge.normalizeWhitespace(partial)
        onStatusHint(LISTENING_HINT)
        syncDisplay()
    }

    fun onAndroidFinal(final: String) {
        if (!userListening || sessionFinalized || manualStopRequested) return
        val piece = VoiceTranscriptMerge.normalizeWhitespace(final)
        if (piece.isNotEmpty()) {
            cycleFinalTranscript = VoiceTranscriptMerge.appendTranscriptSegmentSafe(
                cycleFinalTranscript,
                piece,
            )
        }
        cycleInterimTranscript = ""
        syncDisplay()
    }

    /** Called when the platform recognizer finishes a cycle (results or end). */
    fun onRecognizerCycleEnded() {
        if (sessionFinalized || manualStopRequested) return
        if (!userListening) return
        commitCycleToSession()
        syncDisplay()
        if (blockAutoRestart) return
        scheduleRecognitionRestart()
    }

    fun onRecognizerError(error: VoiceSpeechError) {
        if (sessionFinalized) return
        when (error) {
            VoiceSpeechError.NoSpeech,
            VoiceSpeechError.Timeout,
            -> {
                if (sessionTranscript.isNotEmpty() || cycleFinalTranscript.isNotEmpty()) {
                    commitCycleToSession()
                    syncDisplay()
                    if (userListening && !manualStopRequested && !blockAutoRestart) {
                        scheduleRecognitionRestart()
                        return
                    }
                }
                setPhase(VoiceSessionPhase.Error)
                onRecoverableError(error)
            }
            VoiceSpeechError.Network -> {
                setPhase(VoiceSessionPhase.Error)
                onRecoverableError(error)
            }
            VoiceSpeechError.PermissionDenied,
            VoiceSpeechError.Unavailable,
            VoiceSpeechError.Generic,
            -> {
                blockAutoRestart = true
                userListening = false
                setPhase(VoiceSessionPhase.Error)
                onRecoverableError(error)
            }
        }
    }

    fun retryAfterError(baseInput: String) {
        if (userListening && !sessionFinalized) return
        startSession(baseInput)
    }

    private fun scheduleRecognitionRestart() {
        if (blockAutoRestart || manualStopRequested || sessionFinalized || !userListening) return
        if (autoRestartCount >= config.maxAutoRestarts) {
            setPhase(VoiceSessionPhase.WaitingForContinuation)
            onStatusHint(CONTINUATION_HINT)
            return
        }
        val sinceRestart = clock() - lastRestartAt
        if (lastRestartAt > 0 && sinceRestart < config.restartCooldownMs) {
            setPhase(VoiceSessionPhase.WaitingForContinuation)
            onStatusHint(CONTINUATION_HINT)
            return
        }
        setPhase(VoiceSessionPhase.Restarting)
        val delay = config.restartDelayForAttempt(autoRestartCount)
        scheduleDelayed(delay) {
            if (blockAutoRestart || manualStopRequested || sessionFinalized || !userListening) return@scheduleDelayed
            autoRestartCount += 1
            lastRestartAt = clock()
            setPhase(VoiceSessionPhase.Listening)
            onStatusHint(LISTENING_HINT)
            requestStartRecognizer()
        }
    }

    private fun finalizeSession(flushInterim: Boolean, statusHint: String?) {
        if (sessionFinalized) return
        sessionFinalized = true
        userListening = false
        blockAutoRestart = true
        setPhase(VoiceSessionPhase.Finalizing)
        commitCycleToSession()
        var voiceText = VoiceTranscriptMerge.normalizeWhitespace(sessionTranscript)
        voiceText = VoiceTranscriptMerge.appendTranscriptSegmentSafe(voiceText, cycleFinalTranscript)
        if (flushInterim) {
            voiceText = VoiceTranscriptMerge.appendTranscriptSegmentSafe(voiceText, cycleInterimTranscript)
        }
        val finalText = VoiceTranscriptMerge.appendTranscriptSegmentSafe(baseInputText, voiceText)
            .take(config.maxInputChars)
        onSessionFinalized(finalText)
        resetBuffers()
        setPhase(VoiceSessionPhase.Idle)
        onStatusHint(statusHint)
    }

    private fun commitCycleToSession() {
        if (cycleFinalTranscript.isNotEmpty()) {
            sessionTranscript = VoiceTranscriptMerge.appendTranscriptSegmentSafe(
                sessionTranscript,
                cycleFinalTranscript,
            )
        }
        val interim = VoiceTranscriptMerge.normalizeWhitespace(cycleInterimTranscript)
        if (interim.isNotEmpty()) {
            sessionTranscript = VoiceTranscriptMerge.appendTranscriptSegmentSafe(
                sessionTranscript,
                cycleInterimTranscript,
            )
        }
        cycleFinalTranscript = ""
        cycleInterimTranscript = ""
    }

    private fun syncDisplay() {
        val display = VoiceTranscriptMerge.buildLiveTextareaValue(
            baseInputText,
            sessionTranscript,
            cycleFinalTranscript,
            cycleInterimTranscript,
        ).take(config.maxInputChars)
        onDisplayText(display)
    }

    private fun resetBuffers() {
        sessionTranscript = ""
        cycleFinalTranscript = ""
        cycleInterimTranscript = ""
    }

    private fun resetAll() {
        resetBuffers()
        baseInputText = ""
        userListening = false
        manualStopRequested = false
        sessionFinalized = false
        autoRestartCount = 0
        lastRestartAt = 0L
        blockAutoRestart = false
    }

    private fun setPhase(next: VoiceSessionPhase) {
        if (_phase == next) return
        _phase = next
        onPhaseChanged(next)
    }

    companion object {
        const val LISTENING_HINT = "در حال گوش دادن… می‌توانید بین جمله‌ها مکث کنید."
        const val CONTINUATION_HINT = "می‌تونی ادامه بدی…"
    }
}

enum class VoiceSessionPhase {
    Idle,
    Listening,
    WaitingForContinuation,
    Restarting,
    Finalizing,
    Error,
}
