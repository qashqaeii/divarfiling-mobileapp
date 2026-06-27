package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.network.ExtractionRunDto
import ir.divarfiling.mobile.core.network.ExtractionRunFailRequest
import ir.divarfiling.mobile.core.network.ExtractionScheduleCreateRequest
import ir.divarfiling.mobile.core.network.ExtractionScheduleDto
import ir.divarfiling.mobile.core.network.ExtractionScheduleUpdateRequest
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.ScheduleRunStartData
import ir.divarfiling.mobile.core.network.requireData
import ir.divarfiling.mobile.feature.extract.divar.ExtractFilters
import ir.divarfiling.mobile.feature.extract.schedule.ExtractScheduleMapper
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtractionScheduleRepository @Inject constructor(
    private val api: MobileApi,
    private val json: Json,
) {
    suspend fun listSchedules(): ApiResult<List<ExtractionScheduleDto>> {
        return try {
            val response = api.getExtractionSchedules()
            if (!response.ok) return ApiResult.Error(response.error ?: "خطا در دریافت زمان‌بندی‌ها")
            val list = response.data?.let {
                json.decodeFromJsonElement(ListSerializer(ExtractionScheduleDto.serializer()), it)
            }.orEmpty()
            ApiResult.Success(list)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun getDueSchedules(): ApiResult<List<ExtractionScheduleDto>> {
        return try {
            val response = api.getDueExtractionSchedules()
            if (!response.ok) return ApiResult.Error(response.error ?: "خطا در دریافت زمان‌بندی‌های سررسید")
            val list = response.data?.let {
                json.decodeFromJsonElement(ListSerializer(ExtractionScheduleDto.serializer()), it)
            }.orEmpty()
            ApiResult.Success(list)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun createSchedule(
        filters: ExtractFilters,
        intervalHours: Double,
        title: String = "",
    ): ApiResult<ExtractionScheduleDto> {
        return try {
            val response = api.createExtractionSchedule(
                ExtractionScheduleCreateRequest(
                    title = title,
                    intervalHours = intervalHours,
                    filters = ExtractScheduleMapper.toFiltersDto(filters),
                    maxItems = filters.maxItems,
                    isEnabled = true,
                ),
            )
            if (!response.ok) return ApiResult.Error(response.error ?: "ایجاد زمان‌بندی ناموفق")
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun toggleSchedule(scheduleId: Long): ApiResult<ExtractionScheduleDto> {
        return try {
            val response = api.toggleExtractionSchedule(scheduleId)
            if (!response.ok) return ApiResult.Error(response.error ?: "تغییر وضعیت ناموفق")
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun deleteSchedule(scheduleId: Long): ApiResult<Unit> {
        return try {
            val response = api.deleteExtractionSchedule(scheduleId)
            if (!response.ok) return ApiResult.Error(response.error ?: "حذف ناموفق")
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun updateInterval(scheduleId: Long, intervalHours: Double): ApiResult<ExtractionScheduleDto> {
        return try {
            val response = api.updateExtractionSchedule(
                scheduleId,
                ExtractionScheduleUpdateRequest(intervalHours = intervalHours),
            )
            if (!response.ok) return ApiResult.Error(response.error ?: "ویرایش ناموفق")
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun listRuns(scheduleId: Long): ApiResult<List<ExtractionRunDto>> {
        return try {
            val response = api.getExtractionScheduleRuns(scheduleId)
            if (!response.ok) return ApiResult.Error(response.error ?: "خطا در تاریخچه")
            val list = response.data?.let {
                json.decodeFromJsonElement(ListSerializer(ExtractionRunDto.serializer()), it)
            }.orEmpty()
            ApiResult.Success(list)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun startRun(scheduleId: Long): ApiResult<ScheduleRunStartData> {
        return try {
            val response = api.startExtractionScheduleRun(scheduleId)
            if (!response.ok) return ApiResult.Error(response.error ?: "شروع اجرا ناموفق")
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun failRun(runId: Long, error: String): ApiResult<Unit> {
        return try {
            val response = api.failExtractionRun(runId, ExtractionRunFailRequest(error))
            if (!response.ok) return ApiResult.Error(response.error ?: "ثبت خطا ناموفق")
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }
}
