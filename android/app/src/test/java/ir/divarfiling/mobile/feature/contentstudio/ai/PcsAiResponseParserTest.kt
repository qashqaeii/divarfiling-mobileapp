package ir.divarfiling.mobile.feature.contentstudio.ai

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class PcsAiResponseParserTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parsesPosterHeadlinesFromNestedDataTitles() {
        val raw = json.parseToJsonElement(
            """
            {"data":{"titles":[{"text":"عنوان ۱","style":"پیشنهادی"},{"text":"عنوان ۲","style":"پیشنهادی"}]}}
            """.trimIndent(),
        )
        val list = PcsAiResponseParser.parseTitleSuggestions(raw, "poster-headlines")
        assertEquals(listOf("عنوان ۱", "عنوان ۲"), list)
    }
}
