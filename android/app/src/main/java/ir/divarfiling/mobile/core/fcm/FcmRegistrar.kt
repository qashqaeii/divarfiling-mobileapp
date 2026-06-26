package ir.divarfiling.mobile.core.fcm

import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.network.DeviceFcmPatchRequest
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.util.DeviceIdProvider
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmRegistrar @Inject constructor(
    private val api: MobileApi,
    private val sessionStore: SessionStore,
    private val deviceIdProvider: DeviceIdProvider,
) {
    suspend fun uploadToken(token: String) {
        if (token.isBlank()) return
        val loggedIn = sessionStore.isLoggedIn.first()
        if (!loggedIn) return
        try {
            api.updateDeviceFcm(DeviceFcmPatchRequest(fcmToken = token))
        } catch (_: Exception) {
            // best-effort; token will retry on next login
        }
    }
}
