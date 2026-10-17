package ir.divarfiling.mobile.feature.postcall

import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.feature.postcall.call.PostCallEventDedupStore
import ir.divarfiling.mobile.feature.postcall.call.PostCallMonitor
import ir.divarfiling.mobile.feature.postcall.permission.PostCallPermissionManager
import ir.divarfiling.mobile.feature.postcall.presentation.PostCallOverlayController
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostCallController @Inject constructor(
    private val preferences: PostCallPreferences,
    private val monitor: PostCallMonitor,
    private val sessionStore: SessionStore,
    private val eventHub: PostCallEventHub,
    private val permissionManager: PostCallPermissionManager,
    private val runtimePrefs: PostCallRuntimePrefs,
    private val eventDedupStore: PostCallEventDedupStore,
    private val overlayController: PostCallOverlayController,
    private val presentationLedger: ir.divarfiling.mobile.feature.postcall.presentation.PostCallPresentationLedger,
    private val pendingCallStore: PostCallPendingCallStore,
    private val draftStore: PostCallDraftStore,
) {
    suspend fun refreshRuntime() {
        reconcileFromPersistedConfig()
    }

    suspend fun reconcileFromPersistedConfig() {
        if (!PostCallFeatureFlags.ENABLED) {
            monitor.reconcile(shouldRun = false)
            return
        }
        if (!sessionStore.hasValidSession()) {
            monitor.reconcile(shouldRun = false)
            return
        }
        val user = sessionStore.getUser()
        if (user == null) {
            monitor.reconcile(shouldRun = false)
            return
        }
        val enabled = preferences.isEnabled(user.id)
        runtimePrefs.setEnabledForUser(user.id, enabled)
        if (!enabled) {
            monitor.reconcile(shouldRun = false)
            return
        }
        val snap = permissionManager.snapshot()
        monitor.reconcile(shouldRun = snap.canDetectCalls)
    }

    suspend fun setEnabled(enabled: Boolean): Boolean {
        if (!PostCallFeatureFlags.ENABLED) return false
        val user = sessionStore.getUser() ?: return false
        preferences.setEnabled(user.id, enabled)
        if (!enabled) {
            stopRuntimeOnly()
        } else {
            refreshRuntime()
        }
        return true
    }

    suspend fun dismissPromo() {
        val userId = sessionStore.getUser()?.id ?: return
        preferences.dismissPromo(userId)
    }

    suspend fun shouldShowPromo(): Boolean {
        if (!PostCallFeatureFlags.ENABLED) return false
        if (!sessionStore.hasValidSession()) return false
        val userId = sessionStore.getUser()?.id ?: return false
        return preferences.shouldShowPromo(userId)
    }

    fun permissionSnapshot() = permissionManager.snapshot()

    fun healthSnapshot() = permissionManager.healthSnapshot()

    suspend fun isEnabledForCurrentUser(): Boolean {
        val userId = sessionStore.getUser()?.id ?: return false
        return preferences.isEnabled(userId)
    }

    suspend fun onLogout() {
        stopRuntimeOnly()
        val userId = sessionStore.getUser()?.id
        if (userId != null) {
            draftStore.clearAllForUser(userId)
        }
        preferences.clearAll()
        permissionManager.clearWizardAcknowledgements()
        eventDedupStore.clear()
        pendingCallStore.clearAllUsers()
    }

    fun stopRuntimeOnly() {
        monitor.reconcile(shouldRun = false)
        eventHub.clearSession()
        overlayController.dismissFromAnyThread()
        presentationLedger.clear()
    }
}
