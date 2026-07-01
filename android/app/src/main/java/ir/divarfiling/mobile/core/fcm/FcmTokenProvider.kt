package ir.divarfiling.mobile.core.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class FcmTokenProvider @Inject constructor() {
    suspend fun fetchToken(): String? = suspendCoroutine { cont ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { e ->
                Log.w(TAG, "FCM getToken failed", e)
                cont.resume(null)
            }
    }

    private companion object {
        const val TAG = "FcmTokenProvider"
    }
}
