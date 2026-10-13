package ir.divarfiling.mobile.feature.postcall.call

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.feature.postcall.PostCallLogic
import ir.divarfiling.mobile.feature.postcall.debug.PostCallTrace
import javax.inject.Inject
import javax.inject.Singleton

data class PostCallIdlePayload(
    val snapshot: CallSessionSnapshot,
    val startedAtMs: Long,
)

@Singleton
class PostCallCallSessionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun onRinging(incomingNumber: String?) {
        if (prefs.getBoolean(KEY_OFFHOOK, false)) {
            PostCallTrace.log(PostCallTrace.Stage.State, "ignore ringing while offhook")
            return
        }
        val alreadyRinging = prefs.getBoolean(KEY_RINGING, false)
        val hint = when {
            !PostCallLogic.isPrivateOrEmptyNumber(incomingNumber) -> incomingNumber!!.trim()
            else -> prefs.getString(KEY_HINT, null)
        }
        prefs.edit().apply {
            putBoolean(KEY_RINGING, true)
            if (!alreadyRinging) {
                putLong(KEY_STARTED, System.currentTimeMillis())
            }
            putString(KEY_HINT, hint)
            apply()
        }
    }

    fun onOffHook() {
        val editor = prefs.edit().putBoolean(KEY_OFFHOOK, true)
        if (!prefs.getBoolean(KEY_RINGING, false)) {
            editor.putLong(KEY_STARTED, System.currentTimeMillis())
        }
        editor.apply()
    }

    fun consumeOnIdle(): PostCallIdlePayload? {
        val ringing = prefs.getBoolean(KEY_RINGING, false)
        val offHook = prefs.getBoolean(KEY_OFFHOOK, false)
        val startedAt = prefs.getLong(KEY_STARTED, 0L)
        val hint = prefs.getString(KEY_HINT, null)
        clear()
        val kind = PostCallLogic.classifyCallEnd(ringing, offHook)
        if (kind == null) return null
        PostCallTrace.log(
            PostCallTrace.Stage.State,
            "consume idle ringing=$ringing offhook=$offHook kind=${kind.name}",
        )
        return PostCallIdlePayload(
            snapshot = CallSessionSnapshot(kind = kind, hintedNumber = hint),
            startedAtMs = startedAt,
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS = "post_call_call_session"
        private const val KEY_RINGING = "ringing"
        private const val KEY_OFFHOOK = "offhook"
        private const val KEY_STARTED = "started_at"
        private const val KEY_HINT = "hint"

        internal fun idlePayloadFromPersisted(
            ringing: Boolean,
            offHook: Boolean,
            startedAtMs: Long,
            hint: String?,
        ): PostCallIdlePayload? {
            val kind = PostCallLogic.classifyCallEnd(ringing, offHook) ?: return null
            return PostCallIdlePayload(
                snapshot = CallSessionSnapshot(kind = kind, hintedNumber = hint),
                startedAtMs = startedAtMs,
            )
        }
    }
}

data class CallSessionSnapshot(
    val kind: PostCallLogic.CallEndKind?,
    val hintedNumber: String?,
)
