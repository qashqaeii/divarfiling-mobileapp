package ir.divarfiling.mobile.feature.extract.schedule

import ir.divarfiling.mobile.core.network.ExtractionFiltersDto
import ir.divarfiling.mobile.feature.extract.divar.ExtractAdvancedFilters
import ir.divarfiling.mobile.feature.extract.divar.ExtractFilters

object ExtractScheduleMapper {
    fun toExtractFilters(dto: ExtractionFiltersDto): ExtractFilters {
        return ExtractFilters(
            cityId = dto.cityId,
            cityName = dto.cityName ?: "تهران",
            provinceName = dto.provinceName,
            districtIds = dto.districtIds,
            districtNames = dto.districtNames,
            category = dto.category,
            categoryLabel = dto.categoryLabel,
            transactionTypeLabel = dto.transactionTypeLabel,
            sort = dto.sort,
            maxItems = dto.maxItems,
            outputNameHint = dto.outputNameHint,
            searchQuery = dto.searchQuery?.trim()?.takeIf { it.isNotEmpty() },
            advanced = ExtractAdvancedFilters(
                priceMin = dto.priceMin,
                priceMax = dto.priceMax,
                depositMin = dto.depositMin,
                depositMax = dto.depositMax,
                rentMin = dto.rentMin,
                rentMax = dto.rentMax,
                areaMin = dto.areaMin,
                areaMax = dto.areaMax,
                yearMin = dto.yearMin,
                yearMax = dto.yearMax,
                rooms = dto.rooms,
                advertiserFilter = dto.advertiserFilter,
            ),
        )
    }

    fun toFiltersDto(filters: ExtractFilters): ExtractionFiltersDto {
        return ExtractionFiltersDto(
            cityId = filters.cityId,
            cityName = filters.cityName,
            districtIds = filters.districtIds,
            districtNames = filters.districtNames,
            provinceName = filters.provinceName,
            category = filters.category,
            categoryLabel = filters.categoryLabel,
            transactionTypeLabel = filters.transactionTypeLabel,
            outputNameHint = filters.outputNameHint,
            sort = filters.sort,
            maxItems = filters.maxItems,
            searchQuery = filters.searchQuery?.trim()?.takeIf { it.isNotEmpty() },
            priceMin = filters.advanced.priceMin,
            priceMax = filters.advanced.priceMax,
            depositMin = filters.advanced.depositMin,
            depositMax = filters.advanced.depositMax,
            rentMin = filters.advanced.rentMin,
            rentMax = filters.advanced.rentMax,
            areaMin = filters.advanced.areaMin,
            areaMax = filters.advanced.areaMax,
            yearMin = filters.advanced.yearMin,
            yearMax = filters.advanced.yearMax,
            rooms = filters.advanced.rooms,
            advertiserFilter = filters.advanced.advertiserFilter,
            sourceClient = "android_schedule",
        )
    }
}
