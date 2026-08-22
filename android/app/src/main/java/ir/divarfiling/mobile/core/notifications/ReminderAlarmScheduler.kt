package ir.divarfiling.mobile.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.core.database.ReminderCacheDao
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.ReminderDto
import ir.divarfiling.mobile.data.repository.ApiResult
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderCache: ReminderCacheDao,
    private val api: MobileApi,
    private val json: Json,
) {
    suspend fun rescheduleAllFromCache() {
        reminderCache.getActive().forEach { entity ->
            val dueMillis = DateUtils.parseInstantMillis(entity.dueAt) ?: return@forEach
            if (entity.done || dueMillis <= System.currentTimeMillis()) {
                cancelReminder(entity.id)
            } else {
                syncFromDto(
                    ReminderDto(
                        id = entity.id,
                        title = entity.title,
                        dueAt = entity.dueAt,
                        contactId = entity.contactId,
                        done = entity.done,
                    ),
                )
            }
        }
    }

    suspend fun syncFromServer(): ApiResult<Int> {
        return try {
            val response = api.getReminders(done = false)
            if (!response.ok) {
                return ApiResult.Error(response.error ?: "بارگذاری یادآورها ناموفق")
            }
            val reminders = response.data?.let {
                json.decodeFromJsonElement(ListSerializer(ReminderDto.serializer()), it)
            }.orEmpty()
            var scheduled = 0
            reminders.forEach { reminder ->
                if (syncFromDto(reminder)) scheduled++
            }
            ApiResult.Success(scheduled)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    fun syncFromDto(reminder: ReminderDto): Boolean {
        val id = reminder.id ?: return false
        if (reminder.done) {
            cancelReminder(id)
            return false
        }
        val dueMillis = DateUtils.parseInstantMillis(reminder.dueAt) ?: return false
        if (dueMillis <= System.currentTimeMillis()) {
            cancelReminder(id)
            return false
        }
        schedule(
            reminderId = id,
            dueAtMillis = dueMillis,
            title = reminder.title,
            body = reminder.note.ifBlank { reminder.title },
            notificationType = inferType(reminder.title),
            deepLink = reminderDeepLink(reminder),
            contactId = reminder.contactId,
        )
        return true
    }

    fun scheduleReminder(
        reminderId: Long,
        dueAtIso: String?,
        title: String,
        body: String,
        deepLink: String?,
        notificationType: String,
    ) {
        val dueMillis = DateUtils.parseInstantMillis(dueAtIso) ?: return
        schedule(
            reminderId = reminderId,
            dueAtMillis = dueMillis,
            title = title,
            body = body,
            notificationType = notificationType,
            deepLink = deepLink,
        )
    }

    fun schedule(
        reminderId: Long,
        dueAtMillis: Long,
        title: String,
        body: String,
        notificationType: String,
        deepLink: String?,
        contactId: Long? = null,
    ) {
        if (reminderId <= 0) return
        if (dueAtMillis <= System.currentTimeMillis()) {
            cancelReminder(reminderId)
            return
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderAlarmReceiver.EXTRA_TITLE, title)
            putExtra(ReminderAlarmReceiver.EXTRA_BODY, body)
            putExtra(ReminderAlarmReceiver.EXTRA_TYPE, notificationType)
            putExtra(ReminderAlarmReceiver.EXTRA_DEEP_LINK, deepLink)
            contactId?.let { putExtra(ReminderAlarmReceiver.EXTRA_CONTACT_ID, it) }
        }
        val pending = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueAtMillis, pending)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, dueAtMillis, pending)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm denied for reminder=$reminderId, falling back", e)
            alarmManager.set(AlarmManager.RTC_WAKEUP, dueAtMillis, pending)
        }
    }

    fun cancel(reminderId: Long) = cancelReminder(reminderId)

    fun cancelReminder(reminderId: Long) {
        if (reminderId <= 0) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pending)
    }

    suspend fun cancelAll() {
        reminderCache.getActive().forEach { cancelReminder(it.id) }
    }

    private fun reminderDeepLink(reminder: ReminderDto): String {
        return reminder.contactId?.let { "divarfiling://crm/contacts/$it" }
            ?: "divarfiling://calendar"
    }

    private fun inferType(title: String): String {
        return if (title.contains("بازدید")) "reminder_visit" else "reminder_call"
    }

    private companion object {
        const val TAG = "ReminderAlarmScheduler"
    }
}
