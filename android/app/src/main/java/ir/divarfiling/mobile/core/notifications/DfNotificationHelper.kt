package ir.divarfiling.mobile.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.MainActivity
import ir.divarfiling.mobile.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DfNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val largeIconBitmap: Bitmap by lazy { buildLargeIcon() }

    fun showNotification(
        id: Int,
        title: String,
        body: String,
        deepLink: String? = null,
        notificationType: String? = null,
    ) {
        ensureChannel()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            deepLink?.let { data = Uri.parse(it) }
        }
        val pending = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val summary = typeSummary(notificationType)
        val style = NotificationCompat.BigTextStyle()
            .setBigContentTitle(title)
            .bigText(body.ifBlank { title })
            .setSummaryText(summary)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_divarfiling)
            .setLargeIcon(largeIconBitmap)
            .setColor(ContextCompat.getColor(context, R.color.notification_brand))
            .setContentTitle(title)
            .setContentText(body.ifBlank { title })
            .setSubText(summary)
            .setStyle(style)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        if (notificationType == "customer_match") {
            builder.setGroup(CUSTOMER_MATCH_GROUP_KEY)
        }
        val notification = builder.build()
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            try {
                notificationManager.notify(id, notification)
            } catch (_: SecurityException) {
                // Permission revoked after check (Android 13+).
            }
        }
    }

    private fun buildLargeIcon(): Bitmap {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_notification_large)
            ?: ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
            ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        return drawable.toBitmap(width = 128, height = 128)
    }

    private fun typeSummary(notificationType: String?): String {
        return when (notificationType) {
            "extract_complete" -> "پایان استخراج · فایلینگ دیوار"
            "extract_schedule_due" -> "زمان استخراج · فایلینگ دیوار"
            "extract_schedule_created" -> "زمان‌بندی · فایلینگ دیوار"
            "reminder_call" -> "یادآور تماس · فایلینگ دیوار"
            "reminder_visit" -> "یادآور بازدید · فایلینگ دیوار"
            "today_digest" -> "برنامه امروز · فایلینگ دیوار"
            "new_dataset" -> "فایل جدید · فایلینگ دیوار"
            "customer_match" -> "تطبیق ملک · فایلینگ دیوار"
            "welcome" -> "فایلینگ دیوار"
            else -> "فایلینگ دیوار"
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "اعلان‌های دیوار فایلینگ",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "یادآور CRM، استخراج و کارهای امروز"
            enableVibration(true)
            enableLights(true)
            lightColor = ContextCompat.getColor(context, R.color.notification_brand)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "divar_filing_alerts"
        const val CUSTOMER_MATCH_GROUP_KEY = "customer_match"
        const val CUSTOMER_MATCH_NOTIFICATION_ID = 41001
    }
}
