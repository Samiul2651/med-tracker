package dev.samiul.medtracker.domain

import java.time.LocalDate

/** What the user saved for a dose. Only these two are stored (plan D4). */
enum class LoggedStatus { TAKEN, SKIPPED }

/** What a dose shows on Today and History. */
enum class DoseStatus { PENDING, TAKEN, SKIPPED, MISSED }

/**
 * FR-7, FR-8: a saved status wins; otherwise a dose on a past day is Missed,
 * and a dose today (even after its slot time) or later is still Pending.
 */
fun doseStatus(doseDate: LocalDate, today: LocalDate, logged: LoggedStatus?): DoseStatus =
    when (logged) {
        LoggedStatus.TAKEN -> DoseStatus.TAKEN
        LoggedStatus.SKIPPED -> DoseStatus.SKIPPED
        null -> if (doseDate.isBefore(today)) DoseStatus.MISSED else DoseStatus.PENDING
    }
