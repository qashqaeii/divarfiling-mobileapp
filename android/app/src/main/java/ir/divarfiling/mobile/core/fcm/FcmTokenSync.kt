package ir.divarfiling.mobile.core.fcm

import android.util.Log
import ir.divarfiling.mobile.core.datastore.SessionStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenSync @Inject constructor(
    private val fcmTokenProvider: FcmTokenProvider,
    private val fcmRegistrar: FcmRegistrar,
    private val sessionStore: SessionStore,
) {
    /** Fetch latest FCM token and upload when the user has an active session. */
    suspend fun syncNow() = syncWithRetry()

    /** Retry — Firebase may need a few seconds after install, login, or SHA registration. */
    suspend fun syncWithRetry(maxAttempts: Int = 6) {
        if (!sessionStore.isLoggedIn.first()) return
        repeat(maxAttempts) { attempt ->
            val token = fcmTokenProvider.fetchToken().orEmpty()
            if (token.isNotBlank() && fcmRegistrar.uploadToken(token)) {
                return
            }
            if (attempt < maxAttempts - 1) {
                delay(2_000L * (attempt + 1))
            }
        }
        Log.w(TAG, "FCM token sync failed after $maxAttempts attempts")
    }

    private companion object {
        const val TAG = "FcmTokenSync"
    }
}
