package ir.divarfiling.mobile.feature.filing.map

import org.junit.Assert.assertEquals
import org.junit.Test

class FilingDatasetSelectionLabelsTest {
    @Test
    fun toolbarAllWhenNoneSelected() {
        assertEquals("همه فایل‌ها", FilingDatasetSelectionLabels.toolbarLabel(0) { it })
    }

    @Test
    fun toolbarCountWhenSelected() {
        assertEquals("3 فایل انتخاب شده", FilingDatasetSelectionLabels.toolbarLabel(3) { it })
    }
}
