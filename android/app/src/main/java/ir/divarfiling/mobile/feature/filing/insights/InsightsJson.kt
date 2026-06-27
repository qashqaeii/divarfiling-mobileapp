package ir.divarfiling.mobile.feature.filing.insights

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object InsightsJson {
    fun string(obj: JsonElement?, key: String): String? =
        (obj as? JsonObject)?.get(key)?.jsonPrimitive?.contentOrNull

    fun int(obj: JsonElement?, key: String): Int? =
        (obj as? JsonObject)?.get(key)?.jsonPrimitive?.intOrNull

    fun float(obj: JsonElement?, key: String): Float? =
        (obj as? JsonObject)?.get(key)?.jsonPrimitive?.floatOrNull
            ?: (obj as? JsonObject)?.get(key)?.jsonPrimitive?.doubleOrNull?.toFloat()

    fun objectAt(obj: JsonElement?, key: String): JsonElement? =
        (obj as? JsonObject)?.get(key)

    fun array(obj: JsonElement?, key: String): JsonArray? =
        (obj as? JsonObject)?.get(key)?.jsonArray

    fun rows(obj: JsonElement?, key: String): List<JsonObject> =
        array(obj, key)?.mapNotNull { it as? JsonObject }.orEmpty()

    fun stringList(obj: JsonElement?, key: String): List<String> =
        array(obj, key)?.mapNotNull { it.jsonPrimitive.contentOrNull }.orEmpty()

    fun textItems(section: JsonElement?): List<String> {
        val direct = stringList(section, "items")
        if (direct.isNotEmpty()) return direct
        return rows(section, "items").mapNotNull { string(it, "text") ?: string(it, "label") }
    }
}
