package ir.divarfiling.mobile.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
        val LAST_SUCCESS_COUNT = intPreferencesKey("last_success_count")
        val AVG_DURATION_MINUTES = doublePreferencesKey("avg_duration_minutes")
        val EXTRACTION_SAMPLE_COUNT = intPreferencesKey("extraction_sample_count")
        val LAST_EXTRACTION_AT = longPreferencesKey("last_extraction_at")
    }

    data class SessionStats(
        val lastSuccessfulCount: Int = 0,
        val averageDurationMinutes: Double = 0.0,
        val lastExtractionAtMs: Long = 0L,
    )

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

    suspend fun getSessionStats(): SessionStats {
        val prefs = dataStore.data.first()
        return SessionStats(
            lastSuccessfulCount = prefs[Keys.LAST_SUCCESS_COUNT] ?: 0,
            averageDurationMinutes = prefs[Keys.AVG_DURATION_MINUTES] ?: 0.0,
            lastExtractionAtMs = prefs[Keys.LAST_EXTRACTION_AT] ?: 0L,
        )
    }

    suspend fun recordSuccessfulExtraction(ingestedCount: Int, durationMinutes: Double) {
        dataStore.edit { prefs ->
            val previousCount = prefs[Keys.EXTRACTION_SAMPLE_COUNT] ?: 0
            val previousAvg = prefs[Keys.AVG_DURATION_MINUTES] ?: 0.0
            val safeDuration = durationMinutes.coerceAtLeast(0.0)
            val newCount = previousCount + 1
            val newAvg = if (previousCount == 0) {
                safeDuration
            } else {
                ((previousAvg * previousCount) + safeDuration) / newCount
            }
            prefs[Keys.LAST_SUCCESS_COUNT] = ingestedCount.coerceAtLeast(0)
            prefs[Keys.AVG_DURATION_MINUTES] = newAvg
            prefs[Keys.EXTRACTION_SAMPLE_COUNT] = newCount
            prefs[Keys.LAST_EXTRACTION_AT] = System.currentTimeMillis()
        }
    }

    companion object {
        const val DEFAULT_INTERVAL_HOURS = 6.0
    }
}
