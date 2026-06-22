package com.studyplanner

import com.studyplanner.util.DateUtils
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class DateUtilsTest {

    @Test
    fun parseInputDate_validFormat_returnsEpochMs() {
        val result = DateUtils.parseInputDate("06/22/2026")
        assertNotNull(result)
        val cal = Calendar.getInstance().apply { timeInMillis = result!! }
        assertEquals(6,    cal.get(Calendar.MONTH) + 1) // months are 0-based
        assertEquals(22,   cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(2026, cal.get(Calendar.YEAR))
    }

    @Test
    fun parseInputDate_invalidFormat_returnsNull() {
        assertNull(DateUtils.parseInputDate("not-a-date"))
        assertNull(DateUtils.parseInputDate(""))
        assertNull(DateUtils.parseInputDate("2026-06-22")) // wrong format
    }

    @Test
    fun formatMinutes_zero() {
        assertEquals("0m", DateUtils.formatMinutes(0))
    }

    @Test
    fun formatMinutes_underOneHour() {
        assertEquals("45m", DateUtils.formatMinutes(45))
    }

    @Test
    fun formatMinutes_exactHour() {
        assertEquals("2h", DateUtils.formatMinutes(120))
    }

    @Test
    fun formatMinutes_hoursAndMinutes() {
        assertEquals("1h 30m", DateUtils.formatMinutes(90))
    }

    @Test
    fun formatElapsedTime_underOneMinute() {
        assertEquals("00:45", DateUtils.formatElapsedTime(45))
    }

    @Test
    fun formatElapsedTime_minutes() {
        assertEquals("03:05", DateUtils.formatElapsedTime(185))
    }

    @Test
    fun formatElapsedTime_withHours() {
        assertEquals("1:01:01", DateUtils.formatElapsedTime(3661))
    }

    @Test
    fun isOverdue_pastTimestamp_returnsTrue() {
        val yesterday = System.currentTimeMillis() - 86_400_000L
        assertTrue(DateUtils.isOverdue(yesterday))
    }

    @Test
    fun isOverdue_futureTimestamp_returnsFalse() {
        val tomorrow = System.currentTimeMillis() + 86_400_000L
        assertFalse(DateUtils.isOverdue(tomorrow))
    }

    @Test
    fun toRelativeLabel_today_returnsToday() {
        val todayNoon = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        assertEquals("Today", DateUtils.toRelativeLabel(todayNoon))
    }

    @Test
    fun toRelativeLabel_tomorrow_returnsTomorrow() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }
        assertEquals("Tomorrow", DateUtils.toRelativeLabel(cal.timeInMillis))
    }

    @Test
    fun roundTrip_inputStringAndParse() {
        val now  = System.currentTimeMillis()
        val str  = DateUtils.toInputString(now)
        val back = DateUtils.parseInputDate(str)
        // Should be within one day (time-of-day stripped)
        assertNotNull(back)
        assertTrue(Math.abs(back!! - now) < 86_400_000L)
    }
}
