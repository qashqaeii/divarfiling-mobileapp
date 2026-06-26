package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.license.LicenseState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtractionLicenseGateTest {

  private val repo = ExtractionRepository(
        api = error(""),
        divarClient = error(""),
        sessionStore = error(""),
        licenseRepository = error(""),
        json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true },
    )

    @Test
    fun `deny extract when license invalid`() {
        val gate = repo.gateFromLicense(LicenseState(valid = false, lightExtractEnabled = true))
        assertTrue(gate is ExtractGateResult.Denied)
    }

    @Test
    fun `deny extract when light extract disabled`() {
        val gate = repo.gateFromLicense(LicenseState(valid = true, lightExtractEnabled = false))
        assertTrue(gate is ExtractGateResult.Denied)
    }

    @Test
    fun `allow extract with valid license and light extract`() {
        val gate = repo.gateFromLicense(LicenseState(valid = true, lightExtractEnabled = true))
        assertEquals(ExtractGateResult.Allowed, gate)
    }
}
