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
    fun `extracts image url from carousel with thumbnail_url`() {
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
                          {
                            "image_url": "",
                            "image": {
                              "url": "https://postimage01.divarcdn.com/static/photo/neda/webp_post/zXBkW8ICNLRYcdh_T4pifQ/becf62b6-1b74-4669-92cd-9577deb4b2e4.webp",
                              "thumbnail_url": "https://postimage01.divarcdn.com/static/photo/neda/webp_thumbnail/grv07UPd6lSlAD3kgTpp1Q/becf62b6-1b74-4669-92cd-9577deb4b2e4.webp"
                            }
                          }
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
        assertEquals(
            "https://postimage01.divarcdn.com/static/photo/neda/webp_post/zXBkW8ICNLRYcdh_T4pifQ/becf62b6-1b74-4669-92cd-9577deb4b2e4.webp",
            urls.first(),
        )
        assertEquals(1, urls.size)
    }

    @Test
    fun `returns empty when no media`() {
        val detail = buildJsonObject { put("sections", json.parseToJsonElement("[]")) }
        assertTrue(DivarImageExtractor.extractImageUrls(detail).isEmpty())
    }
}
