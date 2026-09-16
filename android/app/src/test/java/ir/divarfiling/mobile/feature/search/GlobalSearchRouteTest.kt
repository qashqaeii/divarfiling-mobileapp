package ir.divarfiling.mobile.feature.search

import org.junit.Assert.assertEquals
import org.junit.Test

class GlobalSearchRouteTest {
    @Test
    fun routesListingByToken() {
        val route = routeForDestination(
            ir.divarfiling.mobile.core.network.GlobalSearchDestinationDto(
                kind = "listing",
                token = "abc",
            ),
        )
        assertEquals("filing/listing/abc", route)
    }

    @Test
    fun routesPropertyById() {
        val route = routeForDestination(
            ir.divarfiling.mobile.core.network.GlobalSearchDestinationDto(
                kind = "property",
                propertyId = 42,
            ),
        )
        assertEquals("crm/properties/42?openEdit=false", route)
    }
}
