package ir.divarfiling.mobile.feature.postcall

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class PostCallPrompt(
    val phone: String,
    val callEndKind: PostCallLogic.CallEndKind,
    val metadata: PostCallCallMetadata,
    val sessionId: String = java.util.UUID.randomUUID().toString(),
)

@Singleton
class PostCallEventHub @Inject constructor() {
    private val _active = MutableStateFlow<PostCallPrompt?>(null)
    val active: StateFlow<PostCallPrompt?> = _active.asStateFlow()

    private var lastPresentedEventKey: String? = null
    private var lastPresentedAtMs: Long = 0L

    fun tryPresent(
        metadata: PostCallCallMetadata,
        nowMs: Long = System.currentTimeMillis(),
    ): Boolean {
        if (_active.value != null) return false
        val eventKey = PostCallSessionPolicy.eventIdentity(metadata)
        if (lastPresentedEventKey == eventKey && nowMs - lastPresentedAtMs < DEDUP_WINDOW_MS) {
            return false
        }
        lastPresentedEventKey = eventKey
        lastPresentedAtMs = nowMs
        _active.value = PostCallPrompt(
            phone = metadata.phone,
            callEndKind = metadata.callEndKind,
            metadata = metadata,
        )
        return true
    }

    fun dismiss() {
        _active.value = null
    }

    fun clearSession() {
        dismiss()
        lastPresentedEventKey = null
        lastPresentedAtMs = 0L
    }

    companion object {
        private const val DEDUP_WINDOW_MS = 45_000L
    }
}
