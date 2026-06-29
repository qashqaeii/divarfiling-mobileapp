package ir.divarfiling.mobile.feature.extract.divar

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DivarImageExtractorTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `extracts first image from IMAGE carousel`() {
        val detail = json.parseToJsonElement(
            """
            {
              "sections": [
                {
                  "section_name": "IMAGE",
                  "widgets": [
                    {
                      "widget_type": "IMAGE_CAROUSEL",
                      "data": {
                        "items": [
                          { "image": { "url": "https://s100.divarcdn.com/static/a.webp" } },
                          { "image": { "url": "https://s100.divarcdn.com/static/b.webp" } }
                        ]
                      }
                    }
                  ]
                }
              ]
            }
            """.trimIndent(),
        )
        val urls = DivarImageExtractor.extractImageUrls(detail)
        assertEquals("https://s100.divarcdn.com/static/a.webp", urls.first())
        assertEquals(2, urls.size)
    }

    @Test
    fun `extracts list thumbnail from post row data`() {
        val data = buildJsonObject {
            put("image_url", "https://s100.divarcdn.com/static/list-thumb.webp")
        }
        val thumb = DivarImageExtractor.extractListThumbnail(data)
        assertEquals("https://s100.divarcdn.com/static/list-thumb.webp", thumb)
    }

    @Test
    fun `firstImageUrl returns null when no media`() {
        val detail = buildJsonObject { put("sections", json.parseToJsonElement("[]")) }
        assertTrue(DivarImageExtractor.extractImageUrls(detail).isEmpty())
    }
}
