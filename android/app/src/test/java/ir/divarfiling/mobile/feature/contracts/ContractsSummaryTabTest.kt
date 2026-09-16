package ir.divarfiling.mobile.feature.contracts

import org.junit.Assert.assertEquals
import org.junit.Test

class ContractsSummaryTabTest {
    @Test
    fun summaryTabsCount() {
        assertEquals(5, ContractSummaryTab.entries.size)
    }
}
