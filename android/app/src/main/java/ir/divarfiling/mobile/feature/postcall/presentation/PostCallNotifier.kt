package ir.divarfiling.mobile.feature.postcall.presentation

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.notifications.NotificationCatalog
import ir.divarfiling.mobile.feature.postcall.PostCallAssistantActivity
import ir.divarfiling.mobile.feature.postcall.PostCallCallMetadata
import ir.divarfiling.mobile.feature.postcall.PostCallLogic
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostCallNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun showPostCallNotification(metadata: PostCallCallMetadata): Boolean {
        ensureChannel()
        val intent = PostCallAssistantActivity.intent(
            context,
            metadata,
            fromNotification = true,
        )
        val pending = PendingIntent.getActivity(
            context,
            metadata.phone.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, NotificationCatalog.CHANNEL_POST_CALL)
            .setSmallIcon(R.drawable.ic_stat_divarfiling)
            .setColor(context.getColor(R.color.notification_brand))
            .setContentTitle(notificationTitle(metadata.callEndKind))
            .setContentText("ثبت سریع در CRM برای ${metadata.phone}")
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        return notify(notification)
    }

    fun showKeyguardSafeNotification(metadata: PostCallCallMetadata): Boolean {
        ensureChannel()
        val intent = PostCallAssistantActivity.intent(context, metadata, fromNotification = true)
        val pending = PendingIntent.getActivity(
            context,
            metadata.phone.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, NotificationCatalog.CHANNEL_POST_CALL)
            .setSmallIcon(R.drawable.ic_stat_divarfiling)
            .setColor(context.getColor(R.color.notification_brand))
            .setContentTitle("تماس پایان یافت")
            .setContentText("برای ثبت در CRM، اعلان را باز کنید")
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        return notify(notification)
    }

    fun cancel() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun notify(notification: android.app.Notification): Boolean {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return try {
            manager.notify(NOTIFICATION_ID, notification)
            true
        } catch (_: SecurityException) {
            false
        }
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

    private fun notificationTitle(kind: PostCallLogic.CallEndKind): String = when (kind) {
        PostCallLogic.CallEndKind.Missed -> "تماس بی‌پاسخ"
        PostCallLogic.CallEndKind.Outgoing -> "تماس خروجی پایان یافت"
        PostCallLogic.CallEndKind.Answered -> "تماس پایان یافت"
        PostCallLogic.CallEndKind.Ignored -> "تماس پایان یافت"
    }

    companion object {
        private const val NOTIFICATION_ID = 910_042
    }
}
