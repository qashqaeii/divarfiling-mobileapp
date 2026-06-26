package ir.divarfiling.mobile.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceIdProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
  @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID,
        ) ?: "unknown"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest("divarfiling:$androidId".toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
