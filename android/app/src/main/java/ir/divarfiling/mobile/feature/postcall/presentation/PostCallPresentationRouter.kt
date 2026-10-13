package ir.divarfiling.mobile.feature.postcall.presentation

import android.app.KeyguardManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.core.notifications.AppForegroundTracker
import ir.divarfiling.mobile.feature.postcall.PostCallAssistantActivity
import ir.divarfiling.mobile.feature.postcall.PostCallEventHub
import ir.divarfiling.mobile.feature.postcall.PostCallLogic
import ir.divarfiling.mobile.feature.postcall.permission.PostCallPermissionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostCallPresentationRouter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissions: PostCallPermissionManager,
    private val eventHub: PostCallEventHub,
    private val notifier: PostCallNotifier,
) {
    fun presentAfterCall(phone: String, callEndKind: PostCallLogic.CallEndKind) {
        if (isKeyguardLocked()) return
        val snap = permissions.snapshot()
        val inForeground = AppForegroundTracker.isInForeground

        if (inForeground && eventHub.tryPresent(phone, callEndKind)) return

        if (snap.canShowOverlay) {
            try {
                context.startActivity(
                    PostCallAssistantActivity.intent(
                        context,
                        phone,
                        callEndKind,
                        fromNotification = false,
                    ),
                )
                return
            } catch (e: ActivityNotFoundException) {
                Log.w(TAG, "Post-call overlay activity blocked", e)
            } catch (e: Exception) {
                Log.w(TAG, "Post-call overlay activity failed", e)
            }
        }

        if (snap.canNotify) {
            notifier.showPostCallNotification(phone, callEndKind)
            return
        }

        if (inForeground) {
            eventHub.tryPresent(phone, callEndKind)
        }
    }

    private fun isKeyguardLocked(): Boolean {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return km?.isKeyguardLocked == true
    }

    companion object {
        private const val TAG = "PostCallPresentation"
    }
}
