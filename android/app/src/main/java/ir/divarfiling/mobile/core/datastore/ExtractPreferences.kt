package ir.divarfiling.mobile.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.extractPrefsDataStore: DataStore<Preferences> by preferencesDataStore("extract_prefs")

@Singleton
class ExtractPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.extractPrefsDataStore

    private object Keys {
        val SCHEDULE_INTERVAL_HOURS = doublePreferencesKey("schedule_interval_hours")
    }

    val scheduleIntervalHours: Flow<Double> = dataStore.data.map { prefs ->
        prefs[Keys.SCHEDULE_INTERVAL_HOURS] ?: DEFAULT_INTERVAL_HOURS
    }

    suspend fun getScheduleIntervalHours(): Double =
        dataStore.data.first()[Keys.SCHEDULE_INTERVAL_HOURS] ?: DEFAULT_INTERVAL_HOURS

    suspend fun setScheduleIntervalHours(hours: Double) {
        dataStore.edit { prefs ->
            prefs[Keys.SCHEDULE_INTERVAL_HOURS] = hours
        }
    }

    companion object {
        const val DEFAULT_INTERVAL_HOURS = 6.0
    }
}
