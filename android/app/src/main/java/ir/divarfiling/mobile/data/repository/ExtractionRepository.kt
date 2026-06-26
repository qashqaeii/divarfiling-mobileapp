package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.license.ExtractLightLimits
import ir.divarfiling.mobile.core.license.LicenseState
import ir.divarfiling.mobile.core.network.ExtractionFiltersDto
import ir.divarfiling.mobile.core.network.ExtractionItemDto
import ir.divarfiling.mobile.core.network.ExtractionLimitsData
import ir.divarfiling.mobile.core.network.ExtractionUploadData
import ir.divarfiling.mobile.core.network.ExtractionUploadRequest
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.requireData
import ir.divarfiling.mobile.core.network.toUserMessage
import ir.divarfiling.mobile.feature.extract.divar.AdvertiserFilter
import ir.divarfiling.mobile.feature.extract.divar.DivarLightClient
import ir.divarfiling.mobile.feature.extract.divar.ExtractFilters
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

sealed class ExtractGateResult {
    data object Allowed : ExtractGateResult()
    data class Denied(val message: String) : ExtractGateResult()
}

@Singleton
class ExtractionRepository @Inject constructor(
    private val api: MobileApi,
    private val divarClient: DivarLightClient,
    private val sessionStore: SessionStore,
    private val licenseRepository: LicenseRepository,
    private val authRepository: AuthRepository,
    private val json: Json,
) {
    suspend fun checkExtractGate(): ExtractGateResult {
        licenseRepository.refreshLicense()
        val license = sessionStore.licenseState.first()
        return gateFromLicense(license)
    }

    fun gateFromLicense(license: LicenseState): ExtractGateResult {
        return when {
            !license.valid ->
                ExtractGateDenied("برای استخراج سبک نیاز به لایسنس فعال دارید.")
            !license.lightExtractEnabled ->
                ExtractGateDenied("پلن شما شامل استخراج موبایل نیست. از نرم‌افزار ویندوز استفاده کنید.")
            else -> ExtractGateResult.Allowed
        }
    }

    suspend fun getLimits(): ExtractionLimitsData? {
        return try {
            val response = api.extractionLimits()
            if (response.ok) response.requireData<ExtractionLimitsData>(json) else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun runLightExtraction(
        filters: ExtractFilters,
        onProgress: (Int, Int) -> Unit,
        isCancelled: () -> Boolean,
    ): ApiResult<ExtractionUploadData> {
        val gate = checkExtractGate()
        if (gate is ExtractGateResult.Denied) {
            return ApiResult.Error(gate.message, "LICENSE_REQUIRED")
        }

        val limits = getLimits()
        val maxItems = limits?.maxItems ?: ExtractLightLimits.MAX_ITEMS
        val safeFilters = filters.copy(
            maxItems = filters.maxItems.coerceIn(1, maxItems),
        )
        val startedAt = Instant.now().toString()

        val items = try {
            divarClient.collectItems(safeFilters, onProgress, isCancelled)
        } catch (e: Exception) {
            return ApiResult.Error(e.message ?: "خطا در استخراج از دیوار")
        }

        if (items.isEmpty()) {
            return ApiResult.Error("آگهی‌ای یافت نشد")
        }

        val advertiserFilter = safeFilters.advanced.advertiserFilter
        val filteredItems = if (advertiserFilter == "all") {
            items
        } else {
            items.filter { AdvertiserFilter.matches(it.raw, advertiserFilter) }
        }
        if (filteredItems.isEmpty()) {
            return ApiResult.Error("پس از اعمال فیلتر آگهی‌دهنده، آگهی‌ای باقی نماند")
        }

        val finishedAt = Instant.now().toString()
        when (val tokenResult = authRepository.refreshAccessToken()) {
            is ApiResult.Error -> return tokenResult
            is ApiResult.Success -> Unit
        }
        return try {
            val response = api.uploadExtraction(
                ExtractionUploadRequest(
                    filters = ExtractionFiltersDto(
                        cityId = safeFilters.cityId,
                        cityName = safeFilters.cityName,
                        districtIds = safeFilters.districtIds,
                        districtNames = safeFilters.districtNames,
                        provinceName = safeFilters.provinceName,
                        category = safeFilters.category,
                        categoryLabel = safeFilters.categoryLabel,
                        transactionTypeLabel = safeFilters.transactionTypeLabel,
                        outputNameHint = safeFilters.outputNameHint,
                        sort = safeFilters.sort,
                        maxItems = safeFilters.maxItems,
                        priceMin = safeFilters.advanced.priceMin,
                        priceMax = safeFilters.advanced.priceMax,
                        depositMin = safeFilters.advanced.depositMin,
                        depositMax = safeFilters.advanced.depositMax,
                        rentMin = safeFilters.advanced.rentMin,
                        rentMax = safeFilters.advanced.rentMax,
                        areaMin = safeFilters.advanced.areaMin,
                        areaMax = safeFilters.advanced.areaMax,
                        yearMin = safeFilters.advanced.yearMin,
                        yearMax = safeFilters.advanced.yearMax,
                        rooms = safeFilters.advanced.rooms,
                        advertiserFilter = safeFilters.advanced.advertiserFilter,
                    ),
                    startedAt = startedAt,
                    finishedAt = finishedAt,
                    items = filteredItems.map { ExtractionItemDto(it.token, it.raw) },
                ),
            )
            if (!response.ok) {
                ApiResult.Error(response.error ?: "آپلود به سرور ناموفق", response.code)
            } else {
                val data = response.requireData<ExtractionUploadData>(json)
                ApiResult.Success(data)
            }
        } catch (e: Exception) {
            ApiResult.Error(e.toUserMessage("خطای شبکه در آپلود"))
        }
    }

    private fun ExtractGateDenied(message: String) = ExtractGateResult.Denied(message)
}
