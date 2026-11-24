package dev.samiul.medtracker.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DoseStatusTest {

    private val today = LocalDate.of(2026, 12, 10)

    @Test
    fun fr8_unmarkedPastDose_isMissed() {
        assertEquals(DoseStatus.MISSED, doseStatus(today.minusDays(1), today, logged = null))
    }

    @Test
    fun fr8_todayPastSlot_isStillPending() {
        // Status depends on the day only: an unmarked dose today stays Pending until the day ends.
        assertEquals(DoseStatus.PENDING, doseStatus(today, today, logged = null))
    }

    @Test
    fun fr8_savedStatusWinsOverMissed() {
        assertEquals(DoseStatus.TAKEN, doseStatus(today.minusDays(3), today, LoggedStatus.TAKEN))
        assertEquals(DoseStatus.SKIPPED, doseStatus(today.minusDays(3), today, LoggedStatus.SKIPPED))
    }
}
