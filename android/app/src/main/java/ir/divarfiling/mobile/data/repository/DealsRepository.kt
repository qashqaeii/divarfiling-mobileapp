package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.network.DealCreateRequest
import ir.divarfiling.mobile.core.network.DealDto
import ir.divarfiling.mobile.core.network.DealPipelineData
import ir.divarfiling.mobile.core.network.DealStageRequest
import ir.divarfiling.mobile.core.network.DealStagesData
import ir.divarfiling.mobile.core.network.DealUpdateRequest
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.PaginatedResult
import ir.divarfiling.mobile.core.network.PropertyCreateRequest
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.core.network.PropertyStatusRequest
import ir.divarfiling.mobile.core.network.PropertyUpdateRequest
import ir.divarfiling.mobile.core.network.parseData
import ir.divarfiling.mobile.core.network.requireData
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DealsRepository @Inject constructor(
    private val api: MobileApi,
    private val json: Json,
) {
    suspend fun getDeals(
        query: String? = null,
        stage: String? = null,
        customerId: Long? = null,
        page: Int = 1,
    ): ApiResult<PaginatedResult<DealDto>> = paginatedList {
        api.getDeals(query = query?.ifBlank { null }, stage = stage, customerId = customerId, page = page)
    }

    suspend fun getDeal(dealId: Long): ApiResult<DealDto> = single {
        api.getDeal(dealId)
    }

    suspend fun getPipeline(): ApiResult<DealPipelineData> = try {
        val response = api.getDealPipeline()
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun getStages(): ApiResult<List<String>> = try {
        val response = api.getDealStages()
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(response.requireData<DealStagesData>(json).stages)
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun createDeal(request: DealCreateRequest): ApiResult<DealDto> = single {
        api.createDeal(request)
    }

    suspend fun updateDeal(dealId: Long, request: DealUpdateRequest): ApiResult<DealDto> = single {
        api.updateDeal(dealId, request)
    }

    suspend fun updateDealStage(dealId: Long, stage: String): ApiResult<DealDto> = single {
        api.updateDealStage(dealId, DealStageRequest(stage))
    }

    suspend fun getProperties(
        query: String? = null,
        transactionStatus: String? = null,
        page: Int = 1,
    ): ApiResult<PaginatedResult<PropertyDto>> = paginatedProperties {
        api.getProperties(
            query = query?.ifBlank { null },
            transactionStatus = transactionStatus?.ifBlank { null },
            page = page,
        )
    }

    suspend fun getProperty(propertyId: Long): ApiResult<PropertyDto> = single {
        api.getProperty(propertyId)
    }

    suspend fun createProperty(request: PropertyCreateRequest): ApiResult<PropertyDto> = single {
        api.createProperty(request)
    }

    suspend fun updateProperty(propertyId: Long, request: PropertyUpdateRequest): ApiResult<PropertyDto> = single {
        api.updateProperty(propertyId, request)
    }

    suspend fun updatePropertyStatus(propertyId: Long, status: String): ApiResult<PropertyDto> = single {
        api.updatePropertyStatus(propertyId, PropertyStatusRequest(status))
    }

    private suspend inline fun <reified T> single(
        crossinline call: suspend () -> ir.divarfiling.mobile.core.network.ApiEnvelope,
    ): ApiResult<T> = try {
        val response = call()
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    private suspend fun paginatedList(
        call: suspend () -> ir.divarfiling.mobile.core.network.ApiEnvelope,
    ): ApiResult<PaginatedResult<DealDto>> {
        return try {
            val response = call()
            if (!response.ok) return ApiResult.Error(response.error ?: "خطا")
            val list = response.data?.let {
                json.decodeFromJsonElement(ListSerializer(DealDto.serializer()), it)
            }.orEmpty()
            val page = response.meta?.page ?: 1
            val total = response.meta?.total ?: list.size
            ApiResult.Success(PaginatedResult(list, page, total, page * list.size < total))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    private suspend fun paginatedProperties(
        call: suspend () -> ir.divarfiling.mobile.core.network.ApiEnvelope,
    ): ApiResult<PaginatedResult<PropertyDto>> {
        return try {
            val response = call()
            if (!response.ok) return ApiResult.Error(response.error ?: "خطا")
            val list = response.data?.let {
                json.decodeFromJsonElement(ListSerializer(PropertyDto.serializer()), it)
            }.orEmpty()
            val page = response.meta?.page ?: 1
            val total = response.meta?.total ?: list.size
            ApiResult.Success(PaginatedResult(list, page, total, page * list.size < total))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }
}
