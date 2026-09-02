package ir.divarfiling.mobile.core.filing

import ir.divarfiling.mobile.core.network.ListingRetentionDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListingRetentionUtilsTest {
    @Test
    fun isWarning_onlyForWarningStatus() {
        assertTrue(ListingRetentionUtils.isWarning(ListingRetentionDto(status = "warning", daysRemaining = 2)))
        assertFalse(ListingRetentionUtils.isWarning(ListingRetentionDto(status = "normal")))
        assertFalse(ListingRetentionUtils.isWarning(null))
    }

    @Test
    fun countdownLabel_usesBackendDaysRemaining() {
        assertEquals("امروز", ListingRetentionUtils.countdownLabel(0))
        assertEquals("فردا", ListingRetentionUtils.countdownLabel(1))
        assertEquals("3 روز دیگر", ListingRetentionUtils.countdownLabel(3))
    }

    @Test
    fun badgeText_includesDaysWhenPresent() {
        assertEquals("در آستانه پاک‌سازی · 2 روز", ListingRetentionUtils.badgeText(2))
        assertEquals("در آستانه پاک‌سازی", ListingRetentionUtils.badgeText(null))
    }
}
