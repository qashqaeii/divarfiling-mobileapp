package ir.divarfiling.mobile.feature.extract.divar

import ir.divarfiling.mobile.feature.extract.divar.ExtractAdvancedFilters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OutputNameHintTest {

    @Test
    fun build_usesDistrictSlugWhenProvided() {
        val filters = ExtractFilters(
            cityName = "تهران",
            citySlug = "tehran",
            districtSlugs = listOf("ostad-moein"),
        )
        val hint = OutputNameHint.build(filters)
        assertEquals("ostad-moein_apartment-rent", hint)
    }

    @Test
    fun build_includesCategorySoSubcategoriesStaySeparate() {
        val rent = ExtractFilters(
            cityName = "تهران",
            districtSlugs = listOf("ostad-moein"),
            category = "apartment-rent",
        )
        val sell = ExtractFilters(
            cityName = "تهران",
            districtSlugs = listOf("ostad-moein"),
            category = "residential-sell",
        )
        assertEquals("ostad-moein_apartment-rent", OutputNameHint.build(rent))
        assertEquals("ostad-moein_residential-sell", OutputNameHint.build(sell))
    }

    @Test
    fun advertiserFilter_genuinePersonal_rejectsConsultant() {
        val raw = """{"webengage":{"business_type":"premium-panel"}}"""
        val element = kotlinx.serialization.json.Json.parseToJsonElement(raw)
        assertTrue(!AdvertiserFilter.matches(element, "genuine_personal"))
        val personal = """{"webengage":{"business_type":"personal"}}"""
        val personalEl = kotlinx.serialization.json.Json.parseToJsonElement(personal)
        assertTrue(AdvertiserFilter.matches(personalEl, "genuine_personal"))
    }

    @Test
    fun advertiserFilter_personal_rejectsConsultantBusinessType() {
        val raw = """{"webengage":{"business_type":"premium-panel"}}"""
        val element = kotlinx.serialization.json.Json.parseToJsonElement(raw)
        assertTrue(!AdvertiserFilter.matches(element, "personal"))
        assertTrue(AdvertiserFilter.matches(element, "all"))
    }
}
