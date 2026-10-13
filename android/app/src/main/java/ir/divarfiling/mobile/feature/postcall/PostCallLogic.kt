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

    data class ResolvedCallLog(
        val phone: String,
        val dateMs: Long,
        val durationSec: Long,
        val logType: Int,
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

    fun matchesCallLogEntryStrict(entry: CallLogEntry, kind: CallEndKind): Boolean {
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

    fun matchesCallLogEntryRelaxed(entry: CallLogEntry, kind: CallEndKind): Boolean {
        if (!shouldAcceptCallLogType(entry.type, kind)) return false
        return when (kind) {
            CallEndKind.Answered -> entry.type == CallLog.Calls.INCOMING_TYPE
            else -> matchesCallLogEntryStrict(entry, kind)
        }
    }

    fun computeLookbackMs(sessionStartedAtMs: Long, endedAtMs: Long): Long {
        val sessionSpan = (endedAtMs - sessionStartedAtMs).coerceAtLeast(0L)
        return (sessionSpan + DEFAULT_LOOKBACK_MS).coerceAtMost(MAX_LOOKBACK_MS)
    }

    fun pickCallLogEntry(
        entries: List<CallLogEntry>,
        kind: CallEndKind,
        notBeforeMs: Long,
        endedAtMs: Long,
        hintedNormalized: String?,
        normalize: (String) -> String,
        isValidMobile: (String) -> Boolean,
    ): ResolvedCallLog? {
        val lookbackMs = computeLookbackMs(notBeforeMs, endedAtMs)
        val windowStart = notBeforeMs - DEFAULT_LOOKBACK_MS
        val normalizedEntries = entries
            .asSequence()
            .filter { it.dateMs >= windowStart }
            .filter { !isPrivateOrEmptyNumber(it.number) }
            .map { entry ->
                val normalized = normalize(entry.number)
                entry.copy(number = normalized)
            }
            .filter { isValidMobile(it.number) }
            .toList()

        fun pickWithMatcher(matcher: (CallLogEntry) -> Boolean): CallLogEntry? {
            val matched = normalizedEntries.filter { matcher(it) }
            if (matched.isEmpty()) return null
            val hint = hintedNormalized?.takeIf { isValidMobile(it) }
            if (!hint.isNullOrBlank()) {
                matched.firstOrNull { it.number == hint }?.let { return it }
            }
            return matched.maxByOrNull { it.dateMs }
        }

        val strict = pickWithMatcher { matchesCallLogEntryStrict(it, kind) }
        val chosen = strict ?: if (kind == CallEndKind.Answered) {
            pickWithMatcher { matchesCallLogEntryRelaxed(it, kind) }
        } else {
            null
        } ?: return null

        return ResolvedCallLog(
            phone = chosen.number,
            dateMs = chosen.dateMs,
            durationSec = chosen.durationSec,
            logType = chosen.type,
        )
    }

    fun pickCallLogNumber(
        entries: List<CallLogEntry>,
        kind: CallEndKind,
        notBeforeMs: Long,
        hintedNormalized: String?,
        normalize: (String) -> String,
        isValidMobile: (String) -> Boolean,
        lookbackMs: Long = 20_000L,
    ): String? {
        @Suppress("UNUSED_PARAMETER")
        val resolved = pickCallLogEntry(
            entries = entries,
            kind = kind,
            notBeforeMs = notBeforeMs,
            endedAtMs = notBeforeMs + lookbackMs,
            hintedNormalized = hintedNormalized,
            normalize = normalize,
            isValidMobile = isValidMobile,
        )
        return resolved?.phone
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

    private const val DEFAULT_LOOKBACK_MS = 20_000L
    private const val MAX_LOOKBACK_MS = 4 * 60 * 60 * 1000L

    private val PRIVATE_MARKERS = setOf(
        "unknown",
        "private",
        "anonymous",
        "مخفی",
        "نامشخص",
    )
}
