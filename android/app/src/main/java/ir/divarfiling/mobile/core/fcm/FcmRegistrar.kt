package ir.divarfiling.mobile.core.fcm

import android.util.Log
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.network.DeviceFcmPatchRequest
import ir.divarfiling.mobile.core.network.MobileApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmRegistrar @Inject constructor(
    private val api: MobileApi,
    private val sessionStore: SessionStore,
) {
    suspend fun uploadToken(token: String) {
        if (token.isBlank()) return
        val loggedIn = sessionStore.isLoggedIn.first()
        if (!loggedIn) return
        try {
            val response = api.updateDeviceFcm(DeviceFcmPatchRequest(fcmToken = token))
            if (!response.ok) {
                Log.w(TAG, "FCM token upload rejected: ${response.error ?: response.code}")
                return
            }
            Log.i(TAG, "FCM token uploaded (${token.take(12)}…)")
        } catch (e: Exception) {
            Log.w(TAG, "FCM token upload failed", e)
        }
    }

    private companion object {
        const val TAG = "FcmRegistrar"
    }
}
