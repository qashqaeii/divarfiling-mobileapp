package ir.divarfiling.mobile.feature.extract.divar

data class ExtractAdvancedFilters(
    val priceMin: Long? = null,
    val priceMax: Long? = null,
    val depositMin: Long? = null,
    val depositMax: Long? = null,
    val rentMin: Long? = null,
    val rentMax: Long? = null,
    val areaMin: Int? = null,
    val areaMax: Int? = null,
    val yearMin: Int? = null,
    val yearMax: Int? = null,
    val rooms: List<String> = emptyList(),
    val advertiserFilter: String = "all",
)

data class ExtractFilters(
    val cityId: String = "1",
    val cityName: String = "تهران",
    val provinceName: String? = null,
    val districtIds: List<String> = emptyList(),
    val districtNames: List<String> = emptyList(),
    val districtSlugs: List<String> = emptyList(),
    val citySlug: String? = null,
    val category: String = "apartment-rent",
    val categoryLabel: String? = null,
    val transactionTypeLabel: String? = null,
    val sort: String = "sort_date",
    val maxItems: Int = 50,
    val outputNameHint: String? = null,
    val searchQuery: String? = null,
    val advanced: ExtractAdvancedFilters = ExtractAdvancedFilters(),
)
