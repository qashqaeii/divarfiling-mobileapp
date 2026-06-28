package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.network.PropertyDto

object PropertyFilters {
    fun locationLabel(property: PropertyDto): String {
        val parts = listOfNotNull(
            property.neighborhood?.takeIf { it.isNotBlank() },
            property.district?.takeIf { it.isNotBlank() },
            property.city?.takeIf { it.isNotBlank() },
        )
        return parts.joinToString(" · ").ifBlank { "—" }
    }

    fun formatArea(area: Double?): String? =
        area?.let { if (it % 1.0 == 0.0) "${it.toInt()} متر" else "$it متر" }

    fun formatFloor(floor: Int?, totalFloors: Int?): String? {
        if (floor == null) return null
        return if (totalFloors != null) "طبقه $floor/$totalFloors" else "طبقه $floor"
    }

    fun priceSummary(property: PropertyDto): String {
        property.salePrice?.let { return FormatUtils.formatPriceShort(it) + " تومان" }
        if (property.deposit != null || property.rent != null) {
            val dep = property.deposit?.let { FormatUtils.formatPriceShort(it) }
            val rent = property.rent?.let { FormatUtils.formatPriceShort(it) }
            return when {
                dep != null && rent != null -> "رهن $dep + اجاره $rent"
                dep != null -> "رهن $dep"
                rent != null -> "اجاره $rent"
                else -> "قیمت ثبت نشده"
            }
        }
        return "قیمت ثبت نشده"
    }

    fun priceLabel(property: PropertyDto): String = when {
        property.salePrice != null -> "فروش"
        property.deposit != null && property.rent != null -> "رهن و اجاره"
        property.deposit != null -> "رهن"
        property.rent != null -> "اجاره"
        else -> ""
    }

    fun jalaliUpdated(property: PropertyDto): String? =
        property.updatedAt?.let { DateUtils.formatJalaliDateTime(it) }

    fun totalCount(properties: List<PropertyDto>): Int = properties.size

    fun saleCount(properties: List<PropertyDto>): Int =
        properties.count { it.dealMode?.contains("فروش") == true }

    fun rentCount(properties: List<PropertyDto>): Int =
        properties.count {
            it.dealMode?.contains("اجاره") == true || it.dealMode?.contains("رهن") == true
        }

    fun activeCount(properties: List<PropertyDto>): Int =
        properties.count { it.transactionStatus == "فعال" }

    fun isSaleDeal(property: PropertyDto): Boolean =
        property.dealMode?.contains("فروش") == true && !property.dealMode.orEmpty().contains("اجاره")

    fun propertyTypeIcon(type: String?): ImageVector = when {
        type.isNullOrBlank() -> DfIcons.Building
        type.contains("ویلا") -> DfIcons.Home
        type.contains("زمین") -> DfIcons.MapPin
        type.contains("مغازه") -> DfIcons.Building
        else -> DfIcons.Building
    }

    fun txStatusColors(status: String?): Pair<Color, Color> = when (status) {
        "فعال" -> DfColors.Green to DfColors.GreenLight
        "در مذاکره" -> DfColors.Amber to DfColors.AmberLight
        "قرارداد" -> DfColors.Purple to DfColors.PurpleContainer
        "فروخته‌شده", "اجاره‌رفته" -> DfColors.Blue to DfColors.BlueLight
        "بایگانی" -> DfColors.TextMuted to DfColors.SurfaceVariant
        else -> DfColors.Purple to DfColors.PurpleContainer
    }

    fun dealModeAccent(property: PropertyDto): Color = when {
        isSaleDeal(property) -> DfColors.Purple
        property.dealMode?.contains("رهن") == true -> DfColors.Blue
        property.dealMode?.contains("اجاره") == true -> DfColors.Amber
        else -> DfColors.Purple
    }

    fun publishDotColor(status: String?): Color = when (status) {
        "منتشرشده" -> DfColors.Green
        "پیش‌نویس" -> DfColors.Amber
        else -> DfColors.TextMuted
    }
}
