package ir.divarfiling.mobile.feature.postcall

import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.feature.postcall.call.PostCallMonitor
import ir.divarfiling.mobile.feature.postcall.permission.PostCallPermissionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostCallController @Inject constructor(
    private val preferences: PostCallPreferences,
    private val monitor: PostCallMonitor,
    private val sessionStore: SessionStore,
    private val eventHub: PostCallEventHub,
    private val permissionManager: PostCallPermissionManager,
) {
    suspend fun refreshRuntime() {
        if (!PostCallFeatureFlags.ENABLED) {
            monitor.stop()
            return
        }
        val user = sessionStore.getUser()
        if (user == null || !sessionStore.hasValidSession()) {
            monitor.stop()
            return
        }
        val enabled = preferences.isEnabled(user.id)
        if (!enabled) {
            monitor.stop()
            return
        }
        val snap = permissionManager.snapshot()
        if (!snap.canDetectCalls) {
            monitor.stop()
            return
        }
        monitor.start()
    }

    suspend fun setEnabled(enabled: Boolean): Boolean {
        if (!PostCallFeatureFlags.ENABLED) return false
        val user = sessionStore.getUser() ?: return false
        preferences.setEnabled(user.id, enabled)
        if (!enabled) {
            monitor.stop()
            eventHub.clearSession()
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

    suspend fun isEnabledForCurrentUser(): Boolean {
        val userId = sessionStore.getUser()?.id ?: return false
        return preferences.isEnabled(userId)
    }

    suspend fun onLogout() {
        stopRuntimeOnly()
        preferences.clearAll()
    }

    fun stopRuntimeOnly() {
        monitor.stop()
        eventHub.clearSession()
    }
}
