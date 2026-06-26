package ir.divarfiling.mobile.core.network

import retrofit2.http.Body
import retrofit2.http.GET
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

    @GET("crm/contacts")
    suspend fun getContacts(
        @Query("q") query: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50,
    ): ApiEnvelope

    @POST("crm/contacts/quick-lead")
    suspend fun quickLead(@Body body: QuickLeadRequest): ApiEnvelope

    @GET("crm/today")
    suspend fun getToday(): ApiEnvelope

    @GET("filing/datasets")
    suspend fun getDatasets(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): ApiEnvelope

    @GET("filing/datasets/{id}/listings")
    suspend fun getListings(
        @Path("id") datasetId: String,
        @Query("page") page: Int = 1,
        @Query("q") query: String? = null,
    ): ApiEnvelope

    @GET("extractions/limits")
    suspend fun extractionLimits(): ApiEnvelope

    @POST("extractions/upload")
    suspend fun uploadExtraction(@Body body: ExtractionUploadRequest): ApiEnvelope
}
