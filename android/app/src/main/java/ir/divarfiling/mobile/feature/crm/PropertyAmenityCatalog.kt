package ir.divarfiling.mobile.feature.crm

data class PropertyAmenityGroup(
    val id: String,
    val title: String,
    val labels: List<String>,
)

object PropertyAmenityCatalog {
    private val featureManaged = setOf(
        "بالکن", "استخر", "سونا", "جکوزی", "حیوان خانگی مجاز",
        "بازسازی شده", "قابل معاوضه", "نگهبانی",
    )

    val groups: List<PropertyAmenityGroup> = listOf(
        PropertyAmenityGroup("core", "امکانات اصلی", listOf("پارکینگ", "انباری", "آسانسور")),
        PropertyAmenityGroup(
            "building",
            "امکانات ساختمان",
            listOf("تراس", "لابی", "آیفون تصویری", "درب ضدسرقت", "سرایداری", "آنتن مرکزی"),
        ),
        PropertyAmenityGroup(
            "luxury",
            "رفاهی و لوکس",
            listOf("روف گاردن", "سالن ورزش"),
        ),
        PropertyAmenityGroup(
            "conditions",
            "شرایط و وضعیت",
            listOf("سند", "مجوز ساخت"),
        ),
    )

    val allLabels: Set<String> = groups.flatMap { it.labels }.toSet() + featureManaged

    fun checkboxGroups(): List<PropertyAmenityGroup> = groups

    fun parseAmenityLabels(
        amenitiesText: String,
        hasParking: Boolean,
        hasStorage: Boolean,
        hasElevator: Boolean,
    ): Set<String> {
        val found = mutableSetOf<String>()
        if (hasParking) found.add("پارکینگ")
        if (hasStorage) found.add("انباری")
        if (hasElevator) found.add("آسانسور")
        amenitiesText.split('،', ',', '|', '\n').map { it.trim() }.filter { it.isNotBlank() }.forEach { token ->
            allLabels.firstOrNull { label -> token == label || token.contains(label) }?.let(found::add)
        }
        return found
    }

    fun extractExtraText(amenitiesText: String, selected: Set<String>): String {
        if (amenitiesText.isBlank()) return ""
        val leftovers = amenitiesText.split('،', ',', '|', '\n')
            .map { it.trim() }
            .filter { it.isNotBlank() && selected.none { label -> it == label || it.contains(label) } }
        return leftovers.joinToString("، ")
    }

    fun serializeAmenities(selected: Set<String>, extra: String): String {
        val ordered = groups.flatMap { it.labels }.filter { it in selected }
        val extras = extra.trim()
        return when {
            ordered.isEmpty() && extras.isBlank() -> ""
            extras.isBlank() -> ordered.joinToString("، ")
            ordered.isEmpty() -> extras
            else -> ordered.joinToString("، ") + "، " + extras
        }
    }

    fun syncCoreFlags(selected: Set<String>): Triple<Boolean, Boolean, Boolean> =
        Triple("پارکینگ" in selected, "انباری" in selected, "آسانسور" in selected)
}
