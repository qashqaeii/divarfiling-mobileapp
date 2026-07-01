package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.BuildConfig
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.network.DeviceRegisterRequest
import ir.divarfiling.mobile.core.network.LicenseDto
import ir.divarfiling.mobile.core.network.LicenseStatusData
import ir.divarfiling.mobile.core.network.LoginData
import ir.divarfiling.mobile.core.network.LoginRequest
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.RefreshData
import ir.divarfiling.mobile.core.network.RefreshRequest
import ir.divarfiling.mobile.core.network.UserDto
import ir.divarfiling.mobile.core.network.parseData
import ir.divarfiling.mobile.core.network.requireData
import ir.divarfiling.mobile.core.util.DeviceIdProvider
import ir.divarfiling.mobile.core.fcm.FcmTokenProvider
import ir.divarfiling.mobile.core.fcm.FcmTokenSync
import ir.divarfiling.mobile.core.security.LocalDataWiper
import ir.divarfiling.mobile.core.sync.BackgroundWorkManager
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: String? = null) : ApiResult<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val api: MobileApi,
    private val sessionStore: SessionStore,
    private val deviceIdProvider: DeviceIdProvider,
    private val licenseRepository: LicenseRepository,
    private val fcmTokenProvider: FcmTokenProvider,
    private val fcmTokenSync: FcmTokenSync,
    private val localDataWiper: LocalDataWiper,
    private val json: Json,
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context,
) {
    val isLoggedIn = sessionStore.isLoggedIn
    val licenseState = sessionStore.licenseState

    suspend fun login(username: String, password: String): ApiResult<UserDto> {
        return try {
            val response = api.login(LoginRequest(username.trim(), password))
            if (!response.ok) {
                return ApiResult.Error(response.error ?: "ورود ناموفق")
            }
            val data = response.requireData<LoginData>(json)
            val deviceId = deviceIdProvider.getDeviceId()
            localDataWiper.wipeUserData()
            sessionStore.saveSession(
                access = data.access,
                refresh = data.refresh,
                user = data.user,
                deviceId = deviceId,
            )
            data.expiresIn?.let { seconds ->
                sessionStore.saveAccessExpiresAt(System.currentTimeMillis() + seconds * 1000L)
            }
            registerDevice(deviceId)
            licenseRepository.refreshLicense()
            BackgroundWorkManager.register(appContext)
            ApiResult.Success(data.user)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    private suspend fun registerDevice(deviceId: String): LicenseDto? {
        return try {
            val fcmToken = fcmTokenProvider.fetchToken().orEmpty()
            val response = api.registerDevice(
                DeviceRegisterRequest(
                    deviceId = deviceId,
                    deviceModel = android.os.Build.MODEL,
                    osVersion = android.os.Build.VERSION.RELEASE,
                    appVersion = BuildConfig.VERSION_NAME,
                    fcmToken = fcmToken,
                ),
            )
            fcmTokenSync.syncWithRetry()
            val license = response.parseData<DeviceRegisterData>(json)?.license
            sessionStore.saveLicense(license)
            license
        } catch (_: Exception) {
            null
        }
    }

    suspend fun logout() {
        try {
            val refresh = sessionStore.getRefreshToken()
            if (!refresh.isNullOrBlank()) {
                api.logout(RefreshRequest(refresh))
            }
        } catch (_: Exception) {
        } finally {
            localDataWiper.wipeUserData()
            sessionStore.clear()
            BackgroundWorkManager.cancel(appContext)
        }
    }

    suspend fun ensureFreshAccessToken(): ApiResult<Unit> {
        if (!sessionStore.isAccessTokenExpiringSoon()) {
            return ApiResult.Success(Unit)
        }
        return refreshAccessToken()
    }

    suspend fun refreshAccessToken(): ApiResult<Unit> {
        val refresh = sessionStore.getRefreshToken()
        if (refresh.isNullOrBlank()) {
            return ApiResult.Error("نشست شما منقضی شده است. دوباره وارد شوید.", "AUTH_EXPIRED")
        }
        return try {
            val response = api.refresh(RefreshRequest(refresh))
            if (!response.ok) {
                sessionStore.clear()
                return ApiResult.Error(
                    response.error ?: "نشست شما منقضی شده است. دوباره وارد شوید.",
                    response.code ?: "AUTH_EXPIRED",
                )
            }
            val data = response.requireData<RefreshData>(json)
            sessionStore.updateTokens(data.access, data.refresh)
            val ttlMs = (data.expiresIn ?: 900L) * 1000L
            sessionStore.saveAccessExpiresAt(System.currentTimeMillis() + ttlMs)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه در تمدید نشست")
        }
    }
}

@kotlinx.serialization.Serializable
private data class DeviceRegisterData(
    @kotlinx.serialization.SerialName("device_id") val deviceId: String? = null,
    val license: LicenseDto? = null,
)

@Singleton
class LicenseRepository @Inject constructor(
    private val api: MobileApi,
    private val sessionStore: SessionStore,
    private val json: Json,
) {
    suspend fun refreshLicense(): ApiResult<Unit> {
        return try {
            val response = api.licenseStatus()
            if (!response.ok) {
                if (response.code == "LICENSE_REQUIRED" || response.code == "AUTH_EXPIRED") {
                    sessionStore.invalidateLicense()
                } else if (sessionStore.isLicenseStale()) {
                    sessionStore.invalidateLicense()
                }
                return ApiResult.Error(response.error ?: "خطا در دریافت لایسنس", response.code)
            }
            val data = response.requireData<LicenseStatusData>(json)
            sessionStore.saveLicenseFromStatus(
                valid = data.valid,
                plan = data.plan,
                expiresAt = data.expiresAt,
                features = data.features,
                daysRemaining = data.daysRemaining,
                expiringSoon = data.expiringSoon,
            )
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            if (sessionStore.isLicenseStale()) {
                sessionStore.invalidateLicense()
            }
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }
}
