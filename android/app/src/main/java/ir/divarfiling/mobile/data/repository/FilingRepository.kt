package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.database.CachedDatasetEntity
import ir.divarfiling.mobile.core.database.DatasetCacheDao
import ir.divarfiling.mobile.core.network.DatasetDto
import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.core.network.ListingDto
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.PaginatedResult
import ir.divarfiling.mobile.core.network.requireData
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilingRepository @Inject constructor(
    private val api: MobileApi,
    private val datasetCache: DatasetCacheDao,
    private val json: Json,
) {
    suspend fun getDatasets(page: Int = 1, pageSize: Int = 20): ApiResult<PaginatedResult<DatasetDto>> {
        return try {
            val response = api.getDatasets(page = page, pageSize = pageSize)
            if (!response.ok) {
                if (page == 1) {
                    val cached = datasetCache.getAll().map { it.toDto() }
                    if (cached.isNotEmpty()) {
                        return ApiResult.Success(PaginatedResult(cached, 1, cached.size, false))
                    }
                }
                return ApiResult.Error(response.error ?: "خطا در دریافت فایلینگ")
            }
            val list = response.data?.let {
                json.decodeFromJsonElement(ListSerializer(DatasetDto.serializer()), it)
            }.orEmpty()
            if (page == 1) datasetCache.upsertAll(list.map { it.toEntity() })
            val total = response.meta?.total ?: list.size
            ApiResult.Success(PaginatedResult(list, page, total, page * pageSize < total))
        } catch (e: Exception) {
            if (page == 1) {
                val cached = datasetCache.getAll().map { it.toDto() }
                if (cached.isNotEmpty()) {
                    return ApiResult.Success(PaginatedResult(cached, 1, cached.size, false))
                }
            }
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun getListings(
        datasetId: String,
        query: String? = null,
        page: Int = 1,
        pageSize: Int = 50,
        priceMin: Long? = null,
        priceMax: Long? = null,
        areaMin: Int? = null,
        areaMax: Int? = null,
        rooms: Int? = null,
    ): ApiResult<PaginatedResult<ListingDto>> {
        return try {
            val response = api.getListings(
                datasetId = datasetId,
                page = page,
                pageSize = pageSize,
                query = query?.ifBlank { null },
                priceMin = priceMin,
                priceMax = priceMax,
                areaMin = areaMin,
                areaMax = areaMax,
                rooms = rooms,
            )
            if (!response.ok) {
                return ApiResult.Error(response.error ?: "خطا در دریافت آگهی‌ها")
            }
            val list = response.data?.let {
                json.decodeFromJsonElement(ListSerializer(ListingDto.serializer()), it)
            }.orEmpty()
            val total = response.meta?.total ?: list.size
            ApiResult.Success(PaginatedResult(list, page, total, page * pageSize < total))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun getListingDetail(token: String): ApiResult<ListingDetailDto> {
        return try {
            val response = api.getListingDetail(token)
            if (!response.ok) return ApiResult.Error(response.error ?: "خطا در دریافت آگهی")
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    private fun DatasetDto.toEntity() = CachedDatasetEntity(
        id = id,
        name = name,
        itemCount = itemCount,
        city = city,
        district = district,
        createdAt = createdAt,
    )

    private fun CachedDatasetEntity.toDto() = DatasetDto(
        id = id,
        name = name,
        itemCount = itemCount,
        city = city,
        district = district,
        createdAt = createdAt,
    )
}
