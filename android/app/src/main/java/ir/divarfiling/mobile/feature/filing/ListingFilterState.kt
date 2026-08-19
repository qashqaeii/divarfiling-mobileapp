package ir.divarfiling.mobile.feature.filing

/**
 * حالت فیلتر حرفه‌ای فایلینگ — هم‌راستا با query params موبایل/وب.
 */
data class ListingFilterState(
    val priceMin: Long? = null,
    val priceMax: Long? = null,
    val areaMin: Int? = null,
    val areaMax: Int? = null,
    val rooms: String = "",
    val neighborhood: String = "",
    val yearMin: Int? = null,
    val yearMax: Int? = null,
    val parking: String = "", // "" | "1" | "0"
    val elevator: String = "",
    val storage: String = "",
    val consultant: String = "", // "" | "1" | "0" | genuine_personal | disguised_consultant
    val value: String = "", // "" | below | fair | above
    val unique: Boolean = false,
    val newOnly: Boolean = false,
    val sort: String = "",
) {
    fun activeCount(includeSort: Boolean = false): Int {
        var n = 0
        if (priceMin != null) n++
        if (priceMax != null) n++
        if (areaMin != null) n++
        if (areaMax != null) n++
        if (rooms.isNotBlank()) n++
        if (neighborhood.isNotBlank()) n++
        if (yearMin != null) n++
        if (yearMax != null) n++
        if (parking.isNotBlank()) n++
        if (elevator.isNotBlank()) n++
        if (storage.isNotBlank()) n++
        if (consultant.isNotBlank()) n++
        if (value.isNotBlank()) n++
        if (unique) n++
        if (newOnly) n++
        if (includeSort && sort.isNotBlank()) n++
        return n
    }

    fun toQueryMap(datasetId: String? = null): Map<String, String> {
        val out = linkedMapOf<String, String>()
        priceMin?.let { out["price_min"] = it.toString() }
        priceMax?.let { out["price_max"] = it.toString() }
        areaMin?.let { out["area_min"] = it.toString() }
        areaMax?.let { out["area_max"] = it.toString() }
        if (rooms.isNotBlank()) out["rooms"] = rooms.trim()
        if (neighborhood.isNotBlank()) out["neighborhood"] = neighborhood.trim()
        yearMin?.let { out["year_min"] = it.toString() }
        yearMax?.let { out["year_max"] = it.toString() }
        if (parking.isNotBlank()) out["parking"] = parking
        if (elevator.isNotBlank()) out["elevator"] = elevator
        if (storage.isNotBlank()) out["storage"] = storage
        if (consultant.isNotBlank()) out["consultant"] = consultant
        if (value.isNotBlank()) out["value"] = value
        if (unique) out["unique"] = "1"
        if (newOnly) out["new_only"] = "1"
        if (sort.isNotBlank()) out["sort"] = sort
        if (!datasetId.isNullOrBlank()) out["dataset_id"] = datasetId
        return out
    }

    companion object {
        fun fromParams(params: Map<String, String>): ListingFilterState = ListingFilterState(
            priceMin = params["price_min"]?.toLongOrNull(),
            priceMax = params["price_max"]?.toLongOrNull(),
            areaMin = params["area_min"]?.toIntOrNull(),
            areaMax = params["area_max"]?.toIntOrNull(),
            rooms = params["rooms"].orEmpty(),
            neighborhood = params["neighborhood"].orEmpty(),
            yearMin = params["year_min"]?.toIntOrNull(),
            yearMax = params["year_max"]?.toIntOrNull(),
            parking = params["parking"].orEmpty(),
            elevator = params["elevator"].orEmpty(),
            storage = params["storage"].orEmpty(),
            consultant = when (val raw = params["consultant"].orEmpty()) {
                "disguised" -> "disguised_consultant"
                else -> raw
            },
            value = params["value"].orEmpty(),
            unique = params["unique"] == "1",
            newOnly = params["new_only"] == "1",
            sort = params["sort"].orEmpty(),
        )
    }
}

val ListingSortOptions = listOf(
    "" to "پیش‌فرض",
    "-price" to "گران‌ترین",
    "price" to "ارزان‌ترین",
    "-area" to "بزرگ‌ترین",
    "area" to "کوچک‌ترین",
    "-created_at" to "جدیدترین",
    "created_at" to "قدیمی‌ترین",
    "-deposit" to "ودیعه بیشتر",
    "deposit" to "ودیعه کمتر",
)

val ListingAdvertiserOptions = listOf(
    "" to "همه",
    "genuine_personal" to "مالک واقعی",
    "0" to "شخصی",
    "1" to "مشاور",
    "disguised_consultant" to "مشاور پنهان",
)

val ListingValueOptions = listOf(
    "" to "همه",
    "below" to "زیر ارزش",
    "fair" to "منصفانه",
    "above" to "بالای ارزش",
)

val ListingTriStateOptions = listOf(
    "" to "مهم نیست",
    "1" to "دارد",
    "0" to "ندارد",
)
