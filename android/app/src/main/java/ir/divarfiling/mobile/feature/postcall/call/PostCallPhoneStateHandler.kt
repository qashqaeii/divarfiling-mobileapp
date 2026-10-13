package ir.divarfiling.mobile.feature.postcall.call

import android.content.BroadcastReceiver
import android.content.pm.PackageManager
import android.Manifest
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.feature.postcall.PostCallFeatureFlags
import ir.divarfiling.mobile.feature.postcall.PostCallLogic
import ir.divarfiling.mobile.feature.postcall.PostCallPreferences
import ir.divarfiling.mobile.feature.postcall.presentation.PostCallPresentationRouter
import ir.divarfiling.mobile.feature.postcall.permission.PostCallPermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostCallPhoneStateHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val runtimeGate: PostCallRuntimeGate,
    private val sessionStore: PostCallCallSessionStore,
    private val callLogResolver: CallLogResolver,
    private val presentationRouter: PostCallPresentationRouter,
    private val sessionDataStore: SessionStore,
    private val postCallPreferences: PostCallPreferences,
    private val permissionManager: PostCallPermissionManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun onPhoneStateChanged(state: String, incomingNumber: String?, pendingResult: BroadcastReceiver.PendingResult?) {
        if (!runtimeGate.isArmed()) {
            pendingResult?.finish()
            return
        }
        if (!hasPhoneStatePermission()) {
            pendingResult?.finish()
            return
        }
        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                sessionStore.onRinging(incomingNumber)
                pendingResult?.finish()
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                sessionStore.onOffHook()
                pendingResult?.finish()
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                scope.launch {
                    try {
                        processIdleIfAllowed()
                    } finally {
                        pendingResult?.finish()
                    }
                }
            }
            else -> pendingResult?.finish()
        }
    }

    private suspend fun processIdleIfAllowed() {
        if (!runtimeGate.isArmed()) return
        if (!PostCallFeatureFlags.ENABLED) return
        if (!sessionDataStore.hasValidSession()) return
        val userId = sessionDataStore.getUser()?.id ?: return
        if (!postCallPreferences.isEnabled(userId)) return
        if (!permissionManager.snapshot().canDetectCalls) return

        val payload = sessionStore.consumeOnIdle() ?: return
        if (!PostCallLogic.shouldPresentAfterCall(payload.snapshot.kind)) return
        val startedAt = payload.startedAtMs.takeIf { it > 0L } ?: System.currentTimeMillis()
        val kind = payload.snapshot.kind ?: return
        val phone = callLogResolver.resolveRecentNumber(
            afterWallClockMs = startedAt,
            hintedNumber = payload.snapshot.hintedNumber,
            callKind = kind,
        )
        if (phone.isNullOrBlank()) return
        presentationRouter.presentAfterCall(phone, kind)
    }

    private fun hasPhoneStatePermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED
}
