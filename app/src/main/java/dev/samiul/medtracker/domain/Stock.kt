package dev.samiul.medtracker.domain

import java.time.LocalDate

/** Tablet-count rules. FR-15, FR-16, FR-17. */
object Stock {

    /** Days of doses the "Buy more" warning looks ahead (today + 2 more days). */
    const val WARN_DAYS = 3

    /** FR-15: Taken subtracts; never below 0. */
    fun afterTaken(left: Double, tablets: Double): Double = (left - tablets).coerceAtLeast(0.0)

    /** FR-15: Undo of a Taken adds the tablets back. */
    fun afterUndo(left: Double, tablets: Double): Double = left + tablets

    /** FR-16: refill adds tablets. */
    fun afterRefill(left: Double, added: Double): Double {
        require(added > 0.0) { "Add at least 1 tablet" }
        return left + added
    }

    /** Tablets needed for every due dose from [from] to [to] (inclusive, within the course). */
    fun tabletsNeeded(pattern: DosePattern, schedule: Schedule, from: LocalDate, to: LocalDate): Double =
        schedule.dueDays(from, to).size * pattern.perDay

    /**
     * FR-17: warn when tablets left are fewer than the next [WARN_DAYS] days need
     * AND fewer than the rest of the course needs. No count set → no warning.
     *
     * Counts all of today's doses, so after a morning dose is taken the check is a
     * little early, never late.
     */
    fun needsRefill(left: Double?, pattern: DosePattern, schedule: Schedule, today: LocalDate): Boolean {
        if (left == null) return false
        val nextDays = tabletsNeeded(pattern, schedule, today, today.plusDays(WARN_DAYS - 1L))
        val restOfCourse = schedule.endDate
            ?.let { tabletsNeeded(pattern, schedule, today, it) }
            ?: Double.POSITIVE_INFINITY
        return left < nextDays && left < restOfCourse
    }
}
