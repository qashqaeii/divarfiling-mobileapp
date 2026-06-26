package ir.divarfiling.mobile.core.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@kotlinx.serialization.Serializable
data class ApiEnvelope(
    val ok: Boolean,
    val data: JsonElement? = null,
    val error: String? = null,
    val code: String? = null,
    val meta: ApiMeta? = null,
)

inline fun <reified T> ApiEnvelope.parseData(json: Json): T? {
    val element = data ?: return null
    return json.decodeFromJsonElement(element)
}

inline fun <reified T> ApiEnvelope.requireData(json: Json): T {
    return parseData<T>(json) ?: error("Empty API data")
}
