package ir.divarfiling.mobile.feature.postcall.call

import javax.inject.Inject
import javax.inject.Singleton

/**
 * فعال/غیرفعال‌کردن Manifest [PostCallPhoneStateReceiver] و پرچم runtime.
 * رویدادها از طریق [PostCallPhoneStateHandler] به همان مسیر resolve/present می‌رسند.
 */
@Singleton
class PostCallMonitor @Inject constructor(
    private val runtimeGate: PostCallRuntimeGate,
    private val manifestRegistrar: PostCallManifestRegistrar,
    private val sessionStore: PostCallCallSessionStore,
) {
    @Volatile
    private var running = false

    @Synchronized
    fun start() {
        if (running) return
        running = true
        runtimeGate.setArmed(true)
        manifestRegistrar.setManifestReceiverEnabled(true)
    }

    @Synchronized
    fun stop() {
        if (!running) return
        running = false
        runtimeGate.setArmed(false)
        manifestRegistrar.setManifestReceiverEnabled(false)
        sessionStore.clear()
    }

    val isRunning: Boolean get() = running
}
