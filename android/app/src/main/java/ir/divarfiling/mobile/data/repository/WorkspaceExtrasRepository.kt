package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.network.AiDraftMessageRequest
import ir.divarfiling.mobile.core.network.AiQuotaData
import ir.divarfiling.mobile.core.network.AiTextResult
import ir.divarfiling.mobile.core.network.CloudExtractionCreateRequest
import ir.divarfiling.mobile.core.network.CloudExtractionJobDto
import ir.divarfiling.mobile.core.network.MessageTemplateDto
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.SavedFilterDto
import ir.divarfiling.mobile.core.network.SupportTicketCreateRequest
import ir.divarfiling.mobile.core.network.SupportTicketDto
import ir.divarfiling.mobile.core.network.requireData
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkspaceExtrasRepository @Inject constructor(
    private val api: MobileApi,
    private val json: Json,
) {
    suspend fun getMessageTemplates(): ApiResult<List<MessageTemplateDto>> = decodeList {
        json.decodeFromJsonElement(ListSerializer(MessageTemplateDto.serializer()), it)
    } { api.getMessageTemplates() }

    suspend fun getSavedFilters(entity: String? = null): ApiResult<List<SavedFilterDto>> = decodeList {
        json.decodeFromJsonElement(ListSerializer(SavedFilterDto.serializer()), it)
    } { api.getSavedFilters(entity = entity?.ifBlank { null }) }

    suspend fun getSupportTickets(): ApiResult<List<SupportTicketDto>> = decodeList {
        json.decodeFromJsonElement(ListSerializer(SupportTicketDto.serializer()), it)
    } { api.getSupportTickets() }

    suspend fun createSupportTicket(request: SupportTicketCreateRequest): ApiResult<SupportTicketDto> =
        single { api.createSupportTicket(request) }

    suspend fun getAiQuota(): ApiResult<AiQuotaData> = single { api.getAiQuota() }

    suspend fun aiDraftMessage(request: AiDraftMessageRequest): ApiResult<AiTextResult> =
        single { api.aiDraftMessage(request) }

    suspend fun createCloudExtraction(request: CloudExtractionCreateRequest): ApiResult<CloudExtractionJobDto> =
        single { api.createCloudExtraction(request) }

    suspend fun listCloudExtractions(): ApiResult<List<CloudExtractionJobDto>> = decodeList {
        json.decodeFromJsonElement(ListSerializer(CloudExtractionJobDto.serializer()), it)
    } { api.listCloudExtractions() }

    suspend fun getCloudExtraction(jobId: Long): ApiResult<CloudExtractionJobDto> = try {
        val response = api.getCloudExtraction(jobId)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(response.requireData(json))
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

    private suspend fun <T> decodeList(
        decode: (kotlinx.serialization.json.JsonElement) -> List<T>,
        call: suspend () -> ir.divarfiling.mobile.core.network.ApiEnvelope,
    ): ApiResult<List<T>> = try {
        val response = call()
        if (!response.ok) return ApiResult.Error(response.error ?: "خطا")
        val list = response.data?.let(decode).orEmpty()
        ApiResult.Success(list)
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }
}
