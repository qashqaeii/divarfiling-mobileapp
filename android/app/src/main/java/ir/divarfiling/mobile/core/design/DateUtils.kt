package ir.divarfiling.mobile.core.design

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {

    private val jalaliDatePattern = Regex("""^1[34]\d{2}/\d{2}/\d{2}$""")
    private val isoDatePrefix = Regex("""^\d{4}-\d{2}-\d{2}""")

    fun formatJalaliDate(value: String?): String? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isBlank()) return null
        if (jalaliDatePattern.matches(trimmed)) return toPersianDigits(trimmed)
        val datePart = when {
            trimmed.length >= 10 && isoDatePrefix.containsMatchIn(trimmed) -> trimmed.take(10)
            else -> return null
        }
        return parseGregorianDate(datePart)?.let { (y, m, d) ->
            toPersianDigits(formatJalali(y, m, d))
        }
    }

    fun formatJalaliDateTime(value: String?): String? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isBlank()) return null
        return runCatching {
            val zoned = Instant.parse(trimmed).atZone(ZoneId.systemDefault())
            val (jy, jm, jd) = gregorianToJalali(
                zoned.year,
                zoned.monthValue,
                zoned.dayOfMonth,
            )
            val date = toPersianDigits(formatJalali(jy, jm, jd))
            val time = toPersianDigits(
                "%02d:%02d".format(zoned.hour, zoned.minute),
            )
            "$date $time"
        }.getOrElse {
            formatJalaliDate(trimmed)?.let { date ->
                val timePart = trimmed.drop(11).take(5)
                if (timePart.matches(Regex("""\d{2}:\d{2}"""))) {
                    "$date ${toPersianDigits(timePart)}"
                } else {
                    date
                }
            }
        }
    }

    fun formatRelativeFa(value: String?): String {
        val formatted = formatJalaliDateTime(value) ?: formatJalaliDate(value)
        return formatted ?: "—"
    }

    /** زمان نسبی فارسی: «۱ دقیقه پیش»، «۸ ساعت پیش»، «دیروز»، «۲۰ روز پیش» */
    fun formatRelativeTimeAgo(value: String?): String {
        if (value.isNullOrBlank()) return "اخیراً"
        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val then = parseToInstant(value, zone) ?: return "اخیراً"
        if (then.isAfter(now)) return "همین الان"

        val zonedNow = now.atZone(zone)
        val zonedThen = then.atZone(zone)
        val daysBetween = ChronoUnit.DAYS.between(zonedThen.toLocalDate(), zonedNow.toLocalDate())

        when {
            daysBetween >= 30 -> return formatJalaliDate(value) ?: "—"
            daysBetween >= 2 -> return "${toPersianDigits(daysBetween.toString())} روز پیش"
            daysBetween == 1L -> return "دیروز"
        }

        val duration = Duration.between(then, now)
        val minutes = duration.toMinutes()
        val hours = duration.toHours()
        return when {
            minutes < 1 -> "لحظاتی پیش"
            minutes < 60 -> "${toPersianDigits(minutes.toString())} دقیقه پیش"
            else -> "${toPersianDigits(hours.toString())} ساعت پیش"
        }
    }

    private fun parseToInstant(value: String, zone: ZoneId): Instant? {
        val trimmed = value.trim()
        return runCatching { Instant.parse(trimmed) }.getOrNull()
            ?: runCatching {
                LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(zone)
                    .toInstant()
            }.getOrNull()
            ?: runCatching {
                LocalDate.parse(trimmed.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
                    .atStartOfDay(zone)
                    .toInstant()
            }.getOrNull()
    }

    fun todayJalaliLabel(): String {
        val now = LocalDate.now()
        val (jy, jm, jd) = gregorianToJalali(now.year, now.monthValue, now.dayOfMonth)
        return toPersianDigits(formatJalali(jy, jm, jd))
    }

    fun formatJalali(year: Int, month: Int, day: Int): String =
        "%04d/%02d/%02d".format(year, month, day)

    private fun parseGregorianDate(isoDate: String): Triple<Int, Int, Int>? =
        runCatching {
            val parts = isoDate.split('-')
            Triple(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }.getOrNull()

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(0, 31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gy2 = gy - 1600
        var gm2 = gm - 1
        var gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400
        for (i in 0 until gm2) {
            gDayNo += gDaysInMonth[i + 1]
        }
        if (gm2 > 1 && isGregorianLeap(gy)) gDayNo++
        gDayNo += gd2

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 1
        while (jm <= 12 && jDayNo >= jDaysInMonth[jm]) {
            jDayNo -= jDaysInMonth[jm]
            jm++
        }
        val jd = jDayNo + 1
        return Triple(jy, jm, jd)
    }

    private fun isGregorianLeap(year: Int): Boolean =
        year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

    fun toPersianDigits(input: String): String =
        buildString(input.length) {
            input.forEach { ch ->
                append(
                    when (ch) {
                        in '0'..'9' -> persianDigits[ch - '0']
                        else -> ch
                    },
                )
            }
        }

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
}
