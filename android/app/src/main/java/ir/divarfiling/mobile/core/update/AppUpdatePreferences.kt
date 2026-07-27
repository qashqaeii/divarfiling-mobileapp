package ir.divarfiling.mobile.core.update

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appUpdateDataStore: DataStore<Preferences> by preferencesDataStore("app_update_prefs")

@Singleton
class AppUpdatePreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.appUpdateDataStore

    private object Keys {
        val DISMISSED_VERSION_CODE = intPreferencesKey("dismissed_version_code")
        val LAST_CHECK_AT = longPreferencesKey("last_check_at")
    }

    suspend fun getDismissedVersionCode(): Int =
        dataStore.data.first()[Keys.DISMISSED_VERSION_CODE] ?: 0

    suspend fun dismissVersion(versionCode: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.DISMISSED_VERSION_CODE] = versionCode
        }
    }

    suspend fun getLastCheckAt(): Long =
        dataStore.data.first()[Keys.LAST_CHECK_AT] ?: 0L

    suspend fun markCheckedNow() {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_CHECK_AT] = System.currentTimeMillis()
        }
    }

    companion object {
        const val SOFT_CHECK_INTERVAL_MS = 12L * 60L * 60L * 1000L
    }
}
