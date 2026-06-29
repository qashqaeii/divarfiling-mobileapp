package ir.divarfiling.mobile.core.filing

import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.network.ListingDto

data class ListingPriceLine(
    val label: String,
    val value: String,
    val emphasized: Boolean = false,
)

object ListingPriceUtils {

    fun isRental(listing: ListingDto): Boolean =
        (listing.rent ?: 0) > 0 || (listing.deposit ?: 0) > 0

    fun transactionLabel(listing: ListingDto): String {
        listing.transactionType?.takeIf { it.isNotBlank() }?.let { return it }
        return when {
            isRental(listing) -> "اجاره"
            (listing.price ?: 0) > 0 -> "فروش"
            else -> "نامشخص"
        }
    }

    fun primaryPriceLine(listing: ListingDto): ListingPriceLine? = when {
        isRental(listing) -> {
            val rent = listing.rent?.takeIf { it > 0 }
            val deposit = listing.deposit?.takeIf { it > 0 }
            when {
                rent != null && deposit != null ->
                    ListingPriceLine("رهن و اجاره", "${FormatUtils.formatPriceShort(deposit)} / ${FormatUtils.formatPriceShort(rent)}", emphasized = true)
                rent != null ->
                    ListingPriceLine("اجاره ماهانه", FormatUtils.formatPriceShort(rent), emphasized = true)
                deposit != null ->
                    ListingPriceLine("ودیعه", FormatUtils.formatPriceShort(deposit), emphasized = true)
                else -> null
            }
        }
        (listing.price ?: 0) > 0 ->
            ListingPriceLine("قیمت کل", FormatUtils.formatPriceShort(listing.price!!), emphasized = true)
        else -> null
    }

    fun secondaryPriceLines(listing: ListingDto): List<ListingPriceLine> {
        val lines = mutableListOf<ListingPriceLine>()
        val perSqm = listing.pricePerSqm
            ?: listing.price?.takeIf { it > 0 }?.let { price ->
                listing.area?.takeIf { it > 0 }?.let { area -> (price / area).toInt() }
            }
        perSqm?.takeIf { it > 0 }?.let {
            lines.add(ListingPriceLine("هر متر", FormatUtils.formatPriceShort(it.toLong())))
        }
        if (!isRental(listing)) {
            listing.deposit?.takeIf { it > 0 }?.let {
                lines.add(ListingPriceLine("ودیعه", FormatUtils.formatPriceShort(it)))
            }
            listing.rent?.takeIf { it > 0 }?.let {
                lines.add(ListingPriceLine("اجاره", FormatUtils.formatPriceShort(it)))
            }
        } else if ((listing.price ?: 0) > 0) {
            lines.add(ListingPriceLine("قیمت کل", FormatUtils.formatPriceShort(listing.price!!)))
        }
        return lines
    }
}
