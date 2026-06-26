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
        assertTrue(hint.startsWith("ostad-moein_"))
    }

    @Test
    fun advertiserFilter_personal_rejectsConsultantBusinessType() {
        val raw = """{"webengage":{"business_type":"premium-panel"}}"""
        val element = kotlinx.serialization.json.Json.parseToJsonElement(raw)
        assertTrue(!AdvertiserFilter.matches(element, "personal"))
        assertTrue(AdvertiserFilter.matches(element, "all"))
    }
}
