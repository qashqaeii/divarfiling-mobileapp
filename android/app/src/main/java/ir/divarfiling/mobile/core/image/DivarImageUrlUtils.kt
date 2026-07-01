package ir.divarfiling.mobile.core.image

object DivarImageUrlUtils {
    private val thumbnailMarkers = listOf("/webp_thumbnail/", "/thumbnail/")

    fun isDivarImageUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("divarcdn") || lower.contains("divar.ir") || lower.contains("webimage.divar")
    }

    fun fileId(url: String): String? {
        val clean = url.substringBefore('?').trimEnd('/')
        val name = clean.substringAfterLast('/', missingDelimiterValue = "")
        return name.takeIf { it.isNotBlank() && it.contains('.') }?.lowercase()
    }

    fun qualityScore(url: String): Int = when {
        url.lowercase().contains("/webp_post/") -> 0
        thumbnailMarkers.any { url.lowercase().contains(it) } -> 2
        else -> 1
    }

    fun deduplicate(urls: List<String>): List<String> {
        if (urls.isEmpty()) return emptyList()

        val bestByFile = linkedMapOf<String, Triple<Int, Int, String>>()
        urls.forEachIndexed { index, raw ->
            val url = ImageUrlFormatter.normalize(raw) ?: return@forEachIndexed
            val fileId = fileId(url)
            if (fileId != null && isDivarImageUrl(url)) {
                val score = qualityScore(url)
                val current = bestByFile[fileId]
                if (current == null || score < current.first || (score == current.first && index < current.second)) {
                    bestByFile[fileId] = Triple(score, index, url)
                }
            }
        }

        val out = mutableListOf<String>()
        val seenFiles = mutableSetOf<String>()
        val seenUrls = mutableSetOf<String>()

        urls.forEach { raw ->
            val url = ImageUrlFormatter.normalize(raw) ?: return@forEach
            val fileId = fileId(url)
            if (fileId != null && isDivarImageUrl(url)) {
                if (!seenFiles.add(fileId)) return@forEach
                val best = bestByFile[fileId]?.third ?: url
                if (seenUrls.add(best)) out.add(best)
            } else if (seenUrls.add(url)) {
                out.add(url)
            }
        }
        return out
    }
}
