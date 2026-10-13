package ir.divarfiling.mobile.feature.postcall.presentation

import android.app.KeyguardManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.core.notifications.AppForegroundTracker
import ir.divarfiling.mobile.feature.postcall.PostCallCallMetadata
import ir.divarfiling.mobile.feature.postcall.PostCallEventHub
import ir.divarfiling.mobile.feature.postcall.debug.PostCallTrace
import ir.divarfiling.mobile.feature.postcall.permission.PostCallPermissionManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class PostCallPresentationRouter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissions: PostCallPermissionManager,
    private val eventHub: PostCallEventHub,
    private val notifier: PostCallNotifier,
    private val overlayController: PostCallOverlayController,
    private val presentationLedger: PostCallPresentationLedger,
) {
    suspend fun presentAfterCall(metadata: PostCallCallMetadata) {
        val phone = metadata.phone
        val callEndKind = metadata.callEndKind
        PostCallTrace.log(
            PostCallTrace.Stage.Presentation,
            "kind=${callEndKind.name} phone=${PostCallTrace.maskPhone(phone)}",
        )

        if (isKeyguardLocked()) {
            if (!presentationLedger.beginPresentation(phone, callEndKind)) return
            var displayed = false
            try {
                if (permissions.snapshot().canNotify) {
                    displayed = notifier.showKeyguardSafeNotification(metadata)
                    if (displayed) {
                        PostCallTrace.log(PostCallTrace.Stage.Notification, "keyguard safe")
                    }
                }
            } finally {
                if (displayed) presentationLedger.commitPresentation(phone, callEndKind)
                else presentationLedger.rollbackPresentation(phone, callEndKind)
            }
            return
        }

        if (!presentationLedger.beginPresentation(phone, callEndKind)) {
            PostCallTrace.log(PostCallTrace.Stage.Skipped, "duplicate presentation")
            return
        }

        var displayed = false
        try {
            val inForeground = AppForegroundTracker.isInForeground
            if (inForeground) {
                displayed = withContext(Dispatchers.Main.immediate) {
                    eventHub.tryPresent(metadata)
                }
                if (displayed) {
                    PostCallTrace.log(PostCallTrace.Stage.Displayed, "in-app sheet")
                } else {
                    PostCallTrace.log(PostCallTrace.Stage.Skipped, "in-app hub busy or dedup")
                }
                return
            }

            val snap = permissions.snapshot()
            if (snap.canShowOverlay) {
                val shown = overlayController.show(metadata)
                if (shown) {
                    displayed = true
                    return
                }
                PostCallTrace.log(PostCallTrace.Stage.Skipped, "overlay failed, fallback notification")
            }

            if (snap.canNotify) {
                val ok = notifier.showPostCallNotification(metadata)
                if (ok) {
                    displayed = true
                    PostCallTrace.log(PostCallTrace.Stage.Notification, "shown")
                    return
                }
            }

            PostCallTrace.log(PostCallTrace.Stage.Skipped, "no presentation path")
        } finally {
            if (displayed) {
                presentationLedger.commitPresentation(phone, callEndKind)
            } else {
                presentationLedger.rollbackPresentation(phone, callEndKind)
            }
        }
    }

    private fun isKeyguardLocked(): Boolean {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return km?.isKeyguardLocked == true
    }
}
