package ir.divarfiling.mobile.feature.postcall.debug

import android.util.Log
import ir.divarfiling.mobile.feature.postcall.PostCallFeatureFlags

object PostCallTrace {
    enum class Stage {
        Receiver,
        State,
        CallLog,
        Resolve,
        Presentation,
        Displayed,
        Notification,
        Skipped,
    }

    fun log(stage: Stage, detail: String) {
        if (!PostCallFeatureFlags.ENABLED) return
        Log.i(TAG, "[${stage.name}] $detail")
    }

    fun maskPhone(phone: String?): String {
        val digits = phone?.filter { it.isDigit() } ?: return "?"
        if (digits.length < 4) return "****"
        return "****${digits.takeLast(4)}"
    }

    private const val TAG = "PostCallTrace"
}
