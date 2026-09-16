package ir.divarfiling.mobile.core.design

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PresentationLabelsTest {
    @Test
    fun invitationStatusPersian() {
        assertEquals("پذیرفته‌شده", PresentationLabels.invitationStatus("accepted"))
        assertEquals("لغوشده", PresentationLabels.invitationStatus("cancelled"))
        assertEquals("در انتظار", PresentationLabels.invitationStatus("pending"))
    }

    @Test
    fun weakDisplayNameUsesPhoneFallback() {
        assertEquals("موبایل: 09121234567", PresentationLabels.memberDisplayName("Fa", "09121234567"))
        assertFalse(PresentationLabels.isWeakDisplayName("علی رضایی"))
    }

    @Test
    fun attentionSubtitleFormatsRawPair() {
        val text = PresentationLabels.attentionSubtitle("Fa ,09197480933")
        assertTrue(text.contains("09197480933"))
    }
}
