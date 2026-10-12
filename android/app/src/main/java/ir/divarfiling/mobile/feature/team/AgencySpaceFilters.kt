package ir.divarfiling.mobile.feature.team

data class AgencySpaceFilters(
    val ordering: String = "newest",
    val dealMode: String = "",
    val propertyType: String = "",
    val city: String = "",
    val district: String = "",
    val minPrice: String = "",
    val maxPrice: String = "",
    val minArea: String = "",
    val maxArea: String = "",
    val rooms: String = "",
    val customerType: String = "",
    val region: String = "",
    val minBudget: String = "",
    val maxBudget: String = "",
    val visibility: String = "",
    val crmCopy: String = "",
    val contactShareLevel: String = "",
    val publisherId: Long? = null,
    val publishedAfter: String = "",
    val publishedBefore: String = "",
) {
    fun activeCount(): Int {
        var count = 0
        if (ordering != "newest") count++
        if (dealMode.isNotBlank()) count++
        if (propertyType.isNotBlank()) count++
        if (city.isNotBlank()) count++
        if (district.isNotBlank()) count++
        if (minPrice.isNotBlank()) count++
        if (maxPrice.isNotBlank()) count++
        if (minArea.isNotBlank()) count++
        if (maxArea.isNotBlank()) count++
        if (rooms.isNotBlank()) count++
        if (customerType.isNotBlank()) count++
        if (region.isNotBlank()) count++
        if (minBudget.isNotBlank()) count++
        if (maxBudget.isNotBlank()) count++
        if (visibility.isNotBlank()) count++
        if (crmCopy.isNotBlank()) count++
        if (contactShareLevel.isNotBlank()) count++
        if (publisherId != null) count++
        if (publishedAfter.isNotBlank()) count++
        if (publishedBefore.isNotBlank()) count++
        return count
    }

    fun appendQueryParams(
        tab: String,
        into: MutableMap<String, String>,
    ) {
        if (ordering.isNotBlank() && ordering != "newest") into["ordering"] = ordering
        if (dealMode.isNotBlank()) into["deal_mode"] = dealMode.trim()
        if (propertyType.isNotBlank()) into["property_type"] = propertyType.trim()
        if (city.isNotBlank()) into["city"] = city.trim()
        if (district.isNotBlank()) into["district"] = district.trim()
        if (minPrice.isNotBlank()) into["min_price"] = minPrice.trim()
        if (maxPrice.isNotBlank()) into["max_price"] = maxPrice.trim()
        if (minArea.isNotBlank()) into["min_area"] = minArea.trim()
        if (maxArea.isNotBlank()) into["max_area"] = maxArea.trim()
        if (rooms.isNotBlank()) into["rooms"] = rooms.trim()
        if (customerType.isNotBlank()) into["customer_type"] = customerType.trim()
        if (region.isNotBlank()) into["region"] = region.trim()
        if (minBudget.isNotBlank()) into["min_budget"] = minBudget.trim()
        if (maxBudget.isNotBlank()) into["max_budget"] = maxBudget.trim()
        if (visibility.isNotBlank()) into["visibility"] = visibility.trim()
        if (crmCopy.isNotBlank()) into["crm_copy"] = crmCopy.trim()
        if (contactShareLevel.isNotBlank()) into["contact_share_level"] = contactShareLevel.trim()
        if (publisherId != null) into["publisher"] = publisherId.toString()
        if (publishedAfter.isNotBlank()) into["published_after"] = publishedAfter.trim()
        if (publishedBefore.isNotBlank()) into["published_before"] = publishedBefore.trim()
        val propertyTab = tab == AgencySpaceTab.PROPERTIES || tab == AgencySpaceTab.ALL
        val contactTab = tab == AgencySpaceTab.CONTACTS || tab == AgencySpaceTab.ALL
        if (!propertyTab) {
            into.remove("deal_mode")
            into.remove("property_type")
            into.remove("min_price")
            into.remove("max_price")
            into.remove("min_area")
            into.remove("max_area")
            into.remove("rooms")
            into.remove("city")
            into.remove("district")
        }
        if (!contactTab) {
            into.remove("customer_type")
            into.remove("region")
            into.remove("min_budget")
            into.remove("max_budget")
            into.remove("contact_share_level")
        }
    }
}
