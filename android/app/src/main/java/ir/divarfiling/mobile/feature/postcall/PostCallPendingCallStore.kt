package ir.divarfiling.mobile.feature.postcall

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.core.datastore.SessionStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

@Serializable
data class PendingPostCall(
    val id: String,
    val userId: Long,
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
    private val sessionStore: SessionStore,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
    private val _items = MutableStateFlow(readAllForCurrentUser())
    val items: StateFlow<List<PendingPostCall>> = _items.asStateFlow()

    fun refreshForCurrentUser() {
        _items.value = readAllForCurrentUser()
    }

    fun enqueue(metadata: PostCallCallMetadata, contactName: String?) {
        if (!PostCallFeatureFlags.PENDING_INBOX_ENABLED) return
        val userId = currentUserId() ?: return
        val id = PostCallSessionPolicy.eventIdentity(metadata)
        val now = System.currentTimeMillis()
        val current = readAllForUser(userId)
            .filter { it.createdAtMs > now - TTL_MS }
            .filterNot { it.id == id }
        val entry = PendingPostCall(
            id = id,
            userId = userId,
            phone = metadata.phone,
            callEndKind = metadata.callEndKind.name,
            callLogDateMs = metadata.callLogDateMs,
            durationSec = metadata.durationSec,
            createdAtMs = now,
            contactName = contactName?.takeIf { it.isNotBlank() },
        )
        persist(userId, current + entry)
    }

    fun remove(id: String) {
        val userId = currentUserId() ?: return
        persist(userId, readAllForUser(userId).filterNot { it.id == id })
    }

    fun removeForMetadata(metadata: PostCallCallMetadata) {
        remove(PostCallSessionPolicy.eventIdentity(metadata))
    }

    fun clear() {
        val userId = currentUserId()
        if (userId != null) {
            prefs.edit().remove(itemsKey(userId)).apply()
        } else {
            prefs.edit().clear().apply()
        }
        _items.value = emptyList()
    }

    fun clearAllUsers() {
        prefs.edit().clear().apply()
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

    private fun currentUserId(): Long? = runBlocking {
        sessionStore.getUser()?.id
    }

    private fun readAllForCurrentUser(): List<PendingPostCall> {
        val userId = currentUserId() ?: return emptyList()
        return readAllForUser(userId)
    }

    private fun readAllForUser(userId: Long): List<PendingPostCall> {
        val raw = prefs.getString(itemsKey(userId), null) ?: return emptyList()
        val now = System.currentTimeMillis()
        return runCatching { json.decodeFromString<List<PendingPostCall>>(raw) }
            .getOrDefault(emptyList())
            .filter { it.userId == userId && it.createdAtMs > now - TTL_MS }
    }

    private fun persist(userId: Long, list: List<PendingPostCall>) {
        val trimmed = list
            .sortedByDescending { it.createdAtMs }
            .take(MAX_ITEMS)
        prefs.edit().putString(itemsKey(userId), json.encodeToString(trimmed)).apply()
        if (currentUserId() == userId) {
            _items.value = trimmed
        }
    }

    private fun itemsKey(userId: Long) = "items_$userId"

    companion object {
        private const val PREFS = "post_call_pending_inbox_enc"
        private const val MAX_ITEMS = 40
        private const val TTL_MS = 14L * 24 * 60 * 60 * 1000
    }
}
