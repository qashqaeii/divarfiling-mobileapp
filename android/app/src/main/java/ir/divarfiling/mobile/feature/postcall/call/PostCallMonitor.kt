package ir.divarfiling.mobile.feature.postcall.call

import ir.divarfiling.mobile.feature.postcall.debug.PostCallTrace
import javax.inject.Inject
import javax.inject.Singleton

/**
 * فعال/غیرفعال‌کردن Manifest [PostCallPhoneStateReceiver] و پرچم runtime.
 */
@Singleton
class PostCallMonitor @Inject constructor(
    private val runtimeGate: PostCallRuntimeGate,
    private val manifestRegistrar: PostCallManifestRegistrar,
    private val sessionStore: PostCallCallSessionStore,
) {
    @Synchronized
    fun start() {
        runtimeGate.setArmed(true)
        manifestRegistrar.setManifestReceiverEnabled(true)
        PostCallTrace.log(PostCallTrace.Stage.State, "monitor start")
    }

    @Synchronized
    fun stop() {
        runtimeGate.setArmed(false)
        manifestRegistrar.setManifestReceiverEnabled(false)
        sessionStore.clear()
        PostCallTrace.log(PostCallTrace.Stage.State, "monitor stop")
    }

    /**
     * تطبیق وضعیت Component با مقدار ذخیره‌شده — بدون اتکا به متغیر in-memory Process.
     */
    @Synchronized
    fun reconcile(shouldRun: Boolean) {
        val componentEnabled = manifestRegistrar.isManifestReceiverEnabled()
        val desired = manifestRegistrar.desiredReceiverEnabled()
        PostCallTrace.log(
            PostCallTrace.Stage.State,
            "reconcile shouldRun=$shouldRun desired=$desired component=$componentEnabled armed=${runtimeGate.isArmed()}",
        )
        if (!shouldRun) {
            if (componentEnabled || runtimeGate.isArmed()) stop()
            return
        }
        if (!componentEnabled || !runtimeGate.isArmed()) {
            start()
        }
    }
}
