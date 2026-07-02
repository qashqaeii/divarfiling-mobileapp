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

    /** Parses money/area inputs that may use Persian (۰-۹) or Arabic (٠-٩) digits. */
    fun parseLocalizedLong(raw: String): Long? {
        val digits = buildString {
            raw.forEach { ch ->
                when (ch) {
                    in '0'..'9' -> append(ch)
                    in '۰'..'۹' -> append(ch - '۰')
                    in '٠'..'٩' -> append(ch - '٠')
                }
            }
        }
        return digits.takeIf { it.isNotEmpty() }?.toLongOrNull()
    }
}
