package ir.divarfiling.mobile.feature.postcall.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class PostCallPermissionSnapshot(
    val phoneStateGranted: Boolean,
    val callLogGranted: Boolean,
    val callLogRuntimeAvailable: Boolean,
    val overlayGranted: Boolean,
    val notificationsGranted: Boolean,
    val canDetectCalls: Boolean,
    val canShowOverlay: Boolean,
    val canNotify: Boolean,
)

@Singleton
class PostCallPermissionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun snapshot(): PostCallPermissionSnapshot {
        val phoneState = has(Manifest.permission.READ_PHONE_STATE)
        val callLogGranted = has(Manifest.permission.READ_CALL_LOG)
        val callLogRuntime = callLogGranted && isCallLogLikelyUsable()
        val overlay = canDrawOverlays()
        val notifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            has(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
        return PostCallPermissionSnapshot(
            phoneStateGranted = phoneState,
            callLogGranted = callLogGranted,
            callLogRuntimeAvailable = callLogRuntime,
            overlayGranted = overlay,
            notificationsGranted = notifications,
            canDetectCalls = phoneState,
            canShowOverlay = overlay,
            canNotify = notifications,
        )
    }

    /**
     * READ_CALL_LOG اختیاری است؛ اگر سیستم رد کند، با hint تماس یا بدون پنجره همچنان فعال می‌ماند.
     */
    fun requiredRuntimePermissions(): Array<String> {
        val list = mutableListOf(Manifest.permission.READ_PHONE_STATE)
        list += Manifest.permission.READ_CALL_LOG
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list += Manifest.permission.POST_NOTIFICATIONS
        }
        return list.toTypedArray()
    }

    fun hasMinimumPermissionsForEnable(grants: Map<String, Boolean>): Boolean =
        grants[Manifest.permission.READ_PHONE_STATE] == true

    fun overlaySettingsIntent(): Intent =
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun appDetailsIntent(): Intent =
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun canDrawOverlays(): Boolean =
        Settings.canDrawOverlays(context)

    private fun has(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /**
     * READ_CALL_LOG ممکن است در Manifest باشد اما در runtime برای APK مستقیم هم محدود شود.
     */
    private fun isCallLogLikelyUsable(): Boolean {
        if (!has(Manifest.permission.READ_CALL_LOG)) return false
        return try {
            context.contentResolver.query(
                android.provider.CallLog.Calls.CONTENT_URI,
                arrayOf(android.provider.CallLog.Calls._ID),
                null,
                null,
                "${android.provider.CallLog.Calls.DATE} DESC",
            )?.use { true } ?: false
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }
}
