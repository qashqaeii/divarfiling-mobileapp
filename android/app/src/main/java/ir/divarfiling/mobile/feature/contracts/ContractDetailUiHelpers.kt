package ir.divarfiling.mobile.feature.contracts

import ir.divarfiling.mobile.core.network.ContractDetailDto
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

data class LabeledValue(val label: String, val value: String)

fun financialRows(detail: ContractDetailDto): List<LabeledValue> {
    val fin = detail.financial
    if (fin.isEmpty()) return emptyList()
    val labels = mapOf(
        "toman" to "مبلغ",
        "total_price" to "مبلغ معامله",
        "deposit" to "ودیعه",
        "monthly_rent" to "اجاره ماهانه",
        "transfer_date" to "تاریخ انتقال",
        "transfer_place" to "محل انتقال",
        "lease_start" to "شروع اجاره",
        "lease_end" to "پایان اجاره",
        "delivery_date" to "تحویل",
    )
    val out = mutableListOf<LabeledValue>()
    labels.forEach { (key, label) ->
        fin[key]?.let { el -> jsonPrimitive(el)?.takeIf { it.isNotBlank() }?.let { out.add(LabeledValue(label, it)) } }
    }
    if (out.isEmpty()) {
        fin.forEach { (key, el) ->
            if (key == "kind") return@forEach
            jsonPrimitive(el)?.takeIf { it.isNotBlank() }?.let {
                out.add(LabeledValue(key, it))
            }
        }
    }
    return out
}

fun propertySummary(detail: ContractDetailDto): String {
    val prop = detail.property
    if (prop.isEmpty()) return ""
    return listOf("title", "address", "city", "district")
        .mapNotNull { key -> prop[key]?.let { jsonPrimitive(it) }?.takeIf { it.isNotBlank() } }
        .firstOrNull()
        .orEmpty()
}

private fun jsonPrimitive(el: kotlinx.serialization.json.JsonElement): String? {
    return (el as? JsonPrimitive)?.contentOrNull
}
