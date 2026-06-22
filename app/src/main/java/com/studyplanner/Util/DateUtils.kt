package com.studyplanner.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {

    private val dateFormat     = SimpleDateFormat("MM/dd/yyyy",         Locale.getDefault())
    private val displayFormat  = SimpleDateFormat("MMM dd, yyyy",       Locale.getDefault())
    private val shortFormat    = SimpleDateFormat("MMM dd",             Locale.getDefault())
    private val dayFormat      = SimpleDateFormat("EEEE, MMM dd",       Locale.getDefault())
    private val monthYearFmt   = SimpleDateFormat("MMMM yyyy",         Locale.getDefault())

    /** Parse MM/dd/yyyy → epoch ms, null on failure */
    fun parseInputDate(input: String): Long? =
        runCatching { dateFormat.parse(input.trim())?.time }.getOrNull()

    /** Format epoch ms → MM/dd/yyyy (for input fields) */
    fun toInputString(epochMs: Long): String = dateFormat.format(Date(epochMs))

    /** Format epoch ms → "MMM dd, yyyy" for display */
    fun toDisplayString(epochMs: Long): String = displayFormat.format(Date(epochMs))

    /** Short form "MMM dd" */
    fun toShortString(epochMs: Long): String = shortFormat.format(Date(epochMs))

    /** "Monday, Jun 22" */
    fun toDayString(epochMs: Long): String = dayFormat.format(Date(epochMs))

    /** "June 2026" */
    fun toMonthYear(epochMs: Long): String = monthYearFmt.format(Date(epochMs))

    /** True if epochMs falls within today (local time) */
    fun isToday(epochMs: Long): Boolean {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val tomorrow = today.clone() as Calendar
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        return epochMs >= today.timeInMillis && epochMs < tomorrow.timeInMillis
    }

    /** True if epochMs is in the past (before start of today) */
    fun isOverdue(epochMs: Long): Boolean {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return epochMs < startOfDay
    }

    /** Human-readable relative label: "Today", "Tomorrow", "In 3 days", "3 days ago" */
    fun toRelativeLabel(epochMs: Long): String {
        val now = System.currentTimeMillis()
        val diff = epochMs - now
        val absDays = TimeUnit.MILLISECONDS.toDays(Math.abs(diff))
        return when {
            isToday(epochMs)        -> "Today"
            diff > 0 && absDays < 2 -> "Tomorrow"
            diff > 0 && absDays < 8 -> "In $absDays days"
            diff < 0 && absDays < 2 -> "Yesterday"
            diff < 0                -> "$absDays days ago"
            else                    -> toShortString(epochMs)
        }
    }

    /** Start of today in epoch ms */
    fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /** Start of the current week (Sun) in epoch ms */
    fun startOfWeek(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    }.timeInMillis

    /** Start of the current month in epoch ms */
    fun startOfMonth(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        set(Calendar.DAY_OF_MONTH, 1)
    }.timeInMillis

    /** "02:30" or "1:02:30" from seconds */
    fun formatElapsedTime(totalSeconds: Int): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    /** "45m" or "1h 30m" from minutes */
    fun formatMinutes(minutes: Int): String {
        if (minutes <= 0) return "0m"
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0 -> "${m}m"
            m == 0 -> "${h}h"
            else   -> "${h}h ${m}m"
        }
    }
}
