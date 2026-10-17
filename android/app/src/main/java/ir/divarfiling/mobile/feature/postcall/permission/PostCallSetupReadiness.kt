package ir.divarfiling.mobile.feature.postcall.permission

/**
 * منطق خالص راه‌اندازی دستیار — برای UI و تست واحد.
 */
object PostCallSetupReadiness {

    /** دسترسی‌های اصلی که در پیشرفت «X از Y» شمرده می‌شوند. */
    fun measurableGrantedCount(snapshot: PostCallPermissionSnapshot): Int {
        var count = 0
        if (snapshot.phoneStateGranted) count++
        if (snapshot.callLogRuntimeAvailable) count++
        if (snapshot.canShowOverlay) count++
        if (snapshot.canNotify) count++
        return count
    }

    const val MEASURABLE_PERMISSION_COUNT: Int = 4

    fun progressText(snapshot: PostCallPermissionSnapshot): String {
        val granted = measurableGrantedCount(snapshot)
        return "$granted مورد از $MEASURABLE_PERMISSION_COUNT دسترسی آماده است"
    }

    /**
     * حداقل لازم برای فعال‌سازی عملیاتی:
     * تشخیص تماس، شناسایی تماس از Call Log، و حداقل یک مسیر نمایش (Overlay یا اعلان).
     */
    fun meetsMinimumForEnable(snapshot: PostCallPermissionSnapshot): Boolean {
        if (!snapshot.canDetectCalls) return false
        if (!snapshot.callLogRuntimeAvailable) return false
        val hasPresentationPath = snapshot.canShowOverlay || snapshot.canNotify
        return hasPresentationPath
    }

    fun enableBlockedReason(snapshot: PostCallPermissionSnapshot): String? {
        if (!snapshot.phoneStateGranted) {
            return "برای تشخیص پایان تماس، دسترسی «تشخیص پایان تماس» را فعال کن."
        }
        if (!snapshot.callLogGranted) {
            return "برای شناخت شماره تماس، دسترسی «اطلاعات تماس‌ها» را فعال کن."
        }
        if (!snapshot.callLogRuntimeAvailable) {
            return "دسترسی تاریخچه تماس هنوز در دسترس نیست. از تنظیمات برنامه دوباره بررسی کن."
        }
        if (!snapshot.canShowOverlay && !snapshot.canNotify) {
            return "حداقل یکی از «نمایش دستیار روی صفحه» یا «اعلان‌های دستیار» را فعال کن."
        }
        return null
    }
}
