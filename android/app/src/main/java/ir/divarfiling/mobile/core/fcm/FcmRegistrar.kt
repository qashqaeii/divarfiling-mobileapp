package ir.divarfiling.mobile.core.fcm

import android.util.Log
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.network.DeviceFcmPatchRequest
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.notifications.DfNotificationHelper
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmRegistrar @Inject constructor(
    private val api: MobileApi,
    private val sessionStore: SessionStore,
    private val notificationHelper: DfNotificationHelper,
) {
    suspend fun uploadToken(token: String): Boolean {
        if (token.isBlank()) return false
        val loggedIn = sessionStore.isLoggedIn.first()
        if (!loggedIn) return false
        return try {
            val response = api.updateDeviceFcm(DeviceFcmPatchRequest(fcmToken = token))
            if (!response.ok) {
                Log.w(TAG, "FCM token upload rejected: ${response.error ?: response.code}")
                false
            } else {
                Log.i(TAG, "FCM token uploaded (${token.take(12)}…)")
                showLocalWelcomeIfNeeded()
                true
            }
        } catch (e: Exception) {
            Log.w(TAG, "FCM token upload failed", e)
            false
        }
    }

    private suspend fun showLocalWelcomeIfNeeded() {
        if (sessionStore.hasShownFcmWelcome()) return
        sessionStore.setFcmWelcomeShown()
        notificationHelper.showNotification(
            id = WELCOME_NOTIFICATION_ID,
            title = "به فایلینگ دیوار خوش آمدید",
            body = "اعلان‌ها فعال شد. یادآورها و استخراج‌ها از اینجا به شما می‌رسد.",
            deepLink = "divarfiling://home",
        )
    }

    private companion object {
        const val TAG = "FcmRegistrar"
        const val WELCOME_NOTIFICATION_ID = 9001
    }
}
