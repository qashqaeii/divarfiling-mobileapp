package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.BuildConfig
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.network.DeviceRegisterRequest
import ir.divarfiling.mobile.core.network.LicenseDto
import ir.divarfiling.mobile.core.network.LicenseStatusData
import ir.divarfiling.mobile.core.network.LoginData
import ir.divarfiling.mobile.core.network.LoginRequest
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.OtpChallengeData
import ir.divarfiling.mobile.core.network.OtpRequestBody
import ir.divarfiling.mobile.core.network.OtpVerifyBody
import ir.divarfiling.mobile.core.network.PasswordCompleteRequest
import ir.divarfiling.mobile.core.network.RefreshData
import ir.divarfiling.mobile.core.network.RefreshRequest
import ir.divarfiling.mobile.core.network.UserDto
import ir.divarfiling.mobile.core.network.parseData
import ir.divarfiling.mobile.core.network.requireData
import ir.divarfiling.mobile.core.network.mapApiError
import ir.divarfiling.mobile.core.network.toApiFailure
import ir.divarfiling.mobile.core.network.toUserMessage
import ir.divarfiling.mobile.core.util.DeviceIdProvider
import ir.divarfiling.mobile.core.fcm.FcmTokenProvider
import ir.divarfiling.mobile.core.fcm.FcmTokenSync
import ir.divarfiling.mobile.core.notifications.NotificationDedupStore
import ir.divarfiling.mobile.core.notifications.ReminderSyncManager
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
    private val reminderSyncManager: ReminderSyncManager,
    private val notificationDedupStore: NotificationDedupStore,
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
                return ApiResult.Error(
                    mapApiError(response.code, response.error, null, "ورود ناموفق"),
                    response.code,
                )
            }
            val data = response.requireData<LoginData>(json)
            persistAuthenticatedSession(data)
            ApiResult.Success(data.user)
        } catch (e: Exception) {
            val failure = e.toApiFailure("خطای شبکه")
            ApiResult.Error(failure.message, failure.code)
        }
    }

    private suspend fun persistAuthenticatedSession(data: LoginData): UserDto {
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
        reminderSyncManager.rescheduleFromServer()
        BackgroundWorkManager.register(appContext)
        return data.user
    }

    suspend fun requestOtp(phone: String, purpose: String): ApiResult<OtpChallengeData> {
        return try {
            val response = api.otpRequest(OtpRequestBody(phone.trim(), purpose))
            if (!response.ok) {
                return envelopeError(response, "ارسال کد ناموفق بود")
            }
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            e.asApiError("ارسال کد ناموفق بود")
        }
    }

    suspend fun resendOtp(phone: String, purpose: String): ApiResult<OtpChallengeData> {
        return try {
            val response = api.otpResend(OtpRequestBody(phone.trim(), purpose))
            if (!response.ok) {
                return envelopeError(response, "ارسال مجدد ناموفق بود")
            }
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            e.asApiError("ارسال مجدد ناموفق بود")
        }
    }

    suspend fun loginWithOtp(phone: String, code: String): ApiResult<UserDto> {
        return try {
            val response = api.otpVerify(OtpVerifyBody(phone.trim(), "login", code.trim()))
            if (!response.ok) {
                return envelopeError(response, "کد تأیید نادرست است")
            }
            val data = response.requireData<LoginData>(json)
            persistAuthenticatedSession(data)
            ApiResult.Success(data.user)
        } catch (e: Exception) {
            e.asApiError("کد تأیید نادرست است")
        }
    }

    suspend fun verifyOtpStep(phone: String, purpose: String, code: String): ApiResult<OtpChallengeData> {
        return try {
            val response = api.otpVerify(OtpVerifyBody(phone.trim(), purpose, code.trim()))
            if (!response.ok) {
                return envelopeError(response, "کد تأیید نادرست است")
            }
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            e.asApiError("کد تأیید نادرست است")
        }
    }

    suspend fun completeRegister(
        phone: String,
        challengeToken: String,
        password: String,
        passwordConfirm: String,
    ): ApiResult<UserDto> {
        return try {
            val response = api.completeRegister(
                PasswordCompleteRequest(phone.trim(), challengeToken, password, passwordConfirm),
            )
            if (!response.ok) {
                return envelopeError(response, "ثبت‌نام ناموفق بود")
            }
            val data = response.requireData<LoginData>(json)
            persistAuthenticatedSession(data)
            ApiResult.Success(data.user)
        } catch (e: Exception) {
            e.asApiError("ثبت‌نام ناموفق بود")
        }
    }

    suspend fun completePasswordReset(
        phone: String,
        challengeToken: String,
        password: String,
        passwordConfirm: String,
    ): ApiResult<Unit> {
        return try {
            val response = api.completePasswordReset(
                PasswordCompleteRequest(phone.trim(), challengeToken, password, passwordConfirm),
            )
            if (!response.ok) {
                return envelopeError(response, "تغییر رمز ناموفق بود")
            }
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            e.asApiError("تغییر رمز ناموفق بود")
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
            reminderSyncManager.clearAll()
            notificationDedupStore.clear()
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
                val expired = response.code == "AUTH_EXPIRED"
                if (expired) sessionStore.clear()
                return ApiResult.Error(
                    mapApiError(response.code, response.error, null, "تمدید نشست ناموفق بود"),
                    response.code,
                )
            }
            val data = response.requireData<RefreshData>(json)
            sessionStore.updateTokens(data.access, data.refresh)
            val ttlMs = (data.expiresIn ?: 900L) * 1000L
            sessionStore.saveAccessExpiresAt(System.currentTimeMillis() + ttlMs)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            val failure = e.toApiFailure("خطای شبکه در تمدید نشست")
            if (failure.httpCode == 401 || failure.code == "AUTH_EXPIRED") {
                sessionStore.clear()
            }
            ApiResult.Error(failure.message, failure.code ?: "NETWORK_ERROR")
        }
    }

    private fun envelopeError(
        response: ir.divarfiling.mobile.core.network.ApiEnvelope,
        default: String,
    ): ApiResult.Error = ApiResult.Error(
        mapApiError(response.code, response.error, null, default),
        response.code,
    )

    private fun Exception.asApiError(default: String): ApiResult.Error {
        val failure = toApiFailure(default)
        return ApiResult.Error(failure.message, failure.code)
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
                if (response.code == "LICENSE_REQUIRED") {
                    sessionStore.invalidateLicense()
                }
                return ApiResult.Error(
                    mapApiError(response.code, response.error, null, "بررسی لایسنس ناموفق بود"),
                    response.code,
                )
            }
            val data = response.requireData<LicenseStatusData>(json)
            sessionStore.saveLicenseFromStatus(
                valid = data.valid,
                plan = data.plan,
                expiresAt = data.expiresAt,
                features = data.features,
                daysRemaining = data.daysRemaining,
                expiringSoon = data.expiringSoon,
                licenseId = data.licenseId,
                canRenew = data.canRenew,
                status = data.status,
            )
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.toUserMessage("بررسی لایسنس ناموفق بود. اتصال را بررسی کنید."), "NETWORK_ERROR")
        }
    }
}
