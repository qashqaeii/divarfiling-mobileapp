package ir.divarfiling.mobile.core.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

fun JsonElement.displayText(): String = when (this) {
    is JsonPrimitive -> contentOrNull ?: toString()
    is JsonArray -> joinToString("، ") { it.displayText() }.ifBlank { "—" }
    is JsonObject -> entries.joinToString(" · ") { "${it.key}: ${it.value.displayText()}" }.ifBlank { "—" }
    else -> toString()
}

fun Map<String, JsonElement>.displayEntries(): List<Pair<String, String>> =
    entries.map { it.key to it.value.displayText() }
