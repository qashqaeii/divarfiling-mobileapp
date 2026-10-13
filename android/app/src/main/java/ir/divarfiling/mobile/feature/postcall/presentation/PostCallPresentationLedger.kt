package ir.divarfiling.mobile.feature.postcall.presentation

import ir.divarfiling.mobile.feature.postcall.PostCallLogic
import javax.inject.Inject
import javax.inject.Singleton

/** dedup نمایش بین Overlay، Notification و In-App بدون اشغال کردن EventHub. */
@Singleton
class PostCallPresentationLedger @Inject constructor() {
    private var lastKey: String? = null
    private var lastAtMs: Long = 0L

    fun tryClaim(phone: String, callEndKind: PostCallLogic.CallEndKind, nowMs: Long = System.currentTimeMillis()): Boolean {
        if (PostCallLogic.shouldSuppressDuplicate(
                lastPhone = lastKey?.substringBefore('|'),
                lastAtMs = lastAtMs,
                phone = phone,
                nowMs = nowMs,
                windowMs = DEDUP_WINDOW_MS,
            )
        ) {
            return false
        }
        lastKey = "$phone|${callEndKind.name}"
        lastAtMs = nowMs
        return true
    }

    fun clear() {
        lastKey = null
        lastAtMs = 0L
    }

    companion object {
        private const val DEDUP_WINDOW_MS = 45_000L
    }
}
