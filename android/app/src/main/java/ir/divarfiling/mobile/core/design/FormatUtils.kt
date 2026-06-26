package ir.divarfiling.mobile.core.design

object FormatUtils {

    fun formatPriceToman(value: Long): String =
        "%,d تومان".format(value).replace(',', '٬')

    fun formatPriceShort(value: Long): String {
        return when {
            value >= 1_000_000_000 -> "${value / 1_000_000_000} میلیارد"
            value >= 1_000_000 -> "${value / 1_000_000} میلیون"
            else -> formatPriceToman(value)
        }
    }

    fun formatArea(area: Int?): String =
        area?.let { "$it متر" } ?: "—"

    fun formatRooms(rooms: Int?): String =
        rooms?.let { if (it == 0) "بدون اتاق" else "$it خواب" } ?: "—"
}
