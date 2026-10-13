package ir.divarfiling.mobile.feature.postcall.call

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.core.util.PhoneNormalizer
import ir.divarfiling.mobile.feature.postcall.PostCallLogic
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallLogResolver @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun resolveRecentCall(
        afterWallClockMs: Long,
        endedAtMs: Long,
        hintedNumber: String?,
        callKind: PostCallLogic.CallEndKind,
        maxAttempts: Int = 8,
        delayMs: Long = 700L,
    ): PostCallLogic.ResolvedCallLog? {
        val hintedNormalized = hintedNumber
            ?.let { PhoneNormalizer.normalize(it) }
            ?.takeIf { PhoneNormalizer.isValidIranMobile(it) }

        if (callKind == PostCallLogic.CallEndKind.Ignored) return null

        repeat(maxAttempts) { attempt ->
            val entries = queryRecentEntries(limit = 20)
            val picked = PostCallLogic.pickCallLogEntry(
                entries = entries,
                kind = callKind,
                notBeforeMs = afterWallClockMs,
                endedAtMs = endedAtMs,
                hintedNormalized = hintedNormalized,
                normalize = PhoneNormalizer::normalize,
                isValidMobile = PhoneNormalizer::isValidIranMobile,
            )
            if (picked != null) {
                val awaitingDuration = callKind == PostCallLogic.CallEndKind.Answered &&
                    picked.durationSec <= 0L &&
                    attempt < maxAttempts - 1
                if (!awaitingDuration) return picked
            }
            if (attempt < maxAttempts - 1) delay(delayMs)
        }
        val hintOnly = hintedNormalized?.takeIf {
            callKind == PostCallLogic.CallEndKind.Missed || callKind == PostCallLogic.CallEndKind.Answered
        }
        return hintOnly?.let {
            PostCallLogic.ResolvedCallLog(
                phone = it,
                dateMs = 0L,
                durationSec = 0L,
                logType = -1,
            )
        }
    }

    suspend fun resolveRecentNumber(
        afterWallClockMs: Long,
        hintedNumber: String?,
        callKind: PostCallLogic.CallEndKind,
        maxAttempts: Int = 8,
        delayMs: Long = 700L,
    ): String? = resolveRecentCall(
        afterWallClockMs = afterWallClockMs,
        endedAtMs = System.currentTimeMillis(),
        hintedNumber = hintedNumber,
        callKind = callKind,
        maxAttempts = maxAttempts,
        delayMs = delayMs,
    )?.phone

    private fun queryRecentEntries(limit: Int): List<PostCallLogic.CallLogEntry> {
        if (!hasCallLogPermission()) return emptyList()
        val uri = CallLog.Calls.CONTENT_URI
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
        )
        val sort = "${CallLog.Calls.DATE} DESC"
        val cursor: Cursor? = try {
            context.contentResolver.query(uri, projection, null, null, sort)
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }
        val result = mutableListOf<PostCallLogic.CallLogEntry>()
        cursor?.use { c ->
            var count = 0
            while (c.moveToNext() && count < limit) {
                result.add(
                    PostCallLogic.CallLogEntry(
                        number = c.getString(c.getColumnIndexOrThrow(CallLog.Calls.NUMBER)).orEmpty(),
                        type = c.getInt(c.getColumnIndexOrThrow(CallLog.Calls.TYPE)),
                        dateMs = c.getLong(c.getColumnIndexOrThrow(CallLog.Calls.DATE)),
                        durationSec = c.getLong(c.getColumnIndexOrThrow(CallLog.Calls.DURATION)),
                    ),
                )
                count++
            }
        }
        return result
    }

    fun hasCallLogPermission(): Boolean {
        return try {
            context.checkSelfPermission(android.Manifest.permission.READ_CALL_LOG) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    }
}
