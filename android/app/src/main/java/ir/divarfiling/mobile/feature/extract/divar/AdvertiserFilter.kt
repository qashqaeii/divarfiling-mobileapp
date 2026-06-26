package ir.divarfiling.mobile.feature.extract.divar

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Client-side advertiser filter — mirrors Windows `_filter_by_advertiser_type`.
 */
object AdvertiserFilter {

    private val consultantTypes = setOf(
        "premium-panel", "premium_panel", "business", "consultant", "agency",
        "بنگاه", "مشاور", "آژانس",
    )

    fun matches(raw: JsonElement, filter: String): Boolean {
        val mode = filter.trim().lowercase()
        if (mode == "all" || mode.isBlank()) return true

        val obj = raw.jsonObject
        val businessType = obj["webengage"]?.jsonObject
            ?.get("business_type")?.jsonPrimitive?.content.orEmpty().lowercase()
        val consultantName = extractConsultantName(obj)

        val isConsultant = businessType in consultantTypes
            || "premium" in businessType
            || consultantName.isNotBlank()

        return when (mode) {
            "personal" -> !isConsultant && (
                businessType == "personal" || (businessType.isBlank() && consultantName.isBlank())
            )
            "consultant" -> isConsultant
            else -> true
        }
    }

    private fun extractConsultantName(detail: JsonObject): String {
        val widgets = detail["business_lazy_widget_list"]
        if (widgets != null) {
            val fromWidgets = scanWidgetsForConsultant(widgets)
            if (fromWidgets.isNotBlank()) return fromWidgets
        }
        return ""
    }

    private fun scanWidgetsForConsultant(element: JsonElement): String {
        // Shallow scan — full flatten happens server-side; enough for filter decisions.
        val text = element.toString()
        val nameMarkers = listOf("\"business_name\"", "\"name\"", "\"title\"")
        for (marker in nameMarkers) {
            val idx = text.indexOf(marker)
            if (idx < 0) continue
            val after = text.substring(idx)
            val valueStart = after.indexOf('"', marker.length + 1)
            if (valueStart < 0) continue
            val valueEnd = after.indexOf('"', valueStart + 1)
            if (valueEnd < 0) continue
            val value = after.substring(valueStart + 1, valueEnd).trim()
            if (value.length in 2..80 && !value.startsWith("type.googleapis")) return value
        }
        return ""
    }
}
