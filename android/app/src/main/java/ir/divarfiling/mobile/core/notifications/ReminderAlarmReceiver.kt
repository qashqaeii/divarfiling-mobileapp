package ir.divarfiling.mobile.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import ir.divarfiling.mobile.data.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var notificationHelper: DfNotificationHelper
    @Inject lateinit var dedupStore: NotificationDedupStore
    @Inject lateinit var notificationRepository: NotificationRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, 0L)
        if (reminderId <= 0L) return
        val type = intent.getStringExtra(EXTRA_TYPE).orEmpty().ifBlank { "reminder_call" }
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "یادآور" }
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty().ifBlank { title }
        val deepLink = intent.getStringExtra(EXTRA_DEEP_LINK)
        val dedupKey = dedupStore.dedupKey(type = type, reminderId = reminderId)
        if (dedupStore.wasRecentlyDelivered(dedupKey)) return

        val shown = notificationHelper.showPushMessage(
            type = type,
            title = title,
            body = body,
            deepLink = deepLink,
            reminderId = reminderId,
            notificationKey = dedupKey,
            forceTray = true,
        )
        if (!shown) return
        dedupStore.markDelivered(dedupKey)
        val pendingResult = goAsync()
        scope.launch {
            try {
                notificationRepository.reportLocalReminderDelivered(reminderId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_TYPE = "type"
        const val EXTRA_DEEP_LINK = "deep_link"
        const val EXTRA_CONTACT_ID = "contact_id"
    }
}
