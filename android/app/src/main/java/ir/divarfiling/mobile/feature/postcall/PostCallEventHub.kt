package ir.divarfiling.mobile.feature.postcall

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class PostCallPrompt(
    val phone: String,
    val sessionId: String = java.util.UUID.randomUUID().toString(),
)

@Singleton
class PostCallEventHub @Inject constructor() {
    private val _active = MutableStateFlow<PostCallPrompt?>(null)
    val active: StateFlow<PostCallPrompt?> = _active.asStateFlow()

    private var lastPresentedPhone: String? = null
    private var lastPresentedAtMs: Long = 0L

    fun tryPresent(phone: String, nowMs: Long = System.currentTimeMillis()): Boolean {
        if (_active.value != null) return false
        if (PostCallLogic.shouldSuppressDuplicate(lastPresentedPhone, lastPresentedAtMs, phone, nowMs, DEDUP_WINDOW_MS)) {
            return false
        }
        lastPresentedPhone = phone
        lastPresentedAtMs = nowMs
        _active.value = PostCallPrompt(phone = phone)
        return true
    }

    fun dismiss() {
        _active.value = null
    }

    fun clearSession() {
        dismiss()
        lastPresentedPhone = null
        lastPresentedAtMs = 0L
    }

    companion object {
        private const val DEDUP_WINDOW_MS = 45_000L
    }
}
