package ir.divarfiling.mobile.feature.team

import org.junit.Assert.assertEquals
import org.junit.Test

class AgencySpaceTabsTest {
    @Test
    fun tabCount_matchesProductTabs() {
        assertEquals(5, AgencySpaceTab.labels.size)
    }
}
