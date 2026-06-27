package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.NotificationDto
import ir.divarfiling.mobile.core.network.PaginatedResult
import ir.divarfiling.mobile.core.network.parseData
import ir.divarfiling.mobile.core.network.requireData
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val api: MobileApi,
    private val json: Json,
) {
    suspend fun getNotifications(
        page: Int = 1,
        pageSize: Int = 20,
    ): ApiResult<PaginatedResult<NotificationDto>> {
        return try {
            val response = api.getNotifications(page = page, pageSize = pageSize)
            if (!response.ok) {
                return ApiResult.Error(response.error ?: "خطا در دریافت اعلان‌ها")
            }
            val list = response.data?.let {
                json.decodeFromJsonElement(ListSerializer(NotificationDto.serializer()), it)
            }.orEmpty()
            val total = response.meta?.total ?: list.size
            val hasMore = page * pageSize < total
            ApiResult.Success(PaginatedResult(list, page, total, hasMore))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun getUnreadCount(): ApiResult<Int> {
        return try {
            val response = api.getUnreadNotificationCount()
            if (!response.ok) return ApiResult.Error(response.error ?: "خطا")
            val count = response.parseData<UnreadCountData>(json)?.count ?: 0
            ApiResult.Success(count)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun markRead(notificationId: Long): ApiResult<Unit> {
        return try {
            val response = api.markNotificationRead(notificationId)
            if (!response.ok) return ApiResult.Error(response.error ?: "خطا")
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }
}

@kotlinx.serialization.Serializable
private data class UnreadCountData(val count: Int = 0)
