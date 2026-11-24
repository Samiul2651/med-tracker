package dev.samiul.medtracker.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Which days a medicine is due.
 * - Course runs from [start] for [lengthDays] days (null = no end).  FR-1, FR-14
 * - Within the course, due every [everyNDays] days counted from [start]
 *   (1 = every day).  FR-3
 */
data class Schedule(
    val start: LocalDate,
    val lengthDays: Int? = null,
    val everyNDays: Int = 1,
) {
    init {
        require(everyNDays in 1..MAX_EVERY_N_DAYS) { "Repeat must be every 1 to $MAX_EVERY_N_DAYS days" }
        require(lengthDays == null || lengthDays >= 1) { "Course length must be at least 1 day" }
    }

    /** Last day of the course, or null if it never ends. */
    val endDate: LocalDate? get() = lengthDays?.let { start.plusDays(it - 1L) }

    /** True if [date] is inside the course (start..end). */
    fun inCourse(date: LocalDate): Boolean =
        !date.isBefore(start) && endDate.let { it == null || !date.isAfter(it) }

    /** True if a dose is due on [date]. */
    fun isDue(date: LocalDate): Boolean =
        inCourse(date) && ChronoUnit.DAYS.between(start, date) % everyNDays == 0L

    /** Due days from [from] to [to] inclusive (stops at course end). */
    fun dueDays(from: LocalDate, to: LocalDate): List<LocalDate> {
        val last = endDate?.let { if (it.isBefore(to)) it else to } ?: to
        if (last.isBefore(from)) return emptyList()
        return generateSequence(from) { it.plusDays(1) }
            .takeWhile { !it.isAfter(last) }
            .filter { isDue(it) }
            .toList()
    }

    companion object {
        const val MAX_EVERY_N_DAYS = 30
    }
}
