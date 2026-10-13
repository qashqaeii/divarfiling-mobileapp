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

    data class CallLogEntry(
        val number: String,
        val type: Int,
        val dateMs: Long,
        val durationSec: Long,
    )

    fun classifyCallEnd(hadRinging: Boolean, hadOffHook: Boolean): CallEndKind? {
        return when {
            hadOffHook -> if (hadRinging) CallEndKind.Answered else CallEndKind.Outgoing
            hadRinging -> CallEndKind.Missed
            else -> null
        }
    }

    fun shouldPresentAfterCall(kind: CallEndKind?): Boolean =
        kind == CallEndKind.Answered ||
            kind == CallEndKind.Outgoing ||
            kind == CallEndKind.Missed

    fun isPrivateOrEmptyNumber(raw: String?): Boolean {
        val s = raw?.trim().orEmpty()
        if (s.isBlank()) return true
        val lower = s.lowercase()
        return lower in PRIVATE_MARKERS || s == "-1"
    }

    fun shouldAcceptCallLogType(type: Int, kind: CallEndKind): Boolean = when (kind) {
        CallEndKind.Outgoing -> type == CallLog.Calls.OUTGOING_TYPE
        CallEndKind.Answered -> type == CallLog.Calls.INCOMING_TYPE
        CallEndKind.Missed -> type == CallLog.Calls.MISSED_TYPE || type == CallLog.Calls.INCOMING_TYPE
        CallEndKind.Ignored -> false
    }

    /** @deprecated از [shouldAcceptCallLogType] با kind استفاده کنید. */
    fun shouldAcceptCallLogType(type: Int): Boolean =
        type == CallLog.Calls.INCOMING_TYPE ||
            type == CallLog.Calls.OUTGOING_TYPE ||
            type == CallLog.Calls.MISSED_TYPE

    fun matchesCallLogEntryForKind(entry: CallLogEntry, kind: CallEndKind): Boolean {
        if (!shouldAcceptCallLogType(entry.type, kind)) return false
        return when (kind) {
            CallEndKind.Outgoing -> entry.type == CallLog.Calls.OUTGOING_TYPE
            CallEndKind.Answered -> entry.type == CallLog.Calls.INCOMING_TYPE && entry.durationSec > 0
            CallEndKind.Missed -> {
                when (entry.type) {
                    CallLog.Calls.MISSED_TYPE -> true
                    CallLog.Calls.INCOMING_TYPE -> entry.durationSec == 0L
                    else -> false
                }
            }
            CallEndKind.Ignored -> false
        }
    }

    /**
     * انتخاب رکورد Call Log در بازه زمانی؛ اولویت با hint معتبر، سپس جدیدترین تطبیق نوع.
     */
    fun pickCallLogNumber(
        entries: List<CallLogEntry>,
        kind: CallEndKind,
        notBeforeMs: Long,
        hintedNormalized: String?,
        normalize: (String) -> String,
        isValidMobile: (String) -> Boolean,
        lookbackMs: Long = 20_000L,
    ): String? {
        val windowStart = notBeforeMs - lookbackMs
        val inWindow = entries
            .asSequence()
            .filter { it.dateMs >= windowStart }
            .filter { !isPrivateOrEmptyNumber(it.number) }
            .map { entry ->
                val normalized = normalize(entry.number)
                entry.copy(number = normalized)
            }
            .filter { isValidMobile(it.number) }
            .filter { matchesCallLogEntryForKind(it, kind) }
            .toList()

        if (inWindow.isEmpty()) return null

        val hint = hintedNormalized?.takeIf { isValidMobile(it) }
        if (!hint.isNullOrBlank()) {
            inWindow.firstOrNull { it.number == hint }?.let { return it.number }
        }

        return inWindow.maxByOrNull { it.dateMs }?.number
    }

    fun shouldSuppressDuplicate(
        lastPhone: String?,
        lastAtMs: Long,
        phone: String,
        nowMs: Long,
        windowMs: Long,
    ): Boolean {
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
