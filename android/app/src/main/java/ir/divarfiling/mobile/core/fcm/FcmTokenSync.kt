package ir.divarfiling.mobile.core.fcm

import ir.divarfiling.mobile.core.datastore.SessionStore
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
    suspend fun syncNow() {
        if (!sessionStore.isLoggedIn.first()) return
        val token = fcmTokenProvider.fetchToken().orEmpty()
        if (token.isBlank()) return
        fcmRegistrar.uploadToken(token)
    }
}
