package ir.divarfiling.mobile.feature.extract.divar

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * استخراج لینک تصاویر از JSON جزئیات دیوار (حداقل تصویر اول برای thumbnail).
 */
object DivarImageExtractor {
    private val divarImageHosts = listOf(
        "divarcdn",
        "webimage.divar",
        "divar.ir",
    )

    fun extractImageUrls(detail: JsonElement): List<String> {
        val urls = linkedSetOf<String>()
        val root = detail.jsonObject

        extractFromImageSection(root, urls)
        if (urls.isEmpty()) {
            extractFromPostRowPreview(root, urls)
        }
        if (urls.isEmpty()) {
            collectUrlsRecursive(root, urls, depth = 0)
        }
        return urls.map { normalizeDivarImageUrl(it) }.filter { it.isNotBlank() }
    }

    fun firstImageUrl(detail: JsonElement): String? = extractImageUrls(detail).firstOrNull()

    fun extractListThumbnail(postRowData: JsonObject): String? {
        val candidates = listOfNotNull(
            postRowData["image_url"]?.jsonPrimitive?.content,
            postRowData["thumbnail"]?.jsonPrimitive?.content,
            postRowData["image"]?.jsonObject?.get("url")?.jsonPrimitive?.content,
            postRowData["image"]?.jsonObject?.get("webp_url")?.jsonPrimitive?.content,
            postRowData["image"]?.jsonObject?.get("thumbnail_url")?.jsonPrimitive?.content,
            postRowData["cover_image"]?.jsonPrimitive?.content,
        )
        return candidates.firstOrNull { isLikelyImageUrl(it) }?.let(::normalizeDivarImageUrl)
    }

    private fun extractFromImageSection(root: JsonObject, urls: LinkedHashSet<String>) {
        val sections = root["sections"]?.jsonArray ?: return
        for (section in sections) {
            val sec = section.jsonObject
            if (sec["section_name"]?.jsonPrimitive?.content != "IMAGE") continue
            val widgets = sec["widgets"]?.jsonArray ?: continue
            for (widget in widgets) {
                val w = widget.jsonObject
                val wtype = w["widget_type"]?.jsonPrimitive?.content.orEmpty()
                if (!wtype.contains("IMAGE", ignoreCase = true) &&
                    !wtype.contains("CAROUSEL", ignoreCase = true) &&
                    !wtype.contains("GALLERY", ignoreCase = true)
                ) {
                    continue
                }
                val data = w["data"]?.jsonObject ?: continue
                data["items"]?.jsonArray?.forEach { item ->
                    addUrlFromObject(item.jsonObject, urls)
                }
                data["images"]?.jsonArray?.forEach { item ->
                    runCatching { item.jsonObject }.getOrNull()?.let { addUrlFromObject(it, urls) }
                        ?: item.jsonPrimitive.content.takeIf(::isLikelyImageUrl)?.let(urls::add)
                }
                listOf("url", "webp_url", "image_url", "thumbnail").forEach { key ->
                    data[key]?.jsonPrimitive?.content?.takeIf(::isLikelyImageUrl)?.let(urls::add)
                }
            }
        }
    }

    private fun extractFromPostRowPreview(root: JsonObject, urls: LinkedHashSet<String>) {
        val preview = root["list_data"]?.jsonObject
            ?: root["preview"]?.jsonObject
            ?: return
        addUrlFromObject(preview, urls)
    }

    private fun addUrlFromObject(obj: JsonObject, urls: LinkedHashSet<String>) {
        obj["image"]?.jsonObject?.let { image ->
            listOf("url", "webp_url", "thumbnail_url", "thumbnail").forEach { key ->
                image[key]?.jsonPrimitive?.content?.takeIf(::isLikelyImageUrl)?.let(urls::add)
            }
        }
        listOf("image_url", "webp_url", "thumbnail_url", "thumbnail", "url").forEach { key ->
            obj[key]?.jsonPrimitive?.content?.takeIf(::isLikelyImageUrl)?.let(urls::add)
        }
    }

    private fun collectUrlsRecursive(
        element: JsonElement,
        urls: LinkedHashSet<String>,
        depth: Int,
    ) {
        if (depth > 12 || urls.size >= 8) return
        when (element) {
            is JsonObject -> {
                element.forEach { (key, value) ->
                    if (key.equals("url", ignoreCase = true) ||
                        key.contains("image", ignoreCase = true) ||
                        key.contains("webp", ignoreCase = true) ||
                        key.contains("thumbnail", ignoreCase = true)
                    ) {
                        value.primitiveContentOrNull()?.takeIf(::isLikelyImageUrl)?.let(urls::add)
                    }
                    collectUrlsRecursive(value, urls, depth + 1)
                }
            }
            else -> Unit
        }
    }

    private fun JsonElement.primitiveContentOrNull(): String? =
        runCatching { jsonPrimitive.content }.getOrNull()

    private fun isLikelyImageUrl(value: String): Boolean {
        val trimmed = value.trim()
        if (!trimmed.startsWith("http")) return false
        val lower = trimmed.lowercase()
        if (divarImageHosts.any { lower.contains(it) }) return true
        return lower.contains("/image") || lower.endsWith(".jpg") || lower.endsWith(".webp") ||
            lower.endsWith(".jpeg") || lower.endsWith(".png")
    }

    private fun normalizeDivarImageUrl(url: String): String {
        val trimmed = url.trim()
        if (trimmed.startsWith("//")) return "https:$trimmed"
        return trimmed
    }
}
