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
import retrofit2.http.QueryMap
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface MobileApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ApiEnvelope

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): ApiEnvelope

    @POST("auth/logout")
    suspend fun logout(@Body body: RefreshRequest): ApiEnvelope

    @POST("auth/otp/request")
    suspend fun otpRequest(@Body body: OtpRequestBody): ApiEnvelope

    @POST("auth/otp/resend")
    suspend fun otpResend(@Body body: OtpRequestBody): ApiEnvelope

    @POST("auth/otp/verify")
    suspend fun otpVerify(@Body body: OtpVerifyBody): ApiEnvelope

    @POST("auth/register")
    suspend fun completeRegister(@Body body: PasswordCompleteRequest): ApiEnvelope

    @POST("auth/password-reset")
    suspend fun completePasswordReset(@Body body: PasswordCompleteRequest): ApiEnvelope

    @POST("auth/verify-phone/request")
    suspend fun verifyPhoneRequest(): ApiEnvelope

    @POST("auth/verify-phone/verify")
    suspend fun verifyPhoneComplete(@Body body: OtpVerifyBody): ApiEnvelope

    @POST("devices/register")
    suspend fun registerDevice(@Body body: DeviceRegisterRequest): ApiEnvelope

    @PATCH("devices/me")
    suspend fun updateDeviceFcm(@Body body: DeviceFcmPatchRequest): ApiEnvelope

    @GET("license/status")
    suspend fun licenseStatus(): ApiEnvelope

    @GET("shop/plans")
    suspend fun getShopPlans(): ApiEnvelope

    @POST("shop/checkout")
    suspend fun shopCheckout(@Body body: ShopCheckoutRequest): ApiEnvelope

    @POST("shop/discount")
    suspend fun shopDiscountPreview(@Body body: ShopDiscountPreviewRequest): ApiEnvelope

    @GET("shop/orders/{orderId}")
    suspend fun shopOrderStatus(@Path("orderId") orderId: String): ApiEnvelope

    @POST("shop/orders/{orderId}")
    suspend fun shopVerifyOrder(@Path("orderId") orderId: String): ApiEnvelope

    @GET("app/version")
    suspend fun getAppVersion(
        @Query("current_build") currentBuild: Int,
    ): ApiEnvelope

    @GET("settings/profile")
    suspend fun getProfile(): ApiEnvelope

    @PATCH("settings/profile")
    suspend fun updateProfile(@Body body: ProfileUpdateRequest): ApiEnvelope

    @Multipart
    @POST("settings/profile/avatar")
    suspend fun uploadProfileAvatar(@Part avatar: MultipartBody.Part): ApiEnvelope

    @DELETE("settings/profile/avatar")
    suspend fun deleteProfileAvatar(): ApiEnvelope

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

    @POST("notifications/read-all")
    suspend fun markAllNotificationsRead(): ApiEnvelope

    @POST("notifications/local-delivered")
    suspend fun reportReminderLocalDelivered(@Body body: ReminderLocalDeliveredRequest): ApiEnvelope

    @GET("crm/contacts")
    suspend fun getContacts(
        @Query("q") query: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50,
        @Query("customer_type") customerType: String? = null,
        @Query("status") status: String? = null,
        @Query("priority") priority: String? = null,
        @Query("due") due: String? = null,
        @Query("sort") sort: String? = null,
        @Query("tag") tag: String? = null,
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

    @GET("crm/reminders")
    suspend fun getReminders(
        @Query("due_from") dueFrom: String? = null,
        @Query("due_to") dueTo: String? = null,
        @Query("done") done: Boolean? = null,
    ): ApiEnvelope

    @POST("crm/reminders")
    suspend fun createStandaloneReminder(@Body body: ReminderCreateRequest): ApiEnvelope

    @PATCH("crm/reminders/{id}")
    suspend fun patchReminder(
        @Path("id") reminderId: Long,
        @Body body: ReminderPatchRequest,
    ): ApiEnvelope

    @DELETE("crm/reminders/{id}")
    suspend fun deleteReminder(@Path("id") reminderId: Long): ApiEnvelope

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

    @GET("crm/contacts/{id}/matches")
    suspend fun getContactMatches(@Path("id") contactId: Long): ApiEnvelope

    @POST("crm/contacts/{id}/suggest")
    suspend fun suggestContactMatches(
        @Path("id") contactId: Long,
        @Body body: ContactSuggestRequest,
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

    @DELETE("crm/deals/{id}")
    suspend fun deleteDeal(@Path("id") dealId: Long): ApiEnvelope

    @POST("crm/deals/{id}/checklist")
    suspend fun toggleDealChecklist(
        @Path("id") dealId: Long,
        @Body body: DealChecklistToggleRequest,
    ): ApiEnvelope

    @POST("crm/deals/{id}/stage")
    suspend fun updateDealStage(@Path("id") dealId: Long, @Body body: DealStageRequest): ApiEnvelope

    @GET("crm/properties")
    suspend fun getProperties(
        @Query("q") query: String? = null,
        @Query("deal_mode") dealMode: String? = null,
        @Query("property_type") propertyType: String? = null,
        @Query("city") city: String? = null,
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

    @DELETE("crm/properties/{id}")
    suspend fun deleteProperty(@Path("id") propertyId: Long): ApiEnvelope

    @GET("crm/properties/{id}/public-share")
    suspend fun getPropertyPublicShare(@Path("id") propertyId: Long): ApiEnvelope

    @PATCH("crm/properties/{id}/public-share")
    suspend fun updatePropertyPublicShare(
        @Path("id") propertyId: Long,
        @Body body: ListingPublicShareUpdateRequest,
    ): ApiEnvelope

    @POST("crm/properties/{id}/contacts")
    suspend fun linkPropertyContact(
        @Path("id") propertyId: Long,
        @Body body: PropertyLinkContactRequest,
    ): ApiEnvelope

    @Multipart
    @POST("crm/properties/{id}/documents")
    suspend fun uploadPropertyDocument(
        @Path("id") propertyId: Long,
        @Part file: okhttp3.MultipartBody.Part,
        @Part("title") title: okhttp3.RequestBody,
        @Part("doc_type") docType: okhttp3.RequestBody,
        @Part("note") note: okhttp3.RequestBody,
    ): ApiEnvelope

    @DELETE("crm/properties/{id}/documents/{documentId}")
    suspend fun deletePropertyDocument(
        @Path("id") propertyId: Long,
        @Path("documentId") documentId: Long,
    ): ApiEnvelope

    @GET("filing/datasets")
    suspend fun getDatasets(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): ApiEnvelope

    @GET("filing/datasets/{id}")
    suspend fun getDataset(@Path("id") datasetId: String): ApiEnvelope

    @DELETE("filing/datasets/{id}")
    suspend fun deleteDataset(@Path("id") datasetId: String): ApiEnvelope

    @GET("filing/datasets/{id}/listings")
    suspend fun getListings(
        @Path("id") datasetId: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50,
        @Query("q") query: String? = null,
        @QueryMap filters: Map<String, String> = emptyMap(),
    ): ApiEnvelope

    @GET("filing/listings/{token}")
    suspend fun getListingDetail(@Path("token") token: String): ApiEnvelope

    @PATCH("filing/listings/{token}")
    suspend fun updateListing(
        @Path("token") token: String,
        @Body body: ListingUpdateRequest,
    ): ApiEnvelope

    @GET("filing/listings/{token}/public-share")
    suspend fun getListingPublicShare(@Path("token") token: String): ApiEnvelope

    @PATCH("filing/listings/{token}/public-share")
    suspend fun updateListingPublicShare(
        @Path("token") token: String,
        @Body body: ListingPublicShareUpdateRequest,
    ): ApiEnvelope

    @GET("filing/search")
    suspend fun searchListings(
        @Query("q") query: String? = null,
        @Query("dataset_id") datasetId: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 30,
        @QueryMap filters: Map<String, String> = emptyMap(),
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

    @GET("filing/datasets/{id}/insights")
    suspend fun getDatasetInsights(@Path("id") datasetId: String): ApiEnvelope

    @GET("filing/datasets/{id}/map")
    suspend fun getDatasetMap(@Path("id") datasetId: String): ApiEnvelope

    @GET("crm/properties/{id}/contact-matches")
    suspend fun getPropertyContactMatches(@Path("id") propertyId: Long): ApiEnvelope

    @POST("crm/properties/{id}/suggest-contacts")
    suspend fun suggestPropertyContacts(
        @Path("id") propertyId: Long,
        @Body body: PropertySuggestContactsRequest,
    ): ApiEnvelope

    @GET("crm/templates")
    suspend fun getMessageTemplates(): ApiEnvelope

    @GET("crm/saved-filters")
    suspend fun getSavedFilters(
        @Query("entity") entity: String? = null,
        @Query("include_new_count") includeNewCount: Int? = null,
    ): ApiEnvelope

    @POST("crm/saved-filters")
    suspend fun createSavedFilter(@Body body: SavedFilterCreateRequest): ApiEnvelope

    @DELETE("crm/saved-filters/{id}")
    suspend fun deleteSavedFilter(@Path("id") filterId: Long): ApiEnvelope

    @POST("crm/saved-filters/{id}/pin")
    suspend fun pinSavedFilter(@Path("id") filterId: Long): ApiEnvelope

    @GET("support/tickets")
    suspend fun getSupportTickets(): ApiEnvelope

    @POST("support/tickets")
    suspend fun createSupportTicket(@Body body: SupportTicketCreateRequest): ApiEnvelope

    @GET("support/tickets/{id}")
    suspend fun getSupportTicket(@Path("id") ticketId: Long): ApiEnvelope

    @Multipart
    @POST("support/tickets/{id}/reply")
    suspend fun replySupportTicket(
        @Path("id") ticketId: Long,
        @Part("body") body: RequestBody,
        @Part attachment: MultipartBody.Part? = null,
    ): ApiEnvelope

    @POST("support/tickets/{id}/close")
    suspend fun closeSupportTicket(@Path("id") ticketId: Long): ApiEnvelope

    @POST("support/tickets/{id}/reopen")
    suspend fun reopenSupportTicket(@Path("id") ticketId: Long): ApiEnvelope

    @GET("ai/quota")
    suspend fun getAiQuota(): ApiEnvelope

    @POST("ai/draft-message")
    suspend fun aiDraftMessage(@Body body: AiDraftMessageRequest): ApiEnvelope

    @POST("ai/summarize-listing")
    suspend fun aiSummarizeListing(@Body body: AiSummarizeListingRequest): ApiEnvelope

    @GET("crm/team/overview")
    suspend fun getTeamOverview(): ApiEnvelope

    @GET("crm/team/members")
    suspend fun getTeamMembers(@Query("exclude_self") excludeSelf: Int? = null): ApiEnvelope

    @GET("crm/team/unread")
    suspend fun getTeamUnread(): ApiEnvelope

    @GET("crm/team/messages")
    suspend fun getTeamMessages(
        @Query("folder") folder: String? = null,
        @Query("page") page: Int? = null,
    ): ApiEnvelope

    @POST("crm/team/messages")
    suspend fun sendTeamMessage(@Body body: TeamSendMessageRequest): ApiEnvelope

    @GET("crm/team/messages/{id}")
    suspend fun getTeamThread(@Path("id") threadId: Long): ApiEnvelope

    @PATCH("crm/team/messages/{id}")
    suspend fun patchTeamThread(
        @Path("id") threadId: Long,
        @Body body: TeamThreadStateRequest,
    ): ApiEnvelope

    @POST("crm/team/messages/{id}")
    suspend fun replyTeamThread(
        @Path("id") threadId: Long,
        @Body body: TeamReplyRequest,
    ): ApiEnvelope

    @GET("crm/team/announcements")
    suspend fun getTeamAnnouncements(
        @Query("unread") unread: Int? = null,
        @Query("important") important: Int? = null,
    ): ApiEnvelope

    @GET("crm/team/announcements/{id}")
    suspend fun getTeamAnnouncement(@Path("id") announcementId: Long): ApiEnvelope

    @POST("crm/team/announcements/{id}")
    suspend fun markTeamAnnouncementRead(
        @Path("id") announcementId: Long,
        @Body body: TeamActionRequest = TeamActionRequest(action = "read"),
    ): ApiEnvelope

    @GET("crm/team/notifications")
    suspend fun getTeamPanelNotifications(@Query("unread") unread: Int? = null): ApiEnvelope

    @POST("crm/team/notifications")
    suspend fun markTeamPanelNotifications(@Body body: TeamActionRequest): ApiEnvelope

    @GET("crm/team/inbox")
    suspend fun getTeamLeadInbox(): ApiEnvelope

    @POST("crm/team/inbox")
    suspend fun assignTeamLeads(@Body body: TeamAssignLeadsRequest): ApiEnvelope

    @POST("crm/contacts/{id}/assign")
    suspend fun assignContact(
        @Path("id") contactId: Long,
        @Body body: ContactTeamAssignRequest,
    ): ApiEnvelope

    @POST("crm/contacts/{id}/transfer")
    suspend fun transferContact(
        @Path("id") contactId: Long,
        @Body body: ContactTeamTransferRequest,
    ): ApiEnvelope

    @POST("extractions/cloud")
    suspend fun createCloudExtraction(@Body body: CloudExtractionCreateRequest): ApiEnvelope

    @GET("extractions/cloud")
    suspend fun listCloudExtractions(): ApiEnvelope

    @GET("extractions/cloud/{id}")
    suspend fun getCloudExtraction(@Path("id") jobId: Long): ApiEnvelope

    @POST("sync/push")
    suspend fun syncPush(@Body body: SyncPushRequest): ApiEnvelope

    @GET("sync")
    suspend fun syncPull(
        @Query("since") since: String? = null,
        @Query("entities") entities: String = "contacts,deals,properties,reminders,activities",
    ): ApiEnvelope
}
