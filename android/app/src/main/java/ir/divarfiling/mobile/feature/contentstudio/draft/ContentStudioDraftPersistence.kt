package ir.divarfiling.mobile.feature.contentstudio.draft

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneCanvas
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

    private val FORMATS = listOf("story", "post", "square")

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

    fun readPosterScene(
        draft: JsonElement?,
        templateId: String,
        format: String,
        json: Json,
    ): PcsSceneDocument? {
        val conf = posterConfig(draft, templateId) ?: return null
        val scenes = conf["scenes"] as? JsonObject
        val fromScenes = scenes?.get(format)
        if (fromScenes != null) {
            return decodeScene(fromScenes, json)
        }
        val legacy = conf["scene"] ?: return null
        val decoded = decodeScene(legacy, json) ?: return null
        val legacyFormat = decoded.canvas?.format ?: (legacy as? JsonObject)?.let {
            (it["canvas"] as? JsonObject)?.get("format") as? JsonPrimitive
        }?.content
        return decoded.takeIf { legacyFormat == null || legacyFormat == format }
    }

    fun readPosterScenesMap(
        draft: JsonElement?,
        templateId: String,
        json: Json,
    ): Map<String, PcsSceneDocument> {
        val conf = posterConfig(draft, templateId) ?: return emptyMap()
        val out = mutableMapOf<String, PcsSceneDocument>()
        val scenes = conf["scenes"] as? JsonObject
        if (scenes != null) {
            for (fmt in FORMATS) {
                scenes[fmt]?.let { el -> decodeScene(el, json)?.let { out[fmt] = it } }
            }
        }
        decodeScene(conf["scene"], json)?.let { scene ->
            val fmt = scene.canvas?.format ?: "story"
            if (fmt !in out) out[fmt] = scene
        }
        return out
    }

    private fun posterConfig(draft: JsonElement?, templateId: String): JsonObject? {
        val posters = (draft as? JsonObject)?.get("posters") as? JsonObject ?: return null
        return posters[templateId] as? JsonObject
    }

    private fun decodeScene(element: JsonElement?, json: Json): PcsSceneDocument? {
        if (element == null) return null
        return runCatching { json.decodeFromJsonElement(PcsSceneDocument.serializer(), element) }.getOrNull()
    }

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

    fun buildPosterPatch(
        templateId: String,
        format: String,
        headline: String,
        scene: PcsSceneDocument?,
        existingDraft: JsonElement?,
        json: Json,
    ): JsonObject {
        val scenesMap = readPosterScenesMap(existingDraft, templateId, json).toMutableMap()
        if (scene != null) {
            scenesMap[format] = scene.copy(
                canvas = scene.canvas?.copy(format = format)
                    ?: PcsSceneCanvas(
                        width = scene.canvas?.width ?: 1080,
                        height = scene.canvas?.height ?: 1920,
                        format = format,
                    ),
            )
        }
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
                            if (scenesMap.isNotEmpty()) {
                                put(
                                    "scenes",
                                    buildJsonObject {
                                        scenesMap.forEach { (fmt, doc) ->
                                            put(fmt, json.encodeToJsonElement(doc))
                                        }
                                    },
                                )
                            }
                            scenesMap[format]?.let { active ->
                                put("scene", json.encodeToJsonElement(active))
                            }
                        },
                    )
                },
            )
        }
    }
}
