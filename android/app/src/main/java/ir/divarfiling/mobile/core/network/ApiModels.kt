package ir.divarfiling.mobile.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
object EmptyDto

@Serializable
data class ApiResponse<T>(
    val ok: Boolean,
    val data: T? = null,
    val error: String? = null,
    val code: String? = null,
    val meta: ApiMeta? = null,
)

@Serializable
data class ApiMeta(
    val page: Int? = null,
    val total: Int? = null,
    @SerialName("page_size") val pageSize: Int? = null,
    val neighborhoods: List<String> = emptyList(),
    val sort: String? = null,
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class LoginData(
    val access: String,
    val refresh: String,
    @SerialName("expires_in") val expiresIn: Long? = null,
    val user: UserDto,
)

@Serializable
data class UserDto(
    val id: Long,
    @SerialName("full_name") val fullName: String,
    val phone: String? = null,
    @SerialName("agency_name") val agencyName: String? = null,
    @SerialName("phone_verified") val phoneVerified: Boolean = true,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

@Serializable
data class RefreshRequest(val refresh: String)

@Serializable
data class RefreshData(
    val access: String,
    val refresh: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
)

@Serializable
data class DeviceRegisterRequest(
    @SerialName("device_id") val deviceId: String,
    @SerialName("device_model") val deviceModel: String,
    @SerialName("os_version") val osVersion: String,
    @SerialName("app_version") val appVersion: String,
    @SerialName("fcm_token") val fcmToken: String = "",
)

@Serializable
data class DeviceFcmPatchRequest(
    @SerialName("fcm_token") val fcmToken: String,
)

@Serializable
data class LocalReminderDeliveredRequest(
    @SerialName("reminder_id") val reminderId: Long,
)

@Serializable
data class DeviceRegisterData(
    @SerialName("device_id") val deviceId: String,
  val license: LicenseDto? = null,
)

@Serializable
data class LicenseDto(
    val valid: Boolean = false,
    val plan: String? = null,
    @SerialName("mobile_extract_enabled") val mobileExtractEnabled: Boolean = false,
    @SerialName("expires_at") val expiresAt: String? = null,
    val features: LicenseFeaturesDto? = null,
)

@Serializable
data class LicenseFeaturesDto(
    @SerialName("crm_mobile") val crmMobile: Boolean = true,
    @SerialName("filing_view") val filingView: Boolean = true,
    @SerialName("light_extract") val lightExtract: Boolean = false,
    val map: Boolean = true,
    val push: Boolean = true,
)

@Serializable
data class LicenseStatusData(
    val valid: Boolean,
    val plan: String? = null,
    val features: LicenseFeaturesDto? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("days_remaining") val daysRemaining: Int? = null,
    @SerialName("expiring_soon") val expiringSoon: Boolean = false,
    @SerialName("license_id") val licenseId: Long? = null,
    val status: String? = null,
    @SerialName("can_renew") val canRenew: Boolean = false,
)

@Serializable
data class OtpRequestBody(
    val phone: String,
    val purpose: String,
)

@Serializable
data class OtpVerifyBody(
    val phone: String,
    val purpose: String,
    val code: String,
)

@Serializable
data class OtpChallengeData(
    val phone: String? = null,
    @SerialName("phone_display") val phoneDisplay: String? = null,
    val purpose: String? = null,
    @SerialName("expires_in") val expiresIn: Int? = null,
    @SerialName("resend_in") val resendIn: Int? = null,
    @SerialName("challenge_token") val challengeToken: String? = null,
)

@Serializable
data class PasswordCompleteRequest(
    val phone: String,
    @SerialName("challenge_token") val challengeToken: String,
    val password: String,
    @SerialName("password_confirm") val passwordConfirm: String,
)

@Serializable
data class ShopPlanDto(
    val id: Long,
    val name: String,
    @SerialName("plan_type") val planType: String? = null,
    @SerialName("duration_days") val durationDays: Int? = null,
    @SerialName("duration_label") val durationLabel: String? = null,
    val tagline: String? = null,
    @SerialName("original_price") val originalPrice: Long? = null,
    @SerialName("final_price") val finalPrice: Long? = null,
    @SerialName("discount_amount") val discountAmount: Long? = null,
    @SerialName("has_discount") val hasDiscount: Boolean = false,
    @SerialName("is_featured") val isFeatured: Boolean = false,
    @SerialName("offer_badge") val offerBadge: String? = null,
    @SerialName("purchase_blocked") val purchaseBlocked: Boolean = false,
    @SerialName("purchase_block_message") val purchaseBlockMessage: String? = null,
    @SerialName("purchase_block_reason") val purchaseBlockReason: String? = null,
)

@Serializable
data class RenewableLicenseDto(
    @SerialName("license_id") val licenseId: Long,
    val plan: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("is_expired") val isExpired: Boolean = false,
)

@Serializable
data class ShopPlansData(
    @SerialName("product_slug") val productSlug: String? = null,
    @SerialName("product_name") val productName: String? = null,
    val plans: List<ShopPlanDto> = emptyList(),
    @SerialName("renewable_license") val renewableLicense: RenewableLicenseDto? = null,
    @SerialName("phone_verified") val phoneVerified: Boolean = true,
)

@Serializable
data class ShopCheckoutRequest(
    @SerialName("plan_id") val planId: Long,
    @SerialName("renew_license_id") val renewLicenseId: Long? = null,
    @SerialName("discount_code") val discountCode: String? = null,
)

@Serializable
data class ShopDiscountPreviewRequest(
    @SerialName("plan_id") val planId: Long,
    @SerialName("discount_code") val discountCode: String,
)

@Serializable
data class ShopDiscountPreviewData(
    @SerialName("plan_id") val planId: Long? = null,
    val code: String? = null,
    @SerialName("original_price") val originalPrice: Long? = null,
    @SerialName("base_final_price") val baseFinalPrice: Long? = null,
    @SerialName("discount_amount") val discountAmount: Long? = null,
    @SerialName("code_discount_amount") val codeDiscountAmount: Long? = null,
    @SerialName("final_price") val finalPrice: Long? = null,
    @SerialName("has_discount") val hasDiscount: Boolean = false,
)

@Serializable
data class ShopCheckoutData(
    @SerialName("order_id") val orderId: String,
    val status: String? = null,
    val amount: Long? = null,
    @SerialName("plan_name") val planName: String? = null,
    @SerialName("pay_url") val payUrl: String? = null,
    val reused: Boolean = false,
    @SerialName("return_scheme") val returnScheme: String? = null,
)

@Serializable
data class ShopOrderStatusData(
    @SerialName("order_id") val orderId: String,
    val status: String,
    val amount: Long? = null,
    @SerialName("plan_name") val planName: String? = null,
    @SerialName("paid_at") val paidAt: String? = null,
    @SerialName("is_renewal") val isRenewal: Boolean = false,
)

@Serializable
data class ProfileUpdateRequest(
    @SerialName("full_name") val fullName: String? = null,
    val phone: String? = null,
)

@Serializable
data class NotificationPrefsDto(
    @SerialName("crm_reminders") val crmReminders: Boolean = true,
    @SerialName("today_digest") val todayDigest: Boolean = true,
    @SerialName("customer_match") val customerMatch: Boolean = true,
    @SerialName("extract_complete") val extractComplete: Boolean = true,
    @SerialName("extract_schedule_due") val extractScheduleDue: Boolean = true,
    @SerialName("overdue_followup") val overdueFollowup: Boolean = true,
    @SerialName("license_alerts") val licenseAlerts: Boolean = true,
    @SerialName("announcements") val announcements: Boolean = true,
    @SerialName("digest_hour") val digestHour: Int = 8,
)

@Serializable
data class ReminderLocalDeliveredRequest(
    @SerialName("reminder_id") val reminderId: Long,
)

@Serializable
data class ContactDto(
    val id: Long,
    @SerialName("full_name") val fullName: String,
    val phone: String? = null,
    @SerialName("customer_type") val customerType: String? = null,
    val status: String? = null,
    val source: String? = null,
    val priority: String? = null,
    val budget: Long? = null,
    @SerialName("budget_min") val budgetMin: Long? = null,
    @SerialName("budget_max") val budgetMax: Long? = null,
    @SerialName("deposit_min") val depositMin: Long? = null,
    @SerialName("deposit_max") val depositMax: Long? = null,
    @SerialName("rent_min") val rentMin: Long? = null,
    @SerialName("rent_max") val rentMax: Long? = null,
    @SerialName("property_type") val propertyType: String? = null,
    @SerialName("min_area") val minArea: Int? = null,
    @SerialName("max_area") val maxArea: Int? = null,
    val rooms: String? = null,
    val areas: String? = null,
    val notes: String? = null,
    @SerialName("next_follow_up_at") val nextFollowUpAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("match_eligible") val matchEligible: Boolean = false,
    @SerialName("is_builder") val isBuilder: Boolean = false,
    @SerialName("builder_buy_property_types") val builderBuyPropertyTypes: String? = null,
    @SerialName("builder_buy_budget_min") val builderBuyBudgetMin: Long? = null,
    @SerialName("builder_buy_budget_max") val builderBuyBudgetMax: Long? = null,
    @SerialName("builder_buy_areas") val builderBuyAreas: String? = null,
    @SerialName("builder_buy_min_area") val builderBuyMinArea: Int? = null,
    @SerialName("builder_buy_max_area") val builderBuyMaxArea: Int? = null,
    @SerialName("rooms_min") val roomsMin: Int? = null,
    @SerialName("rooms_max") val roomsMax: Int? = null,
    @SerialName("year_min") val yearMin: Int? = null,
    @SerialName("year_max") val yearMax: Int? = null,
    @SerialName("floor_min") val floorMin: Int? = null,
    @SerialName("floor_max") val floorMax: Int? = null,
    @SerialName("want_parking") val wantParking: Boolean = false,
    @SerialName("want_storage") val wantStorage: Boolean = false,
    @SerialName("want_elevator") val wantElevator: Boolean = false,
    val email: String? = null,
    @SerialName("phone_alt") val phoneAlt: String? = null,
    val city: String? = null,
    val district: String? = null,
    val address: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("matching_tolerance_percent") val matchingTolerancePercent: Int? = null,
)

@Serializable
data class PropertyMatchDto(
    val source: String,
    val score: Int = 0,
    val title: String? = null,
    @SerialName("price_label") val priceLabel: String? = null,
    val neighborhood: String? = null,
    val city: String? = null,
    val area: Double? = null,
    val rooms: String? = null,
    val link: String? = null,
    val token: String? = null,
    @SerialName("property_id") val propertyId: Long? = null,
    @SerialName("listing_id") val listingId: Long? = null,
    @SerialName("property_type") val propertyType: String? = null,
    @SerialName("match_intent") val matchIntent: String? = null,
    @SerialName("intent_label") val intentLabel: String? = null,
    val reasons: List<String> = emptyList(),
)

@Serializable
data class ContactMatchGroupDto(
    val id: String = "",
    val title: String = "",
    val hint: String? = null,
    @SerialName("crm_matches") val crmMatches: List<PropertyMatchDto> = emptyList(),
    @SerialName("divar_matches") val divarMatches: List<PropertyMatchDto> = emptyList(),
    @SerialName("total_count") val totalCount: Int = 0,
)

@Serializable
data class ContactMatchesData(
    val eligible: Boolean = false,
    val message: String? = null,
    @SerialName("is_builder") val isBuilder: Boolean = false,
    @SerialName("divar_locked") val divarLocked: Boolean = false,
    @SerialName("match_groups") val matchGroups: List<ContactMatchGroupDto> = emptyList(),
    @SerialName("crm_matches") val crmMatches: List<PropertyMatchDto> = emptyList(),
    @SerialName("divar_matches") val divarMatches: List<PropertyMatchDto> = emptyList(),
    @SerialName("total_count") val totalCount: Int = 0,
)

@Serializable
data class ContactSuggestRequest(
    val matches: List<PropertyMatchDto>,
    val note: String? = null,
)

@Serializable
data class ContactSuggestResponse(
    @SerialName("suggested_count") val suggestedCount: Int = 0,
    @SerialName("whatsapp_text") val whatsappText: String? = null,
    @SerialName("public_url") val publicUrl: String? = null,
)

@Serializable
data class ContactCreateRequest(
    @SerialName("full_name") val fullName: String,
    val phone: String,
    val source: String = "موبایل",
    @SerialName("customer_type") val customerType: String = "سرنخ",
)

@Serializable
data class QuickLeadRequest(
    @SerialName("full_name") val fullName: String,
    val phone: String,
    val source: String = "موبایل",
)

@Serializable
data class TodayData(
    val date: String? = null,
    val overdue: List<TodayItemDto> = emptyList(),
    val today: List<TodayItemDto> = emptyList(),
    val done: List<TodayItemDto> = emptyList(),
    val stats: TodayStatsDto? = null,
)

@Serializable
data class TodayItemDto(
    val type: String? = null,
    val contact: ContactDto? = null,
    val reminder: ReminderDto? = null,
)

@Serializable
data class TodayStatsDto(
    val total: Int = 0,
    val done: Int = 0,
)

@Serializable
data class ReminderDto(
    val id: Long? = null,
    val title: String,
    val note: String = "",
    @SerialName("contact_id") val contactId: Long? = null,
    @SerialName("contact_name") val contactName: String = "",
    @SerialName("due_at") val dueAt: String? = null,
    val done: Boolean = false,
    val token: String = "",
    val recurrence: String = "",
    @SerialName("series_id") val seriesId: Long? = null,
)

@Serializable
data class DatasetDto(
    val id: String,
    val name: String,
    val source: String? = null,
    @SerialName("transaction_type") val transactionType: String? = null,
    @SerialName("subcategory") val subcategory: String? = null,
    val category: String? = null,
    val city: String? = null,
    val district: String? = null,
    @SerialName("item_count") val itemCount: Int = 0,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("original_filename") val originalFilename: String? = null,
    @SerialName("file_format") val fileFormat: String? = null,
)

@Serializable
data class ListingDto(
    val token: String,
    val title: String? = null,
    val price: Long? = null,
    val deposit: Long? = null,
    val rent: Long? = null,
    val area: Int? = null,
    val rooms: Int? = null,
    val district: String? = null,
    val city: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("share_link") val shareLink: String? = null,
    @SerialName("advertiser_type") val advertiserType: String? = null,
    @SerialName("advertiser_signal") val advertiserSignal: String? = null,
    @SerialName("advertiser_signal_label") val advertiserSignalLabel: String? = null,
    @SerialName("business_type") val businessType: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("dataset_id") val datasetId: String? = null,
    @SerialName("dataset_name") val datasetName: String? = null,
    @SerialName("price_per_sqm") val pricePerSqm: Long? = null,
    @SerialName("year_built") val yearBuilt: String? = null,
    val floor: String? = null,
    @SerialName("transaction_type") val transactionType: String? = null,
    @SerialName("feature_highlights") val featureHighlights: List<String> = emptyList(),
    @SerialName("scraped_at") val scrapedAt: String? = null,
    @SerialName("unit_status") val unitStatus: String? = null,
    @SerialName("has_parking") val hasParking: Boolean? = null,
    @SerialName("has_storage") val hasStorage: Boolean? = null,
    @SerialName("has_elevator") val hasElevator: Boolean? = null,
)

@Serializable
data class ExtractionUploadRequest(
    val filters: ExtractionFiltersDto,
    @SerialName("started_at") val startedAt: String,
    @SerialName("finished_at") val finishedAt: String,
    val items: List<ExtractionItemDto>,
    @SerialName("run_id") val runId: Long? = null,
    @SerialName("schedule_id") val scheduleId: Long? = null,
)

@Serializable
data class ExtractionFiltersDto(
    @SerialName("city_id") val cityId: String,
    @SerialName("city_name") val cityName: String? = null,
    @SerialName("district_ids") val districtIds: List<String> = emptyList(),
    val category: String,
    val sort: String = "sort_date",
    @SerialName("max_items") val maxItems: Int,
    @SerialName("price_min") val priceMin: Long? = null,
    @SerialName("price_max") val priceMax: Long? = null,
    @SerialName("deposit_min") val depositMin: Long? = null,
    @SerialName("deposit_max") val depositMax: Long? = null,
    @SerialName("rent_min") val rentMin: Long? = null,
    @SerialName("rent_max") val rentMax: Long? = null,
    @SerialName("area_min") val areaMin: Int? = null,
    @SerialName("area_max") val areaMax: Int? = null,
    @SerialName("year_min") val yearMin: Int? = null,
    @SerialName("year_max") val yearMax: Int? = null,
    val rooms: List<String> = emptyList(),
    @SerialName("advertiser_filter") val advertiserFilter: String = "all",
    @SerialName("district_names") val districtNames: List<String> = emptyList(),
    @SerialName("province_name") val provinceName: String? = null,
    @SerialName("category_label") val categoryLabel: String? = null,
    @SerialName("transaction_type_label") val transactionTypeLabel: String? = null,
    @SerialName("output_name_hint") val outputNameHint: String? = null,
    @SerialName("search_query") val searchQuery: String? = null,
    @SerialName("source_client") val sourceClient: String = "android_light",
)

@Serializable
data class ExtractionItemDto(
    val token: String,
    val raw: JsonElement,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("image_urls") val imageUrls: List<String> = emptyList(),
)

@Serializable
data class ExtractionUploadData(
    @SerialName("dataset_id") val datasetId: String,
    @SerialName("dataset_name") val datasetName: String? = null,
    @SerialName("ingested_count") val ingestedCount: Int,
    @SerialName("skipped_count") val skippedCount: Int = 0,
    @SerialName("created_count") val createdCount: Int = 0,
    @SerialName("updated_count") val updatedCount: Int = 0,
    @SerialName("duplicate_count") val duplicateCount: Int = 0,
    @SerialName("genuine_personal_count") val genuinePersonalCount: Int = 0,
    @SerialName("disguised_consultant_count") val disguisedConsultantCount: Int = 0,
    @SerialName("dataset_merged") val datasetMerged: Boolean = false,
    @SerialName("total_in_dataset") val totalInDataset: Int = 0,
)

@Serializable
data class ExtractionLimitsData(
    @SerialName("max_items") val maxItems: Int = 500,
    @SerialName("max_concurrent_hint") val maxConcurrentHint: Int = 2,
    @SerialName("extractions_today") val extractionsToday: Int = 0,
    @SerialName("extractions_daily_limit") val extractionsDailyLimit: Int = 10,
    @SerialName("can_extract_now") val canExtractNow: Boolean = true,
    @SerialName("remaining_today") val remainingToday: Int = 10,
)

@Serializable
data class ExtractionScheduleDto(
    val id: Long,
    val title: String,
    @SerialName("is_enabled") val isEnabled: Boolean = true,
    @SerialName("interval_hours") val intervalHours: Double = 6.0,
    val filters: ExtractionFiltersDto = ExtractionFiltersDto(cityId = "1", category = "apartment-rent", maxItems = 50),
    @SerialName("max_items") val maxItems: Int = 50,
    @SerialName("next_run_at") val nextRunAt: String? = null,
    @SerialName("last_run_at") val lastRunAt: String? = null,
    @SerialName("last_status") val lastStatus: String? = null,
    @SerialName("last_error") val lastError: String? = null,
    @SerialName("last_dataset_id") val lastDatasetId: String? = null,
    @SerialName("run_count") val runCount: Int = 0,
    @SerialName("consecutive_failures") val consecutiveFailures: Int = 0,
    @SerialName("primary_device_id") val primaryDeviceId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class ExtractionRunDto(
    val id: Long,
    @SerialName("schedule_id") val scheduleId: Long? = null,
    val status: String? = null,
    val trigger: String? = null,
    val filters: ExtractionFiltersDto? = null,
    @SerialName("dataset_id") val datasetId: String? = null,
    @SerialName("ingested_count") val ingestedCount: Int = 0,
    @SerialName("created_count") val createdCount: Int = 0,
    @SerialName("updated_count") val updatedCount: Int = 0,
    @SerialName("skipped_count") val skippedCount: Int = 0,
    val error: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("finished_at") val finishedAt: String? = null,
)

@Serializable
data class ExtractionScheduleCreateRequest(
    val title: String = "",
    @SerialName("interval_hours") val intervalHours: Double = 6.0,
    val filters: ExtractionFiltersDto,
    @SerialName("max_items") val maxItems: Int = 50,
    @SerialName("is_enabled") val isEnabled: Boolean = true,
)

@Serializable
data class ExtractionScheduleUpdateRequest(
    val title: String? = null,
    @SerialName("interval_hours") val intervalHours: Double? = null,
    val filters: ExtractionFiltersDto? = null,
    @SerialName("max_items") val maxItems: Int? = null,
    @SerialName("is_enabled") val isEnabled: Boolean? = null,
)

@Serializable
data class ExtractionRunFailRequest(
    val error: String = "",
)

@Serializable
data class ScheduleRunStartData(
    val run: ExtractionRunDto,
    val schedule: ExtractionScheduleDto? = null,
    val filters: ExtractionFiltersDto,
)

@Serializable
data class DashboardData(
    val stats: DashboardStatsDto = DashboardStatsDto(),
    @SerialName("today_preview") val todayPreview: List<TodayItemDto> = emptyList(),
    val notifications: List<NotificationDto> = emptyList(),
    @SerialName("notifications_unread") val notificationsUnread: Int = 0,
    @SerialName("latest_datasets") val latestDatasets: List<DatasetDto> = emptyList(),
    val license: LicenseStatusData? = null,
)

@Serializable
data class DashboardStatsDto(
    val contacts: Int = 0,
    @SerialName("contacts_new") val contactsNew: Int = 0,
    @SerialName("contacts_in_progress") val contactsInProgress: Int = 0,
    val deals: Int = 0,
    val properties: Int = 0,
    @SerialName("new_files_today") val newFilesToday: Int = 0,
    @SerialName("today_tasks_total") val todayTasksTotal: Int = 0,
    @SerialName("today_tasks_done") val todayTasksDone: Int = 0,
    @SerialName("overdue_count") val overdueCount: Int = 0,
    @SerialName("active_reminders") val activeReminders: Int = 0,
    @SerialName("overdue_followups") val overdueFollowups: Int = 0,
)

@Serializable
data class NotificationDto(
    val id: Long,
    val type: String? = null,
    val title: String,
    val body: String? = null,
    @SerialName("deep_link") val deepLink: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class ContactDetailData(
    val contact: ContactDto,
    val activities: List<ActivityDto> = emptyList(),
    val reminders: List<ReminderDto> = emptyList(),
    @SerialName("linked_listings") val linkedListings: List<LinkedListingDto> = emptyList(),
    val deals: List<DealDto> = emptyList(),
    val properties: List<PropertyDto> = emptyList(),
    val documents: List<CustomerDocumentDto> = emptyList(),
)

@Serializable
data class ActivityDto(
    val id: Long,
    @SerialName("activity_type") val activityType: String? = null,
    @SerialName("activity_type_label") val activityTypeLabel: String? = null,
    val title: String? = null,
    val content: String? = null,
    val token: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("customer_id") val customerId: Long? = null,
    @SerialName("customer_name") val customerName: String? = null,
)

@Serializable
data class LinkedListingDto(
    val id: Long,
    val token: String,
    val title: String? = null,
    val price: String? = null,
    val area: String? = null,
    val link: String? = null,
    val role: String? = null,
    @SerialName("deal_type") val dealType: String? = null,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class CustomerDocumentDto(
    val id: Long,
    val title: String,
    @SerialName("doc_type") val docType: String? = null,
    @SerialName("file_url") val fileUrl: String? = null,
    @SerialName("uploaded_at") val uploadedAt: String? = null,
)

@Serializable
data class DealDto(
    val id: Long,
    val title: String,
    val stage: String? = null,
    val amount: Long? = null,
    @SerialName("listing_token") val listingToken: String? = null,
    @SerialName("property_id") val propertyId: Long? = null,
    @SerialName("customer_id") val customerId: Long? = null,
    val probability: Int? = null,
    @SerialName("commission_rate") val commissionRate: Double? = null,
    @SerialName("commission_amount") val commissionAmount: Long? = null,
    val notes: String? = null,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("property_title") val propertyTitle: String? = null,
    val checklist: List<DealChecklistItemDto> = emptyList(),
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("lost_reason") val lostReason: String? = null,
    @SerialName("contract_number") val contractNumber: String? = null,
    @SerialName("contract_amount") val contractAmount: Long? = null,
    @SerialName("expected_close_date") val expectedCloseDate: String? = null,
)

@Serializable
data class DealChecklistItemDto(
    val id: String? = null,
    val label: String? = null,
    val required: Boolean = false,
    val done: Boolean = false,
)

@Serializable
data class PropertyDto(
    val id: Long,
    val title: String,
    @SerialName("deal_mode") val dealMode: String? = null,
    @SerialName("transaction_status") val transactionStatus: String? = null,
    @SerialName("property_type") val propertyType: String? = null,
    @SerialName("publish_status") val publishStatus: String? = null,
    val city: String? = null,
    val district: String? = null,
    val neighborhood: String? = null,
    @SerialName("sale_price") val salePrice: Long? = null,
    val rent: Long? = null,
    val deposit: Long? = null,
    val area: Double? = null,
    val rooms: String? = null,
    val floor: Int? = null,
    @SerialName("total_floors") val totalFloors: Int? = null,
    @SerialName("build_year") val buildYear: Int? = null,
    @SerialName("has_parking") val hasParking: Boolean = false,
    @SerialName("has_storage") val hasStorage: Boolean = false,
    @SerialName("has_elevator") val hasElevator: Boolean = false,
    @SerialName("is_vacant") val isVacant: Boolean = false,
    val amenities: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val token: String? = null,
    val link: String? = null,
    val phone: String? = null,
    @SerialName("owner_name") val ownerName: String? = null,
    val images: List<String> = emptyList(),
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class DealCreateRequest(
    @SerialName("customer_id") val customerId: Long,
    val title: String = "",
    val stage: String = "سرنخ",
    val amount: Long? = null,
    @SerialName("property_id") val propertyId: Long? = null,
    @SerialName("listing_token") val listingToken: String? = null,
    val notes: String = "",
    @SerialName("commission_rate") val commissionRate: Double? = null,
)

@Serializable
data class DealUpdateRequest(
    val title: String? = null,
    val stage: String? = null,
    val amount: Long? = null,
    val notes: String? = null,
    val probability: Int? = null,
    @SerialName("commission_rate") val commissionRate: Double? = null,
    @SerialName("property_id") val propertyId: Long? = null,
)

@Serializable
data class DealChecklistToggleRequest(
    @SerialName("item_id") val itemId: String,
    val done: Boolean? = null,
)

@Serializable
data class DealChecklistProgressDto(
    val total: Int = 0,
    val done: Int = 0,
    @SerialName("required_total") val requiredTotal: Int = 0,
    @SerialName("required_done") val requiredDone: Int = 0,
    val pct: Int = 0,
    val complete: Boolean = false,
)

@Serializable
data class DealChecklistToggleResponse(
    val checklist: List<DealChecklistItemDto> = emptyList(),
    val progress: DealChecklistProgressDto? = null,
)

@Serializable
data class DealStageRequest(
    val stage: String,
    @SerialName("lost_reason") val lostReason: String? = null,
)

@Serializable
data class PropertyCreateRequest(
    val title: String,
    @SerialName("deal_mode") val dealMode: String = "فروش",
    @SerialName("transaction_status") val transactionStatus: String = "فعال",
    @SerialName("property_type") val propertyType: String = "آپارتمان",
    val city: String = "",
    val district: String = "",
    val neighborhood: String = "",
    @SerialName("sale_price") val salePrice: Long? = null,
    val rent: Long? = null,
    val deposit: Long? = null,
    val area: Double? = null,
    val rooms: String = "",
    val floor: Int? = null,
    @SerialName("build_year") val buildYear: Int? = null,
    @SerialName("has_parking") val hasParking: Boolean? = null,
    @SerialName("has_storage") val hasStorage: Boolean? = null,
    @SerialName("has_elevator") val hasElevator: Boolean? = null,
    val images: List<String> = emptyList(),
    @SerialName("contact_id") val contactId: Long? = null,
    val token: String = "",
    val link: String = "",
    @SerialName("owner_phone") val ownerPhone: String = "",
    @SerialName("owner_name") val ownerName: String = "",
    val notes: String = "",
)

@Serializable
data class PropertyUpdateRequest(
    val title: String? = null,
    @SerialName("deal_mode") val dealMode: String? = null,
    @SerialName("transaction_status") val transactionStatus: String? = null,
    @SerialName("property_type") val propertyType: String? = null,
    val city: String? = null,
    val district: String? = null,
    val neighborhood: String? = null,
    @SerialName("sale_price") val salePrice: Long? = null,
    val rent: Long? = null,
    val deposit: Long? = null,
    val area: Double? = null,
    val rooms: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val floor: Int? = null,
    @SerialName("total_floors") val totalFloors: Int? = null,
    @SerialName("build_year") val buildYear: Int? = null,
    @SerialName("has_parking") val hasParking: Boolean? = null,
    @SerialName("has_storage") val hasStorage: Boolean? = null,
    @SerialName("has_elevator") val hasElevator: Boolean? = null,
    @SerialName("is_vacant") val isVacant: Boolean? = null,
    val amenities: String? = null,
    @SerialName("owner_name") val ownerName: String? = null,
    @SerialName("owner_phone") val ownerPhone: String? = null,
)

@Serializable
data class PropertyStatusRequest(
    @SerialName("transaction_status") val transactionStatus: String,
)

@Serializable
data class PropertyContactLinkDto(
    val id: Long,
    @SerialName("customer_id") val customerId: Long,
    @SerialName("customer_name") val customerName: String,
    val phone: String? = null,
    val status: String? = null,
    val role: String? = null,
    @SerialName("deal_type") val dealType: String? = null,
    @SerialName("interest_level") val interestLevel: String? = null,
    @SerialName("is_primary") val isPrimary: Boolean = false,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class PropertyLinkContactRequest(
    @SerialName("customer_id") val customerId: Long,
    val role: String = "پیشنهادی",
    @SerialName("deal_type") val dealType: String = "سایر",
    @SerialName("interest_level") val interestLevel: String = "",
    @SerialName("is_primary") val isPrimary: Boolean = false,
    val notes: String = "",
)

@Serializable
data class ListingFeatureItemDto(
    val key: String? = null,
    val label: String? = null,
    val value: String? = null,
    val state: String? = null,
    @SerialName("is_chip") val isChip: Boolean = false,
    val chips: List<String> = emptyList(),
)

@Serializable
data class ListingFeatureGroupDto(
    val id: String? = null,
    val title: String? = null,
    val items: List<ListingFeatureItemDto> = emptyList(),
)

@Serializable
data class ListingFeatureCoreDto(
    val key: String? = null,
    val label: String? = null,
    val value: String? = null,
    val state: String? = null,
)

@Serializable
data class ListingFeatureProfileDto(
    val core: List<ListingFeatureCoreDto> = emptyList(),
    val groups: List<ListingFeatureGroupDto> = emptyList(),
    @SerialName("has_details") val hasDetails: Boolean = false,
    @SerialName("detail_count") val detailCount: Int = 0,
)

@Serializable
data class PropertyDetailData(
    val property: PropertyDto,
    val contacts: List<PropertyContactLinkDto> = emptyList(),
    val activities: List<ActivityDto> = emptyList(),
    val documents: List<CustomerDocumentDto> = emptyList(),
    @SerialName("feature_profile") val featureProfile: ListingFeatureProfileDto? = null,
    @SerialName("listing_highlights") val listingHighlights: List<String> = emptyList(),
    @SerialName("can_edit") val canEdit: Boolean = true,
    @SerialName("mask_sensitive") val maskSensitive: Boolean = false,
    @SerialName("contact_count") val contactCount: Int = 0,
    @SerialName("public_share") val publicShare: ListingPublicShareDto? = null,
)

@Serializable
data class DealPipelineData(
    val stages: List<String> = emptyList(),
    val columns: List<DealPipelineColumnDto> = emptyList(),
)

@Serializable
data class DealPipelineColumnDto(
    val stage: String,
    val count: Int = 0,
    @SerialName("total_value") val totalValue: Long = 0,
    val deals: List<DealDto> = emptyList(),
)

@Serializable
data class DealStagesData(
    val stages: List<String> = emptyList(),
)

@Serializable
data class ListingPublicShareDto(
    @SerialName("share_url") val shareUrl: String = "",
    @SerialName("share_token") val shareToken: String = "",
    @SerialName("view_count") val viewCount: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("consultant_name") val consultantName: String = "",
    @SerialName("consultant_phone") val consultantPhone: String = "",
    @SerialName("welcome_message") val welcomeMessage: String = "",
    @SerialName("show_divar_link") val showDivarLink: Boolean = false,
    @SerialName("show_full_address") val showFullAddress: Boolean = false,
    @SerialName("show_internal_notes") val showInternalNotes: Boolean = false,
    @SerialName("default_share_message") val defaultShareMessage: String = "",
    @SerialName("share_message_rendered") val shareMessageRendered: String = "",
    @SerialName("approximate_location") val approximateLocation: Boolean = false,
    @SerialName("approximate_location_radius_m") val approximateLocationRadiusM: Int = 500,
    @SerialName("show_nearby_pois") val showNearbyPois: Boolean = false,
    @SerialName("welcome_is_custom") val welcomeIsCustom: Boolean = false,
)

@Serializable
data class ListingPublicShareUpdateRequest(
    @SerialName("consultant_name") val consultantName: String? = null,
    @SerialName("consultant_phone") val consultantPhone: String? = null,
    @SerialName("welcome_message") val welcomeMessage: String? = null,
    @SerialName("show_divar_link") val showDivarLink: Boolean? = null,
    @SerialName("show_full_address") val showFullAddress: Boolean? = null,
    @SerialName("show_internal_notes") val showInternalNotes: Boolean? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
    @SerialName("approximate_location") val approximateLocation: Boolean? = null,
    @SerialName("approximate_location_radius_m") val approximateLocationRadiusM: Int? = null,
    @SerialName("show_nearby_pois") val showNearbyPois: Boolean? = null,
    @SerialName("default_share_message") val defaultShareMessage: String? = null,
)

@Serializable
data class ListingDetailDto(
    val token: String,
    val title: String? = null,
    val description: String? = null,
    val price: Long? = null,
    val deposit: Long? = null,
    val rent: Long? = null,
    val area: Int? = null,
    val rooms: Int? = null,
    val district: String? = null,
    val city: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val images: List<String> = emptyList(),
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("share_link") val shareLink: String? = null,
    @SerialName("advertiser_type") val advertiserType: String? = null,
    @SerialName("advertiser_signal") val advertiserSignal: String? = null,
    @SerialName("advertiser_signal_label") val advertiserSignalLabel: String? = null,
    @SerialName("business_type") val businessType: String? = null,
    @SerialName("year_built") val yearBuilt: String? = null,
    val floor: String? = null,
    @SerialName("total_floors") val totalFloors: String? = null,
    @SerialName("price_per_sqm") val pricePerSqm: Long? = null,
    @SerialName("scraped_at") val scrapedAt: String? = null,
    @SerialName("dataset_id") val datasetId: String? = null,
    @SerialName("is_expired") val isExpired: Boolean = false,
    @SerialName("owner_phone") val ownerPhone: String? = null,
    @SerialName("owner_name") val ownerName: String? = null,
    @SerialName("unit_status") val unitStatus: String? = null,
    @SerialName("has_parking") val hasParking: Boolean? = null,
    @SerialName("has_storage") val hasStorage: Boolean? = null,
    @SerialName("has_elevator") val hasElevator: Boolean? = null,
    @SerialName("public_share") val publicShare: ListingPublicShareDto? = null,
    @SerialName("feature_profile") val featureProfile: ListingFeatureProfileDto? = null,
    @SerialName("listing_highlights") val listingHighlights: List<String> = emptyList(),
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("transaction_type") val transactionType: String? = null,
)

@Serializable
data class ListingUpdateRequest(
    val title: String? = null,
    val price: Long? = null,
    val deposit: Long? = null,
    val rent: Long? = null,
    val area: Double? = null,
    val rooms: String? = null,
    val floor: String? = null,
    @SerialName("build_year") val buildYear: String? = null,
    val neighborhood: String? = null,
    val city: String? = null,
    @SerialName("business_type") val businessType: String? = null,
    val description: String? = null,
    val link: String? = null,
    @SerialName("owner_phone") val ownerPhone: String? = null,
    @SerialName("owner_name") val ownerName: String? = null,
)

@Serializable
data class ActivityCreateRequest(
    @SerialName("activity_type") val activityType: String,
    val content: String = "",
    val title: String = "",
    val token: String = "",
)

@Serializable
data class NoteCreateRequest(
    val content: String,
    val title: String = "یادداشت",
)

@Serializable
data class ReminderCreateRequest(
    val title: String,
    @SerialName("due_at") val dueAt: String,
    val note: String = "",
    val recurrence: String = "",
    @SerialName("contact_id") val contactId: Long? = null,
    val token: String = "",
)

@Serializable
data class ReminderPatchRequest(
    val action: String? = null,
    val title: String? = null,
    val note: String? = null,
    @SerialName("due_at") val dueAt: String? = null,
    val recurrence: String? = null,
    val days: Int? = null,
    val hours: Int? = null,
)

@Serializable
data class TodayActionRequest(
    @SerialName("contact_id") val contactId: Long? = null,
    @SerialName("reminder_id") val reminderId: Long? = null,
    val action: String = "complete",
    val days: Int = 1,
    val note: String = "",
)

@Serializable
data class LinkListingRequest(
    val token: String,
    val title: String = "",
    val price: String = "",
    val area: String = "",
    val link: String = "",
    val role: String = "پیشنهادی",
)

@Serializable
data class SendListingRequest(
    val token: String,
    val title: String = "",
    val price: String = "",
    val area: String = "",
    val link: String = "",
    val role: String = "پیشنهادی",
    val note: String = "",
    @SerialName("share_message") val shareMessage: String = "",
)

@Serializable
data class ContactUpdateRequest(
    @SerialName("full_name") val fullName: String? = null,
    val phone: String? = null,
    val status: String? = null,
    @SerialName("customer_type") val customerType: String? = null,
    val priority: String? = null,
    val notes: String? = null,
    val budget: Long? = null,
    @SerialName("budget_min") val budgetMin: Long? = null,
    @SerialName("budget_max") val budgetMax: Long? = null,
    @SerialName("deposit_min") val depositMin: Long? = null,
    @SerialName("deposit_max") val depositMax: Long? = null,
    @SerialName("rent_min") val rentMin: Long? = null,
    @SerialName("rent_max") val rentMax: Long? = null,
    @SerialName("property_type") val propertyType: String? = null,
    @SerialName("min_area") val minArea: Int? = null,
    @SerialName("max_area") val maxArea: Int? = null,
    val rooms: String? = null,
    val areas: String? = null,
    @SerialName("builder_buy_property_types") val builderBuyPropertyTypes: String? = null,
    @SerialName("builder_buy_budget_min") val builderBuyBudgetMin: Long? = null,
    @SerialName("builder_buy_budget_max") val builderBuyBudgetMax: Long? = null,
    @SerialName("builder_buy_areas") val builderBuyAreas: String? = null,
    @SerialName("builder_buy_min_area") val builderBuyMinArea: Int? = null,
    @SerialName("builder_buy_max_area") val builderBuyMaxArea: Int? = null,
    val city: String? = null,
    val district: String? = null,
    val email: String? = null,
    @SerialName("phone_alt") val phoneAlt: String? = null,
    val source: String? = null,
    @SerialName("matching_tolerance_percent") val matchingTolerancePercent: Int? = null,
    @SerialName("rooms_min") val roomsMin: Int? = null,
    @SerialName("rooms_max") val roomsMax: Int? = null,
    @SerialName("year_min") val yearMin: Int? = null,
    @SerialName("year_max") val yearMax: Int? = null,
    @SerialName("floor_min") val floorMin: Int? = null,
    @SerialName("floor_max") val floorMax: Int? = null,
    @SerialName("want_parking") val wantParking: Boolean? = null,
    @SerialName("want_storage") val wantStorage: Boolean? = null,
    @SerialName("want_elevator") val wantElevator: Boolean? = null,
)

@Serializable
data class PaginatedResult<T>(
    val items: List<T>,
    val page: Int,
    val total: Int,
    val hasMore: Boolean,
    val neighborhoods: List<String> = emptyList(),
    val sort: String? = null,
)

@Serializable
data class SyncPushRequest(
    val operations: List<SyncOperation>,
)

@Serializable
data class SyncOperation(
    @SerialName("op_id") val opId: String,
    val entity: String,
    val action: String,
    val payload: kotlinx.serialization.json.JsonObject = kotlinx.serialization.json.JsonObject(emptyMap()),
)

@Serializable
data class SyncPushResultData(
    val mapped: List<SyncMappedOp> = emptyList(),
    val conflicts: List<SyncConflictOp> = emptyList(),
)

@Serializable
data class SyncMappedOp(
    @SerialName("op_id") val opId: String,
    @SerialName("server_id") val serverId: Long,
)

@Serializable
data class SyncConflictOp(
    @SerialName("op_id") val opId: String,
    val reason: String = "",
)

@Serializable
data class SyncPullData(
    @SerialName("server_time") val serverTime: String,
    val entities: List<String> = emptyList(),
    val contacts: SyncEntityBatch<ContactDto>? = null,
    val deals: SyncEntityBatch<DealDto>? = null,
    val properties: SyncEntityBatch<PropertyDto>? = null,
    val reminders: SyncEntityBatch<ReminderDto>? = null,
    val activities: SyncEntityBatch<ActivityDto>? = null,
)

@Serializable
data class SyncEntityBatch<T>(
    val upserted: List<T> = emptyList(),
    @SerialName("deleted_ids") val deletedIds: List<Long> = emptyList(),
)

@Serializable
data class DatasetInsightsMetaDto(
    @SerialName("row_count") val rowCount: Int = 0,
    @SerialName("clean_count") val cleanCount: Int = 0,
    @SerialName("geo_count") val geoCount: Int = 0,
    @SerialName("transaction_type") val transactionType: String = "",
    @SerialName("is_rent") val isRent: Boolean = false,
    @SerialName("filter_value_label") val filterValueLabel: String = "قیمت",
)

@Serializable
data class DatasetInsightsData(
    val dataset: DatasetDto? = null,
    val meta: DatasetInsightsMetaDto = DatasetInsightsMetaDto(),
    val confidence: kotlinx.serialization.json.JsonElement? = null,
    val header: kotlinx.serialization.json.JsonObject = kotlinx.serialization.json.JsonObject(emptyMap()),
    @SerialName("quick_snapshot")
    val quickSnapshot: kotlinx.serialization.json.JsonObject = kotlinx.serialization.json.JsonObject(emptyMap()),
    val neighborhoods: List<kotlinx.serialization.json.JsonElement> = emptyList(),
    val opportunities: List<kotlinx.serialization.json.JsonElement> = emptyList(),
    val insights: List<kotlinx.serialization.json.JsonElement> = emptyList(),
    val negotiation: List<kotlinx.serialization.json.JsonElement> = emptyList(),
)

@Serializable
data class DatasetMapMarkerDto(
    val lat: Double? = null,
    val lng: Double? = null,
    val token: String? = null,
    val title: String? = null,
    val price: Long? = null,
    @SerialName("price_label") val priceLabel: String? = null,
    @SerialName("deposit_label") val depositLabel: String? = null,
    @SerialName("rent_label") val rentLabel: String? = null,
    val area: Double? = null,
    @SerialName("area_label") val areaLabel: String? = null,
    val rooms: String? = null,
    @SerialName("pps_label") val ppsLabel: String? = null,
    val neighborhood: String? = null,
    val city: String? = null,
    val thumb: String? = null,
    @SerialName("is_consultant") val isConsultant: Boolean = false,
    @SerialName("is_disguised") val isDisguised: Boolean = false,
    @SerialName("seller_type") val sellerType: String? = null,
    @SerialName("advertiser_signal") val advertiserSignal: String? = null,
    @SerialName("market_tier") val marketTier: String? = null,
    val verdict: String? = null,
    @SerialName("value_score") val valueScore: Double? = null,
    @SerialName("filter_value") val filterValue: Long? = null,
    @SerialName("location_label") val locationLabel: String? = null,
    val color: String? = null,
    val quartile: Int? = null,
)

@Serializable
data class DatasetMapData(
    val dataset: DatasetDto? = null,
    val markers: List<DatasetMapMarkerDto> = emptyList(),
    @SerialName("geo_count") val geoCount: Int = 0,
    @SerialName("markers_shown") val markersShown: Int = 0,
    @SerialName("consultant_count") val consultantCount: Int = 0,
    @SerialName("personal_count") val personalCount: Int = 0,
    val config: DatasetMapConfigDto? = null,
)

@Serializable
data class DatasetMapConfigDto(
    @SerialName("dataset_name") val datasetName: String? = null,
    @SerialName("dataset_city") val datasetCity: String? = null,
    @SerialName("is_rent") val isRent: Boolean = false,
    @SerialName("filter_value_label") val filterValueLabel: String? = null,
)

@Serializable
data class PropertyContactMatchItemDto(
    @SerialName("customer_id") val customerId: Long,
    val score: Int = 0,
    @SerialName("full_name") val fullName: String? = null,
    val phone: String? = null,
    @SerialName("customer_type") val customerType: String? = null,
    val reasons: List<String> = emptyList(),
)

@Serializable
data class PropertyContactMatchesData(
    val eligible: Boolean = false,
    val forbidden: Boolean = false,
    val message: String? = null,
    val matches: List<PropertyContactMatchItemDto> = emptyList(),
    @SerialName("total_count") val totalCount: Int = 0,
)

@Serializable
data class PropertySuggestContactsRequest(
    @SerialName("customer_ids") val customerIds: List<Long>,
    val note: String? = null,
)

@Serializable
data class MessageTemplateDto(
    val id: Long,
    val title: String = "",
    val body: String = "",
    val category: String = "",
)

@Serializable
data class SavedFilterDto(
    val id: Long,
    val name: String = "",
    val scope: String = "",
    val entity: String = "",
    val params: Map<String, String> = emptyMap(),
    val payload: Map<String, String> = emptyMap(),
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("param_count") val paramCount: Int = 0,
    @SerialName("new_count") val newCount: Int = 0,
    @SerialName("updated_at") val updatedAt: String? = null,
) {
    val resolvedScope: String get() = scope.ifBlank { entity }
    val resolvedParams: Map<String, String>
        get() = params.ifEmpty { payload }
}

@Serializable
data class SavedFilterCreateRequest(
    val name: String,
    val scope: String,
    val params: Map<String, String>,
    @SerialName("is_pinned") val isPinned: Boolean = false,
)

@Serializable
data class SupportTicketDto(
    val id: Long,
    @SerialName("ticket_number") val ticketNumber: String = "",
    val subject: String = "",
    val status: String = "",
    val priority: String = "",
    val category: String = "",
    @SerialName("user_has_unread") val userHasUnread: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("last_message_at") val lastMessageAt: String? = null,
    @SerialName("closed_at") val closedAt: String? = null,
    val description: String = "",
    val messages: List<SupportTicketMessageDto> = emptyList(),
)

@Serializable
data class SupportTicketMessageDto(
    val id: Long,
    val body: String = "",
    @SerialName("is_staff_reply") val isStaffReply: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("author_name") val authorName: String = "",
    val attachments: List<SupportTicketAttachmentDto> = emptyList(),
)

@Serializable
data class SupportTicketAttachmentDto(
    val id: Long,
    @SerialName("original_filename") val originalFilename: String = "",
    @SerialName("file_size") val fileSize: Long = 0,
    val url: String = "",
)

@Serializable
data class SupportTicketCreateRequest(
    val subject: String,
    val body: String,
    val category: String = "other",
    val priority: String = "normal",
)

@Serializable
data class SupportTicketReplyResult(
    val ticket: SupportTicketDto? = null,
    val message: SupportTicketMessageDto? = null,
)

@Serializable
data class AiQuotaData(
    val remaining: Int = 0,
    val limit: Int = 0,
    @SerialName("plan_label") val planLabel: String? = null,
    val enabled: Boolean = true,
)

@Serializable
data class AiDraftMessageRequest(
    @SerialName("contact_id") val contactId: Long? = null,
    @SerialName("listing_token") val listingToken: String? = null,
    val tone: String = "رسمی",
    val intent: String? = null,
    val notes: String? = null,
)

@Serializable
data class AiSummarizeListingRequest(
    @SerialName("listing_token") val listingToken: String,
)

@Serializable
data class AiTextResult(
    val text: String = "",
    @SerialName("quota_remaining") val quotaRemaining: Int? = null,
    @SerialName("is_fallback") val isFallback: Boolean = false,
    val source: String? = null,
)

@Serializable
data class CloudExtractionCreateRequest(
    @SerialName("city_id") val cityId: String,
    val category: String,
    @SerialName("district_ids") val districtIds: List<String> = emptyList(),
    @SerialName("max_items") val maxItems: Int = 200,
    @SerialName("advertiser_filter") val advertiserFilter: String = "all",
    @SerialName("search_query") val searchQuery: String? = null,
    @SerialName("city_name") val cityName: String? = null,
    @SerialName("category_label") val categoryLabel: String? = null,
    @SerialName("district_names") val districtNames: List<String> = emptyList(),
)

@Serializable
data class CloudExtractionJobDto(
    val id: Long,
    val status: String = "queued",
    @SerialName("max_items") val maxItems: Int = 0,
    @SerialName("ingested_count") val ingestedCount: Int = 0,
    @SerialName("dataset_id") val datasetId: String? = null,
    val error: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("finished_at") val finishedAt: String? = null,
    val filters: Map<String, kotlinx.serialization.json.JsonElement> = emptyMap(),
)

@Serializable
data class AppVersionData(
    @SerialName("package_id") val packageId: String = "ir.divarfiling.mobile",
    @SerialName("version_name") val versionName: String = "",
    @SerialName("version_code") val versionCode: Int = 0,
    @SerialName("min_supported_version_code") val minSupportedVersionCode: Int = 0,
    @SerialName("force_update") val forceUpdate: Boolean = false,
    val available: Boolean = false,
    @SerialName("update_available") val updateAvailable: Boolean = false,
    @SerialName("apk_url") val apkUrl: String = "",
    @SerialName("apk_filename") val apkFilename: String = "",
    @SerialName("apk_size_bytes") val apkSizeBytes: Long = 0,
    @SerialName("apk_size_label") val apkSizeLabel: String = "",
    @SerialName("apk_sha256") val apkSha256: String = "",
    @SerialName("release_notes") val releaseNotes: String = "",
    @SerialName("min_android") val minAndroid: String = "",
    @SerialName("store_url") val storeUrl: String = "",
    @SerialName("website_url") val websiteUrl: String = "",
)

@Serializable
data class TeamUnreadDto(
    val messages: Int = 0,
    val notifications: Int = 0,
    val announcements: Int = 0,
    val total: Int = 0,
)

@Serializable
data class TeamAgencyDto(
    val id: Long = 0,
    val name: String = "",
    val slug: String = "",
)

@Serializable
data class TeamMembershipDto(
    val id: Long = 0,
    val role: String = "",
    @SerialName("role_label") val roleLabel: String = "",
    val title: String = "",
)

@Serializable
data class TeamPermissionsDto(
    @SerialName("can_manage") val canManage: Boolean = false,
    @SerialName("can_operate_inbox") val canOperateInbox: Boolean = false,
    @SerialName("can_broadcast") val canBroadcast: Boolean = false,
    @SerialName("messages_enabled") val messagesEnabled: Boolean = false,
    @SerialName("announcements_enabled") val announcementsEnabled: Boolean = false,
)

@Serializable
data class TeamOverviewDto(
    @SerialName("has_agency") val hasAgency: Boolean = false,
    val agency: TeamAgencyDto? = null,
    val membership: TeamMembershipDto? = null,
    val permissions: TeamPermissionsDto = TeamPermissionsDto(),
    val unread: TeamUnreadDto = TeamUnreadDto(),
    @SerialName("members_count") val membersCount: Int = 0,
    @SerialName("inbox_leads_count") val inboxLeadsCount: Int = 0,
)

@Serializable
data class TeamMemberDto(
    val id: Long,
    @SerialName("user_id") val userId: Long = 0,
    val name: String = "",
    val phone: String = "",
    val role: String = "",
    @SerialName("role_label") val roleLabel: String = "",
    val title: String = "",
    @SerialName("is_active") val isActive: Boolean = true,
)

@Serializable
data class TeamMembersPayload(
    val members: List<TeamMemberDto> = emptyList(),
    val count: Int = 0,
)

@Serializable
data class TeamMessageAttachmentDto(
    val id: Long = 0,
    val name: String = "",
    val size: Long = 0,
    @SerialName("content_type") val contentType: String = "",
    val url: String = "",
)

@Serializable
data class TeamChatMessageDto(
    val id: Long,
    val body: String = "",
    @SerialName("sender_id") val senderId: Long? = null,
    @SerialName("sender_name") val senderName: String = "",
    @SerialName("is_mine") val isMine: Boolean = false,
    @SerialName("can_delete") val canDelete: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
    val attachments: List<TeamMessageAttachmentDto> = emptyList(),
)

@Serializable
data class TeamThreadDto(
    val id: Long,
    val kind: String = "direct",
    val subject: String = "",
    @SerialName("participants_label") val participantsLabel: String = "",
    @SerialName("is_starred") val isStarred: Boolean = false,
    @SerialName("is_archived") val isArchived: Boolean = false,
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("last_message") val lastMessage: TeamChatMessageDto? = null,
)

@Serializable
data class TeamThreadsPayload(
    val threads: List<TeamThreadDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val folder: String = "inbox",
)

@Serializable
data class TeamThreadDetailDto(
    val thread: TeamThreadDto? = null,
    val messages: List<TeamChatMessageDto> = emptyList(),
)

@Serializable
data class TeamSendMessageRequest(
    val kind: String = "direct",
    val body: String = "",
    val subject: String? = null,
    @SerialName("recipient_member_id") val recipientMemberId: Long? = null,
    @SerialName("target_roles") val targetRoles: List<String> = emptyList(),
)

@Serializable
data class TeamReplyRequest(
    val body: String,
)

@Serializable
data class TeamThreadStateRequest(
    @SerialName("is_starred") val isStarred: Boolean? = null,
    @SerialName("is_archived") val isArchived: Boolean? = null,
)

@Serializable
data class TeamAnnouncementDto(
    val id: Long,
    val title: String = "",
    val body: String = "",
    @SerialName("body_preview") val bodyPreview: String = "",
    val importance: String = "normal",
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("is_read") val isRead: Boolean = false,
    val status: String = "active",
    @SerialName("published_at") val publishedAt: String = "",
    @SerialName("created_by") val createdBy: String = "",
    @SerialName("attachment_url") val attachmentUrl: String = "",
)

@Serializable
data class TeamAnnouncementsPayload(
    val announcements: List<TeamAnnouncementDto> = emptyList(),
    val total: Int = 0,
    val unread: Int = 0,
)

@Serializable
data class TeamAnnouncementDetailPayload(
    val announcement: TeamAnnouncementDto? = null,
)

@Serializable
data class TeamPanelNotificationDto(
    val id: Long,
    val kind: String = "",
    val title: String = "",
    val body: String = "",
    val link: String = "",
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
)

@Serializable
data class TeamPanelNotificationsPayload(
    val notifications: List<TeamPanelNotificationDto> = emptyList(),
    val unread: Int = 0,
)

@Serializable
data class TeamLeadDto(
    val id: Long,
    val name: String = "",
    val phone: String = "",
    val source: String = "",
    @SerialName("created_at") val createdAt: String = "",
)

@Serializable
data class TeamLeadsPayload(
    val leads: List<TeamLeadDto> = emptyList(),
    val count: Int = 0,
)

@Serializable
data class TeamAssignLeadsRequest(
    @SerialName("member_id") val memberId: Long,
    @SerialName("customer_ids") val customerIds: List<Long>,
)

@Serializable
data class ContactTeamAssignRequest(
    @SerialName("member_id") val memberId: Long,
)

@Serializable
data class ContactTeamTransferRequest(
    @SerialName("member_id") val memberId: Long,
    val note: String? = null,
)

@Serializable
data class TeamActionRequest(
    val action: String = "read",
    @SerialName("notification_id") val notificationId: Long? = null,
)
