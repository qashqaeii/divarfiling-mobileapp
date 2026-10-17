package ir.divarfiling.mobile.feature.postcall

import android.provider.CallLog
import java.time.Instant
import java.time.ZoneId

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
        val windowStart = (notBeforeMs - lookbackMs).coerceAtLeast(0L)
        val windowEnd = endedAtMs + CALL_LOG_END_SLACK_MS
        val normalizedEntries = entries
            .asSequence()
            .filter { it.dateMs in windowStart..windowEnd }
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
            val pool = if (!hint.isNullOrBlank()) {
                matched.filter { it.number == hint }.ifEmpty { matched }
            } else {
                matched
            }
            return pool.minByOrNull { kotlin.math.abs(it.dateMs - endedAtMs) }
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
    private const val CALL_LOG_END_SLACK_MS = 8_000L

    private val PRIVATE_MARKERS = setOf(
        "unknown",
        "private",
        "anonymous",
        "مخفی",
        "نامشخص",
    )

    fun resolveStatusToMode(status: String): PostCallUiMode = when (status) {
        "existing_contact" -> PostCallUiMode.Existing
        "new_contact" -> PostCallUiMode.New
        "ambiguous" -> PostCallUiMode.Ambiguous
        "invalid_phone" -> PostCallUiMode.Invalid
        else -> PostCallUiMode.Error
    }

    /** زمان‌بندی یادآور پیگیری — منطق خالص برای تست. */
    object ReminderScheduling {
        const val DEFAULT_TOMORROW_HOUR = 10
        const val DEFAULT_TOMORROW_MINUTE = 0

        fun oneHourFromNow(now: Instant = Instant.now(), zone: ZoneId = ZoneId.systemDefault()): Instant =
            now.plusSeconds(3_600)

        fun tomorrowMorning(
            now: Instant = Instant.now(),
            zone: ZoneId = ZoneId.systemDefault(),
            hour: Int = DEFAULT_TOMORROW_HOUR,
            minute: Int = DEFAULT_TOMORROW_MINUTE,
        ): Instant {
            val zoned = now.atZone(zone)
            val tomorrow = zoned.toLocalDate().plusDays(1)
            return tomorrow.atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
                .atZone(zone)
                .toInstant()
        }

        fun pendingCallId(phone: String, kind: CallEndKind, callLogDateMs: Long?): String =
            listOf(phone, kind.name, callLogDateMs?.toString() ?: "0").joinToString("|")
    }
}
