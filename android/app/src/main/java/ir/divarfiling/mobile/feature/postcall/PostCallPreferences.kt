package ir.divarfiling.mobile.feature.postcall

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.postCallDataStore: DataStore<Preferences> by preferencesDataStore("post_call_prefs")

@Singleton
class PostCallPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val runtimePrefs: PostCallRuntimePrefs,
) {
    private val dataStore = context.postCallDataStore

    private fun enabledKey(userId: Long) = booleanPreferencesKey("enabled_$userId")
    private fun promoDismissedKey(userId: Long) = booleanPreferencesKey("promo_dismissed_$userId")
    private fun incomingKey(userId: Long) = booleanPreferencesKey("incoming_$userId")
    private fun outgoingKey(userId: Long) = booleanPreferencesKey("outgoing_$userId")
    private fun missedKey(userId: Long) = booleanPreferencesKey("missed_$userId")
    private fun preferNotificationKey(userId: Long) = booleanPreferencesKey("prefer_notification_$userId")

    fun isEnabledFlow(userId: Long): Flow<Boolean> =
        dataStore.data.map { it[enabledKey(userId)] == true }

    suspend fun isEnabled(userId: Long): Boolean =
        dataStore.data.first()[enabledKey(userId)] == true

    suspend fun setEnabled(userId: Long, enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[enabledKey(userId)] = enabled
        }
        if (enabled) {
            runtimePrefs.setEnabledForUser(userId, true)
        } else {
            runtimePrefs.clear()
        }
    }

    suspend fun hasDismissedPromo(userId: Long): Boolean =
        dataStore.data.first()[promoDismissedKey(userId)] == true

    suspend fun dismissPromo(userId: Long) {
        dataStore.edit { prefs ->
            prefs[promoDismissedKey(userId)] = true
        }
    }

    suspend fun shouldShowPromo(userId: Long): Boolean {
        val prefs = dataStore.data.first()
        return prefs[enabledKey(userId)] != true && prefs[promoDismissedKey(userId)] != true
    }

    fun shouldShowPromoFlow(userId: Long): Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[enabledKey(userId)] != true && prefs[promoDismissedKey(userId)] != true
        }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
        runtimePrefs.clear()
    }

    fun isEnabledSyncForRuntime(userId: Long): Boolean = runtimePrefs.activeUserId() == userId && runtimePrefs.isEnabledForActiveUser()

    suspend fun shouldPresentCallKind(userId: Long, kind: PostCallLogic.CallEndKind): Boolean {
        val prefs = dataStore.data.first()
        return when (kind) {
            PostCallLogic.CallEndKind.Answered -> prefs[incomingKey(userId)] != false
            PostCallLogic.CallEndKind.Outgoing -> prefs[outgoingKey(userId)] != false
            PostCallLogic.CallEndKind.Missed -> prefs[missedKey(userId)] != false
            PostCallLogic.CallEndKind.Ignored -> false
        }
    }

    suspend fun setIncomingEnabled(userId: Long, enabled: Boolean) {
        dataStore.edit { it[incomingKey(userId)] = enabled }
    }

    suspend fun setOutgoingEnabled(userId: Long, enabled: Boolean) {
        dataStore.edit { it[outgoingKey(userId)] = enabled }
    }

    suspend fun setMissedEnabled(userId: Long, enabled: Boolean) {
        dataStore.edit { it[missedKey(userId)] = enabled }
    }

    suspend fun isPreferNotification(userId: Long): Boolean =
        dataStore.data.first()[preferNotificationKey(userId)] == true

    suspend fun setPreferNotification(userId: Long, prefer: Boolean) {
        dataStore.edit { it[preferNotificationKey(userId)] = prefer }
    }

    fun incomingEnabledFlow(userId: Long): Flow<Boolean> =
        dataStore.data.map { it[incomingKey(userId)] != false }

    fun outgoingEnabledFlow(userId: Long): Flow<Boolean> =
        dataStore.data.map { it[outgoingKey(userId)] != false }

    fun missedEnabledFlow(userId: Long): Flow<Boolean> =
        dataStore.data.map { it[missedKey(userId)] != false }

    fun preferNotificationFlow(userId: Long): Flow<Boolean> =
        dataStore.data.map { it[preferNotificationKey(userId)] == true }
}
