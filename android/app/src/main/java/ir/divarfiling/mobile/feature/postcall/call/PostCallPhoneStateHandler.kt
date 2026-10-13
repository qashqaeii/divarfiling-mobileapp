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
import ir.divarfiling.mobile.feature.postcall.PostCallRuntimePrefs
import ir.divarfiling.mobile.feature.postcall.debug.PostCallTrace
import ir.divarfiling.mobile.feature.postcall.presentation.PostCallPresentationRouter
import ir.divarfiling.mobile.feature.postcall.permission.PostCallPermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val runtimePrefs: PostCallRuntimePrefs,
    private val eventDedupStore: PostCallEventDedupStore,
    private val monitor: PostCallMonitor,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun onPhoneStateChanged(state: String, incomingNumber: String?, pendingResult: BroadcastReceiver.PendingResult?) {
        PostCallTrace.log(PostCallTrace.Stage.Receiver, "state=$state")
        if (!runtimeGate.isArmed() && state != TelephonyManager.EXTRA_STATE_IDLE) {
            PostCallTrace.log(PostCallTrace.Stage.Skipped, "not armed")
            pendingResult?.finish()
            return
        }
        if (!hasPhoneStatePermission()) {
            PostCallTrace.log(PostCallTrace.Stage.Skipped, "no phone permission")
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
        if (!PostCallFeatureFlags.ENABLED) return
        ensureMonitorArmedFromPersisted()
        if (!runtimeGate.isArmed()) return
        if (!sessionDataStore.hasValidSession()) {
            PostCallTrace.log(PostCallTrace.Stage.Skipped, "no session")
            return
        }
        val userId = sessionDataStore.getUser()?.id ?: return
        val enabled = postCallPreferences.isEnabled(userId) ||
            (runtimePrefs.activeUserId() == userId && runtimePrefs.isEnabledForActiveUser())
        if (!enabled) return
        if (!permissionManager.snapshot().canDetectCalls) return

        val payload = sessionStore.consumeOnIdle() ?: run {
            PostCallTrace.log(PostCallTrace.Stage.Skipped, "no call session")
            return
        }
        if (!PostCallLogic.shouldPresentAfterCall(payload.snapshot.kind)) return
        val startedAt = payload.startedAtMs.takeIf { it > 0L } ?: System.currentTimeMillis()
        val kind = payload.snapshot.kind ?: return
        val endedAt = System.currentTimeMillis()
        PostCallTrace.log(PostCallTrace.Stage.State, "kind=${kind.name}")
        val phone = callLogResolver.resolveRecentNumber(
            afterWallClockMs = startedAt,
            hintedNumber = payload.snapshot.hintedNumber,
            callKind = kind,
        )
        if (phone.isNullOrBlank()) {
            PostCallTrace.log(PostCallTrace.Stage.CallLog, "no valid number")
            return
        }
        PostCallTrace.log(PostCallTrace.Stage.CallLog, "picked ${PostCallTrace.maskPhone(phone)}")
        if (!eventDedupStore.shouldProcess(phone, kind, endedAt)) {
            PostCallTrace.log(PostCallTrace.Stage.Skipped, "dedup event")
            return
        }
        withContext(Dispatchers.Main.immediate) {
            presentationRouter.presentAfterCall(phone, kind)
        }
    }

    private suspend fun ensureMonitorArmedFromPersisted() {
        if (!runtimePrefs.isEnabledForActiveUser()) return
        if (!sessionDataStore.hasValidSession()) return
        val userId = sessionDataStore.getUser()?.id ?: return
        if (runtimePrefs.activeUserId() != userId) return
        if (!permissionManager.snapshot().canDetectCalls) return
        monitor.reconcile(shouldRun = true)
    }

    private fun hasPhoneStatePermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED
}
