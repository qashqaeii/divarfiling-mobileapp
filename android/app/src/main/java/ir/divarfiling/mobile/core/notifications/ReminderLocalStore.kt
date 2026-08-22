package ir.divarfiling.mobile.core.notifications

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderLocalStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun saveScheduled(reminderId: Long, dueAtMillis: Long) {
        prefs.edit()
            .putLong(dueKey(reminderId), dueAtMillis)
            .apply()
    }

    fun getScheduledDueAt(reminderId: Long): Long? {
        val value = prefs.getLong(dueKey(reminderId), -1L)
        return value.takeIf { it > 0L }
    }

    fun remove(reminderId: Long) {
        prefs.edit().remove(dueKey(reminderId)).apply()
    }

    fun allScheduled(): Map<Long, Long> {
        return prefs.all.mapNotNull { (key, value) ->
            if (!key.startsWith(PREFIX)) return@mapNotNull null
            val id = key.removePrefix(PREFIX).toLongOrNull() ?: return@mapNotNull null
            val due = value as? Long ?: return@mapNotNull null
            id to due
        }.toMap()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun dueKey(reminderId: Long) = "$PREFIX$reminderId"

    companion object {
        private const val PREFS = "reminder_local_schedule"
        private const val PREFIX = "due_"
    }
}
