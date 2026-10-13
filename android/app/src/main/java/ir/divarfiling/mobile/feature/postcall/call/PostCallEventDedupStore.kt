package ir.divarfiling.mobile.feature.postcall.call

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.feature.postcall.PostCallLogic
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostCallEventDedupStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun shouldProcess(phone: String, kind: PostCallLogic.CallEndKind, endedAtMs: Long): Boolean {
        val key = "${phone}|${kind.name}|${endedAtMs / 1000L}"
        val lastKey = prefs.getString(KEY_LAST_KEY, null)
        val lastAt = prefs.getLong(KEY_LAST_AT, 0L)
        if (lastKey == key && endedAtMs - lastAt < DEDUP_MS) return false
        prefs.edit()
            .putString(KEY_LAST_KEY, key)
            .putLong(KEY_LAST_AT, endedAtMs)
            .apply()
        return true
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS = "post_call_event_dedup"
        private const val KEY_LAST_KEY = "last_key"
        private const val KEY_LAST_AT = "last_at"
        private const val DEDUP_MS = 45_000L
    }
}
