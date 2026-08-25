package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.network.ExportOtpVerifyRequest
import ir.divarfiling.mobile.core.network.ExportPasswordRequest
import ir.divarfiling.mobile.core.network.ExportResendOtpRequest
import ir.divarfiling.mobile.core.network.ExportSecurityChallengeData
import ir.divarfiling.mobile.core.network.ExportSecurityTokenData
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.mapApiError
import ir.divarfiling.mobile.core.network.parseData
import ir.divarfiling.mobile.core.network.requireData
import ir.divarfiling.mobile.core.network.toApiFailure
import ir.divarfiling.mobile.core.security.ExportSecurityStore
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportSecurityRepository @Inject constructor(
    private val api: MobileApi,
    private val store: ExportSecurityStore,
    private val json: Json,
) {
    fun hasValidToken(): Boolean = store.hasValidToken()

    fun getToken(): String? = store.getToken()

    suspend fun verifyPassword(password: String): ApiResult<ExportSecurityChallengeData> {
        return try {
            val response = api.exportSecurityVerifyPassword(ExportPasswordRequest(password.trim()))
            if (!response.ok) {
                return ApiResult.Error(
                    mapApiError(response.code, response.error, null, "تأیید رمز ناموفق"),
                    response.code,
                )
            }
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            val failure = e.toApiFailure("خطای شبکه")
            ApiResult.Error(failure.message, failure.code)
        }
    }

    suspend fun verifyOtp(
        passwordChallenge: String,
        code: String,
    ): ApiResult<ExportSecurityTokenData> {
        return try {
            val response = api.exportSecurityVerifyOtp(
                ExportOtpVerifyRequest(
                    passwordChallenge = passwordChallenge,
                    code = code.trim(),
                ),
            )
            if (!response.ok) {
                return ApiResult.Error(
                    mapApiError(response.code, response.error, null, "کد تأیید نامعتبر"),
                    response.code,
                )
            }
            val data = response.requireData<ExportSecurityTokenData>(json)
            store.saveToken(data.exportToken, data.expiresIn ?: 300L)
            ApiResult.Success(data)
        } catch (e: Exception) {
            val failure = e.toApiFailure("خطای شبکه")
            ApiResult.Error(failure.message, failure.code)
        }
    }

    suspend fun resendOtp(passwordChallenge: String): ApiResult<ExportSecurityChallengeData> {
        return try {
            val response = api.exportSecurityResendOtp(
                ExportResendOtpRequest(passwordChallenge = passwordChallenge),
            )
            if (!response.ok) {
                return ApiResult.Error(
                    mapApiError(response.code, response.error, null, "ارسال مجدد ناموفق"),
                    response.code,
                )
            }
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            val failure = e.toApiFailure("خطای شبکه")
            ApiResult.Error(failure.message, failure.code)
        }
    }
}
