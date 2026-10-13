package ir.divarfiling.mobile.feature.postcall.call

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** پرچم سبک همگام — فقط وقتی PostCallMonitor.start() زده شده true است. */
@Singleton
class PostCallRuntimeGate @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isArmed(): Boolean = prefs.getBoolean(KEY_ARMED, false)

    fun setArmed(armed: Boolean) {
        prefs.edit().putBoolean(KEY_ARMED, armed).apply()
    }

    companion object {
        private const val PREFS = "post_call_runtime_gate"
        private const val KEY_ARMED = "armed"
    }
}
