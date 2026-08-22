package ir.divarfiling.mobile.core.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.notifications.DfNotificationHelper
import ir.divarfiling.mobile.core.notifications.NotificationDedupStore
import ir.divarfiling.mobile.core.notifications.ReminderAlarmScheduler
import ir.divarfiling.mobile.feature.extract.schedule.ScheduleWorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DivarFilingMessagingService : FirebaseMessagingService() {
    @Inject lateinit var fcmRegistrar: FcmRegistrar
    @Inject lateinit var notificationHelper: DfNotificationHelper
    @Inject lateinit var reminderAlarmScheduler: ReminderAlarmScheduler
    @Inject lateinit var dedupStore: NotificationDedupStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch { fcmRegistrar.uploadToken(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        when (data["action"]) {
            "run_schedule" -> {
                val scheduleId = data["schedule_id"]?.toLongOrNull()
                ScheduleWorkManager.enqueueDueRuns(applicationContext, scheduleId)
                if (message.notification == null && data["body"].isNullOrBlank()) return
            }
            "schedule_reminder" -> {
                handleScheduleReminder(data)
                return
            }
            "cancel_reminder" -> {
                data["reminder_id"]?.toLongOrNull()?.let(reminderAlarmScheduler::cancel)
                return
            }
        }

        val title = message.notification?.title ?: data["title"] ?: "دیوار فایلینگ"
        val body = message.notification?.body ?: data["body"] ?: ""
        if (data["action"] == "run_schedule" && body.isBlank() && message.notification == null) return

        val type = data["type"]
        val reminderId = data["reminder_id"]?.toLongOrNull()
        val dedupKey = dedupStore.dedupKey(
            type = type,
            reminderId = reminderId,
            notificationKey = data["notification_key"],
            notificationId = data["notification_id"],
        )
        if (type?.startsWith("reminder_") == true && data["source"] == "server_fallback") {
            // Local alarm is primary; server fallback only if local did not fire recently.
            if (dedupStore.wasRecentlyDelivered(dedupKey)) return
        }
        notificationHelper.showPushMessage(
            type = type,
            title = title,
            body = body,
            deepLink = data["deep_link"],
            channelId = data["channel"],
            notificationKey = data["notification_key"],
            notificationIdValue = data["notification_id"],
            reminderId = reminderId,
        )
    }

    private fun handleScheduleReminder(data: Map<String, String>) {
        val reminderId = data["reminder_id"]?.toLongOrNull() ?: return
        val dueAt = data["due_at"].orEmpty()
        val dueMillis = DateUtils.parseInstantMillis(dueAt) ?: return
        if (dueMillis <= System.currentTimeMillis()) return
        reminderAlarmScheduler.schedule(
            reminderId = reminderId,
            dueAtMillis = dueMillis,
            title = data["title"].orEmpty().ifBlank { "یادآور" },
            body = data["body"].orEmpty(),
            notificationType = data["type"].orEmpty().ifBlank { "reminder_call" },
            deepLink = data["deep_link"],
            contactId = data["contact_id"]?.toLongOrNull(),
        )
    }
}
