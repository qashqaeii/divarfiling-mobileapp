package ir.divarfiling.mobile.feature.postcall

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * تصویر همگام (SharedPreferences) برای مسیر Broadcast/Cold Start.
 * DataStore مبنای UI است؛ این لایه فقط برای خواندن فوری بدون coroutine استفاده می‌شود.
 */
@Singleton
class PostCallRuntimePrefs @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun setEnabledForUser(userId: Long, enabled: Boolean) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }

    fun isEnabledForActiveUser(): Boolean {
        if (!prefs.getBoolean(KEY_ENABLED, false)) return false
        return prefs.getLong(KEY_USER_ID, 0L) > 0L
    }

    fun activeUserId(): Long? {
        if (!prefs.getBoolean(KEY_ENABLED, false)) return null
        val id = prefs.getLong(KEY_USER_ID, 0L)
        return id.takeIf { it > 0L }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS = "post_call_runtime_mirror"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ENABLED = "enabled"
    }
}
