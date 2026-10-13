package ir.divarfiling.mobile.feature.postcall

import android.provider.CallLog

/** منطق خالص برای تست — بدون وابستگی به فریمورک تماس. */
object PostCallLogic {

    enum class CallEndKind {
        Answered,
        Outgoing,
        Missed,
        Ignored,
    }

    fun classifyCallEnd(hadRinging: Boolean, hadOffHook: Boolean): CallEndKind? {
        return when {
            hadOffHook -> if (hadRinging) CallEndKind.Answered else CallEndKind.Outgoing
            hadRinging -> CallEndKind.Missed
            else -> null
        }
    }

    fun shouldPresentAfterCall(kind: CallEndKind?): Boolean =
        kind == CallEndKind.Answered || kind == CallEndKind.Outgoing

    fun isPrivateOrEmptyNumber(raw: String?): Boolean {
        val s = raw?.trim().orEmpty()
        if (s.isBlank()) return true
        val lower = s.lowercase()
        return lower in PRIVATE_MARKERS || s == "-1"
    }

    fun shouldAcceptCallLogType(type: Int): Boolean =
        type == CallLog.Calls.INCOMING_TYPE || type == CallLog.Calls.OUTGOING_TYPE

    fun shouldSuppressDuplicate(lastPhone: String?, lastAtMs: Long, phone: String, nowMs: Long, windowMs: Long): Boolean {
        if (lastPhone.isNullOrBlank()) return false
        if (lastPhone != phone) return false
        return nowMs - lastAtMs < windowMs
    }

    private val PRIVATE_MARKERS = setOf(
        "unknown",
        "private",
        "anonymous",
        "مخفی",
        "نامشخص",
    )
}
