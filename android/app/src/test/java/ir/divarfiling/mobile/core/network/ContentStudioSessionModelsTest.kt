package ir.divarfiling.mobile.core.network

import org.junit.Assert.assertEquals
import org.junit.Test

class ContentStudioSessionModelsTest {

    @Test
    fun templatesCatalog_deserializesFromSnakeCaseKey() {
        val json = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
        }
        val raw = """
            {
              "templates_catalog": [
                {"template_id": "pearlescent", "label": "Pearlescent", "manifest_version": "pcs-manifest-abc"}
              ],
              "can_edit_property": true
            }
        """.trimIndent()
        val data = json.decodeFromString(ContentStudioSessionData.serializer(), raw)
        assertEquals(1, data.templatesCatalog?.size)
        assertEquals("pearlescent", data.templatesCatalog?.first()?.templateId)
    }
}
