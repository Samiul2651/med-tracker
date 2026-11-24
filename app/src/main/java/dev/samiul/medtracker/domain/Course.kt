package dev.samiul.medtracker.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** What the medicine card shows about its course. FR-14. */
sealed interface CourseState {
    /** Course has an end; [daysLeft] counts today. */
    data class Running(val daysLeft: Int, val endDate: LocalDate) : CourseState

    /** Course has not started yet. */
    data class NotStarted(val startDate: LocalDate) : CourseState

    /** No end date ("no end" medicine). */
    data object Ongoing : CourseState

    /** Past the end date: no doses, no reminders. */
    data object Done : CourseState
}

fun Schedule.courseState(today: LocalDate): CourseState {
    val end = endDate
    return when {
        today.isBefore(start) -> CourseState.NotStarted(start)
        end == null -> CourseState.Ongoing
        today.isAfter(end) -> CourseState.Done
        else -> CourseState.Running(
            daysLeft = (ChronoUnit.DAYS.between(today, end) + 1).toInt(),
            endDate = end,
        )
    }
}
