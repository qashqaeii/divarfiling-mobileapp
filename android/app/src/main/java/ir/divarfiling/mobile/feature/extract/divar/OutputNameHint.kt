package ir.divarfiling.mobile.feature.extract.divar

object OutputNameHint {

    /** نام فایل بدون تاریخ — اسلاگ منطقه + دسته تا هر زیردسته فایلینگ جدا بسازد. */
    fun build(filters: ExtractFilters): String {
        val areaSlug = filters.districtSlugs.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: filters.citySlug?.takeIf { it.isNotBlank() }
            ?: makeSlug(filters.districtNames.firstOrNull() ?: filters.cityName)
        val category = filters.category.trim().takeIf { it.isNotBlank() } ?: "unknown"
        return "${areaSlug}_$category"
    }

    private fun makeSlug(text: String): String {
        var s = text.trim().lowercase()
        s = s.replace(" ", "-")
        s = s.replace(Regex("[^a-z0-9\\-_]+"), "-")
        s = s.replace(Regex("-+"), "-").trim('-')
        return s.ifBlank { "unknown" }
    }
}
