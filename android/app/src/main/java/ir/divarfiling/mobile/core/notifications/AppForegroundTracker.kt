package ir.divarfiling.mobile.core.notifications

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.concurrent.atomic.AtomicInteger

object AppForegroundTracker {
    private val started = AtomicInteger(0)

    val isInForeground: Boolean
        get() = started.get() > 0

    fun register(application: Application) {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                started.incrementAndGet()
            }

            override fun onActivityStopped(activity: Activity) {
                started.updateAndGet { current -> (current - 1).coerceAtLeast(0) }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }
}
