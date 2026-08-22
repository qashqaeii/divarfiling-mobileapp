package ir.divarfiling.mobile.feature.filing.insights

import ir.divarfiling.mobile.core.util.displayText
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

data class InsightKpi(
    val label: String,
    val value: String,
)

enum class InsightAmenityState { YES, NO, UNKNOWN }

data class InsightAmenityChip(
    val label: String,
    val state: InsightAmenityState,
    val isLuxury: Boolean = false,
)

enum class InsightBlockKind { GENERIC, OPPORTUNITY, SIGNALS }

data class InsightBlock(
    val title: String,
    val subtitle: String = "",
    val body: String = "",
    val facts: List<InsightKpi> = emptyList(),
    val amenities: List<InsightAmenityChip> = emptyList(),
    val amenityTierLabel: String? = null,
    val badge: String? = null,
    val stars: String? = null,
    val rank: Int? = null,
    val priceLine: String? = null,
    val kind: InsightBlockKind = InsightBlockKind.GENERIC,
)

private val knownLabels = mapOf(
    "district" to "منطقه",
    "city" to "شهر",
    "title" to "عنوان",
    "label" to "عنوان",
    "value" to "مقدار",
    "summary" to "خلاصه",
    "text" to "متن",
    "body" to "شرح",
    "description" to "توضیح",
    "insight" to "بینش",
    "reason" to "دلیل",
    "count" to "تعداد",
    "median" to "میانه",
    "mean" to "میانگین",
    "avg" to "میانگین",
    "average" to "میانگین",
    "min" to "حداقل",
    "max" to "حداکثر",
    "p25" to "چارک اول",
    "p75" to "چارک سوم",
    "price" to "قیمت",
    "rent" to "اجاره",
    "deposit" to "رهن",
    "area" to "متراژ",
    "listings" to "تعداد فایل",
    "row_count" to "تعداد ردیف",
    "clean_count" to "فایل تمیز",
    "geo_count" to "دارای موقعیت",
    "transaction_type" to "نوع معامله",
    "market_sentiment" to "حال بازار",
    "market_value" to "ارزش بازار",
    "property_basis" to "مبنای ملک",
    "ready_numbers" to "اعداد آماده",
    "signals" to "سیگنال‌ها",
    "confidence" to "اطمینان",
    "score" to "امتیاز",
    "pct" to "درصد",
    "percent" to "درصد",
    "expected_price_fmt" to "ارزش تخمینی",
    "expected_range_fmt" to "بازه ارزش",
    "opportunity_score" to "امتیاز فرصت",
    "value_diff_pct" to "زیر بازار",
    "price_fmt" to "قیمت",
    "rent_fmt" to "اجاره",
    "deposit_fmt" to "رهن",
    "full_deposit_fmt" to "رهن کامل",
    "rent_equiv_fmt" to "معادل رهن کامل",
    "price_per_sqm_fmt" to "قیمت هر متر",
    "area_fmt" to "متراژ",
    "valuation_peers" to "فایل مشابه",
    "amenity_score" to "امتیاز امکانات",
    "amenity_tier" to "سطح امکانات",
    "neighborhood" to "محله",
    "headline" to "توضیح",
    "segment_hint" to "بخش پرتقاضا",
    "amenity_note" to "یادداشت امکانات",
    "note" to "نکته",
)

private val hiddenOpportunityKeys = setOf(
    "listing_id",
    "title",
    "label",
    "heading",
    "name",
    "icon",
    "l2",
    "l3",
    "stars",
    "rank",
    "reasons",
    "amenities",
    "amenity_tier",
    "amenity_score",
    "value_diff_pct",
    "value_score",
    "neighborhood",
    "area_fmt",
    "price_fmt",
    "rent_fmt",
    "deposit_fmt",
    "full_deposit_fmt",
    "rent_equiv_fmt",
    "price_per_sqm_fmt",
    "valuation_basis",
    "expected_price_fmt",
    "expected_range_fmt",
    "opportunity_score",
    "valuation_peers",
)

fun labelForInsightKey(key: String): String =
    knownLabels[key] ?: knownLabels[key.lowercase()] ?: humanizeKey(key)

private fun humanizeKey(key: String): String =
    key.replace('_', ' ').replace('-', ' ').trim()

fun presentSnapshot(snapshot: JsonObject): Pair<List<InsightBlock>, List<InsightKpi>> {
    val blocks = mutableListOf<InsightBlock>()
    val leftover = mutableListOf<InsightKpi>()
    snapshot.forEach { (key, value) ->
        when {
            key == "signals" -> presentSignals(value)?.let { blocks += it }
            value is JsonObject -> presentNestedObject(labelForInsightKey(key), value)?.let { blocks += it }
                ?: leftover.addAll(flattenUnknown(key, value))
            value is JsonArray -> {
                val presented = presentElementList(value)
                if (presented.isNotEmpty()) {
                    blocks += presented
                } else {
                    val text = presentStringList(value)
                    if (text.isNotBlank()) {
                        leftover += InsightKpi(labelForInsightKey(key), text)
                    }
                }
            }
            else -> {
                val text = value.displayText()
                if (text.isNotBlank() && text != "—") {
                    leftover += InsightKpi(labelForInsightKey(key), formatDisplayValue(text))
                }
            }
        }
    }
    return blocks to leftover
}

fun presentElementList(items: List<JsonElement>): List<InsightBlock> =
    items.mapIndexedNotNull { index, element -> presentListItem(element, index + 1) }

private fun presentSignals(value: JsonElement): InsightBlock? {
    val facts = when (value) {
        is JsonArray -> value.mapNotNull { presentSignalItem(it) }
        else -> listOfNotNull(presentSignalItem(value))
    }
    if (facts.isEmpty()) return null
    return InsightBlock(
        title = "سیگنال‌ها",
        facts = facts,
        kind = InsightBlockKind.SIGNALS,
    )
}

private fun presentSignalItem(element: JsonElement): InsightKpi? {
    val obj = element as? JsonObject
    if (obj != null) {
        val label = firstText(obj, "label") ?: return null
        val valueText = firstText(obj, "value") ?: return null
        return InsightKpi(label, formatDisplayValue(valueText))
    }
    val text = element.displayText().trim()
    if (text.isBlank() || text == "—") return null
    return InsightKpi("سیگنال", formatDisplayValue(text))
}

private fun presentListItem(element: JsonElement, index: Int): InsightBlock? {
    if (element is JsonPrimitive) {
        val text = element.contentOrNull?.trim().orEmpty()
        if (text.isBlank()) return null
        return InsightBlock(title = "نکته $index", body = text)
    }
    if (element is JsonObject) {
        return if (isOpportunityObject(element)) {
            presentOpportunity(element, index)
        } else {
            presentGenericObject(element, index)
        }
    }
    if (element is JsonArray) {
        val text = presentStringList(element)
        if (text.isBlank()) return null
        return InsightBlock(title = "مورد $index", body = text)
    }
    val text = element.displayText().trim()
    if (text.isBlank() || text == "—") return null
    return InsightBlock(title = "مورد $index", body = text)
}

private fun presentGenericObject(obj: JsonObject, index: Int): InsightBlock? {
    val title = firstText(obj, "title", "label", "heading", "name") ?: "مورد $index"
    val bodyKeys = listOf("summary", "text", "body", "description", "insight", "reason", "message", "headline", "reasons")
    val body = buildBody(obj, bodyKeys)
    val facts = obj.entries
        .filter { it.key !in bodyKeys && it.key !in setOf("title", "label", "heading", "name", "icon", "facts", "amenities") }
        .mapNotNull { (key, value) -> primitiveFact(key, value) }
    if (body.isBlank() && facts.isEmpty()) return null
    return InsightBlock(title = title, body = body, facts = facts)
}

private fun isOpportunityObject(obj: JsonObject): Boolean =
    obj.containsKey("opportunity_score") ||
        obj.containsKey("listing_id") ||
        obj.containsKey("value_diff_pct") ||
        obj.containsKey("expected_price_fmt")

private fun presentOpportunity(obj: JsonObject, index: Int): InsightBlock? {
    val areaFmt = firstText(obj, "area_fmt")
    val titleText = firstText(obj, "title")
    val title = when {
        !areaFmt.isNullOrBlank() -> areaFmt
        !titleText.isNullOrBlank() -> titleText
        else -> "فرصت #$index"
    }
    val subtitle = firstText(obj, "neighborhood").orEmpty()
    val body = presentReasonsBody(obj["reasons"])
    val badge = formatUnderMarketBadge(obj["value_diff_pct"])
    val stars = firstText(obj, "stars")
    val rank = intValue(obj["rank"])
    val amenities = parseAmenityChips(obj["amenities"])
    val amenityTierLabel = amenityTierLabel(firstText(obj, "amenity_tier"))
    val priceLine = buildOpportunityPriceLine(obj)
    val facts = buildOpportunityFacts(obj)

    if (body.isBlank() && facts.isEmpty() && priceLine.isNullOrBlank() && amenities.isEmpty()) {
        return null
    }

    return InsightBlock(
        title = title,
        subtitle = subtitle,
        body = body,
        facts = facts,
        amenities = amenities,
        amenityTierLabel = amenityTierLabel,
        badge = badge,
        stars = stars,
        rank = rank,
        priceLine = priceLine,
        kind = InsightBlockKind.OPPORTUNITY,
    )
}

private fun buildOpportunityFacts(obj: JsonObject): List<InsightKpi> {
    val facts = mutableListOf<InsightKpi>()
    firstText(obj, "expected_price_fmt")?.let { facts += InsightKpi("ارزش تخمینی بازار", it) }
    firstText(obj, "expected_range_fmt")?.let { facts += InsightKpi("بازه ارزش", it) }
    intValue(obj["opportunity_score"])?.let { facts += InsightKpi("امتیاز فرصت", it.toString()) }
    intValue(obj["valuation_peers"])?.let { facts += InsightKpi("مقایسه با", "$it فایل مشابه") }
    intValue(obj["amenity_score"])?.let { facts += InsightKpi("امتیاز امکانات", it.toString()) }
    firstText(obj, "label")?.let { facts += InsightKpi("وضعیت", it) }

    val basisLines = presentStringList(obj["valuation_basis"])
    if (basisLines.isNotBlank()) {
        facts += InsightKpi("مبنای ارزش‌گذاری", basisLines)
    }

    obj.entries
        .filter { (key, _) -> key !in hiddenOpportunityKeys }
        .forEach { (key, value) ->
            primitiveFact(key, value)?.let { facts += it }
        }

    return facts.distinctBy { it.label }
}

private fun buildOpportunityPriceLine(obj: JsonObject): String? {
    val deposit = firstText(obj, "deposit_fmt")
    val rent = firstText(obj, "rent_fmt")
    if (!deposit.isNullOrBlank() && !rent.isNullOrBlank()) {
        return "$deposit رهن + $rent اجاره"
    }
    firstText(obj, "rent_equiv_fmt")?.let { return it }
    firstText(obj, "full_deposit_fmt")?.let { return "رهن کامل $it" }
    val price = firstText(obj, "price_fmt")
    val perSqm = firstText(obj, "price_per_sqm_fmt")
    return when {
        !price.isNullOrBlank() && !perSqm.isNullOrBlank() -> "$price · متری $perSqm"
        !price.isNullOrBlank() -> price
        else -> null
    }
}

private fun presentNestedObject(title: String, obj: JsonObject): InsightBlock? {
    val rows = obj["rows"]
    if (rows is JsonArray) {
        val facts = rows.mapNotNull { row ->
            val rowObj = row as? JsonObject ?: return@mapNotNull null
            val label = firstText(rowObj, "label", "title") ?: return@mapNotNull null
            val value = firstText(rowObj, "value", "text") ?: return@mapNotNull null
            InsightKpi(label, formatDisplayValue(value))
        }
        if (facts.isEmpty()) return null
        return InsightBlock(
            title = firstText(obj, "title") ?: title,
            body = firstText(obj, "headline").orEmpty(),
            facts = facts,
        )
    }

    val factsArray = obj["facts"]
    val factItems = if (factsArray is JsonArray) {
        factsArray.mapNotNull { item ->
            val itemObj = item as? JsonObject ?: return@mapNotNull null
            val label = firstText(itemObj, "label", "title") ?: return@mapNotNull null
            val value = firstText(itemObj, "value", "text") ?: return@mapNotNull null
            InsightKpi(label, formatDisplayValue(value))
        }
    } else {
        emptyList()
    }

    val amenities = parseAmenityChips(obj["amenities"])
    val nestedTitle = firstText(obj, "title", "label") ?: title
    val bodyParts = listOfNotNull(
        firstText(obj, "headline"),
        firstText(obj, "segment_hint"),
        firstText(obj, "amenity_note"),
        firstText(obj, "note"),
        firstText(obj, "summary", "text", "body", "description"),
    ).filter { it.isNotBlank() }

    val primitiveFacts = obj.entries
        .filter { it.key !in setOf("title", "label", "summary", "text", "body", "description", "icon", "rows", "facts", "amenities", "headline", "segment_hint", "amenity_note", "note") }
        .mapNotNull { (key, value) -> primitiveFact(key, value) }

    val allFacts = factItems + primitiveFacts
    val body = bodyParts.joinToString("\n\n")
    if (body.isBlank() && allFacts.isEmpty() && amenities.isEmpty()) return null

    return InsightBlock(
        title = nestedTitle,
        body = body,
        facts = allFacts,
        amenities = amenities,
    )
}

private fun parseAmenityChips(value: JsonElement?): List<InsightAmenityChip> {
    val array = value as? JsonArray ?: return emptyList()
    return array.mapNotNull { item ->
        val obj = item as? JsonObject ?: return@mapNotNull null
        val label = firstText(obj, "label") ?: return@mapNotNull null
        val state = when (firstText(obj, "state")?.lowercase()) {
            "yes" -> InsightAmenityState.YES
            "no" -> InsightAmenityState.NO
            else -> InsightAmenityState.UNKNOWN
        }
        val tier = firstText(obj, "tier")?.lowercase()
        InsightAmenityChip(label = label, state = state, isLuxury = tier == "secondary")
    }
}

private fun presentReasonsBody(value: JsonElement?): String {
    val array = value as? JsonArray ?: return ""
    return array.mapNotNull { (it as? JsonPrimitive)?.contentOrNull?.trim()?.takeIf { text -> text.isNotBlank() } }
        .joinToString("\n") { "• $it" }
}

private fun presentStringList(value: JsonElement?): String {
    val array = value as? JsonArray ?: return value?.displayText()?.takeIf { it != "—" }.orEmpty()
    return array.mapNotNull { element ->
        when (element) {
            is JsonPrimitive -> element.contentOrNull?.trim()?.takeIf { it.isNotBlank() }
            is JsonObject -> firstText(element, "label", "text", "value", "title")
            else -> element.displayText().takeIf { it.isNotBlank() && it != "—" }
        }
    }.joinToString("\n")
}

private fun primitiveFact(key: String, value: JsonElement): InsightKpi? {
    if (value is JsonObject || value is JsonArray) return null
    val text = formatDisplayValue(value.displayText())
    if (text.isBlank() || text == "—") return null
    return InsightKpi(labelForInsightKey(key), text)
}

private fun buildBody(obj: JsonObject, bodyKeys: List<String>): String {
    val parts = bodyKeys.mapNotNull { key ->
        val element = obj[key] ?: return@mapNotNull null
        when (element) {
            is JsonArray -> when (key) {
                "reasons" -> presentReasonsBody(element).takeIf { it.isNotBlank() }
                else -> presentStringList(element).takeIf { it.isNotBlank() }
            }
            else -> firstText(obj, key)
        }
    }
    return parts.filter { it.isNotBlank() }.joinToString("\n\n")
}

private fun flattenUnknown(parentKey: String, value: JsonElement): List<InsightKpi> {
    if (value is JsonObject && value.containsKey("facts")) {
        presentNestedObject(labelForInsightKey(parentKey), value)?.facts?.let { return it }
    }
    val text = when (value) {
        is JsonObject, is JsonArray -> return emptyList()
        else -> formatDisplayValue(value.displayText())
    }
    if (text.isBlank() || text == "—") return emptyList()
    return listOf(InsightKpi(labelForInsightKey(parentKey), text))
}

private fun formatUnderMarketBadge(value: JsonElement?): String? {
    val number = doubleValue(value) ?: return null
    if (number <= 0) return null
    val formatted = if (number % 1.0 == 0.0) number.toInt().toString() else number.toString()
    return "$formatted٪ زیر بازار"
}

private fun amenityTierLabel(tier: String?): String? = when (tier?.lowercase()) {
    "full" -> "فول امکانات"
    "bare" -> "بدون امکانات پایه"
    else -> null
}

private fun formatDisplayValue(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.endsWith("-") && trimmed.dropLast(1).toDoubleOrNull() != null) {
        return "-${trimmed.dropLast(1)}"
    }
    return trimmed
}

private fun firstText(obj: JsonObject, vararg keys: String): String? =
    keys.firstNotNullOfOrNull { key ->
        obj[key]?.let { element ->
            when (element) {
                is JsonPrimitive -> element.contentOrNull?.trim()?.takeIf { it.isNotBlank() && it != "—" }
                is JsonArray -> presentStringList(element).takeIf { it.isNotBlank() }
                else -> null
            }
        }?.let(::formatDisplayValue)
    }

private fun intValue(value: JsonElement?): Int? = when (value) {
    is JsonPrimitive -> value.contentOrNull?.toDoubleOrNull()?.toInt()
    else -> null
}

private fun doubleValue(value: JsonElement?): Double? = when (value) {
    is JsonPrimitive -> value.contentOrNull?.toDoubleOrNull()
    else -> null
}
