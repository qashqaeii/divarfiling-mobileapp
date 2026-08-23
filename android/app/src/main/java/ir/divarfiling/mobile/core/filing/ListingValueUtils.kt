package ir.divarfiling.mobile.core.filing

import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.network.ListingDto
import ir.divarfiling.mobile.core.network.ListingMarketBenchmarkDto
import kotlin.math.abs
import kotlin.math.roundToInt

enum class ListingMarketTier {
    Under,
    Fair,
    Over,
    Unknown,
}

data class ListingValuePresentation(
    val tier: ListingMarketTier,
    val shortLabel: String,
    val detailLabel: String,
    val percent: Int?,
)

object ListingValueUtils {
    private const val THRESHOLD = 12

    fun presentationFor(listing: ListingDto): ListingValuePresentation? =
        presentationFor(listing.valueScore, listing.marketTier, listing.verdict)

    fun presentationFor(
        score: Double?,
        marketTier: String? = null,
        verdict: String? = null,
    ): ListingValuePresentation? {
        if (score == null || score.isNaN()) return null
        val tier = tierFrom(marketTier, score)
        if (tier == ListingMarketTier.Unknown) return null
        return buildPresentation(tier, score, verdict)
    }

    fun presentationFor(market: ListingMarketBenchmarkDto?): ListingValuePresentation? {
        market ?: return null
        return presentationFor(market.valueScore ?: market.diffPct, market.marketTier, market.shortVerdict)
    }

    fun tierFrom(marketTier: String?, score: Double): ListingMarketTier {
        marketTier?.lowercase()?.let {
            return when (it) {
                "under" -> ListingMarketTier.Under
                "over" -> ListingMarketTier.Over
                "fair" -> ListingMarketTier.Fair
                else -> ListingMarketTier.Unknown
            }
        }
        return when {
            score <= -THRESHOLD -> ListingMarketTier.Under
            score >= THRESHOLD -> ListingMarketTier.Over
            else -> ListingMarketTier.Fair
        }
    }

    fun buildPresentation(
        tier: ListingMarketTier,
        score: Double,
        verdict: String? = null,
    ): ListingValuePresentation {
        val percent = abs(score).roundToInt().takeIf { it > 0 }
        val persianPercent = percent?.let { DateUtils.toPersianDigits(it.toString()) }
        return when (tier) {
            ListingMarketTier.Under -> ListingValuePresentation(
                tier = tier,
                shortLabel = verdict?.takeIf { it.isNotBlank() } ?: "زیر بازار",
                detailLabel = persianPercent?.let { "$it٪ زیر بازار" } ?: "زیر بازار",
                percent = percent,
            )
            ListingMarketTier.Over -> ListingValuePresentation(
                tier = tier,
                shortLabel = verdict?.takeIf { it.isNotBlank() } ?: "بالای بازار",
                detailLabel = persianPercent?.let { "$it٪ بالای بازار" } ?: "بالای بازار",
                percent = percent,
            )
            ListingMarketTier.Fair -> {
                val label = verdict?.takeIf { it.isNotBlank() } ?: "متعادل"
                ListingValuePresentation(
                    tier = tier,
                    shortLabel = label,
                    detailLabel = persianPercent?.let { "$label · $it٪ اختلاف" } ?: label,
                    percent = percent,
                )
            }
            ListingMarketTier.Unknown -> ListingValuePresentation(
                tier = tier,
                shortLabel = "—",
                detailLabel = "—",
                percent = null,
            )
        }
    }
}
