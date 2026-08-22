package ir.divarfiling.mobile.core.notifications

import androidx.core.app.NotificationCompat

data class NotificationSpec(
    val type: String,
    val channelId: String,
    val channelName: String,
    val channelDescription: String,
    val importance: Int,
    val groupKey: String?,
    val headsUpInForeground: Boolean,
    val replaceable: Boolean,
)

object NotificationCatalog {
    const val CHANNEL_REMINDERS = "df_reminders"
    const val CHANNEL_MATCHES = "df_matches"
    const val CHANNEL_EXTRACT = "df_extract"
    const val CHANNEL_ACCOUNT = "df_account"
    const val CHANNEL_DIGEST = "df_digest"
    const val CHANNEL_ANNOUNCEMENTS = "df_announcements"
    const val CHANNEL_SUPPORT = "df_support"

    const val GROUP_REMINDERS = "reminders"
    const val GROUP_MATCHES = "customer_match"
    const val GROUP_EXTRACT = "extract"
    const val GROUP_LICENSE = "license"
    const val GROUP_DIGEST = "digest"
    const val GROUP_ANNOUNCEMENTS = "announcements"
    const val GROUP_SUPPORT = "support"

    private val specs = mapOf(
        "reminder_call" to NotificationSpec(
            type = "reminder_call",
            channelId = CHANNEL_REMINDERS,
            channelName = "یادآورها",
            channelDescription = "یادآور تماس و بازدید",
            importance = NotificationCompat.PRIORITY_HIGH,
            groupKey = GROUP_REMINDERS,
            headsUpInForeground = true,
            replaceable = false,
        ),
        "reminder_visit" to NotificationSpec(
            type = "reminder_visit",
            channelId = CHANNEL_REMINDERS,
            channelName = "یادآورها",
            channelDescription = "یادآور تماس و بازدید",
            importance = NotificationCompat.PRIORITY_HIGH,
            groupKey = GROUP_REMINDERS,
            headsUpInForeground = true,
            replaceable = false,
        ),
        "today_digest" to NotificationSpec(
            type = "today_digest",
            channelId = CHANNEL_DIGEST,
            channelName = "کارهای امروز",
            channelDescription = "خلاصه روزانه CRM",
            importance = NotificationCompat.PRIORITY_DEFAULT,
            groupKey = GROUP_DIGEST,
            headsUpInForeground = false,
            replaceable = true,
        ),
        "overdue_followup" to NotificationSpec(
            type = "overdue_followup",
            channelId = CHANNEL_REMINDERS,
            channelName = "یادآورها",
            channelDescription = "پیگیری‌های معوق",
            importance = NotificationCompat.PRIORITY_HIGH,
            groupKey = GROUP_DIGEST,
            headsUpInForeground = false,
            replaceable = true,
        ),
        "extract_complete" to NotificationSpec(
            type = "extract_complete",
            channelId = CHANNEL_EXTRACT,
            channelName = "استخراج",
            channelDescription = "پایان استخراج و زمان‌بندی",
            importance = NotificationCompat.PRIORITY_DEFAULT,
            groupKey = GROUP_EXTRACT,
            headsUpInForeground = false,
            replaceable = false,
        ),
        "extract_schedule_due" to NotificationSpec(
            type = "extract_schedule_due",
            channelId = CHANNEL_EXTRACT,
            channelName = "استخراج",
            channelDescription = "زمان اجرای استخراج",
            importance = NotificationCompat.PRIORITY_DEFAULT,
            groupKey = GROUP_EXTRACT,
            headsUpInForeground = false,
            replaceable = true,
        ),
        "extract_schedule_created" to NotificationSpec(
            type = "extract_schedule_created",
            channelId = CHANNEL_EXTRACT,
            channelName = "استخراج",
            channelDescription = "زمان‌بندی استخراج",
            importance = NotificationCompat.PRIORITY_DEFAULT,
            groupKey = GROUP_EXTRACT,
            headsUpInForeground = false,
            replaceable = false,
        ),
        "customer_match" to NotificationSpec(
            type = "customer_match",
            channelId = CHANNEL_MATCHES,
            channelName = "فایل پیشنهادی",
            channelDescription = "تطبیق مشتری با فایل",
            importance = NotificationCompat.PRIORITY_HIGH,
            groupKey = GROUP_MATCHES,
            headsUpInForeground = false,
            replaceable = false,
        ),
        "license_expiry" to NotificationSpec(
            type = "license_expiry",
            channelId = CHANNEL_ACCOUNT,
            channelName = "حساب و لایسنس",
            channelDescription = "انقضا و تمدید لایسنس",
            importance = NotificationCompat.PRIORITY_HIGH,
            groupKey = GROUP_LICENSE,
            headsUpInForeground = true,
            replaceable = true,
        ),
        "support_reply" to NotificationSpec(
            type = "support_reply",
            channelId = CHANNEL_SUPPORT,
            channelName = "پشتیبانی",
            channelDescription = "پاسخ تیکت پشتیبانی",
            importance = NotificationCompat.PRIORITY_HIGH,
            groupKey = GROUP_SUPPORT,
            headsUpInForeground = true,
            replaceable = false,
        ),
        "welcome" to NotificationSpec(
            type = "welcome",
            channelId = CHANNEL_ANNOUNCEMENTS,
            channelName = "اطلاعیه‌ها",
            channelDescription = "خوش‌آمد و اطلاعیه‌های عمومی",
            importance = NotificationCompat.PRIORITY_DEFAULT,
            groupKey = GROUP_ANNOUNCEMENTS,
            headsUpInForeground = false,
            replaceable = true,
        ),
        "announcement" to NotificationSpec(
            type = "announcement",
            channelId = CHANNEL_ANNOUNCEMENTS,
            channelName = "اطلاعیه‌ها",
            channelDescription = "اطلاعیه‌های عمومی",
            importance = NotificationCompat.PRIORITY_DEFAULT,
            groupKey = GROUP_ANNOUNCEMENTS,
            headsUpInForeground = false,
            replaceable = false,
        ),
        "app_update" to NotificationSpec(
            type = "app_update",
            channelId = CHANNEL_ANNOUNCEMENTS,
            channelName = "اطلاعیه‌ها",
            channelDescription = "بروزرسانی اپ",
            importance = NotificationCompat.PRIORITY_DEFAULT,
            groupKey = GROUP_ANNOUNCEMENTS,
            headsUpInForeground = false,
            replaceable = true,
        ),
        "product_update" to NotificationSpec(
            type = "product_update",
            channelId = CHANNEL_ANNOUNCEMENTS,
            channelName = "اطلاعیه‌ها",
            channelDescription = "قابلیت‌های جدید",
            importance = NotificationCompat.PRIORITY_DEFAULT,
            groupKey = GROUP_ANNOUNCEMENTS,
            headsUpInForeground = false,
            replaceable = false,
        ),
    )

    fun specFor(type: String?): NotificationSpec {
        val key = type?.lowercase().orEmpty()
        return specs[key] ?: NotificationSpec(
            type = key.ifBlank { "general" },
            channelId = CHANNEL_ANNOUNCEMENTS,
            channelName = "اطلاعیه‌ها",
            channelDescription = "اعلان‌های فایلینگ دیوار",
            importance = NotificationCompat.PRIORITY_DEFAULT,
            groupKey = GROUP_ANNOUNCEMENTS,
            headsUpInForeground = false,
            replaceable = false,
        )
    }

    fun channelIdFor(type: String?, fallback: String? = null): String {
        return fallback?.takeIf { it.isNotBlank() } ?: specFor(type).channelId
    }

    fun notificationId(type: String?, key: String?, reminderId: Long? = null): Int {
        val spec = specFor(type)
        if (reminderId != null && reminderId > 0) {
            return REMINDER_ID_BASE + (reminderId % REMINDER_ID_SPAN).toInt()
        }
        val stable = key?.takeIf { it.isNotBlank() }
            ?: type?.takeIf { it.isNotBlank() }
            ?: System.currentTimeMillis().toString()
        return when {
            spec.replaceable -> stable.hashCode()
            else -> (stable + type.orEmpty()).hashCode()
        }
    }

    fun summary(type: String?): String = when (type?.lowercase()) {
        "extract_complete" -> "پایان استخراج · فایلینگ دیوار"
        "extract_schedule_due" -> "زمان استخراج · فایلینگ دیوار"
        "extract_schedule_created" -> "زمان‌بندی · فایلینگ دیوار"
        "reminder_call" -> "یادآور تماس · فایلینگ دیوار"
        "reminder_visit" -> "یادآور بازدید · فایلینگ دیوار"
        "today_digest" -> "برنامه امروز · فایلینگ دیوار"
        "customer_match" -> "تطبیق ملک · فایلینگ دیوار"
        "license_expiry" -> "لایسنس · فایلینگ دیوار"
        "support_reply" -> "پشتیبانی · فایلینگ دیوار"
        "welcome" -> "فایلینگ دیوار"
        else -> "فایلینگ دیوار"
    }

    private const val REMINDER_ID_BASE = 30_000
    private const val REMINDER_ID_SPAN = 50_000
}
