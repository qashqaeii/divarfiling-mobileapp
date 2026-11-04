package ir.divarfiling.mobile.feature.contentstudio.draft

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneDocument
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ContentStudioDraftPersistenceTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun readSelectionUsesFormatAndEditorTemplateId() {
        val draft = buildJsonObject {
            put("format", "post")
            put(
                "editor",
                buildJsonObject {
                    put("open", true)
                    put("template_id", "pearl_gallery")
                },
            )
        }
        val (template, format) = ContentStudioDraftPersistence.readSelection(draft)
        assertEquals("pearl_gallery", template)
        assertEquals("post", format)
    }

    @Test
    fun buildSelectionPatchDoesNotIncludeStudioNativeMeta() {
        val patch = ContentStudioDraftPersistence.buildSelectionPatch("pearlescent", "story")
        assertFalse(patch.containsKey("studio_native_meta"))
        assertEquals("story", (patch["format"] as JsonPrimitive).content)
    }

    @Test
    fun readPosterSceneReturnsNullWhenMissing() {
        assertNull(ContentStudioDraftPersistence.readPosterScene(null, "pearlescent", json))
    }

    @Test
    fun buildPosterPatchIncludesSceneWhenProvided() {
        val scene = PcsSceneDocument(canvas = ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneCanvas(1080, 1920))
        val patch = ContentStudioDraftPersistence.buildPosterPatch("pearlescent", "story", "سلام", scene, json)
        val posters = patch["posters"] as kotlinx.serialization.json.JsonObject
        val conf = posters["pearlescent"] as kotlinx.serialization.json.JsonObject
        assertEquals("سلام", conf["headline"].toString().trim('"'))
        assertEquals(true, conf.containsKey("scene"))
    }
}
