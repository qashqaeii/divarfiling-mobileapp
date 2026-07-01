package ir.divarfiling.mobile.feature.extract.schedule

import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.network.ExtractionFiltersDto
import ir.divarfiling.mobile.core.network.ExtractionScheduleDto

object ScheduleNotificationCopy {
    const val DEEP_LINK = "divarfiling://extract/schedules"
    const val NOTIFICATION_ID_BASE = 9100

    fun createdMessage(schedule: ExtractionScheduleDto): Pair<String, String> {
        val location = locationLabel(schedule.filters, schedule.title)
        val interval = scheduleIntervalLabel(schedule.intervalHours)
        val maxItems = DateUtils.toPersianDigits(schedule.maxItems.toString())
        val title = "زمان‌بندی فعال شد"
        val body = "پایش خودکار استخراج برای $location — $interval، حداکثر $maxItems آگهی — از الان فعال است."
        return title to body
    }

    fun locationLabel(filters: ExtractionFiltersDto, fallbackTitle: String): String {
        val districts = filters.districtNames.filter { it.isNotBlank() }
        val city = filters.cityName?.trim().orEmpty()
        return when {
            districts.isNotEmpty() && city.isNotBlank() ->
                "${districts.joinToString("، ")}، $city"
            districts.isNotEmpty() -> districts.joinToString("، ")
            city.isNotBlank() -> city
            else -> fallbackTitle.trim().ifBlank { "فیلتر ذخیره‌شده" }
        }
    }
}
