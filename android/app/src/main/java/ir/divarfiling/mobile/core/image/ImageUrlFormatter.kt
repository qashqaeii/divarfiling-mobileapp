package ir.divarfiling.mobile.core.image

object ImageUrlFormatter {
    fun normalize(raw: String?): String? {
        val value = raw?.trim().orEmpty()
        if (value.isBlank()) return null
        return when {
            value.startsWith("//") -> "https:$value"
            value.startsWith("http://") || value.startsWith("https://") -> value
            else -> "https://${value.trimStart('/')}"
        }
    }

    fun firstOf(vararg candidates: String?): String? {
        candidates.forEach { candidate ->
            normalize(candidate)?.let { return it }
        }
        return null
    }
}
