package dev.samiul.medtracker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StockTest {

    private val dec1 = LocalDate.of(2026, 12, 1)
    private val twicePerDay = DosePattern.parse("1+0+1").getOrThrow()

    @Test
    fun fr15_taken_subtracts_undo_adds_skipNoChange() {
        val afterTaken = Stock.afterTaken(left = 20.0, tablets = 1.0)
        assertEquals(19.0, afterTaken, 0.0)
        assertEquals(20.0, Stock.afterUndo(left = afterTaken, tablets = 1.0), 0.0)
        // Skipped/Missed: no Stock call is made; count stays as is (wired in step 3).
    }

    @Test
    fun fr15_neverBelowZero() {
        assertEquals(0.0, Stock.afterTaken(left = 0.5, tablets = 1.0), 0.0)
    }

    @Test
    fun fr16_refill_addsTablets() {
        assertEquals(32.0, Stock.afterRefill(left = 2.0, added = 30.0), 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun fr16_refill_rejectsZero() {
        Stock.afterRefill(left = 2.0, added = 0.0)
    }

    @Test
    fun fr17_buyMore_whenUnderThreeDays_andCourseNeedsMore() {
        // spec: 1+0+1, count 5, 20 days left -> 3 days need 6 tablets, 5 < 6 -> warn
        val s = Schedule(start = dec1, lengthDays = 20)
        assertTrue(Stock.needsRefill(left = 5.0, pattern = twicePerDay, schedule = s, today = dec1))
    }

    @Test
    fun fr17_noWarning_whenEnoughForThreeDays() {
        val s = Schedule(start = dec1, lengthDays = 20)
        assertFalse(Stock.needsRefill(left = 6.0, pattern = twicePerDay, schedule = s, today = dec1))
    }

    @Test
    fun fr17_noWarning_whenEnoughToFinishCourse() {
        // 1 day left needs 2 tablets; 2 left is enough even though 3 days would need 6
        val s = Schedule(start = dec1, lengthDays = 1)
        assertFalse(Stock.needsRefill(left = 2.0, pattern = twicePerDay, schedule = s, today = dec1))
    }

    @Test
    fun fr17_noCountSet_noWarning() {
        val s = Schedule(start = dec1)
        assertFalse(Stock.needsRefill(left = null, pattern = twicePerDay, schedule = s, today = dec1))
    }

    @Test
    fun fr17_everyNDays_countsOnlyDueDays() {
        // 1 tablet every 3 days; next 3 days have 1 due day -> 1 tablet is enough
        val once = DosePattern.parse("1+0+0").getOrThrow()
        val s = Schedule(start = dec1, everyNDays = 3)
        assertFalse(Stock.needsRefill(left = 1.0, pattern = once, schedule = s, today = dec1))
        assertTrue(Stock.needsRefill(left = 0.0, pattern = once, schedule = s, today = dec1))
    }
}
