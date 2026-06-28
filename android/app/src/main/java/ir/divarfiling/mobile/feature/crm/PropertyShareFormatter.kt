package ir.divarfiling.mobile.feature.crm

import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.feature.crm.components.PropertyFilters

object PropertyShareFormatter {
    fun buildShareText(property: PropertyDto): String = buildString {
        appendLine("🏠 ${property.title}")
        property.propertyType?.takeIf { it.isNotBlank() }?.let { appendLine("نوع: $it") }
        property.dealMode?.takeIf { it.isNotBlank() }?.let { appendLine("معامله: $it") }
        property.transactionStatus?.takeIf { it.isNotBlank() }?.let { appendLine("وضعیت: $it") }

        val location = PropertyFilters.locationLabel(property)
        if (location != "—") appendLine("موقعیت: $location")

        property.area?.let { appendLine("متراژ: ${PropertyFilters.formatArea(it)}") }
        property.rooms?.takeIf { it.isNotBlank() }?.let { appendLine("اتاق: $it") }
        PropertyFilters.formatFloor(property.floor, property.totalFloors)?.let { appendLine(it) }

        property.salePrice?.let { appendLine("قیمت فروش: ${FormatUtils.formatPriceToman(it)}") }
        property.deposit?.let { appendLine("رهن: ${FormatUtils.formatPriceToman(it)}") }
        property.rent?.let { appendLine("اجاره: ${FormatUtils.formatPriceToman(it)}") }

        property.address?.takeIf { it.isNotBlank() }?.let { appendLine("آدرس: $it") }
        property.notes?.takeIf { it.isNotBlank() }?.let { appendLine("یادداشت: $it") }
        property.link?.takeIf { it.isNotBlank() }?.let { appendLine("لینک: $it") }

        appendLine()
        append("— ارسال از فایلینگ دیوار")
    }
}
