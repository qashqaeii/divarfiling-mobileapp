package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.network.GlobalSearchData
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.requireData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val api: MobileApi,
    private val json: kotlinx.serialization.json.Json,
) {
    suspend fun globalSearch(query: String, limit: Int = 10): ApiResult<GlobalSearchData> {
        return try {
            val trimmed = query.trim()
            if (trimmed.length < 2) {
                return ApiResult.Success(GlobalSearchData(query = trimmed))
            }
            val response = api.globalSearch(trimmed, limit)
            if (!response.ok) return ApiResult.Error(response.error ?: "جستجو ناموفق بود")
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }
}
