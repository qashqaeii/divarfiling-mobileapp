package ir.divarfiling.mobile.core.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import ir.divarfiling.mobile.core.notifications.DfNotificationHelper
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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch { fcmRegistrar.uploadToken(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val action = message.data["action"]
        if (action == "run_schedule") {
            val scheduleId = message.data["schedule_id"]?.toLongOrNull()
            ScheduleWorkManager.enqueueDueRuns(applicationContext, scheduleId)
        }
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "دیوار فایلینگ"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""
        if (action == "run_schedule" && body.isBlank() && message.notification == null) {
            return
        }
        val deepLink = message.data["deep_link"]
        val notificationType = message.data["type"]
        val notificationId = when (notificationType) {
            "customer_match" -> DfNotificationHelper.CUSTOMER_MATCH_NOTIFICATION_ID
            else -> (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        }
        notificationHelper.showNotification(
            id = notificationId,
            title = title,
            body = body,
            deepLink = deepLink,
            notificationType = notificationType,
        )
    }
}
