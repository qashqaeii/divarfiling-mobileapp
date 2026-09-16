package ir.divarfiling.mobile.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.core.network.DealCommissionSplitSaveRequest
import ir.divarfiling.mobile.core.network.DealCommissionSplitsData
import ir.divarfiling.mobile.core.network.DealContractCreateRequest
import ir.divarfiling.mobile.core.network.DealContractItemDto
import ir.divarfiling.mobile.core.network.DealContractsData
import ir.divarfiling.mobile.core.network.DealJourneyData
import ir.divarfiling.mobile.core.network.DuplicateDealCheckData
import ir.divarfiling.mobile.core.network.WorkspaceBridgeData
import ir.divarfiling.mobile.core.network.WorkspaceBridgeRequest
import ir.divarfiling.mobile.core.network.DealChecklistToggleRequest
import ir.divarfiling.mobile.core.network.DealChecklistToggleResponse
import ir.divarfiling.mobile.core.network.DealFinanceDashboardData
import ir.divarfiling.mobile.core.network.DealFinanceDefaultsDto
import ir.divarfiling.mobile.core.network.DealFinanceSaveRequest
import ir.divarfiling.mobile.core.network.DealFinanceSettingsData
import ir.divarfiling.mobile.core.network.DealFollowUpRequest
import ir.divarfiling.mobile.core.network.DealFollowUpResponse
import ir.divarfiling.mobile.core.network.DealNextActionDto
import ir.divarfiling.mobile.core.network.DealNextActionRequest
import ir.divarfiling.mobile.core.network.DealTimelineItemDto
import ir.divarfiling.mobile.core.network.DealDto
import ir.divarfiling.mobile.core.network.DealPipelineData
import ir.divarfiling.mobile.core.network.DealStageRequest
import ir.divarfiling.mobile.core.network.DealStagesData
import ir.divarfiling.mobile.core.network.DealStagesSaveRequest
import ir.divarfiling.mobile.core.network.DealCreateRequest
import ir.divarfiling.mobile.core.network.DealUpdateRequest
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.PaginatedResult
import ir.divarfiling.mobile.core.network.PropertyContactLinkDto
import ir.divarfiling.mobile.core.network.ContactSuggestResponse
import ir.divarfiling.mobile.core.network.PropertyContactMatchesData
import ir.divarfiling.mobile.core.network.PropertyCreateRequest
import ir.divarfiling.mobile.core.network.PropertySuggestContactsRequest
import ir.divarfiling.mobile.core.network.ListingPublicShareDto
import ir.divarfiling.mobile.core.network.ListingPublicShareUpdateRequest
import ir.divarfiling.mobile.core.network.NearbyPoisPayloadDto
import ir.divarfiling.mobile.core.network.PropertyDetailData
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.core.network.PropertyLinkContactRequest
import ir.divarfiling.mobile.core.network.PropertyLocationRequest
import ir.divarfiling.mobile.core.network.PropertyLocationUpdateData
import ir.divarfiling.mobile.core.network.PropertyNearbyPoisRequest
import ir.divarfiling.mobile.core.network.PropertyStatusRequest
import ir.divarfiling.mobile.core.network.PropertyUpdateRequest
import ir.divarfiling.mobile.core.network.PropertyImageUploadData
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

    suspend fun getStages(): ApiResult<DealStagesData> = try {
        val response = api.getDealStages()
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun saveStages(request: DealStagesSaveRequest): ApiResult<DealStagesData> = try {
        val response = api.saveDealStages(request)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun getFinanceDashboard(period: String = "month"): ApiResult<DealFinanceDashboardData> = try {
        val response = api.getDealFinanceDashboard(period)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun getFinanceSettings(): ApiResult<DealFinanceSettingsData> = try {
        val response = api.getDealFinanceSettings()
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun saveFinanceSettings(defaults: DealFinanceDefaultsDto): ApiResult<DealFinanceSettingsData> = try {
        val response = api.saveDealFinanceSettings(defaults)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun updateDealFinance(dealId: Long, request: DealFinanceSaveRequest): ApiResult<DealDto> = single {
        api.updateDealFinance(dealId, request)
    }

    suspend fun createDeal(request: DealCreateRequest): ApiResult<DealDto> = single {
        api.createDeal(request)
    }

    suspend fun updateDeal(dealId: Long, request: DealUpdateRequest): ApiResult<DealDto> = single {
        api.updateDeal(dealId, request)
    }

    suspend fun updateDealStage(
        dealId: Long,
        stage: String,
        lostReason: String? = null,
    ): ApiResult<DealDto> = single {
        api.updateDealStage(dealId, DealStageRequest(stage, lostReason))
    }

    suspend fun deleteDeal(dealId: Long): ApiResult<Unit> = try {
        val response = api.deleteDeal(dealId)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun toggleDealChecklist(
        dealId: Long,
        itemId: String,
        done: Boolean? = null,
    ): ApiResult<DealChecklistToggleResponse> = single {
        api.toggleDealChecklist(dealId, DealChecklistToggleRequest(itemId, done))
    }

    suspend fun createFollowUp(dealId: Long, request: DealFollowUpRequest): ApiResult<DealFollowUpResponse> = try {
        val response = api.createDealFollowUp(dealId, request)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun completeNextAction(dealId: Long, note: String = ""): ApiResult<DealFollowUpResponse> = try {
        val response = api.completeDealNextAction(dealId, mapOf("note" to note))
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun updateNextAction(dealId: Long, request: DealNextActionRequest): ApiResult<DealNextActionDto> = try {
        val response = api.updateDealNextAction(dealId, request)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else {
            val envelope = response.requireData(json) as kotlinx.serialization.json.JsonObject
            val action = envelope["next_action"]?.let {
                json.decodeFromJsonElement(DealNextActionDto.serializer(), it)
            } ?: return ApiResult.Error("پاسخ نامعتبر")
            ApiResult.Success(action)
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun getTimeline(dealId: Long, limit: Int = 30, offset: Int = 0): ApiResult<List<DealTimelineItemDto>> = try {
        val response = api.getDealTimeline(dealId, limit, offset)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else {
            val payload = response.requireData(json) as kotlinx.serialization.json.JsonObject
            val items = payload["items"]?.let {
                json.decodeFromJsonElement(kotlinx.serialization.builtins.ListSerializer(DealTimelineItemDto.serializer()), it)
            } ?: emptyList()
            ApiResult.Success(items)
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun checkDuplicateDeal(propertyId: Long, customerId: Long): ApiResult<DuplicateDealCheckData> = single {
        api.checkDuplicateDeal(propertyId, customerId)
    }

    suspend fun getDealJourney(dealId: Long): ApiResult<DealJourneyData> = single {
        api.getDealJourney(dealId)
    }

    suspend fun getDealContracts(dealId: Long): ApiResult<DealContractsData> = single {
        api.getDealContracts(dealId)
    }

    suspend fun createDealContract(dealId: Long, contractType: String? = null): ApiResult<DealContractItemDto> = single {
        api.createDealContract(dealId, DealContractCreateRequest(contractType))
    }

    suspend fun getCommissionSplits(dealId: Long): ApiResult<DealCommissionSplitsData> = single {
        api.getDealCommissionSplits(dealId)
    }

    suspend fun saveCommissionSplits(
        dealId: Long,
        request: DealCommissionSplitSaveRequest,
    ): ApiResult<DealCommissionSplitsData> = single {
        api.saveDealCommissionSplits(dealId, request)
    }

    suspend fun createWorkspaceBridge(path: String): ApiResult<WorkspaceBridgeData> = single {
        api.createWorkspaceBridge(WorkspaceBridgeRequest(path))
    }

    suspend fun getProperties(
        query: String? = null,
        dealMode: String? = null,
        propertyType: String? = null,
        city: String? = null,
        transactionStatus: String? = null,
        folderId: Long? = null,
        cabinetId: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): ApiResult<PaginatedResult<PropertyDto>> = paginatedProperties {
        api.getProperties(
            query = query?.ifBlank { null },
            dealMode = dealMode?.ifBlank { null },
            propertyType = propertyType?.ifBlank { null },
            city = city?.ifBlank { null },
            transactionStatus = transactionStatus?.ifBlank { null },
            folderId = folderId,
            cabinetId = cabinetId?.ifBlank { null },
            page = page,
            pageSize = pageSize,
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

    suspend fun uploadPropertyImage(propertyId: Long, uri: Uri): ApiResult<PropertyImageUploadData> {
        return try {
            val resolver = context.contentResolver
            val mime = resolver.getType(uri) ?: "image/jpeg"
            val fileName = resolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
            } ?: "image.jpg"
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return ApiResult.Error("خواندن تصویر ناموفق")
            val part = MultipartBody.Part.createFormData(
                "image",
                fileName,
                bytes.toRequestBody(mime.toMediaType()),
            )
            val response = api.uploadPropertyImage(propertyId = propertyId, image = part)
            if (!response.ok) ApiResult.Error(response.error ?: "آپلود تصویر ناموفق")
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

    suspend fun updatePropertyLocation(
        propertyId: Long,
        latitude: Double,
        longitude: Double,
    ): ApiResult<PropertyLocationUpdateData> = single {
        api.updatePropertyLocation(propertyId, PropertyLocationRequest(latitude, longitude))
    }

    suspend fun fetchPropertyNearbyPois(
        propertyId: Long,
        refresh: Boolean = false,
    ): ApiResult<NearbyPoisPayloadDto> = single {
        api.fetchPropertyNearbyPois(propertyId, PropertyNearbyPoisRequest(refresh))
    }

    suspend fun updatePropertyPublicShare(
        propertyId: Long,
        request: ListingPublicShareUpdateRequest,
    ): ApiResult<ListingPublicShareDto> = single {
        api.updatePropertyPublicShare(propertyId, request)
    }

    suspend fun getPropertyContactMatches(propertyId: Long): ApiResult<PropertyContactMatchesData> = try {
        val response = api.getPropertyContactMatches(propertyId)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا در دریافت پیشنهادها")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun suggestPropertyContacts(
        propertyId: Long,
        customerIds: List<Long>,
        note: String? = null,
    ): ApiResult<ContactSuggestResponse> = try {
        val response = api.suggestPropertyContacts(
            propertyId,
            PropertySuggestContactsRequest(customerIds = customerIds, note = note),
        )
        if (!response.ok) ApiResult.Error(response.error ?: "ثبت پیشنهاد ناموفق بود")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun deleteProperty(propertyId: Long): ApiResult<Unit> = try {
        val response = api.deleteProperty(propertyId)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun duplicateProperty(propertyId: Long): ApiResult<PropertyDto> = try {
        val response = api.duplicateProperty(propertyId)
        if (!response.ok) ApiResult.Error(response.error ?: "کپی فایل ناموفق بود")
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
            val pageSize = response.meta?.pageSize ?: list.size
            val hasMore = list.isNotEmpty() && pageSize > 0 && page * pageSize < total
            ApiResult.Success(PaginatedResult(list, page, total, hasMore))
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
            val pageSize = response.meta?.pageSize ?: list.size
            val hasMore = list.isNotEmpty() && pageSize > 0 && page * pageSize < total
            ApiResult.Success(PaginatedResult(list, page, total, hasMore))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun getCrmPropertyMap(params: Map<String, String>): ApiResult<ir.divarfiling.mobile.core.network.CrmPropertyMapData> =
        try {
            val response = api.getCrmPropertyMap(params)
            if (!response.ok) ApiResult.Error(response.error ?: "خطا در دریافت نقشه")
            else ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }

    suspend fun getPropertyTeamShares(
        propertyId: Long,
        includeMembers: Boolean = false,
        memberQuery: String = "",
    ): ApiResult<ir.divarfiling.mobile.core.network.PropertyTeamSharesData> = try {
        val response = api.getPropertyTeamShares(
            propertyId,
            members = if (includeMembers) 1 else null,
            query = memberQuery.ifBlank { null },
        )
        if (!response.ok) ApiResult.Error(response.error ?: "خطا در اشتراک تیم")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun grantPropertyTeamShare(
        propertyId: Long,
        memberId: Long,
        shareMode: String,
    ): ApiResult<ir.divarfiling.mobile.core.network.PropertyTeamShareRowDto> = try {
        val response = api.grantPropertyTeamShare(
            propertyId,
            ir.divarfiling.mobile.core.network.PropertyTeamShareGrantRequest(memberId, shareMode),
        )
        if (!response.ok) ApiResult.Error(response.error ?: "اشتراک‌گذاری ناموفق")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }

    suspend fun revokePropertyTeamShare(propertyId: Long, shareId: Long): ApiResult<Unit> = try {
        val response = api.revokePropertyTeamShare(propertyId, shareId)
        if (!response.ok) ApiResult.Error(response.error ?: "حذف دسترسی ناموفق")
        else ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطای شبکه")
    }
}
