package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.database.CachedContactEntity
import ir.divarfiling.mobile.core.database.CachedDatasetEntity
import ir.divarfiling.mobile.core.database.ContactCacheDao
import ir.divarfiling.mobile.core.database.DatasetCacheDao
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.DatasetDto
import ir.divarfiling.mobile.core.network.ListingDto
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.QuickLeadRequest
import ir.divarfiling.mobile.core.network.TodayData
import ir.divarfiling.mobile.core.network.parseData
import ir.divarfiling.mobile.core.network.requireData
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrmRepository @Inject constructor(
    private val api: MobileApi,
    private val contactCache: ContactCacheDao,
    private val json: Json,
) {
    suspend fun getContacts(query: String? = null): ApiResult<List<ContactDto>> {
        return try {
            val response = api.getContacts(query = query?.ifBlank { null })
            if (!response.ok) {
                val cached = contactCache.getAll().map { it.toDto() }
                if (cached.isNotEmpty()) return ApiResult.Success(cached)
                return ApiResult.Error(response.error ?: "خطا در دریافت مخاطبین")
            }
            val list = response.data?.let {
                json.decodeFromJsonElement(ListSerializer(ContactDto.serializer()), it)
            }.orEmpty()
            contactCache.upsertAll(list.map { it.toEntity() })
            ApiResult.Success(list)
        } catch (e: Exception) {
            val cached = contactCache.getAll().map { it.toDto() }
            if (cached.isNotEmpty()) ApiResult.Success(cached)
            else ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun quickLead(fullName: String, phone: String): ApiResult<ContactDto> {
        return try {
            val response = api.quickLead(QuickLeadRequest(fullName, phone))
            if (!response.ok) {
                return ApiResult.Error(response.error ?: "ثبت سرنخ ناموفق")
            }
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun getToday(): ApiResult<TodayData> {
        return try {
            val response = api.getToday()
            if (!response.ok) {
                return ApiResult.Error(response.error ?: "خطا در دریافت کارهای امروز")
            }
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    private fun ContactDto.toEntity() = CachedContactEntity(
        id = id,
        fullName = fullName,
        phone = phone,
        customerType = customerType,
        status = status,
        updatedAt = updatedAt,
    )

    private fun CachedContactEntity.toDto() = ContactDto(
        id = id,
        fullName = fullName,
        phone = phone,
        customerType = customerType,
        status = status,
        updatedAt = updatedAt,
    )
}

@Singleton
class FilingRepository @Inject constructor(
    private val api: MobileApi,
    private val datasetCache: DatasetCacheDao,
    private val json: Json,
) {
    suspend fun getDatasets(): ApiResult<List<DatasetDto>> {
        return try {
            val response = api.getDatasets()
            if (!response.ok) {
                val cached = datasetCache.getAll().map { it.toDto() }
                if (cached.isNotEmpty()) return ApiResult.Success(cached)
                return ApiResult.Error(response.error ?: "خطا در دریافت فایلینگ")
            }
            val list = response.data?.let {
                json.decodeFromJsonElement(ListSerializer(DatasetDto.serializer()), it)
            }.orEmpty()
            datasetCache.upsertAll(list.map { it.toEntity() })
            ApiResult.Success(list)
        } catch (e: Exception) {
            val cached = datasetCache.getAll().map { it.toDto() }
            if (cached.isNotEmpty()) ApiResult.Success(cached)
            else ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun getListings(datasetId: String, query: String? = null): ApiResult<List<ListingDto>> {
        return try {
            val response = api.getListings(datasetId, query = query?.ifBlank { null })
            if (!response.ok) {
                return ApiResult.Error(response.error ?: "خطا در دریافت آگهی‌ها")
            }
            val list = response.data?.let {
                json.decodeFromJsonElement(ListSerializer(ListingDto.serializer()), it)
            }.orEmpty()
            ApiResult.Success(list)
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
