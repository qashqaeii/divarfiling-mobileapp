package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.network.AiDraftMessageRequest
import ir.divarfiling.mobile.core.network.AiQuotaData
import ir.divarfiling.mobile.core.network.AiSummarizeListingRequest
import ir.divarfiling.mobile.core.network.AiSummarizePropertyRequest
import ir.divarfiling.mobile.core.network.AiTextResult
import ir.divarfiling.mobile.core.network.ApiEnvelope
import ir.divarfiling.mobile.core.network.CloudExtractionCreateRequest
import ir.divarfiling.mobile.core.network.CloudExtractionJobDto
import ir.divarfiling.mobile.core.network.MessageTemplateDto
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.PropertyCabinetCreateRequest
import ir.divarfiling.mobile.core.network.PropertyCabinetDto
import ir.divarfiling.mobile.core.network.PropertyCabinetReorderRequest
import ir.divarfiling.mobile.core.network.PropertyCabinetUpdateRequest
import ir.divarfiling.mobile.core.network.PropertyFolderCreateRequest
import ir.divarfiling.mobile.core.network.PropertyFolderDto
import ir.divarfiling.mobile.core.network.PropertyFolderReorderRequest
import ir.divarfiling.mobile.core.network.PropertyFolderUpdateRequest
import ir.divarfiling.mobile.core.network.SavedFilterCreateRequest
import ir.divarfiling.mobile.core.network.SavedFilterDto
import ir.divarfiling.mobile.core.network.SupportTicketCreateRequest
import ir.divarfiling.mobile.core.network.SupportTicketDto
import ir.divarfiling.mobile.core.network.SupportTicketStatsDto
import ir.divarfiling.mobile.core.network.SupportTicketsPage
import ir.divarfiling.mobile.core.network.SupportTicketReplyResult
import ir.divarfiling.mobile.core.network.requireData
import ir.divarfiling.mobile.core.network.toUserMessage
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
            ApiResult.Error(e.toUserMessage("خطای شبکه"))
        }
    }

    suspend fun pinSavedFilter(filterId: Long): ApiResult<SavedFilterDto> =
        single { api.pinSavedFilter(filterId) }

    suspend fun getPropertyFolders(cabinetId: String? = null): ApiResult<List<PropertyFolderDto>> {
        return decodeList(
            decode = { el ->
                json.decodeFromJsonElement(ListSerializer(PropertyFolderDto.serializer()), el)
            },
            call = { api.getPropertyFolders(cabinetId?.ifBlank { null }) },
        )
    }

    suspend fun createPropertyFolder(request: PropertyFolderCreateRequest): ApiResult<PropertyFolderDto> =
        single { api.createPropertyFolder(request) }

    suspend fun updatePropertyFolder(
        folderId: Long,
        request: PropertyFolderUpdateRequest,
    ): ApiResult<PropertyFolderDto> = single { api.updatePropertyFolder(folderId, request) }

    suspend fun deletePropertyFolder(folderId: Long): ApiResult<Unit> {
        return try {
            val response = api.deletePropertyFolder(folderId)
            if (!response.ok) ApiResult.Error(response.error ?: "حذف زونکن ناموفق")
            else ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.toUserMessage("خطای شبکه"))
        }
    }

    suspend fun reorderPropertyFolders(folderIds: List<Long>): ApiResult<List<PropertyFolderDto>> {
        return decodeList(
            decode = { el ->
                json.decodeFromJsonElement(ListSerializer(PropertyFolderDto.serializer()), el)
            },
            call = { api.reorderPropertyFolders(PropertyFolderReorderRequest(folderIds)) },
        )
    }

    suspend fun togglePropertyFolderMembership(
        folderId: Long,
        propertyId: Long,
        add: Boolean,
    ): ApiResult<Unit> {
        return try {
            val response = if (add) {
                api.addPropertyToFolder(folderId, propertyId)
            } else {
                api.removePropertyFromFolder(folderId, propertyId)
            }
            if (!response.ok) ApiResult.Error(response.error ?: "عملیات زونکن ناموفق")
            else ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.toUserMessage("خطای شبکه"))
        }
    }

    suspend fun getPropertyCabinets(): ApiResult<List<PropertyCabinetDto>> {
        return decodeList(
            decode = { el ->
                json.decodeFromJsonElement(ListSerializer(PropertyCabinetDto.serializer()), el)
            },
            call = { api.getPropertyCabinets() },
        )
    }

    suspend fun createPropertyCabinet(request: PropertyCabinetCreateRequest): ApiResult<PropertyCabinetDto> =
        single { api.createPropertyCabinet(request) }

    suspend fun updatePropertyCabinet(
        cabinetId: Long,
        request: PropertyCabinetUpdateRequest,
    ): ApiResult<PropertyCabinetDto> = single { api.updatePropertyCabinet(cabinetId, request) }

    suspend fun deletePropertyCabinet(cabinetId: Long): ApiResult<Unit> {
        return try {
            val response = api.deletePropertyCabinet(cabinetId)
            if (!response.ok) ApiResult.Error(response.error ?: "حذف کمد ناموفق")
            else ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.toUserMessage("خطای شبکه"))
        }
    }

    suspend fun reorderPropertyCabinets(cabinetIds: List<Long>): ApiResult<List<PropertyCabinetDto>> {
        return decodeList(
            decode = { el ->
                json.decodeFromJsonElement(ListSerializer(PropertyCabinetDto.serializer()), el)
            },
            call = { api.reorderPropertyCabinets(PropertyCabinetReorderRequest(cabinetIds)) },
        )
    }

    suspend fun assignFolderToCabinet(cabinetId: Long, folderId: Long): ApiResult<PropertyFolderDto> =
        single { api.assignFolderToCabinet(cabinetId, folderId) }

    suspend fun removeFolderFromCabinet(cabinetId: Long, folderId: Long): ApiResult<PropertyFolderDto> =
        single { api.removeFolderFromCabinet(cabinetId, folderId) }

    suspend fun getSupportTickets(
        status: String? = null,
        priority: String? = null,
        query: String? = null,
    ): ApiResult<SupportTicketsPage> {
        return try {
            val response = api.getSupportTickets(
                status = status?.ifBlank { null },
                priority = priority?.ifBlank { null },
                query = query?.ifBlank { null },
            )
            if (!response.ok) {
                ApiResult.Error(response.error ?: "خطا")
            } else {
                val tickets = response.data?.let { el ->
                    json.decodeFromJsonElement(ListSerializer(SupportTicketDto.serializer()), el)
                }.orEmpty()
                ApiResult.Success(
                    SupportTicketsPage(
                        tickets = tickets,
                        stats = response.meta?.stats ?: SupportTicketStatsDto(),
                        total = response.meta?.total ?: tickets.size,
                    ),
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.toUserMessage("خطای شبکه"))
        }
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
            ApiResult.Error(e.toUserMessage("خطای شبکه"))
        }
    }

    suspend fun closeSupportTicket(ticketId: Long): ApiResult<SupportTicketDto> =
        single { api.closeSupportTicket(ticketId) }

    suspend fun reopenSupportTicket(ticketId: Long): ApiResult<SupportTicketDto> =
        single { api.reopenSupportTicket(ticketId) }

    suspend fun getAiQuota(): ApiResult<AiQuotaData> = single { api.getAiQuota() }

    suspend fun aiDraftMessage(request: AiDraftMessageRequest): ApiResult<AiTextResult> =
        single { api.aiDraftMessage(request) }

    suspend fun aiSummarizeListing(request: AiSummarizeListingRequest): ApiResult<AiTextResult> =
        single { api.aiSummarizeListing(request) }

    suspend fun aiSummarizeProperty(request: AiSummarizePropertyRequest): ApiResult<AiTextResult> =
        single { api.aiSummarizeProperty(request) }

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
        ApiResult.Error(e.toUserMessage("خطای شبکه"))
    }

    private suspend inline fun <reified T> single(
        crossinline call: suspend () -> ApiEnvelope,
    ): ApiResult<T> = try {
        val response = call()
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.toUserMessage("خطای شبکه"))
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
            ApiResult.Error(e.toUserMessage("خطای شبکه"))
        }
    }
}
