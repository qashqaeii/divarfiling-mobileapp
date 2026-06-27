package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.database.CachedDashboardEntity
import ir.divarfiling.mobile.core.database.DashboardCacheDao
import ir.divarfiling.mobile.core.network.DashboardData
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.parseData
import ir.divarfiling.mobile.core.network.requireData
import ir.divarfiling.mobile.core.network.toUserMessage
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val api: MobileApi,
    private val cache: DashboardCacheDao,
    private val json: Json,
) {
    suspend fun getDashboard(): ApiResult<DashboardData> {
        return try {
            val response = api.getDashboard()
            if (!response.ok) {
                val cached = cache.getLatest()?.payload?.let {
                    json.decodeFromString(DashboardData.serializer(), it)
                }
                if (cached != null) return ApiResult.Success(cached)
                return ApiResult.Error(response.error ?: "خطا در دریافت داشبورد")
            }
            val data = response.requireData<DashboardData>(json)
            cache.upsert(
                CachedDashboardEntity(
                    id = 1,
                    payload = json.encodeToString(DashboardData.serializer(), data),
                    cachedAt = System.currentTimeMillis(),
                ),
            )
            ApiResult.Success(data)
        } catch (e: Exception) {
            val cached = cache.getLatest()?.payload?.let {
                json.decodeFromString(DashboardData.serializer(), it)
            }
            if (cached != null) ApiResult.Success(cached)
            else ApiResult.Error(e.toUserMessage("خطا در اتصال به سرور"))
        }
    }
}
