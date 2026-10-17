package ir.divarfiling.mobile.feature.postcall

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PendingPostCall(
    val id: String,
    val phone: String,
    val callEndKind: String,
    val callLogDateMs: Long?,
    val durationSec: Long?,
    val createdAtMs: Long,
    val contactName: String?,
)

@Singleton
class PostCallPendingCallStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val _items = MutableStateFlow(readAll())
    val items: StateFlow<List<PendingPostCall>> = _items.asStateFlow()

    fun enqueue(metadata: PostCallCallMetadata, contactName: String?) {
        if (!PostCallFeatureFlags.PENDING_INBOX_ENABLED) return
        val id = PostCallLogic.ReminderScheduling.pendingCallId(
            metadata.phone,
            metadata.callEndKind,
            metadata.callLogDateMs,
        )
        val current = readAll().filterNot { it.id == id }
        val entry = PendingPostCall(
            id = id,
            phone = metadata.phone,
            callEndKind = metadata.callEndKind.name,
            callLogDateMs = metadata.callLogDateMs,
            durationSec = metadata.durationSec,
            createdAtMs = System.currentTimeMillis(),
            contactName = contactName?.takeIf { it.isNotBlank() },
        )
        persist(current + entry)
    }

    fun remove(id: String) {
        persist(readAll().filterNot { it.id == id })
    }

    fun removeForMetadata(metadata: PostCallCallMetadata) {
        val id = PostCallLogic.ReminderScheduling.pendingCallId(
            metadata.phone,
            metadata.callEndKind,
            metadata.callLogDateMs,
        )
        remove(id)
    }

    fun clear() {
        prefs.edit().remove(KEY_ITEMS).apply()
        _items.value = emptyList()
    }

    fun toMetadata(item: PendingPostCall): PostCallCallMetadata? {
        val kind = runCatching { PostCallLogic.CallEndKind.valueOf(item.callEndKind) }.getOrNull()
            ?: return null
        return PostCallCallMetadata(
            phone = item.phone,
            callEndKind = kind,
            callLogDateMs = item.callLogDateMs,
            durationSec = item.durationSec,
            callLogType = null,
        )
    }

    private fun readAll(): List<PendingPostCall> {
        val raw = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
        return runCatching { json.decodeFromString<List<PendingPostCall>>(raw) }.getOrDefault(emptyList())
    }

    private fun persist(list: List<PendingPostCall>) {
        val trimmed = list
            .sortedByDescending { it.createdAtMs }
            .take(MAX_ITEMS)
        prefs.edit().putString(KEY_ITEMS, json.encodeToString(trimmed)).apply()
        _items.value = trimmed
    }

    companion object {
        private const val PREFS = "post_call_pending_inbox"
        private const val KEY_ITEMS = "items"
        private const val MAX_ITEMS = 40
    }
}
