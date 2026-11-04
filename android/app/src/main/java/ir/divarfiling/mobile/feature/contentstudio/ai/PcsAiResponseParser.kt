package ir.divarfiling.mobile.feature.contentstudio.ai

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object PcsAiResponseParser {

    fun parseTitleSuggestions(envelope: JsonElement, action: String): List<String> {
        val root = envelope as? JsonObject ?: return emptyList()
        val data = root["data"] as? JsonObject ?: root
        val titles = when (action) {
            "poster-headlines", "divar-titles" -> data["titles"] as? JsonArray
            else -> null
        } ?: return emptyList()
        return titles.mapNotNull { item ->
            when (item) {
                is JsonPrimitive -> item.content.takeIf { it.isNotBlank() }
                is JsonObject -> (item["text"] as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() }
                else -> null
            }
        }
    }

    fun parseDivarBody(envelope: JsonElement): String {
        val root = envelope as? JsonObject ?: return ""
        val data = root["data"] as? JsonObject ?: root
        return (data["body"] as? JsonPrimitive)?.content.orEmpty()
    }

    fun parseSocialPack(envelope: JsonElement): List<String> {
        val root = envelope as? JsonObject ?: return emptyList()
        val data = root["data"] as? JsonObject ?: root
        val out = mutableListOf<String>()
        (data["captions"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.content }?.let { out.addAll(it) }
        (data["whatsapp_short"] as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() }?.let { out.add(it) }
        (data["telegram"] as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() }?.let { out.add(it) }
        return out
    }
}
