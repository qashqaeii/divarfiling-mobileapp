package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.license.LicenseState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtractionLicenseGateTest {

    @Test
    fun `deny extract when license invalid`() {
        val gate = ExtractionRepository.gateFromLicenseStatic(
            LicenseState(valid = false, lightExtractEnabled = true),
        )
        assertTrue(gate is ExtractGateResult.Denied)
    }

    @Test
    fun `deny extract when light extract disabled`() {
        val gate = ExtractionRepository.gateFromLicenseStatic(
            LicenseState(valid = true, lightExtractEnabled = false),
        )
        assertTrue(gate is ExtractGateResult.Denied)
    }

    @Test
    fun `allow extract with valid license and light extract`() {
        val gate = ExtractionRepository.gateFromLicenseStatic(
            LicenseState(valid = true, lightExtractEnabled = true),
        )
        assertEquals(ExtractGateResult.Allowed, gate)
    }
}
