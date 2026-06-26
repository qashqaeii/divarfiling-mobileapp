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
    val rooms: String = "",
)

data class ExtractFilters(
    val cityId: String = "1",
    val cityName: String = "تهران",
    val districtIds: List<String> = emptyList(),
    val districtNames: List<String> = emptyList(),
    val category: String = "apartment-rent",
    val sort: String = "sort_date",
    val maxItems: Int = 50,
    val advanced: ExtractAdvancedFilters = ExtractAdvancedFilters(),
    /** all | personal — فیلتر پس از استخراج در سرور */
    val advertiserFilter: String = "all",
)
