package ir.divarfiling.mobile.core.filing

import ir.divarfiling.mobile.core.network.ListingDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ListingValueUtilsTest {

    @Test
    fun presentationFor_underMarket_includesPercent() {
        val listing = ListingDto(
            token = "1",
            valueScore = -18.5,
            marketTier = "under",
            verdict = "زیر بازار",
        )
        val presentation = ListingValueUtils.presentationFor(listing)
        assertNotNull(presentation)
        assertEquals(ListingMarketTier.Under, presentation?.tier)
        assertEquals("زیر بازار", presentation?.shortLabel)
        assertEquals("۱۸٪ زیر بازار", presentation?.detailLabel)
    }

    @Test
    fun presentationFor_overMarket_includesPercent() {
        val listing = ListingDto(
            token = "2",
            valueScore = 22.0,
            marketTier = "over",
            verdict = "بالای بازار",
        )
        val presentation = ListingValueUtils.presentationFor(listing)
        assertNotNull(presentation)
        assertEquals(ListingMarketTier.Over, presentation?.tier)
        assertEquals("بالای بازار", presentation?.shortLabel)
        assertEquals("۲۲٪ بالای بازار", presentation?.detailLabel)
    }

    @Test
    fun presentationFor_fair_hasNoPercent() {
        val listing = ListingDto(
            token = "3",
            valueScore = 5.0,
            marketTier = "fair",
            verdict = "متعارف",
        )
        val presentation = ListingValueUtils.presentationFor(listing)
        assertNotNull(presentation)
        assertEquals(ListingMarketTier.Fair, presentation?.tier)
        assertEquals("متعارف", presentation?.detailLabel)
        assertNull(presentation?.percent)
    }

    @Test
    fun presentationFor_missingScore_returnsNull() {
        val listing = ListingDto(token = "4")
        assertNull(ListingValueUtils.presentationFor(listing))
    }
}
