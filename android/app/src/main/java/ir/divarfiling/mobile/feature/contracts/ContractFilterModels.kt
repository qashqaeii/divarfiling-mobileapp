package ir.divarfiling.mobile.feature.contracts

data class ContractSheetFilters(
    val contractType: String? = null,
    val dealLinked: String? = null,
    val dateFrom: String = "",
    val dateTo: String = "",
    val sort: String = "updated",
) {
    fun activeCount(): Int {
        var n = 0
        if (!contractType.isNullOrBlank()) n++
        if (!dealLinked.isNullOrBlank()) n++
        if (dateFrom.isNotBlank()) n++
        if (dateTo.isNotBlank()) n++
        if (sort.isNotBlank() && sort != "updated") n++
        return n
    }
}

enum class ContractSortOption(val apiValue: String, val label: String) {
    UPDATED("updated", "آخرین تغییر"),
    CREATED("created", "جدیدترین"),
    NEEDS_ACTION("needs_action", "نیازمند اقدام"),
}
