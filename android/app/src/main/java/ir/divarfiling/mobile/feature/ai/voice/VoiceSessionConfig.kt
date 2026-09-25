package ir.divarfiling.mobile.feature.ai.voice

/** Aligns with crm-smart-command-voice.js mobile restart policy. */
data class VoiceSessionConfig(
    val maxAutoRestarts: Int = 2,
    val restartDelaysMs: List<Long> = listOf(1_000L, 1_800L),
    val restartCooldownMs: Long = 5_000L,
    val restartDelayMs: Long = 200L,
    val silenceTimeoutMs: Long = 4_000L,
    val maxInputChars: Int = 1_500,
) {
    fun restartDelayForAttempt(attemptIndex: Int): Long =
        restartDelaysMs.getOrElse(attemptIndex) { restartDelaysMs.lastOrNull() ?: restartDelayMs }

    companion object {
        val Default = VoiceSessionConfig()
    }
}
