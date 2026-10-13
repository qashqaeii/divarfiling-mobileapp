package ir.divarfiling.mobile.feature.postcall

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import ir.divarfiling.mobile.core.datastore.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostCallBootstrap @Inject constructor(
    private val sessionStore: SessionStore,
    private val controller: PostCallController,
    private val preferences: PostCallPreferences,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var started = false

    fun bind() {
        if (!PostCallFeatureFlags.ENABLED || started) return
        started = true
        scope.launch {
            sessionStore.isLoggedIn
                .flatMapLatest { loggedIn ->
                    if (!loggedIn) {
                        flowOf(false)
                    } else {
                        val userId = sessionStore.getUser()?.id
                        if (userId == null) flowOf(false)
                        else preferences.isEnabledFlow(userId)
                    }
                }
                .distinctUntilChanged()
                .collect { enabled ->
                    if (enabled) controller.refreshRuntime() else controller.stopRuntimeOnly()
                }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    scope.launch { controller.refreshRuntime() }
                }
            },
        )
        scope.launch {
            sessionStore.isLoggedIn.collect { loggedIn ->
                if (!loggedIn) {
                    controller.stopRuntimeOnly()
                }
            }
        }
    }
}
