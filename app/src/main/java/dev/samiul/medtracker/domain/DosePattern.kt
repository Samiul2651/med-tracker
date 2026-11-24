package dev.samiul.medtracker.domain

/** A time of day a dose can be taken. Order = order shown on Today. */
enum class Slot { MORNING, NOON, NIGHT }

/**
 * Tablets per slot, as written on a prescription: `1+0+1` = 1 morning, 0 noon, 1 night.
 * FR-2.
 */
data class DosePattern(val morning: Double, val noon: Double, val night: Double) {

    init {
        require(listOf(morning, noon, night).all { it in 0.0..MAX_PER_SLOT }) {
            "Each slot must be between 0 and $MAX_PER_SLOT tablets"
        }
        require(perDay > 0.0) { "At least one slot must have tablets" }
    }

    val perDay: Double get() = morning + noon + night

    fun tabletsFor(slot: Slot): Double = when (slot) {
        Slot.MORNING -> morning
        Slot.NOON -> noon
        Slot.NIGHT -> night
    }

    /** Slots with at least some tablets, in day order. */
    val slots: List<Slot> get() = Slot.entries.filter { tabletsFor(it) > 0.0 }

    /** Back to the `M+N+E` text, e.g. `1+0+0.5`. */
    override fun toString(): String =
        listOf(morning, noon, night).joinToString("+") { it.toPlainString() }

    companion object {
        const val MAX_PER_SLOT = 10.0

        private val NUMBER = Regex("""\d{1,2}(\.\d{1,2})?""")

        /**
         * Parses `M+N+E`. Spaces are ignored. Each part is a number like `1`, `0.5`, `2`.
         * Fails (with a user-readable message) on letters, wrong part count or `0+0+0`.
         */
        fun parse(text: String): Result<DosePattern> {
            val parts = text.replace(" ", "").split("+")
            if (parts.size != 3) {
                return Result.failure(IllegalArgumentException("Use the form 1+0+1 (morning+noon+night)"))
            }
            if (parts.any { !NUMBER.matches(it) }) {
                return Result.failure(IllegalArgumentException("Only numbers are allowed, e.g. 1+0+1 or 0.5+0+1"))
            }
            val (m, n, e) = parts.map { it.toDouble() }
            return runCatching { DosePattern(m, n, e) }
        }
    }
}

internal fun Double.toPlainString(): String =
    if (this == Math.floor(this)) toLong().toString() else toString()
