package ir.divarfiling.mobile.feature.postcall

import ir.divarfiling.mobile.feature.postcall.permission.MiuiBackgroundPopupStatus
import ir.divarfiling.mobile.feature.postcall.permission.PostCallPermissionSnapshot
import ir.divarfiling.mobile.feature.postcall.permission.PostCallSetupReadiness
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PostCallSetupReadinessTest {

    private fun snapshot(
        phone: Boolean = false,
        callLogGranted: Boolean = false,
        callLogRuntime: Boolean = false,
        overlay: Boolean = false,
        notify: Boolean = false,
    ) = PostCallPermissionSnapshot(
        phoneStateGranted = phone,
        callLogGranted = callLogGranted,
        callLogRuntimeAvailable = callLogRuntime,
        overlayGranted = overlay,
        notificationsGranted = notify,
        isXiaomiFamilyDevice = false,
        miuiBackgroundPopupStatus = MiuiBackgroundPopupStatus.NotApplicable,
        canDetectCalls = phone,
        canShowOverlay = overlay,
        canNotify = notify,
    )

    @Test
    fun progressText_countsMeasurableGrants() {
        val snap = snapshot(phone = true, callLogRuntime = true, overlay = true, notify = false)
        assertEquals("3 مورد از 4 دسترسی آماده است", PostCallSetupReadiness.progressText(snap))
    }

    @Test
    fun meetsMinimum_requiresPhoneCallLogAndPresentationPath() {
        assertFalse(
            PostCallSetupReadiness.meetsMinimumForEnable(
                snapshot(phone = true, callLogRuntime = true, overlay = false, notify = false),
            ),
        )
        assertTrue(
            PostCallSetupReadiness.meetsMinimumForEnable(
                snapshot(phone = true, callLogRuntime = true, overlay = false, notify = true),
            ),
        )
        assertTrue(
            PostCallSetupReadiness.meetsMinimumForEnable(
                snapshot(phone = true, callLogRuntime = true, overlay = true, notify = false),
            ),
        )
    }

    @Test
    fun enableBlockedReason_whenCallLogMissing() {
        val reason = PostCallSetupReadiness.enableBlockedReason(
            snapshot(phone = true, callLogGranted = false),
        )
        assertTrue(reason!!.contains("اطلاعات تماس"))
    }

    @Test
    fun enableBlockedReason_nullWhenReady() {
        assertNull(
            PostCallSetupReadiness.enableBlockedReason(
                snapshot(phone = true, callLogRuntime = true, overlay = true),
            ),
        )
    }
}
