package ir.divarfiling.mobile.feature.team

import ir.divarfiling.mobile.core.network.AgencyActivityDto
import ir.divarfiling.mobile.core.network.AgencyEventTypeOptionDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AgencyActivityEventLabelsTest {

    @Test
    fun localizedEventType_mapsKnownKeys() {
        val event = AgencyActivityDto(
            id = 1L,
            eventType = "seat.assigned",
            title = "",
            eventLabel = "",
            actorName = "علی",
            createdAt = "2026-01-01T10:00:00Z",
        )
        assertEquals("تخصیص صندلی", event.localizedEventType())
        assertEquals("تخصیص صندلی", event.displayTitle())
    }

    @Test
    fun displayTitle_neverShowsRawDotKey() {
        val event = AgencyActivityDto(
            id = 2L,
            eventType = "announcement.created",
            title = "announcement.created",
            eventLabel = "",
            actorName = "",
            createdAt = "",
        )
        val title = event.displayTitle()
        assertFalse(title.contains('.'))
        assertEquals("ثبت اطلاعیه", title)
    }

    @Test
    fun filterOption_localizedLabel() {
        val option = AgencyEventTypeOptionDto(value = "lead.assigned", label = "lead.assigned")
        assertEquals("تخصیص سرنخ", option.localizedLabel())
    }
}
