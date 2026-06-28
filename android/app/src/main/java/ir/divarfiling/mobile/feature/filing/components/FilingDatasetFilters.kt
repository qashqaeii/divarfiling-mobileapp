package ir.divarfiling.mobile.feature.filing.components

import ir.divarfiling.mobile.core.network.DatasetDto
import java.time.LocalDate
import java.time.format.DateTimeParseException

object FilingDatasetFilters {
    const val ALL_FORMATS = "همه فرمت‌ها"
    const val ALL_CITIES = "همه شهرها"
    const val ALL_TRANSACTIONS = "همه معاملات"

    fun filterDatasets(
        datasets: List<DatasetDto>,
        categoryTabId: String,
        favoriteIds: Set<String>,
        formatFilter: String,
        cityFilter: String,
        transactionFilter: String,
        localQuery: String,
    ): List<DatasetDto> {
        return datasets.filter { dataset ->
            matchesCategory(dataset, categoryTabId, favoriteIds) &&
                matchesFormat(dataset, formatFilter) &&
                matchesCity(dataset, cityFilter) &&
                matchesTransaction(dataset, transactionFilter) &&
                matchesQuery(dataset, localQuery)
        }
    }

    fun uniqueFormats(datasets: List<DatasetDto>): List<String> =
        listOf(ALL_FORMATS) + datasets.mapNotNull { it.fileFormat?.uppercase() }.distinct().sorted()

    fun uniqueCities(datasets: List<DatasetDto>): List<String> =
        listOf(ALL_CITIES) + datasets.mapNotNull { it.city?.takeIf { city -> city.isNotBlank() } }.distinct().sorted()

    fun uniqueTransactions(datasets: List<DatasetDto>): List<String> =
        listOf(ALL_TRANSACTIONS) + datasets.mapNotNull { it.transactionType?.takeIf { tx -> tx.isNotBlank() } }.distinct()

    fun totalAds(datasets: List<DatasetDto>): Int = datasets.sumOf { it.itemCount }

    fun estimatedSizeGb(totalAds: Int): Double =
        (totalAds * 0.12).coerceAtLeast(0.1) / 1024.0

    fun datasetsThisMonth(datasets: List<DatasetDto>): Int {
        val prefix = LocalDate.now().toString().substring(0, 7)
        return datasets.count { dataset ->
            dataset.createdAt?.startsWith(prefix) == true ||
                parseMonth(dataset.createdAt) == prefix
        }
    }

    private fun parseMonth(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalDate.parse(value.substring(0, 10)).toString().substring(0, 7)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun matchesCategory(dataset: DatasetDto, tabId: String, favoriteIds: Set<String>): Boolean =
        when (tabId) {
            "residential" -> dataset.transactionType?.contains("مسکونی") == true
            "commercial" -> dataset.transactionType?.contains("تجاری") == true ||
                dataset.transactionType?.contains("اداری") == true
            "land" -> dataset.subcategory?.contains("زمین") == true ||
                dataset.subcategory?.contains("ویلا") == true ||
                dataset.name.contains("ویلا") ||
                dataset.name.contains("زمین")
            "favorites" -> favoriteIds.contains(dataset.id)
            else -> true
        }

    private fun matchesFormat(dataset: DatasetDto, filter: String): Boolean =
        filter == ALL_FORMATS || dataset.fileFormat.equals(filter, ignoreCase = true)

    private fun matchesCity(dataset: DatasetDto, filter: String): Boolean =
        filter == ALL_CITIES || dataset.city == filter

    private fun matchesTransaction(dataset: DatasetDto, filter: String): Boolean =
        filter == ALL_TRANSACTIONS || dataset.transactionType == filter

    private fun matchesQuery(dataset: DatasetDto, query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim()
        return dataset.name.contains(q, ignoreCase = true) ||
            dataset.city?.contains(q, ignoreCase = true) == true ||
            dataset.district?.contains(q, ignoreCase = true) == true ||
            dataset.originalFilename?.contains(q, ignoreCase = true) == true
    }
}
