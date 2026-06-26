package ir.divarfiling.mobile.feature.extract.divar

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object OutputNameHint {

    fun build(filters: ExtractFilters): String {
        val areaSlug = makeSlug(
            filters.districtNames.firstOrNull() ?: filters.cityName,
        )
        val ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        return "${areaSlug}_$ts"
    }

    private fun makeSlug(text: String): String {
        var s = text.trim().lowercase()
        s = s.replace(" ", "-")
        s = s.replace(Regex("[^a-z0-9\\-_]+"), "-")
        s = s.replace(Regex("-+"), "-").trim('-')
        return s.ifBlank { "unknown" }
    }
}
