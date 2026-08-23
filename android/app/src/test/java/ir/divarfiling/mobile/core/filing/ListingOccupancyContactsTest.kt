package ir.divarfiling.mobile.core.filing

import ir.divarfiling.mobile.core.network.ListingLinkedContactDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ListingOccupancyContactsTest {

    @Test
    fun ownerAndTenantAppearAsOccupancyAndAreRemovedFromOthers() {
        val linked = listOf(
            ListingLinkedContactDto(
                id = 1,
                customerId = 11,
                fullName = "علی",
                phone = "09121112233",
                role = "پیشنهادی",
            ),
            ListingLinkedContactDto(
                id = 2,
                customerId = 22,
                fullName = "سارا",
                phone = "09123334455",
                role = "پیشنهادی",
            ),
            ListingLinkedContactDto(
                id = 3,
                customerId = 33,
                fullName = "خریدار",
                phone = "09125556677",
                role = "پیشنهادی",
            ),
        )
        val presentation = ListingOccupancyContacts.build(
            ownerName = "علی رضایی",
            ownerPhone = "09121112233",
            tenantName = "سارا محمدی",
            tenantPhone = "09123334455",
            isVacant = false,
            contacts = linked,
        )
        assertEquals(2, presentation.occupancy.size)
        assertEquals(ListingOccupancyContacts.ROLE_OWNER, presentation.occupancy[0].role)
        assertEquals(ListingOccupancyContacts.ROLE_TENANT, presentation.occupancy[1].role)
        assertEquals(11L, presentation.occupancy[0].customerId)
        assertEquals(1, presentation.others.size)
        assertEquals("خریدار", presentation.others[0].fullName)
    }

    @Test
    fun vacantListingHidesTenantEvenIfPhoneExists() {
        val presentation = ListingOccupancyContacts.build(
            ownerName = "مالک",
            ownerPhone = "09121112233",
            tenantName = "سارا",
            tenantPhone = "09123334455",
            isVacant = true,
            contacts = emptyList(),
        )
        assertEquals(1, presentation.occupancy.size)
        assertEquals(ListingOccupancyContacts.ROLE_OWNER, presentation.occupancy[0].role)
        assertTrue(presentation.others.isEmpty())
    }
}
