package ir.divarfiling.mobile.feature.postcall.presentation

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.notifications.NotificationCatalog
import ir.divarfiling.mobile.feature.postcall.PostCallAssistantActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostCallNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun showPostCallNotification(phone: String) {
        ensureChannel()
        val intent = PostCallAssistantActivity.intent(context, phone, fromNotification = true)
        val pending = PendingIntent.getActivity(
            context,
            phone.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, NotificationCatalog.CHANNEL_POST_CALL)
            .setSmallIcon(R.drawable.ic_stat_divarfiling)
            .setColor(context.getColor(R.color.notification_brand))
            .setContentTitle("تماس پایان یافت")
            .setContentText("ثبت سریع در CRM برای $phone")
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun cancel() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = android.app.NotificationChannel(
            NotificationCatalog.CHANNEL_POST_CALL,
            "دستیار پس از تماس",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "یادآور ثبت مخاطب یا یادداشت بعد از تماس"
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val NOTIFICATION_ID = 910_042
    }
}
