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
    private val dedupStore: NotificationDedupStore,
) {
    private val largeIconBitmap: Bitmap by lazy { buildLargeIcon() }

    fun showPushMessage(
        type: String?,
        title: String,
        body: String,
        deepLink: String? = null,
        channelId: String? = null,
        notificationKey: String? = null,
        notificationIdValue: String? = null,
        reminderId: Long? = null,
        forceTray: Boolean = false,
    ): Boolean {
        val spec = NotificationCatalog.specFor(type)
        val dedupKey = dedupStore.dedupKey(
            type = type,
            reminderId = reminderId,
            notificationKey = notificationKey,
            notificationId = notificationIdValue,
        )
        if (dedupKey.isNotBlank() && dedupStore.wasRecentlyDelivered(dedupKey)) {
            return false
        }
        if (!forceTray && AppForegroundTracker.isInForeground && !spec.headsUpInForeground) {
            return false
        }
        val shown = showNotification(
            id = NotificationCatalog.notificationId(type, dedupKey, reminderId),
            title = title,
            body = body,
            deepLink = deepLink,
            notificationType = type,
            channelId = channelId,
            groupKey = spec.groupKey,
        )
        if (shown && dedupKey.isNotBlank()) {
            dedupStore.markDelivered(dedupKey)
        }
        return shown
    }

    fun showNotification(
        id: Int,
        title: String,
        body: String,
        deepLink: String? = null,
        notificationType: String? = null,
        channelId: String? = null,
        groupKey: String? = null,
    ): Boolean {
        val resolvedChannel = NotificationCatalog.channelIdFor(notificationType, channelId)
        ensureChannels()
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
        val summary = NotificationCatalog.summary(notificationType)
        val style = NotificationCompat.BigTextStyle()
            .setBigContentTitle(title)
            .bigText(body.ifBlank { title })
            .setSummaryText(summary)
        val spec = NotificationCatalog.specFor(notificationType)
        val builder = NotificationCompat.Builder(context, resolvedChannel)
            .setSmallIcon(R.drawable.ic_stat_divarfiling)
            .setLargeIcon(largeIconBitmap)
            .setColor(ContextCompat.getColor(context, R.color.notification_brand))
            .setContentTitle(title)
            .setContentText(body.ifBlank { title })
            .setSubText(summary)
            .setStyle(style)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(spec.importance)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
        groupKey?.let { builder.setGroup(it) }
        val notification = builder.build()
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return try {
                notificationManager.notify(id, notification)
                true
            } catch (_: SecurityException) {
                false
            }
        }
        return false
    }

    fun cancelNotification(id: Int) {
        NotificationManagerCompat.from(context).cancel(id)
    }

    fun cancelLicenseTrayNotifications() {
        cancelNotification(NotificationCatalog.notificationId("license_expiry", "license_expiry"))
    }

    private fun buildLargeIcon(): Bitmap {
        val drawable = ContextCompat.getDrawable(context, R.drawable.logo_divarfiling)
            ?: ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
            ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        return drawable.toBitmap(width = 256, height = 256)
    }

    private fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channels = listOf(
            channel(NotificationCatalog.CHANNEL_REMINDERS, "یادآورها", "یادآور تماس، بازدید و پیگیری", NotificationManager.IMPORTANCE_HIGH),
            channel(NotificationCatalog.CHANNEL_MATCHES, "فایل پیشنهادی", "تطبیق مشتری با فایل", NotificationManager.IMPORTANCE_DEFAULT),
            channel(NotificationCatalog.CHANNEL_EXTRACT, "استخراج", "پایان و زمان‌بندی استخراج", NotificationManager.IMPORTANCE_DEFAULT),
            channel(NotificationCatalog.CHANNEL_ACCOUNT, "حساب و لایسنس", "انقضا و تمدید لایسنس", NotificationManager.IMPORTANCE_HIGH),
            channel(NotificationCatalog.CHANNEL_DIGEST, "کارهای امروز", "خلاصه روزانه CRM", NotificationManager.IMPORTANCE_DEFAULT),
            channel(NotificationCatalog.CHANNEL_ANNOUNCEMENTS, "اطلاعیه‌ها", "اطلاعیه‌ها و بروزرسانی", NotificationManager.IMPORTANCE_DEFAULT),
            channel(NotificationCatalog.CHANNEL_SUPPORT, "پشتیبانی", "پاسخ تیکت پشتیبانی", NotificationManager.IMPORTANCE_HIGH),
            // Legacy channel kept for devices that already had it configured.
            channel(LEGACY_CHANNEL_ID, "اعلان‌های دیوار فایلینگ", "یادآور CRM، استخراج و کارهای امروز", NotificationManager.IMPORTANCE_HIGH),
        )
        channels.forEach(manager::createNotificationChannel)
    }

    private fun channel(id: String, name: String, description: String, importance: Int): NotificationChannel {
        return NotificationChannel(id, name, importance).apply {
            this.description = description
            enableVibration(true)
            enableLights(true)
            lightColor = ContextCompat.getColor(context, R.color.notification_brand)
        }
    }

    companion object {
        const val LEGACY_CHANNEL_ID = "divar_filing_alerts"
    }
}
