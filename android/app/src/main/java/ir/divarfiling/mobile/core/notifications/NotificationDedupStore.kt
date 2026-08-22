package ir.divarfiling.mobile.core.notifications

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationDedupStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun wasRecentlyDelivered(key: String, maxAgeMs: Long = DEFAULT_TTL_MS): Boolean {
        if (key.isBlank()) return false
        val at = prefs.getLong(key, 0L)
        if (at <= 0L) return false
        if (System.currentTimeMillis() - at > maxAgeMs) {
            prefs.edit().remove(key).apply()
            return false
        }
        return true
    }

    fun markDelivered(key: String) {
        if (key.isBlank()) return
        prefs.edit().putLong(key, System.currentTimeMillis()).apply()
        prune()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun dedupKey(
        type: String?,
        reminderId: Long? = null,
        notificationKey: String? = null,
        notificationId: String? = null,
    ): String {
        notificationKey?.takeIf { it.isNotBlank() }?.let { return it }
        if (reminderId != null && reminderId > 0) return "reminder:$reminderId"
        notificationId?.takeIf { it.isNotBlank() }?.let { return "notification:$it" }
        val t = type?.lowercase().orEmpty()
        return if (t.isNotBlank()) "type:$t:${System.currentTimeMillis() / 60_000}" else ""
    }

    private fun prune() {
        val now = System.currentTimeMillis()
        val editor = prefs.edit()
        prefs.all.forEach { (key, value) ->
            if (value is Long && now - value > DEFAULT_TTL_MS) {
                editor.remove(key)
            }
        }
        editor.apply()
    }

    private companion object {
        const val PREFS = "df_notification_dedup"
        const val DEFAULT_TTL_MS = 6 * 60 * 60 * 1000L
    }
}
