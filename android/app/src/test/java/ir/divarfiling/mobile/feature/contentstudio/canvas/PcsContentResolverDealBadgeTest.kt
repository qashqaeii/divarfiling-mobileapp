package ir.divarfiling.mobile.feature.contentstudio.canvas

import org.junit.Assert.assertEquals
import org.junit.Test

class PcsContentResolverDealBadgeTest {

    @Test
    fun posterDealBadgeLabel_matchesWebBindings() {
        assertEquals("آگهی فروش", PcsContentResolver.posterDealBadgeLabel("فروش"))
        assertEquals("آگهی رهن و اجاره", PcsContentResolver.posterDealBadgeLabel("رهن و اجاره"))
    }

    @Test
    fun propertyPriceLabel_respectsShowFlag() {
        val hidden = PcsStudioProperty(
            price = PcsPriceBlock(show = false, label = "۱۰ میلیارد"),
        )
        assertEquals("", PcsContentResolver.propertyPriceLabel(hidden))
    }
}
