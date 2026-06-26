package ir.divarfiling.mobile.feature.extract.divar

object OutputNameHint {

    /** نام فایل بدون تاریخ — فقط اسلاگ منطقه/شهر (هم‌تراز با نام‌گذاری میزکار). */
    fun build(filters: ExtractFilters): String {
        val areaSlug = filters.districtSlugs.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: filters.citySlug?.takeIf { it.isNotBlank() }
            ?: makeSlug(filters.districtNames.firstOrNull() ?: filters.cityName)
        return areaSlug
    }

    private fun makeSlug(text: String): String {
        var s = text.trim().lowercase()
        s = s.replace(" ", "-")
        s = s.replace(Regex("[^a-z0-9\\-_]+"), "-")
        s = s.replace(Regex("-+"), "-").trim('-')
        return s.ifBlank { "unknown" }
    }
}
