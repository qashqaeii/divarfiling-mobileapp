package ir.divarfiling.mobile.core.notifications

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderDeliveryStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun wasShown(reminderId: Long, dueAtMillis: Long): Boolean {
        return prefs.getBoolean(key(reminderId, dueAtMillis), false)
    }

    fun markShown(reminderId: Long, dueAtMillis: Long) {
        prefs.edit().putBoolean(key(reminderId, dueAtMillis), true).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private fun key(reminderId: Long, dueAtMillis: Long): String {
        val bucket = dueAtMillis / 60_000L
        return "shown:$reminderId:$bucket"
    }

    private companion object {
        const val PREFS = "df_reminder_delivery"
    }
}
