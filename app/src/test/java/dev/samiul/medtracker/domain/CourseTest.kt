package dev.samiul.medtracker.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CourseTest {

    private val dec1 = LocalDate.of(2026, 12, 1)

    @Test
    fun fr14_daysLeft_andCourseDoneAfterEnd() {
        val s = Schedule(start = dec1, lengthDays = 30)
        val dec30 = LocalDate.of(2026, 12, 30)

        assertEquals(CourseState.Running(daysLeft = 30, endDate = dec30), s.courseState(dec1))
        assertEquals(CourseState.Running(daysLeft = 1, endDate = dec30), s.courseState(dec30))
        assertEquals(CourseState.Done, s.courseState(LocalDate.of(2026, 12, 31)))
    }

    @Test
    fun fr14_noEnd_neverDone() {
        val s = Schedule(start = dec1, lengthDays = null)
        assertEquals(CourseState.Ongoing, s.courseState(dec1.plusYears(2)))
    }

    @Test
    fun fr14_beforeStart_showsNotStarted() {
        val s = Schedule(start = dec1, lengthDays = 5)
        assertEquals(CourseState.NotStarted(dec1), s.courseState(dec1.minusDays(1)))
    }
}
