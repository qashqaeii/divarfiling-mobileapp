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
    suspend fun resolveRecentNumber(
        afterWallClockMs: Long,
        hintedNumber: String?,
        callKind: PostCallLogic.CallEndKind,
        maxAttempts: Int = 5,
        delayMs: Long = 600L,
    ): String? {
        val hintedNormalized = hintedNumber
            ?.let { PhoneNormalizer.normalize(it) }
            ?.takeIf { PhoneNormalizer.isValidIranMobile(it) }

        if (callKind == PostCallLogic.CallEndKind.Ignored) return null

        repeat(maxAttempts) { attempt ->
            val entries = queryRecentEntries(limit = 12)
            val picked = PostCallLogic.pickCallLogNumber(
                entries = entries,
                kind = callKind,
                notBeforeMs = afterWallClockMs,
                hintedNormalized = hintedNormalized,
                normalize = PhoneNormalizer::normalize,
                isValidMobile = PhoneNormalizer::isValidIranMobile,
            )
            if (!picked.isNullOrBlank()) return picked
            if (attempt < maxAttempts - 1) delay(delayMs)
        }
        return hintedNormalized?.takeIf {
            callKind == PostCallLogic.CallEndKind.Missed || callKind == PostCallLogic.CallEndKind.Answered
        }
    }

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
