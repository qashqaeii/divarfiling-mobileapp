package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.NotificationPrefsDto
import ir.divarfiling.mobile.core.network.ProfileUpdateRequest
import ir.divarfiling.mobile.core.network.UserDto
import ir.divarfiling.mobile.core.network.requireData
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val api: MobileApi,
    private val sessionStore: SessionStore,
    private val json: Json,
) {
    suspend fun getProfile(): ApiResult<UserDto> {
        return try {
            val response = api.getProfile()
            if (!response.ok) return ApiResult.Error(response.error ?: "خطا در دریافت پروفایل")
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun updateProfile(fullName: String, phone: String): ApiResult<UserDto> {
        return try {
            val response = api.updateProfile(
                ProfileUpdateRequest(fullName = fullName.trim(), phone = phone.trim()),
            )
            if (!response.ok) return ApiResult.Error(response.error ?: "ذخیره پروفایل ناموفق")
            val user = response.requireData<UserDto>(json)
            sessionStore.updateUser(user)
            ApiResult.Success(user)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun getNotificationPrefs(): ApiResult<NotificationPrefsDto> {
        return try {
            val response = api.getNotificationPrefs()
            if (!response.ok) return ApiResult.Error(response.error ?: "خطا در دریافت تنظیمات اعلان")
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun updateNotificationPrefs(prefs: NotificationPrefsDto): ApiResult<NotificationPrefsDto> {
        return try {
            val response = api.updateNotificationPrefs(prefs)
            if (!response.ok) return ApiResult.Error(response.error ?: "ذخیره تنظیمات ناموفق")
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }
}
