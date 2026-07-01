package ir.divarfiling.mobile.feature.crm.components

import ir.divarfiling.mobile.core.filing.ListingSpecUtils
import ir.divarfiling.mobile.core.network.ListingFeatureProfileDto
import ir.divarfiling.mobile.core.network.PropertyDto

object PropertyAmenityResolver {

    fun effectiveParking(property: PropertyDto, profile: ListingFeatureProfileDto?): Boolean? =
        resolve(property.hasParking, profile, "پارکینگ", "parking")

    fun effectiveStorage(property: PropertyDto, profile: ListingFeatureProfileDto?): Boolean? =
        resolve(property.hasStorage, profile, "انباری", "storage")

    fun effectiveElevator(property: PropertyDto, profile: ListingFeatureProfileDto?): Boolean? =
        resolve(property.hasElevator, profile, "آسانسور", "elevator")

    fun label(value: Boolean?): String = ListingSpecUtils.boolFeatureLabel(value)

    private fun resolve(
        stored: Boolean,
        profile: ListingFeatureProfileDto?,
        vararg keys: String,
    ): Boolean? {
        if (stored) return true
        profile?.core?.forEach { item ->
            val label = item.label.orEmpty()
            val key = item.key.orEmpty()
            if (keys.any { it == label || it == key }) {
                return parseProfileBool(item.value, item.state)
            }
        }
        profile?.groups?.forEach { group ->
            group.items.forEach { item ->
                val label = item.label.orEmpty()
                val key = item.key.orEmpty()
                if (keys.any { it == label || it == key }) {
                    return parseProfileBool(item.value, item.state)
                }
            }
        }
        return if (stored) true else false
    }

    private fun parseProfileBool(value: String?, state: String?): Boolean? {
        when (state?.lowercase()) {
            "yes" -> return true
            "no" -> return false
        }
        val text = value?.trim().orEmpty()
        if (text.isBlank() || text == "—" || text == "-") return null
        return when {
            text.contains("ندارد", ignoreCase = true) ||
                text.equals("خیر", ignoreCase = true) ||
                text.equals("no", ignoreCase = true) -> false
            text.contains("دارد", ignoreCase = true) ||
                text.equals("بله", ignoreCase = true) ||
                text.equals("yes", ignoreCase = true) -> true
            else -> null
        }
    }
}
