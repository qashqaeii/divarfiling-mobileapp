package ir.divarfiling.mobile.feature.contentstudio.draft

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneDocument
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

/**
 * Draft patches aligned with Django `draft_validate.py` (no `studio_native_meta`).
 */
object ContentStudioDraftPersistence {

    fun readSelection(draft: JsonElement?): Pair<String?, String?> {
        val root = draft as? JsonObject ?: return null to null
        val format = (root["format"] as? JsonPrimitive)?.content
        val editor = root["editor"] as? JsonObject
        val templateId = (editor?.get("template_id") as? JsonPrimitive)?.content
        return templateId to format
    }

    fun readPosterHeadline(draft: JsonElement?, templateId: String): String {
        val posters = (draft as? JsonObject)?.get("posters") as? JsonObject ?: return ""
        val conf = posters[templateId] as? JsonObject ?: return ""
        return (conf["headline"] as? JsonPrimitive)?.content.orEmpty()
    }

    fun readPosterScene(draft: JsonElement?, templateId: String, json: Json): PcsSceneDocument? {
        val posters = (draft as? JsonObject)?.get("posters") as? JsonObject ?: return null
        val conf = posters[templateId] as? JsonObject ?: return null
        val sceneEl = conf["scene"] ?: return null
        return runCatching { json.decodeFromJsonElement(PcsSceneDocument.serializer(), sceneEl) }.getOrNull()
    }

    /** Minimal patch for template/format selection only. */
    fun buildSelectionPatch(templateId: String, format: String): JsonObject {
        return buildJsonObject {
            put("format", format)
            put(
                "editor",
                buildJsonObject {
                    put("open", true)
                    put("template_id", templateId)
                },
            )
        }
    }

    /** Patch poster headline + optional scene for the active template. */
    fun buildPosterPatch(
        templateId: String,
        format: String,
        headline: String,
        scene: PcsSceneDocument?,
        json: Json,
    ): JsonObject {
        return buildJsonObject {
            put("format", format)
            put(
                "editor",
                buildJsonObject {
                    put("open", true)
                    put("template_id", templateId)
                },
            )
            put(
                "posters",
                buildJsonObject {
                    put(
                        templateId,
                        buildJsonObject {
                            if (headline.isNotBlank()) {
                                put("headline", headline)
                            }
                            if (scene != null) {
                                put("scene", json.encodeToJsonElement(scene))
                            }
                        },
                    )
                },
            )
        }
    }
}
