package ir.divarfiling.mobile.feature.filing

import ir.divarfiling.mobile.core.network.DatasetDto
import ir.divarfiling.mobile.feature.extract.ExtractCategories

object DatasetDisplayUtils {

    fun displayTitle(dataset: DatasetDto): String {
        val parts = linkedSetOf<String>()

        dataset.city?.trim()?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
        humanizeSegment(dataset.transactionType)?.let { parts.add(it) }

        val subcategory = humanizeSegment(dataset.subcategory)
        val category = humanizeSegment(dataset.category)
        when {
            !subcategory.isNullOrBlank() -> parts.add(subcategory)
            !category.isNullOrBlank() -> parts.add(category)
        }

        if (parts.isNotEmpty()) return parts.joinToString(" · ")
        return humanizeSegment(dataset.name) ?: dataset.name
    }

    fun humanizeSegment(raw: String?): String? {
        val value = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
        ExtractCategories.labelForSlug(value)?.let { return it }
        if (looksLikeApiSlug(value)) return null
        return value
    }

    private fun looksLikeApiSlug(value: String): Boolean {
        if (!value.contains('-')) return false
        return value.any { it.isLetter() && it.code in 1..127 }
    }
}
