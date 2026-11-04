package ir.divarfiling.mobile.feature.contentstudio.draft

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsStudioBrand
import org.junit.Assert.assertEquals
import org.junit.Test

class PcsBrandMergeTest {

    @Test
    fun effectiveMergesOverrideFieldsOnly() {
        val base = PcsStudioBrand(brandName = "A", consultantName = "B", contactPhone = "0912")
        val override = PcsBrandOverrideDraft(consultantName = "C")
        val merged = PcsBrandMerge.effective(base, override)
        assertEquals("A", merged.brandName)
        assertEquals("C", merged.consultantName)
        assertEquals("0912", merged.contactPhone)
    }
}
