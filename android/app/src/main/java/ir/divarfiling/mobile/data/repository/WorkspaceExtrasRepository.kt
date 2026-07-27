package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.network.AiDraftMessageRequest
import ir.divarfiling.mobile.core.network.AiQuotaData
import ir.divarfiling.mobile.core.network.AiTextResult
import ir.divarfiling.mobile.core.network.ApiEnvelope
import ir.divarfiling.mobile.core.network.CloudExtractionCreateRequest
import ir.divarfiling.mobile.core.network.CloudExtractionJobDto
import ir.divarfiling.mobile.core.network.MessageTemplateDto
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.SavedFilterCreateRequest
import ir.divarfiling.mobile.core.network.SavedFilterDto
import ir.divarfiling.mobile.core.network.SupportTicketCreateRequest
import ir.divarfiling.mobile.core.network.SupportTicketDto
import ir.divarfiling.mobile.core.network.SupportTicketReplyResult
import ir.divarfiling.mobile.core.network.requireData
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkspaceExtrasRepository @Inject constructor(
    private val api: MobileApi,
    private val json: Json,
) {
    suspend fun getMessageTemplates(): ApiResult<List<MessageTemplateDto>> {
        return decodeList(
            decode = { el ->
                json.decodeFromJsonElement(ListSerializer(MessageTemplateDto.serializer()), el)
            },
            call = { api.getMessageTemplates() },
        )
    }

    suspend fun getSavedFilters(
        entity: String? = null,
        includeNewCount: Boolean = false,
    ): ApiResult<List<SavedFilterDto>> {
        return decodeList(
            decode = { el ->
                json.decodeFromJsonElement(ListSerializer(SavedFilterDto.serializer()), el)
            },
            call = {
                api.getSavedFilters(
                    entity = entity?.ifBlank { null },
                    includeNewCount = if (includeNewCount) 1 else null,
                )
            },
        )
    }

    suspend fun createSavedFilter(request: SavedFilterCreateRequest): ApiResult<SavedFilterDto> =
        single { api.createSavedFilter(request) }

    suspend fun deleteSavedFilter(filterId: Long): ApiResult<Unit> {
        return try {
            val response = api.deleteSavedFilter(filterId)
            if (!response.ok) ApiResult.Error(response.error ?: "حذف فیلتر ناموفق")
            else ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun pinSavedFilter(filterId: Long): ApiResult<SavedFilterDto> =
        single { api.pinSavedFilter(filterId) }

    suspend fun getSupportTickets(): ApiResult<List<SupportTicketDto>> {
        return decodeList(
            decode = { el ->
                json.decodeFromJsonElement(ListSerializer(SupportTicketDto.serializer()), el)
            },
            call = { api.getSupportTickets() },
        )
    }

    suspend fun createSupportTicket(request: SupportTicketCreateRequest): ApiResult<SupportTicketDto> =
        single { api.createSupportTicket(request) }

    suspend fun getSupportTicket(ticketId: Long): ApiResult<SupportTicketDto> =
        single { api.getSupportTicket(ticketId) }

    suspend fun replySupportTicket(
        ticketId: Long,
        body: String,
        attachmentFile: File? = null,
        attachmentMime: String = "application/octet-stream",
    ): ApiResult<SupportTicketReplyResult> {
        return try {
            val bodyPart = body.toRequestBody("text/plain".toMediaType())
            val filePart = attachmentFile?.let { file ->
                MultipartBody.Part.createFormData(
                    "attachment",
                    file.name,
                    file.asRequestBody(attachmentMime.toMediaType()),
                )
            }
            val response = api.replySupportTicket(ticketId, bodyPart, filePart)
            if (!response.ok) ApiResult.Error(response.error ?: "ارسال پاسخ ناموفق")
            else ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun closeSupportTicket(ticketId: Long): ApiResult<SupportTicketDto> =
        single { api.closeSupportTicket(ticketId) }

    suspend fun reopenSupportTicket(ticketId: Long): ApiResult<SupportTicketDto> =
        single { api.reopenSupportTicket(ticketId) }

    suspend fun getAiQuota(): ApiResult<AiQuotaData> = single { api.getAiQuota() }

    suspend fun aiDraftMessage(request: AiDraftMessageRequest): ApiResult<AiTextResult> =
        single { api.aiDraftMessage(request) }

    suspend fun createCloudExtraction(request: CloudExtractionCreateRequest): ApiResult<CloudExtractionJobDto> =
        single { api.createCloudExtraction(request) }

    suspend fun listCloudExtractions(): ApiResult<List<CloudExtractionJobDto>> {
        return decodeList(
            decode = { el ->
                json.decodeFromJsonElement(ListSerializer(CloudExtractionJobDto.serializer()), el)
            },
            call = { api.listCloudExtractions() },
        )
    }

    suspend fun getCloudExtraction(jobId: Long): ApiResult<CloudExtractionJobDto> = try {
        val response = api.getCloudExtraction(jobId)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    private suspend inline fun <reified T> single(
        crossinline call: suspend () -> ApiEnvelope,
    ): ApiResult<T> = try {
        val response = call()
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    private suspend fun <T> decodeList(
        decode: (JsonElement) -> List<T>,
        call: suspend () -> ApiEnvelope,
    ): ApiResult<List<T>> {
        return try {
            val response = call()
            if (!response.ok) {
                ApiResult.Error(response.error ?: "خطا")
            } else {
                val list = response.data?.let(decode).orEmpty()
                ApiResult.Success(list)
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }
}
