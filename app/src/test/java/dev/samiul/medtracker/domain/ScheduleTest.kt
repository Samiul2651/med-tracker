package dev.samiul.medtracker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ScheduleTest {

    private val dec1 = LocalDate.of(2026, 12, 1)

    @Test
    fun fr3_everyThreeDays_dueOnStartAndEveryThird() {
        val s = Schedule(start = dec1, everyNDays = 3)
        assertTrue(s.isDue(dec1))
        assertFalse(s.isDue(dec1.plusDays(1))) // 2 Dec
        assertFalse(s.isDue(dec1.plusDays(2))) // 3 Dec
        assertTrue(s.isDue(dec1.plusDays(3)))  // 4 Dec
        assertTrue(s.isDue(dec1.plusDays(6)))  // 7 Dec
    }

    @Test
    fun fr3_everyDay_dueEachDayInCourse_notBeforeOrAfter() {
        val s = Schedule(start = dec1, lengthDays = 30)
        assertFalse(s.isDue(dec1.minusDays(1)))
        assertTrue(s.isDue(dec1))
        assertTrue(s.isDue(LocalDate.of(2026, 12, 30)))
        assertFalse(s.isDue(LocalDate.of(2026, 12, 31)))
    }

    @Test
    fun fr3_threeDosesThreeDaysApart_viaLengthSeven() {
        // spec concern 5: every 3 days + length 7 -> days 1, 4, 7
        val s = Schedule(start = dec1, lengthDays = 7, everyNDays = 3)
        assertEquals(
            listOf(dec1, dec1.plusDays(3), dec1.plusDays(6)),
            s.dueDays(dec1, dec1.plusDays(20)),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun fr3_rejectsEveryNOver30() {
        Schedule(start = dec1, everyNDays = 31)
    }

    @Test(expected = IllegalArgumentException::class)
    fun fr3_rejectsEveryNBelow1() {
        Schedule(start = dec1, everyNDays = 0)
    }
}
