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

enum class MiuiBackgroundPopupStatus {
    NotApplicable,
    NeedsManualConfirmation,
    UserAcknowledged,
}

data class PostCallPermissionSnapshot(
    val phoneStateGranted: Boolean,
    val callLogGranted: Boolean,
    val callLogRuntimeAvailable: Boolean,
    val overlayGranted: Boolean,
    val notificationsGranted: Boolean,
    val isXiaomiFamilyDevice: Boolean,
    val miuiBackgroundPopupStatus: MiuiBackgroundPopupStatus,
    val canDetectCalls: Boolean,
    val canShowOverlay: Boolean,
    val canNotify: Boolean,
)

@Singleton
class PostCallPermissionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val wizardPrefs = context.getSharedPreferences(WIZARD_PREFS, Context.MODE_PRIVATE)

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
        val xiaomi = isXiaomiFamilyDevice()
        val miuiStatus = when {
            !xiaomi -> MiuiBackgroundPopupStatus.NotApplicable
            isMiuiBackgroundPopupUserAcknowledged() -> MiuiBackgroundPopupStatus.UserAcknowledged
            else -> MiuiBackgroundPopupStatus.NeedsManualConfirmation
        }
        return PostCallPermissionSnapshot(
            phoneStateGranted = phoneState,
            callLogGranted = callLogGranted,
            callLogRuntimeAvailable = callLogRuntime,
            overlayGranted = overlay,
            notificationsGranted = notifications,
            isXiaomiFamilyDevice = xiaomi,
            miuiBackgroundPopupStatus = miuiStatus,
            canDetectCalls = phoneState,
            canShowOverlay = overlay,
            canNotify = notifications,
        )
    }

    fun phoneStatePermission(): String = Manifest.permission.READ_PHONE_STATE

    fun callLogPermission(): String = Manifest.permission.READ_CALL_LOG

    fun postNotificationsPermissionOrNull(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }

    /** @deprecated از جریان مرحله‌ای استفاده کنید. */
    fun requiredRuntimePermissions(): Array<String> {
        val list = mutableListOf(Manifest.permission.READ_PHONE_STATE)
        list += Manifest.permission.READ_CALL_LOG
        postNotificationsPermissionOrNull()?.let { list += it }
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

    fun otherPermissionsIntent(): Intent = appDetailsIntent()

    fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(context)

    fun isXiaomiFamilyDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER.orEmpty().lowercase()
        val brand = Build.BRAND.orEmpty().lowercase()
        return manufacturer in XIAOMI_FAMILY || brand in XIAOMI_FAMILY
    }

    /**
     * Intent اختصاصی MIUI در صورت وجود؛ در غیر این صورت null و از [appDetailsIntent] استفاده کنید.
     */
    fun miuiBackgroundPopupSettingsIntentOrNull(): Intent? {
        if (!isXiaomiFamilyDevice()) return null
        val candidates = listOf(
            Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                putExtra("extra_pkgname", context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setPackage("com.miui.securitycenter")
                putExtra("extra_pkgname", context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
        val pm = context.packageManager
        return candidates.firstOrNull { intent ->
            intent.resolveActivity(pm) != null
        }
    }

    fun setMiuiBackgroundPopupUserAcknowledged(acknowledged: Boolean) {
        wizardPrefs.edit().putBoolean(KEY_MIUI_BG_ACK, acknowledged).apply()
    }

    fun isMiuiBackgroundPopupUserAcknowledged(): Boolean =
        wizardPrefs.getBoolean(KEY_MIUI_BG_ACK, false)

    fun clearWizardAcknowledgements() {
        wizardPrefs.edit().clear().apply()
    }

    private fun has(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

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

    companion object {
        private const val WIZARD_PREFS = "post_call_wizard_prefs"
        private const val KEY_MIUI_BG_ACK = "miui_bg_popup_user_ack"
        private val XIAOMI_FAMILY = setOf("xiaomi", "redmi", "poco")
    }
}
