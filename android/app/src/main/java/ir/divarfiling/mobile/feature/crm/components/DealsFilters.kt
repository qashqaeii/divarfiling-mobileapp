package ir.divarfiling.mobile.feature.crm.components

import ir.divarfiling.mobile.core.design.DateUtils

import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.network.DealDto
import ir.divarfiling.mobile.core.network.DealPipelineColumnDto

enum class DealsViewMode { Grid, List }

enum class DealsSortOrder { Newest, Oldest }

object DealsFilters {
    const val ALL_FILTERS = "همه فیلترها"
    const val NEWEST = "جدیدترین"
    const val OLDEST = "قدیمی‌تر"
    const val ALL_OWNERS = "همه مالکین"

    fun filterAndSortDeals(
        deals: List<DealDto>,
        ownerFilter: String,
        sortOrder: DealsSortOrder,
        localQuery: String,
    ): List<DealDto> {
        return deals
            .filter { matchesOwner(it, ownerFilter) }
            .filter { matchesQuery(it, localQuery) }
            .let { sorted ->
                when (sortOrder) {
                    DealsSortOrder.Newest -> sorted.sortedByDescending { it.updatedAt.orEmpty() }
                    DealsSortOrder.Oldest -> sorted.sortedBy { it.updatedAt.orEmpty() }
                }
            }
    }

    fun uniqueOwners(deals: List<DealDto>): List<String> {
        val owners = deals.mapNotNull { it.customerName?.takeIf { name -> name.isNotBlank() } }.distinct().sorted()
        return listOf(ALL_OWNERS) + owners
    }

    fun activeCount(deals: List<DealDto>): Int =
        deals.count { !isClosed(it.stage) && !isLost(it.stage) }

    fun pipelineValue(deals: List<DealDto>, columns: List<DealPipelineColumnDto>): Long {
        val fromColumns = columns.sumOf { it.totalValue }
        if (fromColumns > 0) return fromColumns
        return deals.sumOf { it.amount ?: 0L }
    }

    fun weightedForecast(deals: List<DealDto>): Long =
        deals.sumOf { deal ->
            val amount = deal.amount ?: 0L
            val probability = deal.probability ?: defaultProbability(deal.stage)
            (amount * probability / 100.0).toLong()
        }

    fun closedCommission(deals: List<DealDto>): Long =
        deals.filter { isClosed(it.stage) }.sumOf { it.commissionAmount ?: 0L }

    fun closingRate(deals: List<DealDto>): Int {
        if (deals.isEmpty()) return 0
        val closed = deals.count { isClosed(it.stage) }
        return ((closed.toDouble() / deals.size) * 100).toInt()
    }

    fun progressPercent(deal: DealDto): Int =
        deal.probability ?: defaultProbability(deal.stage)

    fun formatCompactToman(value: Long): String =
        FormatUtils.formatPriceShort(value) + " تومان"

    fun customerInitials(name: String?): String {
        if (name.isNullOrBlank()) return "؟"
        val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}"
            parts.size == 1 -> parts[0].take(2)
            else -> "؟"
        }
    }

    fun splitDateTime(updatedAt: String?): Pair<String, String> {
        if (updatedAt.isNullOrBlank()) return "—" to "—"
        val formatted = DateUtils.formatJalaliDateTime(updatedAt)
        if (formatted != null) {
            val parts = formatted.split(' ')
            return parts.firstOrNull().orEmpty() to parts.drop(1).joinToString(" ").ifBlank { "—" }
        }
        return "—" to "—"
    }

    private fun matchesOwner(deal: DealDto, filter: String): Boolean =
        filter == ALL_OWNERS || deal.customerName == filter

    private fun matchesQuery(deal: DealDto, query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim()
        return deal.title.contains(q, ignoreCase = true) ||
            deal.customerName?.contains(q, ignoreCase = true) == true ||
            deal.propertyTitle?.contains(q, ignoreCase = true) == true ||
            deal.stage?.contains(q, ignoreCase = true) == true
    }

    private fun isClosed(stage: String?): Boolean =
        stage?.contains("بسته") == true || stage?.contains("قرارداد") == true

    private fun isLost(stage: String?): Boolean =
        stage?.contains("از دست") == true || stage?.contains("سرد") == true

    private fun defaultProbability(stage: String?): Int = when {
        isLost(stage) -> 0
        isClosed(stage) -> 100
        stage?.contains("قرارداد") == true -> 95
        stage?.contains("پیش") == true -> 80
        stage?.contains("بازدید") == true -> 60
        stage?.contains("مذاکره") == true -> 40
        else -> 20
    }
}
