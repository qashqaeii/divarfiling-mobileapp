package ir.divarfiling.mobile.feature.team

import ir.divarfiling.mobile.core.network.AgencyAdvisorDto
import ir.divarfiling.mobile.core.network.AgencyAdvisorSeatDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgencySeatDisplayTest {

    @Test
    fun resolvedHasSeat_whenApiSaysHasSeat() {
        val advisor = AgencyAdvisorDto(id = 1, hasSeat = true, status = "active")
        assertTrue(advisor.resolvedHasSeat())
    }

    @Test
    fun resolvedHasSeat_falseWhenExpired() {
        val advisor = AgencyAdvisorDto(id = 1, hasSeat = true, licenseId = 3L, status = "expired")
        assertFalse(advisor.resolvedHasSeat())
        assertTrue(advisor.hasSeatAssignment())
    }

    @Test
    fun hasSeatAssignment_fromLicenseId() {
        val advisor = AgencyAdvisorDto(id = 1, hasSeat = false, licenseId = 7L, status = "active")
        assertTrue(advisor.hasSeatAssignment())
        assertTrue(advisor.resolvedHasSeat())
    }

    @Test
    fun sequenceAlone_doesNotGrantSeat() {
        val advisor = AgencyAdvisorDto(id = 1, hasSeat = false, sequence = 2)
        assertFalse(advisor.resolvedHasSeat())
        assertFalse(advisor.hasSeatAssignment())
    }

    @Test
    fun seatDto_resolvedHasSeat_requiresHasSeatOrLicense() {
        val seat = AgencyAdvisorSeatDto(hasSeat = true, licenseId = 1L)
        assertTrue(seat.resolvedHasSeat())
    }
}
