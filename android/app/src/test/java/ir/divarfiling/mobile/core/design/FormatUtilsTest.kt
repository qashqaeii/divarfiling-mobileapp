package ir.divarfiling.mobile.core.design

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun formatPriceToman_usesPersianSeparators() {
        assertEquals("1٬500٬000٬000 تومان", FormatUtils.formatPriceToman(1_500_000_000L))
    }

    @Test
    fun formatPriceShort_millions() {
        assertEquals("850 میلیون", FormatUtils.formatPriceShort(850_000_000L))
    }
}
