package ir.divarfiling.mobile.feature.postcall.presentation

import ir.divarfiling.mobile.feature.postcall.PostCallLogic
import javax.inject.Inject
import javax.inject.Singleton

/**
 * dedup نمایش بین Overlay، Notification و In-App.
 * فقط پس از نمایش واقعی commit می‌شود؛ در صورت شکست مسیر UI rollback می‌شود.
 */
@Singleton
class PostCallPresentationLedger @Inject constructor() {
    private var committedKey: String? = null
    private var committedAtMs: Long = 0L
    private var pendingKey: String? = null

    fun beginPresentation(
        phone: String,
        callEndKind: PostCallLogic.CallEndKind,
        nowMs: Long = System.currentTimeMillis(),
    ): Boolean {
        val key = presentationKey(phone, callEndKind)
        if (pendingKey == key) return true
        if (PostCallLogic.shouldSuppressDuplicate(
                lastPhone = committedKey?.substringBefore('|'),
                lastAtMs = committedAtMs,
                phone = phone,
                nowMs = nowMs,
                windowMs = DEDUP_WINDOW_MS,
            )
        ) {
            return false
        }
        pendingKey = key
        return true
    }

    fun commitPresentation(phone: String, callEndKind: PostCallLogic.CallEndKind, nowMs: Long = System.currentTimeMillis()) {
        val key = presentationKey(phone, callEndKind)
        if (pendingKey != key && committedKey != key) return
        committedKey = key
        committedAtMs = nowMs
        pendingKey = null
    }

    fun rollbackPresentation(phone: String, callEndKind: PostCallLogic.CallEndKind) {
        val key = presentationKey(phone, callEndKind)
        if (pendingKey == key) pendingKey = null
    }

    fun clear() {
        committedKey = null
        committedAtMs = 0L
        pendingKey = null
    }

    private fun presentationKey(phone: String, callEndKind: PostCallLogic.CallEndKind): String =
        "$phone|${callEndKind.name}"

    companion object {
        private const val DEDUP_WINDOW_MS = 45_000L
    }
}
