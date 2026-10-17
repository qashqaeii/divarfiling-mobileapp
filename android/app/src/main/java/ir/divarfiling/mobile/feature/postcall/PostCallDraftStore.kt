package ir.divarfiling.mobile.feature.postcall

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PostCallDraft(
    val callKey: String,
    val userId: Long,
    val note: String = "",
    val leadName: String = "",
    val outcome: String? = null,
    val reminderMillis: Long? = null,
    val followUpReminderEnabled: Boolean = false,
    val leadCustomerType: String = "سرنخ",
    val leadBudgetMin: String = "",
    val leadBudgetMax: String = "",
    val leadPropertyType: String = "",
    val leadAreas: String = "",
    val updatedAtMs: Long = System.currentTimeMillis(),
)

@Singleton
class PostCallDraftStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun read(userId: Long, metadata: PostCallCallMetadata): PostCallDraft? {
        val raw = prefs.getString(storageKey(userId, metadata), null) ?: return null
        return runCatching { json.decodeFromString<PostCallDraft>(raw) }.getOrNull()
    }

    fun save(userId: Long, metadata: PostCallCallMetadata, draft: PostCallDraft) {
        prefs.edit()
            .putString(storageKey(userId, metadata), json.encodeToString(draft))
            .apply()
    }

    fun clear(userId: Long, metadata: PostCallCallMetadata) {
        prefs.edit().remove(storageKey(userId, metadata)).apply()
    }

    fun clearAllForUser(userId: Long) {
        val prefix = "draft_${userId}_"
        prefs.edit().apply {
            prefs.all.keys.filter { it.startsWith(prefix) }.forEach { remove(it) }
        }.apply()
    }

    private fun storageKey(userId: Long, metadata: PostCallCallMetadata): String {
        val callKey = PostCallSessionPolicy.eventIdentity(metadata)
        return "draft_${userId}_$callKey"
    }

    companion object {
        private const val PREFS_NAME = "post_call_drafts_enc"
    }
}
