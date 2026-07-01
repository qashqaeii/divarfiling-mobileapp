package ir.divarfiling.mobile.core.fcm

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class FcmTokenProvider @Inject constructor() {
    suspend fun fetchToken(): String? = withContext(Dispatchers.IO) {
        try {
            Tasks.await(
                FirebaseMessaging.getInstance().token,
                30,
                TimeUnit.SECONDS,
            )
        } catch (e: Exception) {
            Log.w(TAG, "FCM getToken failed: ${e.message}", e)
            null
        }
    }

    private companion object {
        const val TAG = "FcmTokenProvider"
    }
}
