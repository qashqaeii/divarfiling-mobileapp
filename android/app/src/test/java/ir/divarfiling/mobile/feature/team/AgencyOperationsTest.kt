package ir.divarfiling.mobile.feature.team

import org.junit.Assert.assertEquals
import org.junit.Test

class AgencyOperationsTest {
    @Test
    fun performanceRangeDays() {
        assertEquals(1, AgencyPerformanceRange.daysFor("today"))
        assertEquals(7, AgencyPerformanceRange.daysFor("week"))
        assertEquals(30, AgencyPerformanceRange.daysFor("month"))
    }

    @Test
    fun inboxFilterConstants() {
        assertEquals("all", AgencyInboxFilter.ALL)
        assertEquals("fresh", AgencyInboxFilter.FRESH)
        assertEquals("stale", AgencyInboxFilter.STALE)
    }

    @Test
    fun advisorFilterConstants() {
        assertEquals("invited", AgencyAdvisorFilter.INVITED)
        assertEquals("no_seat", AgencyAdvisorFilter.NO_SEAT)
    }
}
