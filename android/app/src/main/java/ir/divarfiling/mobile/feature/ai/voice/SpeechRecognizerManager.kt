package ir.divarfiling.mobile.feature.ai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class VoiceInputPhase {
    Idle,
    RequestingPermission,
    Listening,
    ProcessingSpeech,
    Ready,
    Error,
}

@Singleton
class SpeechRecognizerManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    private var recognizer: SpeechRecognizer? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(appContext)

    fun createListener(
        onPartial: (String) -> Unit,
        onFinal: (String) -> Unit,
        onError: (VoiceSpeechError) -> Unit,
        onListeningChanged: (Boolean) -> Unit,
    ): RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            onListeningChanged(true)
        }

        override fun onBeginningOfSpeech() = Unit

        override fun onRmsChanged(rmsdB: Float) = Unit

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
            onListeningChanged(false)
        }

        override fun onError(error: Int) {
            onListeningChanged(false)
            onError(SpeechErrorMapper.fromAndroidCode(error))
        }

        override fun onResults(results: Bundle?) {
            onListeningChanged(false)
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            onFinal(text)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (text.isNotBlank()) onPartial(text)
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    fun startListening(listener: RecognitionListener) {
        stopInternal()
        val sr = SpeechRecognizer.createSpeechRecognizer(appContext)
        recognizer = sr
        sr.setRecognitionListener(listener)
        sr.startListening(buildIntent())
    }

    fun stopListening() {
        recognizer?.stopListening()
    }

    fun cancel() {
        recognizer?.cancel()
    }

    fun destroy() {
        stopInternal()
    }

    private fun stopInternal() {
        recognizer?.destroy()
        recognizer = null
    }

    private fun buildIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        // پیش‌فرض سیستم (~۱ث) با مکث کوتاه session را می‌بندد؛ برای جمله‌های طولانی‌تر آزادتر باشد.
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 6_000L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5_000L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 0L)
    }
}

enum class VoiceSpeechError(val userMessage: String) {
    NoSpeech("صدایی تشخیص داده نشد، دوباره امتحان کنید."),
    Network("اتصال شبکه برای تشخیص گفتار برقرار نیست."),
    Unavailable("تشخیص گفتار روی این دستگاه در دسترس نیست."),
    PermissionDenied("برای استفاده از میکروفن، دسترسی ضبط صدا لازم است."),
    Timeout("زمان گوش دادن تمام شد. دوباره امتحان کنید."),
    Generic("خطا در تشخیص گفتار. دوباره امتحان کنید."),
}

object SpeechErrorMapper {
    fun fromAndroidCode(code: Int): VoiceSpeechError = when (code) {
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceSpeechError.Timeout
        SpeechRecognizer.ERROR_NO_MATCH -> VoiceSpeechError.NoSpeech
        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
        -> VoiceSpeechError.Network
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> VoiceSpeechError.PermissionDenied
        SpeechRecognizer.ERROR_CLIENT,
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
        SpeechRecognizer.ERROR_SERVER,
        -> VoiceSpeechError.Unavailable
        else -> VoiceSpeechError.Generic
    }

    fun permissionDenied(): VoiceSpeechError = VoiceSpeechError.PermissionDenied
}
