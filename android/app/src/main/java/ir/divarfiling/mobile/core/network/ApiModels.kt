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
