package ir.divarfiling.mobile.core.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Backend sends `{label, text}` objects (web parity); older payloads may use plain strings.
 */
object SmartCommandExamplesSerializer : KSerializer<List<SmartCommandExampleDto>> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("SmartCommandExamples")

    override fun deserialize(decoder: Decoder): List<SmartCommandExampleDto> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return emptyList()
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonArray -> element.mapNotNull { parseExample(it) }
            else -> emptyList()
        }
    }

    override fun serialize(encoder: Encoder, value: List<SmartCommandExampleDto>) {
        error("Encode not supported")
    }

    private fun parseExample(element: JsonElement): SmartCommandExampleDto? {
        return when (element) {
            is JsonPrimitive -> {
                val text = element.contentOrNull?.trim().orEmpty()
                if (text.isEmpty()) null else SmartCommandExampleDto(text = text)
            }
            is JsonObject -> {
                val label = element["label"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
                val text = element["text"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
                if (label.isEmpty() && text.isEmpty()) null else SmartCommandExampleDto(label = label, text = text)
            }
            else -> null
        }
    }
}
