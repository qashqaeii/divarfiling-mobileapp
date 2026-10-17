package ir.divarfiling.mobile.feature.postcall

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class PostCallReminderSchedulingTest {

    @Test
    fun tomorrow_morning_is_next_calendar_day_not_plus_24h() {
        val zone = ZoneId.of("Asia/Tehran")
        val now = ZonedDateTime.of(2026, 3, 15, 23, 30, 0, 0, zone).toInstant()
        val tomorrow = PostCallLogic.ReminderScheduling.tomorrowMorning(now, zone)
        val z = tomorrow.atZone(zone)
        assertEquals(16, z.dayOfMonth)
        assertEquals(10, z.hour)
        assertEquals(0, z.minute)
    }

    @Test
    fun one_hour_from_now_is_3600_seconds() {
        val now = Instant.parse("2026-01-01T12:00:00Z")
        val later = PostCallLogic.ReminderScheduling.oneHourFromNow(now)
        assertEquals(now.epochSecond + 3600, later.epochSecond)
    }

    @Test
    fun resolve_status_maps_ambiguous_without_defaulting_to_new() {
        assertEquals(PostCallUiMode.Ambiguous, PostCallLogic.resolveStatusToMode("ambiguous"))
        assertEquals(PostCallUiMode.Error, PostCallLogic.resolveStatusToMode("server_error"))
    }

    @Test
    fun pending_call_id_is_stable_for_same_metadata() {
        val a = PostCallLogic.ReminderScheduling.pendingCallId(
            "09121234567",
            PostCallLogic.CallEndKind.Answered,
            100L,
        )
        val b = PostCallLogic.ReminderScheduling.pendingCallId(
            "09121234567",
            PostCallLogic.CallEndKind.Answered,
            100L,
        )
        assertEquals(a, b)
        assertTrue(a.contains("09121234567"))
    }
}
