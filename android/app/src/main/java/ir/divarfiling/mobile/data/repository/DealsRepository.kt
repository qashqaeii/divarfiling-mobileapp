package ir.divarfiling.mobile.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.core.network.DealCreateRequest
import ir.divarfiling.mobile.core.network.DealDto
import ir.divarfiling.mobile.core.network.DealPipelineData
import ir.divarfiling.mobile.core.network.DealStageRequest
import ir.divarfiling.mobile.core.network.DealStagesData
import ir.divarfiling.mobile.core.network.DealUpdateRequest
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.PaginatedResult
import ir.divarfiling.mobile.core.network.PropertyContactLinkDto
import ir.divarfiling.mobile.core.network.PropertyCreateRequest
import ir.divarfiling.mobile.core.network.PropertyDetailData
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.core.network.PropertyLinkContactRequest
import ir.divarfiling.mobile.core.network.PropertyStatusRequest
import ir.divarfiling.mobile.core.network.PropertyUpdateRequest
import ir.divarfiling.mobile.core.network.CustomerDocumentDto
import ir.divarfiling.mobile.core.network.parseData
import ir.divarfiling.mobile.core.network.requireData
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DealsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
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
        dealMode: String? = null,
        propertyType: String? = null,
        city: String? = null,
        transactionStatus: String? = null,
        page: Int = 1,
    ): ApiResult<PaginatedResult<PropertyDto>> = paginatedProperties {
        api.getProperties(
            query = query?.ifBlank { null },
            dealMode = dealMode?.ifBlank { null },
            propertyType = propertyType?.ifBlank { null },
            city = city?.ifBlank { null },
            transactionStatus = transactionStatus?.ifBlank { null },
            page = page,
        )
    }

    suspend fun getProperty(propertyId: Long): ApiResult<PropertyDetailData> = try {
        val response = api.getProperty(propertyId)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun linkPropertyContact(
        propertyId: Long,
        request: PropertyLinkContactRequest,
    ): ApiResult<PropertyContactLinkDto> = single {
        api.linkPropertyContact(propertyId, request)
    }

    suspend fun uploadPropertyDocument(
        propertyId: Long,
        uri: Uri,
        title: String = "",
        docType: String = "",
        note: String = "",
    ): ApiResult<CustomerDocumentDto> {
        return try {
            val resolver = context.contentResolver
            val mime = resolver.getType(uri) ?: "application/octet-stream"
            val fileName = title.ifBlank {
                resolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
                } ?: "document"
            }
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return ApiResult.Error("خواندن فایل ناموفق")
            val part = MultipartBody.Part.createFormData(
                "file",
                fileName,
                bytes.toRequestBody(mime.toMediaType()),
            )
            val response = api.uploadPropertyDocument(
                propertyId = propertyId,
                file = part,
                title = fileName.toRequestBody("text/plain".toMediaType()),
                docType = docType.toRequestBody("text/plain".toMediaType()),
                note = note.toRequestBody("text/plain".toMediaType()),
            )
            if (!response.ok) ApiResult.Error(response.error ?: "آپلود ناموفق")
            else ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun deletePropertyDocument(propertyId: Long, documentId: Long): ApiResult<Unit> = try {
        val response = api.deletePropertyDocument(propertyId, documentId)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
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

    suspend fun deleteProperty(propertyId: Long): ApiResult<Unit> = try {
        val response = api.deleteProperty(propertyId)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
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
