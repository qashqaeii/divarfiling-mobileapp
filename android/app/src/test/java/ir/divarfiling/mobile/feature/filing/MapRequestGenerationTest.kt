package ir.divarfiling.mobile.feature.filing

import ir.divarfiling.mobile.feature.filing.map.isStaleMapResponse
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapRequestGenerationTest {
    @Test
    fun staleRequestDetected() {
        val current = 3
        assertTrue(isStaleMapResponse(requestId = 2, currentGeneration = current))
        assertFalse(isStaleMapResponse(requestId = 3, currentGeneration = current))
    }
}
