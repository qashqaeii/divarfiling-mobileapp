package ir.divarfiling.mobile.core.filing

import ir.divarfiling.mobile.core.network.ListingDetailDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListingLocationUtilsTest {

    @Test
    fun streetAndRegionAppearInDossierRows() {
        val listing = ListingDetailDto(
            token = "tok",
            city = "تهران",
            region = "مرزداران",
            street = "خ بوستان چهارم غربی",
            locationSummary = "خ بوستان چهارم غربی · مرزداران · تهران",
        )
        val parts = ListingLocationUtils.parts(listing)
        val rows = ListingLocationUtils.dossierRows(parts)
        assertEquals("خ بوستان چهارم غربی", parts.street)
        assertTrue(rows.any { it.first == "خیابان" && it.second == "خ بوستان چهارم غربی" })
        assertEquals(
            "خ بوستان چهارم غربی · مرزداران · تهران",
            ListingLocationUtils.headline(parts),
        )
    }

    @Test
    fun duplicateRegionAndNeighborhoodAreCollapsed() {
        val listing = ListingDetailDto(
            token = "tok",
            city = "تهران",
            region = "مرزداران",
            neighborhood = "مرزداران",
        )
        val parts = ListingLocationUtils.parts(listing)
        assertFalse(parts.neighborhood.isNotBlank())
        assertTrue(ListingLocationUtils.dossierRows(parts).none { it.first == "محله" })
    }
}
