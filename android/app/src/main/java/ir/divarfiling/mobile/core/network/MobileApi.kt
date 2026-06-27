package ir.divarfiling.mobile.core.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface MobileApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ApiEnvelope

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): ApiEnvelope

    @POST("auth/logout")
    suspend fun logout(@Body body: RefreshRequest): ApiEnvelope

    @POST("devices/register")
    suspend fun registerDevice(@Body body: DeviceRegisterRequest): ApiEnvelope

    @PATCH("devices/me")
    suspend fun updateDeviceFcm(@Body body: DeviceFcmPatchRequest): ApiEnvelope

    @GET("license/status")
    suspend fun licenseStatus(): ApiEnvelope

    @GET("settings/profile")
    suspend fun getProfile(): ApiEnvelope

    @PATCH("settings/profile")
    suspend fun updateProfile(@Body body: ProfileUpdateRequest): ApiEnvelope

    @GET("settings/notifications")
    suspend fun getNotificationPrefs(): ApiEnvelope

    @PATCH("settings/notifications")
    suspend fun updateNotificationPrefs(@Body body: NotificationPrefsDto): ApiEnvelope

    @GET("dashboard")
    suspend fun getDashboard(): ApiEnvelope

    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): ApiEnvelope

    @GET("notifications/unread-count")
    suspend fun getUnreadNotificationCount(): ApiEnvelope

    @POST("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: Long): ApiEnvelope

    @GET("crm/contacts")
    suspend fun getContacts(
        @Query("q") query: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50,
    ): ApiEnvelope

    @GET("crm/contacts/{id}")
    suspend fun getContact(@Path("id") contactId: Long): ApiEnvelope

    @PATCH("crm/contacts/{id}")
    suspend fun updateContact(
        @Path("id") contactId: Long,
        @Body body: ContactUpdateRequest,
    ): ApiEnvelope

    @POST("crm/contacts/quick-lead")
    suspend fun quickLead(@Body body: QuickLeadRequest): ApiEnvelope

    @POST("crm/contacts/{id}/activities")
    suspend fun createActivity(
        @Path("id") contactId: Long,
        @Body body: ActivityCreateRequest,
    ): ApiEnvelope

    @POST("crm/contacts/{id}/notes")
    suspend fun createNote(
        @Path("id") contactId: Long,
        @Body body: NoteCreateRequest,
    ): ApiEnvelope

    @POST("crm/contacts/{id}/reminders")
    suspend fun createReminder(
        @Path("id") contactId: Long,
        @Body body: ReminderCreateRequest,
    ): ApiEnvelope

    @POST("crm/contacts/{id}/listings")
    suspend fun linkListing(
        @Path("id") contactId: Long,
        @Body body: LinkListingRequest,
    ): ApiEnvelope

    @POST("crm/contacts/{id}/send-listing")
    suspend fun sendListing(
        @Path("id") contactId: Long,
        @Body body: SendListingRequest,
    ): ApiEnvelope

    @GET("crm/contacts/{id}/documents")
    suspend fun getContactDocuments(@Path("id") contactId: Long): ApiEnvelope

    @Multipart
    @POST("crm/contacts/{id}/documents")
    suspend fun uploadContactDocument(
        @Path("id") contactId: Long,
        @Part("title") title: RequestBody,
        @Part file: MultipartBody.Part,
        @Part("doc_type") docType: RequestBody? = null,
        @Part("note") note: RequestBody? = null,
    ): ApiEnvelope

    @DELETE("crm/contacts/{id}/documents/{documentId}")
    suspend fun deleteContactDocument(
        @Path("id") contactId: Long,
        @Path("documentId") documentId: Long,
    ): ApiEnvelope

    @GET("crm/today")
    suspend fun getToday(): ApiEnvelope

    @POST("crm/today/actions")
    suspend fun todayAction(@Body body: TodayActionRequest): ApiEnvelope

    @GET("crm/deals")
    suspend fun getDeals(
        @Query("q") query: String? = null,
        @Query("stage") stage: String? = null,
        @Query("customer_id") customerId: Long? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): ApiEnvelope

    @GET("crm/deals/pipeline")
    suspend fun getDealPipeline(): ApiEnvelope

    @GET("crm/deals/stages")
    suspend fun getDealStages(): ApiEnvelope

    @GET("crm/deals/{id}")
    suspend fun getDeal(@Path("id") dealId: Long): ApiEnvelope

    @POST("crm/deals")
    suspend fun createDeal(@Body body: DealCreateRequest): ApiEnvelope

    @PATCH("crm/deals/{id}")
    suspend fun updateDeal(@Path("id") dealId: Long, @Body body: DealUpdateRequest): ApiEnvelope

    @POST("crm/deals/{id}/stage")
    suspend fun updateDealStage(@Path("id") dealId: Long, @Body body: DealStageRequest): ApiEnvelope

    @GET("crm/properties")
    suspend fun getProperties(
        @Query("q") query: String? = null,
        @Query("deal_mode") dealMode: String? = null,
        @Query("transaction_status") transactionStatus: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): ApiEnvelope

    @GET("crm/properties/{id}")
    suspend fun getProperty(@Path("id") propertyId: Long): ApiEnvelope

    @POST("crm/properties")
    suspend fun createProperty(@Body body: PropertyCreateRequest): ApiEnvelope

    @PATCH("crm/properties/{id}")
    suspend fun updateProperty(
        @Path("id") propertyId: Long,
        @Body body: PropertyUpdateRequest,
    ): ApiEnvelope

    @POST("crm/properties/{id}/status")
    suspend fun updatePropertyStatus(
        @Path("id") propertyId: Long,
        @Body body: PropertyStatusRequest,
    ): ApiEnvelope

    @GET("filing/datasets")
    suspend fun getDatasets(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): ApiEnvelope

    @GET("filing/datasets/{id}")
    suspend fun getDataset(@Path("id") datasetId: String): ApiEnvelope

    @GET("filing/datasets/{id}/listings")
    suspend fun getListings(
        @Path("id") datasetId: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50,
        @Query("q") query: String? = null,
        @Query("price_min") priceMin: Long? = null,
        @Query("price_max") priceMax: Long? = null,
        @Query("area_min") areaMin: Int? = null,
        @Query("area_max") areaMax: Int? = null,
        @Query("rooms") rooms: Int? = null,
    ): ApiEnvelope

    @GET("filing/datasets/{id}/insights")
    suspend fun getDatasetInsights(@Path("id") datasetId: String): ApiEnvelope

    @GET("filing/datasets/{id}/map")
    suspend fun getDatasetMap(@Path("id") datasetId: String): ApiEnvelope

    @GET("filing/listings/{token}")
    suspend fun getListingDetail(@Path("token") token: String): ApiEnvelope

    @GET("filing/search")
    suspend fun searchListings(
        @Query("q") query: String? = null,
        @Query("dataset_id") datasetId: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 30,
        @Query("price_min") priceMin: Long? = null,
        @Query("price_max") priceMax: Long? = null,
        @Query("area_min") areaMin: Int? = null,
        @Query("area_max") areaMax: Int? = null,
        @Query("rooms") rooms: Int? = null,
    ): ApiEnvelope

    @GET("extractions/limits")
    suspend fun extractionLimits(): ApiEnvelope

    @POST("extractions/upload")
    suspend fun uploadExtraction(@Body body: ExtractionUploadRequest): ApiEnvelope

    @GET("extractions/schedules")
    suspend fun getExtractionSchedules(): ApiEnvelope

    @POST("extractions/schedules")
    suspend fun createExtractionSchedule(@Body body: ExtractionScheduleCreateRequest): ApiEnvelope

    @GET("extractions/schedules/due")
    suspend fun getDueExtractionSchedules(): ApiEnvelope

    @GET("extractions/schedules/{id}")
    suspend fun getExtractionSchedule(@Path("id") scheduleId: Long): ApiEnvelope

    @PATCH("extractions/schedules/{id}")
    suspend fun updateExtractionSchedule(
        @Path("id") scheduleId: Long,
        @Body body: ExtractionScheduleUpdateRequest,
    ): ApiEnvelope

    @DELETE("extractions/schedules/{id}")
    suspend fun deleteExtractionSchedule(@Path("id") scheduleId: Long): ApiEnvelope

    @POST("extractions/schedules/{id}/toggle")
    suspend fun toggleExtractionSchedule(@Path("id") scheduleId: Long): ApiEnvelope

    @GET("extractions/schedules/{id}/runs")
    suspend fun getExtractionScheduleRuns(
        @Path("id") scheduleId: Long,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): ApiEnvelope

    @POST("extractions/schedules/{id}/runs/start")
    suspend fun startExtractionScheduleRun(@Path("id") scheduleId: Long): ApiEnvelope

    @POST("extractions/runs/{id}/fail")
    suspend fun failExtractionRun(
        @Path("id") runId: Long,
        @Body body: ExtractionRunFailRequest,
    ): ApiEnvelope

    @POST("sync/push")
    suspend fun syncPush(@Body body: SyncPushRequest): ApiEnvelope

    @GET("sync")
    suspend fun syncPull(
        @Query("since") since: String? = null,
        @Query("entities") entities: String = "contacts,deals,properties,reminders,activities",
    ): ApiEnvelope
}
