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
import ir.divarfiling.mobile.core.security.SecureTokenStore
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
    private val secureTokenStore: SecureTokenStore,
) {
    private val dataStore = context.sessionDataStore

    private object Keys {
        val LEGACY_ACCESS = stringPreferencesKey("access_token")
        val LEGACY_REFRESH = stringPreferencesKey("refresh_token")
        val DEVICE_ID = stringPreferencesKey("device_id")
        val USER_JSON = stringPreferencesKey("user_json")
        val LICENSE_VALID = booleanPreferencesKey("license_valid")
        val LICENSE_PLAN = stringPreferencesKey("license_plan")
        val LICENSE_EXPIRES = stringPreferencesKey("license_expires")
        val LICENSE_DAYS_REMAINING = longPreferencesKey("license_days_remaining")
        val LICENSE_EXPIRING_SOON = booleanPreferencesKey("license_expiring_soon")
        val LICENSE_ID = longPreferencesKey("license_id")
        val LICENSE_CAN_RENEW = booleanPreferencesKey("license_can_renew")
        val LICENSE_STATUS = stringPreferencesKey("license_status")
        val PENDING_ORDER_ID = stringPreferencesKey("pending_order_id")
        val FEATURE_LIGHT_EXTRACT = booleanPreferencesKey("feature_light_extract")
        val FEATURE_CRM = booleanPreferencesKey("feature_crm")
        val FEATURE_FILING = booleanPreferencesKey("feature_filing")
        val USER_ID = longPreferencesKey("user_id")
        val ACCESS_EXPIRES_AT = longPreferencesKey("access_expires_at")
        val LAST_LICENSE_CHECK_AT = longPreferencesKey("last_license_check_at")
        val NOTIFICATION_ONBOARDING_SEEN = booleanPreferencesKey("notification_onboarding_seen")
        val FCM_WELCOME_SHOWN = booleanPreferencesKey("fcm_welcome_shown")
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
        !secureTokenStore.getAccessToken().isNullOrBlank() || !prefs[Keys.LEGACY_ACCESS].isNullOrBlank()
    }

    val licenseState: Flow<LicenseState> = dataStore.data.map { prefs -> licenseFromPrefs(prefs) }

    suspend fun getAccessToken(): String? {
        migrateLegacyTokens()
        return secureTokenStore.getAccessToken()
            ?: dataStore.data.first()[Keys.LEGACY_ACCESS]?.takeIf { it.isNotBlank() }
    }

    suspend fun hasValidSession(): Boolean = !getAccessToken().isNullOrBlank()

    suspend fun getRefreshToken(): String? {
        migrateLegacyTokens()
        return secureTokenStore.getRefreshToken()
            ?: dataStore.data.first()[Keys.LEGACY_REFRESH]?.takeIf { it.isNotBlank() }
    }

    suspend fun getDeviceId(): String? = dataStore.data.first()[Keys.DEVICE_ID]

    suspend fun saveSession(
        access: String,
        refresh: String,
        user: UserDto,
        deviceId: String,
        license: LicenseDto? = null,
    ) {
        secureTokenStore.saveTokens(access, refresh)
        dataStore.edit { prefs ->
            prefs.remove(Keys.LEGACY_ACCESS)
            prefs.remove(Keys.LEGACY_REFRESH)
            prefs[Keys.ACCESS_EXPIRES_AT] = System.currentTimeMillis() + DEFAULT_ACCESS_TTL_MS
            prefs[Keys.DEVICE_ID] = deviceId
            prefs[Keys.USER_JSON] = json.encodeToString(user)
            prefs[Keys.USER_ID] = user.id
            prefs.remove(Keys.FCM_WELCOME_SHOWN)
            applyLicense(prefs, license)
        }
    }

    suspend fun updateTokens(access: String, refresh: String?) {
        secureTokenStore.saveTokens(access, refresh)
        dataStore.edit { prefs ->
            prefs.remove(Keys.LEGACY_ACCESS)
            prefs.remove(Keys.LEGACY_REFRESH)
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
        licenseId: Long? = null,
        canRenew: Boolean = false,
        status: String? = null,
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
            prefs[Keys.LICENSE_ID] = licenseId ?: 0L
            prefs[Keys.LICENSE_CAN_RENEW] = canRenew
            prefs[Keys.LICENSE_STATUS] = status.orEmpty()
            prefs[Keys.FEATURE_LIGHT_EXTRACT] = features?.lightExtract == true && valid
            prefs[Keys.FEATURE_CRM] = valid && features?.crmMobile == true
            prefs[Keys.FEATURE_FILING] = valid && features?.filingView == true
            prefs[Keys.LAST_LICENSE_CHECK_AT] = System.currentTimeMillis()
        }
    }

    suspend fun invalidateLicense() {
        dataStore.edit { prefs ->
            prefs[Keys.LICENSE_VALID] = false
            prefs[Keys.FEATURE_LIGHT_EXTRACT] = false
            prefs[Keys.FEATURE_CRM] = false
            prefs[Keys.FEATURE_FILING] = false
        }
    }

    suspend fun isLicenseStale(maxAgeMs: Long = LICENSE_STALE_MS): Boolean {
        val lastCheck = dataStore.data.first()[Keys.LAST_LICENSE_CHECK_AT] ?: return true
        return System.currentTimeMillis() - lastCheck > maxAgeMs
    }

    suspend fun clear() {
        secureTokenStore.clear()
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

    suspend fun claimFcmWelcomeShown(): Boolean {
        var claimed = false
        dataStore.edit { prefs ->
            if (prefs[Keys.FCM_WELCOME_SHOWN] != true) {
                prefs[Keys.FCM_WELCOME_SHOWN] = true
                claimed = true
            }
        }
        return claimed
    }

    suspend fun setPendingOrderId(orderId: String?) {
        dataStore.edit { prefs ->
            if (orderId.isNullOrBlank()) prefs.remove(Keys.PENDING_ORDER_ID)
            else prefs[Keys.PENDING_ORDER_ID] = orderId
        }
    }

    suspend fun getPendingOrderId(): String? = dataStore.data.first()[Keys.PENDING_ORDER_ID]

    val pendingOrderId: Flow<String?> = dataStore.data.map { it[Keys.PENDING_ORDER_ID] }

    suspend fun setLastSyncAt(iso: String) {
        dataStore.edit { prefs -> prefs[Keys.LAST_SYNC_AT] = iso }
    }

    suspend fun getLastSyncAt(): String? =
        dataStore.data.first()[Keys.LAST_SYNC_AT]?.takeIf { it.isNotBlank() }

    private suspend fun migrateLegacyTokens() {
        if (!secureTokenStore.getAccessToken().isNullOrBlank()) return
        val prefs = dataStore.data.first()
        val legacy = prefs[Keys.LEGACY_ACCESS]?.takeIf { it.isNotBlank() } ?: return
        secureTokenStore.saveTokens(legacy, prefs[Keys.LEGACY_REFRESH])
        dataStore.edit {
            it.remove(Keys.LEGACY_ACCESS)
            it.remove(Keys.LEGACY_REFRESH)
        }
    }

    private fun licenseFromPrefs(prefs: Preferences): LicenseState {
        val valid = prefs[Keys.LICENSE_VALID] ?: false
        return LicenseState(
            valid = valid,
            plan = prefs[Keys.LICENSE_PLAN],
            expiresAt = prefs[Keys.LICENSE_EXPIRES],
            daysRemaining = prefs[Keys.LICENSE_DAYS_REMAINING]?.toInt(),
            expiringSoon = prefs[Keys.LICENSE_EXPIRING_SOON] == true,
            lightExtractEnabled = prefs[Keys.FEATURE_LIGHT_EXTRACT] ?: false,
            crmEnabled = valid && (prefs[Keys.FEATURE_CRM] ?: false),
            filingEnabled = valid && (prefs[Keys.FEATURE_FILING] ?: false),
            licenseId = prefs[Keys.LICENSE_ID]?.takeIf { it > 0 },
            canRenew = prefs[Keys.LICENSE_CAN_RENEW] == true,
            status = prefs[Keys.LICENSE_STATUS],
        )
    }

    companion object {
        private const val DEFAULT_ACCESS_TTL_MS = 15L * 60L * 1000L
        private const val LICENSE_STALE_MS = 6L * 60L * 60L * 1000L
    }

    private fun applyLicense(prefs: androidx.datastore.preferences.core.MutablePreferences, license: LicenseDto?) {
        val valid = license?.valid == true
        prefs[Keys.LICENSE_VALID] = valid
        prefs[Keys.LICENSE_PLAN] = license?.plan ?: ""
        prefs[Keys.LICENSE_EXPIRES] = license?.expiresAt ?: ""
        val light = license?.features?.lightExtract == true ||
            license?.mobileExtractEnabled == true
        prefs[Keys.FEATURE_LIGHT_EXTRACT] = valid && light
        prefs[Keys.FEATURE_CRM] = valid && license?.features?.crmMobile == true
        prefs[Keys.FEATURE_FILING] = valid && license?.features?.filingView == true
        prefs[Keys.LAST_LICENSE_CHECK_AT] = System.currentTimeMillis()
    }
}
