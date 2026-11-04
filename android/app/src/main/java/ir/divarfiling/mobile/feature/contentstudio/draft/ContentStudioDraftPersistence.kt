package ir.divarfiling.mobile.feature.contentstudio.draft

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

object ContentStudioDraftPersistence {
    private const val STUDIO_META_KEY = "studio_native_meta"

    fun mergeSelection(
        existing: JsonElement?,
        templateId: String,
        format: String,
    ): JsonObject {
        val base = (existing as? JsonObject)?.toMutableMap() ?: mutableMapOf()
        base[STUDIO_META_KEY] = buildJsonObject {
            put("template_id", JsonPrimitive(templateId))
            put("format", JsonPrimitive(format))
        }
        return JsonObject(base)
    }

    fun readSelection(existing: JsonElement?): Pair<String?, String?> {
        val meta = (existing as? JsonObject)?.get(STUDIO_META_KEY) as? JsonObject ?: return null to null
        val template = (meta["template_id"] as? JsonPrimitive)?.content
        val format = (meta["format"] as? JsonPrimitive)?.content
        return template to format
    }
}
