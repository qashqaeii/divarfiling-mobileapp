package ir.divarfiling.mobile.feature.contentstudio.canvas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PcsAssetDiagnosticsTest {

    @Test
    fun hostForLogStripsQueryAndPath() {
        assertEquals(
            "divarfiling.ir",
            PcsAssetDiagnostics.hostForLog("https://divarfiling.ir/static/x.png?token=secret&sig=abc"),
        )
    }

    @Test
    fun invalidUrlReturnsNullHost() {
        assertNull(PcsAssetDiagnostics.hostForLog("   "))
    }
}
