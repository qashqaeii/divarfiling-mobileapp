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
    val notes: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
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
    val id: Long,
    val title: String,
    @SerialName("contact_id") val contactId: Long? = null,
    @SerialName("due_at") val dueAt: String? = null,
    val done: Boolean = false,
)

@Serializable
data class DatasetDto(
    val id: String,
    val name: String,
    val source: String? = null,
    @SerialName("transaction_type") val transactionType: String? = null,
    @SerialName("subcategory") val subcategory: String? = null,
    val city: String? = null,
    val district: String? = null,
    @SerialName("item_count") val itemCount: Int = 0,
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
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("share_link") val shareLink: String? = null,
    @SerialName("advertiser_type") val advertiserType: String? = null,
    @SerialName("business_type") val businessType: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
)

@Serializable
data class ExtractionUploadRequest(
    val filters: ExtractionFiltersDto,
    @SerialName("started_at") val startedAt: String,
    @SerialName("finished_at") val finishedAt: String,
    val items: List<ExtractionItemDto>,
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
    @SerialName("source_client") val sourceClient: String = "android_light",
)

@Serializable
data class ExtractionItemDto(
    val token: String,
    val raw: JsonElement,
)

@Serializable
data class ExtractionUploadData(
    @SerialName("dataset_id") val datasetId: String,
    @SerialName("ingested_count") val ingestedCount: Int,
    @SerialName("skipped_count") val skippedCount: Int = 0,
)

@Serializable
data class ExtractionLimitsData(
    @SerialName("max_items") val maxItems: Int = 100,
    @SerialName("max_concurrent_hint") val maxConcurrentHint: Int = 2,
    @SerialName("extractions_today") val extractionsToday: Int = 0,
    @SerialName("extractions_daily_limit") val extractionsDailyLimit: Int = 5,
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
)

@Serializable
data class DealDto(
    val id: Long,
    val title: String,
    val stage: String? = null,
    val amount: Long? = null,
    @SerialName("listing_token") val listingToken: String? = null,
    @SerialName("property_id") val propertyId: Long? = null,
)

@Serializable
data class PropertyDto(
    val id: Long,
    val title: String,
    @SerialName("deal_mode") val dealMode: String? = null,
    @SerialName("transaction_status") val transactionStatus: String? = null,
    val city: String? = null,
    val district: String? = null,
    @SerialName("sale_price") val salePrice: Long? = null,
    val rent: Long? = null,
    val deposit: Long? = null,
    val area: Double? = null,
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
    @SerialName("business_type") val businessType: String? = null,
    @SerialName("year_built") val yearBuilt: String? = null,
    val floor: String? = null,
    @SerialName("total_floors") val totalFloors: String? = null,
    @SerialName("price_per_sqm") val pricePerSqm: Int? = null,
    @SerialName("scraped_at") val scrapedAt: String? = null,
    @SerialName("dataset_id") val datasetId: String? = null,
    @SerialName("is_expired") val isExpired: Boolean = false,
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
data class ContactUpdateRequest(
    @SerialName("full_name") val fullName: String? = null,
    val phone: String? = null,
    val status: String? = null,
    @SerialName("customer_type") val customerType: String? = null,
    val priority: String? = null,
    val notes: String? = null,
)

@Serializable
data class PaginatedResult<T>(
    val items: List<T>,
    val page: Int,
    val total: Int,
    val hasMore: Boolean,
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
