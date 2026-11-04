package ir.divarfiling.mobile.core.filing

import ir.divarfiling.mobile.core.network.ListingDetailDto

object ListingLocationUtils {

    data class LocationParts(
        val city: String = "",
        val region: String = "",
        val neighborhood: String = "",
        val street: String = "",
        val address: String = "",
        val summary: String = "",
    )

    fun parts(listing: ListingDetailDto): LocationParts {
        val summary = listing.locationSummary.orEmpty().trim()
        val street = listing.street.orEmpty().trim()
        val city = listing.city.orEmpty().trim()
        val region = listing.region.orEmpty().trim()
        val neighborhood = listing.neighborhood.orEmpty().trim()
            .ifBlank { listing.district.orEmpty().trim() }
        val dedupedNeighborhood = neighborhood.takeUnless { labelsEqual(it, region) }.orEmpty()
        return LocationParts(
            city = city,
            region = region,
            neighborhood = dedupedNeighborhood,
            street = street,
            address = listing.address.orEmpty().trim(),
            summary = summary,
        )
    }

    fun headline(parts: LocationParts): String {
        if (parts.summary.isNotBlank()) return parts.summary
        return buildList {
            add(parts.street)
            add(parts.neighborhood)
            add(parts.region)
            add(parts.city)
        }
            .filter { it.isNotBlank() }
            .distinctBy { normalizeLabel(it) }
            .joinToString(" · ")
    }

    fun dossierRows(parts: LocationParts): List<Pair<String, String>> = buildList {
        parts.city.takeIf { it.isNotBlank() }?.let { add("شهر" to it) }
        parts.region.takeIf { it.isNotBlank() }?.let { add("منطقه" to it) }
        parts.neighborhood.takeIf { it.isNotBlank() }?.let { add("محله" to it) }
        parts.street.takeIf { it.isNotBlank() }?.let { add("خیابان" to it) }
        parts.address.takeIf { it.isNotBlank() }?.let { add("آدرس" to it) }
    }

    fun hasLocationContent(parts: LocationParts): Boolean =
        headline(parts).isNotBlank() || dossierRows(parts).isNotEmpty()

    private fun labelsEqual(a: String, b: String): Boolean {
        val left = normalizeLabel(a)
        val right = normalizeLabel(b)
        return left.isNotBlank() && left == right
    }

    private fun normalizeLabel(value: String): String =
        value.trim()
            .replace('\u200c', ' ')
            .replace(Regex("\\s+"), " ")
            .lowercase()
}
