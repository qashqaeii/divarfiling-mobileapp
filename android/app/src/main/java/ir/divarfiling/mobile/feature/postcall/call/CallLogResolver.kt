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
        maxAttempts: Int = 3,
        delayMs: Long = 450L,
    ): String? {
        if (!PostCallLogic.isPrivateOrEmptyNumber(hintedNumber)) {
            val normalized = PhoneNormalizer.normalize(hintedNumber)
            if (PhoneNormalizer.isValidIranMobile(normalized)) return normalized
        }
        repeat(maxAttempts) { attempt ->
            val fromLog = queryLatestEligible(afterWallClockMs)
            if (!fromLog.isNullOrBlank()) return fromLog
            if (attempt < maxAttempts - 1) delay(delayMs)
        }
        return null
    }

    private fun queryLatestEligible(notBeforeMs: Long): String? {
        if (!hasCallLogPermission()) return null
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
        cursor?.use { c ->
            if (!c.moveToFirst()) return null
            val date = c.getLong(c.getColumnIndexOrThrow(CallLog.Calls.DATE))
            if (date < notBeforeMs - 5_000L) return null
            val type = c.getInt(c.getColumnIndexOrThrow(CallLog.Calls.TYPE))
            if (!PostCallLogic.shouldAcceptCallLogType(type)) return null
            val number = c.getString(c.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
            if (PostCallLogic.isPrivateOrEmptyNumber(number)) return null
            val normalized = PhoneNormalizer.normalize(number)
            return normalized.takeIf { PhoneNormalizer.isValidIranMobile(it) }
        }
        return null
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
