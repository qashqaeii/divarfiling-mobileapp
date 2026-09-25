package ir.divarfiling.mobile.feature.ai

import ir.divarfiling.mobile.core.network.SmartCommandExampleDto
import ir.divarfiling.mobile.core.network.SmartCommandExamplesSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Test

class SmartCommandExampleSerializerTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun deserializesLabelTextObjects() {
        val array: JsonArray = buildJsonArray {
            add(
                buildJsonObject {
                    put("label", "تماس با مخاطب")
                    put("text", "فردا ساعت ۱۰ یادم بنداز")
                },
            )
        }
        val decoded = json.decodeFromJsonElement(
            SmartCommandExamplesSerializer,
            array,
        )
        assertEquals(1, decoded.size)
        assertEquals("فردا ساعت ۱۰ یادم بنداز", decoded[0].commandText())
    }

    @Test
    fun deserializesPlainStrings() {
        val array: JsonArray = buildJsonArray {
            add("فقط متن")
        }
        val decoded = json.decodeFromJsonElement(SmartCommandExamplesSerializer, array)
        assertEquals("فقط متن", decoded.single().text)
    }
}
