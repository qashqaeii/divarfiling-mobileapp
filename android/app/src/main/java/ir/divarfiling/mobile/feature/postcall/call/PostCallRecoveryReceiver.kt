package ir.divarfiling.mobile.feature.postcall.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import ir.divarfiling.mobile.feature.postcall.PostCallController
import ir.divarfiling.mobile.feature.postcall.PostCallFeatureFlags
import ir.divarfiling.mobile.feature.postcall.debug.PostCallTrace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PostCallRecoveryReceiver : BroadcastReceiver() {
    @Inject lateinit var controller: PostCallController

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (!PostCallFeatureFlags.ENABLED) return
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        PostCallTrace.log(PostCallTrace.Stage.Receiver, "recovery action=$action")
        val pending = goAsync()
        scope.launch {
            try {
                controller.reconcileFromPersistedConfig()
            } finally {
                pending.finish()
            }
        }
    }
}
