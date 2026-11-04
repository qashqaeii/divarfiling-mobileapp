package ir.divarfiling.mobile.feature.contentstudio.canvas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PcsConfidentialMaskingTest {
    @Test
    fun maskedPropertyDoesNotLeakLocationIntoResolverWhenEmpty() {
        val property = PcsStudioProperty(
            title = "ملک محرمانه",
            locationLine = "",
            city = "",
            price = PcsPriceBlock(show = false, label = "۱۲ میلیارد"),
        )
        val layer = PcsLayer(
            id = "loc",
            type = "bound_text",
            binding = ir.divarfiling.mobile.feature.contentstudio.canvas.PcsBinding(
                source = "property",
                field = "location_line",
                mode = "linked",
            ),
            width = 100f,
            height = 40f,
        )
        val text = PcsContentResolver.resolveLayerText(layer, property, posterConfHeadline = "عنوان", brandFooter = "")
        assertEquals("", text)
        assertFalse(text.contains("میلیارد"))
    }
}
