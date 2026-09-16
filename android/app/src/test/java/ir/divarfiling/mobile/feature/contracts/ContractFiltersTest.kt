package ir.divarfiling.mobile.feature.contracts

import org.junit.Assert.assertEquals
import org.junit.Test

class ContractFiltersTest {
    @Test
    fun activeCountDefaultsZero() {
        assertEquals(0, ContractSheetFilters().activeCount())
    }

    @Test
    fun activeCountDetectsFields() {
        val f = ContractSheetFilters(contractType = "sale", dealLinked = "true", sort = "created")
        assertEquals(3, f.activeCount())
    }
}
