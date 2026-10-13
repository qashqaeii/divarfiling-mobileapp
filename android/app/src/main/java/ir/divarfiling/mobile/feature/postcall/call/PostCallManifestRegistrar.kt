package ir.divarfiling.mobile.feature.postcall.call

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostCallManifestRegistrar @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val component = ComponentName(context, PostCallPhoneStateReceiver::class.java)

    fun setManifestReceiverEnabled(enabled: Boolean) {
        val state = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        context.packageManager.setComponentEnabledSetting(
            component,
            state,
            PackageManager.DONT_KILL_APP,
        )
        prefs.edit().putBoolean(KEY_DESIRED, enabled).apply()
    }

    fun isManifestReceiverEnabled(): Boolean {
        val state = context.packageManager.getComponentEnabledSetting(component)
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }

    fun desiredReceiverEnabled(): Boolean = prefs.getBoolean(KEY_DESIRED, false)

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS = "post_call_manifest_registrar"
        private const val KEY_DESIRED = "desired_enabled"
    }
}
