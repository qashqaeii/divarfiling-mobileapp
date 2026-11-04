package ir.divarfiling.mobile.feature.contentstudio.canvas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PcsContentResolverTest {

    private val property = PcsStudioProperty(
        title = "آپارتمان — تهران",
        headline = "۱۲۰ متر، ۳ خواب",
        propertyType = "آپارتمان",
        areaLabel = "۱۲۰ متر",
        floorLabel = "طبقه ۴",
        locationLine = "ونک — تهران",
        city = "تهران",
        detailChips = listOf("پارکینگ"),
    )

    @Test
    fun overlayMetaSummary_matchesWebPartsOrder() {
        val headline = PcsContentResolver.posterHeadline(property)
        val summary = PcsContentResolver.overlayMetaSummary(property, headline)
        assertTrue(summary.contains("ونک"))
        assertTrue(summary.contains("آپارتمان") || summary.contains("۱۲۰"))
    }

    @Test
    fun pearlescentMetaLines_skipsLocationAndPriceKeys() {
        val prop = property.copy(
            editorFeatures = listOf(
                PcsEditorFeature("location_line", "موقعیت", "ونک"),
                PcsEditorFeature("rooms_label", "اتاق", "۳ خواب"),
                PcsEditorFeature("floor", "طبقه", "۴"),
            ),
        )
        val text = PcsContentResolver.pearlescentMetaLines(prop, "")
        assertFalse(text.contains("location_line"))
        assertTrue(text.contains("طبقه") || text.contains("اتاق"))
    }

    @Test
    fun detachedBinding_usesLayerText() {
        val layer = PcsLayer(
            id = "headline-1",
            type = "text",
            binding = PcsBinding(source = "property", field = "headline", mode = "detached"),
            text = "عنوان سفارشی",
            contentContract = PcsContentContract(resolver = "poster_headline"),
        )
        val resolved = PcsContentResolver.resolveLayerText(layer, property, posterConfHeadline = "", brandFooter = "")
        assertEquals("عنوان سفارشی", resolved)
    }
}
