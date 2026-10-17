package ir.divarfiling.mobile.feature.postcall.presentation

import ir.divarfiling.mobile.feature.postcall.PostCallCallMetadata
import ir.divarfiling.mobile.feature.postcall.PostCallSessionPolicy
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
        metadata: PostCallCallMetadata,
        nowMs: Long = System.currentTimeMillis(),
    ): Boolean {
        val key = PostCallSessionPolicy.eventIdentity(metadata)
        if (pendingKey == key) return false
        if (committedKey == key && nowMs - committedAtMs < DEDUP_WINDOW_MS) {
            return false
        }
        pendingKey = key
        return true
    }

    fun commitPresentation(metadata: PostCallCallMetadata, nowMs: Long = System.currentTimeMillis()) {
        val key = PostCallSessionPolicy.eventIdentity(metadata)
        if (pendingKey != key && committedKey != key) return
        committedKey = key
        committedAtMs = nowMs
        pendingKey = null
    }

    fun rollbackPresentation(metadata: PostCallCallMetadata) {
        val key = PostCallSessionPolicy.eventIdentity(metadata)
        if (pendingKey == key) pendingKey = null
    }

    fun clear() {
        committedKey = null
        committedAtMs = 0L
        pendingKey = null
    }

    companion object {
        private const val DEDUP_WINDOW_MS = 45_000L
    }
}
