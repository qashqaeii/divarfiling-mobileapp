package ir.divarfiling.mobile.core.design

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class DateUtilsJalaliTest {

    @Test
    fun gregorianToJalali_roundTrip() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val (jy, jm, jd) = DateUtils.gregorianToJalali(today.year, today.monthValue, today.dayOfMonth)
        val millis = DateUtils.jalaliDateTimeToMillis(jy, jm, jd, 10, 30, zone)
        val formatted = DateUtils.formatJalaliDateTimeFromMillis(millis, zone)
        assertTrue(formatted.contains(DateUtils.toPersianDigits("10:30")))
        val (gy, gm, gd) = DateUtils.jalaliToGregorian(jy, jm, jd)
        assertEquals(today.year, gy)
        assertEquals(today.monthValue, gm)
        assertEquals(today.dayOfMonth, gd)
    }

    @Test
    fun formatForDisplay_usesJalaliForIsoDate() {
        val formatted = DateUtils.formatForDisplay("2026-03-18")
        assertTrue(formatted.contains("۱۴"))
    }

    @Test
    fun formatJalaliDateTimeFromMillis_usesPersianDigits() {
        val zone = ZoneId.systemDefault()
        val millis = DateUtils.jalaliDateTimeToMillis(1404, 1, 1, 9, 5, zone)
        val formatted = DateUtils.formatJalaliDateTimeFromMillis(millis, zone)
        assertTrue(formatted.contains('۰') || formatted.contains('۱'))
    }
}
