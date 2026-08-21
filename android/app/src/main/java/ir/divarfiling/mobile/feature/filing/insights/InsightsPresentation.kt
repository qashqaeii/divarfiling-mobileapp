package ir.divarfiling.mobile.feature.filing.insights

import ir.divarfiling.mobile.core.util.displayText
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

data class InsightKpi(
    val label: String,
    val value: String,
)

data class InsightBlock(
    val title: String,
    val body: String,
    val facts: List<InsightKpi> = emptyList(),
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
            key == "signals" -> {
                val text = when (value) {
                    is JsonArray -> value.map { it.displayText() }.filter { it.isNotBlank() && it != "—" }
                        .joinToString("\n")
                    else -> value.displayText()
                }
                if (text.isNotBlank() && text != "—") {
                    blocks += InsightBlock(title = "سیگنال‌ها", body = text)
                }
            }
            value is JsonObject -> presentNestedObject(labelForInsightKey(key), value)?.let { blocks += it }
                ?: leftover.addAll(flattenUnknown(key, value))
            value is JsonArray -> {
                val presented = presentElementList(value)
                if (presented.isNotEmpty()) {
                    blocks += presented
                } else {
                    leftover += InsightKpi(labelForInsightKey(key), value.displayText())
                }
            }
            else -> {
                val text = value.displayText()
                if (text.isNotBlank() && text != "—") {
                    leftover += InsightKpi(labelForInsightKey(key), text)
                }
            }
        }
    }
    return blocks to leftover
}

fun presentElementList(items: List<JsonElement>): List<InsightBlock> =
    items.mapIndexedNotNull { index, element -> presentListItem(element, index + 1) }

private fun presentListItem(element: JsonElement, index: Int): InsightBlock? {
    val obj = element as? JsonObject ?: return InsightBlock(
        title = "مورد $index",
        body = element.displayText(),
    )
    val title = firstText(obj, "title", "label", "heading", "name") ?: "مورد $index"
    val bodyKeys = listOf("summary", "text", "body", "description", "insight", "reason", "message")
    val body = bodyKeys.mapNotNull { key -> obj[key]?.displayText()?.takeIf { it.isNotBlank() && it != "—" } }
        .joinToString("\n")
    val facts = obj.entries
        .filter { it.key !in bodyKeys && it.key !in setOf("title", "label", "heading", "name", "icon") }
        .mapNotNull { (key, value) ->
            if (value is JsonObject || value is JsonArray) {
                InsightKpi(labelForInsightKey(key), value.displayText()).takeIf { it.value.isNotBlank() && it.value != "—" }
            } else {
                val text = value.displayText()
                if (text.isBlank() || text == "—") null else InsightKpi(labelForInsightKey(key), text)
            }
        }
    if (body.isBlank() && facts.isEmpty()) return null
    return InsightBlock(title = title, body = body, facts = facts)
}

private fun presentNestedObject(title: String, obj: JsonObject): InsightBlock? {
    val rows = obj["rows"]
    if (rows is JsonArray) {
        val facts = rows.mapNotNull { row ->
            val rowObj = row as? JsonObject ?: return@mapNotNull null
            val label = firstText(rowObj, "label", "title") ?: return@mapNotNull null
            val value = firstText(rowObj, "value", "text") ?: rowObj.displayText()
            InsightKpi(label, value)
        }
        val extra = obj.entries
            .filter { it.key != "rows" && it.key != "title" && it.key != "icon" }
            .map { InsightKpi(labelForInsightKey(it.key), it.value.displayText()) }
            .filter { it.value.isNotBlank() && it.value != "—" }
        if (facts.isEmpty() && extra.isEmpty()) return null
        return InsightBlock(
            title = firstText(obj, "title") ?: title,
            body = "",
            facts = facts + extra,
        )
    }
    val nestedTitle = firstText(obj, "title", "label") ?: title
    val body = firstText(obj, "summary", "text", "body", "description")
    val facts = obj.entries
        .filter { it.key !in setOf("title", "label", "summary", "text", "body", "description", "icon") }
        .mapNotNull { (key, value) ->
            val text = if (value is JsonPrimitive) value.displayText() else null
            text?.takeIf { it.isNotBlank() && it != "—" }?.let { InsightKpi(labelForInsightKey(key), it) }
        }
    if (body.isNullOrBlank() && facts.isEmpty()) return null
    return InsightBlock(title = nestedTitle, body = body.orEmpty(), facts = facts)
}

private fun flattenUnknown(parentKey: String, value: JsonElement): List<InsightKpi> =
    listOf(InsightKpi(labelForInsightKey(parentKey), value.displayText()))

private fun firstText(obj: JsonObject, vararg keys: String): String? =
    keys.firstNotNullOfOrNull { key ->
        obj[key]?.displayText()?.takeIf { it.isNotBlank() && it != "—" }
    }
