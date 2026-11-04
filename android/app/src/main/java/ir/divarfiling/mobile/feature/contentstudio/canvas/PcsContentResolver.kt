package ir.divarfiling.mobile.feature.contentstudio.canvas

/**
 * Native resolver engines — parity with pcs-template-scenes.js (no layer.id guessing).
 */
object PcsContentResolver {

    fun resolveLayerText(
        layer: PcsLayer,
        property: PcsStudioProperty,
        posterConfHeadline: String = "",
        brandFooter: String = "",
    ): String {
        val contract = layer.contentContract?.resolver
            ?: inferResolverFromBinding(layer)
        val binding = layer.binding ?: layer.contentContract?.defaultBinding
        val mode = binding?.mode ?: "linked"

        if (mode == "detached") {
            return layer.text.orEmpty()
        }
        if (mode == "override") {
            return layer.text?.takeIf { it.isNotBlank() }
                ?: resolveLinked(contract, binding, property, posterConfHeadline, brandFooter)
        }
        return resolveLinked(contract, binding, property, posterConfHeadline, brandFooter)
    }

    private fun inferResolverFromBinding(layer: PcsLayer): String? {
        val field = layer.binding?.field.orEmpty()
        return when (field) {
            "headline" -> "poster_headline"
            "location_line" -> "property_location_line"
            "price" -> "property_price"
            "deal_mode" -> "property_deal_badge"
            "contact_footer" -> "brand_contact_footer"
            else -> null
        }
    }

    private fun resolveLinked(
        resolver: String?,
        binding: PcsBinding?,
        property: PcsStudioProperty,
        posterConfHeadline: String,
        brandFooter: String,
    ): String {
        val key = resolver ?: when (binding?.field) {
            "headline" -> "poster_headline"
            "location_line" -> "property_location_line"
            "price" -> "property_price"
            "deal_mode" -> "property_deal_badge"
            "contact_footer" -> "brand_contact_footer"
            else -> null
        }
        val headline = posterHeadline(property, posterConfHeadline)
        return when (key) {
            "poster_headline" -> headline
            "overlay_meta_summary" -> overlayMetaSummary(property, headline)
            "pearlescent_meta_lines" -> pearlescentMetaLines(property, headline)
            "property_deal_badge" -> posterDealBadgeLabel(property.dealMode)
            "property_price" -> propertyPriceLabel(property)
            "brand_contact_footer" -> brandFooter
            "property_location_line" -> property.locationLine.ifBlank { property.city }
            else -> layerFallback(binding, property, brandFooter)
        }
    }

    private fun layerFallback(binding: PcsBinding?, property: PcsStudioProperty, brandFooter: String): String {
        return when (binding?.field) {
            "price" -> propertyPriceLabel(property)
            "deal_mode" -> posterDealBadgeLabel(property.dealMode)
            "location_line" -> property.locationLine
            "contact_footer" -> brandFooter
            "headline" -> property.headline.ifBlank { property.title }
            else -> ""
        }
    }

    fun posterHeadline(property: PcsStudioProperty, posterConfHeadline: String = ""): String {
        if (posterConfHeadline.isNotBlank()) return posterConfHeadline
        if (property.headline.isNotBlank()) return property.headline
        return property.title.ifBlank { property.propertyType.ifBlank { "ملک" } }
    }

    fun overlayMetaSummary(property: PcsStudioProperty, headline: String): String {
        val loc = property.locationLine.ifBlank { property.city }.trim()
        val parts = mutableListOf<String>()
        if (loc.isNotEmpty()) parts.add(loc)
        if (property.propertyType.isNotBlank() && !headlineInText(headline, property.propertyType)) {
            parts.add(property.propertyType)
        }
        if (property.areaLabel.isNotBlank() && !headlineInText(headline, property.areaLabel)) {
            parts.add(property.areaLabel)
        }
        if (property.floorLabel.isNotBlank() && !headlineInText(headline, property.floorLabel) && parts.size < 3) {
            parts.add(property.floorLabel)
        }
        for (chip in property.detailChips) {
            val t = chip.trim()
            if (t.isEmpty() || headlineInText(headline, t)) continue
            if (parts.size >= 4) break
            parts.add(t)
        }
        return parts.filter { it.isNotBlank() }.take(4).joinToString(" · ")
    }

    fun pearlescentMetaLines(property: PcsStudioProperty, headline: String): String {
        val skip = setOf(
            "location_line", "price", "deal_mode", "file_code", "area_label", "rooms_label",
        )
        val lines = mutableListOf<String>()
        for (f in property.editorFeatures) {
            if (f.key.isBlank() || skip.contains(f.key)) continue
            val value = f.value.trim()
            if (value.isEmpty() || headlineInText(headline, value)) continue
            val label = f.label.trim()
            lines.add(if (label.isNotEmpty()) "$label : $value" else value)
        }
        if (lines.isNotEmpty()) return lines.take(6).joinToString("\n")
        val fallback = mutableListOf<String>()
        if (property.propertyType.isNotBlank() && !headlineInText(headline, property.propertyType)) {
            fallback.add("نوع ملک : ${property.propertyType}")
        }
        if (property.floorLabel.isNotBlank() && !headlineInText(headline, property.floorLabel)) {
            fallback.add("طبقه : ${property.floorLabel}")
        }
        if (property.buildYearLabel.isNotBlank() && !headlineInText(headline, property.buildYearLabel)) {
            fallback.add("سال ساخت : ${property.buildYearLabel}")
        }
        for (chip in property.detailChips.take(4)) {
            val t = chip.trim()
            if (t.isNotEmpty() && !headlineInText(headline, t)) fallback.add(t)
        }
        return fallback.take(6).joinToString("\n")
    }

    fun headlineInText(headline: String, fragment: String): Boolean {
        val h = headline.trim().lowercase()
        val f = fragment.trim().lowercase()
        if (h.isEmpty() || f.isEmpty()) return false
        return h.contains(f) || f.contains(h)
    }

    fun posterDealBadgeLabel(dealMode: String): String {
        val mode = dealMode.trim()
        if (mode.isEmpty()) return ""
        return when (mode) {
            "فروش" -> "آگهی فروش"
            "رهن و اجاره" -> "آگهی رهن و اجاره"
            "اجاره" -> "آگهی اجاره"
            "پیش‌فروش" -> "آگهی پیش‌فروش"
            else -> if (mode.startsWith("آگهی")) mode else "آگهی $mode"
        }
    }

    fun propertyPriceLabel(property: PcsStudioProperty): String {
        val price = property.price ?: return ""
        return if (price.show) price.label else ""
    }
}
