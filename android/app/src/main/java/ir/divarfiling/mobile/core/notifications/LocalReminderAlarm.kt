package ir.divarfiling.mobile.core.notifications

data class LocalReminderAlarm(
    val reminderId: Long,
    val dueAtMillis: Long,
    val title: String,
    val body: String,
    val deepLink: String,
    val type: String,
)
