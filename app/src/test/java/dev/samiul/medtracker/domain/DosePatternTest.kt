package dev.samiul.medtracker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DosePatternTest {

    @Test
    fun fr2_parse_1plus0plus1() {
        val p = DosePattern.parse("1+0+1").getOrThrow()
        assertEquals(1.0, p.morning, 0.0)
        assertEquals(0.0, p.noon, 0.0)
        assertEquals(1.0, p.night, 0.0)
        assertEquals(listOf(Slot.MORNING, Slot.NIGHT), p.slots)
        assertEquals(2.0, p.perDay, 0.0)
    }

    @Test
    fun fr2_parse_halfTablet() {
        val p = DosePattern.parse("0.5 + 0 + 1").getOrThrow()
        assertEquals(0.5, p.morning, 0.0)
        assertEquals(1.5, p.perDay, 0.0)
        assertEquals("0.5+0+1", p.toString())
    }

    @Test
    fun fr2_rejectsLettersAndAllZero() {
        listOf("1+x+1", "0+0+0", "1+1", "1+0+1+1", "", "+1+1", "-1+0+1", "11+0+0")
            .forEach { input ->
                assertTrue("'$input' should be rejected", DosePattern.parse(input).isFailure)
            }
    }

    @Test
    fun fr2_rejectionHasReadableMessage() {
        val message = DosePattern.parse("1+x+1").exceptionOrNull()?.message.orEmpty()
        assertTrue(message, message.isNotBlank())
    }
}
