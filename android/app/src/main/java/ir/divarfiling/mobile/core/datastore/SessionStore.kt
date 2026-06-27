package ir.divarfiling.mobile.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.core.license.LicenseState
import ir.divarfiling.mobile.core.network.LicenseDto
import ir.divarfiling.mobile.core.network.LicenseFeaturesDto
import ir.divarfiling.mobile.core.network.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore("session")

@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {
    private val dataStore = context.sessionDataStore

    private object Keys {
        val ACCESS = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
        val DEVICE_ID = stringPreferencesKey("device_id")
        val USER_JSON = stringPreferencesKey("user_json")
        val LICENSE_VALID = booleanPreferencesKey("license_valid")
        val LICENSE_PLAN = stringPreferencesKey("license_plan")
        val LICENSE_EXPIRES = stringPreferencesKey("license_expires")
        val LICENSE_DAYS_REMAINING = longPreferencesKey("license_days_remaining")
        val LICENSE_EXPIRING_SOON = booleanPreferencesKey("license_expiring_soon")
        val FEATURE_LIGHT_EXTRACT = booleanPreferencesKey("feature_light_extract")
        val FEATURE_CRM = booleanPreferencesKey("feature_crm")
        val FEATURE_FILING = booleanPreferencesKey("feature_filing")
        val USER_ID = longPreferencesKey("user_id")
        val ACCESS_EXPIRES_AT = longPreferencesKey("access_expires_at")
        val NOTIFICATION_ONBOARDING_SEEN = booleanPreferencesKey("notification_onboarding_seen")
        val LAST_SYNC_AT = stringPreferencesKey("last_sync_at")
    }

    val currentUser: Flow<UserDto?> = dataStore.data.map { prefs ->
        prefs[Keys.USER_JSON]?.let { raw ->
            runCatching { json.decodeFromString<UserDto>(raw) }.getOrNull()
        }
    }

    suspend fun getUser(): UserDto? = dataStore.data.first()[Keys.USER_JSON]?.let { raw ->
        runCatching { json.decodeFromString<UserDto>(raw) }.getOrNull()
    }

    suspend fun updateUser(user: UserDto) {
        dataStore.edit { prefs ->
            prefs[Keys.USER_JSON] = json.encodeToString(user)
            prefs[Keys.USER_ID] = user.id
        }
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        !prefs[Keys.ACCESS].isNullOrBlank()
    }

    val licenseState: Flow<LicenseState> = dataStore.data.map { prefs ->
        LicenseState(
            valid = prefs[Keys.LICENSE_VALID] ?: false,
            plan = prefs[Keys.LICENSE_PLAN],
            expiresAt = prefs[Keys.LICENSE_EXPIRES],
            daysRemaining = prefs[Keys.LICENSE_DAYS_REMAINING]?.toInt(),
            expiringSoon = prefs[Keys.LICENSE_EXPIRING_SOON] == true,
            lightExtractEnabled = prefs[Keys.FEATURE_LIGHT_EXTRACT] ?: false,
            crmEnabled = prefs[Keys.FEATURE_CRM] ?: true,
            filingEnabled = prefs[Keys.FEATURE_FILING] ?: true,
        )
    }

    suspend fun getAccessToken(): String? = dataStore.data.first()[Keys.ACCESS]
    suspend fun getRefreshToken(): String? = dataStore.data.first()[Keys.REFRESH]
    suspend fun getDeviceId(): String? = dataStore.data.first()[Keys.DEVICE_ID]

    suspend fun saveSession(
        access: String,
        refresh: String,
        user: UserDto,
        deviceId: String,
        license: LicenseDto? = null,
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.ACCESS] = access
            prefs[Keys.REFRESH] = refresh
            prefs[Keys.ACCESS_EXPIRES_AT] = System.currentTimeMillis() + DEFAULT_ACCESS_TTL_MS
            prefs[Keys.DEVICE_ID] = deviceId
            prefs[Keys.USER_JSON] = json.encodeToString(user)
            prefs[Keys.USER_ID] = user.id
            applyLicense(prefs, license)
        }
    }

    suspend fun updateTokens(access: String, refresh: String?) {
        dataStore.edit { prefs ->
            prefs[Keys.ACCESS] = access
            if (!refresh.isNullOrBlank()) {
                prefs[Keys.REFRESH] = refresh
            }
        }
    }

    suspend fun saveAccessExpiresAt(expiresAtEpochMs: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.ACCESS_EXPIRES_AT] = expiresAtEpochMs
        }
    }

    suspend fun isAccessTokenExpiringSoon(withinMs: Long = 120_000L): Boolean {
        val expiresAt = dataStore.data.first()[Keys.ACCESS_EXPIRES_AT] ?: return false
        return System.currentTimeMillis() + withinMs >= expiresAt
    }

    suspend fun saveLicense(license: LicenseDto?) {
        dataStore.edit { prefs -> applyLicense(prefs, license) }
    }

    suspend fun saveLicenseFromStatus(
        valid: Boolean,
        plan: String?,
        expiresAt: String?,
        features: LicenseFeaturesDto?,
        daysRemaining: Int? = null,
        expiringSoon: Boolean = false,
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.LICENSE_VALID] = valid
            prefs[Keys.LICENSE_PLAN] = plan ?: ""
            prefs[Keys.LICENSE_EXPIRES] = expiresAt ?: ""
            if (daysRemaining != null) {
                prefs[Keys.LICENSE_DAYS_REMAINING] = daysRemaining.toLong()
            } else {
                prefs.remove(Keys.LICENSE_DAYS_REMAINING)
            }
            prefs[Keys.LICENSE_EXPIRING_SOON] = expiringSoon
            prefs[Keys.FEATURE_LIGHT_EXTRACT] = features?.lightExtract == true && valid
            prefs[Keys.FEATURE_CRM] = features?.crmMobile != false
            prefs[Keys.FEATURE_FILING] = features?.filingView != false
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    suspend fun hasSeenNotificationOnboarding(): Boolean {
        return dataStore.data.first()[Keys.NOTIFICATION_ONBOARDING_SEEN] == true
    }

    suspend fun setNotificationOnboardingSeen() {
        dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATION_ONBOARDING_SEEN] = true
        }
    }

    suspend fun getLastSyncAt(): String? = dataStore.data.first()[Keys.LAST_SYNC_AT]

    suspend fun setLastSyncAt(iso: String) {
        dataStore.edit { prefs -> prefs[Keys.LAST_SYNC_AT] = iso }
    }

    companion object {
        private const val DEFAULT_ACCESS_TTL_MS = 15L * 60L * 1000L
    }

    private fun applyLicense(prefs: androidx.datastore.preferences.core.MutablePreferences, license: LicenseDto?) {
        val valid = license?.valid == true
        prefs[Keys.LICENSE_VALID] = valid
        prefs[Keys.LICENSE_PLAN] = license?.plan ?: ""
        prefs[Keys.LICENSE_EXPIRES] = license?.expiresAt ?: ""
        val light = license?.features?.lightExtract == true ||
            license?.mobileExtractEnabled == true
        prefs[Keys.FEATURE_LIGHT_EXTRACT] = valid && light
        prefs[Keys.FEATURE_CRM] = license?.features?.crmMobile != false
        prefs[Keys.FEATURE_FILING] = license?.features?.filingView != false
    }
}
