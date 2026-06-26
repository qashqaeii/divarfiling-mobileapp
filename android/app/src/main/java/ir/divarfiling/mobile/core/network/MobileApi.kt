package ir.divarfiling.mobile.core.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MobileApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ApiEnvelope

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): ApiEnvelope

    @POST("auth/logout")
    suspend fun logout(@Body body: RefreshRequest): ApiEnvelope

    @POST("devices/register")
    suspend fun registerDevice(@Body body: DeviceRegisterRequest): ApiEnvelope

    @GET("license/status")
    suspend fun licenseStatus(): ApiEnvelope

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

    @GET("crm/today")
    suspend fun getToday(): ApiEnvelope

    @POST("crm/today/actions")
    suspend fun todayAction(@Body body: TodayActionRequest): ApiEnvelope

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

    @GET("filing/listings/{token}")
    suspend fun getListingDetail(@Path("token") token: String): ApiEnvelope

    @GET("extractions/limits")
    suspend fun extractionLimits(): ApiEnvelope

    @POST("extractions/upload")
    suspend fun uploadExtraction(@Body body: ExtractionUploadRequest): ApiEnvelope

    @POST("sync/push")
    suspend fun syncPush(@Body body: SyncPushRequest): ApiEnvelope
}
